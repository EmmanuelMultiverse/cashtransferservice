package com.cashtransfer.main.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {

	private String token;
	private String error;

	public AuthResponse(String token) {
		this.token = token;
	}

}
