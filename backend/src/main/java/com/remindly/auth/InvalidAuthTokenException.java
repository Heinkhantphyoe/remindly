package com.remindly.auth;
public class InvalidAuthTokenException extends RuntimeException {
    public InvalidAuthTokenException() { super("This link is invalid or has expired"); }
}
