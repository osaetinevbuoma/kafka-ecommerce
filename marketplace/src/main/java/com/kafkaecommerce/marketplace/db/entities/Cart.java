package com.kafkaecommerce.marketplace.db.entities;

import jakarta.persistence.Entity;

@Entity
public class Cart extends BaseEntity {
    private int userId;
    private int productId;
    private int quantity;

    public Cart() {
    }

    public Cart(int userId, int productId, int quantity) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public int getUserId() {
        return userId;
    }

    public int getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }
}
