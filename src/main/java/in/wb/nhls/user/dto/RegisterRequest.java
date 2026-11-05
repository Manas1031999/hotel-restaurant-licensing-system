package in.wb.nhls.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Username দিতে হবে")
        @Size(min = 4, max = 50, message = "Username ৪ থেকে ৫০ অক্ষরের মধ্যে হতে হবে")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username-এ শুধু অক্ষর, সংখ্যা, . _ - চলবে")
        String username,

        @NotBlank(message = "Password দিতে হবে")
        @Size(min = 8, max = 100, message = "Password কমপক্ষে ৮ অক্ষরের হতে হবে")
        String password,

        @NotBlank(message = "পুরো নাম দিতে হবে")
        @Size(max = 100, message = "নাম ১০০ অক্ষরের বেশি হতে পারবে না")
        String fullName) {
}
