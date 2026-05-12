package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Spring Data JPA repository for {@link Product} entities.
 *
 * <p>Extends {@link JpaRepository} to inherit standard CRUD operations
 * (save, findById, findAll, delete, count, etc.) without any boilerplate.</p>
 *
 * <p>Custom finders use two techniques:</p>
 * <ol>
 *   <li><b>Method-name derivation</b> – Spring parses the method name and
 *       generates the JPQL automatically at startup.</li>
 *   <li><b>{@code @Query}</b> – explicit JPQL for queries that are too
 *       complex or ambiguous for name derivation.</li>
 * </ol>
 *
 * @author  Your Name
 * @version 1.0
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Finds all products whose associated category has the given name.
     *
     * <p>Method-name derivation: Spring generates
     * {@code SELECT p FROM Product p WHERE p.category.name = ?1}.</p>
     *
     * @param name the category name to filter by (case-sensitive)
     * @return list of matching products, empty if none found
     */
    List<Product> findByCategoryName(String name);

    /**
     * Finds all products whose name contains the given keyword (case-insensitive).
     *
     * <p>Method-name derivation with {@code Containing} + {@code IgnoreCase}.</p>
     *
     * @param keyword the search term
     * @return list of matching products
     */
    List<Product> findByNameContainingIgnoreCase(String keyword);

    /**
     * Finds all products whose price falls within the given inclusive range.
     *
     * <p>Uses an explicit {@code @Query} with JPQL and named parameters
     * ({@code :min}, {@code :max}) for clarity.</p>
     *
     * @param min lower bound (inclusive)
     * @param max upper bound (inclusive)
     * @return list of products in the price range
     */
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :min AND :max ORDER BY p.price ASC")
    List<Product> findByPriceRange(
            @Param("min") BigDecimal min,
            @Param("max") BigDecimal max
    );

    /**
     * Finds all products that are currently in stock (stock > 0).
     *
     * <p>Method-name derivation: {@code GreaterThan} maps to a {@code >}
     * comparison in the generated JPQL.</p>
     *
     * @return list of in-stock products
     */
    List<Product> findByStockGreaterThan(int stock);
}
