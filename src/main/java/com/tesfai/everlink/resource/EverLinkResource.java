package com.tesfai.everlink.resource;

import com.tesfai.everlink.dto.EmailDTO;
import com.tesfai.everlink.dto.MemberDTO;
import com.tesfai.everlink.dto.UserDTO;
import com.tesfai.everlink.service.IEmailService;
import com.tesfai.everlink.service.IEverLinkService;
import com.tesfai.everlink.utils.EverLinkUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/members")
@CrossOrigin(origins = "*")
public class EverLinkResource {
    private final IEverLinkService everLinkService;
    private final IEmailService emailService;
    private final AuthenticationManager authenticationManager;

    public EverLinkResource(IEverLinkService everLinkService, IEmailService emailService, AuthenticationManager authenticationManager) {
        this.everLinkService = everLinkService;
        this.emailService = emailService;
        this.authenticationManager = authenticationManager;
    }


    @GetMapping
    public List<MemberDTO> getMembers(){
        return everLinkService.getMembers();
    }

    @PostMapping("/signin")
    public ResponseEntity<?> login(@RequestBody UserDTO userDTO, HttpServletRequest httpRequest) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDTO.getUsername(), userDTO.getPassword());
        try {
            Authentication auth = authenticationManager.authenticate(authToken);

            // Create new SecurityContext and save it to session
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);

            new HttpSessionSecurityContextRepository().saveContext(context, httpRequest, null);

            userDTO = everLinkService.signinMember(userDTO);
            return ResponseEntity.ok(Map.of(
                    "username", userDTO.getUsername(),
                    "memberId", userDTO.getMemberId(),
                    "roles", userDTO.getRoles()
            ));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserDTO userDTO){
        try {
            String username = userDTO.getUsername();
            if(username.length()<5 || !EverLinkUtils.isAlphanumeric(username) ){
                return ResponseEntity.badRequest().body("Invalid username.");
            }
            String password = userDTO.getPassword();
            if(password.length()<5 || !EverLinkUtils.isAlphanumeric(password)){
                return ResponseEntity.badRequest().body("Invalid password.");
            }
            String memberId = userDTO.getMemberId();
            List<String> memberIdList = everLinkService.getMembers().stream()
                    .map(m -> m.getMemberId())
                    .collect(Collectors.toList());
            if(!memberIdList.contains(memberId)){
                return ResponseEntity.badRequest().body("Invalid member ID.");
            }
            MemberDTO memberDTO = everLinkService.retrieveMember(userDTO.getMemberId());
            if(memberDTO.getSignedUp()!=null && memberDTO.getSignedUp()){
                return ResponseEntity.badRequest().body("Member with ID "+userDTO.getMemberId()+" already have account.");
            }

            return ResponseEntity.ok(everLinkService.signupMember(userDTO));
        }catch (Exception e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/admin")
    public ResponseEntity<?> updateUser(@RequestBody UserDTO userDTO){
        try {
            String memberId = userDTO.getMemberId();
            //Check memberId is valid
            List<String> memberIdList = everLinkService.getMembers().stream()
                    .map(m -> m.getMemberId())
                    .collect(Collectors.toList());
            if(!memberIdList.contains(memberId)){
                return ResponseEntity.badRequest().body("Invalid member ID.");
            }
            //check if member has account
            MemberDTO memberDTO = everLinkService.retrieveMember(userDTO.getMemberId());
            if(!memberDTO.getSignedUp()){
                return ResponseEntity.badRequest().body("Member with ID "+userDTO.getMemberId()+" has no account.");
            }

            return ResponseEntity.ok(everLinkService.updateUser(userDTO));
        }catch (Exception e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerMember(@RequestBody MemberDTO memberDTO){
        try {
            String joinDate = memberDTO.getJoinDate();
            if(StringUtils.isBlank(joinDate)){
                joinDate = EverLinkUtils.toString(LocalDate.now());
            }
            boolean isValid = EverLinkUtils.fromString(joinDate).isBefore(LocalDate.now()) ||  EverLinkUtils.fromString(joinDate).isEqual(LocalDate.now());
            if (!isValid) {
                return ResponseEntity.badRequest().body("Join date must not be in the future.");
            }
            return ResponseEntity.ok(everLinkService.registerMember(memberDTO));
        }catch (DateTimeParseException e){
            return ResponseEntity.badRequest().body("Invalid join date format. Please use MM/dd/yyyy.");
        }
    }

    @PostMapping("/update/{memberId}")
    public ResponseEntity<?> updateMember(@PathVariable String memberId, @RequestBody MemberDTO memberDTO){
        try {
            LocalDate now = LocalDate.now();
            LocalDate joinDate = EverLinkUtils.fromString(memberDTO.getJoinDate());
            LocalDate leaveDate = EverLinkUtils.fromString(memberDTO.getLeaveDate());
            LocalDate statusChangeDate = EverLinkUtils.fromString(memberDTO.getStatusChangeDate());

            // Rule 1: Dates must not be in the future
            if (joinDate != null && joinDate.isAfter(now)) {
                return ResponseEntity.badRequest().body("Join date cannot be in the future.");
            }
            if (leaveDate != null && leaveDate.isAfter(now)) {
                return ResponseEntity.badRequest().body("Leave date cannot be in the future.");
            }
            if (statusChangeDate != null && statusChangeDate.isAfter(now)) {
                return ResponseEntity.badRequest().body("Status change date cannot be in the future.");
            }

            // Rule 2: Leave date must be after join date
            if (joinDate != null && leaveDate != null && leaveDate.isBefore(joinDate)) {
                return ResponseEntity.badRequest().body("Leave date cannot be before join date.");
            }

            // Rule 3: Leave date must be after status change date
            if (leaveDate != null && statusChangeDate != null && statusChangeDate.isBefore(leaveDate)) {
                return ResponseEntity.badRequest().body("Leave date cannot be before status change date.");
            }

            // Rule 4: Status change date must be after join date
            if (joinDate != null && statusChangeDate != null && statusChangeDate.isBefore(joinDate)) {
                return ResponseEntity.badRequest().body("Status change date cannot be before join date.");
            }

            // Passes all validations
            return ResponseEntity.ok(everLinkService.updateMember(memberId, memberDTO));

        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Invalid date format. Please use MM/dd/yyyy.");
        }

    }

    @GetMapping("/retrieve/{memberId}")
    public ResponseEntity<?>  retrieveMember(@PathVariable String  memberId){
        return ResponseEntity.ok(everLinkService.retrieveMember(memberId));
    }

    @DeleteMapping("/delete/{memberId}")
    public String deleteMember(@PathVariable String memberId){
        return everLinkService.deleteMember(memberId);
    }

    @DeleteMapping("/delete")
    public String deleteMemberAll(){
        return everLinkService.deleteMember();
    }

    @GetMapping("/reset")
    public String  resetData(){
        return everLinkService.resetData();
    }

    @PostMapping("/email/send")
    public String sendEmailToAllMembers( @RequestBody EmailDTO emailDTO) {
        emailService.sendEmailToMembers(emailDTO.getSubject(), emailDTO.getBody());
        return "Email sent to members";
    }

    @GetMapping("/health-check")
    public String  healthCheck(){
        return "APPISUPNOW";
    }
}
