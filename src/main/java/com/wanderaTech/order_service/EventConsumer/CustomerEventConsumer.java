package com.wanderaTech.order_service.EventConsumer;

import com.wanderaTech.common_events.UsersEvent.UserCreatedEvent;
import com.wanderaTech.order_service.Repository.UserSnapShotRepository;
import com.wanderaTech.order_service.UsersReplicaModel.UsersSnapShot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerEventConsumer {
    private final UserSnapShotRepository userSnapShotRepository;

    //this listen to the customer-topic from customer service to store customer info in order service
    @KafkaListener(topics = "customer-topic", groupId = "order-group")
    public void consume(UserCreatedEvent event) {

        UsersSnapShot snapShot=new UsersSnapShot();
        snapShot.setUserId(event.getUserId());
        snapShot.setFirstName(event.getFirstName());
        snapShot.setLastName(event.getLastName());
        snapShot.setEmail(event.getEmail());
        snapShot.setPhoneNumber(event.getPhoneNumber());

        userSnapShotRepository.save(snapShot);

        log.info("Customer snapshot saved  successfully of customerId {}", event.getUserId());
    }
}
