package com.sdcodebase.assignment.analytics.resource;

import com.sdcodebase.assignment.analytics.application.AnalyticsService;
import com.sdcodebase.assignment.analytics.application.dto.ActivityStatsResponse;
import com.sdcodebase.assignment.auth.domain.AuthenticatedUser;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private static final MediaType TEXT_CSV_UTF8 = new MediaType("text", "csv", StandardCharsets.UTF_8);

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * 지난 24시간 동안의 가입/로그인/대화 생성 수를 반환한다. 관리자만 호출 가능.
     */
    @GetMapping("/activity")
    public ActivityStatsResponse activityStats(@AuthenticationPrincipal AuthenticatedUser user) {
        return analyticsService.activityStats(user.isAdmin());
    }

    /**
     * 지난 24시간 동안 생성된 모든 대화를 CSV 보고서로 반환한다. 관리자만 호출 가능.
     */
    @GetMapping("/report")
    public ResponseEntity<String> chatReport(@AuthenticationPrincipal AuthenticatedUser user) {
        String csv = analyticsService.generateChatReportCsv(user.isAdmin());
        return ResponseEntity.ok()
                .contentType(TEXT_CSV_UTF8)
                .header("Content-Disposition", "attachment; filename=\"chat-report.csv\"")
                .body(csv);
    }

}
