package com.FlowPay.transaction;

import com.FlowPay.merchant.MerchantService;
import com.FlowPay.payment.Payment;
import com.FlowPay.payment.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController { // primarily exposes the transaction history

    private final TransactionService service;
    private final MerchantService merchants;
    private final PaymentService payments;

    public TransactionController(TransactionService service, MerchantService merchants, PaymentService payments) {
        this.service = service;
        this.merchants = merchants;
        this.payments = payments;
    }

    @GetMapping("/list")
    public List<Payment> list(@RequestHeader("X-Api-Key") String key,
                              @RequestParam(required = false) String status,
                              @RequestParam(required = false) LocalDate from,
                              @RequestParam(required = false) LocalDate to){
    return service.list(merchants.authenticate(key).id,status,from,to);
    }

    @GetMapping("/ledger")
    public List<LedgerEntry> ledger(@RequestHeader("X-Api-Key") String key){
        return service.ledger(merchants.authenticate(key).id);
    }


@GetMapping("/{id}")
    public Payment details(@RequestHeader("X-Api-Key") String key,@PathVariable String id){
       return payments.get(merchants.authenticate(key).id,id);
    }
}
