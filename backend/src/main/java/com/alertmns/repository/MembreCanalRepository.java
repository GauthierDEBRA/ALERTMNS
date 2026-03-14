package com.alertmns.repository;

import com.alertmns.entity.MembreCanal;
import com.alertmns.entity.MembreCanalId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembreCanalRepository extends JpaRepository<MembreCanal, MembreCanalId> {

    List<MembreCanal> findByIdIdCanal(Long canalId);

    List<MembreCanal> findByIdIdUser(Long userId);

    Optional<MembreCanal> findByIdIdUserAndIdIdCanal(Long userId, Long canalId);

    @Query("SELECT mc FROM MembreCanal mc WHERE mc.canal.idCanal = :canalId")
    List<MembreCanal> findByCanalId(@Param("canalId") Long canalId);

    boolean existsByIdIdUserAndIdIdCanal(Long userId, Long canalId);

    long countByIdIdCanal(Long canalId);

    long countByIdIdCanalAndRole(Long canalId, String role);
}
