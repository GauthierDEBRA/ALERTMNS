package com.alertmns.repository;

import com.alertmns.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    List<Utilisateur> findByIsActiveTrue();

    boolean existsByEmail(String email);

    @Query("SELECT u FROM Utilisateur u WHERE u.role = :role AND u.isActive = true")
    List<Utilisateur> findByRoleAndIsActiveTrue(String role);
}
