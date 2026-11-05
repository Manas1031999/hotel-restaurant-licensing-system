package in.wb.nhls.user.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "Username দিতে হবে")
        String username,

        @NotBlank(message = "Password দিতে হবে")
        String password) {
}
