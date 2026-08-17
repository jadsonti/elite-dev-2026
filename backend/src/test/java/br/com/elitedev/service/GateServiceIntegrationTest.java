package br.com.elitedev.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import br.com.elitedev.domain.event.Event;
import br.com.elitedev.domain.payment.PaymentStatus;
import br.com.elitedev.domain.ticket.TicketValidationResult;
import br.com.elitedev.domain.user.Role;
import br.com.elitedev.domain.user.User;
import br.com.elitedev.dto.gate.ValidateTicketRequest;
import br.com.elitedev.dto.payment.ProcessPaymentRequest;
import br.com.elitedev.dto.reservation.CreateReservationRequest;
import br.com.elitedev.repository.EventRepository;
import br.com.elitedev.repository.UserRepository;

@SpringBootTest
class GateServiceIntegrationTest {

    @Autowired
    private GateService gateService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldIdentifyWrongEventValidateOnceAndRejectReuse() {
        Fixture fixture = createFixture();
        String token = issueTicket(fixture);

        assertThat(gateService.validate(new ValidateTicketRequest(
                token, fixture.otherEvent().getId())).result())
                .isEqualTo(TicketValidationResult.WRONG_EVENT);

        assertThat(gateService.validate(new ValidateTicketRequest(
                token, fixture.event().getId())).result())
                .isEqualTo(TicketValidationResult.VALID);

        assertThat(gateService.validate(new ValidateTicketRequest(
                token, fixture.event().getId())).result())
                .isEqualTo(TicketValidationResult.ALREADY_USED);
    }

    @Test
    void shouldRejectForgedTicket() {
        Fixture fixture = createFixture();
        String token = issueTicket(fixture);
        String forged = token.substring(0, token.length() - 1)
                + (token.endsWith("A") ? "B" : "A");

        assertThat(gateService.validate(new ValidateTicketRequest(
                forged, fixture.event().getId())).result())
                .isEqualTo(TicketValidationResult.INVALID);
    }

    private String issueTicket(Fixture fixture) {
        var reservation = reservationService.create(
                new CreateReservationRequest(fixture.event().getId(), 1),
                fixture.customer().getEmail());
        paymentService.process(
                new ProcessPaymentRequest(reservation.id(), PaymentStatus.APPROVED),
                fixture.customer().getEmail());
        return ticketService.listMine(fixture.customer().getEmail()).getFirst().qrToken();
    }

    private Fixture createFixture() {
        String suffix = UUID.randomUUID().toString();
        User organizer = userRepository.save(new User(
                "Organizador", "organizer-gate-" + suffix + "@test.local",
                "password", Role.ADMIN));
        User customer = userRepository.save(new User(
                "Cliente", "customer-gate-" + suffix + "@test.local",
                "password", Role.CUSTOMER));
        Event event = createEvent("Evento principal", organizer);
        Event otherEvent = createEvent("Outro evento", organizer);
        return new Fixture(event, otherEvent, customer);
    }

    private Event createEvent(String title, User organizer) {
        Event event = new Event(
                null, null, title, null, null,
                LocalDateTime.now().plusDays(2),
                "Local de teste", 20, new BigDecimal("35.00"), organizer);
        event.publish();
        return eventRepository.save(event);
    }

    private record Fixture(Event event, Event otherEvent, User customer) {
    }
}
