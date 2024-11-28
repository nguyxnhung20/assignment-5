package org.example.service;

import org.example.entity.Product;
import org.example.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final KafkaTemplate<String, Product> kafkaTemplate;

    @Autowired
    public ProductServiceImpl(
            ProductRepository productRepository,
            @Autowired(required = false) KafkaTemplate<String, Product> kafkaTemplate
    ) {
        this.productRepository = productRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    @Cacheable(cacheNames = "products")
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    @Cacheable(cacheNames = "product", key = "#id")
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    @CachePut(cacheNames = "product", key = "#result.id")
    public Product createProduct(Product product) {
        Product savedProduct = productRepository.save(product);

        if (kafkaTemplate != null) {
            try {
                kafkaTemplate.send("new-products", savedProduct);
            } catch (Exception e) {
                // Log the error but don't block the operation
                System.err.println("Failed to send Kafka message: " + e.getMessage());
            }
        }

        return savedProduct;
    }

    @Override
    @CachePut(cacheNames = "product", key = "#id")
    public Optional<Product> updateProduct(Long id, Product productDetails) {
        return productRepository.findById(id)
                .map(product -> {
                    product.setName(productDetails.getName());
                    product.setDescription(productDetails.getDescription());
                    product.setPrice(productDetails.getPrice());

                    Product updatedProduct = productRepository.save(product);

                    if (kafkaTemplate != null) {
                        try {
                            kafkaTemplate.send("updated-products", updatedProduct);
                        } catch (Exception e) {
                            System.err.println("Failed to send Kafka message: " + e.getMessage());
                        }
                    }

                    return updatedProduct;
                });
    }

    @Override
    @CacheEvict(cacheNames = {"products", "product"}, key = "#id")
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);

        if (kafkaTemplate != null) {
            try {
                Product deletedProduct = new Product(id, null, null, null);
                kafkaTemplate.send("deleted-products", deletedProduct);
            } catch (Exception e) {
                System.err.println("Failed to send Kafka message: " + e.getMessage());
            }
        }
    }
}