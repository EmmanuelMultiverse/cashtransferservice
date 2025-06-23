package com.cashtransfer.main.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.cashtransfer.main.model.Account;

public interface AccountRepository extends CrudRepository<Account, Long> {
    Optional<Account> findByUserId(Long userId);

    Optional<Account> findByAccountNumber(String accountNumber);
}
