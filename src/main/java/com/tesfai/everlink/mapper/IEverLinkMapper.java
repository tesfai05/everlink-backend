package com.tesfai.everlink.mapper;

import com.tesfai.everlink.dto.BeneficiaryDTO;
import com.tesfai.everlink.dto.MemberDTO;
import com.tesfai.everlink.dto.UserDTO;
import com.tesfai.everlink.entity.Beneficiary;
import com.tesfai.everlink.entity.Member;
import com.tesfai.everlink.entity.User;

import java.util.List;

public interface IEverLinkMapper {
    List<MemberDTO> mapToDto(List<Member> members);

    Member mapToEntity(MemberDTO memberDTO);

    Member updateMember(Member member, MemberDTO memberDTO);

    User mapToUser(UserDTO userDTO, User user);

    UserDTO mapToUserDTO(User savedUser, String memberId);

    boolean passwordMatches(UserDTO userDTO, User user);

    BeneficiaryDTO mapToBeneficiaryDTO(Beneficiary beneficiary);
}
