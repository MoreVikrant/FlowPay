package com.FlowPay.webhook;


import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface WebhookRepository extends JpaRepository<Webhook,String> {


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Webhook e where e.id=:id")
    Optional<Webhook> lockbyId(@Param("id") String id);


    List<Webhook> findByMerchantIdOrderByCreatedAtDesc(String m);

    List<Webhook> findTop50ByStatusAndNextAttemptAtBeforeOrderByCreatedAtDesc(String status , Instant now);


}
