
package in.wb.nhls.license.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LicenseExpiryScheduler {

    private final LicenseService licenseService;

    public LicenseExpiryScheduler(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void checkLicenseExpiry() {

        int expiredCount = licenseService.expireLicenses();

        System.out.println(
                "License expiry check completed. Expired licenses: "
                        + expiredCount);
    }
}
