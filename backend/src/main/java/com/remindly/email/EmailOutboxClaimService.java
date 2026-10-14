package com.remindly.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class EmailOutboxClaimService {
    private final EmailOutboxRepository repository;
    private final Clock clock;
    private final int batchSize;
    private final Duration staleProcessing;

    public EmailOutboxClaimService(EmailOutboxRepository repository,
                                   Clock clock,
                                   @Value("${email.outbox.batch-size:25}") int batchSize,
                                   @Value("${email.outbox.stale-processing-minutes:15}") long staleProcessingMinutes) {
        this.repository = repository;
        this.clock = clock;
        this.batchSize = batchSize;
        this.staleProcessing = Duration.ofMinutes(staleProcessingMinutes);
    }

    @Transactional
    public List<EmailOutbox> claimDueJobs() {
        Instant now = clock.instant();
        for (EmailOutbox stale : repository.findByStatusAndProcessingStartedAtBefore(
                EmailOutboxStatus.PROCESSING, now.minus(staleProcessing))) {
            stale.requeue(now);
        }
        List<EmailOutbox> jobs = repository
                .findTop100ByStatusAndNextAttemptAtLessThanEqualOrderByNextAttemptAtAsc(
                        EmailOutboxStatus.PENDING, now, PageRequest.of(0, batchSize));
        jobs.forEach(job -> job.claim(now));
        repository.saveAll(jobs);
        return jobs;
    }
}
