package com.tesfai.everlink.resource;

import com.tesfai.everlink.dto.*;
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
public class MemberResource {
    private final IEverLinkService everLinkService;
    private final IEmailService emailService;

    public MemberResource(IEverLinkService everLinkService, IEmailService emailService) {
        this.everLinkService = everLinkService;
        this.emailService = emailService;
    }


    @GetMapping("/admin")
    public List<MemberDTO> getMembers(){
        return everLinkService.getMembers();
    }

    @GetMapping("/admin/refresh-record")
    public ResponseEntity<?> refreshRecord(){
        everLinkService.refreshRecord();
        return ResponseEntity.ok("Record refreshed.");
    }

    @PostMapping("/admin/register")
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
            String fullName = memberDTO.getFullName().replaceAll("\\s+", " ");
            String[] name = fullName.split("\s");
            if(name!=null && name.length<2){
                ErrorDTO errorDTO = new ErrorDTO(
                        "400",
                        "First name and Last name are required."
                );
                return ResponseEntity.badRequest().body(errorDTO);
            }
            memberDTO.setFullName(fullName);
            return ResponseEntity.ok(everLinkService.registerMember(memberDTO));
        }catch (DateTimeParseException e){
            return ResponseEntity.badRequest().body("Invalid join date format. Please use MM/dd/yyyy.");
        }
    }

    @PostMapping("/user/update/{memberId}")
    public ResponseEntity<?> updateMember(@PathVariable String memberId, @RequestBody MemberDTO memberDTO){
        try {
            String fullName = memberDTO.getFullName().replaceAll("\\s+", " ");
            String[] name = fullName.split("\s");
            if(name!=null && name.length<2){
                return ResponseEntity.badRequest().body("First name and Last name are required.");
            }
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

    @DeleteMapping("/admin/delete/{memberId}")
    public String deleteMember(@PathVariable String memberId){
        return everLinkService.deleteMember(memberId);
    }

    @DeleteMapping("/admin/delete")
    public String deleteMemberAll(){
        return everLinkService.deleteMember();
    }

    @GetMapping("/admin/reset")
    public String  resetData(){
        return everLinkService.resetData();
    }

    @PostMapping("/admin/send-email")
    public String sendEmailToAllMembers( @RequestBody EmailDTO emailDTO) {
        emailService.sendEmailToMembers(emailDTO.getSubject(), emailDTO.getBody());
        return "Email sent to members";
    }

    @GetMapping("/user/retrieve/{memberId}")
    public ResponseEntity<?>  retrieveMember(@PathVariable String  memberId){
        return ResponseEntity.ok(everLinkService.retrieveMember(memberId));
    }
}
