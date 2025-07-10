package com.tesfai.everlink.service;

import com.tesfai.everlink.mapper.IEverLinkMapper;
import com.tesfai.everlink.repository.IEverLinkRepository;
import com.tesfai.everlink.utils.EverLinkUtils;
import com.tesfai.everlink.constant.EverLinkConstants;
import com.tesfai.everlink.constant.MartialStatusEnum;
import com.tesfai.everlink.constant.MembershipEnum;
import com.tesfai.everlink.dto.MemberDTO;
import com.tesfai.everlink.entity.Member;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class EverLinkServiceImpl implements IEverLinkService{

    private final IEverLinkRepository everLinkRepository;
    private final IEverLinkMapper everLinkMapper;

    public EverLinkServiceImpl(IEverLinkRepository everLinkRepository, IEverLinkMapper everLinkMapper) {
        this.everLinkRepository = everLinkRepository;
        this.everLinkMapper = everLinkMapper;
    }


    @Override
    public List<MemberDTO> getMembers(){
        List<Member> members = everLinkRepository.findAll();
        return everLinkMapper.mapToDto(members);
    }

    @Override
    public MemberDTO registerMember(MemberDTO memberDTO) {
        String memberId = generateMemberId(memberDTO);
        memberDTO.setMemberId(memberId);
        Member member = everLinkMapper.mapToEntity(memberDTO);
        Double mc = member.getMartialStatus().equalsIgnoreCase(MartialStatusEnum.Married.name())? EverLinkConstants.MEMBER_CONTRIBUTION_MARRIED:EverLinkConstants.MEMBER_CONTRIBUTION_SINGLE;
        member.setCurrentMonthlyContribution(mc);
        String joinDate = memberDTO.getJoinDate()!=null?memberDTO.getJoinDate():EverLinkUtils.toString(LocalDate.now());
        member.setJoinDate(joinDate);
        member.setMembershipStatus(MembershipEnum.Active.name());
        Member savedMember = everLinkRepository.save(member);
        calculateTotalContribution();
        calculatePercentageOfOwnership();
        return everLinkMapper.mapToDto(List.of(savedMember)).get(0);
    }

    private String generateMemberId(MemberDTO memberDTO) {
        String[] split = memberDTO.getFullName().split("\s");
        char firstInitial = Character.toUpperCase(split[0].charAt(0));
        char lastInitial = Character.toUpperCase(split[1].charAt(0));
        int randomNumber = new Random().nextInt(900) + 100;
        return "" + firstInitial + lastInitial + randomNumber;
    }

    @Override
    public MemberDTO updateMember(String memberId, MemberDTO memberDTO) {
        Optional<Member> memberFromDb = everLinkRepository.findByMemberId(memberId);
        Member updatedMember = null;
        Double lp = 0.0;
        if(memberFromDb.isPresent()) {
            Member member = memberFromDb.get();
            lp = member.getTotalPreviousLegacyPool()==null?0.0:member.getTotalPreviousLegacyPool();
            boolean statusNotChanged = member.getMartialStatus().equalsIgnoreCase(memberDTO.getMartialStatus());
            if(!statusNotChanged && StringUtils.isBlank(memberDTO.getLeaveDate())){
                member.setPreviousMonthlyContribution(member.getCurrentMonthlyContribution());
                String changeDate = memberDTO.getStatusChangeDate()==null?EverLinkUtils.toString(LocalDate.now()):memberDTO.getStatusChangeDate();
                member.setStatusChangeDate(changeDate);
                member.setStatusChanged(true);
            }
            member = everLinkMapper.updateMember(member, memberDTO);
            updatedMember = everLinkRepository.save(member);
        }
        calculateTotalContribution();
        calculateLegacyPoolOnLeave(updatedMember, lp);
        calculatePercentageOfOwnership();
        return everLinkMapper.mapToDto(List.of(updatedMember)).get(0);
    }

    @Override
    public String resetData() {
        everLinkRepository.findAll().stream()
                .peek(m->{
                    m.setTotalContribution(0.0);
                    m.setTotalPreviousLegacyPool(0.0);
                    m.setPercentageOfOwnership(0.0);
                    m.setLeaveDate(null);
                    m.setStatusChangeDate(null);
                    m.setStatusChanged(false);
                    m.setMembershipStatus(MembershipEnum.Active.name());
                    everLinkRepository.save(m);
                    calculateTotalContribution();
                })
                .collect(Collectors.toList());
        return "Successfully reset !!!";
    }

    @Override
    @Transactional
    public String deleteMember(String memberId) {
        Optional<Member> member = everLinkRepository.findByMemberId(memberId);
        if(!member.isPresent()){
           return "No member with id : "+memberId;
        }
        everLinkRepository.deleteByMemberId(memberId);
        return "Member with id : "+memberId+" deleted.";
    }

    @Override
    public String deleteMember() {
        everLinkRepository.deleteAll();
        return "Members deleted.";
    }

    @Override
    public MemberDTO retrieveMember(String memberId) {
        Optional<Member> member = everLinkRepository.findByMemberId(memberId);
        if(member.isPresent()){
            List<MemberDTO> memberDTOList = everLinkMapper.mapToDto(List.of(member.get()));
            return memberDTOList.get(0);
        }
        return null;
    }

    private void calculateTotalContribution(){
        everLinkRepository.findAll().stream()
                .peek(m->{
                    Double currentMonthlyContribution = m.getCurrentMonthlyContribution()==null?0.0:m.getCurrentMonthlyContribution();
                    Double previousMonthlyContribution = m.getPreviousMonthlyContribution()==null?0.0:m.getPreviousMonthlyContribution();
                    Double tc = 0.0;
                    if(StringUtils.isNotBlank(m.getLeaveDate()) && StringUtils.isNotBlank(m.getStatusChangeDate())){
                        tc = currentMonthlyContribution * EverLinkUtils.monthsBetweenDates(EverLinkUtils.fromString(m.getStatusChangeDate()), EverLinkUtils.fromString(m.getLeaveDate())) +
                                previousMonthlyContribution * EverLinkUtils.monthsBetweenDates(EverLinkUtils.fromString(m.getJoinDate()), EverLinkUtils.fromString(m.getStatusChangeDate()));
                    }else if(StringUtils.isNotBlank(m.getLeaveDate())){
                        tc = currentMonthlyContribution * EverLinkUtils.monthsBetweenDates(EverLinkUtils.fromString(m.getJoinDate()), EverLinkUtils.fromString(m.getLeaveDate()));
                    }else if(StringUtils.isNotBlank(m.getStatusChangeDate())){
                        tc = currentMonthlyContribution * EverLinkUtils.monthsBetweenDates(EverLinkUtils.fromString(m.getStatusChangeDate()), LocalDate.now()) +
                                previousMonthlyContribution * EverLinkUtils.monthsBetweenDates(EverLinkUtils.fromString(m.getJoinDate()), EverLinkUtils.fromString(m.getStatusChangeDate()));
                    }else{
                        tc = currentMonthlyContribution * EverLinkUtils.monthsBetweenDates(EverLinkUtils.fromString(m.getJoinDate()), LocalDate.now());
                    }
                    m.setTotalContribution(tc);
                    everLinkRepository.save(m);
                })
                .collect(Collectors.toList());
    }

    private void calculateLegacyPoolOnLeave(Member updatedMember, Double leftLp){
        String leaveDate = updatedMember.getLeaveDate();
        if(StringUtils.isBlank(leaveDate)){
            return;
        }
        Double tc = updatedMember.getTotalContribution()==null?0.0:updatedMember.getTotalContribution() + leftLp;

        LocalDate endDate = EverLinkUtils.fromString(leaveDate);

        double totalWeight = everLinkRepository.findAll().stream()
                .filter(m -> m.getMembershipStatus().equalsIgnoreCase(MembershipEnum.Active.name())
                        && !(EverLinkUtils.isAfter(EverLinkUtils.fromString(m.getJoinDate()), endDate)
                                || EverLinkUtils.fromString(m.getJoinDate()).isEqual(endDate)
                            )
                        )
                .map(m -> {
                    LocalDate joinDate = EverLinkUtils.fromString(m.getJoinDate());
                    long months = EverLinkUtils.monthsBetweenDates(joinDate, LocalDate.now());
                    double factor = m.getMartialStatus().equalsIgnoreCase(MartialStatusEnum.Married.name()) ? 1.5 : 1.0;
                    return months * factor;
                })
                .reduce(0.0, Double::sum);


        everLinkRepository.findAll().stream()
                .filter(m->m.getMembershipStatus().equalsIgnoreCase(MembershipEnum.Active.name())
                    && !(EverLinkUtils.isAfter(EverLinkUtils.fromString(m.getJoinDate()), endDate)
                        || EverLinkUtils.fromString(m.getJoinDate()).isEqual(endDate)
                ))
                .peek(m->{
                    Double lp =0.0;
                    int monthsOfMember = EverLinkUtils.monthsBetweenDates(EverLinkUtils.fromString(m.getJoinDate()), LocalDate.now());
                    Double ratio ;
                    if(m.getMartialStatus().equalsIgnoreCase(MartialStatusEnum.Married.name())){
                        ratio = (1.5*monthsOfMember)/totalWeight;
                        Double lpm = ratio*tc;
                        lp = m.getTotalPreviousLegacyPool()==null?0.0:m.getTotalPreviousLegacyPool()+lpm;
                    }else{
                        ratio = monthsOfMember/totalWeight;
                        Double lps = ratio*tc;
                        lp = m.getTotalPreviousLegacyPool()==null?0.0:m.getTotalPreviousLegacyPool()+lps;
                    }
                    Double per = new BigDecimal(Double.toString(lp))
                            .setScale(2, RoundingMode.CEILING)
                            .doubleValue();
                    m.setTotalPreviousLegacyPool(per);
                    everLinkRepository.save(m);
                })
                .collect(Collectors.toList());
    }

    private void calculatePercentageOfOwnership(){
        Double totalContribution = everLinkRepository.findAll().stream()
                .map(Member::getTotalContribution)
                .reduce(0.0, Double::sum);
        everLinkRepository.findAll().stream()
                .filter(m->m.getMembershipStatus().equalsIgnoreCase(MembershipEnum.Active.name()))
                .peek(m->{
                    if(totalContribution!=0.0) {
                        Double lp = m.getTotalPreviousLegacyPool() == null ? 0.0 : m.getTotalPreviousLegacyPool();
                        Double tc = m.getTotalContribution() == null ? 0.0 : m.getTotalContribution();
                        Double p = 100 * (lp + tc) / totalContribution;
                        Double per = new BigDecimal(Double.toString(p))
                                .setScale(2, RoundingMode.CEILING)
                                .doubleValue();
                        m.setPercentageOfOwnership(per);
                    }else{
                        m.setPercentageOfOwnership(0.0);
                    }
                    everLinkRepository.save(m);
                })
                .collect(Collectors.toList());
    }
}
