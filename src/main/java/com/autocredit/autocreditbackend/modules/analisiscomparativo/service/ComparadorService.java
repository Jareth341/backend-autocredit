package com.autocredit.autocreditbackend.modules.analisiscomparativo.service;

import com.autocredit.autocreditbackend.core.exception.ResourceNotFoundException;
import com.autocredit.autocreditbackend.modules.analisiscomparativo.dto.ComparacionEscenariosDTO;
import com.autocredit.autocreditbackend.modules.analisiscomparativo.entity.SimulacionGuardada;
import com.autocredit.autocreditbackend.modules.analisiscomparativo.repository.SimulacionGuardadaRepository;
import com.autocredit.autocreditbackend.modules.creditos.enums.TipoTasa;
import com.autocredit.autocreditbackend.modules.vehiculos.enums.MonedaVehiculo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ComparadorService {

    private final SimulacionGuardadaRepository repository;

    public List<SimulacionGuardada> listar(String filtro) {
        if (filtro != null && !filtro.isBlank()) {
            return repository.findByClienteNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(filtro, filtro);
        }
        return repository.findAll();
    }

    public SimulacionGuardada obtenerPorId(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Simulacion no encontrada"));
    }

    public ComparacionEscenariosDTO compararEscenarios(String idA, String idB) {
        SimulacionGuardada escenarioA = obtenerPorId(idA);
        SimulacionGuardada escenarioB = obtenerPorId(idB);

        if (escenarioA.getMoneda() != escenarioB.getMoneda()) {
            return new ComparacionEscenariosDTO(
                    escenarioA,
                    escenarioB,
                    null,
                    false,
                    "MONEDAS_NO_COMPARABLES",
                    "No se elige mejor opcion porque las monedas son distintas. Use un tipo de cambio referencial para normalizar montos."
            );
        }

        if (escenarioA.getTcea() == null || escenarioB.getTcea() == null) {
            return new ComparacionEscenariosDTO(
                    escenarioA,
                    escenarioB,
                    null,
                    true,
                    "TCEA_NO_DISPONIBLE",
                    "No se elige mejor opcion porque una TCEA no esta disponible."
            );
        }

        String mejorOpcionId = escenarioA.getTcea() <= escenarioB.getTcea() ? escenarioA.getId() : escenarioB.getId();
        return new ComparacionEscenariosDTO(escenarioA, escenarioB, mejorOpcionId, true, null, null);
    }

    public SimulacionGuardada duplicar(String id) {
        SimulacionGuardada original = obtenerPorId(id);

        SimulacionGuardada copia = SimulacionGuardada.builder()
                .codigo(generarCodigo())
                .clienteId(original.getClienteId())
                .creditoId(original.getCreditoId())
                .clienteNombre(original.getClienteNombre())
                .entidad(original.getEntidad())
                .moneda(original.getMoneda())
                .cuotaMensual(original.getCuotaMensual())
                .tcea(original.getTcea())
                .tirMensual(original.getTirMensual())
                .costoTotalCredito(original.getCostoTotalCredito())
                .plazoMeses(original.getPlazoMeses())
                .nivelRiesgo(original.getNivelRiesgo())
                .tipoTasa(original.getTipoTasa())
                .valorTasa(original.getValorTasa())
                .fecha(LocalDate.now())
                .estado("GUARDADA")
                .build();

        return repository.save(copia);
    }

    public void sincronizarDesdeCredito(
            String clienteId, String creditoId, String clienteNombre, String entidad,
            MonedaVehiculo moneda, double cuotaMensual, Double tcea, Double tirMensual,
            double costoTotalCredito, int plazoMeses, TipoTasa tipoTasa,
            double valorTasa, double porcentajeCuotaIngreso
    ) {
        SimulacionGuardada simulacion = repository.findByCreditoId(creditoId)
                .orElseGet(() -> SimulacionGuardada.builder()
                        .codigo(generarCodigo())
                        .creditoId(creditoId)
                        .build());

        String nivelRiesgo = porcentajeCuotaIngreso > 35 ? "ALTO" : porcentajeCuotaIngreso > 25 ? "MODERADO" : "BAJO";

        simulacion.setClienteId(clienteId);
        simulacion.setCreditoId(creditoId);
        simulacion.setClienteNombre(clienteNombre);
        simulacion.setEntidad(entidad);
        simulacion.setMoneda(moneda);
        simulacion.setCuotaMensual(cuotaMensual);
        simulacion.setTcea(tcea);
        simulacion.setTirMensual(tirMensual);
        simulacion.setCostoTotalCredito(costoTotalCredito);
        simulacion.setPlazoMeses(plazoMeses);
        simulacion.setNivelRiesgo(nivelRiesgo);
        simulacion.setTipoTasa(tipoTasa);
        simulacion.setValorTasa(valorTasa);
        simulacion.setFecha(LocalDate.now());
        simulacion.setEstado("GUARDADA");

        repository.save(simulacion);
    }

    public void eliminar(String id) {
        SimulacionGuardada simulacion = obtenerPorId(id);
        repository.delete(simulacion);
    }

    public void eliminarPorCredito(String creditoId) {
        repository.deleteByCreditoId(creditoId);
    }

    private String generarCodigo() {
        return "#SIM-" + (1000 + new Random().nextInt(9000));
    }
}
