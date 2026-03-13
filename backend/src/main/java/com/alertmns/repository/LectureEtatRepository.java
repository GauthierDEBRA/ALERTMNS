package com.alertmns.repository;

import com.alertmns.entity.LectureEtat;
import com.alertmns.entity.LectureEtatId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LectureEtatRepository extends JpaRepository<LectureEtat, LectureEtatId> {

    Optional<LectureEtat> findByIdIdUserAndIdIdCanal(Long userId, Long canalId);
}
