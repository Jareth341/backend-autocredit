package com.autocredit.autocreditbackend.modules.clientes.service;

import com.autocredit.autocreditbackend.core.exception.DuplicateResourceException;
import com.autocredit.autocreditbackend.core.exception.ResourceNotFoundException;
import com.autocredit.autocreditbackend.modules.analisiscomparativo.service.ComparadorService;
import com.autocredit.autocreditbackend.modules.autenticacion.entity.Usuario;
import com.autocredit.autocreditbackend.modules.autenticacion.enums.Rol;
import com.autocredit.autocreditbackend.modules.clientes.dto.ClienteFormDTO;
import com.autocredit.autocreditbackend.modules.clientes.dto.ClienteListItemDTO;
import com.autocredit.autocreditbackend.modules.clientes.entity.Cliente;
import com.autocredit.autocreditbackend.modules.clientes.enums.EstadoCliente;
import com.autocredit.autocreditbackend.modules.clientes.enums.TipoDocumento;
import com.autocredit.autocreditbackend.modules.clientes.repository.ClienteRepository;
import com.autocredit.autocreditbackend.modules.creditos.service.CreditoService;
import com.autocredit.autocreditbackend.modules.simulacion.service.SimulacionService;
import com.autocredit.autocreditbackend.modules.vehiculos.service.VehiculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final VehiculoService vehiculoService;
    private final CreditoService creditoService;
    private final SimulacionService simulacionService;
    private final ComparadorService comparadorService;

    public List<ClienteListItemDTO> listar(String filtro, String estado) {
        List<Cliente> clientes;

        if (filtro != null && !filtro.isBlank()) {
            clientes = clienteRepository
                    .findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrNumeroDocumentoContainingIgnoreCaseOrCorreoContainingIgnoreCase(
                            filtro, filtro, filtro, filtro
                    );
        } else if (estado != null && !estado.isBlank()) {
            clientes = clienteRepository.findByEstado(EstadoCliente.valueOf(estado));
        } else {
            clientes = clienteRepository.findAll();
        }

        if (obtenerRolUsuarioActual() == Rol.ASESOR_FINANCIERO) {
            String idUsuarioActual = obtenerIdUsuarioActual();
            clientes = clientes.stream()
                    .filter(c -> Objects.equals(c.getAsesorId(), idUsuarioActual))
                    .toList();
        }

        return clientes.stream().map(this::aListItem).toList();
    }

    public Cliente obtenerPorId(String id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
    }

    public Cliente obtenerMisDatos() {
        String correoUsuarioActual = obtenerCorreoUsuarioActual();
        return clienteRepository.findByCorreo(correoUsuarioActual)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro un cliente asociado a tu cuenta. Contacta a un asesor."
                ));
    }

    public Cliente crear(ClienteFormDTO dto) {
        validarCliente(dto, null);

        Cliente cliente = mapearDesdeDTO(dto);
        cliente.setEstado(EstadoCliente.RECIEN_REGISTRADO);
        cliente.setAsesorId(obtenerIdUsuarioActual());
        cliente.setFechaRegistro(LocalDate.now());

        return clienteRepository.save(cliente);
    }

    public Cliente actualizar(String id, ClienteFormDTO dto) {
        Cliente cliente = obtenerPorId(id);
        validarCliente(dto, id);

        actualizarCamposDesdeDTO(cliente, dto);
        cliente.setFechaActualizacion(LocalDate.now());

        return clienteRepository.save(cliente);
    }

    public Cliente actualizarMisDatos(ClienteFormDTO dto) {
        Cliente cliente = obtenerMisDatos();
        validarCliente(dto, cliente.getId());
        actualizarCamposDesdeDTO(cliente, dto);
        cliente.setFechaActualizacion(LocalDate.now());
        return clienteRepository.save(cliente);
    }

    public void eliminar(String id) {
        Cliente cliente = obtenerPorId(id);
        if (cliente.getCreditoId() != null) {
            simulacionService.eliminarPorCredito(cliente.getCreditoId());
            comparadorService.eliminarPorCredito(cliente.getCreditoId());
        }
        vehiculoService.eliminarPorCliente(id);
        creditoService.eliminarPorCliente(id);
        clienteRepository.delete(cliente);
    }

    public boolean existeCorreoDuplicado(String correo, String idExcluir) {
        return idExcluir != null
                ? clienteRepository.existsByCorreoAndIdNot(correo, idExcluir)
                : clienteRepository.existsByCorreo(correo);
    }

    private void validarCliente(ClienteFormDTO dto, String idActual) {
        validarDocumento(dto);
        validarTelefono(dto.getTelefono());
        validarFechaNacimiento(dto.getFechaNacimiento());

        boolean correoDuplicado = idActual == null
                ? clienteRepository.existsByCorreo(dto.getCorreo())
                : clienteRepository.existsByCorreoAndIdNot(dto.getCorreo(), idActual);
        if (correoDuplicado) {
            throw new DuplicateResourceException("CORREO_DUPLICADO");
        }

        boolean documentoDuplicado = idActual == null
                ? clienteRepository.existsByNumeroDocumento(dto.getNumeroDocumento())
                : clienteRepository.existsByNumeroDocumentoAndIdNot(dto.getNumeroDocumento(), idActual);
        if (documentoDuplicado) {
            throw new DuplicateResourceException("DOCUMENTO_DUPLICADO");
        }
    }

    private void validarDocumento(ClienteFormDTO dto) {
        if (dto.getTipoDocumento() == TipoDocumento.DNI && !dto.getNumeroDocumento().matches("\\d{8}")) {
            throw new IllegalArgumentException("El DNI debe tener 8 digitos");
        }
        if (dto.getTipoDocumento() == TipoDocumento.RUC && !dto.getNumeroDocumento().matches("\\d{11}")) {
            throw new IllegalArgumentException("El RUC debe tener 11 digitos");
        }
    }

    private void validarTelefono(String telefono) {
        if (telefono == null || !telefono.matches("[0-9+()\\-\\s]{6,20}")) {
            throw new IllegalArgumentException("El telefono no tiene un formato valido");
        }
    }

    private void validarFechaNacimiento(LocalDate fechaNacimiento) {
        if (fechaNacimiento != null && fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede estar en el futuro");
        }
    }

    private Cliente mapearDesdeDTO(ClienteFormDTO dto) {
        return Cliente.builder()
                .tipoDocumento(dto.getTipoDocumento())
                .numeroDocumento(dto.getNumeroDocumento())
                .nombres(dto.getNombres())
                .apellidos(dto.getApellidos())
                .fechaNacimiento(dto.getFechaNacimiento())
                .genero(dto.getGenero())
                .estadoCivil(dto.getEstadoCivil())
                .direccion(dto.getDireccion())
                .telefono(dto.getTelefono())
                .correo(dto.getCorreo())
                .situacionLaboral(dto.getSituacionLaboral())
                .ingresoMensual(dto.getIngresoMensual())
                .observaciones(dto.getObservaciones())
                .build();
    }

    private void actualizarCamposDesdeDTO(Cliente cliente, ClienteFormDTO dto) {
        cliente.setTipoDocumento(dto.getTipoDocumento());
        cliente.setNumeroDocumento(dto.getNumeroDocumento());
        cliente.setNombres(dto.getNombres());
        cliente.setApellidos(dto.getApellidos());
        cliente.setFechaNacimiento(dto.getFechaNacimiento());
        cliente.setGenero(dto.getGenero());
        cliente.setEstadoCivil(dto.getEstadoCivil());
        cliente.setDireccion(dto.getDireccion());
        cliente.setTelefono(dto.getTelefono());
        cliente.setCorreo(dto.getCorreo());
        cliente.setSituacionLaboral(dto.getSituacionLaboral());
        cliente.setIngresoMensual(dto.getIngresoMensual());
        cliente.setObservaciones(dto.getObservaciones());
    }

    private ClienteListItemDTO aListItem(Cliente c) {
        return new ClienteListItemDTO(
                c.getId(), c.getNombres(), c.getApellidos(), c.getNumeroDocumento(),
                c.getCorreo(), c.getTelefono(), c.getVehiculoAsociado(),
                c.getIngresoMensual(), c.getEstado(), c.getFechaRegistro()
        );
    }

    private Rol obtenerRolUsuarioActual() {
        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return usuario.getRol();
    }

    private String obtenerIdUsuarioActual() {
        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return usuario.getId();
    }

    private String obtenerCorreoUsuarioActual() {
        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return usuario.getCorreo();
    }
}
