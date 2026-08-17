package br.com.elitedev.domain.ticket;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.elitedev.domain.reservation.Reservation;
import br.com.elitedev.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    @Column(nullable = false, unique = true, columnDefinition = "uuid")
    private UUID code;

    @Column(nullable = false, length = 43)
    private String signature;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    protected Ticket() {
    }

    public Ticket(
            Reservation reservation,
            Integer sequenceNumber,
            UUID code,
            String signature) {
        this.reservation = reservation;
        this.customer = reservation.getCustomer();
        this.sequenceNumber = sequenceNumber;
        this.code = code;
        this.signature = signature;
        this.status = TicketStatus.ACTIVE;
    }

    @PrePersist
    void prePersist() {
        issuedAt = LocalDateTime.now();
    }

    public String token() {
        return code + "." + signature;
    }

    public void use() {
        if (status == TicketStatus.USED) {
            throw new IllegalStateException("O ingresso já foi utilizado.");
        }
        status = TicketStatus.USED;
        usedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public User getCustomer() {
        return customer;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public UUID getCode() {
        return code;
    }

    public String getSignature() {
        return signature;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }
}
