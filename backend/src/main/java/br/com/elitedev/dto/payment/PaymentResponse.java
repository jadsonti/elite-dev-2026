package br.com.elitedev.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.elitedev.domain.payment.Payment;

public record PaymentResponse(
        Long id,
        Long reservationId,
        Long eventId,
        String eventTitle,
        BigDecimal amount,
        String status,
        String reservationStatus,
        String failureReason,
        LocalDateTime processedAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getReservation().getId(),
                payment.getReservation().getEvent().getId(),
                payment.getReservation().getEvent().getTitle(),
                payment.getAmount(),
                payment.getStatus().name(),
                payment.getReservation().getStatus().name(),
                payment.getFailureReason(),
                payment.getProcessedAt()
        );
    }
}
