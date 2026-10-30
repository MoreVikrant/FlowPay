package com.FlowPay.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(name = "app.kafka-enabled",havingValue = "true")
public class OutboxPublisher { //producer that  Publishes an event to Kafka

    private final EventRepository repo;
    private final KafkaTemplate<String,String> kafka;
                      // message key , message Value
    // for communication between the topics in the kafka we need the kafkaTemplate
    // KafkaTemplate is Spring Kafka's helper for sending messages to Kafka.

    public OutboxPublisher(EventRepository repo, KafkaTemplate<String, String> kafka) {
        this.repo = repo;
        this.kafka = kafka;
    }

    @Transactional    ///actually sending the event to Kafka
    public void publish(String id) throws Exception{
         Event e = repo.lockById(id).orElseThrow();
         if(e.publishedAt != null) return;
         // send msg to kafka
         kafka.send(KafkaConfig.Topic,e.merchantId,e.id).get(12, TimeUnit.SECONDS);  // wait for result for 12 seconds before continue
         e.publishedAt= Instant.now();        // recording the publication time here
    }

 // merchant.id = Message Key
    // Message value = event.id
}
