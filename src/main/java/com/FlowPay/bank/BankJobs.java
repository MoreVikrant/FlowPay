package com.FlowPay.bank;

import com.FlowPay.payment.PaymentRepository;
import com.FlowPay.payment.PaymentService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@ConditionalOnProperty(name="app.jobs-enabled",havingValue = "true") // Allows the job to be enabled or disabled through configuration.
@EnableScheduling
public class BankJobs {

    private final PaymentRepository repo;
    private final PaymentService service;

    public BankJobs(PaymentRepository repo, PaymentService service) {
        this.repo = repo;
        this.service = service;
    }


    @Scheduled(fixedDelay =2000 )
    public void completeDelayed(){

    }
}
