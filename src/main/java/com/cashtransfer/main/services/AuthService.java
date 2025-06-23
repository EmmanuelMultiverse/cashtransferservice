package com.cashtransfer.main.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cashtransfer.main.model.Account;
import com.cashtransfer.main.model.AuthRequest;
import com.cashtransfer.main.model.RegistrationResponse;
import com.cashtransfer.main.model.User;
import com.cashtransfer.main.repository.UserRepository;
import com.cashtransfer.main.util.JwtUtil;

@Service
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

        var newUser = new User(
            null,
            authRequest.getUsername(),
            passwordEncoder.encode(authRequest.getPassword()),
            "USER",
            null
        );
        
        var savedUser = userRepository.save(newUser);

        var initialAccount = accountService.createInitialAccountForUser(savedUser, "CHECKING");
        
        return new RegistrationResponse(
            savedUser.getUsername(),
            "User registered successfully",
            initialAccount
        );
    }

    public String authenticateAndGenerateToken(AuthRequest authRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid username or password", e);
        }

        var userDetails = (UserDetails) authentication.getPrincipal();

        return jwtUtil.generateToken(userDetails);
    }



}   
