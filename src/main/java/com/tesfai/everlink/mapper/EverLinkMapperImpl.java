package com.tesfai.everlink.mapper;

import com.tesfai.everlink.constant.EverLinkConstants;
import com.tesfai.everlink.constant.MartialStatusEnum;
import com.tesfai.everlink.constant.MembershipEnum;
import com.tesfai.everlink.dto.MemberDTO;
import com.tesfai.everlink.entity.Member;
import com.tesfai.everlink.utils.EverLinkUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class EverLinkMapperImpl implements IEverLinkMapper{

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
        if(StringUtils.isNotBlank(memberDTO.getMartialStatus())){
            member.setMartialStatus(memberDTO.getMartialStatus());
            Double mc = member.getMartialStatus().equalsIgnoreCase(MartialStatusEnum.Married.name())? EverLinkConstants.MEMBER_CONTRIBUTION_MARRIED:EverLinkConstants.MEMBER_CONTRIBUTION_SINGLE;
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

    private Member mapToMember(MemberDTO memberDTO){
        Member member = new Member();
        member.setMemberId(memberDTO.getMemberId());
        member.setFullName(memberDTO.getFullName());
        member.setEmail(memberDTO.getEmail());
        member.setJoinDate(memberDTO.getJoinDate());
        member.setLeaveDate(memberDTO.getLeaveDate());
        member.setMartialStatus(memberDTO.getMartialStatus());
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
        memberDTO.setMartialStatus(member.getMartialStatus());
        memberDTO.setMembershipStatus(member.getMembershipStatus());
        memberDTO.setTotalContribution(member.getTotalContribution());
        memberDTO.setTotalPreviousLegacyPool(member.getTotalPreviousLegacyPool());
        memberDTO.setPercentageOfOwnership(member.getPercentageOfOwnership());
        memberDTO.setStatusChangeDate(member.getStatusChangeDate());
        memberDTO.setStatusChanged(member.getStatusChanged());
        return memberDTO;
    }
}
