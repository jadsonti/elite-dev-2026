package br.com.elitedev.dto.auth;

public record CurrentUserResponse(
        String email,
        String role
) {
}