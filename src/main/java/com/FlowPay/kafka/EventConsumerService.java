package com.FlowPay.kafka;

import com.FlowPay.common.Ids;
import com.FlowPay.merchant.Merchant;
import com.FlowPay.merchant.MerchantRepository;
import com.FlowPay.webhook.Webhook;
import com.FlowPay.webhook.WebhookRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class EventConsumerService {  // Processes an event and creates a webhook record

    private final EventRepository events;
    private final MerchantRepository merchants;
    private final WebhookRepository webHooks; // // developing the package not done yet just repo and entity done

    public EventConsumerService(EventRepository events, MerchantRepository merchants, WebhookRepository webHooks) {
        this.events = events ;
        this.merchants = merchants;
        this.webHooks = webHooks;
    }

    @Transactional
    public void consume(String id){

        Event e =events.lockById(id).orElseThrow();
        if(e.consumedAt != null) return;   //if already consumed then return
        Merchant m = merchants.findById(e.merchantId).orElseThrow();  // retrieve the merchant here

        Webhook w = new Webhook();        // create the webHook record
        w.id = Ids.next("wh");
        w.eventId=e.id;
        w.merchantId=m.id;
        w.url=m.webhookUrl;
        if(w.url == null || w.url.isBlank()){
            w.status = "SKIPPED";
            w.lastError="No WebHook Url Configured";
        }
       webHooks.save(w);
        e.consumedAt= Instant.now();
    }
}
