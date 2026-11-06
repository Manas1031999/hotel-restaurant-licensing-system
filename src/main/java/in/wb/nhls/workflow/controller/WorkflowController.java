package in.wb.nhls.workflow.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.wb.nhls.business.dto.ApiResponse;
import in.wb.nhls.workflow.dto.TransitionRequest;
import in.wb.nhls.workflow.dto.TransitionResponse;
import in.wb.nhls.workflow.service.WorkflowService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/workflow/transitions")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransitionResponse>>> list() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Workflow-এর নিয়ম", workflowService.listAll(), null));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TransitionResponse>> add(@Valid @RequestBody TransitionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                true, "নতুন নিয়ম যোগ হয়েছে", workflowService.add(request), null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<TransitionResponse>> disable(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "নিয়মটি বন্ধ করা হয়েছে", workflowService.disable(id), null));
    }
}
