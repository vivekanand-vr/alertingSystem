package com.requestmonitor.controller;

import com.requestmonitor.dto.SubmitRequestDTO;
import com.requestmonitor.model.FailedRequest;
import com.requestmonitor.service.RequestMonitoringService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SubmitController {
    private final RequestMonitoringService monitoringService;
    
    // Hardcoded valid authorization token and required params
    private static final String VALID_AUTH_TOKEN = "Bearer FIXED_SECRET_TOKEN_2024";
    private static final List<String> REQUIRED_PARAMS = List.of("clientId", "version");
    
    @RequestMapping("/submit")
    public ResponseEntity<?> submitRequest(
        @RequestHeader(value = "Authorization", required = false) String authToken,
        @RequestHeader(value = "X-Custom-Header", required = false) String customHeader,
        @RequestParam Map<String, String> allParams,
        HttpServletRequest request,
        @RequestBody(required = false) SubmitRequestDTO requestDTO
    ) {
        // Check for allowed HTTP method
        if (!request.getMethod().equals(HttpMethod.POST.name())) {
            logFailedRequest(request, "Method Not Allowed: Only POST is supported");
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body("Error: Only POST method is allowed on this endpoint");
        }
        
        // Validate authorization token
        if (authToken == null || !authToken.equals(VALID_AUTH_TOKEN)) {
            logFailedRequest(request, "Invalid or Missing Authorization Token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Error: Invalid or missing authorization token");
        }
        
        // Validate custom header
        if (customHeader == null || customHeader.isEmpty()) {
            logFailedRequest(request, "Missing Custom Header");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error: Custom header is required");
        }
        
        // Validate required parameters
        for (String requiredParam : REQUIRED_PARAMS) {
            if (!allParams.containsKey(requiredParam)) {
                logFailedRequest(request, "Missing Required Parameter: " + requiredParam);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: Missing required parameter: " + requiredParam);
            }
        }
        
        // Validate request body
        if (requestDTO == null) {
            logFailedRequest(request, "Empty Request Body");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error: Request body is required and cannot be empty");
        }
        
        // If all validations pass, process the request
        return ResponseEntity.ok("Request submitted successfully");
    }
    
    @GetMapping("/metrics")
    public ResponseEntity<List<FailedRequest>> getFailedRequestMetrics() {
        return ResponseEntity.ok(monitoringService.getFailedRequestMetrics());
    }
    
    private void logFailedRequest(HttpServletRequest request, String reason) {
        FailedRequest failedRequest = new FailedRequest();
        failedRequest.setIpAddress(request.getRemoteAddr());
        failedRequest.setRequestPath(request.getRequestURI());
        failedRequest.setFailureReason(reason);
        failedRequest.setTimestamp(LocalDateTime.now());
        failedRequest.setRequestMethod(request.getMethod());
        
        monitoringService.logFailedRequest(failedRequest);
    }
}