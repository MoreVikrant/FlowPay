package com.FlowPay.order;


import com.FlowPay.common.ApiException;
import com.FlowPay.common.Ids;
import com.FlowPay.kafka.EventService;
import com.FlowPay.merchant.MerchantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {


    private final PaymentOrderRepository repo;
    private final MerchantService merchants;
    private final EventService events;



    public OrderService(PaymentOrderRepository repo, MerchantService merchants, EventService events) {
       this.repo = repo;
        this.merchants = merchants;
        this.events = events;
    }


    @Transactional
    public PaymentOrder create(String merchantId, OrderDtos.Create r){
        merchants.lock(merchantId);
        PaymentOrder p = new PaymentOrder();
        p.id = Ids.next("order");
        p.merchantId = merchantId;
        p.amount = r.amount();
        p.receipt = r.receipt();

        PaymentOrder saved = repo.save(p);

        // same transaction: the order and its event are saved together or not at all
        events.create(merchantId, "order.created", saved.id, saved.amount);

        return saved;
    }

    // now i want the details of the orders thats why we need the merchant id and the product id which  we ger from the base entity
  public PaymentOrder get(String merchantId,String id){

         PaymentOrder p = repo.findById(id).orElseThrow(ApiException::missing);
         if(!p.merchantId.equals(merchantId)) throw ApiException.missing();
         return p;
  }

  // List of all the orders from this merchantId
  public List<PaymentOrder> list(String merchantId){
        return repo.findByMerchantIdOrderByCreatedAtDesc(merchantId);
  }



}
