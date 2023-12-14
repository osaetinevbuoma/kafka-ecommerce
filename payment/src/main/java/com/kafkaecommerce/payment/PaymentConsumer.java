package com.kafkaecommerce.payment;

import com.kafkaecommerce.payment.records.Order;
import com.kafkaecommerce.payment.services.PaymentService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Component
public class PaymentConsumer implements ApplicationListener<ApplicationContextEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentConsumer.class);
    private final Environment environment;
    private final PaymentService paymentService;

    public PaymentConsumer(Environment environment, PaymentService paymentService) {
        this.environment = environment;
        this.paymentService = paymentService;
    }

    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", this.environment.getProperty("kafka.bootstrap-servers"));
        properties.setProperty("group.id", "KAFKA_ECOMMERCE");
        properties.setProperty("enable.auto.commit", "true");
        properties.setProperty("auto.commit.interval.ms", "1000");
        properties.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.setProperty("value.deserializer", "com.kafkaecommerce.payment.serializers.OrderRecordDeserializer");

        try (KafkaConsumer<String, Order> consumer = new KafkaConsumer<>(properties)) {
            final String KAFKA_TOPIC = "KAFKA_ECOMMERCE_MARKETPLACE";
            consumer.subscribe(List.of(KAFKA_TOPIC));
            while (true) {
                ConsumerRecords<String, Order> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, Order> consumedRecord : records) {
                    LOGGER.info("Consuming record => " + consumedRecord);
                    this.paymentService.saveOrder(consumedRecord.value());
                }
            }
        }
    }
}
