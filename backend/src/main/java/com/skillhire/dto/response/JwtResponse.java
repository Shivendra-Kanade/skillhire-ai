package com.skillhire.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtResponse {
    private String token;
    private Long id;
    private String email;
    private String fullName;
    private String role;
    private boolean emailNotificationsEnabled;
}
