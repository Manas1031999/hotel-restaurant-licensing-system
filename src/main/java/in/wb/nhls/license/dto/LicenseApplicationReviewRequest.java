package in.wb.nhls.license.dto;

import in.wb.nhls.license.LicenseApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LicenseApplicationReviewRequest(

        @NotNull(message = "আবেদনের status দিতে হবে") LicenseApplicationStatus applicationStatus,

        @Size(max = 1000, message = "Remarks 1000 character-এর বেশি হতে পারবে না") String remarks

) {
}
