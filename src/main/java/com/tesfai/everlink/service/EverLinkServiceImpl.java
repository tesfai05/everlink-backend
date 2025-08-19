package com.tesfai.everlink.service;

import com.tesfai.everlink.dto.*;
import com.tesfai.everlink.entity.*;
import com.tesfai.everlink.mapper.IEverLinkMapper;
import com.tesfai.everlink.repository.*;
import com.tesfai.everlink.utils.EverLinkUtils;
import com.tesfai.everlink.constant.EverLinkConstants;
import com.tesfai.everlink.constant.MartialStatusEnum;
import com.tesfai.everlink.constant.MembershipEnum;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLIntegrityConstraintViolationException;
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
    private final IBeneficiaryRepository beneficiaryRepository;
    private final ISpouseRepository spouseRepository;

    public EverLinkServiceImpl(IEverLinkRepository everLinkRepository, IEverLinkMapper everLinkMapper, IUserRepository userRepository, IRoleRepository roleRepository, IEmailService emailService, IBeneficiaryRepository beneficiaryRepository, ISpouseRepository spouseRepository) {
        this.everLinkRepository = everLinkRepository;
        this.everLinkMapper = everLinkMapper;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.emailService = emailService;
        this.beneficiaryRepository = beneficiaryRepository;
        this.spouseRepository = spouseRepository;
    }


    @Override
    public List<MemberDTO> getMembers(){
        List<Member> members = everLinkRepository.findAll();
        return everLinkMapper.mapToDto(members);
    }

    @Override
    public MemberDTO registerMember(MemberDTO memberDTO) {
        String memberId = generateMemberId(memberDTO.getFullName());
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
        String subject = "Membership Registration Confirmation. ";
        String body = """
                        Hi %s,
                    
                        Thank you for registering with EverLink Holding LLC.
                        Your Member ID is: %s.
                        Please keep this ID in a safe place, as you will need it to create and manage your account with us.
                        We’re glad to have you as part of our community!
                    
                        Warm Regards,
                        EverLink Holding LLC Support Team.
                        """.formatted(memberDTO.getFullName(), memberId);

        emailService.sendEmail(memberDTO.getEmail(), subject, body);
        return savedMemberSTO;
    }

    private String generateMemberId(String fullName) {
        String[] split = fullName.split("\s");
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
                //sent email
                String subject = " Marital Status Update Confirmation. ";
                String body = """
                        Hi %s,
                    
                        Congratulations! Your status has been successfully updated to %s , effective %s.
                        We wish you happiness and joy in this new chapter of your life.
                    
                        Warm Regards,
                        EverLink Holding LLC Support Team.
                        """.formatted(memberDTO.getFullName(), memberDTO.getMaritalStatus(), changeDate);
                emailService.sendEmailOnUpdateInfo(subject, body, member.getMemberId());
            }
            member = everLinkMapper.updateMember(member, memberDTO);
            if(memberDTO.getLeaveDate()!=null){
                //sent email
                String subject = "Farewell and Best Wishes. ";
                String body = """
                        Hi %s,
                    
                        We are sorry to hear that you are leaving our partnership LLC as of %s. 
                        Your contributions have been truly appreciated, and you will be missed.
                        You are always welcome to return to the community at any time that is convenient for you. 
                        We wish you all the best in your future endeavors.
                        
                        Warm Regards,
                        EverLink Holding LLC Support Team.
                        """.formatted(memberDTO.getFullName(), member.getLeaveDate());
                emailService.sendEmailOnUpdateInfo(subject, body, member.getMemberId());
            }
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
    public void addBeneficiaries(BeneficiaryFormDTO beneficiaryFormDTO) {
        for(BeneficiaryDTO b : beneficiaryFormDTO.getBeneficiaries()){
            Beneficiary beneficiary = new Beneficiary();
            beneficiary.setGrantorId(beneficiaryFormDTO.getGrantorId());
            beneficiary.setBeneficiaryId(generateMemberId(b.getFullName()));
            beneficiary.setFullName(b.getFullName());
            beneficiary.setMaritalStatus(b.getMaritalStatus());
            beneficiary.setEmail(b.getEmail());
            beneficiaryRepository.save(beneficiary);
        }
    }

    @Override
    public List<BeneficiaryDTO> retrieveBeneficiaries(String grantorId) {
        List<Beneficiary> beneficiaries = beneficiaryRepository.findByGrantorId(grantorId);
        List<BeneficiaryDTO> beneficiaryDTOList = new ArrayList<>();
        if(beneficiaries!=null && beneficiaries.size()>0) {
            for(Beneficiary beneficiary : beneficiaries){
                BeneficiaryDTO beneficiaryDTO = everLinkMapper.mapToBeneficiaryDTO(beneficiary);
                beneficiaryDTOList.add(beneficiaryDTO);
            }
        }
        return beneficiaryDTOList;
    }

    @Override
    public void addSpouse(SpouseDTO spouseDTO) throws SQLIntegrityConstraintViolationException, DataIntegrityViolationException {
        Spouse spouse = new Spouse();
        spouse.setGrantorId(spouseDTO.getGrantorId());
        spouse.setSpouseId(generateMemberId(spouseDTO.getFullName()));
        spouse.setFullName(spouseDTO.getFullName());
        spouse.setMaritalStatus(spouseDTO.getMaritalStatus());
        spouse.setEmail(spouseDTO.getEmail());
        spouseRepository.save(spouse);
    }

    @Override
    public SpouseDTO retrieveSpouse(String grantorId) {
        Spouse spouse = spouseRepository.findByGrantorId(grantorId);
        SpouseDTO spouseDTO = new SpouseDTO();
        if(spouse!=null) {
            spouseDTO.setGrantorId(spouse.getGrantorId());
            spouseDTO.setSpouseId(spouse.getSpouseId());
            spouseDTO.setFullName(spouse.getFullName());
            spouseDTO.setMaritalStatus(spouse.getMaritalStatus());
            spouseDTO.setEmail(spouse.getEmail());
        }
        return spouseDTO;
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
        //sent email
        String subject = "Account Opening Confirmation.";
        String body = """
                        Hi %s,
                    
                        Congratulations! You have successfully created an account with EverLink Holding LLC.
                        Your username is: %s.
                        
                        Warm Regards,
                        EverLink Holding LLC Support Team.
                        """.formatted(member.getFullName(), user.getUsername());

        emailService.sendEmailOnUpdateInfo(subject, body, userDTO.getMemberId());
        return everLinkMapper.mapToUserDTO(savedUser, userDTO.getMemberId());
    }

    @Override
    public UserDTO changePassword(UserDTO userDTO, User user) {
        user = everLinkMapper.mapToUser(userDTO, user);
        User savedUser = userRepository.save(user);
        //sent email
        Member member = everLinkRepository.findByMemberId(userDTO.getMemberId()).get();
        String subject = "Password change notification. ";
        String body = """
                        Hi %s,
                    
                        You have asked Everlink Holding LLC to change the password associated with your account.
                        If you did not change your password, please contact us at everlinkholdingllc@gmail.com.
                        
                        Warm Regards,
                        EverLink Holding LLC Support Team.
                        """.formatted(member.getFullName());

        emailService.sendEmailOnUpdateInfo(subject, body, userDTO.getMemberId());
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

    @Override
    public void refreshRecord() {
        List<Member> members = everLinkRepository.findAll();
        if(!members.isEmpty()) {
            members.stream().map(member -> {
                calculateTotalContribution();
                calculatePercentageOfOwnership();
                return member;
            });
        }
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
