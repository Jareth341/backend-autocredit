package com.autocredit.autocreditbackend.modules.creditos.service;

import com.autocredit.autocreditbackend.modules.creditos.enums.Capitalizacion;
import com.autocredit.autocreditbackend.modules.creditos.enums.TipoTasa;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TasaConverterServiceTest {

    private final TasaConverterService service = new TasaConverterService();

    @Test
    void teaATemConvierteCorrectamente() {
        double tem = service.calcularTem(TipoTasa.TEA, 12.0, null);

        assertEquals(0.9489, tem, 0.0001);
    }

    @Test
    void tnaMensualATemConvierteCorrectamente() {
        double tem = service.calcularTem(TipoTasa.TNA, 12.0, Capitalizacion.MENSUAL);

        assertEquals(1.0, tem, 0.0001);
    }

    @Test
    void tnaSinCapitalizacionLanzaError() {
        assertThrows(IllegalArgumentException.class,
                () -> service.calcularTem(TipoTasa.TNA, 12.0, null));
    }
}
