package com.mindvault.backend.repository;

import com.mindvault.backend.model.Token;
import com.mindvault.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByToken(String token);
    List<Token> findAllByUser(User user);
}
