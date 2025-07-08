package com.tesfai.everlink.service;

import com.tesfai.everlink.dto.MemberDTO;

import java.util.List;

public interface IEverLinkService {
    List<MemberDTO> getMembers();

    MemberDTO registerMember(MemberDTO memberDTO);

    MemberDTO updateMember(String memberId, MemberDTO memberDTO);

    String resetData();

    String deleteMember(String memberId);
    String deleteMember();

    MemberDTO retrieveMember(String memberId);
}
