package in.wb.nhls.business.dto;

import in.wb.nhls.business.entity.BusinessType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BusinessRequest(

        @NotBlank(message = "ব্যবসার নাম দিতে হবে") @Size(max = 150, message = "ব্যবসার নাম ১৫০ অক্ষরের বেশি হতে পারবে না") String businessName,

        @NotNull(message = "ব্যবসার ধরন নির্বাচন করুন") BusinessType businessType,

        @NotBlank(message = "মালিকের নাম দিতে হবে") @Size(max = 100, message = "মালিকের নাম ১০০ অক্ষরের বেশি হতে পারবে না") String ownerName,

        @NotBlank(message = "মোবাইল নম্বর দিতে হবে") @Pattern(regexp = "^[6-9]\\d{9}$", message = "সঠিক ১০ ডিজিটের ভারতীয় মোবাইল নম্বর দিতে হবে") String mobileNumber,

        @NotBlank(message = "ইমেল ঠিকানা দিতে হবে") @Email(message = "সঠিক ইমেল ঠিকানা দিতে হবে") String email,

        @NotBlank(message = "জেলা নির্বাচন করুন") String district,

        @NotBlank(message = "ব্যবসার ঠিকানা দিতে হবে") String address

) {
}