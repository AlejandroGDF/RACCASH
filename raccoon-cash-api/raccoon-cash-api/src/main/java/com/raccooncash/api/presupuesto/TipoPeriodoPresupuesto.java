package com.raccooncash.api.presupuesto;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TipoPeriodoPresupuesto {
    DIARIO,
    SEMANAL,
    MENSUAL,
    ANUAL,
    PERSONALIZADO;

    @JsonCreator
    public static TipoPeriodoPresupuesto desdeJson(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return switch (value.trim().toUpperCase()) {
            case "DAILY" -> DIARIO;
            case "WEEKLY" -> SEMANAL;
            case "MONTHLY" -> MENSUAL;
            case "YEARLY" -> ANUAL;
            case "CUSTOM" -> PERSONALIZADO;
            default -> TipoPeriodoPresupuesto.valueOf(value.trim().toUpperCase());
        };
    }
}
