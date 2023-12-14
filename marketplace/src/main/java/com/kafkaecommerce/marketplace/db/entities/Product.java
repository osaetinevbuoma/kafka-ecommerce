package com.kafkaecommerce.marketplace.db.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
public class Product extends BaseEntity {
    private String title;
    private BigDecimal price;
    @Column(columnDefinition = "text")
    private String description;
    private String category;
    private String image;

    public Product() {
    }

    public Product(String title, double price, String description, String category, String image) {
        this.title = title;
        this.price = BigDecimal.valueOf(price);
        this.description = description;
        this.category = category;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getImage() {
        return image;
    }
}
