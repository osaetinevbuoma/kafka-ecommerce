package com.kafkaecommerce.payment.records;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderedProduct(
        String title,
        BigDecimal price,
        String description,
        String category,
        String image,
        int quantity
) {
}
