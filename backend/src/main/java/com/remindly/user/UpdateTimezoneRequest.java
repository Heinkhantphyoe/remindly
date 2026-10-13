package com.remindly.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTimezoneRequest(@NotBlank @Size(max = 64) String timezone) {
}

