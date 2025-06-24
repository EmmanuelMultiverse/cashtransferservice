package com.cashtransfer.main.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransferResponse {
    private final BigDecimal transferAmount;
    private final Account account;
}
