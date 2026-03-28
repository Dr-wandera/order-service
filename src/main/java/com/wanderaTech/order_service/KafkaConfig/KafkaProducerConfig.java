package com.wanderaTech.order_service.KafkaConfig;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaProducerConfig {

    //create kafka topic
    @Bean
    public NewTopic reduceStockTopic(){
        return TopicBuilder
                .name("reduceStock-topic")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic customerNotificationTopic(){
        return TopicBuilder
                .name("order-topic")
                .partitions(1)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic sellerNotificationTopic(){
        return TopicBuilder
                .name("sellerNotification-topic")
                .partitions(1)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic paymentRequestTopic(){
        return TopicBuilder
                .name("paymentRequest-topic")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
