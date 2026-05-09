package com.gestion.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestion.config.AppConfig;
import com.gestion.services.InternoService.ServiceResult;
import okhttp3.*;

import java.io.IOException;

public class ReportesService {

    private static final OkHttpClient client = AppConfig.HTTP_CLIENT;
    private static final ObjectMapper mapper = new ObjectMapper();

    private static Request.Builder authBuilder(String url) {
        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + AppConfig.getJwtToken());
    }

    // ── GET /reportes/deuda-total ─────────────────────────────
    public static ServiceResult<JsonNode> deudaTotal() {
        return get("/reportes/deuda-total");
    }

    // ── GET /reportes/ingresos-mes ────────────────────────────
    public static ServiceResult<JsonNode> ingresosMes(int anio, int mes) {
        return get("/reportes/ingresos-mes?anio=" + anio + "&mes=" + mes);
    }

    // ── GET /reportes/egresos-mes ─────────────────────────────
    public static ServiceResult<JsonNode> egresosMes(int anio, int mes) {
        return get("/reportes/egresos-mes?anio=" + anio + "&mes=" + mes);
    }

    // ── GET /reportes/balance-mensual ─────────────────────────
    public static ServiceResult<JsonNode> balanceMensual(int anio, int mes) {
        return get("/reportes/balance-mensual?anio=" + anio + "&mes=" + mes);
    }

    // ── GET /reportes/balance-anual ───────────────────────────
    public static ServiceResult<JsonNode> balanceAnual(int anio) {
        return get("/reportes/balance-anual?anio=" + anio);
    }

    // ── GET /reportes/deudores ────────────────────────────────
    public static ServiceResult<JsonNode> deudores(String orden, String tipoPago) {
        StringBuilder url = new StringBuilder("/reportes/deudores?orden=" + (orden != null ? orden : "desc"));
        if (tipoPago != null && !tipoPago.isEmpty()) url.append("&tipo_pago=").append(tipoPago);
        return get(url.toString());
    }

    // ── GET /reportes/internos-activos ────────────────────────
    public static ServiceResult<JsonNode> internosActivos(String tipoPago) {
        String url = "/reportes/internos-activos" + (tipoPago != null ? "?tipo_pago=" + tipoPago : "");
        return get(url);
    }

    // ── GET /reportes/bajas ───────────────────────────────────
    public static ServiceResult<JsonNode> bajas(String desde, String hasta, String motivo) {
        StringBuilder url = new StringBuilder("/reportes/bajas?");
        if (desde  != null) url.append("desde=").append(desde).append("&");
        if (hasta  != null) url.append("hasta=").append(hasta).append("&");
        if (motivo != null) url.append("motivo=").append(motivo);
        return get(url.toString());
    }

    // ── GET /reportes/comparativo ─────────────────────────────
    public static ServiceResult<JsonNode> comparativo(int mes1, int anio1, int mes2, int anio2) {
        return get("/reportes/comparativo?mes1=" + mes1 + "&anio1=" + anio1
                + "&mes2=" + mes2 + "&anio2=" + anio2);
    }

    // ── GET /reportes/clasificacion-pago ──────────────────────
    public static ServiceResult<JsonNode> clasificacionPago(int anio, int mes) {
        return get("/reportes/clasificacion-pago?anio=" + anio + "&mes=" + mes);
    }

    // ── GET /reportes/control-deuda ───────────────────────────
    public static ServiceResult<JsonNode> controlDeuda() {
        return get("/reportes/control-deuda");
    }

    // ── Helper común ──────────────────────────────────────────
    private static ServiceResult<JsonNode> get(String path) {
        try {
            Request req = authBuilder(AppConfig.API_BASE_URL + path).get().build();
            try (Response response = client.newCall(req).execute()) {
                String body = response.body().string();
                JsonNode node = mapper.readTree(body);
                if (!response.isSuccessful()) {
                    String msg = node.has("mensaje") ? node.get("mensaje").asText() : "Error " + response.code();
                    return ServiceResult.error(msg);
                }
                return ServiceResult.success(node, 1);
            }
        } catch (IOException e) {
            return ServiceResult.error("Error de conexión: " + e.getMessage());
        }
    }
}
