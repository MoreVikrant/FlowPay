package com.FlowPay.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@ConditionalOnProperty(name ="app.kafka-enabled",havingValue = "true")
public class KafkaConfig {  //Configuring the Kafka topic and error handler here

    // we cant add the event directly in the kafka we first have to  do the serialization
    public static final String Topic="FlowPay.events";

    @Bean
    NewTopic paymentEvents(){
        return TopicBuilder.name(Topic).partitions(3).replicas(1).build();
    }

    // Kafka has its on Error Handler
    @Bean
    DefaultErrorHandler kafkaErrorHandler(){
        return new DefaultErrorHandler(new FixedBackOff(2000L,FixedBackOff.UNLIMITED_ATTEMPTS));
        //Unlimited retries also need care: a permanently invalid message can repeatedly fail and block progress
    }                                                                // Production systems often use bounded retries
}

//This will make topic for us
