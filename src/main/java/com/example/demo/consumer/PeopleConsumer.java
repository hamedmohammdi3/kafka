package com.example.demo.consumer;

import com.example.demo.entities.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PeopleConsumer {
    static final Logger LOGGER = LoggerFactory.getLogger(PeopleConsumer.class);

    @KafkaListener(topics = "people.basic.java", containerFactory = "personListenerFactory")
    public void listen(Person person) {
        LOGGER.info("Received record: {}", person);
        System.out.println(person.toString());
    }
}
