package com.cashtransfer.main.services;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.cashtransfer.main.model.AuthRequest;
import com.cashtransfer.main.model.RegistrationResponse;
import com.cashtransfer.main.model.User;
import com.cashtransfer.main.repository.UserRepository;
import com.cashtransfer.main.util.JwtUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthService {

	private final AuthenticationManager authenticationManager;

	private final AccountService accountService;

	private final JwtUtil jwtUtil;

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	public AuthService(AuthenticationManager authenticationManager, AccountService accountService, JwtUtil jwtUtil,
			UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.authenticationManager = authenticationManager;
		this.accountService = accountService;
		this.jwtUtil = jwtUtil;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public RegistrationResponse registeUser(AuthRequest authRequest) {
		if (userRepository.findByUsername(authRequest.getUsername()).isPresent()) {
			throw new IllegalArgumentException("Username already exists");
		}

		var newUser = new User(null, authRequest.getUsername(), passwordEncoder.encode(authRequest.getPassword()),
				"USER", null);

		log.info("Creating new user {}...", authRequest.getUsername());
		var savedUser = userRepository.save(newUser);
		log.info("Successfuly created new user {}!", authRequest.getUsername());


		log.info("Creating initial account for {}..", authRequest.getUsername());
		var initialAccount = accountService.createInitialAccountForUser(savedUser, "CHECKING");
		log.info("Successfuly created initial account for {}!", authRequest.getUsername());


		return new RegistrationResponse(savedUser.getUsername(), "User registered successfully", initialAccount);
	}

	public String authenticateAndGenerateToken(AuthRequest authRequest) {
		Authentication authentication;
		try {
			log.info("Authenticating user: {}", authRequest.getUsername());
			authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
		}
		catch (BadCredentialsException e) {
			throw new BadCredentialsException("Invalid username or password", e);
		}

		log.info("Successfuly authenticated user: {}", authRequest.getUsername());

		var userDetails = (UserDetails) authentication.getPrincipal();

		return jwtUtil.generateToken(userDetails);
	}

	public User getCurrentAuthenticatedUser() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authorized");
		}

		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String username = userDetails.getUsername();

		return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException());
	}

}
