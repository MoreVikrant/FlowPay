package com.FlowPay.refund;

import com.FlowPay.merchant.MerchantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/refunds")
public class RefundController {

    private final MerchantService merchants;
    private final RefundService service;

    public RefundController(MerchantService merchants, RefundService service) {
        this.merchants = merchants;
        this.service = service;
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Refund create(
            @RequestHeader("X-Api-Key") String key,
            @RequestHeader("Idempotency-Key") String ik,        // Identifies the logically repeated request
             @Valid @RequestBody RefundDtos.Create r
    ){
        return service.create(merchants.authenticate(key).id,ik,r);
    }

    @GetMapping
    public List<Refund> list(@RequestHeader("X-Api-Key") String key){
        return service.list(merchants.authenticate(key).id);
    }

}
