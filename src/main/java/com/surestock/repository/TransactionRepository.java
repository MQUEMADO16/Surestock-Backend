package com.surestock.repository;

import com.surestock.model.SalesTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<SalesTransaction, Long> {

    // For analytics: Find transactions by business
    List<SalesTransaction> findByBusinessId(Long businessId);

    /**
     * Deletes all sales transactions belonging to a specific business ID.
     */
    @Modifying
    @Transactional
    void deleteByBusinessId(Long businessId);
}