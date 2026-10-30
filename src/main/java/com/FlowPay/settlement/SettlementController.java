package com.FlowPay.settlement;

import com.FlowPay.merchant.MerchantService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settlements")
public class SettlementController {

    private final SettlementService service;
    private final MerchantService merchants;

    public SettlementController(SettlementService service, MerchantService merchants) {
        this.service = service;
        this.merchants = merchants;
    }

    @PostMapping("/create")
    public Settlement create(
            @RequestHeader("X-Api-Key") String key,
            @RequestHeader("Idempotency-Key") String ik     // Identifies the logically repeated request
            ){ // in this case we don't need the dto here because we will directly give the balance to the merchant
        return service.create(merchants.authenticate(key).id,ik);
    }

    @GetMapping
    public List<Settlement> list(@RequestHeader("X-Api-Key") String key){
        return service.list(merchants.authenticate(key).id);
    }

}
