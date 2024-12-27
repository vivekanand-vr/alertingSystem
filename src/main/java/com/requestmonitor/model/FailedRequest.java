package com.requestmonitor.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Data
@Document(collection = "failed_requests")
public class FailedRequest {
    @Id
    private String id;
    
    private String ipAddress;
    private String requestPath;
    private String failureReason;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    private String requestMethod;
    private String requestBody;
}