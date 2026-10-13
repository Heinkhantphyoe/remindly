package com.remindly.reminder;

import com.remindly.auth.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @PostMapping
    public ResponseEntity<ReminderResponse> create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateReminderRequest request
    ) {
        return ResponseEntity.ok(reminderService.create(user.id(), request));
    }

    @GetMapping
    public ResponseEntity<Page<ReminderResponse>> search(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Instant dueFrom,
            @RequestParam(required = false) Instant dueTo,
            @PageableDefault(size = 20, sort = "dueAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(reminderService.search(user.id(), completed, category, dueFrom, dueTo, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReminderResponse> get(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(reminderService.get(user.id(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReminderResponse> update(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateReminderRequest request
    ) {
        return ResponseEntity.ok(reminderService.update(user.id(), id, request));
    }

    @PatchMapping("/{id}/completion")
    public ResponseEntity<ReminderResponse> updateCompletion(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID id,
            @Valid @RequestBody CompletionRequest request
    ) {
        return ResponseEntity.ok(reminderService.updateCompletion(user.id(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID id
    ) {
        reminderService.delete(user.id(), id);
        return ResponseEntity.noContent().build();
    }
}
