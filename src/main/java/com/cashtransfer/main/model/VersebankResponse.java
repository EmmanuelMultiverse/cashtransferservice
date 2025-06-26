package com.cashtransfer.main.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VersebankResponse {

	private String message;

	private String account_number;

	private String new_balance;

}
