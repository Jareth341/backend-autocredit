package com.autocredit.autocreditbackend.modules.autenticacion.service;

import com.autocredit.autocreditbackend.config.JwtService;
import com.autocredit.autocreditbackend.modules.autenticacion.dto.LoginRequest;
import com.autocredit.autocreditbackend.modules.autenticacion.dto.RegisterRequest;
import com.autocredit.autocreditbackend.modules.autenticacion.dto.UsuarioResponseDTO;
import com.autocredit.autocreditbackend.modules.autenticacion.entity.Usuario;
import com.autocredit.autocreditbackend.modules.autenticacion.enums.Rol;
import com.autocredit.autocreditbackend.modules.autenticacion.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final AuthService service = new AuthService(usuarioRepository, passwordEncoder, authenticationManager, jwtService);

    @Test
    void loginCorrectoDevuelveTokenYUsuarioSinPassword() {
        Usuario usuario = usuario();
        LoginRequest request = new LoginRequest();
        request.setCorreo(usuario.getCorreo());
        request.setPassword("Cliente123");

        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(usuarioRepository.findByCorreo(usuario.getCorreo())).thenReturn(Optional.of(usuario));
        when(jwtService.generarToken(usuario)).thenReturn("jwt-token");

        var response = service.login(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals(usuario.getId(), response.getUsuario().getId());
        assertEquals(usuario.getCorreo(), response.getUsuario().getCorreo());
    }

    @Test
    void loginIncorrectoLanzaBadCredentials() {
        LoginRequest request = new LoginRequest();
        request.setCorreo("nadie@autocredit.pe");
        request.setPassword("bad");
        doThrow(new BadCredentialsException("bad")).when(authenticationManager).authenticate(any());

        assertThrows(BadCredentialsException.class, () -> service.login(request));
    }

    @Test
    void registerPublicoPermiteCrearAdministrador() {
        RegisterRequest request = new RegisterRequest();
        request.setNombres("Admin");
        request.setApellidos("Demo");
        request.setCorreo("new-admin@autocredit.pe");
        request.setUsuario("new.admin");
        request.setPassword("Admin1234");
        request.setConfirmarPassword("Admin1234");
        request.setEntidad("AutoCredit");
        request.setRol(Rol.ADMINISTRADOR);

        when(passwordEncoder.encode("Admin1234")).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario guardado = invocation.getArgument(0);
            guardado.setId("u3");
            return guardado;
        });

        UsuarioResponseDTO response = service.register(request);

        assertEquals(Rol.ADMINISTRADOR, response.getRol());
    }

    @Test
    void registerClienteDevuelveDtoSinPassword() {
        RegisterRequest request = new RegisterRequest();
        request.setNombres("Cliente");
        request.setApellidos("Demo");
        request.setCorreo("cliente.demo@autocredit.pe");
        request.setUsuario("cliente.demo");
        request.setPassword("Cliente1234");
        request.setConfirmarPassword("Cliente1234");
        request.setEntidad("AutoCredit");
        request.setRol(Rol.CLIENTE);

        when(passwordEncoder.encode("Cliente1234")).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario guardado = invocation.getArgument(0);
            guardado.setId("u2");
            return guardado;
        });

        var response = service.register(request);

        assertNotNull(response.getId());
        assertEquals(Rol.CLIENTE, response.getRol());
    }

    private Usuario usuario() {
        return Usuario.builder()
                .id("u1")
                .nombres("Cliente")
                .apellidos("Demo")
                .correo("cliente@autocredit.pe")
                .usuario("cliente.demo")
                .password("hash")
                .rol(Rol.CLIENTE)
                .entidad("AutoCredit")
                .activo(true)
                .fechaRegistro(LocalDate.now())
                .build();
    }
}