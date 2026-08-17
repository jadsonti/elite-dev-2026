package br.com.elitedev.controller;

import java.util.List;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.elitedev.dto.ticket.SharedTicketResponse;
import br.com.elitedev.dto.ticket.TicketResponse;
import br.com.elitedev.service.TicketService;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/me")
    public ResponseEntity<List<TicketResponse>> listMine(Authentication authentication) {
        return ResponseEntity.ok(ticketService.listMine(authentication.getName()));
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketResponse> getMine(
            @PathVariable Long ticketId,
            Authentication authentication) {
        return ResponseEntity.ok(ticketService.getMine(
                ticketId, authentication.getName()));
    }

    @GetMapping(value = "/{ticketId}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateQrMine(
            @PathVariable Long ticketId,
            Authentication authentication) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.noStore())
                .body(ticketService.generateQrMine(
                        ticketId, authentication.getName()));
    }

    @GetMapping("/shared/{token}")
    public ResponseEntity<SharedTicketResponse> findShared(@PathVariable String token) {
        return ResponseEntity.ok(ticketService.findShared(token));
    }
}
