package com.requestmonitor.service;

import com.requestmonitor.model.FailedRequest;
import com.requestmonitor.dao.FailedRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestMonitoringService {
    private final FailedRequestRepository failedRequestRepository;
    private final JavaMailSender mailSender;
    
    @Value("${alert.threshold:5}")
    private int alertThreshold;
    
    @Value("${alert.email:vivekvernekar21@gmail.com}")
    private String alertEmail;
    
    public void logFailedRequest(FailedRequest request) {
        failedRequestRepository.save(request);
        checkAndSendAlert(request.getIpAddress());
    }
    
    private void checkAndSendAlert(String ipAddress) {
        LocalDateTime now = LocalDateTime.now();
        List<FailedRequest> recentFailedRequests = failedRequestRepository
            .findByIpAddressAndTimestampBetween(
                ipAddress, 
                now.minusMinutes(10), 
                now
            );
        
        if (recentFailedRequests.size() >= alertThreshold) {
            sendAlertEmail(ipAddress, recentFailedRequests);
        }
    }
    
    private void sendAlertEmail(String ipAddress, List<FailedRequest> failedRequests) {
        // Construct the email message
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(alertEmail);
        message.setSubject("High Failed Request Alert");

        // Build detailed email text
        StringBuilder emailText = new StringBuilder();
        emailText.append(String.format(
            "IP Address %s has %d failed requests in the last 10 minutes.\n", 
            ipAddress, 
            failedRequests.size()
        ));
        emailText.append("\nDetails of failed requests:\n");
        emailText.append("\n"); // Next line

        // Include detailed failed requests in the email
        for (FailedRequest failedRequest : failedRequests) {
            emailText.append(String.format(
                "Timestamp: %s, Error: %s\n", 
                failedRequest.getTimestamp(), 
                failedRequest.getFailureReason()
            ));
        }

        emailText.append("\nPlease investigate this issue.");
        message.setText(emailText.toString());

        // Send the email
        mailSender.send(message);
    }
    
    public List<FailedRequest> getFailedRequestMetrics() {
        return failedRequestRepository.findAll();
    }
}