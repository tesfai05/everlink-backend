package com.tesfai.everlink.resource;

import com.tesfai.everlink.dto.EmailDTO;
import com.tesfai.everlink.dto.MemberDTO;
import com.tesfai.everlink.service.IEmailService;
import com.tesfai.everlink.service.IEverLinkService;
import com.tesfai.everlink.utils.EverLinkUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/members")
@CrossOrigin(origins = "*")
public class EverLinkResource {
    private final IEverLinkService everLinkService;

    private final IEmailService emailService;

    public EverLinkResource(IEverLinkService everLinkService, IEmailService emailService) {
        this.everLinkService = everLinkService;
        this.emailService = emailService;
    }


    @GetMapping
    public List<MemberDTO> getMembers(){
        return everLinkService.getMembers();
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
