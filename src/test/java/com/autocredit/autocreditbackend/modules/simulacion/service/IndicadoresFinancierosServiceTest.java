package com.autocredit.autocreditbackend.modules.simulacion.service;

import com.autocredit.autocreditbackend.modules.simulacion.entity.Cronograma;
import com.autocredit.autocreditbackend.modules.simulacion.entity.PeriodoPago;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IndicadoresFinancierosServiceTest {

    private final IndicadoresFinancierosService service = new IndicadoresFinancierosService();

    @Test
    void vanConFlujoConocidoDesdeDeudor() {
        Cronograma cronograma = cronogramaConPagos(1000, 100, 12);

        assertEquals(-200.0, service.calcularVan(cronograma, 0), 0.01);
    }

    @Test
    void tirYTceaNoDevuelvenNan() {
        Cronograma cronograma = cronogramaConPagos(1000, 100, 12);

        Double tir = service.calcularTirMensual(cronograma);
        Double tcea = service.calcularTcea(cronograma);

        assertTrue(tir > 0);
        assertTrue(tcea > 0);
        assertFalse(Double.isNaN(tir));
        assertFalse(Double.isNaN(tcea));
    }

    @Test
    void tirNoConvergenteDevuelveNull() {
        Cronograma cronograma = cronogramaConPagos(1000, 0, 12);

        assertNull(service.calcularTirMensual(cronograma));
    }

    private Cronograma cronogramaConPagos(double monto, double pago, int meses) {
        List<PeriodoPago> periodos = new ArrayList<>();
        for (int i = 1; i <= meses; i++) {
            periodos.add(PeriodoPago.builder()
                    .numero(i)
                    .cuotaTotal(pago)
                    .build());
        }
        return Cronograma.builder()
                .montoFinanciado(monto)
                .interesTotal(0.0)
                .seguroTotal(0.0)
                .comisionTotal(0.0)
                .costoTotalCredito(pago * meses)
                .periodos(periodos)
                .build();
    }
}
