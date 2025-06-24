package com.cashtransfer.main.repository;

import java.util.List;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.cashtransfer.main.model.Transaction;

public interface TransactionRepository extends CrudRepository<Transaction, Long> {

    @Query("SELECT * FROM \"transactions\" WHERE \"user_id\" = :userId")
    List<Transaction> findTransactionsByUserId(@Param("userId") Long userId);
}
