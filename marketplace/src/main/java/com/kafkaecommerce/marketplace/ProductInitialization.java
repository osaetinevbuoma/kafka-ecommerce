package com.kafkaecommerce.marketplace;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkaecommerce.marketplace.db.entities.Product;
import com.kafkaecommerce.marketplace.db.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
public class ProductInitialization implements ApplicationListener<ApplicationContextEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductInitialization.class);
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProductInitialization(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @EventListener
    public void onApplicationEvent(ApplicationContextEvent event) {
        initializeProducts();
    }

    private void initializeProducts() {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://fakestoreapi.com/products"))
                    .GET()
                    .build();

            List<Product> products = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(response -> {
                        try {
                            com.kafkaecommerce.marketplace.records.Product[] productsArr = objectMapper.readValue(
                                    response, com.kafkaecommerce.marketplace.records.Product[].class
                            );
                            return Arrays.asList(productsArr);
                        } catch (JsonProcessingException e) {
                            LOGGER.error(e.getMessage());
                            return null;
                        }
                    })
                    .thenApply(
                            productRecords -> productRecords.stream()
                                    .map(
                                            productRecord -> new Product(
                                                    productRecord.title(),
                                                    productRecord.price().doubleValue(),
                                                    productRecord.description(),
                                                    productRecord.category(),
                                                    productRecord.image()
                                            )
                                    )
                                    .toList()
                    )
                    .get();

            this.productRepository.saveAll(products);
        } catch (ExecutionException | InterruptedException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
