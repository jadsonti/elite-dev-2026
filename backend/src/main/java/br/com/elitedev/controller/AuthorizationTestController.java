package br.com.elitedev.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class AuthorizationTestController {

    @GetMapping("/authenticated")
    public ResponseEntity<String> authenticated() {

        return ResponseEntity.ok(
                "Usuário autenticado com sucesso."
        );
    }

    @GetMapping("/admin")
    public ResponseEntity<String> admin() {

        return ResponseEntity.ok(
                "Acesso autorizado para ADMIN."
        );
    }

    @GetMapping("/customer")
    public ResponseEntity<String> customer() {

        return ResponseEntity.ok(
                "Acesso autorizado para CUSTOMER."
        );
    }

    @GetMapping("/gate")
    public ResponseEntity<String> gate() {

        return ResponseEntity.ok(
                "Acesso autorizado para GATE."
        );
    }
}