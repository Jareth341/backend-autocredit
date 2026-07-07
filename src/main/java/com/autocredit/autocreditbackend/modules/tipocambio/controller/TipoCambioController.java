package com.autocredit.autocreditbackend.modules.tipocambio.controller;

import com.autocredit.autocreditbackend.modules.tipocambio.dto.TipoCambioResponseDTO;
import com.autocredit.autocreditbackend.modules.tipocambio.service.TipoCambioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tipo-cambio")
@RequiredArgsConstructor
public class TipoCambioController {

    private final TipoCambioService tipoCambioService;

    @GetMapping("/latest")
    public ResponseEntity<TipoCambioResponseDTO> latest(
            @RequestParam(defaultValue = "USD") String base,
            @RequestParam(defaultValue = "PEN") String target
    ) {
        return ResponseEntity.ok(tipoCambioService.obtenerUltimo(base, target));
    }
}
