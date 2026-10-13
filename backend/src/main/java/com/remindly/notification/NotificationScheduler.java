package com.remindly.notification;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationScheduler {

    private final NotificationSchedulingService schedulingService;

    public NotificationScheduler(NotificationSchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @Scheduled(fixedRateString = "${notifications.scheduler.fixed-rate-ms:60000}")
    public void run() {
        schedulingService.processNow();
    }
}
