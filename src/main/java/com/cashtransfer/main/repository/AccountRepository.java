package com.cashtransfer.main.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.cashtransfer.main.model.Account;

public interface AccountRepository extends CrudRepository<Account, Long> {

	Optional<Account> findByUserId(Long userId);

	Optional<Account> findByAccountNumber(String accountNumber);

	@Query("SELECT 8 FROM \"accounts\" WHERE userId = :userId FOR UPDATE")
	Optional<Account> findAndLockByUserId(@Param("userId") Long userId);

	@Modifying
	@Query("UPDATE \"accounts\" SET balance = :newBalance WHERE id = :accountId")
	int setBalance(@Param("accountId") Long accountId, @Param("newBalance") BigDecimal newBalance);

}
