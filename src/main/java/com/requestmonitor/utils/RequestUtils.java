package com.requestmonitor.utils;

import com.requestmonitor.model.FailedRequest;
import jakarta.servlet.http.HttpServletRequest;

public class RequestUtils {
	
    private static final String VALID_BASE_PATH = "/api";
    private static final String VALID_ENDPOINT = "/submit";
    
	public boolean isValidPath(String path) {
        return path.equals(VALID_BASE_PATH + VALID_ENDPOINT);
    }
	
	public FailedRequest createFailedRequest(HttpServletRequest request, String reason) {
        FailedRequest failedRequest = new FailedRequest();
        failedRequest.setIpAddress(request.getRemoteAddr());
        failedRequest.setRequestPath(request.getRequestURI());
        failedRequest.setFailureReason(reason);
        failedRequest.setTimestamp(java.time.LocalDateTime.now());
        failedRequest.setRequestMethod(request.getMethod());
        return failedRequest;
    }
}
