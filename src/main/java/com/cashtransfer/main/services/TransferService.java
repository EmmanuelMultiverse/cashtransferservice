package com.cashtransfer.main.services;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cashtransfer.main.clients.MultibankExternalClient;
import com.cashtransfer.main.clients.VersebankClient;
import com.cashtransfer.main.controllers.TransactionController;
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

    private final TransactionController transactionController;

	private final AccountRepository accountRepository;

	private final AuthService authService;

	private final UserRepository userRepository;

	private final TransactionService transactionService;

	private final MultibankExternalClient multibankExternalClient;

	private final VersebankClient versebankClient;

	public TransferService(VersebankClient versebankClient, MultibankExternalClient multibankExternalClient,
			TransactionService transactionService, AccountRepository accountRepository, UserRepository userRepository,
			AuthService authService, TransactionController transactionController) {
		this.multibankExternalClient = multibankExternalClient;
		this.versebankClient = versebankClient;
		this.transactionService = transactionService;
		this.accountRepository = accountRepository;
		this.userRepository = userRepository;
		this.authService = authService;
		this.transactionController = transactionController;
	}

	@Transactional
	public TransferResponse transferMoneyToPeer(PeerTransferRequest transferRequest) {
        User sendingUser = getSendingUser();
        User receivingUser = getUser(transferRequest.getReceivingUsername());

        Account sendingAccount = getSendingUserAccountAndValidate(sendingUser.getId(), transferRequest.getAmount(), transferRequest.getTransferType());
        Account receivingAccount = getAccount(receivingUser.getId());

		processTransferBetweenAccounts(sendingAccount, receivingAccount, transferRequest.getAmount());
		processTransaction(transferRequest.getAmount(), sendingUser.getId(), receivingUser.getId(), sendingAccount.getAccountNumber(), receivingAccount.getAccountNumber());

		return new TransferResponse(transferRequest.getAmount(), getAccount(sendingUser.getId()), "Transfer successful");
	}

	@Transactional
	public TransferResponse transferCashToBankAccount(BankTransferRequest transferRequest) {
		User user = getSendingUser();
		Account userAccount = getSendingUserAccountAndValidate(user.getId(), transferRequest.getAmount(), transferRequest.getTransferType());

        withdrawalFundsFromAccount(userAccount.getId(), userAccount.getBalance(), transferRequest.getAmount());

		String externalTransferMessage = executeExternalTransfer(transferRequest);
        
		validateExternalTransferResult(externalTransferMessage);

		return prepareTransferResponse(user.getId(), transferRequest.getAmount(), externalTransferMessage);
	}

	@Transactional
	public TransferResponse transferCashToMulticashAccount(BankTransferRequest transferRequest) {
		User user = getSendingUser();
		Account account = getAccount(user.getId());

		depositFundsIntoAccount(account.getId(), account.getBalance(), transferRequest.getAmount());

		String externalResponse = executeExternalTransfer(transferRequest);

		validateExternalTransferResult(externalResponse);

		return prepareTransferResponse(user.getId(), transferRequest.getAmount(), externalResponse);
	}

	private Account getSendingUserAccountAndValidate(Long id, BigDecimal amount, String transferType) {

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

	private void processTransferBetweenAccounts(Account sendingAccount, Account receivingAccount, BigDecimal amount) {
		BigDecimal sendingNewBalance = sendingAccount.getBalance().subtract(amount);
		BigDecimal receivingNewBalance = sendingAccount.getBalance().add(amount);

		accountRepository.setBalance(sendingAccount.getId(), sendingNewBalance);
		accountRepository.setBalance(receivingAccount.getId(), receivingNewBalance);
	}

	private void processTransaction(BigDecimal amount, Long sendingUserId, Long receivingUserId, String sendingAccountNumber, String receivingAccountNumber) {
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
			VersebankClientRequest req = new VersebankClientRequest(transferRequest.getAccountNumber(), transferRequest.getAmount());
			if (transferRequest.getTransferType().equalsIgnoreCase("DEPOSIT")) {
				return versebankClient.transferCashToMulticashAccount(req).getMessage();
			} else {
				return versebankClient.transferCashToBank(req).getMessage();
			}
        } else {
            if (transferRequest.getTransferType().equalsIgnoreCase("DEPOSIT")) {
				return multibankExternalClient.transferToMulticashAccount(transferRequest);
			} else {
				return multibankExternalClient.depositToAccount(transferRequest);
			}
        }
    }

    private void validateExternalTransferResult(String res) {
        if (!res.toLowerCase().contains("succesful"));
            throw new RuntimeException("Transfer Failed.");
    }

    private TransferResponse prepareTransferResponse(Long userId, BigDecimal transferAmount, String message) {
        TransferResponse transferResponse = new TransferResponse();

        Account updatedAccount = getAccount(userId);
        
        transferResponse.setAccount(updatedAccount);
        transferResponse.setMessage(message);
        transferResponse.setTransferAmount(transferAmount);
        
        return transferResponse;
    }
}
