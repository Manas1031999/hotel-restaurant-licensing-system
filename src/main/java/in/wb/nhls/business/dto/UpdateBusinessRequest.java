
package in.wb.nhls.business.dto;

import in.wb.nhls.business.entity.BusinessType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateBusinessRequest(

        @NotBlank(message = "ব্যবসার নাম দিতে হবে") 
        @Size(max = 150, message = "ব্যবসার নাম ১৫০ অক্ষরের মধ্যে হতে হবে") 
        String businessName,

        @NotNull(message = "ব্যবসার ধরন নির্বাচন করতে হবে") 
        BusinessType businessType,

        @NotBlank(message = "মালিকের নাম দিতে হবে") 
        String ownerName,

        @NotBlank(message = "মোবাইল নম্বর দিতে হবে") 
        @Pattern(regexp = "^[6-9][0-9]{9}$", message = "সঠিক ১০ সংখ্যার ভারতীয় মোবাইল নম্বর দিতে হবে") 
        String mobileNumber,

        @NotBlank(message = "ইমেল দিতে হবে") 
        @Email(message = "সঠিক ইমেল ঠিকানা দিতে হবে") 
        String email,

        @NotBlank(message = "জেলা নির্বাচন করতে হবে") 
        String district,

        @NotBlank(message = "ব্যবসার ঠিকানা দিতে হবে") 
        String address

) {
}
