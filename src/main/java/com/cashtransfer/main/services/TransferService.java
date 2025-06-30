package com.cashtransfer.main.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cashtransfer.main.clients.MultibankExternalClient;
import com.cashtransfer.main.clients.VersebankClient;
import com.cashtransfer.main.model.Account;
import com.cashtransfer.main.model.BankTransferRequest;
import com.cashtransfer.main.model.PeerTransferRequest;
import com.cashtransfer.main.model.Transaction;
import com.cashtransfer.main.model.TransferResponse;
import com.cashtransfer.main.model.User;
import com.cashtransfer.main.model.VersebankClientRequest;
import com.cashtransfer.main.repository.AccountRepository;
import com.cashtransfer.main.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TransferService {

	private final AccountRepository accountRepository;

	private final AuthService authService;

	private final UserRepository userRepository;

	private final TransactionService transactionService;

	private final MultibankExternalClient multibankExternalClient;

	private final VersebankClient versebankClient;

	public TransferService(VersebankClient versebankClient, MultibankExternalClient multibankExternalClient,
			TransactionService transactionService, AccountRepository accountRepository, UserRepository userRepository,
			AuthService authService) {
		this.multibankExternalClient = multibankExternalClient;
		this.versebankClient = versebankClient;
		this.transactionService = transactionService;
		this.accountRepository = accountRepository;
		this.userRepository = userRepository;
		this.authService = authService;
	}

	@Transactional
	public TransferResponse transferMoneyToPeer(PeerTransferRequest transferRequest) {
		log.info("Getting user account..");
		User sendingUser = getSendingUser();
		User receivingUser = getUser(transferRequest.getReceivingUsername());

		Account sendingAccount = getSendingUserAccountAndValidate(sendingUser.getId(), transferRequest.getAmount(),
				transferRequest.getTransferType());
		log.info("Succesfuly got user info!");
		
		Account receivingAccount = getAccountAndLock(receivingUser.getId());
		
		log.info("Processing peer transfer..");
		processTransferBetweenAccounts(sendingAccount, receivingAccount, transferRequest.getAmount());
		log.info("Succesfuly processed peer transfer!");

		log.info("Storing transaction..");
		processTransaction(transferRequest.getAmount(), sendingUser.getId(), receivingUser.getId(),
				sendingAccount.getAccountNumber(), receivingAccount.getAccountNumber());
		log.info("Succesfully managed to store transaction!");

		return prepareTransferResponse(sendingUser.getId(), transferRequest.getAmount(), sendingAccount, "Success");
	}

	@Transactional
	public TransferResponse transferCashToBankAccount(BankTransferRequest transferRequest) {
		log.info("Getting user info..");
		User user = getSendingUser();
		Account userAccount = getSendingUserAccountAndValidate(user.getId(), transferRequest.getAmount(),
				transferRequest.getTransferType());
		log.info("Successfuly got user info!");
		log.info("Processing withdrawal..");
		withdrawalFundsFromAccount(userAccount.getId(), userAccount.getBalance(), transferRequest.getAmount());
		
		log.info("Making call to external bank..");
		String externalTransferMessage;
		try {
			externalTransferMessage = executeExternalTransfer(transferRequest);
			validateExternalTransferResult(externalTransferMessage);
			log.info("External bank call complete!");
		} catch (Exception e) {
			throw new RuntimeException("External Bank call failed.");
		}

		return prepareTransferResponse(user.getId(), transferRequest.getAmount(), userAccount, externalTransferMessage);
	}

	@Transactional
	public TransferResponse transferCashToMulticashAccount(BankTransferRequest transferRequest) {
		log.info("Getting user info..");
		User user = getSendingUser();
		Account account = getAccountAndLock(user.getId());
		log.info("Succesfully got user info..");

		log.info("Processing deposit..");
		depositFundsIntoAccount(account.getId(), account.getBalance(), transferRequest.getAmount());

		log.info("Making call to external bank..");
		String externalResponse;
		try {
			externalResponse = executeExternalTransfer(transferRequest);
			validateExternalTransferResult(externalResponse);
			log.info("External bank call complete!");
		} catch (Exception e) {
			throw new RuntimeException("External bank call failed.");
		}
		
		return prepareTransferResponse(user.getId(), transferRequest.getAmount(), account, externalResponse);
	}

	private Account getSendingUserAccountAndValidate(Long id, BigDecimal amount, String transferType) {

		Account authenticatedUserAccount = accountRepository.findAndLockByUserId(id)
			.orElseThrow(() -> new RuntimeException("Could not find account for authenticated user."));

		// Rewrite to switch statement if too many checks are happening
		if (transferType.equalsIgnoreCase("PEER") || transferType.equalsIgnoreCase("WITHDRAWAL")) {
			if (authenticatedUserAccount.getBalance().compareTo(amount) < 0)
				throw new RuntimeException("Not enough funds for transfer");
		}

		return authenticatedUserAccount;
	}

	private User getUser(String username) {
		return userRepository.findByUsername(username)
			.orElseThrow(() -> new RuntimeException(String.format("Could not find username: %s", username)));
	}

	private Account getAccountAndLock(Long userId) {
		return accountRepository.findAndLockByUserId(userId)
			.orElseThrow(() -> new RuntimeException("Could not find account"));
	}

	private Account getAccount(Long userId) {
		return accountRepository.findByUserId(userId)
			.orElseThrow(() -> new RuntimeException(String.format("Could not find account for userId: %d", userId)));
	}

	private void processTransferBetweenAccounts(Account sendingAccount, Account receivingAccount, BigDecimal amount) {
		BigDecimal sendingNewBalance = sendingAccount.getBalance().subtract(amount);
		BigDecimal receivingNewBalance = receivingAccount.getBalance().add(amount); // Corrected from sendingAccount

		// Ensure a consistent lock order by always updating the account with the smaller ID first.
		Account first = sendingAccount.getId() < receivingAccount.getId() ? sendingAccount : receivingAccount;
		Account second = sendingAccount.getId() < receivingAccount.getId() ? receivingAccount : sendingAccount;

		// The balances are calculated before, so we just need to apply them in the correct order.
		BigDecimal firstNewBalance = first.equals(sendingAccount) ? sendingNewBalance : receivingNewBalance;
		BigDecimal secondNewBalance = second.equals(sendingAccount) ? sendingNewBalance : receivingNewBalance;

		accountRepository.setBalance(first.getId(), firstNewBalance);
		accountRepository.setBalance(second.getId(), secondNewBalance);
	}

	private void processTransaction(BigDecimal amount, Long sendingUserId, Long receivingUserId,
			String sendingAccountNumber, String receivingAccountNumber) {
		Transaction userTransaction = new Transaction(null, sendingAccountNumber, receivingAccountNumber, amount,
				LocalDateTime.now(), sendingUserId);
		Transaction sendingUserTransaction = new Transaction(null, sendingAccountNumber, receivingAccountNumber, amount,
				LocalDateTime.now(), receivingUserId);

		transactionService.storeTransaction(userTransaction);
		transactionService.storeTransaction(sendingUserTransaction);
	}

	private User getSendingUser() {
		User sendingUser = authService.getCurrentAuthenticatedUser();
		return sendingUser;
	}

	private void withdrawalFundsFromAccount(Long accountId, BigDecimal currentBalance, BigDecimal amount) {
		BigDecimal newBalance = currentBalance.subtract(amount);
		accountRepository.setBalance(accountId, newBalance);
	}

	private void depositFundsIntoAccount(Long accountId, BigDecimal currentBalance, BigDecimal amount) {
		BigDecimal newBalance = currentBalance.add(amount);
		accountRepository.setBalance(accountId, newBalance);
	}

	private String executeExternalTransfer(BankTransferRequest transferRequest) {
		if (transferRequest.getAccountNumber().length() > 6) {
			VersebankClientRequest req = new VersebankClientRequest(transferRequest.getAccountNumber(),
					transferRequest.getAmount());
			if (transferRequest.getTransferType().equalsIgnoreCase("DEPOSIT")) {
				return versebankClient.transferCashToMulticashAccount(req).getMessage();
			}
			else {
				return versebankClient.transferCashToBank(req).getMessage();
			}
		}
		else {
			if (transferRequest.getTransferType().equalsIgnoreCase("DEPOSIT")) {
				return multibankExternalClient.transferToMulticashAccount(transferRequest);
			}
			else {
				return multibankExternalClient.depositToAccount(transferRequest);
			}
		}
	}

	private void validateExternalTransferResult(String res) {
		if (!res.toLowerCase().contains("successful"))
			throw new RuntimeException(res);
	}

	private TransferResponse prepareTransferResponse(Long userId, BigDecimal transferAmount, Account accountBeforeTransfer, String message) {
		TransferResponse transferResponse = new TransferResponse();

		Account updatedAccount = getAccount(userId);

		transferResponse.setAccount(updatedAccount);
		transferResponse.setMessage(message);
		transferResponse.setTransferAmount(transferAmount);
		transferResponse.setAccountBefore(accountBeforeTransfer);
		return transferResponse;
	}

}
