package com.surestock.repository;

import com.surestock.model.Product;
import jakarta.persistence.LockModeType;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Finds all products associated with a specific business ID.
     * This ensures multi-tenancy: a user only sees their own inventory.
     *
     * @param businessId The unique identifier of the business.
     * @return A list of Product entities belonging to the business.
     */
    List<Product> findByBusinessId(Long businessId);

    /**
     * Finds a specific product by its ID and confirms it belongs to the given business.
     * Used for read and update operations where access control is required.
     *
     * @param id The ID of the product.
     * @param businessId The unique identifier of the business.
     * @return An Optional containing the Product if found and owned by the business.
     */
    Optional<Product> findByIdAndBusinessId(Long id, Long businessId);

    /**
     * Finds a product by its ID and applies a Pessimistic Write Lock.
     * This is critical for preventing race conditions during stock updates (Read-Modify-Write cycle).
     * Any other transaction attempting to read or update this row will be blocked until the
     * current transaction (updateStock) is complete.
     *
     * @param id The ID of the product.
     * @return An Optional containing the locked Product.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @NotNull
    Optional<Product> findById(@NotNull Long id);

    /**
     * Deletes all products belonging to a specific business ID.
     */
    @Modifying
    @Transactional
    void deleteByBusinessId(Long businessId);
}