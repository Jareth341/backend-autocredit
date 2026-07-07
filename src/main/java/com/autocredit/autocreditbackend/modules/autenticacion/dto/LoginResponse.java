package com.autocredit.autocreditbackend.modules.autenticacion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private UsuarioResponseDTO usuario;
}
