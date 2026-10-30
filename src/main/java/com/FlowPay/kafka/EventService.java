package com.FlowPay.kafka;

import com.FlowPay.common.Ids;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;


import java.util.Map;

@Service
public class EventService { /// Here we are constructing the event before publishing

  private final EventRepository repo;

  private final ObjectMapper json;


    public EventService(EventRepository repo, ObjectMapper json) {
        this.repo = repo;
        this.json = json;
    }

    @Transactional   // using the transactional because we need it to either complete or to fail not to get the middle like stopping in the mid of execution
    public void create(String merchantId,String type,String referenceId,long amount){

        Event e = new Event();                  // we have used the transactional outbox pattern here in this package
        e.id = Ids.next("evt");           //our code first saves an event in the database:
        e.merchantId=merchantId;
        e.type = type;
        try{
            e.payLoad = json.writeValueAsString(       //Convert the payload into JSON
                    Map.of( "id",e.id,
                            "type",type,
                            "merchantId",merchantId,
                            "referenceId",referenceId,
                            "amount",amount,
                            "currency",
                            "INR",
                            "createdAt",
                            e.createdAt.toString()));
        }catch(Exception ex){  // handling the serialization error
          throw new IllegalStateException(ex);
        }
        repo.save(e);
        // then saved into not necessarily sent to kafka this separation is because of the transactional outbox pattern
    }
}
