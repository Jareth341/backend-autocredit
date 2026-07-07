package com.autocredit.autocreditbackend.modules.creditos.service;

import com.autocredit.autocreditbackend.core.exception.ResourceNotFoundException;
import com.autocredit.autocreditbackend.modules.analisiscomparativo.repository.SimulacionGuardadaRepository;
import com.autocredit.autocreditbackend.modules.clientes.entity.Cliente;
import com.autocredit.autocreditbackend.modules.clientes.enums.EstadoCliente;
import com.autocredit.autocreditbackend.modules.clientes.repository.ClienteRepository;
import com.autocredit.autocreditbackend.modules.creditos.dto.CreditoFormDTO;
import com.autocredit.autocreditbackend.modules.creditos.entity.Credito;
import com.autocredit.autocreditbackend.modules.creditos.enums.TipoGracia;
import com.autocredit.autocreditbackend.modules.creditos.enums.TipoTasa;
import com.autocredit.autocreditbackend.modules.creditos.repository.CreditoRepository;
import com.autocredit.autocreditbackend.modules.simulacion.repository.CronogramaRepository;
import com.autocredit.autocreditbackend.modules.vehiculos.entity.Vehiculo;
import com.autocredit.autocreditbackend.modules.vehiculos.repository.VehiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CreditoService {

    private final CreditoRepository creditoRepository;
    private final ClienteRepository clienteRepository;
    private final VehiculoRepository vehiculoRepository;
    private final TasaConverterService tasaConverterService;
    private final CronogramaRepository cronogramaRepository;
    private final SimulacionGuardadaRepository simulacionGuardadaRepository;

    public List<Credito> listar() {
        return creditoRepository.findAll();
    }

    public Credito obtenerPorId(String id) {
        return creditoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credito no encontrado"));
    }

    public Optional<Credito> obtenerPorCliente(String clienteId) {
        return creditoRepository.findByClienteId(clienteId);
    }

    public Credito crear(CreditoFormDTO dto) {
        Cliente cliente = obtenerCliente(dto.getClienteId());
        Vehiculo vehiculo = obtenerVehiculo(dto.getVehiculoId());
        validarCredito(dto, vehiculo);

        double montoAFinanciar = dto.getPrecioVehiculo() - dto.getCuotaInicial();
        double tem = tasaConverterService.calcularTem(dto.getTipoTasa(), dto.getValorTasa(), dto.getCapitalizacion());

        Credito credito = mapearDesdeDTO(dto);
        credito.setMontoAFinanciar(montoAFinanciar);
        credito.setTasaEfectivaMensual(tem);
        credito.setFechaRegistro(LocalDate.now());

        Credito guardado = creditoRepository.save(credito);
        marcarClienteEnSimulacion(cliente, guardado.getId());

        return guardado;
    }

    public Credito actualizar(String id, CreditoFormDTO dto) {
        Credito credito = obtenerPorId(id);
        Vehiculo vehiculo = obtenerVehiculo(dto.getVehiculoId());
        validarCredito(dto, vehiculo);

        double montoAFinanciar = dto.getPrecioVehiculo() - dto.getCuotaInicial();
        double tem = tasaConverterService.calcularTem(dto.getTipoTasa(), dto.getValorTasa(), dto.getCapitalizacion());

        actualizarCamposDesdeDTO(credito, dto);
        credito.setMontoAFinanciar(montoAFinanciar);
        credito.setTasaEfectivaMensual(tem);

        Credito actualizado = creditoRepository.save(credito);
        marcarClienteEnSimulacion(obtenerCliente(dto.getClienteId()), actualizado.getId());
        return actualizado;
    }

    public void eliminar(String id) {
        Credito credito = obtenerPorId(id);
        cronogramaRepository.deleteByCreditoId(id);
        simulacionGuardadaRepository.deleteByCreditoId(id);
        clienteRepository.findById(credito.getClienteId()).ifPresent(cliente -> {
            cliente.setCreditoId(null);
            if (cliente.getVehiculoId() == null) {
                cliente.setEstado(EstadoCliente.RECIEN_REGISTRADO);
            }
            clienteRepository.save(cliente);
        });
        creditoRepository.delete(credito);
    }

    public void eliminarPorCliente(String clienteId) {
        creditoRepository.findByClienteId(clienteId).ifPresent(credito -> {
            cronogramaRepository.deleteByCreditoId(credito.getId());
            simulacionGuardadaRepository.deleteByCreditoId(credito.getId());
        });
        creditoRepository.deleteByClienteId(clienteId);
    }

    private Cliente obtenerCliente(String clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
    }

    private Vehiculo obtenerVehiculo(String vehiculoId) {
        return vehiculoRepository.findById(vehiculoId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehiculo no encontrado"));
    }

    private void validarCredito(CreditoFormDTO dto, Vehiculo vehiculo) {
        if (!vehiculo.getClienteId().equals(dto.getClienteId())) {
            throw new IllegalArgumentException("El vehiculo no pertenece al cliente indicado");
        }
        if (vehiculo.getMoneda() != dto.getMoneda()) {
            throw new IllegalArgumentException("La moneda del credito debe coincidir con la moneda del vehiculo");
        }
        if (dto.getCuotaInicial() >= dto.getPrecioVehiculo()) {
            throw new IllegalArgumentException("La cuota inicial debe ser menor al precio del vehiculo");
        }

        double montoAFinanciar = dto.getPrecioVehiculo() - dto.getCuotaInicial();
        double balloon = valorNoNulo(dto.getCuotaBalloon());
        if (balloon > montoAFinanciar) {
            throw new IllegalArgumentException("La cuota balloon no puede exceder el monto a financiar");
        }

        int mesesGracia = dto.getMesesGracia() != null ? dto.getMesesGracia() : 0;
        if (mesesGracia >= dto.getPlazoMeses()) {
            throw new IllegalArgumentException("Los meses de gracia deben ser menores al plazo");
        }
        if (dto.getTipoGracia() == TipoGracia.SIN_GRACIA && mesesGracia != 0) {
            throw new IllegalArgumentException("Si no hay gracia, los meses de gracia deben ser 0");
        }
        if (dto.getTipoTasa() == TipoTasa.TNA && dto.getCapitalizacion() == null) {
            throw new IllegalArgumentException("La capitalizacion es obligatoria cuando la tasa es TNA");
        }
        if (valorNoNulo(dto.getSeguroVehicularAnual()) < 0
                || valorNoNulo(dto.getSeguroDesgravamen()) < 0
                || valorNoNulo(dto.getComisionGastos()) < 0) {
            throw new IllegalArgumentException("Seguros, comisiones y gastos no pueden ser negativos");
        }
        if (dto.getTasaDescuentoVan() < 0) {
            throw new IllegalArgumentException("La tasa de descuento VAN no puede ser negativa");
        }
    }

    private void marcarClienteEnSimulacion(Cliente cliente, String creditoId) {
        cliente.setCreditoId(creditoId);
        cliente.setEstado(EstadoCliente.EN_SIMULACION);
        clienteRepository.save(cliente);
    }

    private Credito mapearDesdeDTO(CreditoFormDTO dto) {
        return Credito.builder()
                .clienteId(dto.getClienteId())
                .vehiculoId(dto.getVehiculoId())
                .moneda(dto.getMoneda())
                .precioVehiculo(dto.getPrecioVehiculo())
                .cuotaInicial(dto.getCuotaInicial())
                .plazoMeses(dto.getPlazoMeses())
                .cuotaBalloon(valorNoNulo(dto.getCuotaBalloon()))
                .tipoTasa(dto.getTipoTasa())
                .valorTasa(dto.getValorTasa())
                .capitalizacion(dto.getTipoTasa() == TipoTasa.TNA ? dto.getCapitalizacion() : null)
                .tipoGracia(dto.getTipoGracia())
                .mesesGracia(dto.getMesesGracia() != null ? dto.getMesesGracia() : 0)
                .seguroVehicularAnual(valorNoNulo(dto.getSeguroVehicularAnual()))
                .seguroDesgravamen(valorNoNulo(dto.getSeguroDesgravamen()))
                .comisionGastos(valorNoNulo(dto.getComisionGastos()))
                .tasaDescuentoVan(dto.getTasaDescuentoVan())
                .build();
    }

    private void actualizarCamposDesdeDTO(Credito credito, CreditoFormDTO dto) {
        credito.setClienteId(dto.getClienteId());
        credito.setVehiculoId(dto.getVehiculoId());
        credito.setMoneda(dto.getMoneda());
        credito.setPrecioVehiculo(dto.getPrecioVehiculo());
        credito.setCuotaInicial(dto.getCuotaInicial());
        credito.setPlazoMeses(dto.getPlazoMeses());
        credito.setCuotaBalloon(valorNoNulo(dto.getCuotaBalloon()));
        credito.setTipoTasa(dto.getTipoTasa());
        credito.setValorTasa(dto.getValorTasa());
        credito.setCapitalizacion(dto.getTipoTasa() == TipoTasa.TNA ? dto.getCapitalizacion() : null);
        credito.setTipoGracia(dto.getTipoGracia());
        credito.setMesesGracia(dto.getMesesGracia() != null ? dto.getMesesGracia() : 0);
        credito.setSeguroVehicularAnual(valorNoNulo(dto.getSeguroVehicularAnual()));
        credito.setSeguroDesgravamen(valorNoNulo(dto.getSeguroDesgravamen()));
        credito.setComisionGastos(valorNoNulo(dto.getComisionGastos()));
        credito.setTasaDescuentoVan(dto.getTasaDescuentoVan());
    }

    private double valorNoNulo(Double valor) {
        return valor != null ? valor : 0.0;
    }
}
