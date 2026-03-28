package com.wanderaTech.order_service.KafkaConfig;

import com.wanderaTech.common_events.productEvent.StockReduceEvent;
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
public class ReduceStockProducer {
    private final KafkaTemplate<String, StockReduceEvent> kafkaTemplate;

    //publish reduce stock event to inventory
    public void sendReduceStockAfterProductPurchase(StockReduceEvent stockReduceEvent){
        log.info("Start sending reduce stock event to  inventory");

        Message<StockReduceEvent> message= MessageBuilder
                .withPayload(stockReduceEvent)
                .setHeader(KafkaHeaders.TOPIC,"reduceStock-topic")
                .build();
        kafkaTemplate.send(message);

        log.info("sent reduce stock event to  inventory of product Id {}", stockReduceEvent.getProductId());
        kafkaTemplate.flush();   //this ensures all the event created is sent to kafka  if the service crushes

    }
}
