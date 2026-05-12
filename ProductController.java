package com.ecommerce.controller;

import com.ecommerce.dto.ProductDTO;
import com.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller exposing product-related endpoints.
 *
 * <p>All endpoints are now backed by a real MySQL database via
 * {@link ProductService}. No hardcoded arrays remain.</p>
 *
 * <p>Base path: {@code /api/products}</p>
 *
 * @author  Your Name
 * @version 1.0
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ── GET /api/products ─────────────────────────────────────────────────
    /** Returns all products, or filtered by optional query parameters. */
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        List<ProductDTO> products;

        if (category != null && !category.isBlank()) {
            products = productService.getProductsByCategory(category);
        } else if (search != null && !search.isBlank()) {
            products = productService.searchProducts(search);
        } else if (minPrice != null && maxPrice != null) {
            products = productService.getProductsByPriceRange(minPrice, maxPrice);
        } else {
            products = productService.getAllProducts();
        }

        return ResponseEntity.ok(products);
    }

    // ── GET /api/products/{id} ────────────────────────────────────────────
    /** Returns a single product by ID. Responds 404 if not found. */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // ── POST /api/products ────────────────────────────────────────────────
    /** Creates and persists a new product. Returns 201 Created. */
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO dto) {
        ProductDTO created = productService.createProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ── PUT /api/products/{id} ────────────────────────────────────────────
    /** Updates an existing product. Responds 404 if not found. */
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO dto
    ) {
        return ResponseEntity.ok(productService.updateProduct(id, dto));
    }

    // ── DELETE /api/products/{id} ─────────────────────────────────────────
    /** Deletes a product by ID. Responds 204 No Content. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
