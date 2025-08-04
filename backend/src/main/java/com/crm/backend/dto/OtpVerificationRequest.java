package com.crm.backend.dto;

public class OtpVerificationRequest {
    private String email;
    private String otp;
    private String userRole; // The role we want to verify
    
    // Getters and Setters
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getOtp() {
        return otp;
    }
    
    public void setOtp(String otp) {
        this.otp = otp;
    }
    
    public String getuserRole() {
        return userRole;
    }
    
    public void setuserRole(String userRole) {
        this.userRole = userRole;
    }
}