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
}