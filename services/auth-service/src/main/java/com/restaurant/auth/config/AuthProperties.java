package com.restaurant.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    private String defaultTenantId;
    private String defaultPropertyId;
    private int otpExpiryMinutes;
    private boolean exposeDevOtp;
    private String seedAdminUsername;
    private String seedAdminPassword;
    private String seedAdminEmail;
    private String seedAdminPhoneCountryCode;
    private String seedAdminPhoneNumber;
    private int sessionTokenMinutes;

    public String getDefaultTenantId() {
        return defaultTenantId;
    }

    public void setDefaultTenantId(String defaultTenantId) {
        this.defaultTenantId = defaultTenantId;
    }

    public int getOtpExpiryMinutes() {
        return otpExpiryMinutes;
    }

    public String getDefaultPropertyId() {
        return defaultPropertyId;
    }

    public void setDefaultPropertyId(String defaultPropertyId) {
        this.defaultPropertyId = defaultPropertyId;
    }

    public void setOtpExpiryMinutes(int otpExpiryMinutes) {
        this.otpExpiryMinutes = otpExpiryMinutes;
    }

    public boolean isExposeDevOtp() {
        return exposeDevOtp;
    }

    public void setExposeDevOtp(boolean exposeDevOtp) {
        this.exposeDevOtp = exposeDevOtp;
    }

    public String getSeedAdminUsername() {
        return seedAdminUsername;
    }

    public void setSeedAdminUsername(String seedAdminUsername) {
        this.seedAdminUsername = seedAdminUsername;
    }

    public String getSeedAdminPassword() {
        return seedAdminPassword;
    }

    public void setSeedAdminPassword(String seedAdminPassword) {
        this.seedAdminPassword = seedAdminPassword;
    }

    public String getSeedAdminEmail() {
        return seedAdminEmail;
    }

    public void setSeedAdminEmail(String seedAdminEmail) {
        this.seedAdminEmail = seedAdminEmail;
    }

    public String getSeedAdminPhoneCountryCode() {
        return seedAdminPhoneCountryCode;
    }

    public void setSeedAdminPhoneCountryCode(String seedAdminPhoneCountryCode) {
        this.seedAdminPhoneCountryCode = seedAdminPhoneCountryCode;
    }

    public String getSeedAdminPhoneNumber() {
        return seedAdminPhoneNumber;
    }

    public void setSeedAdminPhoneNumber(String seedAdminPhoneNumber) {
        this.seedAdminPhoneNumber = seedAdminPhoneNumber;
    }

    public int getSessionTokenMinutes() {
        return sessionTokenMinutes;
    }

    public void setSessionTokenMinutes(int sessionTokenMinutes) {
        this.sessionTokenMinutes = sessionTokenMinutes;
    }
}
