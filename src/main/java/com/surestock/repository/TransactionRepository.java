package com.surestock.repository;

import com.surestock.model.SalesTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<SalesTransaction, Long> {

    @EntityGraph(attributePaths = {"product"})
    List<SalesTransaction> findByBusinessId(Long businessId);

    @EntityGraph(attributePaths = {"product"})
    List<SalesTransaction> findByBusinessIdOrderByTimestampDesc(Long businessId);

    // Used for Sales Reports
    List<SalesTransaction> findByBusinessIdAndTimestampBetween(Long businessId, LocalDateTime start, LocalDateTime end);

    // Used for Top Sellers Report
    @Query("SELECT t.product, SUM(t.quantitySold) FROM SalesTransaction t " +
            "WHERE t.businessId = :businessId " +
            "GROUP BY t.product " +
            "ORDER BY SUM(t.quantitySold) DESC")
    List<Object[]> findTopSellingProducts(@Param("businessId") Long businessId, Pageable pageable);

    @Modifying
    @Transactional
    void deleteByBusinessId(Long businessId);
}