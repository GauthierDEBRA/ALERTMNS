package com.alertmns.repository;

import com.alertmns.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT DISTINCT m FROM Message m " +
            "LEFT JOIN FETCH m.utilisateur " +
            "LEFT JOIN FETCH m.piecesJointes " +
            "LEFT JOIN FETCH m.reactions r " +
            "LEFT JOIN FETCH r.utilisateur " +
            "WHERE m.canal.idCanal = :canalId ORDER BY m.dateEnvoi ASC")
    List<Message> findByIdCanalOrderByDateEnvoi(@Param("canalId") Long canalId);

    @Query("SELECT DISTINCT m FROM Message m " +
            "LEFT JOIN FETCH m.utilisateur " +
            "LEFT JOIN FETCH m.piecesJointes " +
            "LEFT JOIN FETCH m.reactions r " +
            "LEFT JOIN FETCH r.utilisateur " +
            "WHERE m.idMessage = :messageId")
    Optional<Message> findDetailedById(@Param("messageId") Long messageId);

    @Query("SELECT m FROM Message m WHERE m.canal.idCanal = :canalId ORDER BY m.dateEnvoi DESC")
    List<Message> findByCanalIdDesc(@Param("canalId") Long canalId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.canal.idCanal = :canalId AND m.idMessage > :lastReadId")
    Long countUnreadMessages(@Param("canalId") Long canalId, @Param("lastReadId") Long lastReadId);

    @Query("SELECT DISTINCT m FROM Message m " +
            "JOIN FETCH m.utilisateur " +
            "JOIN FETCH m.canal c " +
            "WHERE EXISTS (" +
            "   SELECT 1 FROM MembreCanal mc " +
            "   WHERE mc.canal = c AND mc.utilisateur.idUser = :userId" +
            ") " +
            "AND (m.isDeleted = false OR m.isDeleted IS NULL) " +
            "AND LOWER(m.contenu) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "ORDER BY m.dateEnvoi DESC")
    List<Message> searchAccessibleMessages(@Param("userId") Long userId, @Param("query") String query);
}
