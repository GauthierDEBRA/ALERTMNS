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
              AND (:targetType IS NULL OR a.targetType = :targetType)
              AND (:start IS NULL OR a.createdAt >= :start)
              AND (:end IS NULL OR a.createdAt <= :end)
              AND (
                    :query IS NULL
                    OR LOWER(COALESCE(a.action, '')) LIKE CONCAT('%', :query, '%')
                    OR LOWER(COALESCE(a.targetType, '')) LIKE CONCAT('%', :query, '%')
                    OR LOWER(COALESCE(a.details, '')) LIKE CONCAT('%', :query, '%')
                    OR LOWER(COALESCE(actor.prenom, '')) LIKE CONCAT('%', :query, '%')
                    OR LOWER(COALESCE(actor.nom, '')) LIKE CONCAT('%', :query, '%')
                    OR LOWER(COALESCE(actor.email, '')) LIKE CONCAT('%', :query, '%')
                    OR LOWER(COALESCE(actor.role, '')) LIKE CONCAT('%', :query, '%')
                    OR (:queryNumeric = TRUE AND STR(a.targetId) LIKE CONCAT('%', :query, '%'))
                  )
            ORDER BY a.createdAt DESC
            """)
    List<AuditLog> findForAudit(@Param("actorId") Long actorId,
                                @Param("action") String action,
                                @Param("targetType") String targetType,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end,
                                @Param("query") String query,
                                @Param("queryNumeric") boolean queryNumeric);
}
