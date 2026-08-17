package br.com.elitedev.domain.reservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.elitedev.domain.event.Event;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReservationStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    protected Reservation() {
    }

    public Reservation(Event event, User customer, Integer quantity) {
        this.event = event;
        this.customer = customer;
        this.quantity = quantity;
        this.unitPrice = event.getPrice();
        this.totalPrice = event.getPrice().multiply(BigDecimal.valueOf(quantity));
        this.status = ReservationStatus.PENDING_PAYMENT;
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (status == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("A reserva já está cancelada.");
        }
        if (status == ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Uma reserva paga não pode ser cancelada por este fluxo.");
        }
        status = ReservationStatus.CANCELLED;
        cancelledAt = LocalDateTime.now();
    }

    public void confirmPayment() {
        if (status == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Uma reserva cancelada não pode ser paga.");
        }
        if (status == ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("A reserva já está paga.");
        }
        status = ReservationStatus.CONFIRMED;
    }

    public Long getId() { return id; }
    public Event getEvent() { return event; }
    public User getCustomer() { return customer; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public ReservationStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
}
