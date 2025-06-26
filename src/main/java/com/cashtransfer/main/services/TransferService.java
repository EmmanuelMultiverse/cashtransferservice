package com.cashtransfer.main.services;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cashtransfer.main.clients.MultibankExternalClient;
import com.cashtransfer.main.clients.VersebankClient;
import com.cashtransfer.main.model.Account;
import com.cashtransfer.main.model.BankTransferRequest;
import com.cashtransfer.main.model.PeerTransferRequest;
import com.cashtransfer.main.model.Transaction;
import com.cashtransfer.main.model.TransferRequest;
import com.cashtransfer.main.model.TransferResponse;
import com.cashtransfer.main.model.User;
import com.cashtransfer.main.model.VersebankClientRequest;
import com.cashtransfer.main.repository.AccountRepository;
import com.cashtransfer.main.repository.UserRepository;

import java.time.LocalDateTime;

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
	public TransferResponse transferMoney(PeerTransferRequest transferRequest) {

		User authenticatedUser = authService.getCurrentAuthenticatedUser();
		Account sendingAccount = getCurrentUserAccountAndValidate(authenticatedUser.getId(),
				transferRequest.getAmount(), transferRequest.getTransferType());

		User receivingUser = getUser(transferRequest.getReceivingUsername());
		Account receivingAccount = getAccount(receivingUser.getId());

		processTransfer(sendingAccount, receivingAccount, transferRequest.getAmount());

		processTransactions(sendingAccount.getAccountNumber(), receivingAccount.getAccountNumber(),
				transferRequest.getAmount(), authenticatedUser.getId(), receivingUser.getId());

		Account accountAfterTransfer = getAccount(authenticatedUser.getId());

		return new TransferResponse(transferRequest.getAmount(), accountAfterTransfer, "");
	}

	@Transactional
	public TransferResponse transferCashToBankAccount(BankTransferRequest transferRequest) {
		User authenticatedUser = authService.getCurrentAuthenticatedUser();
		Account userAccount = accountRepository.findByUserId(authenticatedUser.getId())
			.orElseThrow(() -> new RuntimeException(""));

		BigDecimal newBalance = userAccount.getBalance().subtract(transferRequest.getAmount());

		if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
			throw new RuntimeException("Not enough funds");
		}

		accountRepository.setBalance(userAccount.getId(), newBalance);

		TransferResponse transferResponse = new TransferResponse();

		if (transferRequest.getAccountNumber().length() > 6) {
			VersebankClientRequest req = new VersebankClientRequest(transferRequest.getAccountNumber(),
					transferRequest.getAmount());
			transferResponse.setMessage(versebankClient.transferCashToBank(req).getMessage());
		}
		else {
			transferResponse.setMessage(multibankExternalClient.depositToAccount(transferRequest));
		}
		System.out.println(transferResponse.getMessage());
		if (!transferResponse.getMessage().toLowerCase().equals("deposit successful")) {
			throw new RuntimeException("Deposit failed");
		}

		Account updatedAccount = accountRepository.findById(userAccount.getId())
			.orElseThrow(() -> new RuntimeException("Could not find user account"));
		transferResponse.setAccount(updatedAccount);
		transferResponse.setTransferAmount(transferRequest.getAmount());
		return transferResponse;
	}

	@Transactional
	public TransferResponse transferCashToMulticashAccount(BankTransferRequest transferRequest) {

		User user = authService.getCurrentAuthenticatedUser();
		Account account = accountRepository.findByUserId(user.getId()).orElseThrow(() -> new RuntimeException(""));

		TransferResponse transferResponse = new TransferResponse();

		if (transferRequest.getAccountNumber().length() > 6) {
			VersebankClientRequest req = new VersebankClientRequest(transferRequest.getAccountNumber(),
					transferRequest.getAmount());
			transferResponse.setMessage(versebankClient.transferCashToMulticashAccount(req).getMessage());
		}
		else {
			transferResponse.setMessage(multibankExternalClient.transferToMulticashAccount(transferRequest));
		}

		if (!transferResponse.getMessage().toLowerCase().equals("withdrawal successful")) {
			throw new RuntimeException("Transfer to MultiCash account failed");
		}

		BigDecimal newBalance = account.getBalance().add(transferRequest.getAmount());

		accountRepository.setBalance(account.getId(), newBalance);

		Account updatedAccount = accountRepository.findById(account.getId())
			.orElseThrow(() -> new RuntimeException("Could not find account"));

		transferResponse.setAccount(updatedAccount);
		transferResponse.setTransferAmount(transferRequest.getAmount());

		return transferResponse;
	}

	private Account getCurrentUserAccountAndValidate(Long id, BigDecimal amount, String transferType) {

		Account authenticatedUserAccount = accountRepository.findByUserId(id)
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

	private Account getAccount(Long userId) {
		return accountRepository.findByUserId(userId)
			.orElseThrow(() -> new RuntimeException(String.format("Could not find account for userId: %d", userId)));
	}

	private void processTransfer(Account sendingAccount, Account receivingAccount, BigDecimal amount) {
		BigDecimal sendingNewBalance = sendingAccount.getBalance().subtract(amount);
		BigDecimal receivingNewBalance = sendingAccount.getBalance().add(amount);

		accountRepository.setBalance(sendingAccount.getId(), sendingNewBalance);
		accountRepository.setBalance(receivingAccount.getId(), receivingNewBalance);
	}

	private void processTransactions(String sendingAccountNumber, String receivingAccountNumber, BigDecimal amount,
			Long userId, Long receivingUserId) {
		Transaction userTransaction = new Transaction(null, sendingAccountNumber, receivingAccountNumber, amount,
				LocalDateTime.now(), userId);
		Transaction sendingUserTransaction = new Transaction(null, sendingAccountNumber, receivingAccountNumber, amount,
				LocalDateTime.now(), receivingUserId);

		transactionService.storeTransaction(userTransaction);
		transactionService.storeTransaction(sendingUserTransaction);
	}

}
