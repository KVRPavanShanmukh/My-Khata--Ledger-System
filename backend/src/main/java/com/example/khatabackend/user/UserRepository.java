package com.example.khatabackend.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> { // Change Long to String for mobile
    Optional<User> findByMobile(String mobile); // Change findByEmail to findByMobile
    Optional<User> findByEmail(String email);
}