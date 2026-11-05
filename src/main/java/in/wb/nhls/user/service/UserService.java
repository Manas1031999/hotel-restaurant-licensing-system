package in.wb.nhls.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.wb.nhls.audit.annotation.Auditable;
import in.wb.nhls.user.dto.CreateUserRequest;
import in.wb.nhls.user.dto.UserResponse;
import in.wb.nhls.user.repository.AppUserRepository;

@Service
public class UserService {

    private final AppUserRepository userRepository;
    private final AuthService authService;

    public UserService(AppUserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    // ADMIN অন্য role-এর user (REVIEWER, APPROVER, ADMIN) বানায়
    @Auditable(action = "USER_CREATE", entityType = "USER",
            entityId = "#result?.username()", detail = "'role: ' + #request.role()")
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        return UserResponse.fromEntity(authService.createUser(
                request.username(), request.password(), request.fullName(), request.role()));
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(int page, int size) {
        return userRepository
                .findAll(PageRequest.of(page, Math.min(size, 100), Sort.by("id")))
                .map(UserResponse::fromEntity);
    }
}
