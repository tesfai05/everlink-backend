package com.tesfai.everlink.dto;

import java.util.List;

public class BeneficiaryFormDTO {
    private String grantorId;
    private List<BeneficiaryDTO> beneficiaries;

    public BeneficiaryFormDTO(String grantorId, List<BeneficiaryDTO> beneficiaries) {
        this.grantorId = grantorId;
        this.beneficiaries = beneficiaries;
    }

    public String getGrantorId() {
        return grantorId;
    }

    public void setGrantorId(String grantorId) {
        this.grantorId = grantorId;
    }

    public List<BeneficiaryDTO> getBeneficiaries() {
        return beneficiaries;
    }

    public void setBeneficiaries(List<BeneficiaryDTO> beneficiaries) {
        this.beneficiaries = beneficiaries;
    }
}
