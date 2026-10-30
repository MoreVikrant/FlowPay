package com.FlowPay.payment;

import com.FlowPay.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name ="payments",
        uniqueConstraints =@UniqueConstraint(columnNames = {"merchantId","idempotencyKey"}) )

public class Payment extends BaseEntity {

    @Column(nullable = false,length = 48)
    public String merchantId;

    @Column(nullable = false,length = 48)
    public String orderId;

    @Column(nullable = false,length = 100)
    public String idempotencyKey;  //Helps prevent duplicate payment creation

    @Column(nullable = false,length = 20)
    public String method;        // by upi or card

    @Column(nullable = false,length = 30)
    public String scenario;  // In real life this does not exist but we are taking this for mock
    // that failed scenario , passed scenario we will mock by UI

    @Column(nullable = false,length = 20)
    public String status ="CREATED";

    @Column(nullable = false)
    public long amount;

    @Column(nullable = false)
    public long fee;   // like the commission on the transaction

    @Column(nullable = false)
    public long refunded;

    public Instant resolvedAt;    // just for keeping the ledger

}
