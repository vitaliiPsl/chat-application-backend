package com.example.chat.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String msg) {
        super(msg);
    }

    public ResourceNotFoundException(String identifier, Class<?> type) {
        super(String.format("Couldn't find %s with identifier %s", type.getSimpleName(), identifier));
    }
}
