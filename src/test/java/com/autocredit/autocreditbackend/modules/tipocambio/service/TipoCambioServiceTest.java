package com.autocredit.autocreditbackend.modules.tipocambio.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TipoCambioServiceTest {

    private final TipoCambioService service = new TipoCambioService(3.80);

    @Test
    void fallbackUsdPenFunciona() {
        var response = service.obtenerUltimo("USD", "PEN");

        assertEquals("USD", response.getBase());
        assertEquals("PEN", response.getTarget());
        assertEquals(3.80, response.getRate(), 0.0001);
        assertEquals("FALLBACK", response.getSource());
    }

    @Test
    void monedaNoSoportadaDevuelveErrorClaro() {
        assertThrows(IllegalArgumentException.class, () -> service.obtenerUltimo("EUR", "PEN"));
    }
}
