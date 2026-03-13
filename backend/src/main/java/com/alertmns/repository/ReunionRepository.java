package com.alertmns.repository;

import com.alertmns.entity.Reunion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReunionRepository extends JpaRepository<Reunion, Long> {

    List<Reunion> findByCreateurIdUser(Long userId);

    @Query("SELECT r FROM Reunion r JOIN r.participants p WHERE p.utilisateur.idUser = :userId ORDER BY r.datePrevue ASC")
    List<Reunion> findReunionsForUser(@Param("userId") Long userId);

    @Query("SELECT r FROM Reunion r ORDER BY r.datePrevue DESC")
    List<Reunion> findAllOrderByDatePrevue();
}
