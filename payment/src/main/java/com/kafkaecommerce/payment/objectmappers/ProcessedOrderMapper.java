package com.kafkaecommerce.payment.objectmappers;

import com.kafkaecommerce.payment.db.entities.Order;
import com.kafkaecommerce.payment.db.entities.Product;
import com.kafkaecommerce.payment.db.respositories.ProductRepository;
import com.kafkaecommerce.payment.records.OrderedProduct;
import com.kafkaecommerce.payment.records.ProcessedOrder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class ProcessedOrderMapper implements Function<Order, ProcessedOrder> {
    private final OrderedProductMapper orderedProductMapper;
    private final ProductRepository productRepository;

    public ProcessedOrderMapper(OrderedProductMapper orderedProductMapper, ProductRepository productRepository) {
        this.orderedProductMapper = orderedProductMapper;
        this.productRepository = productRepository;
    }

    @Override
    public ProcessedOrder apply(Order order) {
        List<Product> products = this.productRepository.findAllByOrder(order);
        List<OrderedProduct> orderedProducts = products.stream()
                .map(this.orderedProductMapper)
                .toList();

        return new ProcessedOrder(
                order.getUserId(),
                order.getOrderId(),
                order.getTotalPrice(),
                orderedProducts
        );
    }
}
