package br.com.elitedev.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.elitedev.dto.event.CreateEventRequest;
import br.com.elitedev.dto.event.EventResponse;
import br.com.elitedev.dto.event.UpdateEventRequest;
import br.com.elitedev.service.EventService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> listPublished(
            @RequestParam(required = false) String query) {

        return ResponseEntity.ok(
                eventService.listPublished(query)
        );
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getPublished(
            @PathVariable Long eventId) {

        return ResponseEntity.ok(
                eventService.getPublished(eventId)
        );
    }

    @GetMapping("/organizer/me")
    public ResponseEntity<List<EventResponse>>
            listOrganizerEvents(Authentication authentication) {

        return ResponseEntity.ok(
                eventService.listOrganizerEvents(
                        authentication.getName()
                )
        );
    }

    @PostMapping
    public ResponseEntity<EventResponse> create(
            @Valid @RequestBody CreateEventRequest request,
            Authentication authentication) {

        EventResponse response =
                eventService.create(
                        request,
                        authentication.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponse> update(
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                eventService.update(
                        eventId,
                        request,
                        authentication.getName()
                )
        );
    }

    @PatchMapping("/{eventId}/publish")
    public ResponseEntity<EventResponse> publish(
            @PathVariable Long eventId,
            Authentication authentication) {

        return ResponseEntity.ok(
                eventService.publish(
                        eventId,
                        authentication.getName()
                )
        );
    }

    @PatchMapping("/{eventId}/cancel")
    public ResponseEntity<EventResponse> cancel(
            @PathVariable Long eventId,
            Authentication authentication) {

        return ResponseEntity.ok(
                eventService.cancel(
                        eventId,
                        authentication.getName()
                )
        );
    }
}