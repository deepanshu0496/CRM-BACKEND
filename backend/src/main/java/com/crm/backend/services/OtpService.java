package com.crm.backend.services;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crm.backend.entity.Otp;
import com.crm.backend.repository.OtpRepo;

@Service
public class OtpService {
    
    @Autowired
    private OtpRepo otpRepo;

    @Transactional
    public void saveOtp(String userEmail, String userRole, Long otpValue) {
        Otp otp = new Otp();
        otp.setUserEmail(userEmail);
        otp.setUserRole(userRole);
        otp.setOtp(otpValue);
        otp.setCreatedAt(LocalDateTime.now());
        otp.setUsed(false); // Initialize as unused
        otpRepo.save(otp);
    }

    @Transactional
    public boolean verifyOtp(String email, String otp, String userRole) {
        try {
            Long otpValue = Long.parseLong(otp);
            Otp storedOtp = findByEmailAndOtp(email, otpValue);
            
            if (storedOtp == null || storedOtp.isUsed()) {
                return false; // OTP not found or already used
            }

            // Check expiry and role
            boolean isValid = storedOtp.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(5))
                          && (userRole == null || userRole.equals(storedOtp.getUserRole()));

            if (isValid) {
                storedOtp.setUsed(true); // Mark as used instead of deleting
                otpRepo.save(storedOtp);
            }
            
            return isValid;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Find OTP by email and value
    public Otp findByEmailAndOtp(String email, Long otpValue) {
        return otpRepo.findByUserEmailAndOtp(email, otpValue);
    }

    // Optional: Cleanup expired OTPs (run as scheduled job)
    @Transactional
@Scheduled(fixedRate = 24 * 60 * 60 * 1000) // Runs daily (adjust as needed)
public void cleanupExpiredOtps() {
    // Delete OTPs older than 30 minutes (adjust time as needed)
    otpRepo.deleteByCreatedAtBefore(LocalDateTime.now().minusMinutes(30));
    
    // Optional: Log how many records were deleted
    // logger.info("Cleaned up expired OTPs");
}

    // Check if OTP is valid (unused and not expired)
    public boolean isOtpValid(String email, Long otpValue) {
        Otp otp = findByEmailAndOtp(email, otpValue);
        return otp != null 
               && !otp.isUsed() 
               && otp.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(5));
    }
}