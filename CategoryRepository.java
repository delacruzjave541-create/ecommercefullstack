package com.ecommerce.repository;

import com.ecommerce.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Category} entities.
 *
 * <p>Inherits full CRUD from {@link JpaRepository}.
 * The custom {@code findByName} finder uses method-name derivation
 * to locate a category by its unique name.</p>
 *
 * @author  Your Name
 * @version 1.0
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Finds a category by its exact name.
     *
     * @param name the category name
     * @return an {@link Optional} containing the category, or empty if not found
     */
    Optional<Category> findByName(String name);
}
