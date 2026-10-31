package com.FlowPay.bank;

import com.FlowPay.payment.Payment;
import com.FlowPay.payment.PaymentRepository;
import com.FlowPay.payment.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.jobs-enabled", havingValue = "true")
@EnableScheduling
public class BankJobs {

    private static final Logger log = LoggerFactory.getLogger(BankJobs.class);

    private final PaymentRepository repo;
    private final PaymentService service;

    public BankJobs(PaymentRepository repo, PaymentService service) {
        this.repo = repo;
        this.service = service;
    }

    @Scheduled(fixedDelay = 2000)
    public void completeDelayed() {

        Instant now = Instant.now();

        completeDue("DELAYED_SUCCESS", now);
        completeDue("TIMEOUT", now);
    }

    private void completeDue(String scenario, Instant now) {

        List<Payment> payments =
                repo.findTop50ByStatusAndScenarioAndResolvedAtBeforeOrderByCreatedAtDesc(
                        "PENDING", scenario, now
                );

        for (Payment payment : payments) {
            try {
                service.reconcile(payment.merchantId, payment.id);
            } catch (Exception e) {
                log.error("Failed to complete delayed payment: {}", payment.id, e);
            }
        }
    }
}

/*
 Running every 2 seconds.
Finds pending payments whose resolution time has passed.
Processes up to 50 payments per scenario per run.
Uses your existing reconciliation and ledger logic.
Logs individual failures without stopping the remaining payments.  */
