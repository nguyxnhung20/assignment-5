package org.example.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic newProductsTopic() {
        return TopicBuilder.name("new-products")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic updatedProductsTopic() {
        return TopicBuilder.name("updated-products")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic deletedProductsTopic() {
        return TopicBuilder.name("deleted-products")
                .partitions(1)
                .replicas(1)
                .build();
    }
}