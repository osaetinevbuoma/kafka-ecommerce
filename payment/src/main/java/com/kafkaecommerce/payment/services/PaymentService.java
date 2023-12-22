package com.kafkaecommerce.payment.services;

import com.kafkaecommerce.payment.db.entities.Product;
import com.kafkaecommerce.payment.db.respositories.OrderRepository;
import com.kafkaecommerce.payment.db.respositories.ProductRepository;
import com.kafkaecommerce.payment.objectmappers.ProcessedOrderMapper;
import com.kafkaecommerce.payment.records.Order;
import com.kafkaecommerce.payment.records.ProcessedOrder;
import com.kafkaecommerce.payment.serializers.ProcessedOrderRecordSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Service
public class PaymentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentService.class);
    private final Environment environment;
    private final OrderRepository orderRepository;
    private final ProcessedOrderMapper processedOrderMapper;
    private final ProductRepository productRepository;

    public PaymentService(
            Environment environment,
            OrderRepository orderRepository,
            ProcessedOrderMapper processedOrderMapper,
            ProductRepository productRepository
    ) {
        this.environment = environment;
        this.orderRepository = orderRepository;
        this.processedOrderMapper = processedOrderMapper;
        this.productRepository = productRepository;
    }

    public void saveOrder(Order order) {
        Optional<com.kafkaecommerce.payment.db.entities.Order> existingOrder = this.orderRepository
                .findByOrderId(order.orderId());
        if (existingOrder.isPresent()) {
            return;
        }

        com.kafkaecommerce.payment.db.entities.Order orderEntity = new com.kafkaecommerce.payment.db.entities.Order(
                order.userId(),
                order.orderId()
        );
        this.orderRepository.save(orderEntity);

        List<Product> products = order.products().stream()
                .map(
                        product -> new Product(
                                product.title(),
                                product.price(),
                                product.description(),
                                product.category(),
                                product.image(),
                                product.quantity(),
                                orderEntity
                        )
                )
                .toList();
        this.productRepository.saveAll(products);
        LOGGER.info("Saved Order from Kafka Broker");

        LOGGER.info("Processing payments");
        this.processPayment();
    }

    private void processPayment() {
        List<com.kafkaecommerce.payment.db.entities.Order> orders = this.orderRepository.findAllByIsProcessed(false);
        orders.forEach(order -> {
            List<Product> products = this.productRepository.findAllByOrder(order);
            BigDecimal totalPrice = products.stream()
                    .map(product -> product.getPrice().multiply(BigDecimal.valueOf(product.getQuantity())))
                    .reduce(BigDecimal.ZERO, (subtotal, total) -> subtotal.add(total));
            order.setTotalPrice(totalPrice);
            order.setIsProcessed(true);
            this.orderRepository.save(order);
            LOGGER.info("Processed Order: %s".formatted(order.getOrderId()));
            LOGGER.info("Total price of processed order => %s".formatted(totalPrice));

            ProcessedOrder processedOrder = this.processedOrderMapper.apply(order);
            LOGGER.info("Processed Order => %s".formatted(processedOrder));
            sendEvent(processedOrder);
        });
    }

    private void sendEvent(ProcessedOrder processedOrder) {
        try {
            Properties properties = new Properties();
            properties.put("bootstrap.servers", this.environment.getProperty("kafka.bootstrap-servers"));
            properties.put("group.id", "KAFKA_ECOMMERCE");
            properties.put("client.id", InetAddress.getLocalHost().getHostName());
            properties.put("linger.ms", 1);
            properties.put("acks", "all");
            properties.put("key.serializer", StringSerializer.class.getName());
            properties.put("value.serializer", ProcessedOrderRecordSerializer.class.getName());

            try (Producer<String, ProcessedOrder> producer = new KafkaProducer<>(properties)) {
                final String KAFKA_TOPIC = "KAFKA_ECOMMERCE_PAYMENT";
                producer.send(new ProducerRecord<>(KAFKA_TOPIC, processedOrder.orderId().toString(), processedOrder),
                        (recordMetadata, exception) -> {
                            if (exception != null) {
                                LOGGER.error(exception.getMessage());
                                return;
                            }

                            LOGGER.info("Payment producer returns with the following metadata");
                            LOGGER.info("Topic: %s %nPartition: %s %nTimestamp: %s".formatted(
                                    recordMetadata.topic(),
                                    recordMetadata.partition(),
                                    recordMetadata.timestamp()
                            ));
                        });
            }
        } catch (UnknownHostException exception) {
            LOGGER.error(exception.getMessage());
        }
    }
}
