package com.raccooncash.api.presupuesto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TipoPeriodoPresupuestoConvertidor implements AttributeConverter<TipoPeriodoPresupuesto, String> {

    @Override
    public String convertToDatabaseColumn(TipoPeriodoPresupuesto attribute) {
        return attribute != null ? attribute.name() : null;
    }

    @Override
    public TipoPeriodoPresupuesto convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        return switch (dbData.trim().toUpperCase()) {
            case "DAILY" -> TipoPeriodoPresupuesto.DIARIO;
            case "WEEKLY" -> TipoPeriodoPresupuesto.SEMANAL;
            case "MONTHLY" -> TipoPeriodoPresupuesto.MENSUAL;
            case "YEARLY" -> TipoPeriodoPresupuesto.ANUAL;
            case "CUSTOM" -> TipoPeriodoPresupuesto.PERSONALIZADO;
            default -> TipoPeriodoPresupuesto.valueOf(dbData.trim().toUpperCase());
        };
    }
}
