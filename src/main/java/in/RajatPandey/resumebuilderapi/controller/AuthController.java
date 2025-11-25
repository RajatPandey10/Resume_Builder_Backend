package in.RajatPandey.resumebuilderapi.controller;

import in.RajatPandey.resumebuilderapi.Documents.User;
import in.RajatPandey.resumebuilderapi.dto.AuthResponse;
import in.RajatPandey.resumebuilderapi.dto.LoginRequest;
import in.RajatPandey.resumebuilderapi.dto.RegisterRequest;
import in.RajatPandey.resumebuilderapi.service.AuthService;
import in.RajatPandey.resumebuilderapi.service.FileUploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static in.RajatPandey.resumebuilderapi.utils.AppConstants.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(AUTH_CONTROLLER)
public class AuthController {

    private final AuthService authService;
    private final FileUploadService fileUploadService;

    @PostMapping(REGISTER)
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request){
        log.info("Register request received for email: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        log.info("User registered successfully with id: {}", response.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @GetMapping(VERIFY_EMAIL)
    public ResponseEntity<?> verifyEmail(@RequestParam String token){
        log.info("Email verification requested");
        authService.verifyEmail(token);
//        return ResponseEntity.status(HttpStatus.FOUND).location("/")
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message","Email Verified Successfully"));

    }

    @PostMapping(UPLOAD_PROFILE)
    public ResponseEntity<?> uploadImage(@RequestPart("image") MultipartFile file) throws IOException {
        log.info("Inside AuthController - uploadImage()");
        Map<String,String> response = fileUploadService.uploadSingleImage(file);
        return ResponseEntity.ok(response);
    }

    @PostMapping(LOGIN)
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request){
        log.info("Login attempt for the email: {}",request.getEmail());
        AuthResponse response = authService.login(request);
        log.info("Logging successful for userId: {}",response.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    public String testAuthValidationToken(){
        return "Token Validation is working";
    }

    @PostMapping(RESEND_VERIFICATION)
    public ResponseEntity<?> resendVerification(@RequestBody Map<String,String> body){
        log.info("Resend verification email request ");
        String email = body.get("email");

         if(Objects.isNull(email)){
             return ResponseEntity.badRequest().body(Map.of("message","Email is required"));
         }

         authService.resendVerification(email);

         return ResponseEntity.ok(Map.of("Success",true,"message","Verification email sent"));
    }

    @GetMapping(PROFILE)
    public ResponseEntity<?> getProfile(Authentication authentication){
        log.info("Fetching user profile");
        Object principalObject = authentication.getPrincipal();

        AuthResponse currentProfile = authService.getProfile(principalObject);

        return ResponseEntity.ok(currentProfile);

    }
}
