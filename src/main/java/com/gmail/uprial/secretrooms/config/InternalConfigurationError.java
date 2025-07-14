package com.gmail.uprial.secretrooms.config;

@SuppressWarnings("ExceptionClassNameDoesntEndWithException")
public class InternalConfigurationError extends RuntimeException {
    public InternalConfigurationError(String message) {
        super(message);
    }
}
