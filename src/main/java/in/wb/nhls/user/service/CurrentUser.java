package in.wb.nhls.user.service;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import in.wb.nhls.user.entity.AppUser;
import in.wb.nhls.user.entity.Role;

public final class CurrentUser {

    private CurrentUser() {
    }

    public static String username() {
        Authentication auth = authenticated();
        return auth == null ? null : auth.getName();
    }

    public static boolean hasRole(Role role) {
        Authentication auth = authenticated();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role.name()));
    }

    public static boolean isStaff() {
        return hasRole(Role.REVIEWER) || hasRole(Role.APPROVER) || hasRole(Role.ADMIN);
    }

    // staff সব দেখতে পারে, applicant শুধু নিজের
    public static boolean canAccess(AppUser owner) {
        if (isStaff()) {
            return true;
        }
        String username = username();
        return username != null && owner != null && username.equals(owner.getUsername());
    }

    private static Authentication authenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return auth;
    }
}
