package br.com.elitedev.dto.payment;

import br.com.elitedev.domain.payment.PaymentStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ProcessPaymentRequest(
        @NotNull(message = "Reserva é obrigatória.")
        @Min(value = 1, message = "Reserva inválida.")
        Long reservationId,

        @NotNull(message = "Resultado da simulação é obrigatório.")
        PaymentStatus outcome
) {
}
