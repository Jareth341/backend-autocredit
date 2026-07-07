package com.autocredit.autocreditbackend.modules.creditos.controller;

import com.autocredit.autocreditbackend.modules.creditos.dto.CreditoFormDTO;
import com.autocredit.autocreditbackend.modules.creditos.entity.Credito;
import com.autocredit.autocreditbackend.modules.creditos.service.CreditoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/creditos")
@RequiredArgsConstructor
public class CreditoController {

    private final CreditoService creditoService;

    @GetMapping
    public ResponseEntity<List<Credito>> listar() {
        return ResponseEntity.ok(creditoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Credito> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(creditoService.obtenerPorId(id));
    }

    @GetMapping("/por-cliente/{clienteId}")
    public ResponseEntity<Credito> obtenerPorCliente(@PathVariable String clienteId) {
        return creditoService.obtenerPorCliente(clienteId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(null));
    }

    @PostMapping
    public ResponseEntity<Credito> crear(@Valid @RequestBody CreditoFormDTO dto) {
        Credito credito = creditoService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(credito);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Credito> actualizar(@PathVariable String id, @Valid @RequestBody CreditoFormDTO dto) {
        return ResponseEntity.ok(creditoService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        creditoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
