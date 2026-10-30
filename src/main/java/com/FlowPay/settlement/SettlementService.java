package com.FlowPay.settlement;

import com.FlowPay.common.ApiException;
import com.FlowPay.common.Idempotency;
import com.FlowPay.common.Ids;
import com.FlowPay.merchant.Merchant;
import com.FlowPay.merchant.MerchantService;
import com.FlowPay.transaction.LedgerService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SettlementService {

    // Settlement means getting the money from Flowpay wallet to merchant
    private final SettlementRepository repo;
    private final MerchantService merchants;
    private final LedgerService ledger;

    public SettlementService(SettlementRepository repo, MerchantService merchants, LedgerService ledger) {
        this.repo = repo;
        this.merchants = merchants;
        this.ledger = ledger;
    }

    @Transactional
    public Settlement create(String m, String key){
        Idempotency.validate(key);
        Merchant merchant = merchants.lock(m);
        Settlement old = repo.findByMerchantIdAndIdempotencyKey(m,key).orElse(null);
        if(old != null) return old;
        if(merchant.balance <=0){
            throw ApiException.conflict("No Available balance to Settle");
        }

        Settlement s = new Settlement();
        s.id = Ids.next("Settle");
        s.merchantId=m;
        s.idempotencyKey = key;
        s.amount = merchant.balance;        // Payout everything
        repo.save(s);
        ledger.record(merchant,s.id,"SETTLEMENT",-s.amount);       // debit the entry in the ledger
        return s;
    }

    public List<Settlement> list(String s){
        return repo.findByMerchantIdOrderByCreatedAtDesc(s);
    }
}
