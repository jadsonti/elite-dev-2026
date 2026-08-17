package br.com.elitedev.dto.ticket;

import java.time.LocalDateTime;

import br.com.elitedev.domain.ticket.Ticket;

public record SharedTicketResponse(
        Long eventId,
        String eventTitle,
        LocalDateTime eventDateTime,
        String eventLocation,
        Integer sequenceNumber,
        Integer reservationQuantity,
        String status,
        LocalDateTime issuedAt
) {
    public static SharedTicketResponse from(Ticket ticket) {
        return new SharedTicketResponse(
                ticket.getReservation().getEvent().getId(),
                ticket.getReservation().getEvent().getTitle(),
                ticket.getReservation().getEvent().getEventDateTime(),
                ticket.getReservation().getEvent().getLocation(),
                ticket.getSequenceNumber(),
                ticket.getReservation().getQuantity(),
                ticket.getStatus().name(),
                ticket.getIssuedAt()
        );
    }
}
