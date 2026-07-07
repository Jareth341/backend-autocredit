package com.autocredit.autocreditbackend.modules.simulacion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IndicadoresFinancierosDTO {
    private double van;
    private Double tirMensual;
    private Double tirAnual;
    private Double tcea;
    private double interesTotal;
    private double seguroTotal;
    private double comisionTotal;
    private double costoTotalCredito;
}
