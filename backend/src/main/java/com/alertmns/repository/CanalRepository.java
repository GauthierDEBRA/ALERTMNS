package com.alertmns.repository;

import com.alertmns.entity.Canal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CanalRepository extends JpaRepository<Canal, Long> {

    @Query("SELECT c FROM Canal c JOIN MembreCanal mc ON mc.canal.idCanal = c.idCanal WHERE mc.utilisateur.idUser = :userId")
    List<Canal> findCanauxForUser(@Param("userId") Long userId);

    @Query("SELECT c FROM Canal c WHERE c.estPrive = false")
    List<Canal> findPublicCanaux();
}
