package com.example.Cheonan.Exception;

public class UpstreamTimeoutException extends RuntimeException {
    public UpstreamTimeoutException(String message) {
        super(message);
    }
}
