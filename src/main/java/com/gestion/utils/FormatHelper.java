package com.gestion.utils;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class FormatHelper {

    private static final DateTimeFormatter FMT_DISPLAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_ISO     = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final NumberFormat      NUM_FORMAT  = NumberFormat.getCurrencyInstance(Locale.of("es", "AR"));

    private FormatHelper() {}

    // ── Fechas ────────────────────────────────────────────────
    /** "2025-05-07" → "07/05/2025" */
    public static String isoADisplay(String isoDate) {
        if (isoDate == null || isoDate.isBlank()) return "";
        try {
            // La API puede devolver "2025-05-07" o "2025-05-07T00:00:00.000Z"
            String clean = isoDate.length() > 10 ? isoDate.substring(0, 10) : isoDate;
            return LocalDate.parse(clean, FMT_ISO).format(FMT_DISPLAY);
        } catch (Exception e) {
            return isoDate;
        }
    }

    /** "07/05/2025" → "2025-05-07" */
    public static String displayAIso(String displayDate) {
        if (displayDate == null || displayDate.isBlank()) return null;
        try {
            return LocalDate.parse(displayDate, FMT_DISPLAY).format(FMT_ISO);
        } catch (Exception e) {
            return displayDate;
        }
    }

    /** LocalDate → "yyyy-MM-dd" */
    public static String localDateAIso(LocalDate date) {
        return date == null ? null : date.format(FMT_ISO);
    }

    // ── Edad ──────────────────────────────────────────────────
    /** Calcula edad a partir de fecha ISO */
    public static int calcularEdad(String fechaNacimientoIso) {
        if (fechaNacimientoIso == null || fechaNacimientoIso.isBlank()) return 0;
        try {
            String clean = fechaNacimientoIso.length() > 10
                    ? fechaNacimientoIso.substring(0, 10)
                    : fechaNacimientoIso;
            return Period.between(LocalDate.parse(clean, FMT_ISO), LocalDate.now()).getYears();
        } catch (Exception e) {
            return 0;
        }
    }

    // ── Montos ────────────────────────────────────────────────
    /** 1500.5 → "$ 1.500,50" (formato ARS) */
    public static String moneda(double monto) {
        return NUM_FORMAT.format(monto);
    }

    /** "1500.50" (string de la API) → "$ 1.500,50" */
    public static String monedaDesdeString(String monto) {
        if (monto == null || monto.isBlank()) return "$ 0,00";
        try {
            return moneda(Double.parseDouble(monto));
        } catch (NumberFormatException e) {
            return monto;
        }
    }

    // ── Estados ───────────────────────────────────────────────
    public static String estadoDisplay(String estado) {
        if (estado == null) return "";
        return switch (estado) {
            case "activo"    -> "Activo";
            case "alta"      -> "Alta médica";
            case "abandono"  -> "Abandono";
            case "fallecido" -> "Fallecido";
            case "derivado"  -> "Derivado";
            default          -> estado;
        };
    }

    public static String tipoPagoDisplay(String tipo) {
        if (tipo == null) return "";
        return "obra_social".equals(tipo) ? "Obra Social" : "Particular";
    }

    // ── DNI ───────────────────────────────────────────────────
    /** "30123456" → "30.123.456" */
    public static String formatearDni(String dni) {
        if (dni == null) return "";
        return dni.replaceAll("(\\d)(?=(\\d{3})+$)", "$1.");
    }
}
