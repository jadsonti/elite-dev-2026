package br.com.elitedev.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.elitedev.domain.reservation.Reservation;
import br.com.elitedev.domain.reservation.ReservationStatus;
import br.com.elitedev.domain.ticket.Ticket;
import br.com.elitedev.domain.user.User;
import br.com.elitedev.dto.ticket.SharedTicketResponse;
import br.com.elitedev.dto.ticket.TicketResponse;
import br.com.elitedev.exception.ResourceNotFoundException;
import br.com.elitedev.repository.TicketRepository;
import br.com.elitedev.repository.UserRepository;
import br.com.elitedev.security.TicketTokenService;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketTokenService ticketTokenService;
    private final QrCodeService qrCodeService;
    private final String shareBaseUrl;

    public TicketService(
            TicketRepository ticketRepository,
            UserRepository userRepository,
            TicketTokenService ticketTokenService,
            QrCodeService qrCodeService,
            @Value("${app.ticket.share-base-url}") String shareBaseUrl) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.ticketTokenService = ticketTokenService;
        this.qrCodeService = qrCodeService;
        this.shareBaseUrl = removeTrailingSlash(shareBaseUrl);
    }

    @Transactional
    public List<Ticket> issueForReservation(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Somente reservas pagas recebem ingressos.");
        }

        List<Ticket> existing = ticketRepository
                .findByReservationIdOrderBySequenceNumber(reservation.getId());
        if (!existing.isEmpty()) {
            if (existing.size() != reservation.getQuantity()) {
                throw new IllegalStateException("A emissão dos ingressos está inconsistente.");
            }
            return existing;
        }

        List<Ticket> tickets = new ArrayList<>(reservation.getQuantity());
        for (int sequence = 1; sequence <= reservation.getQuantity(); sequence++) {
            var signedToken = ticketTokenService.generate();
            tickets.add(new Ticket(
                    reservation,
                    sequence,
                    signedToken.code(),
                    signedToken.signature()));
        }
        return ticketRepository.saveAll(tickets);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> listMine(String customerEmail) {
        User customer = findCustomer(customerEmail);
        return ticketRepository.findByCustomerIdOrderByIssuedAtDesc(customer.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketResponse getMine(Long ticketId, String customerEmail) {
        User customer = findCustomer(customerEmail);
        return toResponse(findMine(ticketId, customer));
    }

    @Transactional(readOnly = true)
    public byte[] generateQrMine(Long ticketId, String customerEmail) {
        User customer = findCustomer(customerEmail);
        Ticket ticket = findMine(ticketId, customer);
        return qrCodeService.generatePng(ticket.token());
    }

    @Transactional(readOnly = true)
    public SharedTicketResponse findShared(String token) {
        var code = ticketTokenService.verify(token)
                .orElseThrow(this::invalidTicket);
        Ticket ticket = ticketRepository.findByCode(code)
                .orElseThrow(this::invalidTicket);
        return SharedTicketResponse.from(ticket);
    }

    private Ticket findMine(Long ticketId, User customer) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ingresso não encontrado."));
        if (!ticket.getCustomer().getId().equals(customer.getId())) {
            throw new SecurityException("Você não possui permissão para acessar este ingresso.");
        }
        return ticket;
    }

    private User findCustomer(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        return userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));
    }

    private TicketResponse toResponse(Ticket ticket) {
        return TicketResponse.from(ticket, shareBaseUrl + "/" + ticket.token());
    }

    private ResourceNotFoundException invalidTicket() {
        return new ResourceNotFoundException("Ingresso inválido ou inexistente.");
    }

    private String removeTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
