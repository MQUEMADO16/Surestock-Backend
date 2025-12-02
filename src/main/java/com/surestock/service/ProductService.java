package com.surestock.service;

import com.surestock.dto.ProductDTO;
import com.surestock.model.Product;
import com.surestock.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
     * @param quantityChange Positive to add stock, negative to remove stock.
     */
    public Product updateStock(Long productId, int quantityChange) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        int newQuantity = product.getQuantity() + quantityChange;

        // Prevent negative stock
        if (newQuantity < 0) {
            throw new RuntimeException("Insufficient stock! Cannot reduce below 0.");
        }

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

        // Note: Quantity updates are typically handled via updateStock,
        // but could be forcefully overridden here if desired.

        return productRepository.save(product);
    }
}