package com.autocredit.autocreditbackend.modules.analisiscomparativo.service;

import com.autocredit.autocreditbackend.modules.analisiscomparativo.entity.SimulacionGuardada;
import com.autocredit.autocreditbackend.modules.analisiscomparativo.repository.SimulacionGuardadaRepository;
import com.autocredit.autocreditbackend.modules.vehiculos.enums.MonedaVehiculo;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ComparadorServiceTest {

    private final SimulacionGuardadaRepository repository = mock(SimulacionGuardadaRepository.class);
    private final ComparadorService service = new ComparadorService(repository);

    @Test
    void comparaMismaMonedaPorMenorTcea() {
        SimulacionGuardada a = simulacion("a", MonedaVehiculo.PEN, 20.0);
        SimulacionGuardada b = simulacion("b", MonedaVehiculo.PEN, 18.0);
        when(repository.findById("a")).thenReturn(Optional.of(a));
        when(repository.findById("b")).thenReturn(Optional.of(b));

        var response = service.compararEscenarios("a", "b");

        assertTrue(response.isComparable());
        assertEquals("b", response.getMejorOpcionId());
    }

    @Test
    void noComparaMonedasDistintas() {
        SimulacionGuardada a = simulacion("a", MonedaVehiculo.PEN, 20.0);
        SimulacionGuardada b = simulacion("b", MonedaVehiculo.USD, 18.0);
        when(repository.findById("a")).thenReturn(Optional.of(a));
        when(repository.findById("b")).thenReturn(Optional.of(b));

        var response = service.compararEscenarios("a", "b");

        assertFalse(response.isComparable());
        assertNull(response.getMejorOpcionId());
        assertEquals("MONEDAS_NO_COMPARABLES", response.getCodigoAdvertencia());
    }

    private SimulacionGuardada simulacion(String id, MonedaVehiculo moneda, Double tcea) {
        return SimulacionGuardada.builder()
                .id(id)
                .codigo("#SIM-" + id)
                .clienteId("c" + id)
                .creditoId("cr" + id)
                .moneda(moneda)
                .tcea(tcea)
                .build();
    }
}
