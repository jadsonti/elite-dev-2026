package br.com.elitedev.dto.auth;

public record LoginResponse(

        String token,
        String type,
        Long userId,
        String name,
        String email,
        String role

) {
}