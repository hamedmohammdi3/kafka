package com.example.demo.saga;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class PaymentService {


    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final String topic = "your_topic";
    private final int time = 6000;

    @Autowired
    public PaymentService(KafkaTemplate<String, String> kafkaTemplate, ReactiveRedisTemplate<String, String> reactiveRedisTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    public Mono<Void> sendMessage(String key, String message) {
        return Mono.fromRunnable(() -> {
            kafkaTemplate.send(topic, message);
            System.out.println("Message sent: " + message);
        }).then(reactiveRedisTemplate.opsForValue().set(key, message)
                .delayElement(Duration.ofMinutes(5))
                .doOnSuccess(aVoid -> System.out.println("Key stored in Redis: " + key))
                .then());
    }

    @KafkaListener(topics = "your_topic", groupId = "my-group")
    public Mono<Void> consume(String message) {
        System.out.println("Message consumed: " + message);

        return reactiveRedisTemplate.delete("messageKey").then();
    }

    @Scheduled(fixedRate = time)
    public void checkExpiredMessages() {
        Flux<String> keys = reactiveRedisTemplate.keys("*").flatMap(key ->
                reactiveRedisTemplate.getExpire(key)
                        .filter(expire -> expire.toSeconds() > 300)
                        .doOnNext(expire -> {
                            System.out.println("Message with key " + key + " has expired after 5 minutes.");
                            // import action
                        })
                        .then(Mono.just(key))
        );

        keys.flatMap(reactiveRedisTemplate::delete)
                .subscribe(result -> {
                    System.out.println("Deleted key: " + result);
                });

    }

}