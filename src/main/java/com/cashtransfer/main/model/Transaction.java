package com.cashtransfer.main.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Table("transactions")
public class Transaction {
    @Id
    private final Long id;
    private final String sendingAccountId;
    private final String receivingAccountId;
    private final BigDecimal transferAmount;
    private final LocalDateTime localDateTime;

    @Column("user_id")
    private Long userId;
}
