package com.autocredit.autocreditbackend.modules.alertasriesgo.dto;

import com.autocredit.autocreditbackend.modules.creditos.enums.TipoGracia;
import com.autocredit.autocreditbackend.modules.vehiculos.enums.MonedaVehiculo;
import lombok.Data;

@Data
public class DatosParaAlertasDTO {
    private Double cuotaMensual;
    private Double ingresoMensualCliente;
    private Double cuotaBalloon;
    private Double montoFinanciado;
    private MonedaVehiculo monedaCredito;
    private TipoGracia tipoGracia;
    private Integer mesesGracia;
    private Double tcea;
    private Double costoTotalCredito;
    private Double segurosComisiones;
}
