package com.tesfai.everlink.mapper;

import com.tesfai.everlink.dto.MemberDTO;
import com.tesfai.everlink.entity.Member;

import java.util.List;

public interface IEverLinkMapper {
    List<MemberDTO> mapToDto(List<Member> members);

    Member mapToEntity(MemberDTO memberDTO);

    Member updateMember(Member member, MemberDTO memberDTO);
}
