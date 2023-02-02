package com.example.chat.exception;

public class ResourceAlreadyExistException extends RuntimeException{

    public ResourceAlreadyExistException(String msg) {
        super(msg);
    }

    public ResourceAlreadyExistException(String id, Class<?> type) {
        super(String.format("%s with id %s already exist", type.getSimpleName(), id));
    }

    public ResourceAlreadyExistException(String identifierKey, String identifierValue, Class<?> type) {
        super(String.format("%s with %s %s already exist", type.getSimpleName(), identifierKey, identifierValue));
    }
}
