package com.kk.cibaria.exception;

public class PageDoesNotExistException extends RuntimeException{
    public PageDoesNotExistException(String message)
    {
        super(message);
    }
}
