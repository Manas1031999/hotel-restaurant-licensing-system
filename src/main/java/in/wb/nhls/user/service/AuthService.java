package in.wb.nhls.user.service;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import in.wb.nhls.audit.annotation.Auditable;
import in.wb.nhls.user.dto.LoginRequest;
import in.wb.nhls.user.dto.RegisterRequest;
import in.wb.nhls.user.dto.TokenResponse;
import in.wb.nhls.user.dto.UserResponse;
import in.wb.nhls.user.entity.AppUser;
import in.wb.nhls.user.entity.Role;
import in.wb.nhls.user.repository.AppUserRepository;

@Service
public class AuthService {

    private static final String BAD_CREDENTIALS = "ভুল username অথবা password";

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    // user না থাকলেও password যাচাইয়ে একই সময় লাগুক, যাতে username আছে কিনা বোঝা না যায়
    private final String dummyHash;

    public AuthService(AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.dummyHash = passwordEncoder.encode("dummy-password-for-timing");
    }

    // নিজে register করলে সবসময় APPLICANT
    @Auditable(action = "USER_REGISTER", entityType = "USER", entityId = "#result?.username()")
    @Transactional
    public UserResponse register(RegisterRequest request) {
        AppUser user = createUser(request.username(), request.password(),
                request.fullName(), Role.APPLICANT);
        return UserResponse.fromEntity(user);
    }

    @Auditable(action = "LOGIN", entityType = "USER", entityId = "#request.username()")
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        AppUser user = userRepository.findByUsername(normalize(request.username())).orElse(null);

        if (user == null) {
            passwordEncoder.matches(request.password(), dummyHash);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, BAD_CREDENTIALS);
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, BAD_CREDENTIALS);
        }

        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "এই account নিষ্ক্রিয় করা হয়েছে");
        }

        return new TokenResponse(
                tokenService.issue(user),
                "Bearer",
                tokenService.expirySeconds(),
                user.getUsername(),
                user.getRole());
    }

    @Transactional(readOnly = true)
    public UserResponse me() {
        String username = CurrentUser.username();
        return userRepository.findByUsername(username)
                .map(UserResponse::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User খুঁজে পাওয়া যায়নি"));
    }

    public AppUser createUser(String username, String rawPassword, String fullName, Role role) {
        String normalized = normalize(username);

        if (userRepository.existsByUsername(normalized)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "এই username ইতিমধ্যে নেওয়া হয়েছে");
        }

        AppUser user = new AppUser();
        user.setUsername(normalized);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName.trim());
        user.setRole(role);

        return userRepository.save(user);
    }

    public static String normalize(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }
}
