package com.ecommerce.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private Long userId;
    private String email;
    private String role;

    public static AuthResponse of(Long userId, String email, String role) {
        return AuthResponse.builder()
                .userId(userId)
                .email(email)
                .role(role)
                .build();
    }
}
