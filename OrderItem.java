package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Represents a single line item within an {@link Order}.
 *
 * <p>Relationships:</p>
 * <ul>
 *   <li><b>ManyToOne → {@link Order}</b>: Many order items belong to one
 *       order. This side owns the FK column {@code order_id}.</li>
 *   <li><b>ManyToOne → {@link Product}</b>: Many order items can reference
 *       the same product. This side owns the FK column {@code product_id}.</li>
 * </ul>
 *
 * <p>Both associations use {@code FetchType.LAZY} to avoid unnecessary joins
 * when querying order items in bulk.</p>
 *
 * @author  Your Name
 * @version 1.0
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    /** Primary key, auto-incremented by the database. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The parent order this item belongs to.
     *
     * <p>{@code FetchType.LAZY}: the Order row is not joined unless
     * {@code getOrder()} is explicitly called.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * The product referenced by this line item.
     *
     * <p>No cascade here – we do not want deleting an order item to
     * accidentally delete the underlying product from the catalogue.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Number of units of the product ordered. Must be at least 1. */
    @Column(nullable = false)
    private int quantity;

    /**
     * Unit price captured at the time the order was placed.
     * Stored separately from {@code product.price} so that subsequent
     * price changes do not retroactively alter historical orders.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Calculates the subtotal for this line item.
     *
     * @return {@code unitPrice × quantity} as a {@link BigDecimal}
     */
    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
