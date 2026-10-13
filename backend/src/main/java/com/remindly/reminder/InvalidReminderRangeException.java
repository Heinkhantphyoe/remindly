package com.remindly.reminder;

public class InvalidReminderRangeException extends RuntimeException {
    public InvalidReminderRangeException() {
        super("dueFrom must be before or equal to dueTo");
    }
}
