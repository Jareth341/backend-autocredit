package com.autocredit.autocreditbackend.modules.simulacion.service;

import com.autocredit.autocreditbackend.modules.simulacion.dto.IndicadoresFinancierosDTO;
import com.autocredit.autocreditbackend.modules.simulacion.entity.Cronograma;
import com.autocredit.autocreditbackend.modules.simulacion.entity.PeriodoPago;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class IndicadoresFinancierosService {

    private List<Double> construirFlujoCaja(Cronograma cronograma) {
        List<Double> flujo = new ArrayList<>();
        flujo.add(cronograma.getMontoFinanciado());
        for (PeriodoPago periodo : cronograma.getPeriodos()) {
            flujo.add(-periodo.getCuotaTotal());
        }
        return flujo;
    }

    public double calcularVan(Cronograma cronograma, double tasaDescuentoAnualPct) {
        if (tasaDescuentoAnualPct < 0 || !Double.isFinite(tasaDescuentoAnualPct)) {
            throw new IllegalArgumentException("La tasa de descuento VAN no puede ser negativa");
        }

        List<Double> flujo = construirFlujoCaja(cronograma);
        double tasaMensual = tasaDescuentoAnualPct == 0
                ? 0
                : Math.pow(1 + tasaDescuentoAnualPct / 100, 1.0 / 12) - 1;

        double van = 0;
        for (int periodo = 0; periodo < flujo.size(); periodo++) {
            van += flujo.get(periodo) / Math.pow(1 + tasaMensual, periodo);
        }
        return redondearMoneda(van);
    }

    public Double calcularTirMensual(Cronograma cronograma) {
        Double tirDecimal = calcularTirMensualDecimal(cronograma);
        return tirDecimal == null ? null : redondearPorcentaje(tirDecimal * 100);
    }

    public Double tirMensualATirAnual(Double tirMensualPct) {
        if (tirMensualPct == null) {
            return null;
        }
        double tem = tirMensualPct / 100;
        if (tem <= -1 || !Double.isFinite(tem)) {
            return null;
        }
        double tea = Math.pow(1 + tem, 12) - 1;
        return redondearPorcentaje(tea * 100);
    }

    public Double calcularTcea(Cronograma cronograma) {
        return tirMensualATirAnual(calcularTirMensual(cronograma));
    }

    public IndicadoresFinancierosDTO calcularIndicadoresCompletos(Cronograma cronograma, double tasaDescuentoAnualPct) {
        Double tirMensual = calcularTirMensual(cronograma);
        Double tirAnual = tirMensualATirAnual(tirMensual);
        return new IndicadoresFinancierosDTO(
                calcularVan(cronograma, tasaDescuentoAnualPct),
                tirMensual,
                tirAnual,
                tirAnual,
                cronograma.getInteresTotal(),
                cronograma.getSeguroTotal(),
                cronograma.getComisionTotal(),
                cronograma.getCostoTotalCredito()
        );
    }

    private Double calcularTirMensualDecimal(Cronograma cronograma) {
        List<Double> flujo = construirFlujoCaja(cronograma);
        boolean hayPositivo = flujo.stream().anyMatch(valor -> valor > 0);
        boolean hayNegativo = flujo.stream().anyMatch(valor -> valor < 0);
        if (!hayPositivo || !hayNegativo) {
            return null;
        }

        double bajo = -0.9999;
        double alto = 10.0;
        double fBajo = vpn(flujo, bajo);
        double fAlto = vpn(flujo, alto);

        int expansiones = 0;
        while (mismoSigno(fBajo, fAlto) && expansiones < 20) {
            alto *= 2;
            fAlto = vpn(flujo, alto);
            expansiones++;
        }

        if (!Double.isFinite(fBajo) || !Double.isFinite(fAlto) || mismoSigno(fBajo, fAlto)) {
            return null;
        }

        for (int i = 0; i < 200; i++) {
            double medio = (bajo + alto) / 2;
            double fMedio = vpn(flujo, medio);
            if (!Double.isFinite(fMedio)) {
                return null;
            }
            if (Math.abs(fMedio) < 1e-7) {
                return medio;
            }
            if (mismoSigno(fBajo, fMedio)) {
                bajo = medio;
                fBajo = fMedio;
            } else {
                alto = medio;
            }
        }

        return (bajo + alto) / 2;
    }

    private double vpn(List<Double> flujo, double tasa) {
        double total = 0;
        for (int periodo = 0; periodo < flujo.size(); periodo++) {
            total += flujo.get(periodo) / Math.pow(1 + tasa, periodo);
        }
        return total;
    }

    private boolean mismoSigno(double a, double b) {
        return Math.signum(a) == Math.signum(b);
    }

    private double redondearMoneda(double valor) {
        if (!Double.isFinite(valor)) {
            throw new IllegalArgumentException("El indicador financiero produjo un valor invalido");
        }
        return Math.round(valor * 100) / 100.0;
    }

    private Double redondearPorcentaje(double valor) {
        if (!Double.isFinite(valor)) {
            return null;
        }
        return Math.round(valor * 100) / 100.0;
    }
}
