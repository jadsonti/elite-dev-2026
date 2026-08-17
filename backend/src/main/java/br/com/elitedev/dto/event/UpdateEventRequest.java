package br.com.elitedev.dto.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateEventRequest(

        @NotBlank(message = "Título é obrigatório.")
        @Size(max = 180)
        String title,

        String description,

        @Size(max = 1000)
        String imageUrl,

        @NotNull(message = "Data do evento é obrigatória.")
        @Future(message = "A data do evento deve estar no futuro.")
        LocalDateTime eventDateTime,

        @NotBlank(message = "Local é obrigatório.")
        @Size(max = 255)
        String location,

        @NotNull(message = "Capacidade é obrigatória.")
        @Min(value = 1)
        Integer capacity,

        @NotNull(message = "Preço é obrigatório.")
        @DecimalMin(value = "0.00", inclusive = true)
        BigDecimal price

) {
}