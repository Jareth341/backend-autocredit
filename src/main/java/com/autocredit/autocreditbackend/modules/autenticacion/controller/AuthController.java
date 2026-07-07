package com.autocredit.autocreditbackend.modules.autenticacion.controller;

import com.autocredit.autocreditbackend.modules.autenticacion.dto.LoginRequest;
import com.autocredit.autocreditbackend.modules.autenticacion.dto.LoginResponse;
import com.autocredit.autocreditbackend.modules.autenticacion.dto.RegisterRequest;
import com.autocredit.autocreditbackend.modules.autenticacion.dto.UsuarioResponseDTO;
import com.autocredit.autocreditbackend.modules.autenticacion.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponseDTO> register(@Valid @RequestBody RegisterRequest request) {
        UsuarioResponseDTO usuario = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }
}
