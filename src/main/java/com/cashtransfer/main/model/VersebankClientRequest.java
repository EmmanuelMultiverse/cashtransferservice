package com.cashtransfer.main.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VersebankClientRequest {
    private String account_number;
    private BigDecimal amount;
}
