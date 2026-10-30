package com.FlowPay.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = {"app.jobs-enabled","app.kafka-enabled"}, havingValue = "true")
public class OutBoxJobs {


        private static final Logger log = LoggerFactory.getLogger(OutBoxJobs.class);
        private final EventRepository repo;
        private final OutboxPublisher publisher;

    public OutBoxJobs(EventRepository repo, OutboxPublisher publisher) {
        this.repo = repo;
        this.publisher = publisher;
    }

    // how we use Scheduler in java

    @Scheduled(fixedDelay = 3000)
    public void publish(){
        log.info("Outbox job tick");

        for(Event e : repo.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc()){
            try{
                publisher.publish(e.id);
            } catch (Exception ex) {
                log.info("Event remains {} pending  {}",e.id,ex.getClass().getSimpleName());
                break;
            }
        }

    }
}
