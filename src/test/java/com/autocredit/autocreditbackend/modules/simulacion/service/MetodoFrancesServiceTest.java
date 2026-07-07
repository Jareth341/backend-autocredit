package com.autocredit.autocreditbackend.modules.simulacion.service;

import com.autocredit.autocreditbackend.modules.creditos.enums.TipoGracia;
import com.autocredit.autocreditbackend.modules.simulacion.dto.ParametrosCronogramaDTO;
import com.autocredit.autocreditbackend.modules.simulacion.entity.Cronograma;
import com.autocredit.autocreditbackend.modules.simulacion.entity.PeriodoPago;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetodoFrancesServiceTest {

    private final MetodoFrancesService service = new MetodoFrancesService();

    @Test
    void generaCreditoSinGraciaConSaldoFinalCero() {
        Cronograma cronograma = service.generarCronograma(parametros(TipoGracia.SIN_GRACIA, 0, 0));

        assertEquals(12, cronograma.getPeriodos().size());
        assertEquals(0.0, ultimo(cronograma).getSaldoFinal(), 0.01);
        assertTrue(cronograma.getCostoTotalCredito() > cronograma.getMontoFinanciado());
    }

    @Test
    void graciaTotalCapitalizaInteresSinCuotaBase() {
        Cronograma cronograma = service.generarCronograma(parametros(TipoGracia.GRACIA_TOTAL, 2, 0));
        PeriodoPago primero = cronograma.getPeriodos().get(0);

        assertEquals(0.0, primero.getCuotaBase(), 0.01);
        assertTrue(primero.getInteresCapitalizado() > 0);
        assertTrue(primero.getSaldoFinal() > primero.getSaldoInicial());
    }

    @Test
    void graciaParcialPagaInteresSinAmortizar() {
        Cronograma cronograma = service.generarCronograma(parametros(TipoGracia.GRACIA_PARCIAL, 2, 0));
        PeriodoPago primero = cronograma.getPeriodos().get(0);

        assertEquals(primero.getInteres(), primero.getCuotaBase(), 0.01);
        assertEquals(0.0, primero.getAmortizacion(), 0.01);
        assertEquals(primero.getSaldoInicial(), primero.getSaldoFinal(), 0.01);
    }

    @Test
    void balloonSePagaEnUltimoPeriodoYSaldoFinalCero() {
        Cronograma cronograma = service.generarCronograma(parametros(TipoGracia.SIN_GRACIA, 0, 2000));
        PeriodoPago ultimo = ultimo(cronograma);

        assertEquals(2000.0, ultimo.getBalloon(), 0.01);
        assertEquals(0.0, ultimo.getSaldoFinal(), 0.01);
        assertTrue(ultimo.getCuotaTotal() > ultimo.getCuotaBase());
    }

    private ParametrosCronogramaDTO parametros(TipoGracia tipoGracia, int mesesGracia, double balloon) {
        ParametrosCronogramaDTO params = new ParametrosCronogramaDTO();
        params.setMontoAFinanciar(10000);
        params.setTemPorcentual(1.0);
        params.setPlazoMeses(12);
        params.setCuotaBalloon(balloon);
        params.setTipoGracia(tipoGracia);
        params.setMesesGracia(mesesGracia);
        params.setSeguroVehicularAnual(0);
        params.setPrecioVehiculo(12000);
        params.setSeguroDesgravamenPct(0);
        params.setComisionGastos(0);
        return params;
    }

    private PeriodoPago ultimo(Cronograma cronograma) {
        return cronograma.getPeriodos().get(cronograma.getPeriodos().size() - 1);
    }
}
