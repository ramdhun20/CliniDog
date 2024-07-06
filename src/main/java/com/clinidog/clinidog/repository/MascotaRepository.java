package com.clinidog.clinidog.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clinidog.clinidog.model.Mascotas;

public interface MascotaRepository extends JpaRepository<Mascotas, Long> {
    List<Mascotas> findAllByUsuariosId(Long usuarioId);
    List<Mascotas> findByUsuarios_Id(Long usuarioId);
}
