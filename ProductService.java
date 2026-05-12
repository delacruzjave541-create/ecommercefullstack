package com.ecommerce.service;

import com.ecommerce.dto.ProductDTO;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for product-related business logic.
 *
 * <p>All ArrayList/manual storage logic from Lab 7 has been removed.
 * Data is now persisted to and read from a MySQL database via
 * {@link ProductRepository} (Spring Data JPA).</p>
 *
 * <p>{@code @Transactional} on mutating methods ensures that all DB
 * operations within a method succeed or are rolled back together.</p>
 *
 * @author  Your Name
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // ──────────────────────────────────────────────────────────────────────
    // Read operations
    // ──────────────────────────────────────────────────────────────────────

    /** Returns all products as DTOs. */
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** Returns a single product by its ID, or throws 404. */
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        Product product = findProductOrThrow(id);
        return toDTO(product);
    }

    /** Returns all products belonging to the given category name. */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategory(String categoryName) {
        return productRepository.findByCategoryName(categoryName)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** Returns all products within the given price range (inclusive). */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByPriceRange(BigDecimal min, BigDecimal max) {
        return productRepository.findByPriceRange(min, max)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** Returns all products whose name contains the search keyword. */
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────────────────────────────
    // Mutating operations
    // ──────────────────────────────────────────────────────────────────────

    /** Persists a new product to the database and returns the saved DTO. */
    @Transactional
    public ProductDTO createProduct(ProductDTO dto) {
        Product product = toEntity(dto);
        Product saved = productRepository.save(product);
        return toDTO(saved);
    }

    /** Updates an existing product; throws 404 if not found. */
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        Product existing = findProductOrThrow(id);

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setPrice(dto.getPrice());
        existing.setStock(dto.getStock());
        existing.setImageUrl(dto.getImageUrl());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Category not found with id: " + dto.getCategoryId()));
            existing.setCategory(category);
        }

        return toDTO(productRepository.save(existing));
    }

    /** Deletes a product by ID; throws 404 if not found. */
    @Transactional
    public void deleteProduct(Long id) {
        Product product = findProductOrThrow(id);
        productRepository.delete(product);
    }

    // ──────────────────────────────────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────────────────────────────────

    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Product not found with id: " + id));
    }

    /** Maps a {@link ProductDTO} (request) to a {@link Product} entity. */
    private Product toEntity(ProductDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setImageUrl(dto.getImageUrl());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Category not found with id: " + dto.getCategoryId()));
            product.setCategory(category);
        }
        return product;
    }

    /** Maps a {@link Product} entity to a {@link ProductDTO} (response). */
    private ProductDTO toDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategoryName())
                .build();
    }
}
