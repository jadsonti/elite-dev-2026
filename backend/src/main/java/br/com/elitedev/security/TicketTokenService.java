package br.com.elitedev.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TicketTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final SecretKeySpec signingKey;

    public TicketTokenService(@Value("${app.ticket.signing-secret}") String secret) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException(
                    "O segredo de assinatura dos ingressos deve possuir ao menos 32 bytes.");
        }
        signingKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
    }

    public SignedTicketToken generate() {
        UUID code = UUID.randomUUID();
        String signature = sign(code);
        return new SignedTicketToken(code, signature, code + "." + signature);
    }

    public Optional<UUID> verify(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        String[] parts = token.split("\\.", -1);
        if (parts.length != 2) {
            return Optional.empty();
        }

        try {
            UUID code = UUID.fromString(parts[0]);
            byte[] provided = parts[1].getBytes(StandardCharsets.US_ASCII);
            byte[] expected = sign(code).getBytes(StandardCharsets.US_ASCII);
            return MessageDigest.isEqual(provided, expected)
                    ? Optional.of(code)
                    : Optional.empty();
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private String sign(UUID code) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(signingKey);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(
                    mac.doFinal(code.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Não foi possível assinar o ingresso.", exception);
        }
    }

    public record SignedTicketToken(
            UUID code,
            String signature,
            String token
    ) {
    }
}
