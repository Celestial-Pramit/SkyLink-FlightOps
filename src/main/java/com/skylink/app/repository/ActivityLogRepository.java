package com.skylink.app.repository;

import com.skylink.app.entity.ActivityLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    @Query("SELECT a FROM ActivityLog a ORDER BY a.createdAt DESC")
    List<ActivityLog> findRecent(Pageable pageable);

    @Query("SELECT a FROM ActivityLog a WHERE a.actorEmail = :email ORDER BY a.createdAt DESC")
    List<ActivityLog> findByActor(String email, Pageable pageable);

    @Query("SELECT a FROM ActivityLog a WHERE a.entityType = :type ORDER BY a.createdAt DESC")
    List<ActivityLog> findByEntityType(String type, Pageable pageable);
}
