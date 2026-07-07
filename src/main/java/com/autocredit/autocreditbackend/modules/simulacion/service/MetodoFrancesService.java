package com.autocredit.autocreditbackend.modules.simulacion.service;

import com.autocredit.autocreditbackend.modules.creditos.enums.TipoGracia;
import com.autocredit.autocreditbackend.modules.simulacion.dto.ParametrosCronogramaDTO;
import com.autocredit.autocreditbackend.modules.simulacion.entity.Cronograma;
import com.autocredit.autocreditbackend.modules.simulacion.entity.PeriodoPago;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MetodoFrancesService {

    public double calcularCuota(double montoAFinanciar, double temPorcentual, int plazoMeses, double balloon) {
        validarValoresBase(montoAFinanciar, temPorcentual, plazoMeses, balloon);

        double i = temPorcentual / 100;
        if (i == 0) {
            return (montoAFinanciar - balloon) / plazoMeses;
        }

        double factor = Math.pow(1 + i, plazoMeses);
        double valorPresenteBalloon = balloon / factor;
        return ((montoAFinanciar - valorPresenteBalloon) * i) / (1 - Math.pow(1 + i, -plazoMeses));
    }

    public Cronograma generarCronograma(ParametrosCronogramaDTO params) {
        double montoAFinanciar = params.getMontoAFinanciar();
        double temPorcentual = params.getTemPorcentual();
        int plazoMeses = params.getPlazoMeses();
        double cuotaBalloon = params.getCuotaBalloon();
        TipoGracia tipoGracia = params.getTipoGracia() != null ? params.getTipoGracia() : TipoGracia.SIN_GRACIA;
        int mesesGracia = params.getMesesGracia();
        double seguroVehicularAnual = params.getSeguroVehicularAnual();
        double precioVehiculo = params.getPrecioVehiculo();
        double seguroDesgravamenPct = params.getSeguroDesgravamenPct();
        double comisionGastos = params.getComisionGastos();

        validarParametros(params, tipoGracia);

        double i = temPorcentual / 100;
        double seguroMensualFijo = (seguroVehicularAnual / 100 * precioVehiculo) / 12;

        List<PeriodoPago> periodos = new ArrayList<>();
        double saldo = montoAFinanciar;
        int plazoEfectivoParaCuota = plazoMeses;
        double montoBaseParaCuota = montoAFinanciar;

        if (tipoGracia == TipoGracia.GRACIA_TOTAL && mesesGracia > 0) {
            montoBaseParaCuota = montoAFinanciar * Math.pow(1 + i, mesesGracia);
            plazoEfectivoParaCuota = plazoMeses - mesesGracia;
        } else if (tipoGracia == TipoGracia.GRACIA_PARCIAL && mesesGracia > 0) {
            plazoEfectivoParaCuota = plazoMeses - mesesGracia;
        }

        double cuotaNormal = calcularCuota(montoBaseParaCuota, temPorcentual, plazoEfectivoParaCuota, cuotaBalloon);

        for (int n = 1; n <= plazoMeses; n++) {
            double saldoInicial = saldo;
            double interes = saldoInicial * i;
            boolean enGracia = tipoGracia != TipoGracia.SIN_GRACIA && n <= mesesGracia;
            boolean esUltimoPeriodo = n == plazoMeses;

            double amortizacion = 0;
            double cuotaBase = 0;
            double interesCapitalizado = 0;
            double saldoFinal;

            if (enGracia && tipoGracia == TipoGracia.GRACIA_TOTAL) {
                interesCapitalizado = interes;
                saldoFinal = saldoInicial + interesCapitalizado;
            } else if (enGracia && tipoGracia == TipoGracia.GRACIA_PARCIAL) {
                cuotaBase = interes;
                saldoFinal = saldoInicial;
            } else if (esUltimoPeriodo) {
                amortizacion = Math.max(saldoInicial - cuotaBalloon, 0);
                cuotaBase = amortizacion + interes;
                saldoFinal = saldoInicial - amortizacion - cuotaBalloon;
            } else {
                cuotaBase = cuotaNormal;
                amortizacion = cuotaBase - interes;
                saldoFinal = saldoInicial - amortizacion;
            }

            if (Math.abs(saldoFinal) < 0.05 || esUltimoPeriodo) {
                saldoFinal = 0;
            }

            double seguro = seguroMensualFijo + (saldoInicial * seguroDesgravamenPct / 100);
            double comision = n == 1 ? comisionGastos : 0;
            double balloonPeriodo = esUltimoPeriodo ? cuotaBalloon : 0;
            double cuotaTotal = cuotaBase + seguro + comision + balloonPeriodo;

            periodos.add(PeriodoPago.builder()
                    .numero(n)
                    .estado(esUltimoPeriodo ? "ULTIMA_CUOTA" : enGracia ? "GRACIA" : "NORMAL")
                    .saldoInicial(redondear(saldoInicial))
                    .interes(redondear(interes))
                    .interesCapitalizado(redondear(interesCapitalizado))
                    .amortizacion(redondear(amortizacion))
                    .cuotaBase(redondear(cuotaBase))
                    .seguro(redondear(seguro))
                    .comision(redondear(comision))
                    .balloon(esUltimoPeriodo ? redondear(balloonPeriodo) : 0)
                    .cuotaTotal(redondear(cuotaTotal))
                    .saldoFinal(redondear(Math.max(saldoFinal, 0)))
                    .build());

            saldo = saldoFinal;
        }

        double interesTotal = periodos.stream().mapToDouble(PeriodoPago::getInteres).sum();
        double seguroTotal = periodos.stream().mapToDouble(PeriodoPago::getSeguro).sum();
        double comisionTotal = periodos.stream().mapToDouble(PeriodoPago::getComision).sum();
        double costoTotalCredito = periodos.stream().mapToDouble(PeriodoPago::getCuotaTotal).sum();

        return Cronograma.builder()
                .montoFinanciado(redondear(montoAFinanciar))
                .cuotaMensual(redondear(cuotaNormal))
                .plazoMeses(plazoMeses)
                .tem(redondear(temPorcentual))
                .interesTotal(redondear(interesTotal))
                .seguroTotal(redondear(seguroTotal))
                .comisionTotal(redondear(comisionTotal))
                .costoTotalCredito(redondear(costoTotalCredito))
                .periodos(periodos)
                .guardado(false)
                .fechaGeneracion(LocalDateTime.now())
                .build();
    }

    private void validarParametros(ParametrosCronogramaDTO params, TipoGracia tipoGracia) {
        validarValoresBase(params.getMontoAFinanciar(), params.getTemPorcentual(), params.getPlazoMeses(), params.getCuotaBalloon());
        if (params.getMesesGracia() < 0) {
            throw new IllegalArgumentException("Los meses de gracia no pueden ser negativos");
        }
        if (params.getMesesGracia() >= params.getPlazoMeses()) {
            throw new IllegalArgumentException("Los meses de gracia deben ser menores al plazo");
        }
        if (tipoGracia == TipoGracia.SIN_GRACIA && params.getMesesGracia() != 0) {
            throw new IllegalArgumentException("Si no hay gracia, los meses de gracia deben ser 0");
        }
        if (params.getSeguroVehicularAnual() < 0
                || params.getSeguroDesgravamenPct() < 0
                || params.getComisionGastos() < 0) {
            throw new IllegalArgumentException("Seguros, comisiones y gastos no pueden ser negativos");
        }
    }

    private void validarValoresBase(double montoAFinanciar, double temPorcentual, int plazoMeses, double balloon) {
        if (!Double.isFinite(montoAFinanciar) || montoAFinanciar <= 0) {
            throw new IllegalArgumentException("El monto financiado debe ser mayor a 0");
        }
        if (!Double.isFinite(temPorcentual) || temPorcentual < 0) {
            throw new IllegalArgumentException("La TEM debe ser mayor o igual a 0");
        }
        if (plazoMeses <= 0) {
            throw new IllegalArgumentException("El plazo debe ser mayor a 0");
        }
        if (!Double.isFinite(balloon) || balloon < 0 || balloon > montoAFinanciar) {
            throw new IllegalArgumentException("La cuota balloon debe estar entre 0 y el monto financiado");
        }
    }

    private double redondear(double valor) {
        if (!Double.isFinite(valor)) {
            throw new IllegalArgumentException("El cronograma genero un valor financiero invalido");
        }
        return Math.round(valor * 100) / 100.0;
    }
}
