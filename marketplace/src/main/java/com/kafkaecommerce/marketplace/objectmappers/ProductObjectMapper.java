package com.kafkaecommerce.marketplace.objectmappers;

import com.kafkaecommerce.marketplace.db.entities.Product;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class ProductObjectMapper implements Function<Product, com.kafkaecommerce.marketplace.records.Product> {
    @Override
    public com.kafkaecommerce.marketplace.records.Product apply(Product product) {
        return new com.kafkaecommerce.marketplace.records.Product(
                product.getId(),
                product.getTitle(),
                product.getPrice(),
                product.getDescription(),
                product.getCategory(),
                product.getImage()
        );
    }
}
