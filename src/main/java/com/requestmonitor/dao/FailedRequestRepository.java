package com.requestmonitor.dao;

import com.requestmonitor.model.FailedRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FailedRequestRepository extends MongoRepository<FailedRequest, String> {
    List<FailedRequest> findByIpAddressAndTimestampBetween(
        String ipAddress, 
        LocalDateTime start, 
        LocalDateTime end
    );
}