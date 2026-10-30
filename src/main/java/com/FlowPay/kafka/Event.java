package com.FlowPay.kafka;

import com.FlowPay.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name="events")
public class Event extends BaseEntity {

    @Column(nullable = false,length = 48)
    public String merchantId;

    @Column(nullable = false,length = 60)
    public String type;

    @Column(nullable = false,columnDefinition = "TEXT")
    public String payLoad;

    public Instant publishedAt;

    public Instant consumedAt;

}
