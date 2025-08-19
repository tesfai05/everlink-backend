package com.tesfai.everlink.dto;


import java.util.List;

public class MemberDTO {
    private String memberId;
    private String fullName;
    private String maritalStatus;
    private String email;
    private String joinDate;
    private String leaveDate;
    private String membershipStatus;
    private Double totalContribution;
    private Double totalPreviousLegacyPool;
    private Double percentageOfOwnership;
    private String statusChangeDate;
    private Boolean isStatusChanged;
    private Boolean isSignedUp;
    private Double currentMonthlyContribution;
    private Double previousMonthlyContribution;

    private SpouseDTO spouse;
    private List<BeneficiaryDTO> beneficiaries;

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
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

    public String getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(String joinDate) {
        this.joinDate = joinDate;
    }

    public String getLeaveDate() {
        return leaveDate;
    }

    public void setLeaveDate(String leaveDate) {
        this.leaveDate = leaveDate;
    }

    public String getMembershipStatus() {
        return membershipStatus;
    }

    public void setMembershipStatus(String membershipStatus) {
        this.membershipStatus = membershipStatus;
    }

    public Double getTotalContribution() {
        return totalContribution;
    }

    public void setTotalContribution(Double totalContribution) {
        this.totalContribution = totalContribution;
    }

    public Double getTotalPreviousLegacyPool() {
        return totalPreviousLegacyPool;
    }

    public void setTotalPreviousLegacyPool(Double totalPreviousLegacyPool) {
        this.totalPreviousLegacyPool = totalPreviousLegacyPool;
    }

    public Double getPercentageOfOwnership() {
        return percentageOfOwnership;
    }

    public void setPercentageOfOwnership(Double percentageOfOwnership) {
        this.percentageOfOwnership = percentageOfOwnership;
    }

    public String getStatusChangeDate() {
        return statusChangeDate;
    }

    public void setStatusChangeDate(String statusChangeDate) {
        this.statusChangeDate = statusChangeDate;
    }

    public Boolean getStatusChanged() {
        return isStatusChanged;
    }

    public void setStatusChanged(Boolean statusChanged) {
        isStatusChanged = statusChanged;
    }

    public Boolean getSignedUp() {
        return isSignedUp;
    }

    public void setSignedUp(Boolean signedUp) {
        isSignedUp = signedUp;
    }

    public Double getCurrentMonthlyContribution() {
        return currentMonthlyContribution;
    }

    public void setCurrentMonthlyContribution(Double currentMonthlyContribution) {
        this.currentMonthlyContribution = currentMonthlyContribution;
    }

    public Double getPreviousMonthlyContribution() {
        return previousMonthlyContribution;
    }

    public void setPreviousMonthlyContribution(Double previousMonthlyContribution) {
        this.previousMonthlyContribution = previousMonthlyContribution;
    }

    public SpouseDTO getSpouse() {
        return spouse;
    }

    public void setSpouse(SpouseDTO spouse) {
        this.spouse = spouse;
    }

    public List<BeneficiaryDTO> getBeneficiaries() {
        return beneficiaries;
    }

    public void setBeneficiaries(List<BeneficiaryDTO> beneficiaries) {
        this.beneficiaries = beneficiaries;
    }
}
