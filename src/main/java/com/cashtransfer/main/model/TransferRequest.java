package com.cashtransfer.main.model;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class TransferRequest {

	@NotNull(message = "Amount is required.")
	private BigDecimal amount;

	@NotNull(message = "Transfer type is required.")
	private String transferType;

	public TransferRequest(BigDecimal amount) {
		this.amount = amount;
	}

}
