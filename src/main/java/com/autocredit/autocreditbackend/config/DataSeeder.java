package com.autocredit.autocreditbackend.config;

import com.autocredit.autocreditbackend.modules.autenticacion.entity.Usuario;
import com.autocredit.autocreditbackend.modules.autenticacion.enums.Rol;
import com.autocredit.autocreditbackend.modules.autenticacion.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Profile({"dev", "test"})
@ConditionalOnProperty(name = "app.data-seeder.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }

        usuarioRepository.save(Usuario.builder()
                .nombres("Carlos").apellidos("Mendoza")
                .correo("admin@autocredit.pe").usuario("carlos.mendoza")
                .password(passwordEncoder.encode("Admin123"))
                .rol(Rol.ADMINISTRADOR).entidad("BCP")
                .activo(true).fechaRegistro(LocalDate.of(2026, 1, 1))
                .build());

        usuarioRepository.save(Usuario.builder()
                .nombres("Juan").apellidos("Rios")
                .correo("asesor@autocredit.pe").usuario("juan.rios")
                .password(passwordEncoder.encode("Asesor123"))
                .rol(Rol.ASESOR_FINANCIERO).entidad("BCP")
                .activo(true).fechaRegistro(LocalDate.of(2026, 1, 5))
                .build());

        usuarioRepository.save(Usuario.builder()
                .nombres("Maria").apellidos("Rojas")
                .correo("analista@autocredit.pe").usuario("maria.rojas")
                .password(passwordEncoder.encode("Analista123"))
                .rol(Rol.ANALISTA_FINANCIERO).entidad("Interbank")
                .activo(true).fechaRegistro(LocalDate.of(2026, 1, 7))
                .build());

        usuarioRepository.save(Usuario.builder()
                .nombres("Oscar").apellidos("Torres")
                .correo("cliente@autocredit.pe").usuario("oscar.torres")
                .password(passwordEncoder.encode("Cliente123"))
                .rol(Rol.CLIENTE).entidad("BCP")
                .activo(true).fechaRegistro(LocalDate.of(2026, 1, 10))
                .build());
    }
}
