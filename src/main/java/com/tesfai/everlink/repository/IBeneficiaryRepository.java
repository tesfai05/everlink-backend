package com.tesfai.everlink.repository;

import com.tesfai.everlink.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IBeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
    List<Beneficiary> findByGrantorId(String grantorId);
}
