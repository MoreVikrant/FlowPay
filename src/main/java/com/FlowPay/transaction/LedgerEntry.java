package com.FlowPay.transaction;

import com.FlowPay.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;


@Entity
@Table(name = "transactions")
public class LedgerEntry  extends BaseEntity {

    @Column(nullable = false,length=48)
    public String merchantId;

    @Column(nullable = false,length=48)
    public String referenceId;   /// reference to the payment such as pay_102

    @Column(nullable=false,length=20)
    public String type;  // which type

    @Column(nullable=false)
    public long amount; // change:

    @Column(nullable=false)
    public long balanceAfter; //  records the balance immediately after each entry
    // thats why its balance after




}
