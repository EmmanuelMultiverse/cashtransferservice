package com.cashtransfer.main.services;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cashtransfer.main.model.Account;
import com.cashtransfer.main.model.User;
import com.cashtransfer.main.repository.AccountRepository;
import com.cashtransfer.main.repository.UserRepository;

@Service
public class AccountService {

	private final AccountRepository accountRepository;

	private final UserRepository userRepository;

	public AccountService(AccountRepository accountRepository, UserRepository userRepository) {
		this.accountRepository = accountRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public Account createInitialAccountForUser(User user, String accountType) {
		if (accountRepository.findByUserId(user.getId()).isPresent()) {
			throw new IllegalStateException("User: " + user.getUsername() + " already has an account.");
		}

		String newAccountNumber = UUID.randomUUID().toString().replace("-", "").substring(0, 10);

		while (accountRepository.findByAccountNumber(newAccountNumber).isPresent()) {
			newAccountNumber = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
		}

		var account = new Account();
		account.setAccountNumber(newAccountNumber);
		account.setAccountType(accountType);
		account.setBalance(BigDecimal.ZERO);
		account.setUserId(user.getId());

		user.setAccount(account);
		userRepository.save(user);

		return account;
	}

}
