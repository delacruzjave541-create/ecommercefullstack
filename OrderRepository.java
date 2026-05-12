package com.ecommerce.repository;

import com.ecommerce.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Order} entities.
 *
 * @author  Your Name
 * @version 1.0
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Finds all orders placed by a specific customer (by email).
     *
     * @param email the customer's email address
     * @return list of orders for that customer
     */
    List<Order> findByCustomerEmailIgnoreCase(String email);
}
