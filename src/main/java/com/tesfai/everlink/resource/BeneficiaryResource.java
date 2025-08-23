package com.tesfai.everlink.resource;

import com.tesfai.everlink.dto.*;
import com.tesfai.everlink.service.IEverLinkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/beneficiaries/user")
@CrossOrigin(origins = "*")
public class BeneficiaryResource {
    private final IEverLinkService everLinkService;

    public BeneficiaryResource(IEverLinkService everLinkService) {
        this.everLinkService = everLinkService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addBeneficiaries(@RequestBody BeneficiaryFormDTO beneficiaryFormDTO) {
        for(BeneficiaryDTO beneficiaryDTO : beneficiaryFormDTO.getBeneficiaries()){
            String fullName = beneficiaryDTO.getFullName().replaceAll("\\s+", " ");
            String[] name = fullName.split("\s");
            if(name!=null && name.length<2){
                ErrorDTO errorDTO = new ErrorDTO(
                        "400",
                        "First name and Last name are required."
                );
                return ResponseEntity.badRequest().body(errorDTO);
            }
        }

        //Check memberId is valid
        String memberId = beneficiaryFormDTO.getGrantorId();
        List<String> memberIdList = everLinkService.getMembers().stream()
                .map(m -> m.getMemberId())
                .collect(Collectors.toList());
        if(!memberIdList.contains(memberId)){
            return ResponseEntity.badRequest().body("Invalid grantor ID.");
        }
        everLinkService.addBeneficiaries(beneficiaryFormDTO);
        return ResponseEntity.ok("Beneficiaries added successfully");
    }

    @GetMapping("/retrieve-member/{grantorId}")
    public ResponseEntity<?> retrieveBeneficiaries(@PathVariable String grantorId) {
        //Check memberId is valid
        List<String> memberIdList = everLinkService.getMembers().stream()
                .map(m -> m.getMemberId())
                .collect(Collectors.toList());
        if(!memberIdList.contains(grantorId)){
            return ResponseEntity.badRequest().body("Invalid grantor ID.");
        }
        List<BeneficiaryDTO> beneficiaries = everLinkService.retrieveBeneficiaries(grantorId);
        return ResponseEntity.ok(beneficiaries);
    }

    @GetMapping("/retrieve/{beneficiaryId}")
    public ResponseEntity<?>  retrieveBeneficiary(@PathVariable String  beneficiaryId){
        return ResponseEntity.ok(everLinkService.retrieveBeneficiary(beneficiaryId));
    }

    @PutMapping("/update/{beneficiaryId}")
    public ResponseEntity<?>  updateBeneficiary(@RequestBody BeneficiaryDTO  beneficiaryDTO, @PathVariable String beneficiaryId){
        return ResponseEntity.ok(everLinkService.updateBeneficiary(beneficiaryDTO, beneficiaryId));
    }
    @DeleteMapping("/delete/{beneficiaryId}")
    public ResponseEntity<?>  removeBeneficiary(@PathVariable String  beneficiaryId){
        everLinkService.removeBeneficiary(beneficiaryId);
        return ResponseEntity.ok("Beneficiary removed successfully");
    }
}
