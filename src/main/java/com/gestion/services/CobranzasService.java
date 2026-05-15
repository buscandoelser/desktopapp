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

    /**
     * Lee el body y lo parsea como JSON.
     * Si el servidor devuelve HTML lanza IOException con mensaje claro
     * y loguea los primeros 300 chars del body en consola.
     */
    private static JsonNode safeReadJson(Response response) throws IOException {
        String body    = response.body().string();
        String trimmed = body.trim();
        System.out.println("[HTTP " + response.code() + "] " + response.request().url());
        System.out.println("[RESPONSE] " + trimmed.substring(0, Math.min(500, trimmed.length())));
        if (trimmed.startsWith("<")) {
            throw new IOException("El servidor devolvió HTML (HTTP " + response.code() + ")."
                    + " El backend puede estar durmiendo o la ruta no existe.");
        }
        return mapper.readTree(trimmed);
    }

    // ── GET /cuotas ─────────────────────────────────
    public static ServiceResult<List<Cuota>> listarCuotas(String estado, int anio, int mes, int page) {
        try {
            HttpUrl.Builder url = HttpUrl.parse(AppConfig.API_BASE_URL + "/cuotas").newBuilder();
            url.addQueryParameter("page", String.valueOf(page));
            if (estado != null && !estado.isEmpty()) url.addQueryParameter("estado", estado);
            if (anio > 0) url.addQueryParameter("anio", String.valueOf(anio));
            if (mes  > 0) url.addQueryParameter("mes",  String.valueOf(mes));

            Request req = authBuilder(url.build().toString()).get().build();

            try (Response response = client.newCall(req).execute()) {
                JsonNode node = safeReadJson(response);
                if (!response.isSuccessful()) return ServiceResult.error(mensajeError(node));

                JsonNode datosNode = node.get("datos");
                if (datosNode == null || !datosNode.isArray())
                    return ServiceResult.error("Respuesta inesperada del servidor");

                List<Cuota> lista = new ArrayList<>();
                for (JsonNode item : datosNode) lista.add(mapper.treeToValue(item, Cuota.class));
                int total = node.has("total") ? node.get("total").asInt() : lista.size();
                return ServiceResult.success(lista, total);
            }
        } catch (Exception e) {
            System.err.println("[listarCuotas] " + e.getMessage());
            return ServiceResult.error(e.getMessage());
        }
    }

    // ── GET /cuotas/:internoId ─────────────────────
    public static ServiceResult<JsonNode> cuentaCorriente(int internoId) {
        try {
            Request req = authBuilder(AppConfig.API_BASE_URL + "/cuotas/" + internoId).get().build();
            try (Response response = client.newCall(req).execute()) {
                JsonNode node = safeReadJson(response);
                if (!response.isSuccessful()) return ServiceResult.error(mensajeError(node));
                return ServiceResult.success(node, 1);
            }
        } catch (Exception e) {
            System.err.println("[cuentaCorriente] " + e.getMessage());
            return ServiceResult.error(e.getMessage());
        }
    }

    // ── POST /cuotas/generar-mes ───────────────────
    public static ServiceResult<JsonNode> generarMes(int anio, int mes, Integer internoId) {
        try {
            Map<String, Object> datos = new java.util.HashMap<>();
            datos.put("anio", anio);
            datos.put("mes",  mes);
            if (internoId != null) datos.put("interno_id", internoId);

            RequestBody body = RequestBody.create(mapper.writeValueAsString(datos), JSON_TYPE);
            Request req = authBuilder(AppConfig.API_BASE_URL + "/cuotas/generar-mes").post(body).build();

            try (Response response = client.newCall(req).execute()) {
                JsonNode node = safeReadJson(response);
                if (!response.isSuccessful()) return ServiceResult.error(mensajeError(node));
                return ServiceResult.success(node, 1);
            }
        } catch (Exception e) {
            System.err.println("[generarMes] " + e.getMessage());
            return ServiceResult.error(e.getMessage());
        }
    }

    // ── POST /pagos ─────────────────────────────────
    public static ServiceResult<JsonNode> registrarPago(Map<String, Object> datos) {
        try {
            RequestBody body = RequestBody.create(mapper.writeValueAsString(datos), JSON_TYPE);
            Request req = authBuilder(AppConfig.API_BASE_URL + "/pagos").post(body).build();

            try (Response response = client.newCall(req).execute()) {
                JsonNode node = safeReadJson(response);
                if (!response.isSuccessful()) return ServiceResult.error(mensajeError(node));
                return ServiceResult.success(node, 1);
            }
        } catch (Exception e) {
            System.err.println("[registrarPago] " + e.getMessage());
            return ServiceResult.error(e.getMessage());
        }
    }

    // ── GET /pagos/:internoId ──────────────────────
    public static ServiceResult<JsonNode> historialPagos(int internoId) {
        try {
            Request req = authBuilder(AppConfig.API_BASE_URL + "/pagos/" + internoId).get().build();
            try (Response response = client.newCall(req).execute()) {
                JsonNode node = safeReadJson(response);
                if (!response.isSuccessful()) return ServiceResult.error(mensajeError(node));
                return ServiceResult.success(node, 1);
            }
        } catch (Exception e) {
            System.err.println("[historialPagos] " + e.getMessage());
            return ServiceResult.error(e.getMessage());
        }
    }

    // ── POST /ejecutar-interes ─────────────────────
    public static ServiceResult<JsonNode> ejecutarInteres() {
        try {
            RequestBody body = RequestBody.create("{}", JSON_TYPE);
            Request req = authBuilder(AppConfig.API_BASE_URL + "/ejecutar-interes").post(body).build();
            try (Response response = client.newCall(req).execute()) {
                JsonNode node = safeReadJson(response);
                if (!response.isSuccessful()) return ServiceResult.error(mensajeError(node));
                return ServiceResult.success(node, 1);
            }
        } catch (Exception e) {
            System.err.println("[ejecutarInteres] " + e.getMessage());
            return ServiceResult.error(e.getMessage());
        }
    }

    // ── GET /egresos ────────────────────────────────
    public static ServiceResult<List<Egreso>> listarEgresos(String desde, String hasta, String categoria, String medioPago, int page) {
        try {
            HttpUrl.Builder url = HttpUrl.parse(AppConfig.API_BASE_URL + "/egresos").newBuilder();
            url.addQueryParameter("page", String.valueOf(page));
            if (desde     != null && !desde.isEmpty())     url.addQueryParameter("desde",     desde);
            if (hasta     != null && !hasta.isEmpty())     url.addQueryParameter("hasta",     hasta);
            if (categoria != null && !categoria.isEmpty()) url.addQueryParameter("categoria", categoria);
            if (medioPago != null && !medioPago.isEmpty()) url.addQueryParameter("medio_pago", medioPago);

            Request req = authBuilder(url.build().toString()).get().build();

            try (Response response = client.newCall(req).execute()) {
                JsonNode node = safeReadJson(response);
                if (!response.isSuccessful()) return ServiceResult.error(mensajeError(node));

                JsonNode datosNode = node.get("datos");
                if (datosNode == null || !datosNode.isArray())
                    return ServiceResult.error("Respuesta inesperada del servidor");

                List<Egreso> lista = new ArrayList<>();
                for (JsonNode item : datosNode) lista.add(mapper.treeToValue(item, Egreso.class));
                int total = node.has("total") ? node.get("total").asInt() : lista.size();
                return ServiceResult.success(lista, total);
            }
        } catch (Exception e) {
            System.err.println("[listarEgresos] " + e.getMessage());
            return ServiceResult.error(e.getMessage());
        }
    }

    // ── POST /egresos ───────────────────────────────
    public static ServiceResult<JsonNode> registrarEgreso(Map<String, Object> datos) {
        try {
            String json = mapper.writeValueAsString(datos);
            System.out.println("[registrarEgreso] POST " + AppConfig.API_BASE_URL + "/egresos");
            System.out.println("[registrarEgreso] Body: " + json);
            RequestBody body = RequestBody.create(json, JSON_TYPE);
            Request req = authBuilder(AppConfig.API_BASE_URL + "/egresos").post(body).build();

            try (Response response = client.newCall(req).execute()) {
                JsonNode node = safeReadJson(response);
                if (!response.isSuccessful()) return ServiceResult.error(mensajeError(node));
                return ServiceResult.success(node, 1);
            }
        } catch (Exception e) {
            System.err.println("[registrarEgreso] " + e.getMessage());
            return ServiceResult.error(e.getMessage());
        }
    }

    // ── Helper ────────────────────────────────────────────────
    private static String mensajeError(JsonNode node) {
        return node != null && node.has("mensaje") ? node.get("mensaje").asText() : "Error desconocido";
    }
}
