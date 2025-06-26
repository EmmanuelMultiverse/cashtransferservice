package com.cashtransfer.main.util;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwsAuthFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;

	private final UserDetailsService userDetailsService;

	public JwsAuthFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		final String authHeader = request.getHeader("Authorization");
		final String username;
		final String jwt;

		System.out.println("--- JwsAuthFilter: START processing request for " + request.getRequestURI() + " ---");
		System.out.println("Authorization Header: " + authHeader);

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			System.out.println("JwsAuthFilter: No Bearer token found. Skipping filter.");

			filterChain.doFilter(request, response);
			return;
		}

		jwt = authHeader.substring(7);
		username = jwtUtil.extractUsername(jwt);
		System.out.println("JwsAuthFilter: JWT extracted: " + jwt.substring(0, Math.min(jwt.length(), 20)) + "..."); // Print
																														// first
																														// 20
																														// chars
		System.out.println("JwsAuthFilter: Username extracted from JWT: " + username);

		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			var userDetails = this.userDetailsService.loadUserByUsername(username);
			System.out.println("JwsAuthFilter: Username is valid (" + username
					+ ") and SecurityContext is null. Attempting to load UserDetails.");

			if (jwtUtil.validateToken(jwt, userDetails)) {
				var authToken = new UsernamePasswordAuthenticationToken(userDetails, null,
						userDetails.getAuthorities());
				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authToken);
			}
		}

		filterChain.doFilter(request, response);
	}

}
