package com.tesfai.everlink.service;

import com.tesfai.everlink.dto.MemberDTO;
import com.tesfai.everlink.dto.UserDTO;
import com.tesfai.everlink.entity.User;

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
}
