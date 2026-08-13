package br.com.elitedev.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.elitedev.dto.auth.LoginRequest;
import br.com.elitedev.dto.auth.LoginResponse;
import br.com.elitedev.service.AuthService;
import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;

import br.com.elitedev.dto.auth.CurrentUserResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

		return ResponseEntity.ok(authService.login(request));
	}

	@GetMapping("/me")
	public ResponseEntity<CurrentUserResponse> me(Authentication authentication) {

		String role = authentication.getAuthorities().stream().findFirst()
				.map(authority -> authority.getAuthority().replace("ROLE_", "")).orElse("");

		return ResponseEntity.ok(new CurrentUserResponse(authentication.getName(), role));
	}
}