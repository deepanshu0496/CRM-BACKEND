package com.crm.backend.services;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crm.backend.entity.Otp;
import com.crm.backend.repository.OtpRepo;

@Service
public class OtpService {
    
    @Autowired
    private OtpRepo otpRepo;

    public void saveOtp(String userEmail, String userRole, Long otpValue) {
        Otp otp = new Otp();
        otp.setUserEmail(userEmail);
        otp.setUserRole(userRole);
        otp.setOtp(otpValue);
        otp.setCreatedAt(LocalDateTime.now());
        otpRepo.save(otp);
    }

  // verify otp
    public boolean verifyOtp(String email, String otp, String userRole) {
        try {
            Long otpValue = Long.parseLong(otp);
            Otp storedOtp = findByEmailAndOtp(email, otpValue);
            
            // Check if OTP exists, is valid, and role matches
            return storedOtp != null 
                   && storedOtp.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(5))
                   && (userRole == null || userRole.equals(storedOtp.getUserRole()));
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    // You might want to add other methods like:
    // - findByEmailAndOtp
    // - deleteOtp
    // - isOtpValid, etc.

    // Additional methods
    public Otp findByEmailAndOtp(String email, Long otpValue) {
        return otpRepo.findByUserEmailAndOtp(email, otpValue);
    }
    
    public void deleteOtp(Otp otp) {
        otpRepo.delete(otp);
    }

    public boolean isOtpValid(String email, Long otpValue) {
        Otp otp = findByEmailAndOtp(email, otpValue);
        return otp != null && otp.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(5));
    }
}