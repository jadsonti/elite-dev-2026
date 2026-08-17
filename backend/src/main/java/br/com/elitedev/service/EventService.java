package br.com.elitedev.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.elitedev.domain.event.Event;
import br.com.elitedev.domain.event.EventStatus;
import br.com.elitedev.domain.user.User;
import br.com.elitedev.dto.event.CreateEventRequest;
import br.com.elitedev.dto.event.EventResponse;
import br.com.elitedev.dto.event.UpdateEventRequest;
import br.com.elitedev.repository.EventRepository;
import br.com.elitedev.repository.UserRepository;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventService(
            EventRepository eventRepository,
            UserRepository userRepository) {

        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public EventResponse create(
            CreateEventRequest request,
            String organizerEmail) {

        User organizer = findUserByEmail(organizerEmail);

        Event event = new Event(
                normalizeNullable(request.externalSource()),
                normalizeNullable(request.externalId()),
                request.title().trim(),
                normalizeNullable(request.description()),
                normalizeNullable(request.imageUrl()),
                request.eventDateTime(),
                request.location().trim(),
                request.capacity(),
                request.price(),
                organizer
        );

        return EventResponse.from(
                eventRepository.save(event)
        );
    }

    @Transactional
    public EventResponse update(
            Long eventId,
            UpdateEventRequest request,
            String organizerEmail) {

        User organizer = findUserByEmail(organizerEmail);

        Event event = findEvent(eventId);

        validateOwnership(event, organizer);

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Evento cancelado não pode ser alterado."
            );
        }

        event.update(
                request.title().trim(),
                normalizeNullable(request.description()),
                normalizeNullable(request.imageUrl()),
                request.eventDateTime(),
                request.location().trim(),
                request.capacity(),
                request.price()
        );

        return EventResponse.from(event);
    }

    @Transactional
    public EventResponse publish(
            Long eventId,
            String organizerEmail) {

        User organizer = findUserByEmail(organizerEmail);

        Event event = findEvent(eventId);

        validateOwnership(event, organizer);

        if (event.getStatus() == EventStatus.PUBLISHED) {
            throw new IllegalStateException(
                    "Evento já está publicado."
            );
        }

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Evento cancelado não pode ser publicado."
            );
        }

        event.publish();

        return EventResponse.from(event);
    }

    @Transactional
    public EventResponse cancel(
            Long eventId,
            String organizerEmail) {

        User organizer = findUserByEmail(organizerEmail);

        Event event = findEvent(eventId);

        validateOwnership(event, organizer);

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Evento já está cancelado."
            );
        }

        event.cancel();

        return EventResponse.from(event);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> listPublished(String query) {

        List<Event> events;

        if (query == null || query.isBlank()) {

            events =
                    eventRepository
                            .findByStatusOrderByEventDateTimeAsc(
                                    EventStatus.PUBLISHED
                            );

        } else {

            events =
                    eventRepository
                            .findByStatusAndTitleContainingIgnoreCaseOrderByEventDateTimeAsc(
                                    EventStatus.PUBLISHED,
                                    query.trim()
                            );
        }

        return events.stream()
                .map(EventResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public EventResponse getPublished(Long eventId) {

        Event event = findEvent(eventId);

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new IllegalArgumentException(
                    "Evento não encontrado."
            );
        }

        return EventResponse.from(event);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> listOrganizerEvents(
            String organizerEmail) {

        User organizer = findUserByEmail(organizerEmail);

        return eventRepository
                .findByCreatedByIdOrderByCreatedAtDesc(
                        organizer.getId()
                )
                .stream()
                .map(EventResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean existsByExternalReference(
            String externalSource,
            String externalId) {
        return eventRepository.existsByExternalSourceAndExternalId(
                externalSource, externalId);
    }

    private User findUserByEmail(String email) {

        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        return userRepository
                .findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuário não encontrado."
                        )
                );
    }

    private Event findEvent(Long eventId) {

        return eventRepository
                .findById(eventId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Evento não encontrado."
                        )
                );
    }

    private void validateOwnership(
            Event event,
            User organizer) {

        if (!event.getCreatedBy()
                .getId()
                .equals(organizer.getId())) {

            throw new SecurityException(
                    "Você não possui permissão para alterar este evento."
            );
        }
    }

    private String normalizeNullable(String value) {

        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isBlank()
                ? null
                : normalized;
    }
}
