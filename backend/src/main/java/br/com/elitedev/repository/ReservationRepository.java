package br.com.elitedev.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.elitedev.domain.reservation.Reservation;
import jakarta.persistence.LockModeType;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @EntityGraph(attributePaths = {"event", "customer"})
    List<Reservation> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    @EntityGraph(attributePaths = {"event", "customer"})
    Optional<Reservation> findById(Long reservationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"event", "customer"})
    @Query("select reservation from Reservation reservation where reservation.id = :reservationId")
    Optional<Reservation> findByIdForUpdate(
            @Param("reservationId") Long reservationId);
}
