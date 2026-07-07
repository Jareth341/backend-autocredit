package com.autocredit.autocreditbackend.modules.creditos.service;

import com.autocredit.autocreditbackend.modules.analisiscomparativo.repository.SimulacionGuardadaRepository;
import com.autocredit.autocreditbackend.modules.clientes.entity.Cliente;
import com.autocredit.autocreditbackend.modules.clientes.repository.ClienteRepository;
import com.autocredit.autocreditbackend.modules.creditos.dto.CreditoFormDTO;
import com.autocredit.autocreditbackend.modules.creditos.enums.Capitalizacion;
import com.autocredit.autocreditbackend.modules.creditos.enums.TipoGracia;
import com.autocredit.autocreditbackend.modules.creditos.enums.TipoTasa;
import com.autocredit.autocreditbackend.modules.creditos.repository.CreditoRepository;
import com.autocredit.autocreditbackend.modules.simulacion.repository.CronogramaRepository;
import com.autocredit.autocreditbackend.modules.vehiculos.entity.Vehiculo;
import com.autocredit.autocreditbackend.modules.vehiculos.enums.CondicionVehiculo;
import com.autocredit.autocreditbackend.modules.vehiculos.enums.MonedaVehiculo;
import com.autocredit.autocreditbackend.modules.vehiculos.repository.VehiculoRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreditoValidationTest {

    private final CreditoRepository creditoRepository = mock(CreditoRepository.class);
    private final ClienteRepository clienteRepository = mock(ClienteRepository.class);
    private final VehiculoRepository vehiculoRepository = mock(VehiculoRepository.class);
    private final CronogramaRepository cronogramaRepository = mock(CronogramaRepository.class);
    private final SimulacionGuardadaRepository simulacionGuardadaRepository = mock(SimulacionGuardadaRepository.class);
    private final CreditoService service = new CreditoService(
            creditoRepository,
            clienteRepository,
            vehiculoRepository,
            new TasaConverterService(),
            cronogramaRepository,
            simulacionGuardadaRepository
    );

    @Test
    void cuotaInicialDebeSerMenorAlPrecio() {
        CreditoFormDTO dto = base();
        dto.setCuotaInicial(12000.0);
        prepararRepositorios();

        assertThrows(IllegalArgumentException.class, () -> service.crear(dto));
    }

    @Test
    void mesesGraciaDebeSerMenorAlPlazo() {
        CreditoFormDTO dto = base();
        dto.setTipoGracia(TipoGracia.GRACIA_TOTAL);
        dto.setMesesGracia(12);
        prepararRepositorios();

        assertThrows(IllegalArgumentException.class, () -> service.crear(dto));
    }

    @Test
    void tnaRequiereCapitalizacion() {
        CreditoFormDTO dto = base();
        dto.setTipoTasa(TipoTasa.TNA);
        dto.setCapitalizacion(null);
        prepararRepositorios();

        assertThrows(IllegalArgumentException.class, () -> service.crear(dto));
    }

    @Test
    void balloonNoPuedeExcederMontoFinanciado() {
        CreditoFormDTO dto = base();
        dto.setCuotaBalloon(10000.0);
        prepararRepositorios();

        assertThrows(IllegalArgumentException.class, () -> service.crear(dto));
    }

    private void prepararRepositorios() {
        when(clienteRepository.findById("c1")).thenReturn(Optional.of(Cliente.builder().id("c1").build()));
        when(vehiculoRepository.findById("v1")).thenReturn(Optional.of(Vehiculo.builder()
                .id("v1")
                .clienteId("c1")
                .marca("Toyota")
                .modelo("Yaris")
                .anio(2025)
                .condicion(CondicionVehiculo.NUEVO)
                .moneda(MonedaVehiculo.PEN)
                .precioVenta(12000.0)
                .cuotaInicial(2000.0)
                .build()));
    }

    private CreditoFormDTO base() {
        CreditoFormDTO dto = new CreditoFormDTO();
        dto.setClienteId("c1");
        dto.setVehiculoId("v1");
        dto.setMoneda(MonedaVehiculo.PEN);
        dto.setPrecioVehiculo(12000.0);
        dto.setCuotaInicial(2000.0);
        dto.setPlazoMeses(12);
        dto.setCuotaBalloon(0.0);
        dto.setTipoTasa(TipoTasa.TEA);
        dto.setValorTasa(18.0);
        dto.setCapitalizacion(Capitalizacion.MENSUAL);
        dto.setTipoGracia(TipoGracia.SIN_GRACIA);
        dto.setMesesGracia(0);
        dto.setSeguroVehicularAnual(0.0);
        dto.setSeguroDesgravamen(0.0);
        dto.setComisionGastos(0.0);
        dto.setTasaDescuentoVan(10.0);
        return dto;
    }
}
