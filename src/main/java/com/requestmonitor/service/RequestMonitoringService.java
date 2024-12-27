package com.requestmonitor.service;

import com.requestmonitor.model.FailedRequest;
import com.requestmonitor.dao.FailedRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestMonitoringService {
    private final FailedRequestRepository failedRequestRepository;
    private final RequestCacheService cacheService;
    private final JavaMailSender mailSender;
    
    @Value("${alert.threshold:5}")
    private int alertThreshold;
    
    @Value("${alert.email:vivekvernekar21@gmail.com}")
    private String alertEmail;
    
    public void logFailedRequest(FailedRequest request) {
        // Cache the failed request
        cacheService.cacheFailedRequest(request.getIpAddress(), request);
        
        // Check if threshold is exceeded
        log.warn("Checking the failed count of the request Ip address in Redis Cache");
        int failedCount = cacheService.getFailedRequestCount(request.getIpAddress());
        
        log.warn("Failed count of " + request.getIpAddress() + " is : " + failedCount);
        if (failedCount >= alertThreshold) {
            // Get all failed requests from cache
            List<FailedRequest> failedRequests = cacheService.getFailedRequests(request.getIpAddress());
            
            // Save all requests to database
            failedRequestRepository.saveAll(failedRequests);
            
            // Send alert
            sendAlertEmail(request.getIpAddress(), failedRequests);
            
            // Clear cache for this IP
            cacheService.clearFailedRequests(request.getIpAddress());
        }
    }
    
    private void sendAlertEmail(String ipAddress, List<FailedRequest> failedRequests) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(alertEmail);
        message.setSubject("High Failed Request Alert");
        
        StringBuilder emailText = new StringBuilder();
        emailText.append(String.format(
            "IP Address %s has %d failed requests in the last 10 minutes.\n",
            ipAddress,
            failedRequests.size()
        ));
        emailText.append("\nDetails of failed requests:\n\n");
        
        for (FailedRequest failedRequest : failedRequests) {
            emailText.append(String.format(
                "Timestamp: %s, Error: %s\n",
                failedRequest.getTimestamp(),
                failedRequest.getFailureReason()
            ));
        }
        
        emailText.append("\nPlease investigate this issue.");
        message.setText(emailText.toString());
        
        mailSender.send(message);
    }
    
    public List<FailedRequest> getFailedRequestMetrics() {
        return failedRequestRepository.findAll();
    }
}