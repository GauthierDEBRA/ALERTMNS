package com.alertmns.repository;

import com.alertmns.entity.Structure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StructureRepository extends JpaRepository<Structure, Long> {

    Optional<Structure> findByNom(String nom);

    Optional<Structure> findByNomIgnoreCase(String nom);

    boolean existsByNomIgnoreCase(String nom);
}
