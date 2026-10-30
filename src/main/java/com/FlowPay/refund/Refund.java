package com.FlowPay.refund;

import com.FlowPay.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name="refunds",
        uniqueConstraints = @UniqueConstraint(columnNames = {"idempotencyKey","merchantId"}))
public class Refund  extends BaseEntity {

    @Column(nullable = false,length=48)
    public String merchantId;

    @Column(nullable = false,length=48)
    public String paymentId;

    @Column(nullable = false,length=100)
    public String idempotencyKey;

    @Column(nullable = false)
    public long amount;

    @Column(nullable = false,length=20)
    public String status = "SUCCESS";

}
