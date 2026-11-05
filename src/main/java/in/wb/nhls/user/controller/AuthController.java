package in.wb.nhls.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.wb.nhls.business.dto.ApiResponse;
import in.wb.nhls.user.dto.LoginRequest;
import in.wb.nhls.user.dto.RegisterRequest;
import in.wb.nhls.user.dto.TokenResponse;
import in.wb.nhls.user.dto.UserResponse;
import in.wb.nhls.user.service.AuthService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                true, "নিবন্ধন সফল হয়েছে, এবার login করুন", authService.register(request), null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(new ApiResponse<>(
                true, "Login সফল হয়েছে", authService.login(request), null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me() {
        return ResponseEntity.ok(new ApiResponse<>(
                true, "বর্তমান user-এর তথ্য", authService.me(), null));
    }
}
