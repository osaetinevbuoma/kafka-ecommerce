package com.kafkaecommerce.payment.records;

import java.util.List;
import java.util.UUID;

public record Order(int userId, UUID orderId, List<OrderedProduct> products) {
}
