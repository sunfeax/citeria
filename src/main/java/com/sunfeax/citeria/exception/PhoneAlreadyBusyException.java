package com.sunfeax.citeria.exception;

public class PhoneAlreadyBusyException extends RuntimeException {
    public PhoneAlreadyBusyException(String message) {
        super(message);
    }
}