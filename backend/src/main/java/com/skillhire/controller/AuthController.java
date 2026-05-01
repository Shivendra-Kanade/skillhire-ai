package com.skillhire.controller;

import com.skillhire.dto.request.LoginRequest;
import com.skillhire.dto.request.RegisterRequest;
import com.skillhire.dto.response.JwtResponse;
import com.skillhire.entity.User;
import com.skillhire.security.service.UserDetailsImpl;
import com.skillhire.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return ResponseEntity.ok(Map.of(
                "message", "Registration successful! Please login.",
                "email", user.getEmail()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        JwtResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/notifications")
    public ResponseEntity<?> updateNotifications(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody Map<String, Boolean> body) {
        boolean enabled = Boolean.TRUE.equals(body.get("emailNotificationsEnabled"));
        User updated = authService.updateNotificationPreference(userDetails.getId(), enabled);
        return ResponseEntity.ok(Map.of(
                "emailNotificationsEnabled", updated.isEmailNotificationsEnabled(),
                "message", enabled ? "Email notifications enabled" : "Email notifications disabled"
        ));
    }
}
