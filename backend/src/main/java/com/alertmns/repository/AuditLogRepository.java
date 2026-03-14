package com.alertmns.repository;

import com.alertmns.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("""
            SELECT a
            FROM AuditLog a
            LEFT JOIN FETCH a.actor actor
            WHERE (:actorId IS NULL OR actor.idUser = :actorId)
              AND (:action IS NULL OR a.action = :action)
              AND (:start IS NULL OR a.createdAt >= :start)
              AND (:end IS NULL OR a.createdAt <= :end)
            ORDER BY a.createdAt DESC
            """)
    List<AuditLog> findForAudit(@Param("actorId") Long actorId,
                                @Param("action") String action,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);
}
