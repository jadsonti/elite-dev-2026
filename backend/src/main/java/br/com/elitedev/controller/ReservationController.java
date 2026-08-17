package br.com.elitedev.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.elitedev.dto.reservation.CreateReservationRequest;
import br.com.elitedev.dto.reservation.ReservationResponse;
import br.com.elitedev.service.ReservationService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @Valid @RequestBody CreateReservationRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                reservationService.create(request, authentication.getName()));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ReservationResponse>> listMine(
            Authentication authentication) {
        return ResponseEntity.ok(
                reservationService.listMine(authentication.getName()));
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> getMine(
            @PathVariable Long reservationId,
            Authentication authentication) {
        return ResponseEntity.ok(
                reservationService.getMine(reservationId, authentication.getName()));
    }

    @PatchMapping("/{reservationId}/cancel")
    public ResponseEntity<ReservationResponse> cancel(
            @PathVariable Long reservationId,
            Authentication authentication) {
        return ResponseEntity.ok(
                reservationService.cancel(reservationId, authentication.getName()));
    }
}
