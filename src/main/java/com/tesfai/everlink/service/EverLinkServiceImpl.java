package com.tesfai.everlink.service;

import com.tesfai.everlink.dto.UserDTO;
import com.tesfai.everlink.entity.Role;
import com.tesfai.everlink.entity.User;
import com.tesfai.everlink.mapper.IEverLinkMapper;
import com.tesfai.everlink.repository.IEverLinkRepository;
import com.tesfai.everlink.repository.IRoleRepository;
import com.tesfai.everlink.repository.IUserRepository;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EverLinkServiceImpl implements IEverLinkService{

    private final IEverLinkRepository everLinkRepository;
    private final IEverLinkMapper everLinkMapper;
    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final IEmailService emailService;

    public EverLinkServiceImpl(IEverLinkRepository everLinkRepository, IEverLinkMapper everLinkMapper, IUserRepository userRepository, IRoleRepository roleRepository, IEmailService emailService) {
        this.everLinkRepository = everLinkRepository;
        this.everLinkMapper = everLinkMapper;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.emailService = emailService;
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
        Double mc = member.getMaritalStatus().equalsIgnoreCase(MartialStatusEnum.Married.name())? EverLinkConstants.MEMBER_CONTRIBUTION_MARRIED:EverLinkConstants.MEMBER_CONTRIBUTION_SINGLE;
        member.setCurrentMonthlyContribution(mc);
        String joinDate = memberDTO.getJoinDate()!=null?memberDTO.getJoinDate():EverLinkUtils.toString(LocalDate.now());
        member.setJoinDate(joinDate);
        member.setMembershipStatus(MembershipEnum.Active.name());
        Member savedMember = everLinkRepository.save(member);
        calculateTotalContribution();
        calculatePercentageOfOwnership();
        MemberDTO savedMemberSTO = everLinkMapper.mapToDto(List.of(savedMember)).get(0);
        //sent email
        String subject = "Thank you for registered with Ever Link Holding LLC. ";
        String body = "Hi "+memberDTO.getFullName()+", \n\n"+
                "Thank you for registered with Ever Link Holding LLC. "+"\n"+
                "Your member ID is "+memberId+", please keep in a save place. You need this ID to create account with us."+"\n\n"+
                "With Regards,"+" \n"+
                "EverLink Holding LLC";
        emailService.sendEmail(memberDTO.getEmail(), subject, body);
        return savedMemberSTO;
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
            boolean statusNotChanged = member.getMaritalStatus().equalsIgnoreCase(memberDTO.getMaritalStatus());
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
        return new MemberDTO();
    }

    @Override
    public UserDTO signupMember(UserDTO userDTO) {
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default role USER not found"));
        User user = everLinkMapper.mapToUser(userDTO, new User());
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        //add role to user & save
        user.setRoles(roles);
        User savedUser = userRepository.save(user);
        //update member with signedUp flag
        Member member = everLinkRepository.findByMemberId(userDTO.getMemberId()).get();
        member.setSignedUp(true);
        everLinkRepository.save(member);
        return everLinkMapper.mapToUserDTO(savedUser, userDTO.getMemberId());
    }

    @Override
    public UserDTO changePassword(UserDTO userDTO, User user) {
        user = everLinkMapper.mapToUser(userDTO, user);
        User savedUser = userRepository.save(user);
        return everLinkMapper.mapToUserDTO(savedUser, userDTO.getMemberId());
    }

    @Override
    public UserDTO updateUser(UserDTO userDTO) {
        Optional<User> userOptional = userRepository.findByUsername(userDTO.getUsername());
        if(userOptional.isPresent()){
            User user = userOptional.get();
            Role userRole;
            boolean isAdmin = user.getRoles().stream().anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getName()));
            if(isAdmin){
                userRole = roleRepository.findByName("USER")
                        .orElseThrow(() -> new RuntimeException("USER role not found"));
            }else{
                userRole = roleRepository.findByName("ADMIN")
                        .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
            }
            Set<Role> roles = new HashSet<>();
            roles.add(userRole);
            //update role to user & save
            user.setRoles(roles);
            User savedUser = userRepository.save(user);
            return everLinkMapper.mapToUserDTO(savedUser, userDTO.getMemberId());
        }
        return new UserDTO();
    }

    @Override
    public UserDTO signinMember(UserDTO userDTO) {
        Optional<User> user = userRepository.findByUsername(userDTO.getUsername());
        if(!user.isPresent()){
            throw new RuntimeException("Invalid username or password.");
        }
        boolean matches = everLinkMapper.passwordMatches(userDTO, user.get());
        if(!matches){
            throw new RuntimeException("Invalid username or password.");
        }
        String memberId = user.get().getMemberId();
        //Member member = everLinkRepository.findByMemberId(memberId).get();
        return everLinkMapper.mapToUserDTO(user.get(), memberId);
    }

    @Override
    public Optional<User> retrieveUser(String memberId) {
        return userRepository.findByMemberId(memberId);
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
                    double factor = m.getMaritalStatus().equalsIgnoreCase(MartialStatusEnum.Married.name()) ? 1.5 : 1.0;
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
                    if(m.getMaritalStatus().equalsIgnoreCase(MartialStatusEnum.Married.name())){
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
