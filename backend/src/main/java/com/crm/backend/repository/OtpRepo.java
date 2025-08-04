package com.crm.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.crm.backend.entity.Otp;

@Repository
public interface OtpRepo extends JpaRepository<Otp, Long>{
    Otp findByUserEmailAndOtp(String email, Long otp);
}
