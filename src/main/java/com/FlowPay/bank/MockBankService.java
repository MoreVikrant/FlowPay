package com.FlowPay.bank;

import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class MockBankService {

    // we are simulating the bank
    public record Result(String status, Instant resolvedAt)
    {

    }

    public Result initiate(String scenario){

        return switch(scenario)
        { // we will give the button on the UI
            case "SUCCESS" -> new Result("SUCCESS",null); // Immediate success
            case "FAILURE" -> new Result("FAILED",null); // Immediate failure
            case "DELAYED" -> new Result("PENDING",Instant.now().plusSeconds(8));
            case "TIMEOUT" -> new Result("PENDING",Instant.now().plusSeconds(20));
            default -> throw new IllegalArgumentException("Unknown Simulation Scenario");
        };
    }

    public String check(Instant resolvedAt){
        return resolvedAt != null &&  !Instant.now().isBefore(resolvedAt) ? "SUCCESS" : "PENDING";
    }
    // If the current time is after the resolved timing then gets the answer success
    // only have pending and success therefore Delayed and Timeout are not permanent after the time it will lead to SUCCESS
}
