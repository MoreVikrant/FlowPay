package com.FlowPay.refund;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class RefundDtos {

    // describe the data that the client is allowed to send to the API

    public record Create(@NotBlank String paymentId, @Min(1)@Max(100000000) long amount){}
}
