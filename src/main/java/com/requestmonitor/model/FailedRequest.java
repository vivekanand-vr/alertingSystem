package com.requestmonitor.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "failed_requests")
public class FailedRequest {
    @Id
    private String id;
    
    private String ipAddress;
    private String requestPath;
    private String failureReason;
    private LocalDateTime timestamp;
    private String requestMethod;
    private String requestBody;
    private boolean alertSent;
}