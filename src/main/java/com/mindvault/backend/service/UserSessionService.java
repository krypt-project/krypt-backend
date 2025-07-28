package com.mindvault.backend.service;

import com.mindvault.backend.model.UserSession;
import com.mindvault.backend.repository.UserSessionRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserSessionService {
    private final UserSessionRepository userSessionRepository;

    public UserSessionService(UserSessionRepository userSessionRepository) {
        this.userSessionRepository = userSessionRepository;
    }

    public void invalidateUserSession(String token) {
        Optional<UserSession> userSession = userSessionRepository.findByToken(token);
        userSession.ifPresent(userSessionRepository::delete);
    }
}
