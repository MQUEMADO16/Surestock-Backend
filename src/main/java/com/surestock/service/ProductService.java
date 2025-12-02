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

    public Product createProduct(ProductDTO dto, Long businessId) {
        if (dto.getPrice() < 0) {
            throw new RuntimeException("Price cannot be negative.");
        }
        if (dto.getQuantity() < 0) {
            throw new RuntimeException("Initial quantity cannot be negative.");
        }

        Product product = new Product();
        product.setName(dto.getName());
        product.setSku(dto.getSku());
        product.setPrice(dto.getPrice());
        product.setQuantity(dto.getQuantity());
        product.setReorderThreshold(dto.getReorderThreshold());
        product.setBusinessId(businessId);

        return productRepository.save(product);
    }

    public List<Product> getAllProducts(Long businessId) {
        return productRepository.findByBusinessId(businessId);
    }

    public Product updateStock(Long productId, int quantityChange) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        int newQuantity = product.getQuantity() + quantityChange;
        if (newQuantity < 0) {
            throw new RuntimeException("Insufficient stock! Cannot reduce below 0.");
        }

        product.setQuantity(newQuantity);
        return productRepository.save(product);
    }

    public void deleteProduct(Long productId, Long businessId) {
        // Ensure the product exists AND belongs to this business
        Product product = productRepository.findByIdAndBusinessId(productId, businessId)
                .orElseThrow(() -> new RuntimeException("Product not found or access denied."));

        productRepository.delete(product);
    }
}