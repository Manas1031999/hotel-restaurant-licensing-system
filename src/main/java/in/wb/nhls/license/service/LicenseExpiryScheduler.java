
package in.wb.nhls.license.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LicenseExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(LicenseExpiryScheduler.class);

    private final LicenseService licenseService;

    public LicenseExpiryScheduler(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void checkLicenseExpiry() {

        int expiredCount = licenseService.expireLicenses();

        // System.out নয়, logger: তাহলে Loki-তেও যায়
        log.info("License expiry check completed. Expired licenses: {}", expiredCount);
    }
}
