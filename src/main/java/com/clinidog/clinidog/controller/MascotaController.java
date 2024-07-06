package com.clinidog.clinidog.controller;

import com.clinidog.clinidog.dto.MascotaRequest;
import com.clinidog.clinidog.model.Mascotas;
import com.clinidog.clinidog.model.Vacunas;
import com.clinidog.clinidog.repository.MascotaRepository;
import com.clinidog.clinidog.model.Usuarios;
import com.clinidog.clinidog.repository.UsuariosRepository;
import com.clinidog.clinidog.repository.VacunaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/mascotas")
@CrossOrigin
public class MascotaController {

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private VacunaRepository vacunaRepository;

    private Usuarios getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        return usuariosRepository.findByEmail(email).orElse(null);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USUARIO')")
    public ResponseEntity<?> addMascota(@RequestParam("usuarioId") Long usuarioId, @RequestBody MascotaRequest mascotaRequest) {
        Optional<Usuarios> usuarioOpt = usuariosRepository.findById(usuarioId);
        if (!usuarioOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        Set<Vacunas> vacunas = new HashSet<>();
        for (String nombreVacuna : mascotaRequest.getVacunas()) {
            Optional<Vacunas> vacunaOpt = vacunaRepository.findByNombre(nombreVacuna);
            if (!vacunaOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vacuna con nombre '" + nombreVacuna + "' no encontrada");
            }
            vacunas.add(vacunaOpt.get());
        }

        Mascotas mascota = new Mascotas();
        mascota.setNombre(mascotaRequest.getNombre());
        mascota.setRaza(mascotaRequest.getRaza());
        mascota.setEdad(mascotaRequest.getEdad());
        mascota.setUsuarios(usuarioOpt.get());
        mascota.setVacunas(vacunas);

        Mascotas savedMascota = mascotaRepository.save(mascota);

        return ResponseEntity.ok(savedMascota);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USUARIO')")
    public ResponseEntity<?> actualizarMascota(@PathVariable Long id, @RequestBody MascotaRequest mascotaRequest) {
        Optional<Mascotas> mascotaOpt = mascotaRepository.findById(id);
        if (!mascotaOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mascota no encontrada.");
        }

        Mascotas mascota = mascotaOpt.get();

        // Aquí puedes realizar las validaciones y actualizaciones necesarias
        if (mascotaRequest.getNombre() != null && !mascotaRequest.getNombre().isEmpty()) {
            mascota.setNombre(mascotaRequest.getNombre());
        }
        // Añadir más campos a actualizar según sea necesario

        Mascotas mascotaActualizada = mascotaRepository.save(mascota);
        return ResponseEntity.ok(mascotaActualizada);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USUARIO')")
    public ResponseEntity<String> eliminarMascota(@PathVariable Long id) {
        Usuarios usuario = getAuthenticatedUser();
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autorizado.");
        }

        Optional<Mascotas> mascotaOpt = mascotaRepository.findById(id);
        if (!mascotaOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mascota no encontrada.");
        }

        Mascotas mascotasExistente = mascotaOpt.get();
        if (!mascotasExistente.getUsuarios().getId().equals(usuario.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No está autorizado para eliminar esta mascota.");
        }

        mascotaRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body("Mascota eliminada correctamente.");
    }

    @GetMapping("/{idUsuario}")
    public ResponseEntity<Object> obtenerMascotasPorUsuario(@PathVariable Long idUsuario) {
        List<Mascotas> mascotas = mascotaRepository.findByUsuarios_Id(idUsuario);
        if (mascotas.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Este usuario no tiene mascotas.");
        }

        return ResponseEntity.ok(mascotas);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USUARIO')")
    public ResponseEntity<List<Mascotas>> obtenerTodasLasMascotas() {
        List<Mascotas> mascotas = mascotaRepository.findAll();
        return ResponseEntity.ok(mascotas);
    }


}
