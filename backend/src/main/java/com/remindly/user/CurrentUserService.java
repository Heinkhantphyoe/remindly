package com.remindly.user;

import com.remindly.auth.UserResponse;
import com.remindly.auth.InvalidTimezoneException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.time.DateTimeException;
import java.time.ZoneId;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateTimezone(UUID userId, String timezone) {
        String normalizedTimezone = timezone.trim();
        try {
            ZoneId.of(normalizedTimezone);
        } catch (DateTimeException exception) {
            throw new InvalidTimezoneException();
        }

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        user.updateTimezone(normalizedTimezone);
        return UserResponse.from(user);
    }
}

