package com.FlowPay.transaction;

import com.FlowPay.common.Ids;
import com.FlowPay.merchant.Merchant;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class LedgerService {  // Changes a balance and records a financial movement

    private final LedgerEntryRepository repo;

    public LedgerService(LedgerEntryRepository repo) {
        this.repo = repo;
    }

   @Transactional                  //merchant , pay-101 , "CREDIT,10000000         // change to the balance
    public void record(Merchant m, String reference, String type, long delta ){
        m.balance = Math.addExact(m.balance,delta);
        LedgerEntry t = new LedgerEntry();
       t.id = Ids.next("txn");
       t.merchantId=m.id;   // associating the entry with the merchant
       t.referenceId=reference;
       t.type =type; // record the type
        t.amount=delta;
        t.balanceAfter=m.balance;  // Records the balance after applying the movement.
      repo.save(t);
    }
}
