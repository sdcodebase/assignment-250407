package com.sdcodebase.assignment.global;

import com.sdcodebase.assignment.analytics.domain.AnalyticsAccessDeniedException;
import com.sdcodebase.assignment.auth.domain.EmailAlreadyExistsException;
import com.sdcodebase.assignment.auth.domain.InvalidCredentialsException;
import com.sdcodebase.assignment.chat.domain.ChatNotFoundException;
import com.sdcodebase.assignment.chat.domain.ChatThreadAccessDeniedException;
import com.sdcodebase.assignment.chat.domain.ChatThreadNotFoundException;
import com.sdcodebase.assignment.feedback.domain.DuplicateFeedbackException;
import com.sdcodebase.assignment.feedback.domain.FeedbackAccessDeniedException;
import com.sdcodebase.assignment.feedback.domain.FeedbackNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ChatThreadNotFoundException.class, ChatNotFoundException.class, FeedbackNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler({ChatThreadAccessDeniedException.class, FeedbackAccessDeniedException.class, AnalyticsAccessDeniedException.class})
    public ResponseEntity<Map<String, String>> handleForbidden(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler({EmailAlreadyExistsException.class, DuplicateFeedbackException.class})
    public ResponseEntity<Map<String, String>> handleConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorized(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", ex.getMessage()));
    }
}
