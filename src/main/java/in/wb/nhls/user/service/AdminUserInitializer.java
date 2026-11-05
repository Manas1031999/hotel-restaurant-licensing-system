package in.wb.nhls.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import in.wb.nhls.user.entity.Role;
import in.wb.nhls.user.repository.AppUserRepository;

// প্রথম চালুতে একটা ADMIN না থাকলে কেউ অন্য staff বানাতে পারবে না
@Component
public class AdminUserInitializer implements ApplicationRunner {

    static final String DEFAULT_PASSWORD = "Admin@12345";

    private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);

    private final AppUserRepository userRepository;
    private final AuthService authService;
    private final String username;
    private final String password;

    public AdminUserInitializer(AppUserRepository userRepository, AuthService authService,
            @Value("${nhls.admin.username}") String username,
            @Value("${nhls.admin.password}") String password) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.username = username;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByUsername(AuthService.normalize(username))) {
            return;
        }

        authService.createUser(username, password, "System Admin", Role.ADMIN);
        log.info("Admin user তৈরি হয়েছে: {}", username);

        if (DEFAULT_PASSWORD.equals(password)) {
            log.warn("Admin default password ব্যবহার হচ্ছে। Production-এ ADMIN_PASSWORD env variable দিন।");
        }
    }
}
