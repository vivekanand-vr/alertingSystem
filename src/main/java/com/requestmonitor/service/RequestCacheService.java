package com.requestmonitor.service;

import com.requestmonitor.model.FailedRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestCacheService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String FAILED_REQUESTS_PREFIX = "failed_requests:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    public void cacheFailedRequest(String ipAddress, FailedRequest failedRequest) {
        String key = FAILED_REQUESTS_PREFIX + ipAddress;
        
        // Get existing failed requests for this IP
        List<FailedRequest> failedRequests = getFailedRequests(ipAddress);
        if (failedRequests == null) {
            failedRequests = new ArrayList<>();
        }
        
        // Add new failed request
        failedRequests.add(failedRequest);
        
        // Update cache with new TTL
        redisTemplate.opsForValue().set(key, failedRequests, CACHE_TTL);
    }
    
    public List<FailedRequest> getFailedRequests(String ipAddress) {
        Object cachedValue = redisTemplate.opsForValue().get(FAILED_REQUESTS_PREFIX + ipAddress);
        
        if (cachedValue == null) {
            return new ArrayList<>();
        }
        
        try {
            // Convert the cached value to JSON string
            String jsonString = objectMapper.writeValueAsString(cachedValue);
            // Convert JSON string to List<FailedRequest>
            return objectMapper.readValue(
                jsonString,
                objectMapper.getTypeFactory().constructCollectionType(List.class, FailedRequest.class)
            );
        } catch (Exception e) {
            // Log the error and return empty list
            System.err.println("Error deserializing cached requests: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public void clearFailedRequests(String ipAddress) {
        redisTemplate.delete(FAILED_REQUESTS_PREFIX + ipAddress);
    }
    
    public int getFailedRequestCount(String ipAddress) {
        List<FailedRequest> requests = getFailedRequests(ipAddress);
        return requests.size();
    }
}