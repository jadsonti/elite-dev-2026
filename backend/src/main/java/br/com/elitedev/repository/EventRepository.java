package br.com.elitedev.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.elitedev.domain.event.Event;
import br.com.elitedev.domain.event.EventStatus;
import jakarta.persistence.LockModeType;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select event from Event event where event.id = :eventId")
    java.util.Optional<Event> findByIdForUpdate(@Param("eventId") Long eventId);

    boolean existsByExternalSourceAndExternalId(
            String externalSource,
            String externalId
    );

    List<Event> findByStatusOrderByEventDateTimeAsc(
            EventStatus status
    );

    List<Event> findByStatusAndTitleContainingIgnoreCaseOrderByEventDateTimeAsc(
            EventStatus status,
            String title
    );

    List<Event> findByCreatedByIdOrderByCreatedAtDesc(
            Long createdById
    );
}
