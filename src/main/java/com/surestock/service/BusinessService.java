package com.surestock.service;

import com.surestock.model.Business;
import com.surestock.repository.BusinessRepository;
import com.surestock.repository.ProductRepository;
import com.surestock.repository.TransactionRepository;
import com.surestock.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BusinessService {

    @Autowired private BusinessRepository businessRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private TransactionRepository transactionRepository;

    /**
     * Deletes the entire business tenant and ALL associated data (products, sales, users).
     * This is an atomic operation: all deletes must succeed, or all fail.
     * * @param businessId The ID of the tenant to delete.
     */
    @Transactional
    public void deleteBusinessAndAllData(Long businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Business not found."));

        // Delete dependent data (Products and Transactions)
        productRepository.deleteByBusinessId(businessId);
        transactionRepository.deleteByBusinessId(businessId);

        // Delete the users (Owner/Employees)
        userRepository.deleteByBusinessId(businessId);

        // Delete the parent entity
        businessRepository.delete(business);
    }
}