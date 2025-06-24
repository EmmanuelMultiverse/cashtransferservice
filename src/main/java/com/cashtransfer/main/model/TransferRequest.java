package com.cashtransfer.main.model;

import java.math.BigDecimal;

import org.springframework.data.repository.NoRepositoryBean;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoRepositoryBean
public class TransferRequest {
    private final String receivingUsername;
    private final BigDecimal amount;

}
