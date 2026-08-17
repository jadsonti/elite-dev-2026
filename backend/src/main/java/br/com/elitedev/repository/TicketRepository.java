package br.com.elitedev.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.elitedev.domain.ticket.Ticket;
import jakarta.persistence.LockModeType;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @EntityGraph(attributePaths = {"reservation", "reservation.event", "customer"})
    List<Ticket> findByCustomerIdOrderByIssuedAtDesc(Long customerId);

    @EntityGraph(attributePaths = {"reservation", "reservation.event", "customer"})
    List<Ticket> findByReservationIdOrderBySequenceNumber(Long reservationId);

    @EntityGraph(attributePaths = {"reservation", "reservation.event", "customer"})
    Optional<Ticket> findById(Long ticketId);

    @EntityGraph(attributePaths = {"reservation", "reservation.event", "customer"})
    Optional<Ticket> findByCode(UUID code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"reservation", "reservation.event", "customer"})
    @Query("select ticket from Ticket ticket where ticket.code = :code")
    Optional<Ticket> findByCodeForUpdate(@Param("code") UUID code);
}
