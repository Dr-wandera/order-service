package com.wanderaTech.order_service.EventConsumer;

import com.wanderaTech.common_events.SellerEvent.SellerCreatedEvent;
import com.wanderaTech.order_service.Repository.SellerSnapShotRepository;
import com.wanderaTech.order_service.UsersReplicaModel.SellerSnapShot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class SellerEventConsumer {
    private final SellerSnapShotRepository repository;

    //this listen to the seller-topic to replicate the seller info in order service
    @KafkaListener(topics = "seller-topic", groupId = "order-group")
    public void consume(SellerCreatedEvent event) {

        SellerSnapShot snapshot = SellerSnapShot.builder()
                .sellerId(event.getSellerId())
                .email(event.getEmail())
                .firstName(event.getFirstName())
                .build();

        repository.save(snapshot);

        log.info("Seller snapshot saved  successfully of customerId {}", event.getSellerId());
    }
}
