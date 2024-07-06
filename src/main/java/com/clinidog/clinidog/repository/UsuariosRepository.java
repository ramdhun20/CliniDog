package com.clinidog.clinidog.repository;


import com.clinidog.clinidog.model.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuariosRepository extends JpaRepository<Usuarios, Long> {

    boolean existsByEmail(String email);

    Optional<Usuarios> findByEmail(String email);
}
