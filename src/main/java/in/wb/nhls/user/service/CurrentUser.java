package in.wb.nhls.user.service;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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

    private static Authentication authenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return auth;
    }
}
