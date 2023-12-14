package com.kafkaecommerce.payment.db.respositories;

import com.kafkaecommerce.payment.db.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findAllByIsProcessed(boolean isProcessed);

    Optional<Order> findByOrderId(UUID orderId);
}
