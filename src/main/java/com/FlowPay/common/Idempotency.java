package com.FlowPay.common;

public class Idempotency {

    private Idempotency(){

    } // private constructor so nobody can write the new Ids
    // I dont want anyone to make the variable of this



  //  In payments, a client may retry the same request (network timeout, double click).
  //  An idempotency key is a unique string sent with the request,
  //  so the server knows “this is the same request, don’t charge twice”.
  //  This class checks that the key is valid.

    public static void validate(String key){
        if(key == null || !key.matches("[A-Za-z0-9_-]{8,100}")){
            throw ApiException.bad("Idempotency key must contain 8-100 letters,digits,underscores or hyphen");
        }
    }
    // I am putting the validation here suppose i gave Idempotency key  to the user this will validate it
}
