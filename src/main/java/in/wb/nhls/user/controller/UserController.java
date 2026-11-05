package in.wb.nhls.user.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.wb.nhls.business.dto.ApiResponse;
import in.wb.nhls.business.dto.PaginationResponse;
import in.wb.nhls.user.dto.CreateUserRequest;
import in.wb.nhls.user.dto.UserResponse;
import in.wb.nhls.user.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                true, "User তৈরি হয়েছে", userService.createUser(request), null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<UserResponse> users = userService.listUsers(page, size);

        PaginationResponse pagination = new PaginationResponse(
                users.getNumber(), users.getSize(), users.getTotalElements(), users.getTotalPages());

        return ResponseEntity.ok(new ApiResponse<>(
                true, "User-এর তালিকা", users.getContent(), pagination));
    }
}
