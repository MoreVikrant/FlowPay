package com.FlowPay.merchant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MerchantDtos {

    public record Register( // data expected when registering a merchant.
            @NotBlank @Size(max=100) String name, @NotBlank @Email @Size(max=100) String email){}

    public record Update(   // which details a merchant can update
            @NotBlank @Size(max=100) String name,
            @NotBlank @Size(max=100)String email,
           @Size(max=500)String webhookUrl
    ){}

    public record Registered( // response your service returns after registration.
            Merchant merchant,String apiKey, String webhookSecret
                                                  // used for signing the webhooks to make them secure
    ){}
}
