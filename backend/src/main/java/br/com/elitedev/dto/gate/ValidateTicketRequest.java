package br.com.elitedev.dto.gate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ValidateTicketRequest(
        @NotBlank(message = "Código do ingresso é obrigatório.")
        String token,

        @NotNull(message = "Evento é obrigatório.")
        @Min(value = 1, message = "Evento inválido.")
        Long eventId
) {
}
