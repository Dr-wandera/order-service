package com.wanderaTech.order_service.KafkaConfig;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaProducerConfig {

    //this creates reduce stock topic to kafka that reduce product quantity after customer purchase
    @Bean
    public NewTopic reduceStockTopic(){
        return TopicBuilder
                .name("reduceStock-topic")
                .partitions(1)
                .replicas(1)
                .build();
    }

    //this creates notification topic
    @Bean
    public NewTopic customerNotificationTopic(){
        return TopicBuilder
                .name("order-topic")
                .partitions(1)
                .replicas(1)
                .build();
    }
    //this creates notification topi
    @Bean
    public NewTopic sellerNotificationTopic(){
        return TopicBuilder
                .name("sellerNotification-topic")
                .partitions(1)
                .replicas(1)
                .build();
    }
    //creates payment request topic to payment service
    @Bean
    public NewTopic paymentRequestTopic(){
        return TopicBuilder
                .name("paymentRequest-topic")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
