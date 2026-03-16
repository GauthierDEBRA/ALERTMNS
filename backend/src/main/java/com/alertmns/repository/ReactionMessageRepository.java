package com.alertmns.repository;

import com.alertmns.entity.ReactionMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReactionMessageRepository extends JpaRepository<ReactionMessage, Long> {

    Optional<ReactionMessage> findByMessageIdMessageAndUtilisateurIdUserAndEmoji(Long messageId, Long userId, String emoji);

    List<ReactionMessage> findByMessageIdMessage(Long messageId);

    @Query("SELECT r FROM ReactionMessage r LEFT JOIN FETCH r.utilisateur WHERE r.message.idMessage = :messageId ORDER BY r.idReaction ASC")
    List<ReactionMessage> findDetailedByMessageIdMessage(@Param("messageId") Long messageId);

    @Query("SELECT r FROM ReactionMessage r LEFT JOIN FETCH r.utilisateur WHERE r.message.idMessage IN :messageIds ORDER BY r.idReaction ASC")
    List<ReactionMessage> findDetailedByMessageIdMessageIn(@Param("messageIds") List<Long> messageIds);

    void deleteByMessageIdMessage(Long messageId);
}
