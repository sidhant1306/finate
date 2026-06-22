package com.example.finatebackend.dao;

import com.example.finatebackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserEmail(String username);

    Optional<User>findByUserId(Long userId);

    void removeUserByUserId(Long userId);
}
