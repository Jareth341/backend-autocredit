package com.autocredit.autocreditbackend.modules.tipocambio.service;

import com.autocredit.autocreditbackend.modules.tipocambio.dto.TipoCambioResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Locale;

@Service
public class TipoCambioService {

    private final double fallbackUsdPen;

    public TipoCambioService(@Value("${app.exchange.fallback.usd-pen:3.80}") double fallbackUsdPen) {
        this.fallbackUsdPen = fallbackUsdPen;
    }

    public TipoCambioResponseDTO obtenerUltimo(String base, String target) {
        String baseNormalizada = normalizar(base);
        String targetNormalizada = normalizar(target);

        if (baseNormalizada.equals(targetNormalizada)) {
            return respuesta(baseNormalizada, targetNormalizada, 1.0, "FALLBACK", "Misma moneda");
        }

        if (baseNormalizada.equals("USD") && targetNormalizada.equals("PEN")) {
            return respuesta("USD", "PEN", fallbackUsdPen, "FALLBACK", "Tipo de cambio referencial desde configuracion");
        }

        if (baseNormalizada.equals("PEN") && targetNormalizada.equals("USD")) {
            return respuesta("PEN", "USD", redondear(1 / fallbackUsdPen), "FALLBACK", "Tipo de cambio inverso referencial");
        }

        throw new IllegalArgumentException("Par de monedas no soportado. Use USD/PEN o PEN/USD");
    }

    private TipoCambioResponseDTO respuesta(String base, String target, double rate, String source, String message) {
        return new TipoCambioResponseDTO(base, target, redondear(rate), source, OffsetDateTime.now(), message);
    }

    private String normalizar(String moneda) {
        if (moneda == null || moneda.isBlank()) {
            throw new IllegalArgumentException("base y target son obligatorios");
        }
        return moneda.trim().toUpperCase(Locale.ROOT);
    }

    private double redondear(double valor) {
        return Math.round(valor * 10000.0) / 10000.0;
    }
}
