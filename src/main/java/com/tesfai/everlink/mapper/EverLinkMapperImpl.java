package com.tesfai.everlink.mapper;

import com.tesfai.everlink.constant.EverLinkConstants;
import com.tesfai.everlink.constant.MartialStatusEnum;
import com.tesfai.everlink.constant.MembershipEnum;
import com.tesfai.everlink.dto.*;
import com.tesfai.everlink.entity.Beneficiary;
import com.tesfai.everlink.entity.Member;
import com.tesfai.everlink.entity.Spouse;
import com.tesfai.everlink.entity.User;
import com.tesfai.everlink.utils.EverLinkUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
public class EverLinkMapperImpl implements IEverLinkMapper{

    private final PasswordEncoder passwordEncoder;

    public EverLinkMapperImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<MemberDTO> mapToDto(List<Member> members) {
        List<MemberDTO> memberDTOList = new ArrayList<>();
        members.forEach(member -> {
            MemberDTO memberDTO = mapToMemberDTO(member);
            memberDTOList.add(memberDTO);
        });
        return memberDTOList;
    }

    @Override
    public Member mapToEntity(MemberDTO memberDTO) {
        return mapToMember(memberDTO);
    }

    @Override
    public Member updateMember(Member member, MemberDTO memberDTO) {
        if(StringUtils.isNotBlank(memberDTO.getFullName())){
            member.setFullName(memberDTO.getFullName());
        }
        if(StringUtils.isNotBlank(memberDTO.getEmail())){
            member.setEmail(memberDTO.getEmail());
        }
        if(StringUtils.isNotBlank(memberDTO.getMaritalStatus())){
            member.setMaritalStatus(memberDTO.getMaritalStatus());
            Double mc = member.getMaritalStatus().equalsIgnoreCase(MartialStatusEnum.Married.name())? EverLinkConstants.MEMBER_CONTRIBUTION_MARRIED:EverLinkConstants.MEMBER_CONTRIBUTION_SINGLE;
            member.setCurrentMonthlyContribution(mc);
        }
        if(StringUtils.isNotBlank(memberDTO.getMembershipStatus())){
            if(memberDTO.getMembershipStatus().equalsIgnoreCase(MembershipEnum.Exited.name())
                && StringUtils.isBlank(member.getLeaveDate())){
                member.setLeaveDate(EverLinkUtils.toString(LocalDate.now()));
                member.setTotalPreviousLegacyPool(0.0);
            }
            if(memberDTO.getMembershipStatus().equalsIgnoreCase(MembershipEnum.Active.name())
                    && StringUtils.isNotBlank(member.getLeaveDate())){
                member.setLeaveDate(null);
            }
            member.setMembershipStatus(memberDTO.getMembershipStatus());
        }
        if(StringUtils.isBlank(member.getLeaveDate()) && StringUtils.isNotBlank(memberDTO.getLeaveDate())){
            member.setLeaveDate(memberDTO.getLeaveDate());
            member.setMembershipStatus(MembershipEnum.Exited.name());
            member.setTotalPreviousLegacyPool(0.0);
            member.setPercentageOfOwnership(0.0);
        }
        return member;
    }

    @Override
    public User mapToUser(UserDTO userDTO, User user) {
        user.setMemberId(userDTO.getMemberId());
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setEnabled(true);
        return user;
    }

    @Override
    public UserDTO mapToUserDTO(User savedUser, String memberId) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(savedUser.getUsername());
        Set<RoleDTO> roles = new HashSet<>();
        savedUser.getRoles().forEach(r->{
            RoleDTO role = new RoleDTO();
            role.setName(r.getName());
            roles.add(role);
        });
        userDTO.setRoles(roles);
        userDTO.setUsername(savedUser.getUsername());
        userDTO.setMemberId(memberId);
        return userDTO;
    }

    @Override
    public boolean passwordMatches(UserDTO userDTO, User user) {
        return passwordEncoder.matches(userDTO.getPassword(),user.getPassword());
    }

    @Override
    public BeneficiaryDTO mapToBeneficiaryDTO(Beneficiary beneficiary) {
        BeneficiaryDTO beneficiaryDTO = new BeneficiaryDTO();
        beneficiaryDTO.setGrantorId(beneficiary.getGrantorId());
        beneficiaryDTO.setBeneficiaryId(beneficiary.getBeneficiaryId());
        beneficiaryDTO.setFullName(beneficiary.getFullName());
        beneficiaryDTO.setMaritalStatus(beneficiary.getMaritalStatus());
        beneficiaryDTO.setEmail(beneficiary.getEmail());
        return beneficiaryDTO;
    }

    @Override
    public Beneficiary mapToBeneficiaryEntity(Beneficiary beneficiary, BeneficiaryDTO beneficiaryDTO) {
        beneficiary.setFullName(beneficiaryDTO.getFullName());
        beneficiary.setMaritalStatus(beneficiaryDTO.getMaritalStatus());
        beneficiary.setEmail(beneficiaryDTO.getEmail());
        return beneficiary;
    }

    @Override
    public Spouse mapToSpouseEntity(Spouse spouse, SpouseDTO spouseDTO) {
        spouse.setFullName(spouseDTO.getFullName());
        spouse.setMaritalStatus(spouseDTO.getMaritalStatus());
        spouse.setEmail(spouseDTO.getEmail());
        return spouse;
    }

    @Override
    public SpouseDTO mapToSpouseDTO(Spouse spouse) {
        SpouseDTO spouseDTO = new SpouseDTO();
        spouseDTO.setGrantorId(spouse.getGrantorId());
        spouseDTO.setSpouseId(spouse.getSpouseId());
        spouseDTO.setFullName(spouse.getFullName());
        spouseDTO.setMaritalStatus(spouse.getMaritalStatus());
        spouseDTO.setEmail(spouse.getEmail());
        return spouseDTO;
    }

    private Member mapToMember(MemberDTO memberDTO){
        Member member = new Member();
        member.setMemberId(memberDTO.getMemberId());
        member.setFullName(memberDTO.getFullName());
        member.setEmail(memberDTO.getEmail());
        member.setJoinDate(memberDTO.getJoinDate());
        member.setLeaveDate(memberDTO.getLeaveDate());
        member.setMaritalStatus(memberDTO.getMaritalStatus());
        member.setMembershipStatus(memberDTO.getMembershipStatus());
        return member;
    }
    private MemberDTO mapToMemberDTO(Member member){
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setMemberId(member.getMemberId());
        memberDTO.setFullName(member.getFullName());
        memberDTO.setEmail(member.getEmail());
        memberDTO.setJoinDate(member.getJoinDate());
        memberDTO.setLeaveDate(member.getLeaveDate());
        memberDTO.setMaritalStatus(member.getMaritalStatus());
        memberDTO.setMembershipStatus(member.getMembershipStatus());
        memberDTO.setTotalContribution(member.getTotalContribution());
        memberDTO.setTotalPreviousLegacyPool(member.getTotalPreviousLegacyPool());
        memberDTO.setPercentageOfOwnership(member.getPercentageOfOwnership());
        memberDTO.setStatusChangeDate(member.getStatusChangeDate());
        memberDTO.setStatusChanged(member.getStatusChanged());
        memberDTO.setSignedUp(member.getSignedUp());
        memberDTO.setCurrentMonthlyContribution(member.getCurrentMonthlyContribution());
        memberDTO.setPreviousMonthlyContribution(member.getPreviousMonthlyContribution());
        return memberDTO;
    }
}
