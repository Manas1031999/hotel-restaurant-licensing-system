package in.wb.nhls;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.jayway.jsonpath.JsonPath;

import in.wb.nhls.notification.entity.NotificationOutbox;
import in.wb.nhls.notification.repository.NotificationOutboxRepository;
import in.wb.nhls.notification.template.NotificationTemplate;
import in.wb.nhls.user.entity.Role;
import in.wb.nhls.user.service.AuthService;

// আসল Postgres (Testcontainers) + পুরো Spring context + security; শুধু JWT তৈরিটা নকল
@SpringBootTest(properties = "nhls.notification.dispatcher.enabled=false")
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class LicenseWorkflowIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AuthService authService;

    @Autowired
    private NotificationOutboxRepository outboxRepository;

    private MockMvc mvc;
    private String applicant;
    private String otherApplicant;
    private String reviewer;
    private String approver;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

        String id = UUID.randomUUID().toString().substring(0, 8);
        applicant = createUser("app_" + id, Role.APPLICANT);
        otherApplicant = createUser("other_" + id, Role.APPLICANT);
        reviewer = createUser("rev_" + id, Role.REVIEWER);
        approver = createUser("apr_" + id, Role.APPROVER);
    }

    @Test
    void fullReviewFlowRecordsHistoryAndNotifications() throws Exception {
        String email = "owner_" + applicant + "@example.com";
        String appNo = submitApplication(applicant, "RESTAURANT", email);

        review(appNo, reviewer, Role.REVIEWER, "UNDER_REVIEW").andExpect(status().isOk());
        // reviewer চূড়ান্ত সিদ্ধান্ত দিতে পারে না
        review(appNo, reviewer, Role.REVIEWER, "APPROVED").andExpect(status().isForbidden());
        review(appNo, approver, Role.APPROVER, "APPROVED").andExpect(status().isOk());

        String history = mvc.perform(get("/api/v1/license-applications/{n}/status-history", appNo)
                        .with(as(applicant, Role.APPLICANT)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        List<String> statuses = JsonPath.read(history, "$[*].newStatus");
        List<String> changedBy = JsonPath.read(history, "$[*].changedBy");
        assertThat(statuses).containsExactly("PENDING", "UNDER_REVIEW", "APPROVED");
        assertThat(changedBy).containsExactly(applicant, reviewer, approver);

        // ব্যর্থ চেষ্টার (403) জন্য কোনো notification তৈরি হয়নি
        List<NotificationTemplate> queued = outboxRepository.findAll().stream()
                .filter(n -> n.getRecipient().equals(email))
                .map(NotificationOutbox::getTemplate)
                .toList();
        assertThat(queued).containsExactly(
                NotificationTemplate.APPLICATION_SUBMITTED,
                NotificationTemplate.APPLICATION_STATUS_CHANGED,
                NotificationTemplate.APPLICATION_STATUS_CHANGED);
    }

    @Test
    void skippingTheReviewStepIsRejected() throws Exception {
        String appNo = submitApplication(applicant, "HOTEL", "skip_" + applicant + "@example.com");

        review(appNo, approver, Role.APPROVER, "APPROVED")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("UNDER_REVIEW")));
    }

    @Test
    void applicantCannotSeeAnotherApplicantsRecords() throws Exception {
        String appNo = submitApplication(applicant, "LODGE", "own_" + applicant + "@example.com");

        mvc.perform(get("/api/v1/license-applications/{n}", appNo).with(as(otherApplicant, Role.APPLICANT)))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/businesses").with(as(otherApplicant, Role.APPLICANT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagination.totalElements").value(0));

        // staff সবারটা দেখতে পায়
        mvc.perform(get("/api/v1/license-applications/{n}", appNo).with(as(reviewer, Role.REVIEWER)))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpointsNeedAToken() throws Exception {
        mvc.perform(get("/api/v1/businesses")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/audit-logs").with(as(applicant, Role.APPLICANT))).andExpect(status().isForbidden());
    }

    // ---------- helpers ----------

    private String createUser(String username, Role role) {
        authService.createUser(username, "Passw0rd!", username, role);
        return username;
    }

    private static RequestPostProcessor as(String username, Role role) {
        return jwt().jwt(j -> j.subject(username)).authorities(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    private String submitApplication(String owner, String businessType, String email) throws Exception {
        String mobile = "9" + ThreadLocalRandom.current().nextLong(100_000_000L, 1_000_000_000L);
        String business = """
                {"businessName": "Test %s", "businessType": "%s", "ownerName": "Owner",
                 "mobileNumber": "%s", "email": "%s", "district": "Kolkata", "address": "Street"}
                """.formatted(owner, businessType, mobile, email);

        String created = mvc.perform(post("/api/v1/businesses").with(as(owner, Role.APPLICANT))
                        .contentType(MediaType.APPLICATION_JSON).content(business))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        Number businessId = JsonPath.read(created, "$.businessId");

        String submitted = mvc.perform(post("/api/v1/license-applications").with(as(owner, Role.APPLICANT))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"businessId\": " + businessId + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return JsonPath.read(submitted, "$.applicationNumber");
    }

    private org.springframework.test.web.servlet.ResultActions review(
            String appNo, String user, Role role, String newStatus) throws Exception {
        return mvc.perform(put("/api/v1/license-applications/{n}/review", appNo).with(as(user, role))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"applicationStatus\": \"" + newStatus + "\", \"remarks\": \"test\"}"));
    }
}
