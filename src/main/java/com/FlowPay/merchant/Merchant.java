package com.FlowPay.merchant;

import com.FlowPay.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name ="merchants")
public class Merchant extends BaseEntity {

    @Column(nullable = false,length=100)
     public String name;

    @Column(nullable = false,length=160)
    public String email;

    @JsonIgnore
    @Column(nullable = false,length=64)
    public String apiKeyHash;

    @Column(nullable = false)
    public long balance;

    @JsonIgnore
    @Column(nullable = false,length=100)
    public String webhookSecret;

    @Column(length=500)
    public String webhookUrl;
}


