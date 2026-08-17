package br.com.elitedev.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.elitedev.domain.event.Event;
import br.com.elitedev.domain.event.EventStatus;

public interface EventRepository extends JpaRepository<Event, Long> {

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
