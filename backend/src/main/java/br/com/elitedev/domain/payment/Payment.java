package br.com.elitedev.domain.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "processed_at", nullable = false, updatable = false)
    private LocalDateTime processedAt;

    protected Payment() {
    }

    public Payment(
            Reservation reservation,
            User customer,
            PaymentStatus status,
            String failureReason) {
        this.reservation = reservation;
        this.customer = customer;
        this.amount = reservation.getTotalPrice();
        this.status = status;
        this.failureReason = failureReason;
    }

    @PrePersist
    void prePersist() {
        processedAt = LocalDateTime.now();
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

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
}
