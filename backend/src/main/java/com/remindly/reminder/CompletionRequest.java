package com.remindly.reminder;

import jakarta.validation.constraints.NotNull;

public record CompletionRequest(@NotNull Boolean completed) {
}
