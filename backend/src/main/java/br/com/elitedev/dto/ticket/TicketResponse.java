package br.com.elitedev.dto.ticket;

import java.time.LocalDateTime;

import br.com.elitedev.domain.ticket.Ticket;

public record TicketResponse(
        Long id,
        Long reservationId,
        Long eventId,
        String eventTitle,
        LocalDateTime eventDateTime,
        String eventLocation,
        Integer sequenceNumber,
        Integer reservationQuantity,
        String customerName,
        String status,
        String qrToken,
        String shareUrl,
        LocalDateTime issuedAt,
        LocalDateTime usedAt
) {
    public static TicketResponse from(Ticket ticket, String shareUrl) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getReservation().getId(),
                ticket.getReservation().getEvent().getId(),
                ticket.getReservation().getEvent().getTitle(),
                ticket.getReservation().getEvent().getEventDateTime(),
                ticket.getReservation().getEvent().getLocation(),
                ticket.getSequenceNumber(),
                ticket.getReservation().getQuantity(),
                ticket.getCustomer().getName(),
                ticket.getStatus().name(),
                ticket.token(),
                shareUrl,
                ticket.getIssuedAt(),
                ticket.getUsedAt()
        );
    }
}
