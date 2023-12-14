package com.kafkaecommerce.payment.records;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProcessedOrder(int userId, UUID orderId, BigDecimal totalPrice, List<OrderedProduct> products) {
}
