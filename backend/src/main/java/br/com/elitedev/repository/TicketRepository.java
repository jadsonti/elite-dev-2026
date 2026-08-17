package br.com.elitedev.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.elitedev.domain.ticket.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @EntityGraph(attributePaths = {"reservation", "reservation.event", "customer"})
    List<Ticket> findByCustomerIdOrderByIssuedAtDesc(Long customerId);

    @EntityGraph(attributePaths = {"reservation", "reservation.event", "customer"})
    List<Ticket> findByReservationIdOrderBySequenceNumber(Long reservationId);

    @EntityGraph(attributePaths = {"reservation", "reservation.event", "customer"})
    Optional<Ticket> findById(Long ticketId);

    @EntityGraph(attributePaths = {"reservation", "reservation.event", "customer"})
    Optional<Ticket> findByCode(UUID code);
}
