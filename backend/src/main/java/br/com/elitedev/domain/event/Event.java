package br.com.elitedev.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_source", length = 30)
    private String externalSource;

    @Column(name = "external_id", length = 100)
    private String externalId;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "event_date_time", nullable = false)
    private LocalDateTime eventDateTime;

    @Column(nullable = false, length = 255)
    private String location;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EventStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Event() {
    }

    public Event(
            String externalSource,
            String externalId,
            String title,
            String description,
            String imageUrl,
            LocalDateTime eventDateTime,
            String location,
            Integer capacity,
            BigDecimal price,
            User createdBy) {

        this.externalSource = externalSource;
        this.externalId = externalId;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.eventDateTime = eventDateTime;
        this.location = location;
        this.capacity = capacity;
        this.availableQuantity = capacity;
        this.price = price;
        this.status = EventStatus.DRAFT;
        this.createdBy = createdBy;
    }

    @PrePersist
    public void prePersist() {

        LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void update(
            String title,
            String description,
            String imageUrl,
            LocalDateTime eventDateTime,
            String location,
            Integer capacity,
            BigDecimal price) {

        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.eventDateTime = eventDateTime;
        this.location = location;

        if (!this.capacity.equals(capacity)) {

            int quantityAlreadyReserved =
                    this.capacity - this.availableQuantity;

            if (capacity < quantityAlreadyReserved) {
                throw new IllegalArgumentException(
                        "A capacidade não pode ser menor que a quantidade já reservada."
                );
            }

            this.capacity = capacity;
            this.availableQuantity =
                    capacity - quantityAlreadyReserved;
        }

        this.price = price;
    }

    public void publish() {
        this.status = EventStatus.PUBLISHED;
    }

    public void cancel() {
        this.status = EventStatus.CANCELLED;
    }

    public void reserve(Integer quantity) {
        if (status != EventStatus.PUBLISHED) {
            throw new IllegalStateException("Somente eventos publicados aceitam reservas.");
        }
        if (!eventDateTime.isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Não é possível reservar um evento encerrado.");
        }
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        if (availableQuantity < quantity) {
            throw new IllegalStateException("Quantidade de ingressos indisponível.");
        }
        availableQuantity -= quantity;
    }

    public void release(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        if (availableQuantity + quantity > capacity) {
            throw new IllegalStateException("A devolução ultrapassa a capacidade do evento.");
        }
        availableQuantity += quantity;
    }

    public Long getId() {
        return id;
    }

    public String getExternalSource() {
        return externalSource;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public LocalDateTime getEventDateTime() {
        return eventDateTime;
    }

    public String getLocation() {
        return location;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public EventStatus getStatus() {
        return status;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
