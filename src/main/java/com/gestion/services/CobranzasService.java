package com.gestion.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestion.config.AppConfig;
import com.gestion.models.Cuota;
import com.gestion.models.Egreso;
import com.gestion.services.InternoService.ServiceResult;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CobranzasService {

    private static final OkHttpClient client    = AppConfig.HTTP_CLIENT;
    private static final ObjectMapper mapper    = new ObjectMapper();
    private static final MediaType    JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    private static Request.Builder authBuilder(String url) {
        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + AppConfig.getJwtToken());
    }

    // ── GET /cobranzas/cuotas ─────────────────────────────────
    public static ServiceResult<List<Cuota>> listarCuotas(String estado, int anio, int mes, int page) {
        try {
            HttpUrl.Builder url = HttpUrl.parse(AppConfig.API_BASE_URL + "/cobranzas/cuotas").newBuilder();
            url.addQueryParameter("page", String.valueOf(page));
            if (estado != null && !estado.isEmpty()) url.addQueryParameter("estado", estado);
            if (anio > 0) url.addQueryParameter("anio", String.valueOf(anio));
            if (mes  > 0) url.addQueryParameter("mes",  String.valueOf(mes));

            Request req = authBuilder(url.build().toString()).get().build();

            try (Response response = client.newCall(req).execute()) {
                String body = response.body().string();
                JsonNode node = mapper.readTree(body);
                if (!response.isSuccessful()) return ServiceResult.error(mensajeError(node));

                List<Cuota> lista = new ArrayList<>();
                for (JsonNode item : node.get("datos")) lista.add(mapper.treeToValue(item, Cuota.class));
                return ServiceResult.success(lista, node.get("total").asInt());
            }
        } catch (IOException e) {
            return ServiceResult.error("Error de conexión: " + e.getMessage());
        }
    }

    // ── GET /cobranzas/cuotas/:internoId ─────────────────────
    public static ServiceResult<JsonNode> cuentaCorriente(int internoId) {
        try {
            Request req = authBuilder(AppConfig.API_BASE_URL + "/cobranzas/cuotas/" + internoId).get().build();

            try (Response response = client.newCall(req).execute()) {
                String body = response.body().string();
                JsonNode node = mapper.readTree(body);
                if (!response.isSuccessful()) return ServiceResult.error(mensajeError(node));
                return ServiceResult.success(node, 1);
            }
        } catch (IOException e) {
            return ServiceResult.error("Error de conexión: " + e.getMessage());
        }
    }

    // ── POST /cobranzas/cuotas/generar-mes ───────────────────
    public static ServiceResult<JsonNode> generarMes(int anio, int mes, Integer internoId) {
        try {
            Map<String, Object> datos = new java.util.HashMap<>();
            datos.put("anio", anio);
            datos.put("mes",  mes);
            if (internoId != null) datos.put("interno_id", internoId);

            RequestBody body = RequestBody.create(mapper.writeValueAsString(datos), JSON_TYPE);
            Request req = authBuilder(AppConfig.API_BASE_URL + "/cobranzas/cuotas/generar-mes").post(body).build();

            try (Response response = client.newCall(req).execute()) {
                String rb = response.body().string();
                JsonNode node = mapper.readTree(rb);
                if (!response.isSuccessful()) return ServiceResult.error(mensajeError(node));
                return ServiceResult.success(node, 1);
            }
        } catch (IOException e) {
            return ServiceResult.error("Error de conexión: " + e.getMessage());
        }
    }

    // ── POST /cobranzas/pagos ─────────────────────────────────
    public static ServiceResult<JsonNode> registrarPago(Map<String, Object> datos) {
        try {
            RequestBody body = RequestBody.create(mapper.writeValueAsString(datos), JSON_TYPE);
            Request req = authBuilder(AppConfig.API_BASE_URL + "/cobranzas/pagos").post(body).build();

            try (Response response = client.newCall(req).execute()) {
                String rb = response.body().string();
                JsonNode node = mapper.readTree(rb);
                if (!response.isSuccessful()) return ServiceResult.error(mensajeError(node));
                return ServiceResult.success(node, 1);
            }
        } catch (IOException e) {
            return ServiceResult.error("Error de conexión: " + e.getMessage());
        }
    }

    // ── GET /cobranzas/egresos ────────────────────────────────
    public static ServiceResult<List<Egreso>> listarEgresos(String desde, String hasta, String categoria, int page) {
        try {
            HttpUrl.Builder url = HttpUrl.parse(AppConfig.API_BASE_URL + "/cobranzas/egresos").newBuilder();
            url.addQueryParameter("page", String.valueOf(page));
            if (desde     != null && !desde.isEmpty())     url.addQueryParameter("desde",     desde);
            if (hasta     != null && !hasta.isEmpty())     url.addQueryParameter("hasta",     hasta);
            if (categoria != null && !categoria.isEmpty()) url.addQueryParameter("categoria", categoria);

            Request req = authBuilder(url.build().toString()).get().build();

            try (Response response = client.newCall(req).execute()) {
                String body = response.body().string();
                JsonNode node = mapper.readTree(body);
                if (!response.isSuccessful()) return ServiceResult.error(mensajeError(node));

                List<Egreso> lista = new ArrayList<>();
                for (JsonNode item : node.get("datos")) lista.add(mapper.treeToValue(item, Egreso.class));
                return ServiceResult.success(lista, node.get("total").asInt());
            }
        } catch (IOException e) {
            return ServiceResult.error("Error de conexión: " + e.getMessage());
        }
    }

    // ── POST /cobranzas/egresos ───────────────────────────────
    public static ServiceResult<JsonNode> registrarEgreso(Map<String, Object> datos) {
        try {
            RequestBody body = RequestBody.create(mapper.writeValueAsString(datos), JSON_TYPE);
            Request req = authBuilder(AppConfig.API_BASE_URL + "/cobranzas/egresos").post(body).build();

            try (Response response = client.newCall(req).execute()) {
                String rb = response.body().string();
                JsonNode node = mapper.readTree(rb);
                if (!response.isSuccessful()) return ServiceResult.error(mensajeError(node));
                return ServiceResult.success(node, 1);
            }
        } catch (IOException e) {
            return ServiceResult.error("Error de conexión: " + e.getMessage());
        }
    }

    // ── Helper ────────────────────────────────────────────────
    private static String mensajeError(JsonNode node) {
        return node != null && node.has("mensaje") ? node.get("mensaje").asText() : "Error desconocido";
    }
}
