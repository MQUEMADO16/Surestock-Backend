package com.surestock.service;

import com.surestock.dto.transaction.SaleItemDTO;
import com.surestock.dto.transaction.SaleRequestDTO;
import com.surestock.model.Product;
import com.surestock.model.SalesTransaction;
import com.surestock.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SalesService {

    @Autowired
    private ProductService productService;

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Processes a full sale transaction.
     * Iterates through items.
     * Deducts stock (Atomic & Safe).
     * Records the transaction log.
     */
    @Transactional(readOnly = false, isolation = Isolation.READ_COMMITTED)
    public List<SalesTransaction> processSale(Long businessId, SaleRequestDTO request) {
        List<SalesTransaction> transactions = new ArrayList<>();

        for (SaleItemDTO item : request.getItems()) {
            // Deduct Stock (This handles locking internally via ProductService)
            // We pass negative quantity to deduct
            Product updatedProduct = productService.updateStock(item.getProductId(), -item.getQuantity());

            // Create Transaction Record
            SalesTransaction transaction = new SalesTransaction();
            transaction.setBusinessId(businessId);
            transaction.setProduct(updatedProduct);
            transaction.setQuantitySold(item.getQuantity());

            // Calculate Total Price (Price * Qty)
            // Note: In a real app, we would apply tax here using BusinessService
            double total = updatedProduct.getPrice() * item.getQuantity();
            transaction.setTotalPrice(total);

            transaction.setTimestamp(LocalDateTime.now());

            transactions.add(transactionRepository.save(transaction));
        }

        return transactions;
    }
}