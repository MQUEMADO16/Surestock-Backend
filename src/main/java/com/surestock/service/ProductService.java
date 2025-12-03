package com.surestock.service;

import com.surestock.controller.ProductController.ProductDetailsUpdateRequest;
import com.surestock.dto.ProductDTO;
import com.surestock.model.Business;
import com.surestock.model.Product;
import com.surestock.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BusinessService businessService; // Inject BusinessService to read defaults

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public Product createProduct(ProductDTO dto, Long businessId) {
        if (dto.getPrice() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price cannot be negative.");
        }
        if (dto.getQuantity() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Initial quantity cannot be negative.");
        }

        Product product = new Product();
        product.setName(dto.getName());
        product.setSku(dto.getSku());
        product.setPrice(dto.getPrice());
        product.setCost(dto.getCost());
        product.setQuantity(dto.getQuantity());
        product.setBusinessId(businessId);

        if (dto.getReorderThreshold() != null) {
            // Use the specific value provided in the DTO
            if (dto.getReorderThreshold() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Threshold cannot be negative.");
            }
            product.setReorderThreshold(dto.getReorderThreshold());
        } else {
            // Fallback to Business Default
            Business business = businessService.getBusinessById(businessId);
            product.setReorderThreshold(business.getLowStockThreshold());
        }

        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProducts(Long businessId) {
        return productRepository.findByBusinessId(businessId);
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + id));
    }

    @Transactional(readOnly = false, isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public Product updateStock(Long productId, int quantityChange) {
        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        int newQuantity = product.getQuantity() + quantityChange;

        if (newQuantity < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient stock! Cannot reduce below 0.");
        }

        product.setQuantity(newQuantity);
        return productRepository.save(product);
    }

    @Transactional(readOnly = false, isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public Product updateProductDetails(Long productId, ProductDetailsUpdateRequest request) {
        Product product = getProductById(productId);

        if (request.getName() != null) product.setName(request.getName());
        if (request.getSku() != null) product.setSku(request.getSku());

        if (request.getPrice() != null) {
            if (request.getPrice() < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price cannot be negative.");
            product.setPrice(request.getPrice());
        }
        if (request.getCost() != null) {
            if (request.getCost() < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cost cannot be negative.");
            product.setCost(request.getCost());
        }

        if (request.getReorderThreshold() != null) {
            if (request.getReorderThreshold() < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Threshold cannot be negative.");
            product.setReorderThreshold(request.getReorderThreshold());
        }

        return productRepository.save(product);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public void deleteProduct(Long productId, Long businessId) {
        Product product = productRepository.findByIdAndBusinessId(productId, businessId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found or access denied."));

        productRepository.delete(product);
    }
}