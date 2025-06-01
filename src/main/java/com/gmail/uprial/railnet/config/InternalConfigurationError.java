package com.gmail.uprial.railnet.config;

@SuppressWarnings("ExceptionClassNameDoesntEndWithException")
class InternalConfigurationError extends RuntimeException {
    InternalConfigurationError(String message) {
        super(message);
    }
}
