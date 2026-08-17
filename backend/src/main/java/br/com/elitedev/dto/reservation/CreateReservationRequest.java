package br.com.elitedev.dto.reservation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateReservationRequest(
        @NotNull(message = "Evento é obrigatório.")
        @Min(value = 1, message = "Evento inválido.")
        Long eventId,

        @NotNull(message = "Quantidade é obrigatória.")
        @Min(value = 1, message = "Quantidade deve ser maior que zero.")
        Integer quantity
) {
}
