package com.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Represents a sellable product in the e-commerce catalogue.
 *
 * <p>Relationships:</p>
 * <ul>
 *   <li><b>ManyToOne → {@link Category}</b>: Many products belong to one
 *       category. This side owns the foreign-key column {@code category_id}.</li>
 *   <li><b>OneToMany → {@link OrderItem}</b>: One product can appear in many
 *       order items (handled on the {@link OrderItem} side).</li>
 * </ul>
 *
 * <p>{@code FetchType.LAZY} on the category association avoids an extra JOIN
 * on every product query; the category is loaded only when accessed.</p>
 *
 * @author  Your Name
 * @version 1.0
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    /** Primary key, auto-incremented by the database. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Display name of the product. Cannot be blank. */
    @NotBlank(message = "Product name is required")
    @Column(nullable = false, length = 200)
    private String name;

    /** Detailed description of the product. */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Retail price of the product.
     * Uses {@link BigDecimal} for precise monetary arithmetic.
     * Must be a positive value with at most 2 decimal places.
     */
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** Current stock quantity. Cannot be negative. */
    @Min(value = 0, message = "Stock cannot be negative")
    @Column(nullable = false)
    private int stock;

    /** URL of the product image (optional). */
    @Column(length = 500)
    private String imageUrl;

    /**
     * The category this product belongs to.
     *
     * <p>{@code @ManyToOne} with {@code FetchType.LAZY}: the category row is
     * fetched only when {@code getCategory()} is called, not on every product
     * query. {@code @JoinColumn} specifies the FK column name in the DB.</p>
     *
     * <p>{@code @JsonBackReference} prevents infinite recursion in JSON
     * serialization by omitting this side of the bidirectional reference.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonBackReference
    private Category category;

    /** Convenience getter that exposes the category name without the full object. */
    @Transient
    public String getCategoryName() {
        return category != null ? category.getName() : null;
    }
}
