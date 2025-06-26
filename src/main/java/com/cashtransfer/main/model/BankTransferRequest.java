package com.cashtransfer.main.model;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BankTransferRequest extends TransferRequest {

	@NotNull(message = "Need account number.")
	private String accountNumber;

	public BankTransferRequest(String accountNumber, BigDecimal amount, String transferType) {
		super(amount, transferType);
		this.accountNumber = accountNumber;
	}

}
