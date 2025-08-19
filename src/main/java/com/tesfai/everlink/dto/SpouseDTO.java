package com.tesfai.everlink.dto;

public class SpouseDTO {
    private String grantorId;
    private String spouseId;
    private String fullName;
    private String maritalStatus;
    private String email;

    public String getGrantorId() {
        return grantorId;
    }

    public void setGrantorId(String grantorId) {
        this.grantorId = grantorId;
    }

    public String getSpouseId() {
        return spouseId;
    }

    public void setSpouseId(String spouseId) {
        this.spouseId = spouseId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
