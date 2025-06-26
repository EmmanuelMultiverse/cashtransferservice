package com.cashtransfer.main.model;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PeerTransferRequest extends TransferRequest {

	@NotNull(message = "Need receiving username.")
	private String receivingUsername;

	public PeerTransferRequest(String receivingUsername, BigDecimal amount, String transferType) {
		super(amount, transferType);
		this.receivingUsername = receivingUsername;
	}

}
