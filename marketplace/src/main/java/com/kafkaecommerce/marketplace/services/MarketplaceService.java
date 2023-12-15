package com.kafkaecommerce.marketplace.services;

import com.kafkaecommerce.marketplace.db.entities.Cart;
import com.kafkaecommerce.marketplace.db.repositories.CartRepository;
import com.kafkaecommerce.marketplace.db.repositories.ProductRepository;
import com.kafkaecommerce.marketplace.exceptions.ProductNotFoundException;
import com.kafkaecommerce.marketplace.objectmappers.ProductObjectMapper;
import com.kafkaecommerce.marketplace.records.CartProduct;
import com.kafkaecommerce.marketplace.records.Order;
import com.kafkaecommerce.marketplace.records.OrderedProduct;
import com.kafkaecommerce.marketplace.records.Product;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

@Service
public class MarketplaceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarketplaceService.class);
    private final CartRepository cartRepository;
    private final Environment environment;
    private final ProductObjectMapper productObjectMapper;
    private final ProductRepository productRepository;

    public MarketplaceService(
            CartRepository cartRepository,
            Environment environment,
            ProductObjectMapper productObjectMapper,
            ProductRepository productRepository
    ) {
        this.cartRepository = cartRepository;
        this.environment = environment;
        this.productObjectMapper = productObjectMapper;
        this.productRepository = productRepository;
    }

    public List<Product> listProducts() {
        return this.productRepository.findAll().stream()
                .map(this.productObjectMapper)
                .toList();
    }

    public CartProduct addToCart(CartProduct cartProduct) {
        Optional<com.kafkaecommerce.marketplace.db.entities.Product> product = this.productRepository
                .findById(cartProduct.productId());
        if (product.isEmpty()) {
            throw new ProductNotFoundException("Product with ID " + cartProduct.productId() + " does not exist");
        }

        Cart cart = new Cart(cartProduct.userId(), cartProduct.productId(), cartProduct.quantity());
        this.cartRepository.save(cart);
        return cartProduct;
    }

    public void placeOrder(int userId) {
        List<Cart> carts = this.cartRepository.findByUserId(userId);
        List<OrderedProduct> orderedProducts = carts.stream()
                .map(cart -> {
                    Optional<com.kafkaecommerce.marketplace.db.entities.Product> product = this.productRepository
                            .findById(cart.getProductId());
                    if (product.isEmpty()) {
                        LOGGER.error("Product with ID: " + cart.getProductId() + " not found");
                        return null;
                    }

                    return new OrderedProduct(
                            product.get().getId(),
                            product.get().getTitle(),
                            product.get().getPrice(),
                            product.get().getDescription(),
                            product.get().getCategory(),
                            product.get().getImage(),
                            cart.getQuantity()
                    );
                })
                .toList();
        this.cartRepository.deleteAll(carts);

        Order order = new Order(userId, UUID.randomUUID(), orderedProducts);
        sendEvent(order);
    }

    private void sendEvent(Order order) {
        try {
            Properties properties = new Properties();
            properties.put("bootstrap.servers", this.environment.getProperty("kafka.bootstrap-servers"));
            properties.put("client.id", InetAddress.getLocalHost().getHostName());
            properties.put("linger.ms", 1);
            properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            properties.put("value.serializer", "com.kafkaecommerce.marketplace.serializers.OrderRecordSerializer");

            try (Producer<String, Order> producer = new KafkaProducer<>(properties)) {
                final String KAFKA_TOPIC = "KAFKA_ECOMMERCE_MARKETPLACE";
                producer.send(new ProducerRecord<>(KAFKA_TOPIC, order.orderId().toString(), order));
            }
        } catch (UnknownHostException exception) {
            LOGGER.error(exception.getMessage());
        }
    }
}
