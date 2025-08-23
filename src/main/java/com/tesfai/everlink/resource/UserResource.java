package com.tesfai.everlink.resource;

import com.tesfai.everlink.dto.*;
import com.tesfai.everlink.service.IEverLinkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*")
public class UserResource {
    private final IEverLinkService everLinkService;

    public UserResource(IEverLinkService everLinkService) {
        this.everLinkService = everLinkService;
    }

    @PostMapping("/admin/update")
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
            return ResponseEntity.internalServerError().body("An unexpected error during update user occurred.");
        }
    }
}
