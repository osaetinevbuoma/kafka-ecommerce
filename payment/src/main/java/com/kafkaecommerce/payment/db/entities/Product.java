package com.kafkaecommerce.payment.db.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;

@Entity
public class Product extends BaseEntity {
    private String title;
    private BigDecimal price;
    @Column(columnDefinition = "text")
    private String description;
    private String category;
    private String image;
    private int quantity;

    @ManyToOne(targetEntity = Order.class)
    private Order order;

    public Product() {
    }

    public Product(
            String title,
            BigDecimal price,
            String description,
            String category,
            String image,
            int quantity,
            Order order
    ) {
        this.title = title;
        this.price = price;
        this.description = description;
        this.category = category;
        this.image = image;
        this.quantity = quantity;
        this.order = order;
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

    public int getQuantity() {
        return quantity;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
