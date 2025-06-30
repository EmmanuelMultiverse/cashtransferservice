package com.cashtransfer.main.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cashtransfer.main.model.AuthRequest;
import com.cashtransfer.main.model.AuthResponse;
import com.cashtransfer.main.model.RegistrationResponse;
import com.cashtransfer.main.services.AuthService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	public ResponseEntity<?> registerUser(@RequestBody AuthRequest authRequest) {
		
		log.info(String.format("Registering new user: {}", authRequest.getUsername()));

		try {
			RegistrationResponse res = authService.registeUser(authRequest);
			return ResponseEntity.ok(res);
		}
		catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		catch (Exception e) {
			return ResponseEntity.internalServerError().body("Registration Failed " + e.getMessage());
		}

	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> createAuthenticationToken(@RequestBody AuthRequest authRequest) {
		log.info("Logging in user: {}", authRequest.getUsername());
		try {
			String jwt = authService.authenticateAndGenerateToken(authRequest);
			return ResponseEntity.ok(new AuthResponse(jwt, null));
		}
		catch (BadCredentialsException e) {
			return ResponseEntity.status(401).body(new AuthResponse(null, e.getMessage()));
		}
	}

}
