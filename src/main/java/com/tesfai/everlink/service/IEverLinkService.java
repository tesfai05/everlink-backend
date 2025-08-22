package com.tesfai.everlink.service;

import com.tesfai.everlink.dto.*;
import com.tesfai.everlink.entity.User;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Optional;

public interface IEverLinkService {
    List<MemberDTO> getMembers();
    MemberDTO registerMember(MemberDTO memberDTO);
    MemberDTO retrieveMember(String memberId);
    MemberDTO updateMember(String memberId, MemberDTO memberDTO);

    String deleteMember(String memberId);
    String deleteMember();

    UserDTO signupMember(UserDTO userDTO);
    UserDTO changePassword(UserDTO userDTO, User user);
    UserDTO updateUser(UserDTO userDTO) ;
    UserDTO signinMember(UserDTO userDTO);
    Optional<User> retrieveUser(String memberId);

    void refreshRecord();
    String resetData();

    void addBeneficiaries(BeneficiaryFormDTO beneficiaryFormDTO);

    List<BeneficiaryDTO> retrieveBeneficiaries(String grantorId);

    void addSpouse(SpouseDTO spouseDTO) throws SQLIntegrityConstraintViolationException, DataIntegrityViolationException;

    SpouseDTO retrieveSpouse(String grantorId);

    BeneficiaryDTO retrieveBeneficiary(String beneficiaryId);

    BeneficiaryDTO updateBeneficiary(BeneficiaryDTO beneficiaryDTO, String beneficiaryId);

    void removeBeneficiary(String beneficiaryId);

    SpouseDTO updateSpouse(SpouseDTO spouseDTO, String spouseId);

    void deleteSpouse(String spouseId);
}
