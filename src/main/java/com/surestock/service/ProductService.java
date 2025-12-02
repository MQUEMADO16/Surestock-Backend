package com.surestock.service;

import com.surestock.dto.ProductDTO;
import com.surestock.model.Product;
import com.surestock.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    /**
     * Creates a new product for a specific business.
     */
    public Product createProduct(ProductDTO dto, Long businessId) {
        // Validation logic
        if (dto.getPrice() < 0) {
            throw new RuntimeException("Price cannot be negative.");
        }
        if (dto.getQuantity() < 0) {
            throw new RuntimeException("Initial quantity cannot be negative.");
        }

        // Map DTO to Entity
        Product product = new Product();
        product.setName(dto.getName());
        product.setSku(dto.getSku());
        product.setPrice(dto.getPrice());
        product.setCost(dto.getCost()); // For Profit Margin Analytics
        product.setQuantity(dto.getQuantity());
        product.setReorderThreshold(dto.getReorderThreshold());

        // Set the Business ID (Critical for Multi-tenancy)
        product.setBusinessId(businessId);

        return productRepository.save(product);
    }

    /**
     * Retrieves all products belonging to a specific business.
     */
    public List<Product> getAllProducts(Long businessId) {
        return productRepository.findByBusinessId(businessId);
    }

    /**
     * Updates the stock level of a product.
     * * Uses @Transactional to define the scope of the update, and relies on
     * Pessimistic Locking in the repository to prevent race conditions.
     * * @param quantityChange Positive to add stock, negative to remove stock.
     */
    @Transactional // Defines the transaction boundary
    public Product updateStock(Long productId, int quantityChange) {
        // NOTE: findById is now expected to have @Lock(PESSIMISTIC_WRITE) in the repository.
        // This locks the product row in the database as soon as it's read.
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        int newQuantity = product.getQuantity() + quantityChange;

        // Prevent negative stock
        if (newQuantity < 0) {
            // Transaction fails, lock is released, and original state is maintained.
            throw new RuntimeException("Insufficient stock! Cannot reduce below 0.");
        }

        // Write operation completes the transaction and releases the lock.
        product.setQuantity(newQuantity);
        return productRepository.save(product);
    }

    /**
     * Deletes a product ensuring it belongs to the business.
     */
    public void deleteProduct(Long productId, Long businessId) {
        Product product = productRepository.findByIdAndBusinessId(productId, businessId)
                .orElseThrow(() -> new RuntimeException("Product not found or access denied."));

        productRepository.delete(product);
    }

    /**
     * Updates editable details of a product (Price, Cost, Threshold, Name, SKU).
     */
    public Product updateProductDetails(Long productId, ProductDTO dto, Long businessId) {
        Product product = productRepository.findByIdAndBusinessId(productId, businessId)
                .orElseThrow(() -> new RuntimeException("Product not found or access denied."));

        // Update fields if provided (and valid)
        if (dto.getName() != null) product.setName(dto.getName());
        if (dto.getSku() != null) product.setSku(dto.getSku());

        if (dto.getPrice() != null && dto.getPrice() >= 0) {
            product.setPrice(dto.getPrice());
        }

        if (dto.getCost() != null && dto.getCost() >= 0) {
            product.setCost(dto.getCost());
        }

        if (dto.getReorderThreshold() != null && dto.getReorderThreshold() >= 0) {
            product.setReorderThreshold(dto.getReorderThreshold());
        }

        return productRepository.save(product);
    }
}