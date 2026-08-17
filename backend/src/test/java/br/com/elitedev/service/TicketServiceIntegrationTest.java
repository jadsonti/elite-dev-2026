package br.com.elitedev.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import br.com.elitedev.domain.event.Event;
import br.com.elitedev.domain.payment.PaymentStatus;
import br.com.elitedev.domain.ticket.TicketStatus;
import br.com.elitedev.domain.user.Role;
import br.com.elitedev.domain.user.User;
import br.com.elitedev.dto.payment.ProcessPaymentRequest;
import br.com.elitedev.dto.reservation.CreateReservationRequest;
import br.com.elitedev.exception.ResourceNotFoundException;
import br.com.elitedev.repository.EventRepository;
import br.com.elitedev.repository.ReservationRepository;
import br.com.elitedev.repository.UserRepository;

@SpringBootTest
class TicketServiceIntegrationTest {

    @Autowired
    private TicketService ticketService;

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
    void shouldIssueOneSecureTicketPerReservedUnitAfterApproval() throws Exception {
        Fixture fixture = createFixture();
        var reservation = reservationService.create(
                new CreateReservationRequest(fixture.event().getId(), 2),
                fixture.firstCustomer().getEmail());

        paymentService.process(
                new ProcessPaymentRequest(reservation.id(), PaymentStatus.APPROVED),
                fixture.firstCustomer().getEmail());

        var tickets = ticketService.listMine(fixture.firstCustomer().getEmail());
        assertThat(tickets).hasSize(2);
        assertThat(tickets).extracting(ticket -> ticket.sequenceNumber())
                .containsExactlyInAnyOrder(1, 2);
        assertThat(tickets).extracting(ticket -> ticket.qrToken()).doesNotHaveDuplicates();
        assertThat(tickets).allSatisfy(ticket -> {
            assertThat(ticket.status()).isEqualTo(TicketStatus.ACTIVE.name());
            assertThat(ticket.shareUrl()).endsWith("/" + ticket.qrToken());
        });

        var first = tickets.getFirst();
        var shared = ticketService.findShared(first.qrToken());
        assertThat(shared.eventId()).isEqualTo(fixture.event().getId());
        assertThat(shared.sequenceNumber()).isEqualTo(first.sequenceNumber());

        byte[] png = ticketService.generateQrMine(
                first.id(), fixture.firstCustomer().getEmail());
        assertThat(png).startsWith((byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47);
        assertThat(decodeQr(png)).isEqualTo(first.qrToken());
    }

    @Test
    void shouldRejectForgedTokenAndTicketFromAnotherCustomer() {
        Fixture fixture = createFixture();
        var reservation = reservationService.create(
                new CreateReservationRequest(fixture.event().getId(), 1),
                fixture.firstCustomer().getEmail());
        paymentService.process(
                new ProcessPaymentRequest(reservation.id(), PaymentStatus.APPROVED),
                fixture.firstCustomer().getEmail());
        var ticket = ticketService.listMine(fixture.firstCustomer().getEmail()).getFirst();

        assertThatThrownBy(() -> ticketService.getMine(
                ticket.id(), fixture.secondCustomer().getEmail()))
                .isInstanceOf(SecurityException.class);

        String forgedToken = changeLastCharacter(ticket.qrToken());
        assertThatThrownBy(() -> ticketService.findShared(forgedToken))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldNotIssueOnDeclineAndShouldKeepIssuanceIdempotent() {
        Fixture fixture = createFixture();
        var reservation = reservationService.create(
                new CreateReservationRequest(fixture.event().getId(), 2),
                fixture.firstCustomer().getEmail());

        paymentService.process(
                new ProcessPaymentRequest(reservation.id(), PaymentStatus.DECLINED),
                fixture.firstCustomer().getEmail());
        assertThat(ticketService.listMine(fixture.firstCustomer().getEmail())).isEmpty();

        paymentService.process(
                new ProcessPaymentRequest(reservation.id(), PaymentStatus.APPROVED),
                fixture.firstCustomer().getEmail());
        var before = ticketService.listMine(fixture.firstCustomer().getEmail());

        var confirmedReservation = reservationRepository.findById(reservation.id()).orElseThrow();
        ticketService.issueForReservation(confirmedReservation);
        var after = ticketService.listMine(fixture.firstCustomer().getEmail());

        assertThat(after).extracting(ticket -> ticket.id())
                .containsExactlyInAnyOrderElementsOf(
                        before.stream().map(ticket -> ticket.id()).toList());
    }

    private String decodeQr(byte[] png) throws Exception {
        var image = ImageIO.read(new ByteArrayInputStream(png));
        var bitmap = new BinaryBitmap(new HybridBinarizer(
                new BufferedImageLuminanceSource(image)));
        return new QRCodeReader().decode(bitmap).getText();
    }

    private String changeLastCharacter(String token) {
        char last = token.charAt(token.length() - 1);
        return token.substring(0, token.length() - 1) + (last == 'A' ? 'B' : 'A');
    }

    private Fixture createFixture() {
        String suffix = UUID.randomUUID().toString();
        User organizer = userRepository.save(new User(
                "Organizador", "organizer-ticket-" + suffix + "@test.local",
                "password", Role.ADMIN));
        User firstCustomer = userRepository.save(new User(
                "Cliente 1", "customer-ticket-1-" + suffix + "@test.local",
                "password", Role.CUSTOMER));
        User secondCustomer = userRepository.save(new User(
                "Cliente 2", "customer-ticket-2-" + suffix + "@test.local",
                "password", Role.CUSTOMER));

        Event event = new Event(
                null,
                null,
                "Evento de ingressos",
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
