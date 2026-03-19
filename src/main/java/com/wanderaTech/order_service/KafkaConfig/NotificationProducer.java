package com.wanderaTech.order_service.KafkaConfig;

import com.wanderaTech.common_events.NotificationEvent.OrderPlacedEvent;
import com.wanderaTech.common_events.NotificationEvent.SellerNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationProducer {
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    //this method publish an event to Kafka when an order is placed, so that another microservice (Notification Service) can process it and notify the customer.
    public void sendOrderPlacedNotificationToCustomer(OrderPlacedEvent orderPlacedEvent){
        log.info("Start sending order notification event to Notification Service");

        Message<OrderPlacedEvent> message= MessageBuilder
                .withPayload(orderPlacedEvent)
                .setHeader(KafkaHeaders.TOPIC,"order-topic")
                .build();
        kafkaTemplate.send(message);

        log.info("Customer Notification sent  successfully {}", orderPlacedEvent.getOrderNumber());
        kafkaTemplate.flush();   //this ensures all the event created is sent to kafka  if the service crushes

    }
    //this method publish an event to Kafka when an order is placed, so that another microservice (Notification Service) can process it and notify the customer.
    public void sendOrderPlacedNotificationToSeller(SellerNotificationEvent sellerNotificationEvent){
        log.info("Start sending order notification event to Notification Service to notify seller of his product sold ");

        Message<SellerNotificationEvent> message= MessageBuilder
                .withPayload(sellerNotificationEvent)
                .setHeader(KafkaHeaders.TOPIC,"sellerNotification-topic")
                .build();
        kafkaTemplate.send(message);

        log.info("Seller Notification sent  successfully {}", sellerNotificationEvent.getOrderNumber());
        kafkaTemplate.flush();   //this ensures all the event created is sent to kafka  if the service crushes

    }


}
