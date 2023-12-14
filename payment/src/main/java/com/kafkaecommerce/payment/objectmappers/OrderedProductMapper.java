package com.kafkaecommerce.payment.objectmappers;

import com.kafkaecommerce.payment.db.entities.Product;
import com.kafkaecommerce.payment.records.OrderedProduct;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class OrderedProductMapper implements Function<Product, OrderedProduct> {
    @Override
    public OrderedProduct apply(Product product) {
        return new OrderedProduct(
                product.getTitle(),
                product.getPrice(),
                product.getDescription(),
                product.getCategory(),
                product.getImage(),
                product.getQuantity()
        );
    }
}
