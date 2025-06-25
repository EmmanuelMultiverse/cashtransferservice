package com.cashtransfer.main.model;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PeerTransferRequest extends TransferRequest {
    private String receivingUsername;

    public PeerTransferRequest(String receivingUsername, BigDecimal amount) {
        super(amount);
        this.receivingUsername = receivingUsername;
    }
}
