package com.remindly.user;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {
        super("Authenticated user no longer exists");
    }
}

