package com.surestock.service;

import com.surestock.dto.BusinessSettingsDTO;
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

    public Business getBusinessById(Long businessId) {
        return businessRepository.findById(businessId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Business not found."));
    }

    /**
     * Updates the configurable settings for a business.
     */
    @Transactional
    public Business updateSettings(Long businessId, BusinessSettingsDTO dto) {
        Business business = getBusinessById(businessId);

        if (dto.getName() != null) business.setName(dto.getName());
        if (dto.getCurrency() != null) business.setCurrency(dto.getCurrency());
        if (dto.getTaxRate() != null) {
            if (dto.getTaxRate() < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tax rate cannot be negative.");
            business.setTaxRate(dto.getTaxRate());
        }
        if (dto.getLowStockThreshold() != null) {
            if (dto.getLowStockThreshold() < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Threshold cannot be negative.");
            business.setLowStockThreshold(dto.getLowStockThreshold());
        }
        if (dto.getContactAddress() != null) business.setContactAddress(dto.getContactAddress());

        return businessRepository.save(business);
    }

    /**
     * Deletes the entire business tenant and ALL associated data (products, sales, users).
     * This is an atomic operation: all deletes must succeed, or all fail.
     * * @param businessId The ID of the tenant to delete.
     */
    @Transactional
    public void deleteBusinessAndAllData(Long businessId) {
        Business business = getBusinessById(businessId);

        // Delete dependent data (Products and Transactions)
        transactionRepository.deleteByBusinessId(businessId);
        productRepository.deleteByBusinessId(businessId);

        // Delete the users (Owner/Employees)
        userRepository.deleteByBusinessId(businessId);

        // Delete the parent entity
        businessRepository.delete(business);
    }
}