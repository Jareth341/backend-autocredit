package com.autocredit.autocreditbackend.modules.alertasriesgo.service;

import com.autocredit.autocreditbackend.modules.alertasriesgo.dto.AlertaFinancieraDTO;
import com.autocredit.autocreditbackend.modules.alertasriesgo.dto.DatosParaAlertasDTO;
import com.autocredit.autocreditbackend.modules.creditos.enums.TipoGracia;
import com.autocredit.autocreditbackend.modules.vehiculos.enums.MonedaVehiculo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AlertasServiceTest {

    private final AlertasService service = new AlertasService();

    @Test
    void alertaCuotaIngresoAltoBalloonUsdGraciaYTcea() {
        DatosParaAlertasDTO datos = base();
        datos.setCuotaMensual(500.0);
        datos.setIngresoMensualCliente(1000.0);
        datos.setCuotaBalloon(3000.0);
        datos.setMonedaCredito(MonedaVehiculo.USD);
        datos.setTipoGracia(TipoGracia.GRACIA_TOTAL);
        datos.setMesesGracia(2);
        datos.setTcea(45.0);

        List<AlertaFinancieraDTO> alertas = service.evaluarAlertas(datos);

        assertTrue(contiene(alertas, "Cuota representa"));
        assertTrue(contiene(alertas, "balloon elevada"));
        assertTrue(contiene(alertas, "Riesgo cambiario"));
        assertTrue(contiene(alertas, "capitaliza intereses"));
        assertTrue(contiene(alertas, "TCEA alta"));
    }

    @Test
    void sinAlertasWarnParaEscenarioSano() {
        DatosParaAlertasDTO datos = base();

        List<AlertaFinancieraDTO> alertas = service.evaluarAlertas(datos);

        assertTrue(alertas.stream().noneMatch(alerta -> "WARN".equals(alerta.getTipo())));
    }

    private DatosParaAlertasDTO base() {
        DatosParaAlertasDTO datos = new DatosParaAlertasDTO();
        datos.setCuotaMensual(100.0);
        datos.setIngresoMensualCliente(3000.0);
        datos.setCuotaBalloon(0.0);
        datos.setMontoFinanciado(10000.0);
        datos.setMonedaCredito(MonedaVehiculo.PEN);
        datos.setTipoGracia(TipoGracia.SIN_GRACIA);
        datos.setMesesGracia(0);
        datos.setTcea(20.0);
        datos.setCostoTotalCredito(11000.0);
        datos.setSegurosComisiones(200.0);
        return datos;
    }

    private boolean contiene(List<AlertaFinancieraDTO> alertas, String texto) {
        return alertas.stream().anyMatch(alerta ->
                alerta.getTitulo().contains(texto) || alerta.getDescripcion().contains(texto));
    }
}
