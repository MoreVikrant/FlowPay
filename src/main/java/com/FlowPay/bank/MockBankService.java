package com.FlowPay.bank;

import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class MockBankService {


    public record Result(String status, Instant resolvedAt)
    {

    }

    public Result initiate(String scenario){

        return switch(scenario)
        {
            case "SUCCESS" -> new Result("SUCCESS",null); // Immediate success
            case "FAILURE" -> new Result("FAILED",null); // Immediate failure
            case "DELAYED_SUCCESS" -> new Result("PENDING", Instant.now().plusSeconds(8));
            case "TIMEOUT" -> new Result("PENDING",Instant.now().plusSeconds(20));
            default -> throw new IllegalArgumentException("Unknown Simulation Scenario");
        };
    }

    public String check(Instant resolvedAt){
        return resolvedAt != null &&  !Instant.now().isBefore(resolvedAt) ? "SUCCESS" : "PENDING";
    }

}
