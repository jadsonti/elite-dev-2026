package br.com.elitedev.dto.reservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.elitedev.domain.reservation.Reservation;

public record ReservationResponse(
        Long id,
        Long eventId,
        String eventTitle,
        LocalDateTime eventDateTime,
        String eventLocation,
        Long customerId,
        String customerName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime cancelledAt
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getEvent().getId(),
                reservation.getEvent().getTitle(),
                reservation.getEvent().getEventDateTime(),
                reservation.getEvent().getLocation(),
                reservation.getCustomer().getId(),
                reservation.getCustomer().getName(),
                reservation.getQuantity(),
                reservation.getUnitPrice(),
                reservation.getTotalPrice(),
                reservation.getStatus().name(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt(),
                reservation.getCancelledAt()
        );
    }
}
