package in.wb.nhls.license.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LicenseApplicationRequest(

        @NotNull(message = "ব্যবসার ID দিতে হবে") @Positive(message = "ব্যবসার ID অবশ্যই positive number হতে হবে") Long businessId

) {
}
