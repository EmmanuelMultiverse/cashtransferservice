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
import com.cashtransfer.main.model.TransferResponse;
import com.cashtransfer.main.model.User;
import com.cashtransfer.main.model.VersebankClientRequest;
import com.cashtransfer.main.model.VersebankResponse;
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

    public TransferService(VersebankClient versebankClient, MultibankExternalClient multibankExternalClient, TransactionService transactionService, AccountRepository accountRepository, UserRepository userRepository, AuthService authService) {
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
        Account sendingAccount = accountRepository.findById(authenticatedUser.getId())
                                    .orElseThrow(() -> new RuntimeException());
        if (sendingAccount.getBalance().subtract(transferRequest.getAmount()).compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Not enough funds");
        }

        User receivingUser = userRepository.findByUsername(transferRequest.getReceivingUsername()).orElseThrow(() -> new RuntimeException());

        Account receivingAccount = accountRepository.findByUserId(receivingUser.getId())
                                    .orElseThrow(() -> new RuntimeException());
        
        BigDecimal sendingNewBalance = sendingAccount.getBalance().subtract(transferRequest.getAmount());
        BigDecimal receivingNewBalance = receivingAccount.getBalance().add(transferRequest.getAmount());

        accountRepository.setBalance(sendingAccount.getId(), sendingNewBalance);
        accountRepository.setBalance(receivingAccount.getId(), receivingNewBalance);

        Account accountAfterTransfer = accountRepository.findById(authenticatedUser.getId())
                                        .orElseThrow(() -> new RuntimeException());
        
        transactionService.storeTransaction(new Transaction(null, sendingAccount.getAccountNumber(), receivingAccount.getAccountNumber(), transferRequest.getAmount(), LocalDateTime.now(), authenticatedUser.getId()));
        transactionService.storeTransaction(new Transaction(null, sendingAccount.getAccountNumber(), receivingAccount.getAccountNumber(), transferRequest.getAmount(), LocalDateTime.now(), receivingUser.getId()));

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
            VersebankClientRequest req = new VersebankClientRequest(transferRequest.getAccountNumber(), transferRequest.getAmount());
            transferResponse.setMessage(versebankClient.transferCashToBank(req).getMessage());
        } else {
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
}
