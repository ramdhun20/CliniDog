package com.clinidog.clinidog.controller;

import com.google.gson.Gson;
import com.clinidog.clinidog.model.Usuarios;
import com.clinidog.clinidog.repository.UsuariosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin
public class UsuarioController {

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping
    public ResponseEntity<String> registrarUsuario(@RequestBody Usuarios usuarios) {
        Optional<Usuarios> usuarioExistente = usuariosRepository.findByEmail(usuarios.getEmail());
        if (usuarioExistente.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El correo electrónico ya está en uso.");
        }

        if (!validarEmail(usuarios.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El correo electrónico no es válido.");
        }

        if (usuarios.getNombre() == null || usuarios.getNombre().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El nombre es obligatorio.");
        }

        if (usuarios.getApellidos() == null || usuarios.getApellidos().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El apellido es obligatorio.");
        }

        String contrasena = usuarios.getContrasena();
        if (!validarContrasena(contrasena)) {
            StringBuilder mensaje = new StringBuilder("La contraseña no cumple con los requisitos mínimos. Requisitos faltantes:");
            if (contrasena.length() < 8) {
                mensaje.append(" La contraseña debe tener al menos 8 caracteres.");
            }
            if (!contieneLetraMayuscula(contrasena)) {
                mensaje.append(" Agregue letras mayúsculas.");
            }
            if (!contieneLetraMinuscula(contrasena)) {
                mensaje.append(" Agregue letras minúsculas.");
            }
            if (!contieneCaracterEspecial(contrasena)) {
                mensaje.append(" Agregue un caracter especial.");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mensaje.toString());
        }

        usuarios.setContrasena(passwordEncoder.encode(usuarios.getContrasena()));
        usuarios.setRol("ADMIN"); // Establecer el rol por defecto como USUARIO
        Usuarios usuarioGuardado = usuariosRepository.save(usuarios);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Usuario registrado correctamente.");
        Gson gson = new Gson();
        String responseBody = gson.toJson(response);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USUARIO')")
    public ResponseEntity<String> actualizarUsuario(@PathVariable Long id, @RequestBody Usuarios usuarioActualizado) {
        String authenticatedUserEmail = getAuthenticatedUserEmail();

        Optional<Usuarios> usuarioExistenteOpt = usuariosRepository.findById(id);
        if (!usuarioExistenteOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
        }

        Usuarios usuarioExistente = usuarioExistenteOpt.get();

        // Permitir solo a los admins o al propio usuario modificar la información
        if (!usuarioExistente.getEmail().equals(authenticatedUserEmail) && !usuarioExistente.getRol().equals("USUARIO")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para modificar la información de este usuario.");
        }

        // Validaciones y actualizaciones
        if (usuarioActualizado.getEmail() != null && !usuarioActualizado.getEmail().equals(usuarioExistente.getEmail())) {
            if (!validarEmail(usuarioActualizado.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El correo electrónico no es válido.");
            }
            Optional<Usuarios> usuarioPorEmail = usuariosRepository.findByEmail(usuarioActualizado.getEmail());
            if (usuarioPorEmail.isPresent() && !usuarioPorEmail.get().getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El correo electrónico ya está en uso.");
            }
            usuarioExistente.setEmail(usuarioActualizado.getEmail());
        }

        if (usuarioActualizado.getContrasena() != null && !usuarioActualizado.getContrasena().isEmpty()) {
            if (!validarContrasena(usuarioActualizado.getContrasena())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La contraseña no cumple con los requisitos mínimos.");
            }
            usuarioExistente.setContrasena(passwordEncoder.encode(usuarioActualizado.getContrasena()));
        }

        if (usuarioActualizado.getNombre() != null && !usuarioActualizado.getNombre().isEmpty()) {
            usuarioExistente.setNombre(usuarioActualizado.getNombre());
        }

        if (usuarioActualizado.getApellidos() != null && !usuarioActualizado.getApellidos().isEmpty()) {
            usuarioExistente.setApellidos(usuarioActualizado.getApellidos());
        }

        usuariosRepository.save(usuarioExistente);
        return ResponseEntity.ok("Usuario actualizado correctamente.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarUsuario(@PathVariable Long id) {
        String authenticatedUserEmail = getAuthenticatedUserEmail();
        Optional<Usuarios> usuarioExistenteOpt = usuariosRepository.findById(id);
        if (!usuarioExistenteOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
        }

        Usuarios usuarioExistente = usuarioExistenteOpt.get();

        // Permitir solo a los admins o al propio usuario eliminar su cuenta
        if (!usuarioExistente.getEmail().equals(authenticatedUserEmail) && !usuarioExistente.getRol().equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para eliminar este usuario.");
        }

        usuariosRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body("Usuario eliminado correctamente.");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuarios> obtenerUsuarioPorId(@PathVariable Long id) {
        Optional<Usuarios> usuario = usuariosRepository.findById(id);
        return usuario.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @GetMapping
    public ResponseEntity<List<Usuarios>> obtenerTodosLosUsuarios() {
        List<Usuarios> usuarios = usuariosRepository.findAll();
        return ResponseEntity.ok(usuarios);
    }

    // Métodos de validación

    private boolean validarEmail(String email) {
        String regex = "^[a-zA-Z0-9_+&-]+(?:\\.[a-zA-Z0-9_+&-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean validarContrasena(String contrasena) {
        if (contrasena.length() < 8) {
            return false;
        }

        String pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
        return contrasena.matches(pattern);
    }

    private boolean contieneLetraMayuscula(String contrasena) {
        return contrasena.matches(".*[A-Z].*");
    }

    private boolean contieneLetraMinuscula(String contrasena) {
        return contrasena.matches(".*[a-z].*");
    }

    private boolean contieneCaracterEspecial(String contrasena) {
        return contrasena.matches(".*[@#$%^&+=!].*");
    }

    // Método para obtener el usuario autenticado
    private String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userDetails.getUsername();
        }
        return null;
    }
}
