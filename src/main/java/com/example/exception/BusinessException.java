package com.example.exception;

import com.example.model.Transaction;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

public class BusinessException extends RuntimeException implements Publisher<Transaction> {
    public BusinessException(String message) {
        super(message);
    }

    @Override
    public void subscribe(Subscriber<? super Transaction> s) {

    }
}
