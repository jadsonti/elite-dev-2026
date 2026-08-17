package br.com.elitedev.config.dev;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import br.com.elitedev.domain.user.Role;
import br.com.elitedev.dto.event.CreateEventRequest;
import br.com.elitedev.service.EventService;
import br.com.elitedev.service.UserService;

@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    private final UserService userService;
    private final EventService eventService;

    public DevDataSeeder(
            UserService userService,
            EventService eventService) {
        this.userService = userService;
        this.eventService = eventService;
    }

    @Override
    public void run(String... args) {

        createUserIfNotExists(
                "Administrador",
                "organizador@demo.com",
                "123456",
                Role.ADMIN
        );

        createUserIfNotExists(
                "Cliente 1",
                "cliente1@demo.com",
                "123456",
                Role.CUSTOMER
        );

        createUserIfNotExists(
                "Cliente 2",
                "cliente2@demo.com",
                "123456",
                Role.CUSTOMER
        );

        createUserIfNotExists(
                "Portaria",
                "portaria@demo.com",
                "123456",
                Role.GATE
        );

        createPublishedEventIfNotExists();
    }

    private void createPublishedEventIfNotExists() {
        String source = "TMDB";
        String externalId = "603";

        if (eventService.existsByExternalReference(source, externalId)) {
            return;
        }

        var event = eventService.create(
                new CreateEventRequest(
                        source,
                        externalId,
                        "Matrix — Sessão Especial",
                        "Uma sessão de cinema preparada para percorrer o fluxo de demonstração.",
                        "https://image.tmdb.org/t/p/w780/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg",
                        LocalDateTime.now().plusMonths(3),
                        "Cine Elite — Sala 1",
                        80,
                        new BigDecimal("35.00")
                ),
                "organizador@demo.com"
        );

        eventService.publish(event.id(), "organizador@demo.com");
    }

    private void createUserIfNotExists(
            String name,
            String email,
            String password,
            Role role) {

        if (!userService.existsByEmail(email)) {
            userService.create(
                    name,
                    email,
                    password,
                    role
            );
        }
    }
}
