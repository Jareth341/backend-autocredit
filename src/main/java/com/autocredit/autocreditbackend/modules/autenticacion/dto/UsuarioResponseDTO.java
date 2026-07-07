package com.autocredit.autocreditbackend.modules.autenticacion.dto;

import com.autocredit.autocreditbackend.modules.autenticacion.entity.Usuario;
import com.autocredit.autocreditbackend.modules.autenticacion.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {
    private String id;
    private String nombres;
    private String apellidos;
    private String correo;
    private String usuario;
    private Rol rol;
    private String entidad;
    private boolean activo;
    private LocalDate fechaRegistro;

    public static UsuarioResponseDTO from(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getCorreo(),
                usuario.getUsuario(),
                usuario.getRol(),
                usuario.getEntidad(),
                usuario.isActivo(),
                usuario.getFechaRegistro()
        );
    }
}
