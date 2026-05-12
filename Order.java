package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer purchase order.
 *
 * <p>Relationship: One {@code Order} has many {@link OrderItem} entities.
 * This is the "One" side of a {@code @OneToMany} bidirectional mapping.
 * {@code CascadeType.ALL} ensures that saving or deleting an order also
 * saves or deletes its line items, eliminating the need for explicit
 * {@code orderItemRepository.save()} calls.</p>
 *
 * <p>{@code FetchType.LAZY} on {@code items} means the list is not loaded
 * until it is accessed, which is the recommended default for collections.</p>
 *
 * @author  Your Name
 * @version 1.0
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    /** Primary key, auto-incremented by the database. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Name of the customer who placed the order. */
    @Column(nullable = false, length = 150)
    private String customerName;

    /** Customer email address for notifications. */
    @Column(nullable = false, length = 200)
    private String customerEmail;

    /** Timestamp when the order was created (set automatically). */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Current status of the order.
     * Valid values: PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    /**
     * The line items belonging to this order.
     *
     * <ul>
     *   <li>{@code mappedBy = "order"} – the FK column lives in
     *       {@link OrderItem}, not here.</li>
     *   <li>{@code CascadeType.ALL} – persisting/removing an order
     *       automatically cascades to all its items.</li>
     *   <li>{@code orphanRemoval = true} – removing an item from the
     *       list will DELETE that row from the database.</li>
     * </ul>
     */
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    /** Sets the creation timestamp before the entity is first persisted. */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Calculates the total cost of the order by summing all item subtotals.
     *
     * @return the grand total as a {@link BigDecimal}
     */
    public BigDecimal getTotal() {
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Lifecycle statuses an order can go through. */
    public enum OrderStatus {
        PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED
    }
}
