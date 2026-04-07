package com.sdcodebase.assignment.feedback.resource;

import com.sdcodebase.assignment.auth.domain.AuthenticatedUser;
import com.sdcodebase.assignment.feedback.application.FeedbackService;
import com.sdcodebase.assignment.feedback.application.dto.CreateFeedbackRequest;
import com.sdcodebase.assignment.feedback.application.dto.FeedbackResponse;
import com.sdcodebase.assignment.feedback.application.dto.UpdateFeedbackStatusRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feedbacks")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    public ResponseEntity<FeedbackResponse> createFeedback(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateFeedbackRequest request
    ) {
        FeedbackResponse response = feedbackService.createFeedback(user.id(), user.isAdmin(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public Page<FeedbackResponse> listFeedbacks(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) Boolean positive,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return feedbackService.listFeedbacks(user.id(), user.isAdmin(), positive, pageable);
    }

    @PatchMapping("/{feedbackId}/status")
    public FeedbackResponse updateStatus(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long feedbackId,
            @Valid @RequestBody UpdateFeedbackStatusRequest request
    ) {
        return feedbackService.updateStatus(user.isAdmin(), feedbackId, request.status());
    }

}
