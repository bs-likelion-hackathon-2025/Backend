package com.example.Cheonan.Exception;

public class TooManyRequestsException extends RuntimeException{
    public TooManyRequestsException(String message) {
        super(message);
    }
}
