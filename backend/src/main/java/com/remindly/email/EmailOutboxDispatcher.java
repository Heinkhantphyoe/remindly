package com.remindly.email;

import com.remindly.notification.Notification;
import com.remindly.notification.NotificationRepository;
import com.remindly.reminder.Reminder;
import com.remindly.reminder.ReminderRepository;
import com.remindly.user.User;
import com.remindly.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailOutboxDispatcher {
    private static final DateTimeFormatter DUE_FORMAT = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy 'at' h:mm a z");

    private final EmailOutboxRepository outboxRepository;
    private final EmailOutboxClaimService claimService;
    private final NotificationRepository notificationRepository;
    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final EmailSender emailSender;
    private final Clock clock;
    private final int maxAttempts;
    private final long initialBackoffSeconds;
    private final long maxBackoffSeconds;
    private final String appUrl;

    public EmailOutboxDispatcher(EmailOutboxRepository outboxRepository,
                                 EmailOutboxClaimService claimService,
                                 NotificationRepository notificationRepository,
                                 ReminderRepository reminderRepository,
                                 UserRepository userRepository,
                                 EmailSender emailSender,
                                 Clock clock,
                                 @Value("${email.outbox.max-attempts:5}") int maxAttempts,
                                 @Value("${email.outbox.initial-backoff-seconds:60}") long initialBackoffSeconds,
                                 @Value("${email.outbox.max-backoff-seconds:3600}") long maxBackoffSeconds,
                                 @Value("${email.app-url:http://localhost:5173}") String appUrl) {
        this.outboxRepository = outboxRepository;
        this.claimService = claimService;
        this.notificationRepository = notificationRepository;
        this.reminderRepository = reminderRepository;
        this.userRepository = userRepository;
        this.emailSender = emailSender;
        this.clock = clock;
        this.maxAttempts = maxAttempts;
        this.initialBackoffSeconds = initialBackoffSeconds;
        this.maxBackoffSeconds = maxBackoffSeconds;
        this.appUrl = appUrl.replaceAll("/$", "");
    }

    @Scheduled(fixedRateString = "${email.outbox.fixed-rate-ms:60000}")
    public void run() { dispatchNow(); }

    public int dispatchNow() {
        List<EmailOutbox> jobs = claimService.claimDueJobs();
        for (EmailOutbox job : jobs) {
            try {
                send(job);
                markSent(job);
            } catch (Exception exception) {
                markFailure(job, exception);
            }
        }
        return jobs.size();
    }


    private void send(EmailOutbox job) {
        Notification notification = notificationRepository.findById(job.getNotificationId()).orElseThrow();
        Reminder reminder = reminderRepository.findById(notification.getReminderId()).orElseThrow();
        User user = userRepository.findById(notification.getUserId()).orElseThrow();
        ZoneId zone = ZoneId.of(user.getTimezone());
        String dueAt = DUE_FORMAT.format(reminder.getDueAt().atZone(zone));
        String kind = notification.getNotificationType() == com.remindly.notification.NotificationType.DUE
                ? "Reminder due" : "Upcoming reminder";
        String subject = kind + ": " + reminder.getTitle();
        String plain = kind + "\n\n" + reminder.getTitle() + "\nDue: " + dueAt + "\n"
                + (reminder.getDescription() == null ? "" : "\n" + reminder.getDescription() + "\n")
                + "\nOpen Remindly: " + appUrl;
        String safeTitle = escape(reminder.getTitle());
        String safeDescription = reminder.getDescription() == null ? "" : "<p>" + escape(reminder.getDescription()) + "</p>";
        String html = "<html><body><h2>" + escape(kind) + "</h2><h1>" + safeTitle + "</h1>"
                + "<p><strong>Due:</strong> " + escape(dueAt) + "</p>" + safeDescription
                + "<p><a href=\"" + escapeAttribute(appUrl) + "\">Open Remindly</a></p></body></html>";
        emailSender.send(job.getRecipientEmail(), subject, plain, html);
    }

    @Transactional
    void markSent(EmailOutbox job) { job.markSent(clock.instant()); outboxRepository.save(job); }

    @Transactional
    void markFailure(EmailOutbox job, Exception exception) {
        String message = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
        if (job.getAttemptCount() >= maxAttempts) {
            job.fail(message);
        } else {
            long multiplier = 1L << Math.min(job.getAttemptCount() - 1, 30);
            long delay = Math.min(maxBackoffSeconds, Math.max(initialBackoffSeconds, initialBackoffSeconds * multiplier));
            job.retry(clock.instant().plusSeconds(delay), message);
        }
        outboxRepository.save(job);
    }

    private String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private String escapeAttribute(String value) { return escape(value).replace(" ", "%20"); }
}
