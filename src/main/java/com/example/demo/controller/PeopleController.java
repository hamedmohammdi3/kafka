package com.example.demo.controller;

import com.example.demo.commands.CreatePeopleCommand;
import com.example.demo.entities.Person;
import jakarta.websocket.SendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class PeopleController {
    static final Logger logger = LoggerFactory.getLogger(PeopleController.class);
    @Value("${topics.people-basic.name}")
    String  peopleTopic;

    private KafkaTemplate<String , Person> kafkaTemplate;

    public PeopleController(KafkaTemplate<String, Person> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    @PostMapping("/people")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Person> people(@RequestBody CreatePeopleCommand cmd) {
        logger.info("Creating people"+cmd);
        List<Person> people = new ArrayList<>();
        for (var i= 0;i< cmd.getCount(); i++) {
            var person = new Person();
            person.setId(UUID.randomUUID().toString());
            person.setName("person" + i);
            person.setTitle("job" + i);
            people.add(person);
               kafkaTemplate.send(peopleTopic, person.getTitle(),person);
            }
        return people;
    }

}
