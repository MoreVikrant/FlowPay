package com.FlowPay.transaction;

import com.FlowPay.common.ApiException;
import com.FlowPay.payment.Payment;
import com.FlowPay.payment.PaymentService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class TransactionService { //Retrieves and filters payment and ledger history
    // we are not making the Controller
    // Ledger is record which run by transaction that's why we created this ;

    private final LedgerEntryRepository ledger;
    private final PaymentService payments;

    public TransactionService(LedgerEntryRepository ledger, PaymentService payments) {
        this.ledger = ledger;
        this.payments = payments;
    }

    public List<Payment> list(String merchant, String status, LocalDate from, LocalDate to){

        if(from !=null && to !=null && from.isAfter(to)){
            throw ApiException.bad("from date must not be after to date");
        }

        return payments.list(merchant).stream()
                .filter(p ->status==null || status.isBlank()|| status.equals(p.status))
                // provided status should match required status otherwise exclude it
                .filter(p->from==null || !p.createdAt.isBefore(from.atStartOfDay(ZoneOffset.UTC).toInstant()))
             //from date at the start of that day (UTC)
                .filter(p-> to==null || p.createdAt.isBefore(to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()))
                        .toList();  // by to date before the start of the next day, so to includes the whole day

    }

    public List<LedgerEntry> ledger(String m){
        return ledger.findByMerchantIdOrderByCreatedAtDesc(m);
    }
    
}
