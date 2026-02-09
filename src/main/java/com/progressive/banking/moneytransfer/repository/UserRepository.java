package com.progressive.banking.moneytransfer.repository;

import com.progressive.banking.moneytransfer.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}