package com.cashtransfer.main.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cashtransfer.main.model.Transaction;
import com.cashtransfer.main.repository.TransactionRepository;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void storeTransaction(Transaction transaction) {
        transactionRepository.save(transaction);
    }

    public List<Transaction> getTransactionsById(Long id) {
        
        return transactionRepository.findTransactionsByUserId(id);
    }
}
