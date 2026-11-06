package in.wb.nhls.workflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import in.wb.nhls.business.entity.BusinessType;
import in.wb.nhls.license.LicenseApplicationStatus;
import in.wb.nhls.workflow.entity.WorkflowTransition;

public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransition, Long> {

    List<WorkflowTransition> findByBusinessTypeAndFromStatusAndActiveTrue(
            BusinessType businessType, LicenseApplicationStatus fromStatus);

    List<WorkflowTransition> findByBusinessTypeIsNullAndFromStatusAndActiveTrue(
            LicenseApplicationStatus fromStatus);

    List<WorkflowTransition> findAllByOrderByBusinessTypeAscFromStatusAscIdAsc();
}
