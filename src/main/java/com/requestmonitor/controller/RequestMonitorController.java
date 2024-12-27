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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RequestMonitorController {
    private final RequestMonitoringService monitoringService;
    
    private static final String VALID_AUTH_TOKEN = "Bearer FIXED_SECRET_TOKEN_2024";
    private static final List<String> REQUIRED_PARAMS = List.of("clientId", "version");
    private static final String VALID_BASE_PATH = "/api";
    private static final String VALID_ENDPOINT = "/submit";
    
    @RequestMapping("/**")
    public ResponseEntity<?> handleAllRequests(
        @RequestHeader(value = "Authorization", required = false) String authToken,
        @RequestHeader(value = "X-Custom-Header", required = false) String customHeader,
        @RequestParam Map<String, String> allParams,
        HttpServletRequest request,
        @RequestBody(required = false) SubmitRequestDTO requestDTO
    ) {
        // Validate URL path
        String requestPath = request.getRequestURI();
        if (!isValidPath(requestPath)) {
        	return handleError(request, 
                HttpStatus.NOT_FOUND, 
                "Invalid request path"
            );
        }
        
        // Check for allowed HTTP method
        if (!request.getMethod().equals(HttpMethod.POST.name())) {
            return handleError(request,
                HttpStatus.METHOD_NOT_ALLOWED,
                "Method Not Allowed: Only POST is supported"
            );
        }
        
        // Validate authorization token
        if (authToken == null || !authToken.equals(VALID_AUTH_TOKEN)) {
        	return handleError(request,
                HttpStatus.UNAUTHORIZED,
                "Invalid or Missing Authorization Token"
            );
        }
        
        // Validate custom header
        if (customHeader == null || customHeader.isEmpty()) {
            return handleError(request,
                HttpStatus.BAD_REQUEST,
                "Missing Custom Header"
            );
        }
        
        // Validate required parameters
        for (String requiredParam : REQUIRED_PARAMS) {
            if (!allParams.containsKey(requiredParam)) {
                return handleError(request,
                    HttpStatus.BAD_REQUEST,
                    "Missing Required Parameter: " + requiredParam
                );
            }
        }
        
        // Validate request body
        if (requestDTO == null) {
            return handleError(request,
                HttpStatus.BAD_REQUEST,
                "Request body is required and cannot be empty"
            );
        }
        
        // If all validations pass, process the request
        return ResponseEntity.ok("Request submitted successfully");
    }
    
    @GetMapping("/metrics")
    public ResponseEntity<List<FailedRequest>> getFailedRequestMetrics() {
        return ResponseEntity.ok(monitoringService.getFailedRequestMetrics());
    }
    
    private boolean isValidPath(String path) {
        return path.equals(VALID_BASE_PATH + VALID_ENDPOINT);
    }
    
    private ResponseEntity<?> handleError(HttpServletRequest request, HttpStatus status, String message) {
        monitoringService.logFailedRequest(createFailedRequest(request, message));
        return ResponseEntity.status(status).body("Error: " + message);
    }
    
    private FailedRequest createFailedRequest(HttpServletRequest request, String reason) {
        FailedRequest failedRequest = new FailedRequest();
        failedRequest.setIpAddress(request.getRemoteAddr());
        failedRequest.setRequestPath(request.getRequestURI());
        failedRequest.setFailureReason(reason);
        failedRequest.setTimestamp(java.time.LocalDateTime.now());
        failedRequest.setRequestMethod(request.getMethod());
        return failedRequest;
    }
}