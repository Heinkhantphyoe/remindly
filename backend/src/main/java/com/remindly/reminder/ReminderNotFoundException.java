package com.remindly.reminder;

public class ReminderNotFoundException extends RuntimeException {
    public ReminderNotFoundException() {
        super("Reminder not found");
    }
}
