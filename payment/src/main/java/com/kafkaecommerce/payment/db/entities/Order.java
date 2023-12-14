package com.kafkaecommerce.payment.db.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order extends BaseEntity {
    private int userId;
    private UUID orderId;
    private Boolean isProcessed;
    private BigDecimal totalPrice;

    @OneToMany(targetEntity = Product.class, mappedBy = "order", cascade = CascadeType.REMOVE)
    private List<Product> products;

    public Order() {
    }

    public Order(int userId, UUID orderId) {
        this.userId = userId;
        this.orderId = orderId;
        this.isProcessed = false;
    }

    public int getUserId() {
        return userId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public boolean isProcessed() {
        return isProcessed;
    }

    public void setIsProcessed(boolean isProcessed) {
        this.isProcessed = isProcessed;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<Product> getProducts() {
        return products;
    }

}
