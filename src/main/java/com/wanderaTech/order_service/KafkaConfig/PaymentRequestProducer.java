package com.wanderaTech.order_service.KafkaConfig;

import com.wanderaTech.common_events.PaymentEvent.OrderPaymentEvent;
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
public class PaymentRequestProducer {
    private final KafkaTemplate<String, OrderPaymentEvent> kafkaTemplate;

    //sends event to payment service for payment of the order placed
    public void sendOrderPlacementEvent(OrderPaymentEvent orderPaymentEvent){
        log.info("Start sending payment   event to payment service");

        Message<OrderPaymentEvent> message= MessageBuilder
                .withPayload(orderPaymentEvent)
                .setHeader(KafkaHeaders.TOPIC,"paymentRequest-topic")
                .build();
        kafkaTemplate.send(message);

        log.info("Payment event  sent  successfully of order number {}", orderPaymentEvent.getOrderNumber());
        kafkaTemplate.flush();   //this ensures all the event created is sent to kafka  if the service crushes

    }
}
