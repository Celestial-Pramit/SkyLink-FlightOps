package com.skylink.app.service.impl;

import com.skylink.app.entity.ActivityLog;
import com.skylink.app.repository.ActivityLogRepository;
import com.skylink.app.service.IActivityLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityLogServiceImpl implements IActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String actorEmail, String actorName,
                    String action, String entityType,
                    String entityId, String detail) {
        try {
            activityLogRepository.save(
                ActivityLog.of(actorEmail, actorName,
                    action, entityType, entityId, detail));
        } catch (Exception e) {
            log.warn("Activity log write failed: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityLog> getRecent(int limit) {
        return activityLogRepository.findRecent(PageRequest.of(0, limit));
    }
}
