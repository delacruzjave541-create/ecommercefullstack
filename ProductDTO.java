package com.ecommerce.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for {@code Product} to decouple the API contract
 * from the JPA entity and prevent Jackson from chasing lazy-loaded proxies.
 *
 * <p>Used for both request payloads (POST/PUT) and response bodies (GET).</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Long id;

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @Min(value = 0, message = "Stock cannot be negative")
    private int stock;

    private String imageUrl;

    /** ID of the category this product belongs to (used in POST/PUT requests). */
    private Long categoryId;

    /** Category name – populated in GET responses for easy display. */
    private String categoryName;
}
