package com.surestock.repository;

import com.surestock.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByBusinessId(Long businessId);

    // Find a specific product ensuring it belongs to the business
    Optional<Product> findByIdAndBusinessId(Long id, Long businessId);
}