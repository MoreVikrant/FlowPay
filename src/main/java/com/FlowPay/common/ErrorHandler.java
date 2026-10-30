package com.FlowPay.common;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

@RestControllerAdvice
// tells the spring if any controller or service throws an Exception this class gets a chance to handle it
public class ErrorHandler {
                     // If someone throws the ApiException.class then this method will run
    @ExceptionHandler(ApiException.class)
    ResponseEntity<?> business(ApiException e){
        return ResponseEntity.status(e.status).body(Map.of("message" ,e.getMessage()));
    }

    // if request fail anywhere in between
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
             MethodArgumentTypeMismatchException.class,
              HttpMessageNotReadableException.class,
    MissingRequestHeaderException.class})
    ResponseEntity<?> validation(Exception e){

        String message = e instanceof MethodArgumentNotValidException v
                ? v.getBindingResult().getFieldErrors().stream()
                .map(f ->f.getField()+": "+f.getDefaultMessage())
                .findFirst()
                .orElse("Invalid Request")
                :"Invalid Request,missing Header,unknown field";
        return ResponseEntity.badRequest().body(Map.of("message",message));
    }

    // What if someone did duplicate transaction
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<?> duplicate(DataIntegrityViolationException e){
        return ResponseEntity.status(409).body(Map.of("message" ,"Duplicate or conflict record"));
    }
}
