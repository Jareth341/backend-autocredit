package com.autocredit.autocreditbackend.modules.alertasriesgo.service;

import com.autocredit.autocreditbackend.modules.alertasriesgo.dto.AlertaFinancieraDTO;
import com.autocredit.autocreditbackend.modules.alertasriesgo.dto.DatosParaAlertasDTO;
import com.autocredit.autocreditbackend.modules.creditos.enums.TipoGracia;
import com.autocredit.autocreditbackend.modules.vehiculos.enums.MonedaVehiculo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AlertasService {

    private static final double LIMITE_CUOTA_INGRESO_PCT = 35;
    private static final double LIMITE_BALLOON_MONTO_PCT = 20;
    private static final double LIMITE_TCEA_ALTA_PCT = 40;

    public List<AlertaFinancieraDTO> evaluarAlertas(DatosParaAlertasDTO datos) {
        List<AlertaFinancieraDTO> alertas = new ArrayList<>();

        alertas.add(evaluarCuotaIngreso(datos));
        alertas.add(evaluarBalloon(datos));
        alertas.add(evaluarRiesgoCambiario(datos));

        agregarSiAplica(alertas, evaluarGracia(datos));
        agregarSiAplica(alertas, evaluarTcea(datos));
        agregarSiAplica(alertas, evaluarCostoTotal(datos));
        agregarSiAplica(alertas, evaluarSegurosComisiones(datos));

        return alertas;
    }

    private AlertaFinancieraDTO evaluarCuotaIngreso(DatosParaAlertasDTO datos) {
        double ingreso = valor(datos.getIngresoMensualCliente());
        double cuota = valor(datos.getCuotaMensual());

        if (ingreso <= 0) {
            return new AlertaFinancieraDTO(
                    "WARN",
                    "Ingreso mensual no disponible",
                    "No se pudo calcular el ratio cuota/ingreso porque el ingreso es cero o no fue informado."
            );
        }

        double porcentaje = (cuota / ingreso) * 100;
        if (porcentaje > LIMITE_CUOTA_INGRESO_PCT) {
            return new AlertaFinancieraDTO(
                    "WARN",
                    String.format("Cuota representa el %.1f%% del ingreso mensual", porcentaje),
                    String.format("La cuota ordinaria %.2f supera el %.0f%% recomendado del ingreso declarado.",
                            cuota, LIMITE_CUOTA_INGRESO_PCT)
            );
        }
        return new AlertaFinancieraDTO(
                "OK",
                "Cuota dentro del rango saludable",
                String.format("La cuota ordinaria representa el %.1f%% del ingreso declarado.", porcentaje)
        );
    }

    private AlertaFinancieraDTO evaluarBalloon(DatosParaAlertasDTO datos) {
        double balloon = valor(datos.getCuotaBalloon());
        double montoFinanciado = valor(datos.getMontoFinanciado());

        if (balloon <= 0) {
            return new AlertaFinancieraDTO(
                    "OK",
                    "Sin cuota balloon",
                    "Esta simulacion no tiene pago final balloon."
            );
        }
        if (montoFinanciado <= 0) {
            return new AlertaFinancieraDTO(
                    "WARN",
                    "Monto financiado no disponible",
                    "No se pudo evaluar el peso del balloon porque falta el monto financiado."
            );
        }

        double porcentaje = (balloon / montoFinanciado) * 100;
        if (porcentaje > LIMITE_BALLOON_MONTO_PCT) {
            return new AlertaFinancieraDTO(
                    "WARN",
                    "Cuota final balloon elevada",
                    String.format("La cuota final %.2f representa el %.1f%% del monto financiado.", balloon, porcentaje)
            );
        }
        return new AlertaFinancieraDTO(
                "OK",
                "Balloon dentro de rango razonable",
                String.format("El balloon representa el %.1f%% del monto financiado.", porcentaje)
        );
    }

    private AlertaFinancieraDTO evaluarRiesgoCambiario(DatosParaAlertasDTO datos) {
        if (datos.getMonedaCredito() == MonedaVehiculo.USD) {
            return new AlertaFinancieraDTO(
                    "WARN",
                    "Riesgo cambiario",
                    "El credito esta en USD. Si los ingresos son en PEN, el tipo de cambio puede afectar la cuota."
            );
        }
        return new AlertaFinancieraDTO(
                "OK",
                "Sin riesgo cambiario directo",
                "El credito esta en PEN o no se identifico exposicion USD."
        );
    }

    private AlertaFinancieraDTO evaluarGracia(DatosParaAlertasDTO datos) {
        int mesesGracia = datos.getMesesGracia() != null ? datos.getMesesGracia() : 0;
        if (datos.getTipoGracia() == null || datos.getTipoGracia() == TipoGracia.SIN_GRACIA || mesesGracia <= 0) {
            return null;
        }

        if (datos.getTipoGracia() == TipoGracia.GRACIA_TOTAL) {
            return new AlertaFinancieraDTO(
                    "WARN",
                    "Gracia total capitaliza intereses",
                    String.format("Durante %d meses no se pagan intereses ni capital; los intereses se suman al saldo.", mesesGracia)
            );
        }

        return new AlertaFinancieraDTO(
                "WARN",
                "Gracia parcial no reduce capital",
                String.format("Durante %d meses se pagan intereses, pero el saldo de capital no baja.", mesesGracia)
        );
    }

    private AlertaFinancieraDTO evaluarTcea(DatosParaAlertasDTO datos) {
        if (datos.getTcea() == null) {
            return null;
        }
        if (datos.getTcea() > LIMITE_TCEA_ALTA_PCT) {
            return new AlertaFinancieraDTO(
                    "WARN",
                    "TCEA alta",
                    String.format("La TCEA %.2f%% supera el umbral referencial %.0f%%.", datos.getTcea(), LIMITE_TCEA_ALTA_PCT)
            );
        }
        return null;
    }

    private AlertaFinancieraDTO evaluarCostoTotal(DatosParaAlertasDTO datos) {
        double costoTotal = valor(datos.getCostoTotalCredito());
        double montoFinanciado = valor(datos.getMontoFinanciado());
        if (costoTotal <= 0 || montoFinanciado <= 0) {
            return null;
        }
        if (costoTotal > montoFinanciado * 1.5) {
            return new AlertaFinancieraDTO(
                    "WARN",
                    "Costo total elevado",
                    "El costo total del credito supera 1.5 veces el monto financiado."
            );
        }
        return null;
    }

    private AlertaFinancieraDTO evaluarSegurosComisiones(DatosParaAlertasDTO datos) {
        double extras = valor(datos.getSegurosComisiones());
        double costoTotal = valor(datos.getCostoTotalCredito());
        if (extras <= 0 || costoTotal <= 0) {
            return null;
        }
        double porcentaje = extras / costoTotal * 100;
        if (porcentaje > 10) {
            return new AlertaFinancieraDTO(
                    "WARN",
                    "Seguros y comisiones relevantes",
                    String.format("Seguros, comisiones y gastos representan %.1f%% del costo total.", porcentaje)
            );
        }
        return null;
    }

    private void agregarSiAplica(List<AlertaFinancieraDTO> alertas, AlertaFinancieraDTO alerta) {
        if (alerta != null) {
            alertas.add(alerta);
        }
    }

    private double valor(Double valor) {
        return valor != null ? valor : 0.0;
    }
}
