package com.wanderaTech.order_service.EventConsumer;

import com.wanderaTech.common_events.CustomerEvent.CustomerCreatedEvent;
import com.wanderaTech.order_service.Repository.CustomerSnapshotRepository;
import com.wanderaTech.order_service.UsersReplicaModel.CustomerSnapSot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerEventConsumer {
    private final CustomerSnapshotRepository repository;

    //this listen to the customer-topic from customer service to store customer info in order service
    @KafkaListener(topics = "customer-topic", groupId = "order-group")
    public void consume(CustomerCreatedEvent event) {

        CustomerSnapSot snapshot = CustomerSnapSot.builder()
                .customerId(event.getCustomerId())
                .email(event.getEmail())
                .phoneNumber(event.getPhoneNumber())
                .firstName(event.getFirstName())
                .build();

        repository.save(snapshot);

        log.info("Customer snapshot saved  successfully of customerId {}", event.getCustomerId());
    }
}
