package com.FlowPay.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = {"app.jobs-enabled","app.kafka-enabled"}, havingValue = "true")
public class OutBoxJobs { //Finds pending events and triggers publication

// here we will use logger as we also will be using this in the production
                            // comes from slf4j   // cannot create object of LoggerFactory
        private static final Logger log = LoggerFactory.getLogger(OutBoxJobs.class);
        private final EventRepository repo;
        private final OutboxPublisher publisher;

    public OutBoxJobs(EventRepository repo, OutboxPublisher publisher) {
        this.repo = repo;
        this.publisher = publisher;
    }

    // how we use Scheduler in java

    @Scheduled(fixedDelay = 3000)   // this method will run repeatedly after this delay
    public void publish(){
        log.info("Outbox job tick");
              // The job loads up to 50 unpublished events, oldest first, and loops over them.
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
