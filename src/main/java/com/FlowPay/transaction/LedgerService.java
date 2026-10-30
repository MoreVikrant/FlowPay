package com.FlowPay.transaction;

import com.FlowPay.common.Ids;
import com.FlowPay.merchant.Merchant;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class LedgerService {

    private final LedgerEntryRepository repo;

    public LedgerService(LedgerEntryRepository repo) {
        this.repo = repo;
    }

   @Transactional                         // change to the balance
    public void record(Merchant m, String reference, String type, long delta ){
        m.balance = Math.addExact(m.balance,delta);
        LedgerEntry t = new LedgerEntry();
       t.id = Ids.next("txn");
       t.merchantId=m.id;
       t.referenceId=reference;
       t.type =type;
        t.amount=delta;
        t.balanceAfter=m.balance;
      repo.save(t);
    }
}
