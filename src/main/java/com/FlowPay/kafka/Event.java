package com.FlowPay.kafka;

import com.FlowPay.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name="events")
public class Event extends BaseEntity {
    // this only application will consume it and will send the event to kafka
    // this is not the good design because why are we not sending the event on kafka directly
    // if i removed this from the picture that this application will not consume the event
    // then the concept of webhook will be removed so we are doing to learn
    // later on will remove this part and will create a separate microservice for this

    @Column(nullable = false,length = 48)
    public String merchantId;   // we need to know who is sending the events
    // publisher also uses it as the Kafka message key

    @Column(nullable = false,length = 60)
    public String type;

    @Column(nullable = false,columnDefinition = "TEXT")
    public String payLoad;              // Stores the event's data as a JSON string

    public Instant publishedAt;

    public Instant consumedAt;

}
