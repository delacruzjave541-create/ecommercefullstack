package com.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a product category (e.g., Electronics, Clothing).
 *
 * <p>Relationship: One {@code Category} has many {@link Product} entities.
 * This is the "One" side of a {@code @OneToMany} bidirectional mapping.
 * {@code CascadeType.ALL} ensures that any category-level persistence
 * operation (save, delete) cascades down to owned products.
 * {@code FetchType.LAZY} defers loading the products list until it is
 * explicitly accessed, improving performance for large datasets.</p>
 *
 * @author  Your Name
 * @version 1.0
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    /** Primary key, auto-incremented by the database. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-readable category name (must be unique and non-null). */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * The list of products belonging to this category.
     *
     * <ul>
     *   <li>{@code mappedBy = "category"} tells JPA that the
     *       {@code Product.category} field owns the FK column.</li>
     *   <li>{@code CascadeType.ALL} propagates all state changes.</li>
     *   <li>{@code FetchType.LAZY} loads products only on demand.</li>
     *   <li>{@code orphanRemoval = true} deletes a product row when it
     *       is removed from this list.</li>
     * </ul>
     */
    @OneToMany(
            mappedBy = "category",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    @JsonManagedReference   // prevents infinite recursion during JSON serialization
    @Builder.Default
    private List<Product> products = new ArrayList<>();
}
