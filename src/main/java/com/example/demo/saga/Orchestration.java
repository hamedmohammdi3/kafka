package com.example.demo.saga;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.Mono;

public interface Orchestration {

    Mono<Void> sendMessage(String key, String message);

    @KafkaListener(topics = "your_topic", groupId = "my-group")
    Mono<Void> consume(String message);

    @Scheduled(fixedRate = 0)
    void checkExpiredMessages();

    void rollBackExpiredMessages();

    void nextState();
}
