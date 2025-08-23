package com.tesfai.everlink.repository;

import com.tesfai.everlink.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByMemberId(String memberId);
    void deleteByMemberId(String memberId);
}

