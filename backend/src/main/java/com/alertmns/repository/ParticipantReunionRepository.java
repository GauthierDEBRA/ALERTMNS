package com.alertmns.repository;

import com.alertmns.entity.ParticipantReunion;
import com.alertmns.entity.ParticipantReunionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantReunionRepository extends JpaRepository<ParticipantReunion, ParticipantReunionId> {

    List<ParticipantReunion> findByIdIdReunion(Long reunionId);

    List<ParticipantReunion> findByIdIdUser(Long userId);

    Optional<ParticipantReunion> findByIdIdReunionAndIdIdUser(Long reunionId, Long userId);
}
