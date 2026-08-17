package br.com.elitedev.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.elitedev.dto.payment.PaymentResponse;
import br.com.elitedev.dto.payment.ProcessPaymentRequest;
import br.com.elitedev.service.PaymentService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> process(
            @Valid @RequestBody ProcessPaymentRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                paymentService.process(request, authentication.getName()));
    }

    @GetMapping("/reservations/{reservationId}")
    public ResponseEntity<List<PaymentResponse>> listByReservation(
            @PathVariable Long reservationId,
            Authentication authentication) {
        return ResponseEntity.ok(paymentService.listByReservation(
                reservationId, authentication.getName()));
    }
}
