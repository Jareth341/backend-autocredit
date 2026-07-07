package com.autocredit.autocreditbackend.modules.tipocambio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class TipoCambioResponseDTO {
    private String base;
    private String target;
    private Double rate;
    private String source;
    private OffsetDateTime updatedAt;
    private String message;
}
