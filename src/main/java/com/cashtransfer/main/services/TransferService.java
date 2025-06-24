package com.cashtransfer.main.services;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cashtransfer.main.model.Account;
import com.cashtransfer.main.model.Transaction;
import com.cashtransfer.main.model.TransferRequest;
import com.cashtransfer.main.model.TransferResponse;
import com.cashtransfer.main.model.User;
import com.cashtransfer.main.repository.AccountRepository;
import com.cashtransfer.main.repository.UserRepository;

import java.time.LocalDateTime;


@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final TransactionService transactionService;

    public TransferService(TransactionService transactionService, AccountRepository accountRepository, UserRepository userRepository, AuthService authService) {
        this.transactionService = transactionService;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @Transactional
    public TransferResponse transferMoney(TransferRequest transferRequest) {

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

        return new TransferResponse(transferRequest.getAmount(), accountAfterTransfer);
    }
}
