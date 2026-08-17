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
import br.com.elitedev.domain.payment.PaymentStatus;
import br.com.elitedev.domain.reservation.ReservationStatus;
import br.com.elitedev.domain.user.Role;
import br.com.elitedev.domain.user.User;
import br.com.elitedev.dto.payment.ProcessPaymentRequest;
import br.com.elitedev.dto.reservation.CreateReservationRequest;
import br.com.elitedev.dto.reservation.ReservationResponse;
import br.com.elitedev.repository.EventRepository;
import br.com.elitedev.repository.ReservationRepository;
import br.com.elitedev.repository.UserRepository;

@SpringBootTest
class PaymentServiceIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldApprovePaymentAndConfirmReservation() {
        Fixture fixture = createFixture();
        var reservation = createReservation(fixture, fixture.firstCustomer());

        var payment = paymentService.process(
                new ProcessPaymentRequest(reservation.id(), PaymentStatus.APPROVED),
                fixture.firstCustomer().getEmail());

        assertThat(payment.status()).isEqualTo(PaymentStatus.APPROVED.name());
        assertThat(payment.reservationStatus()).isEqualTo(ReservationStatus.CONFIRMED.name());
        assertThat(payment.amount()).isEqualByComparingTo("70.00");
        assertThat(payment.failureReason()).isNull();
        assertThat(findReservationStatus(reservation.id())).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(paymentService.listByReservation(
                reservation.id(), fixture.firstCustomer().getEmail()))
                .extracting(response -> response.id())
                .containsExactly(payment.id());
    }

    @Test
    void shouldRecordDeclineAndAllowAnotherAttempt() {
        Fixture fixture = createFixture();
        var reservation = createReservation(fixture, fixture.firstCustomer());

        var declined = paymentService.process(
                new ProcessPaymentRequest(reservation.id(), PaymentStatus.DECLINED),
                fixture.firstCustomer().getEmail());

        assertThat(declined.status()).isEqualTo(PaymentStatus.DECLINED.name());
        assertThat(declined.failureReason()).isNotBlank();
        assertThat(findReservationStatus(reservation.id()))
                .isEqualTo(ReservationStatus.PENDING_PAYMENT);

        var approved = paymentService.process(
                new ProcessPaymentRequest(reservation.id(), PaymentStatus.APPROVED),
                fixture.firstCustomer().getEmail());

        assertThat(approved.status()).isEqualTo(PaymentStatus.APPROVED.name());
        assertThat(paymentService.listByReservation(
                reservation.id(), fixture.firstCustomer().getEmail()))
                .extracting(response -> response.status())
                .containsExactlyInAnyOrder(
                        PaymentStatus.APPROVED.name(),
                        PaymentStatus.DECLINED.name());
    }

    @Test
    void shouldRejectAnotherCustomerAndDuplicateConcurrentApproval() throws Exception {
        Fixture fixture = createFixture();
        var reservation = createReservation(fixture, fixture.firstCustomer());

        assertThatThrownBy(() -> paymentService.process(
                new ProcessPaymentRequest(reservation.id(), PaymentStatus.APPROVED),
                fixture.secondCustomer().getEmail()))
                .isInstanceOf(SecurityException.class);

        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> approveAfterSignal(
                    start, reservation.id(), fixture.firstCustomer().getEmail()));
            var second = executor.submit(() -> approveAfterSignal(
                    start, reservation.id(), fixture.firstCustomer().getEmail()));

            start.countDown();

            assertThat(first.get(20, TimeUnit.SECONDS) ^ second.get(20, TimeUnit.SECONDS))
                    .isTrue();
        }

        assertThat(paymentService.listByReservation(
                reservation.id(), fixture.firstCustomer().getEmail())).hasSize(1);
        assertThat(findReservationStatus(reservation.id())).isEqualTo(ReservationStatus.CONFIRMED);
    }

    private boolean approveAfterSignal(
            CountDownLatch start,
            Long reservationId,
            String customerEmail) throws InterruptedException {
        start.await(10, TimeUnit.SECONDS);
        try {
            paymentService.process(
                    new ProcessPaymentRequest(reservationId, PaymentStatus.APPROVED),
                    customerEmail);
            return true;
        } catch (IllegalStateException exception) {
            return false;
        }
    }

    private ReservationResponse createReservation(
            Fixture fixture,
            User customer) {
        return reservationService.create(
                new CreateReservationRequest(fixture.event().getId(), 2),
                customer.getEmail());
    }

    private ReservationStatus findReservationStatus(Long reservationId) {
        return reservationRepository.findById(reservationId).orElseThrow().getStatus();
    }

    private Fixture createFixture() {
        String suffix = UUID.randomUUID().toString();
        User organizer = userRepository.save(new User(
                "Organizador", "organizer-payment-" + suffix + "@test.local",
                "password", Role.ADMIN));
        User firstCustomer = userRepository.save(new User(
                "Cliente 1", "customer-payment-1-" + suffix + "@test.local",
                "password", Role.CUSTOMER));
        User secondCustomer = userRepository.save(new User(
                "Cliente 2", "customer-payment-2-" + suffix + "@test.local",
                "password", Role.CUSTOMER));

        Event event = new Event(
                null,
                null,
                "Evento de pagamento",
                null,
                null,
                LocalDateTime.now().plusDays(2),
                "Local de teste",
                20,
                new BigDecimal("35.00"),
                organizer);
        event.publish();
        eventRepository.save(event);
        return new Fixture(event, firstCustomer, secondCustomer);
    }

    private record Fixture(
            Event event,
            User firstCustomer,
            User secondCustomer) {
    }
}
