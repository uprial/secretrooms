package com.gmail.uprial.secretrooms.config;

@SuppressWarnings("ExceptionClassNameDoesntEndWithException")
class InternalConfigurationError extends RuntimeException {
    InternalConfigurationError(String message) {
        super(message);
    }
}
