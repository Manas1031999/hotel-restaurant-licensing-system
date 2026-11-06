package in.wb.nhls.workflow.service;

import static in.wb.nhls.license.LicenseApplicationStatus.APPROVED;
import static in.wb.nhls.license.LicenseApplicationStatus.PENDING;
import static in.wb.nhls.license.LicenseApplicationStatus.REJECTED;
import static in.wb.nhls.license.LicenseApplicationStatus.UNDER_REVIEW;
import static in.wb.nhls.user.entity.Role.APPROVER;
import static in.wb.nhls.user.entity.Role.REVIEWER;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import in.wb.nhls.workflow.entity.WorkflowTransition;
import in.wb.nhls.workflow.repository.WorkflowTransitionRepository;

// table খালি থাকলে আগের hardcoded নিয়মগুলোই default হিসেবে বসে
@Component
public class WorkflowSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(WorkflowSeeder.class);

    private final WorkflowTransitionRepository repository;

    public WorkflowSeeder(WorkflowTransitionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) {
            return;
        }

        repository.saveAll(List.of(
                new WorkflowTransition(null, PENDING, UNDER_REVIEW, REVIEWER),
                new WorkflowTransition(null, UNDER_REVIEW, APPROVED, APPROVER),
                new WorkflowTransition(null, UNDER_REVIEW, REJECTED, APPROVER)));

        log.info("Default workflow নিয়ম তৈরি হয়েছে");
    }
}
