package com.tesfai.everlink.repository;

import com.tesfai.everlink.entity.Spouse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ISpouseRepository extends JpaRepository<Spouse, Long> {
    Spouse findByGrantorId(String grantorId);

    Spouse findBySpouseId(String spouseId);

    void deleteBySpouseId(String spouseId);
}
