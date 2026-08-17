package br.com.elitedev.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.elitedev.domain.event.EventStatus;
import br.com.elitedev.domain.ticket.Ticket;
import br.com.elitedev.domain.ticket.TicketStatus;
import br.com.elitedev.domain.ticket.TicketValidationResult;
import br.com.elitedev.dto.gate.TicketValidationResponse;
import br.com.elitedev.dto.gate.ValidateTicketRequest;
import br.com.elitedev.repository.TicketRepository;
import br.com.elitedev.security.TicketTokenService;

@Service
public class GateService {

    private final TicketRepository ticketRepository;
    private final TicketTokenService ticketTokenService;

    public GateService(
            TicketRepository ticketRepository,
            TicketTokenService ticketTokenService) {
        this.ticketRepository = ticketRepository;
        this.ticketTokenService = ticketTokenService;
    }

    @Transactional
    public TicketValidationResponse validate(ValidateTicketRequest request) {
        var code = ticketTokenService.verify(request.token());
        if (code.isEmpty()) {
            return TicketValidationResponse.invalid();
        }

        var ticket = ticketRepository.findByCodeForUpdate(code.get());
        if (ticket.isEmpty()) {
            return TicketValidationResponse.invalid();
        }

        Ticket found = ticket.get();
        if (!found.getReservation().getEvent().getId().equals(request.eventId())) {
            return response(
                    found,
                    TicketValidationResult.WRONG_EVENT,
                    "Ingresso pertence a outro evento.");
        }
        if (found.getReservation().getEvent().getStatus() != EventStatus.PUBLISHED) {
            return response(
                    found,
                    TicketValidationResult.INVALID,
                    "O evento deste ingresso não está ativo.");
        }
        if (found.getStatus() == TicketStatus.USED) {
            return response(
                    found,
                    TicketValidationResult.ALREADY_USED,
                    "Ingresso já utilizado.");
        }

        found.use();
        return response(
                found,
                TicketValidationResult.VALID,
                "Ingresso validado. Entrada liberada.");
    }

    private TicketValidationResponse response(
            Ticket ticket,
            TicketValidationResult result,
            String message) {
        return new TicketValidationResponse(
                result,
                message,
                ticket.getId(),
                ticket.getReservation().getEvent().getId(),
                ticket.getReservation().getEvent().getTitle(),
                ticket.getSequenceNumber(),
                ticket.getUsedAt() == null
                        ? java.time.LocalDateTime.now()
                        : ticket.getUsedAt());
    }
}
