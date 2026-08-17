package br.com.elitedev.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.elitedev.domain.payment.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @EntityGraph(attributePaths = {
            "reservation",
            "reservation.event",
            "reservation.customer"
    })
    List<Payment> findByReservationIdOrderByProcessedAtDesc(Long reservationId);
}
