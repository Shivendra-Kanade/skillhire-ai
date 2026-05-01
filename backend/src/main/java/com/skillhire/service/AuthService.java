package com.skillhire.service;

import com.skillhire.dto.request.LoginRequest;
import com.skillhire.dto.request.RegisterRequest;
import com.skillhire.dto.response.JwtResponse;
import com.skillhire.entity.User;
import com.skillhire.exception.DuplicateResourceException;
import com.skillhire.repository.UserRepository;
import com.skillhire.security.jwt.JwtUtils;
import com.skillhire.security.service.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final EmailNotificationService emailNotificationService;

    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(User.Role.valueOf(request.getRole()))
                .emailNotificationsEnabled(true)
                .build();

        User saved = userRepository.save(user);
        log.info("New user registered: {} ({})", saved.getEmail(), saved.getRole());

        // Send welcome email asynchronously
        emailNotificationService.sendWelcomeEmail(saved);

        return saved;
    }

    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return JwtResponse.builder()
                .token(jwt)
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .emailNotificationsEnabled(user.isEmailNotificationsEnabled())
                .build();
    }

    public User updateNotificationPreference(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEmailNotificationsEnabled(enabled);
        return userRepository.save(user);
    }
}
