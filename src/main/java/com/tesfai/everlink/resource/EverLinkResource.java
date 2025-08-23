package com.tesfai.everlink.resource;

import com.tesfai.everlink.dto.*;
import com.tesfai.everlink.entity.User;
import com.tesfai.everlink.service.IEmailService;
import com.tesfai.everlink.service.IEverLinkService;
import com.tesfai.everlink.utils.EverLinkUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/public")
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
            return ResponseEntity.internalServerError().body("An unexpected error during signup occurred.");
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody UserDTO userDTO){
        try {
            String memberId = userDTO.getMemberId();
            Optional<User> retrievedUser = everLinkService.retrieveUser(userDTO.getMemberId());
            if(!retrievedUser.isPresent()){
                return ResponseEntity.badRequest().body("User with member ID "+memberId+" is not signed up.");
            }
            String username = userDTO.getUsername();
            if(username.length()<5 || !EverLinkUtils.isAlphanumeric(username) ){
                return ResponseEntity.badRequest().body("Invalid username or password.");
            }
            String password = userDTO.getPassword();
            if(password.length()<5 || !EverLinkUtils.isAlphanumeric(password)){
                return ResponseEntity.badRequest().body("Invalid password or password.");
            }
            List<String> memberIdList = everLinkService.getMembers().stream()
                    .map(m -> m.getMemberId())
                    .collect(Collectors.toList());
            MemberDTO retrievedMember = everLinkService.retrieveMember(userDTO.getMemberId());
            User user = retrievedUser.get();
            if(!userDTO.getUsername().equalsIgnoreCase(user.getUsername())
                    || !memberIdList.contains(memberId)
                    || !retrievedMember.getEmail().equalsIgnoreCase(userDTO.getEmail())){
                return ResponseEntity.badRequest().body("User is not verified to change password.");
            }
            userDTO = everLinkService.changePassword(userDTO, user);
            return ResponseEntity.ok(userDTO);
        }catch (Exception e){
            return ResponseEntity.internalServerError().body("An unexpected error during password change occurred.");
        }
    }

    @GetMapping("/health-check")
    public String  healthCheck(){
        return "APPISUPNOW";
    }
}
