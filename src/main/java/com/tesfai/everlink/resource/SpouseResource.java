package com.tesfai.everlink.resource;

import com.tesfai.everlink.dto.*;
import com.tesfai.everlink.service.IEverLinkService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/spouse")
@CrossOrigin(origins = "*")
public class SpouseResource {
    private final IEverLinkService everLinkService;

    public SpouseResource(IEverLinkService everLinkService) {
        this.everLinkService = everLinkService;
    }

    @PostMapping("/user/add")
    public ResponseEntity<?> addSpouse(@RequestBody SpouseDTO spouseDTO) {
        //Check memberId is valid
        String memberId = spouseDTO.getGrantorId();
        List<String> memberIdList = everLinkService.getMembers().stream()
                .map(m -> m.getMemberId())
                .collect(Collectors.toList());
        if(!memberIdList.contains(memberId)){
            return ResponseEntity.badRequest().body("Invalid grantor ID.");
        }
        String fullName = spouseDTO.getFullName().replaceAll("\\s+", " ");
        String[] name = fullName.split("\s");
        if(name!=null && name.length<2){
            ErrorDTO errorDTO = new ErrorDTO(
                    "400",
                    "First name and Last name are required."
            );
            return ResponseEntity.badRequest().body(errorDTO);
        }
        try {
            everLinkService.addSpouse(spouseDTO);
        } catch (SQLIntegrityConstraintViolationException | DataIntegrityViolationException e) {
            ErrorDTO errorDTO = new ErrorDTO(
                    "500",
                    "You have already added your spouse."
            );
           return ResponseEntity.internalServerError().body(errorDTO);
        }
        return ResponseEntity.ok("Spouse added successfully");
    }

    @GetMapping("/user/retrieve/{grantorId}")
    public ResponseEntity<?> retrieveSpouse(@PathVariable String grantorId) {
        //Check memberId is valid
        List<String> memberIdList = everLinkService.getMembers().stream()
                .map(m -> m.getMemberId())
                .collect(Collectors.toList());
        if(!memberIdList.contains(grantorId)){
            return ResponseEntity.badRequest().body("Invalid grantor ID.");
        }
        SpouseDTO spouseDTO = everLinkService.retrieveSpouse(grantorId);
        return ResponseEntity.ok(spouseDTO);
    }
    @PutMapping("/user/update/{spouseId}")
    public ResponseEntity<?>  updateSpouse(@RequestBody SpouseDTO  spouseDTO, @PathVariable String spouseId){
        return ResponseEntity.ok(everLinkService.updateSpouse(spouseDTO, spouseId));
    }
    @DeleteMapping("/admin/delete/{spouseId}")
    public ResponseEntity<?>  deleteSpouse(@PathVariable String  spouseId){
        everLinkService.deleteSpouse(spouseId);
        return ResponseEntity.ok("Spouse deleted successfully");
    }
}
