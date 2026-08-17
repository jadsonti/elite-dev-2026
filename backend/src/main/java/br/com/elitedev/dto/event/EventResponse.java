package br.com.elitedev.dto.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.elitedev.domain.event.Event;

public record EventResponse(

        Long id,
        String externalSource,
        String externalId,
        String title,
        String description,
        String imageUrl,
        LocalDateTime eventDateTime,
        String location,
        Integer capacity,
        Integer availableQuantity,
        BigDecimal price,
        String status,
        Long createdById,
        String createdByName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {

    public static EventResponse from(Event event) {

        return new EventResponse(
                event.getId(),
                event.getExternalSource(),
                event.getExternalId(),
                event.getTitle(),
                event.getDescription(),
                event.getImageUrl(),
                event.getEventDateTime(),
                event.getLocation(),
                event.getCapacity(),
                event.getAvailableQuantity(),
                event.getPrice(),
                event.getStatus().name(),
                event.getCreatedBy().getId(),
                event.getCreatedBy().getName(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }
}