package com.autocredit.autocreditbackend.modules.administracionusuarios.service;

import com.autocredit.autocreditbackend.core.exception.DuplicateResourceException;
import com.autocredit.autocreditbackend.core.exception.ResourceNotFoundException;
import com.autocredit.autocreditbackend.modules.administracionusuarios.dto.EstadisticasUsuariosDTO;
import com.autocredit.autocreditbackend.modules.administracionusuarios.dto.UsuarioAdminFormDTO;
import com.autocredit.autocreditbackend.modules.autenticacion.dto.UsuarioResponseDTO;
import com.autocredit.autocreditbackend.modules.autenticacion.entity.Usuario;
import com.autocredit.autocreditbackend.modules.autenticacion.enums.Rol;
import com.autocredit.autocreditbackend.modules.autenticacion.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GestionUsuariosService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UsuarioResponseDTO> listar(String filtro, String rol) {
        return buscar(filtro, rol).stream()
                .map(UsuarioResponseDTO::from)
                .toList();
    }

    private List<Usuario> buscar(String filtro, String rol) {
        if (filtro != null && !filtro.isBlank()) {
            return usuarioRepository
                    .findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrCorreoContainingIgnoreCase(
                            filtro, filtro, filtro
                    );
        }
        if (rol != null && !rol.isBlank()) {
            return usuarioRepository.findByRol(Rol.valueOf(rol));
        }
        return usuarioRepository.findAll();
    }

    public EstadisticasUsuariosDTO obtenerEstadisticas() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        long total = usuarios.size();
        long activos = usuarios.stream().filter(Usuario::isActivo).count();
        long inactivos = total - activos;
        long administradores = usuarios.stream().filter(u -> u.getRol() == Rol.ADMINISTRADOR).count();

        return new EstadisticasUsuariosDTO(total, activos, inactivos, administradores);
    }

    public UsuarioResponseDTO crear(UsuarioAdminFormDTO dto) {
        validarDuplicadosParaCrear(dto);
        if (dto.getPasswordTemporal() == null || dto.getPasswordTemporal().isBlank()) {
            throw new IllegalArgumentException("La contrasena temporal es obligatoria para crear usuarios");
        }

        Usuario usuario = Usuario.builder()
                .nombres(dto.getNombres())
                .apellidos(dto.getApellidos())
                .correo(dto.getCorreo())
                .usuario(dto.getUsuario())
                .password(passwordEncoder.encode(dto.getPasswordTemporal()))
                .rol(dto.getRol())
                .entidad(dto.getEntidad())
                .activo(true)
                .fechaRegistro(LocalDate.now())
                .build();

        return UsuarioResponseDTO.from(usuarioRepository.save(usuario));
    }

    public UsuarioResponseDTO actualizar(String id, UsuarioAdminFormDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        validarDuplicadosParaActualizar(id, dto);

        usuario.setNombres(dto.getNombres());
        usuario.setApellidos(dto.getApellidos());
        usuario.setCorreo(dto.getCorreo());
        usuario.setUsuario(dto.getUsuario());
        usuario.setRol(dto.getRol());
        usuario.setEntidad(dto.getEntidad());

        if (dto.getPasswordTemporal() != null && !dto.getPasswordTemporal().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(dto.getPasswordTemporal()));
        }

        return UsuarioResponseDTO.from(usuarioRepository.save(usuario));
    }

    public UsuarioResponseDTO cambiarEstado(String id, boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        usuario.setActivo(activo);
        return UsuarioResponseDTO.from(usuarioRepository.save(usuario));
    }

    public void eliminar(String id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        usuarioRepository.delete(usuario);
    }

    public boolean existeCorreoDuplicado(String correo, String idExcluir) {
        return idExcluir != null
                ? usuarioRepository.existsByCorreoAndIdNot(correo, idExcluir)
                : usuarioRepository.existsByCorreo(correo);
    }

    private void validarDuplicadosParaCrear(UsuarioAdminFormDTO dto) {
        if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
            throw new DuplicateResourceException("CORREO_DUPLICADO");
        }
        if (usuarioRepository.existsByUsuario(dto.getUsuario())) {
            throw new DuplicateResourceException("USUARIO_DUPLICADO");
        }
    }

    private void validarDuplicadosParaActualizar(String id, UsuarioAdminFormDTO dto) {
        if (usuarioRepository.existsByCorreoAndIdNot(dto.getCorreo(), id)) {
            throw new DuplicateResourceException("CORREO_DUPLICADO");
        }
        if (usuarioRepository.existsByUsuarioAndIdNot(dto.getUsuario(), id)) {
            throw new DuplicateResourceException("USUARIO_DUPLICADO");
        }
    }
}
