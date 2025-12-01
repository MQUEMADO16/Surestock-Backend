package com.surestock.repository;

import com.surestock.model.SalesTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<SalesTransaction, Long> {
    List<SalesTransaction> findByBusinessId(Long businessId);
}