package com.autocredit.autocreditbackend.modules.simulacion.service;

import com.autocredit.autocreditbackend.core.exception.ResourceNotFoundException;
import com.autocredit.autocreditbackend.modules.analisiscomparativo.service.ComparadorService;
import com.autocredit.autocreditbackend.modules.clientes.entity.Cliente;
import com.autocredit.autocreditbackend.modules.clientes.enums.EstadoCliente;
import com.autocredit.autocreditbackend.modules.clientes.repository.ClienteRepository;
import com.autocredit.autocreditbackend.modules.creditos.entity.Credito;
import com.autocredit.autocreditbackend.modules.creditos.repository.CreditoRepository;
import com.autocredit.autocreditbackend.modules.creditos.service.TasaConverterService;
import com.autocredit.autocreditbackend.modules.simulacion.dto.ParametrosCronogramaDTO;
import com.autocredit.autocreditbackend.modules.simulacion.entity.Cronograma;
import com.autocredit.autocreditbackend.modules.simulacion.entity.PeriodoPago;
import com.autocredit.autocreditbackend.modules.simulacion.repository.CronogramaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SimulacionService {

    private final MetodoFrancesService metodoFrancesService;
    private final TasaConverterService tasaConverterService;
    private final IndicadoresFinancierosService indicadoresFinancierosService;
    private final CronogramaRepository cronogramaRepository;
    private final CreditoRepository creditoRepository;
    private final ClienteRepository clienteRepository;
    private final ComparadorService comparadorService;

    public Cronograma generarCronogramaDeCredito(String creditoId) {
        Credito credito = creditoRepository.findById(creditoId)
                .orElseThrow(() -> new ResourceNotFoundException("Credito no encontrado"));

        double tem = tasaConverterService.calcularTem(
                credito.getTipoTasa(), credito.getValorTasa(), credito.getCapitalizacion()
        );

        ParametrosCronogramaDTO params = new ParametrosCronogramaDTO();
        params.setMontoAFinanciar(credito.getMontoAFinanciar());
        params.setTemPorcentual(tem);
        params.setPlazoMeses(credito.getPlazoMeses());
        params.setCuotaBalloon(credito.getCuotaBalloon() != null ? credito.getCuotaBalloon() : 0);
        params.setTipoGracia(credito.getTipoGracia());
        params.setMesesGracia(credito.getMesesGracia() != null ? credito.getMesesGracia() : 0);
        params.setSeguroVehicularAnual(credito.getSeguroVehicularAnual() != null ? credito.getSeguroVehicularAnual() : 0);
        params.setPrecioVehiculo(credito.getPrecioVehiculo());
        params.setSeguroDesgravamenPct(credito.getSeguroDesgravamen() != null ? credito.getSeguroDesgravamen() : 0);
        params.setComisionGastos(credito.getComisionGastos() != null ? credito.getComisionGastos() : 0);

        Cronograma cronograma = metodoFrancesService.generarCronograma(params);
        cronograma.setCreditoId(creditoId);

        return cronograma;
    }

    @Transactional
    public Cronograma guardarSimulacion(Cronograma cronograma) {
        if (cronograma.getCreditoId() == null || cronograma.getCreditoId().isBlank()) {
            throw new IllegalArgumentException("creditoId es obligatorio para guardar la simulacion");
        }
        if (!creditoRepository.existsById(cronograma.getCreditoId())) {
            throw new ResourceNotFoundException("Credito no encontrado");
        }

        Cronograma destino = cronogramaRepository.findByCreditoId(cronograma.getCreditoId()).orElse(cronograma);
        copiarCronograma(cronograma, destino);
        destino.setGuardado(true);

        Cronograma guardado = cronogramaRepository.save(destino);
        marcarClienteConCreditoActivo(guardado.getCreditoId());
        sincronizarConAnalisisComparativo(guardado);

        return guardado;
    }

    private void sincronizarConAnalisisComparativo(Cronograma cronograma) {
        Credito credito = creditoRepository.findById(cronograma.getCreditoId()).orElse(null);
        if (credito == null) {
            return;
        }

        Cliente cliente = clienteRepository.findById(credito.getClienteId()).orElse(null);
        if (cliente == null) {
            return;
        }

        Double tirEstimada = indicadoresFinancierosService.calcularTirMensual(cronograma);
        Double tceaEstimada = indicadoresFinancierosService.tirMensualATirAnual(tirEstimada);
        double cuotaOrdinaria = obtenerCuotaOrdinaria(cronograma);
        double porcentajeCuotaIngreso = cliente.getIngresoMensual() != null && cliente.getIngresoMensual() > 0
                ? (cuotaOrdinaria / cliente.getIngresoMensual()) * 100
                : 0;

        comparadorService.sincronizarDesdeCredito(
                cliente.getId(), credito.getId(), cliente.getNombres() + " " + cliente.getApellidos(),
                cliente.getEntidad() != null ? cliente.getEntidad() : "AutoCredit",
                credito.getMoneda(), cuotaOrdinaria, tceaEstimada, tirEstimada,
                cronograma.getCostoTotalCredito(), cronograma.getPlazoMeses(),
                credito.getTipoTasa(), credito.getValorTasa(), porcentajeCuotaIngreso
        );
    }

    public Optional<Cronograma> obtenerPorCredito(String creditoId) {
        return cronogramaRepository.findByCreditoId(creditoId);
    }

    public void eliminarPorCredito(String creditoId) {
        cronogramaRepository.deleteByCreditoId(creditoId);
    }

    private void marcarClienteConCreditoActivo(String creditoId) {
        Credito credito = creditoRepository.findById(creditoId).orElse(null);
        if (credito == null) {
            return;
        }

        Cliente cliente = clienteRepository.findById(credito.getClienteId()).orElse(null);
        if (cliente == null) {
            return;
        }

        cliente.setEstado(EstadoCliente.CREDITO_ACTIVO);
        clienteRepository.save(cliente);
    }

    private void copiarCronograma(Cronograma origen, Cronograma destino) {
        destino.setCreditoId(origen.getCreditoId());
        destino.setMontoFinanciado(origen.getMontoFinanciado());
        destino.setCuotaMensual(origen.getCuotaMensual());
        destino.setPlazoMeses(origen.getPlazoMeses());
        destino.setTem(origen.getTem());
        destino.setInteresTotal(origen.getInteresTotal());
        destino.setSeguroTotal(origen.getSeguroTotal());
        destino.setComisionTotal(origen.getComisionTotal());
        destino.setCostoTotalCredito(origen.getCostoTotalCredito());
        destino.setPeriodos(origen.getPeriodos());
        destino.setFechaGeneracion(origen.getFechaGeneracion());
    }

    private double obtenerCuotaOrdinaria(Cronograma cronograma) {
        return cronograma.getPeriodos().stream()
                .filter(periodo -> periodo.getBalloon() == null || periodo.getBalloon() == 0)
                .filter(periodo -> periodo.getCuotaBase() != null && periodo.getCuotaBase() > 0)
                .mapToDouble(PeriodoPago::getCuotaTotal)
                .findFirst()
                .orElse(cronograma.getCuotaMensual());
    }
}
