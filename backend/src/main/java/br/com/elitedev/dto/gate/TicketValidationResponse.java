package br.com.elitedev.dto.gate;

import java.time.LocalDateTime;

import br.com.elitedev.domain.ticket.TicketValidationResult;

public record TicketValidationResponse(
        TicketValidationResult result,
        String message,
        Long ticketId,
        Long eventId,
        String eventTitle,
        Integer sequenceNumber,
        LocalDateTime validatedAt
) {
    public static TicketValidationResponse invalid() {
        return new TicketValidationResponse(
                TicketValidationResult.INVALID,
                "Ingresso inválido.",
                null,
                null,
                null,
                null,
                LocalDateTime.now());
    }
}
