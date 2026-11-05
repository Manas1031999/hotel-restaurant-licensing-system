package in.wb.nhls.user.dto;

import java.time.LocalDateTime;

import in.wb.nhls.user.entity.AppUser;
import in.wb.nhls.user.entity.Role;

public record UserResponse(
        Long id,
        String username,
        String fullName,
        Role role,
        boolean active,
        LocalDateTime createdAt) {

    public static UserResponse fromEntity(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt());
    }
}
