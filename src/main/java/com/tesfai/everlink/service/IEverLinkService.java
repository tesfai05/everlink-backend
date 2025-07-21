package com.tesfai.everlink.service;

import com.tesfai.everlink.dto.MemberDTO;
import com.tesfai.everlink.dto.UserDTO;

import java.util.List;

public interface IEverLinkService {
    List<MemberDTO> getMembers();

    MemberDTO registerMember(MemberDTO memberDTO);

    MemberDTO updateMember(String memberId, MemberDTO memberDTO);

    String resetData();

    String deleteMember(String memberId);
    String deleteMember();

    MemberDTO retrieveMember(String memberId);

    UserDTO signupMember(UserDTO userDTO);
    UserDTO updateUser(UserDTO userDTO) ;
    UserDTO signinMember(UserDTO userDTO);
}
