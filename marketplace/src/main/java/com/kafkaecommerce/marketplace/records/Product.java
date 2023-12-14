package com.kafkaecommerce.marketplace.records;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;


@JsonIgnoreProperties(ignoreUnknown = true)
public record Product(
        int id,
        String title,
        BigDecimal price,
        String description,
        String category,
        String image
) {
}
