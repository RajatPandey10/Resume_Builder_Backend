package in.RajatPandey.resumebuilderapi.service;

import in.RajatPandey.resumebuilderapi.Documents.User;
import in.RajatPandey.resumebuilderapi.dto.AuthResponse;
import in.RajatPandey.resumebuilderapi.dto.LoginRequest;
import in.RajatPandey.resumebuilderapi.dto.RegisterRequest;
import in.RajatPandey.resumebuilderapi.exceptions.ResourceExistsException;
import in.RajatPandey.resumebuilderapi.repository.UserRepository;
import in.RajatPandey.resumebuilderapi.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.base.url:http://localhost:8080}")
    private String appBaseUrl;



    public AuthResponse register(RegisterRequest request){
        log.info("Inside AuthService: register() {} ",request);
        if(userRepository.existsByEmail(request.getEmail())){
            throw new ResourceExistsException("User already exists with this email");
        }

        User newUser = toDocument(request);
        userRepository.save(newUser);
        sendVerificationEmail(newUser);
       return toResponse(newUser);

    }

    private void sendVerificationEmail(User newUser){
        log.info("Inside AuthService - sendVerificationEmail(): {}",newUser);
        try{
            String link = appBaseUrl + "/api/auth/verify-email?token=" + newUser.getVerificationToken();

            String html = "<div style=\"font-family: 'Arial', sans-serif; background-color: #f7f7f7; padding: 20px;\">\n" +
                    "    <div style=\"max-width: 600px; margin: auto; background-color: #ffffff; padding: 30px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);\">\n" +
                    "        <h2 style=\"color: #333333; text-align: center; font-size: 28px;\">Welcome to Our Platform, {{name}} 😊</h2>\n" +
                    "        <p style=\"font-size: 16px; color: #555555; line-height: 1.5; text-align: center;\">We’re excited to have you on board! Please confirm your email address to activate your account and get started. It only takes a moment.</p>\n" +
                    "        <p style=\"text-align: center;\">\n" +
                    "            <a href=\"{{link}}\" style=\"display: inline-block; padding: 15px 30px; background-color: #6366f1; color: #ffffff; text-decoration: none; font-size: 18px; border-radius: 6px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); transition: background-color 0.3s;\">Verify Email</a>\n" +
                    "        </p>\n" +
                    "        <p style=\"font-size: 14px; color: #888888; text-align: center; margin-top: 20px;\">Or copy this link into your browser: <br> <a href=\"{{link}}\" style=\"color: #6366f1; text-decoration: none;\">{{link}}</a></p>\n" +
                    "        <p style=\"font-size: 14px; color: #888888; text-align: center; margin-top: 10px;\">This link will expire in 24 hours.</p>\n" +
                    "        <p style=\"font-size: 14px; color: #888888; text-align: center; margin-top: 40px;\">If you did not create an account with us, please ignore this email.</p>\n" +
                    "    </div>\n" +
                    "</div>\n";

            // Replace placeholders
            html =  html.replace("{{name}}", newUser.getName());
            html = html.replace("{{link}}", link);


            emailService.sendHtmlEmail(newUser.getEmail(),"Verify your email",html);
        }catch (Exception e){
            log.error("Exception occurs at sendVerificationEmail(): {}",e.getMessage());
            throw new RuntimeException("Failed to send verification email: "+e.getMessage());
        }
    }

    private AuthResponse toResponse(User newUser){
        return AuthResponse.builder()
                .id(newUser.getId())
                .name(newUser.getName())
                .email(newUser.getEmail())
                .profileImageUrl(newUser.getProfileImageUrl())
                .emailVerified(newUser.isEmailVerified())
                .subscriptionPlan(newUser.getSubscriptionPlan())
                .createdAt(newUser.getCreatedAt())
                .updatedAt(newUser.getUpdatedAt())

                .build();
    }

    private  User toDocument(RegisterRequest request){
        return User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .profileImageUrl(request.getProfileImageUrl())
                .subscriptionPlan("Basic")
                .emailVerified(false)
                .verificationToken(UUID.randomUUID().toString())
                .verificationExpires(LocalDateTime.now().plusHours(24))
                .build();
    }


    public boolean verifyEmail(String token) {
        log.info("Inside AuthService - verifyEmail(): {}", token);

        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired verification token"));

        // Check expiration
        if (user.getVerificationExpires() != null &&
                user.getVerificationExpires().isBefore(LocalDateTime.now())) {

            throw new RuntimeException("Verification token has expired. Please request a new one.");
        }

        // Update user
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationExpires(null);
        userRepository.save(user);

        return true;
    }



    public AuthResponse login(LoginRequest request){
        User existingUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new UsernameNotFoundException("Invalid email or password"));

        if(!passwordEncoder.matches(request.getPassword(), existingUser.getPassword())){
            throw new UsernameNotFoundException("Invalid email or password");
        }
        if(!existingUser.isEmailVerified()){
            throw new RuntimeException("Please verify your email before logging in.");
        }

        String token = jwtUtil.generateToken(existingUser.getId());

        AuthResponse response = toResponse(existingUser);
        response.setToken(token);
        return response;
    }

    public void resendVerification(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new RuntimeException("User not found"));

        if(user.isEmailVerified()){
            throw new RuntimeException("Email is already verified");
        }

        user.setVerificationToken(UUID.randomUUID().toString());;
        user.setVerificationExpires(LocalDate.now().atStartOfDay().plusHours(24));
        userRepository.save(user);

        sendVerificationEmail(user);
    }

    public AuthResponse getProfile(Object principalObject) {
        User existingUser = (User)principalObject;
        return toResponse(existingUser);
    }
}
