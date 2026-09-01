package com.skylink.app.service;

import com.skylink.app.entity.ActivityLog;

import java.util.List;

public interface IActivityLogService {
    void log(String actorEmail, String actorName,
             String action, String entityType,
             String entityId, String detail);

    List<ActivityLog> getRecent(int limit);
}
