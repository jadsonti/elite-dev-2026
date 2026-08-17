package br.com.elitedev.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import br.com.elitedev.security.CustomUserDetailsService;
import br.com.elitedev.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) {

        return configuration.getAuthenticationManager();
    }

    @Bean
    AuthenticationEntryPoint authenticationEntryPoint() {

        return (request, response, authException) -> {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            response.setContentType(
                    MediaType.APPLICATION_JSON_VALUE
            );

            response.setCharacterEncoding("UTF-8");

            response.getWriter().write("""
                    {
                      "status": 401,
                      "error": "Unauthorized",
                      "message": "Autenticação necessária para acessar este recurso."
                    }
                    """);
        };
    }

    @Bean
    AccessDeniedHandler accessDeniedHandler() {

        return (request, response, accessDeniedException) -> {

            response.setStatus(
                    HttpServletResponse.SC_FORBIDDEN
            );

            response.setContentType(
                    MediaType.APPLICATION_JSON_VALUE
            );

            response.setCharacterEncoding("UTF-8");

            response.getWriter().write("""
                    {
                      "status": 403,
                      "error": "Forbidden",
                      "message": "Você não possui permissão para acessar este recurso."
                    }
                    """);
        };
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(
                    authenticationEntryPoint()
                )
                .accessDeniedHandler(
                    accessDeniedHandler()
                )
            )

            .authorizeHttpRequests(auth -> auth

                // Públicos
                .requestMatchers(
                    "/api/auth/login",
                    "/actuator/health"
                ).permitAll()

                // Consulta pública dos eventos publicados
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/events",
                    "/api/events/*"
                ).permitAll()

                .requestMatchers(
                    "/api/catalog/**"
                ).hasAuthority("ROLE_ADMIN")

                // Administração dos eventos
                .requestMatchers(
                    "/api/events/organizer/**"
                ).hasAuthority("ROLE_ADMIN")

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/events"
                ).hasAuthority("ROLE_ADMIN")

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/events/**"
                ).hasAuthority("ROLE_ADMIN")

                .requestMatchers(
                    HttpMethod.PATCH,
                    "/api/events/**"
                ).hasAuthority("ROLE_ADMIN")

                // Infraestrutura atual de autenticação
                .requestMatchers(
                    "/api/test/admin"
                ).hasAuthority("ROLE_ADMIN")

                .requestMatchers(
                    "/api/test/customer"
                ).hasAuthority("ROLE_CUSTOMER")

                .requestMatchers(
                    "/api/test/gate"
                ).hasAuthority("ROLE_GATE")

                .requestMatchers(
                    "/api/auth/me",
                    "/api/test/authenticated"
                ).authenticated()

                .anyRequest().authenticated()
            )

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
