package com.FlowPay.payment;

import com.FlowPay.bank.MockBankService;
import com.FlowPay.common.ApiException;
import com.FlowPay.common.Idempotency;
import com.FlowPay.common.Ids;
import com.FlowPay.kafka.EventService;
import com.FlowPay.merchant.MerchantService;
import com.FlowPay.merchant.Merchant;
import com.FlowPay.order.OrderService;
import com.FlowPay.order.PaymentOrder;
import com.FlowPay.transaction.LedgerService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository repo;
    private final OrderService orders;
    private final MerchantService merchants;
    private final MockBankService bank;
    private final EventService event ;  //  just created this classes in the packages we will be adding code later
    private final LedgerService ledger;  //


    public PaymentService(PaymentRepository repo, OrderService orders, MerchantService merchants, MockBankService bank, EventService event, LedgerService ledger) {
        this.repo = repo;
        this.orders = orders;
        this.merchants = merchants;
        this.bank = bank;
        this.event = event;
        this.ledger = ledger;
    }

    public static long fee(long amount){
        return (Math.multiplyExact(amount , 2)+99)/100;
    }

    @Transactional
    public Payment create(String m,String key,PaymentDtos.Create r ){
/* example, a merchant sends a request, but the network times out before the response reaches the client.
 The client retries with the same key.
You don't want the retry to create another payment.     */
        Idempotency.validate(key);
        merchants.lock(m); // so that one merchant requests run at a time

        Payment existing = repo.findByMerchantIdAndIdempotencyKey(m,key).orElse(null);
        if(existing !=null){

            if(!existing.orderId.equals(r.orderId())
             || !existing.method.equals(r.method().name())
            ||!existing.scenario.equals(r.scenario().name()))
            {
              throw ApiException.bad("Idempotency key is reused with different request");
            }
            return existing;
        }

        PaymentOrder o = orders.get(m,r.orderId());  //order amount should come from existing order
         if(o.status.equals("PAID")
          || repo.existsByOrderIdAndStatusIn(o.id, List.of("SUCCESS","PENDING"))) {
             throw ApiException.bad("Order already paid or has an Active Payment");

         }

        Payment p = new Payment();
        p.id = Ids.next("pay");
        p.merchantId = m;
        p.orderId=o.id;
        p.amount=o.amount;
        p.idempotencyKey=key;
        p.scenario=r.scenario().name();     // .name() converts the enum into the String
        p.method = r.method().name();
        return repo.save(p);
    }

    public Payment get(String m, String id){
      Payment p = repo.findById(id).orElseThrow(ApiException::missing);
      if(!p.merchantId.equals(m)) throw ApiException.missing();
      return p;
    }

    @Transactional
    public Payment process(String m, String id){

        Merchant merchant = merchants.lock(m);
        Payment p = get(m,id);
        if(!p.status.equals("CREATED")) return p;
        MockBankService.Result result = bank.initiate(p.scenario);  // asking the mock bank to initiate the payment
        p.resolvedAt=result.resolvedAt();
        finish(merchant,p,result.status());
        return p;
    }

    private void finish(Merchant m,Payment p,String status){
        p.status=status;
        if(status.equals("SUCCESS")){
            p.fee=fee(p.amount);
            orders.get(m.id,p.orderId).status="PAID";
            // Now here we want ledger to check the records that is not made yet
            ledger.record(m,p.id,"PAYMENT",p.amount-p.fee);

        }else if(status.equals("FAILURE")){
            event.create(m.id, "payment.failed", p.id, p.amount);
        }
    }

    @Transactional    // check a pending payment again
    public Payment reconcile(String m,String id){
        Merchant merchant = merchants.lock(m);
        Payment p= get(m,id);
        if(p.status.equals("PENDING")){
            finish(merchant,p, bank.check(p.resolvedAt));
        }
        return p;
    }

    public List<Payment> list(String m){
        return repo.findByMerchantIdOrderByCreatedAtDesc(m);
    }


}
