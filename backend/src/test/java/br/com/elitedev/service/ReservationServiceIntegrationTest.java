package br.com.elitedev.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import br.com.elitedev.domain.event.Event;
import br.com.elitedev.domain.reservation.ReservationStatus;
import br.com.elitedev.domain.user.Role;
import br.com.elitedev.domain.user.User;
import br.com.elitedev.dto.reservation.CreateReservationRequest;
import br.com.elitedev.dto.reservation.ReservationResponse;
import br.com.elitedev.repository.EventRepository;
import br.com.elitedev.repository.UserRepository;

@SpringBootTest
class ReservationServiceIntegrationTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldReserveAndRestoreAvailabilityWhenCancelled() {
        Fixture fixture = createFixture(10);

        var reservation = reservationService.create(
                new CreateReservationRequest(fixture.event().getId(), 3),
                fixture.firstCustomer().getEmail());

        assertThat(reservation.status()).isEqualTo(ReservationStatus.PENDING_PAYMENT.name());
        assertThat(reservation.quantity()).isEqualTo(3);
        assertThat(reservation.totalPrice()).isEqualByComparingTo("105.00");
        assertThat(findEvent(fixture.event().getId()).getAvailableQuantity()).isEqualTo(7);

        var cancelled = reservationService.cancel(
                reservation.id(), fixture.firstCustomer().getEmail());

        assertThat(cancelled.status()).isEqualTo(ReservationStatus.CANCELLED.name());
        assertThat(cancelled.cancelledAt()).isNotNull();
        assertThat(findEvent(fixture.event().getId()).getAvailableQuantity()).isEqualTo(10);
    }

    @Test
    void shouldNotOversellWhenReservationsAreConcurrent() throws Exception {
        Fixture fixture = createFixture(10);
        CountDownLatch start = new CountDownLatch(1);

        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> reserveAfterSignal(
                    start, fixture.event().getId(), fixture.firstCustomer().getEmail(), 6));
            var second = executor.submit(() -> reserveAfterSignal(
                    start, fixture.event().getId(), fixture.secondCustomer().getEmail(), 6));

            start.countDown();

            boolean firstSucceeded = first.get(20, TimeUnit.SECONDS);
            boolean secondSucceeded = second.get(20, TimeUnit.SECONDS);

            assertThat(firstSucceeded ^ secondSucceeded).isTrue();
            assertThat(findEvent(fixture.event().getId()).getAvailableQuantity()).isEqualTo(4);
        }
    }

    @Test
    void shouldRestrictReservationToItsCustomerAndPreventDuplicateCancellation() {
        Fixture fixture = createFixture(10);
        var reservation = reservationService.create(
                new CreateReservationRequest(fixture.event().getId(), 2),
                fixture.firstCustomer().getEmail());

        assertThat(reservationService.listMine(fixture.firstCustomer().getEmail()))
                .extracting(ReservationResponse::id)
                .containsExactly(reservation.id());
        assertThat(reservationService.listMine(fixture.secondCustomer().getEmail())).isEmpty();
        assertThatThrownBy(() -> reservationService.getMine(
                reservation.id(), fixture.secondCustomer().getEmail()))
                .isInstanceOf(SecurityException.class);

        reservationService.cancel(reservation.id(), fixture.firstCustomer().getEmail());

        assertThatThrownBy(() -> reservationService.cancel(
                reservation.id(), fixture.firstCustomer().getEmail()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findEvent(fixture.event().getId()).getAvailableQuantity()).isEqualTo(10);
    }

    private boolean reserveAfterSignal(
            CountDownLatch start,
            Long eventId,
            String customerEmail,
            int quantity) throws InterruptedException {
        start.await(10, TimeUnit.SECONDS);
        try {
            reservationService.create(
                    new CreateReservationRequest(eventId, quantity), customerEmail);
            return true;
        } catch (IllegalStateException exception) {
            return false;
        }
    }

    private Fixture createFixture(int capacity) {
        String suffix = UUID.randomUUID().toString();
        User organizer = userRepository.save(new User(
                "Organizador", "organizer-" + suffix + "@test.local", "password", Role.ADMIN));
        User firstCustomer = userRepository.save(new User(
                "Cliente 1", "customer-1-" + suffix + "@test.local", "password", Role.CUSTOMER));
        User secondCustomer = userRepository.save(new User(
                "Cliente 2", "customer-2-" + suffix + "@test.local", "password", Role.CUSTOMER));

        Event event = new Event(
                null,
                null,
                "Evento de teste",
                null,
                null,
                LocalDateTime.now().plusDays(2),
                "Local de teste",
                capacity,
                new BigDecimal("35.00"),
                organizer);
        event.publish();
        eventRepository.save(event);

        return new Fixture(event, firstCustomer, secondCustomer);
    }

    private Event findEvent(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow();
    }

    private record Fixture(
            Event event,
            User firstCustomer,
            User secondCustomer) {
    }
}
