package com.cashtransfer.main.model;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BankTransferRequest extends TransferRequest {

	private String accountNumber;

	public BankTransferRequest(String accountNumber, BigDecimal amount) {
		super(amount);
		this.accountNumber = accountNumber;
	}

}
