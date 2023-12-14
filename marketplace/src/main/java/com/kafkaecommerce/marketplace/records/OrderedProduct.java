package com.kafkaecommerce.marketplace.records;

import java.math.BigDecimal;

public record OrderedProduct(
        int id,
        String title,
        BigDecimal price,
        String description,
        String category,
        String image,
        int quantity
) {
}
