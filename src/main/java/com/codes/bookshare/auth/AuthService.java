package com.codes.bookshare.auth;

import com.codes.bookshare.email.EmailService;
import com.codes.bookshare.email.EmailTemplate;
import com.codes.bookshare.role.RoleRepository;
import com.codes.bookshare.security.JwtService;
import com.codes.bookshare.user.ActivationToken;
import com.codes.bookshare.user.ActivationTokenRepository;
import com.codes.bookshare.user.User;
import com.codes.bookshare.user.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

   private final UserRepository userRepository;
   private final PasswordEncoder passwordEncoder;
   private final RoleRepository roleRepository;
   private final ActivationTokenRepository activationTokenRepository;
   private final EmailService emailService;
   private final JwtService jwtService;
   private final AuthenticationManager authenticationManager;

    public void register(RegistrationRequest registrationRequest) throws MessagingException {
        var userRole = roleRepository.findByName("USER").orElseThrow(
                () -> new IllegalStateException("Role USER not found")
        );

        var user = User.builder()
                .roles(List.of(userRole))
                .firstName(registrationRequest.getFirstName())
                .lastName(registrationRequest.getLastName())
                .email(registrationRequest.getEmail())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .accountLocked(false)
                .enabled(false)
                .build();

        userRepository.save(user);
        sendValidationEmail(user);

    }

    private void sendValidationEmail(User user) throws MessagingException {
        String code = generateActivationToken(user);
        emailService.send(
                user.getEmail(),
                "Account Activation",
                user.getName(),
                code,
                EmailTemplate.ACTIVATE_ACCOUNT
        );
    }

    private String generateActivationToken (User user) {
        String characters = "0123456789";
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < 6; i++) {
            int randomIndex = random.nextInt(characters.length());
            sb.append(characters.charAt(randomIndex));
        }

        String code = sb.toString();

        var activationToken = ActivationToken.builder()
                .user(user)
                .token(code)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
       activationTokenRepository.save(activationToken);
       return code;
    }

    public AuthResponse authenticate(AuthRequest authRequest) {
       var auth = authenticationManager.authenticate(
               new UsernamePasswordAuthenticationToken(
                       authRequest.getEmail(),
                       authRequest.getPassword()
               )
       );
       var user = (User) auth.getPrincipal();
//       var claims = new HashMap<String, Object>();
//       claims.put("name", user.getName());
//       var jwt = jwtService.generateToken(claims, user);
        System.out.println(user.getName());
        var jwt = jwtService.generateToken(user);
       return AuthResponse.builder().token(jwt).build();

    }

    public void activateAccount(String token) throws MessagingException {
        var savedToken = activationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid activation token"));

        if(LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
            sendValidationEmail(savedToken.getUser());
            throw new RuntimeException("Expired activation token");
        }

        var user = userRepository.findById(savedToken.getUser().getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);
        savedToken.setValidatedAt(LocalDateTime.now());
        activationTokenRepository.save(savedToken);
    }

}
