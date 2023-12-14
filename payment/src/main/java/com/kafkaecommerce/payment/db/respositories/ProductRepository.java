package com.kafkaecommerce.payment.db.respositories;

import com.kafkaecommerce.payment.db.entities.Order;
import com.kafkaecommerce.payment.db.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findAllByOrder(Order order);
}
