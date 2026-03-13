package com.alertmns.repository;

import com.alertmns.entity.Pointage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PointageRepository extends JpaRepository<Pointage, Long> {

    @Query("SELECT p FROM Pointage p WHERE p.utilisateur.idUser = :userId ORDER BY p.dateDebut ASC")
    List<Pointage> findByIdUserOrderByDateDebut(@Param("userId") Long userId);

    @Query("SELECT p FROM Pointage p WHERE p.utilisateur.idUser = :userId AND p.dateFin IS NULL")
    Optional<Pointage> findOpenPointage(@Param("userId") Long userId);

    @Query("SELECT p FROM Pointage p WHERE p.utilisateur.idUser = :userId AND p.dateDebut >= :start AND p.dateDebut <= :end ORDER BY p.dateDebut ASC")
    List<Pointage> findByUserAndDateRange(@Param("userId") Long userId,
                                          @Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);

    @Query("SELECT DISTINCT p.utilisateur.idUser FROM Pointage p WHERE p.dateFin IS NULL")
    List<Long> findPresentUserIds();
}
