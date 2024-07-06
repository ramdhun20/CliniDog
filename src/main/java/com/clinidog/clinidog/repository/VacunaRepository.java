package com.clinidog.clinidog.repository;

import com.clinidog.clinidog.model.Vacunas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VacunaRepository extends JpaRepository<Vacunas, Long> {
    Optional<Vacunas> findByNombre(String nombre);
}
