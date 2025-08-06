package com.crm.backend.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.crm.backend.entity.Otp;

@Repository
public interface OtpRepo extends JpaRepository<Otp, Long> {
    Otp findByUserEmailAndOtp(String email, Long otp);
    
    @Modifying
    @Query("DELETE FROM Otp o WHERE o.createdAt < :expiryTime")
    void deleteByCreatedAtBefore(LocalDateTime expiryTime);
}