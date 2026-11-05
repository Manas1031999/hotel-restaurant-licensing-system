package in.wb.nhls.user.dto;

import in.wb.nhls.user.entity.Role;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        String username,
        Role role) {
}
