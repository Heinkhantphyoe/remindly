package com.remindly.reminder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface ReminderRepository extends JpaRepository<Reminder, UUID> {

    Optional<Reminder> findByIdAndUserId(UUID id, UUID userId);

    List<Reminder> findByCompletedFalseAndDueAtBetween(Instant dueFrom, Instant dueTo);

    List<Reminder> findByCompletedFalseAndAdvanceOffsetMinutesIsNotNullAndDueAtAfter(Instant dueAt);

    @Query("""
            select r from Reminder r
            where r.userId = :userId
              and coalesce(:completed, r.completed) = r.completed
              and (coalesce(:category, '') = '' or r.category = :category)
              and r.dueAt >= coalesce(:dueFrom, r.dueAt)
              and r.dueAt <= coalesce(:dueTo, r.dueAt)
            """)
    Page<Reminder> search(
            @Param("userId") UUID userId,
            @Param("completed") Boolean completed,
            @Param("category") String category,
            @Param("dueFrom") Instant dueFrom,
            @Param("dueTo") Instant dueTo,
            Pageable pageable
    );
}
