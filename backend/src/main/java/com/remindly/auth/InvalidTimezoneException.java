package com.remindly.auth;

public class InvalidTimezoneException extends RuntimeException {

    public InvalidTimezoneException() {
        super("Timezone must be a valid IANA timezone");
    }
}

