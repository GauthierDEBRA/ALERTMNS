package com.alertmns.repository;

import com.alertmns.entity.Canal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CanalRepository extends JpaRepository<Canal, Long> {

    boolean existsByNomIgnoreCase(String nom);

    boolean existsByNomIgnoreCaseAndIdCanalNot(String nom, Long idCanal);

    List<Canal> findByTypeCanalNotOrderByNomAsc(String typeCanal);

    @Query("SELECT DISTINCT c FROM Canal c JOIN MembreCanal mc ON mc.canal.idCanal = c.idCanal WHERE mc.utilisateur.idUser = :userId")
    List<Canal> findCanauxForUser(@Param("userId") Long userId);

    @Query("SELECT c FROM Canal c WHERE c.estPrive = false")
    List<Canal> findPublicCanaux();

    @Query("""
        SELECT DISTINCT c
        FROM Canal c
        JOIN MembreCanal mc1 ON mc1.canal.idCanal = c.idCanal
        JOIN MembreCanal mc2 ON mc2.canal.idCanal = c.idCanal
        WHERE c.typeCanal = 'direct'
          AND mc1.utilisateur.idUser = :userId
          AND mc2.utilisateur.idUser = :otherUserId
        """)
    Optional<Canal> findDirectCanalBetweenUsers(@Param("userId") Long userId,
                                                @Param("otherUserId") Long otherUserId);
}
