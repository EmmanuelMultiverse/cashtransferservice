package com.cashtransfer.main.model;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("accounts")
public class Account {
    @Id
    private Long id;
    
    private String accountNumber;
    private BigDecimal balance;
    private String accountType;

    @Column("user_id")
    private Long userId;
}
