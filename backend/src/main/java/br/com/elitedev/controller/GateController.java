package br.com.elitedev.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.elitedev.dto.gate.TicketValidationResponse;
import br.com.elitedev.dto.gate.ValidateTicketRequest;
import br.com.elitedev.service.GateService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/gate")
public class GateController {

    private final GateService gateService;

    public GateController(GateService gateService) {
        this.gateService = gateService;
    }

    @PostMapping("/validate")
    public ResponseEntity<TicketValidationResponse> validate(
            @Valid @RequestBody ValidateTicketRequest request) {
        return ResponseEntity.ok(gateService.validate(request));
    }
}
