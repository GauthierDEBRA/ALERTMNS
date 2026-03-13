package com.alertmns.repository;

import com.alertmns.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m LEFT JOIN FETCH m.utilisateur LEFT JOIN FETCH m.piecesJointes WHERE m.canal.idCanal = :canalId ORDER BY m.dateEnvoi ASC")
    List<Message> findByIdCanalOrderByDateEnvoi(@Param("canalId") Long canalId);

    @Query("SELECT m FROM Message m WHERE m.canal.idCanal = :canalId ORDER BY m.dateEnvoi DESC")
    List<Message> findByCanalIdDesc(@Param("canalId") Long canalId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.canal.idCanal = :canalId AND m.idMessage > :lastReadId")
    Long countUnreadMessages(@Param("canalId") Long canalId, @Param("lastReadId") Long lastReadId);
}
