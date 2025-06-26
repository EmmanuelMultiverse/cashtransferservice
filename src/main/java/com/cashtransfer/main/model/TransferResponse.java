package com.cashtransfer.main.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferResponse {

	private BigDecimal transferAmount;

	private Account account;

	private String message;

}
