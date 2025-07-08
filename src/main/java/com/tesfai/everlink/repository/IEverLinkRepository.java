package com.tesfai.everlink.repository;

import com.tesfai.everlink.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IEverLinkRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByMemberId(String memberId);
    void deleteByMemberId(String memberId);
}
