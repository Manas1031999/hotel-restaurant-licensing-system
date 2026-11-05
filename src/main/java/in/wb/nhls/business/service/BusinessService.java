package in.wb.nhls.business.service;

import in.wb.nhls.audit.annotation.Auditable;
import in.wb.nhls.business.dto.BusinessRequest;
import in.wb.nhls.business.dto.BusinessResponse;
import in.wb.nhls.business.dto.UpdateBusinessRequest;
import in.wb.nhls.business.entity.Business;
import in.wb.nhls.business.repository.BusinessRepository;
import in.wb.nhls.user.entity.AppUser;
import in.wb.nhls.user.repository.AppUserRepository;
import in.wb.nhls.user.service.CurrentUser;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BusinessService {

    private static final String NOT_FOUND = "এই ব্যবসার কোনো তথ্য পাওয়া যায়নি";

    private final BusinessRepository businessRepository;
    private final AppUserRepository userRepository;

    public BusinessService(BusinessRepository businessRepository, AppUserRepository userRepository) {
        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
    }

    @Auditable(action = "BUSINESS_REGISTER", entityType = "BUSINESS",
            entityId = "#result?.id",
            detail = "'name: ' + #request.businessName() + ', type: ' + #request.businessType()")
    @Transactional
    public Business registerBusiness(BusinessRequest request) {

        if (businessRepository.existsByMobileNumber(
                request.mobileNumber())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "এই মোবাইল নম্বর দিয়ে ইতিমধ্যে একটি ব্যবসা নিবন্ধিত হয়েছে");
        }

        if (businessRepository.existsByEmail(
                request.email())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "এই ইমেল ঠিকানা দিয়ে ইতিমধ্যে একটি ব্যবসা নিবন্ধিত হয়েছে");
        }

        Business business = new Business();

        business.setBusinessName(request.businessName());
        business.setBusinessType(request.businessType());
        business.setOwnerName(request.ownerName());
        business.setMobileNumber(request.mobileNumber());
        business.setEmail(request.email());
        business.setDistrict(request.district());
        business.setAddress(request.address());
        business.setOwner(currentUser());

        return businessRepository.save(business);
    }

    @Transactional(readOnly = true)
    public Page<BusinessResponse> getAllBusinesses(Pageable pageable) {

        Page<Business> businesses = CurrentUser.isStaff()
                ? businessRepository.findByActiveTrue(pageable)
                : businessRepository.findByOwnerUsernameAndActiveTrue(CurrentUser.username(), pageable);

        return businesses.map(BusinessResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public BusinessResponse getBusinessById(Long id) {

        return BusinessResponse.fromEntity(findAccessible(id));
    }

    @Auditable(action = "BUSINESS_UPDATE", entityType = "BUSINESS", entityId = "#id")
    @Transactional
    public BusinessResponse updateBusiness(
            Long id,
            UpdateBusinessRequest request) {

        Business business = findAccessible(id);

        business.setBusinessName(request.businessName());
        business.setBusinessType(request.businessType());
        business.setOwnerName(request.ownerName());
        business.setMobileNumber(request.mobileNumber());
        business.setEmail(request.email());
        business.setDistrict(request.district());
        business.setAddress(request.address());

        Business updatedBusiness = businessRepository.save(business);

        return BusinessResponse.fromEntity(updatedBusiness);
    }

    public List<Business> getActiveBusinesses() {
        return businessRepository.findByActiveTrue();
    }

    @Auditable(action = "BUSINESS_DEACTIVATE", entityType = "BUSINESS", entityId = "#id")
    @Transactional
    public void deactivateBusiness(Long id) {

        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "ব্যবসাটি খুঁজে পাওয়া যায়নি"));

        business.setActive(false);

        businessRepository.save(business);
    }

    // অন্যের ব্যবসা হলে 404, যাতে সেটা আছে কিনা বোঝা না যায়
    private Business findAccessible(Long id) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, NOT_FOUND));

        if (!CurrentUser.canAccess(business.getOwner())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, NOT_FOUND);
        }
        return business;
    }

    private AppUser currentUser() {
        return userRepository.findByUsername(CurrentUser.username())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "অনুগ্রহ করে লগইন করুন"));
    }
}
