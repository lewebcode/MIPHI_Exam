package ru.mifi.practice.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mifi.practice.booking.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
