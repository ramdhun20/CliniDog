package com.clinidog.clinidog.controller;

import com.clinidog.clinidog.dto.LoginRequest;
import com.clinidog.clinidog.model.Usuarios;
import com.clinidog.clinidog.repository.UsuariosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuariosRepository usuariosRepository;

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getContrasena())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Obtiene el usuario autenticado desde el servicio de detalles de usuario
            Usuarios usuario = usuariosRepository.findByEmail(loginRequest.getEmail()).orElse(null);

            if (usuario == null) {
                throw new RuntimeException("Usuario no encontrado");
            }

            // Verifica el rol del usuario
            String role = usuario.getRol();

            // Establece el rol del usuario en el contexto de seguridad
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(authentication.getPrincipal(), null, AuthorityUtils.createAuthorityList(role))
            );

            Map<String, String> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("email", loginRequest.getEmail());
            response.put("role", role); // Añade el rol al objeto de respuesta
            return response;
        } catch (AuthenticationException e) {
            throw new RuntimeException("Combinación de nombre de usuario/contraseña no válida");
        }
    }

}