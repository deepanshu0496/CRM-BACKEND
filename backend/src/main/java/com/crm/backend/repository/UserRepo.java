package com.crm.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.crm.backend.entity.User;

public interface UserRepo extends JpaRepository<User, Long>{
    Optional<User> findByUserEmailAndUserRole(String userEmail, String userRole);
}
