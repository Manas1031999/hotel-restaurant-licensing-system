package in.wb.nhls.workflow.service;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import in.wb.nhls.audit.annotation.Auditable;
import in.wb.nhls.business.entity.BusinessType;
import in.wb.nhls.license.LicenseApplicationStatus;
import in.wb.nhls.user.entity.Role;
import in.wb.nhls.user.service.CurrentUser;
import in.wb.nhls.workflow.dto.TransitionRequest;
import in.wb.nhls.workflow.dto.TransitionResponse;
import in.wb.nhls.workflow.entity.WorkflowTransition;
import in.wb.nhls.workflow.repository.WorkflowTransitionRepository;

@Service
public class WorkflowService {

    // review API-তে শুধু এরা ঢুকতে পারে, তাই নিয়মও শুধু এদের জন্য
    private static final Set<Role> REVIEW_ROLES = Set.of(Role.REVIEWER, Role.APPROVER);

    private final WorkflowTransitionRepository repository;

    public WorkflowService(WorkflowTransitionRepository repository) {
        this.repository = repository;
    }

    // ব্যবসার ধরনের নিজস্ব নিয়ম থাকলে সেটা, নাহলে default
    @Transactional(readOnly = true)
    public List<WorkflowTransition> rulesFrom(BusinessType type, LicenseApplicationStatus from) {
        List<WorkflowTransition> specific = repository.findByBusinessTypeAndFromStatusAndActiveTrue(type, from);
        return specific.isEmpty()
                ? repository.findByBusinessTypeIsNullAndFromStatusAndActiveTrue(from)
                : specific;
    }

    // ভুল ধাপ হলে 400, ভুল role হলে 403
    @Transactional(readOnly = true)
    public void checkTransition(BusinessType type, LicenseApplicationStatus from, LicenseApplicationStatus to) {
        List<WorkflowTransition> rules = rulesFrom(type, from);

        if (rules.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "এই আবেদনটি ইতিমধ্যে চূড়ান্তভাবে নিষ্পত্তি হয়েছে");
        }

        WorkflowTransition rule = rules.stream()
                .filter(r -> r.getToStatus() == to)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        from.name() + " থেকে " + to.name() + " করা যাবে না। অনুমোদিত: "
                                + rules.stream().map(r -> r.getToStatus().name()).toList()));

        if (!CurrentUser.hasRole(rule.getRequiredRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    to.name() + " করার অনুমতি শুধু " + rule.getRequiredRole().name() + "-এর আছে");
        }
    }

    // বর্তমান user এখন কোন কোন status দিতে পারবে
    @Transactional(readOnly = true)
    public List<LicenseApplicationStatus> availableActions(BusinessType type, LicenseApplicationStatus from) {
        return rulesFrom(type, from).stream()
                .filter(r -> CurrentUser.hasRole(r.getRequiredRole()))
                .map(WorkflowTransition::getToStatus)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransitionResponse> listAll() {
        return repository.findAllByOrderByBusinessTypeAscFromStatusAscIdAsc().stream()
                .map(TransitionResponse::fromEntity)
                .toList();
    }

    @Auditable(action = "WORKFLOW_RULE_ADD", entityType = "WORKFLOW_TRANSITION",
            entityId = "#result?.id()",
            detail = "#request.businessType() + ': ' + #request.fromStatus() + ' -> ' + #request.toStatus()"
                    + " + ' (' + #request.requiredRole() + ')'")
    @Transactional
    public TransitionResponse add(TransitionRequest request) {
        if (request.fromStatus() == request.toStatus()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromStatus আর toStatus একই হতে পারে না");
        }
        if (request.toStatus() == LicenseApplicationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PENDING-এ ফেরত পাঠানো যাবে না");
        }
        if (!REVIEW_ROLES.contains(request.requiredRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "requiredRole শুধু REVIEWER অথবা APPROVER হতে পারে");
        }

        boolean duplicate = (request.businessType() == null
                ? repository.findByBusinessTypeIsNullAndFromStatusAndActiveTrue(request.fromStatus())
                : repository.findByBusinessTypeAndFromStatusAndActiveTrue(request.businessType(), request.fromStatus()))
                .stream().anyMatch(r -> r.getToStatus() == request.toStatus());
        if (duplicate) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "এই নিয়ম ইতিমধ্যে আছে");
        }

        WorkflowTransition saved = repository.save(new WorkflowTransition(
                request.businessType(), request.fromStatus(), request.toStatus(), request.requiredRole()));
        return TransitionResponse.fromEntity(saved);
    }

    // মুছে ফেলা নয়, বন্ধ করা; ইতিহাস থাকে
    @Auditable(action = "WORKFLOW_RULE_DISABLE", entityType = "WORKFLOW_TRANSITION", entityId = "#id")
    @Transactional
    public TransitionResponse disable(Long id) {
        WorkflowTransition rule = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "নিয়মটি পাওয়া যায়নি"));
        rule.setActive(false);
        return TransitionResponse.fromEntity(rule);
    }
}
