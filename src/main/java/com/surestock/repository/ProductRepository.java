package com.surestock.repository;

import com.surestock.model.Product;
import jakarta.persistence.LockModeType;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Finds all products associated with a specific business ID.
     * This ensures multi-tenancy: a user only sees their own inventory.
     */
    List<Product> findByBusinessId(Long businessId);

    /**
     * Finds a specific product by its ID and confirms it belongs to the given business.
     * Used for read and update operations where access control is required.
     */
    Optional<Product> findByIdAndBusinessId(Long id, Long businessId);

    /**
     * STANDARD READ (No Lock).
     * Overrides JpaRepository's findById to ensure it's lock-free by default.
     * Use this for checking existence, ownership, or displaying data.
     */
    @NotNull
    @Override
    Optional<Product> findById(@NotNull Long id);

    /**
     * SPECIALIZED LOCKING READ.
     * Finds a product by its ID and applies a Pessimistic Write Lock.
     * This is critical for preventing race conditions during stock updates.
     * Only call this from inside a @Transactional service method where you intend to WRITE.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    /**
     * Deletes all products belonging to a specific business ID.
     */
    @Modifying
    @Transactional
    void deleteByBusinessId(Long businessId);
}