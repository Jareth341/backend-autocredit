package com.autocredit.autocreditbackend.modules.creditos.service;

import com.autocredit.autocreditbackend.modules.creditos.enums.Capitalizacion;
import com.autocredit.autocreditbackend.modules.creditos.enums.TipoTasa;
import org.springframework.stereotype.Service;

@Service
public class TasaConverterService {

    public double tnaATea(double tnaPorcentual, Capitalizacion capitalizacion) {
        if (capitalizacion == null) {
            throw new IllegalArgumentException("La capitalizacion es requerida cuando la tasa es TNA");
        }
        validarTasa(tnaPorcentual);
        double tna = tnaPorcentual / 100;
        double m = 360.0 / capitalizacion.getDias();
        double tea = Math.pow(1 + tna / m, m) - 1;
        return tea * 100;
    }

    public double teaATem(double teaPorcentual) {
        validarTasa(teaPorcentual);
        double tea = teaPorcentual / 100;
        double tem = Math.pow(1 + tea, 1.0 / 12) - 1;
        return tem * 100;
    }

    public double calcularTem(TipoTasa tipoTasa, double valorTasa, Capitalizacion capitalizacion) {
        if (tipoTasa == null) {
            throw new IllegalArgumentException("El tipo de tasa es obligatorio");
        }
        validarTasa(valorTasa);
        if (tipoTasa == TipoTasa.TNA) {
            double tea = tnaATea(valorTasa, capitalizacion);
            return teaATem(tea);
        }
        return teaATem(valorTasa);
    }

    public double temATeaAnual(double temPorcentual) {
        validarTasa(temPorcentual);
        double tem = temPorcentual / 100;
        double tea = Math.pow(1 + tem, 12) - 1;
        return tea * 100;
    }

    private void validarTasa(double tasa) {
        if (!Double.isFinite(tasa) || tasa < 0) {
            throw new IllegalArgumentException("La tasa debe ser un numero mayor o igual a 0");
        }
    }
}
