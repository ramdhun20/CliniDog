package com.clinidog.clinidog.controller;

import com.clinidog.clinidog.model.Vacunas;
import com.clinidog.clinidog.repository.VacunaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/vacunas")
public class VacunaController {

    @Autowired
    private VacunaRepository vacunaRepository;

    @PostMapping("/agregar")
    public ResponseEntity<?> crearVacuna(@RequestBody Vacunas vacuna) {
        try {
            Vacunas nuevaVacuna = vacunaRepository.save(vacuna);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaVacuna);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear la vacuna: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Vacunas>> obtenerTodasVacunas() {
        List<Vacunas> vacunas = vacunaRepository.findAll();
        return ResponseEntity.ok(vacunas);
    }
}
