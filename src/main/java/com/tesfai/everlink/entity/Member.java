package com.tesfai.everlink.entity;

import jakarta.persistence.*;

@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String memberId;
    private String fullName;
    private String maritalStatus;
    private String email;
    private String joinDate;
    private String leaveDate;
    private String membershipStatus;

    private Double currentMonthlyContribution;
    private Double previousMonthlyContribution;
    private Double totalPreviousLegacyPool;
    private Double totalContribution;
    private Double percentageOfOwnership;

    private String statusChangeDate;
    private Boolean isStatusChanged = false;
    private Boolean isSignedUp = false;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Double getTotalPreviousLegacyPool() {
        return totalPreviousLegacyPool;
    }

    public void setTotalPreviousLegacyPool(Double totalPreviousLegacyPool) {
        this.totalPreviousLegacyPool = totalPreviousLegacyPool;
    }

    public Double getTotalContribution() {
        return totalContribution;
    }

    public void setTotalContribution(Double totalContribution) {
        this.totalContribution = totalContribution;
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
}
