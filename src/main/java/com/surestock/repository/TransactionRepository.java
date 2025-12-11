package com.surestock.repository;

import com.surestock.model.SalesTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Repository
public interface TransactionRepository extends JpaRepository<SalesTransaction, Long> {

    // For analytics: Find transactions by business
    List<SalesTransaction> findByBusinessId(Long businessId);

    List<SalesTransaction> findByBusinessIdOrderByTimestampDesc(Long businessId);

    /**
     * Deletes all sales transactions belonging to a specific business ID.
     */
    @Modifying
    @Transactional
    void deleteByBusinessId(Long businessId);

    /**
     * Finds sales transactions for a business within a specific date range.
     */
    List<SalesTransaction> findByBusinessIdAndTimestampBetween(
            Long businessId,
            java.time.LocalDateTime startDate,
            java.time.LocalDateTime endDate);

    /**
     * Aggregates sales volume and ranks products for the Top Sellers report.
     * Returns a list of Map<String, Object> where keys are "productId" and "totalQuantitySold".
     */
    @Query("SELECT new map(st.productId as productId, SUM(st.quantitySold) as totalQuantitySold) " +
            "FROM SalesTransaction st " +
            "WHERE st.businessId = :businessId AND st.timestamp >= :startDate " +
            "GROUP BY st.productId " +
            "ORDER BY totalQuantitySold DESC")
    List<Map<String, Object>> findTopSellingProducts(
            @Param("businessId") Long businessId,
            @Param("startDate") java.time.LocalDateTime startDate);
}