package br.com.elitedev.config.dev;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import br.com.elitedev.domain.user.Role;
import br.com.elitedev.service.UserService;

@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    private final UserService userService;

    public DevDataSeeder(UserService userService) {
        this.userService = userService;
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