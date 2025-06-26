package com.cashtransfer.main.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cashtransfer.main.model.Transaction;
import com.cashtransfer.main.model.User;
import com.cashtransfer.main.services.AuthService;
import com.cashtransfer.main.services.TransactionService;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

	private final TransactionService transactionService;

	private final AuthService authService;

	public TransactionController(AuthService authService, TransactionService transactionService) {
		this.transactionService = transactionService;
		this.authService = authService;
	}

	@GetMapping
	public List<Transaction> getTransactions() {
		User authenticatedUser = authService.getCurrentAuthenticatedUser();

		return transactionService.getTransactionsById(authenticatedUser.getId());
	}

}
