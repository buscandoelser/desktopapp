package com.gestion.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestion.config.AppConfig;
import com.gestion.models.Interno;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InternoService {

    private static final OkHttpClient client   = new OkHttpClient();
    private static final ObjectMapper mapper   = new ObjectMapper();
    private static final MediaType    JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    private static Request.Builder authBuilder(String url) {
        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + AppConfig.getJwtToken());
    }

    // ── GET /internos ─────────────────────────────────────────
    public static ServiceResult<List<Interno>> listar(String estado, String busqueda, int page) {
        try {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(AppConfig.API_BASE_URL + "/internos").newBuilder();
            urlBuilder.addQueryParameter("page", String.valueOf(page));
            if (estado   != null && !estado.isEmpty())   urlBuilder.addQueryParameter("estado",   estado);
            if (busqueda != null && !busqueda.isEmpty()) urlBuilder.addQueryParameter("busqueda", busqueda);

            Request request = authBuilder(urlBuilder.build().toString()).get().build();

            try (Response response = client.newCall(request).execute()) {
                String body = response.body().string();
                JsonNode node = mapper.readTree(body);

                if (!response.isSuccessful()) {
                    return ServiceResult.error(mensajeDeError(node));
                }

                List<Interno> internos = new ArrayList<>();
                for (JsonNode item : node.get("datos")) {
                    internos.add(mapper.treeToValue(item, Interno.class));
                }
                return ServiceResult.success(internos, node.get("total").asInt());
            }
        } catch (IOException e) {
            return ServiceResult.error("Error de conexión: " + e.getMessage());
        }
    }

    // ── GET /internos/:id ─────────────────────────────────────
    public static ServiceResult<Interno> obtener(int id) {
        try {
            Request request = authBuilder(AppConfig.API_BASE_URL + "/internos/" + id).get().build();

            try (Response response = client.newCall(request).execute()) {
                String body = response.body().string();
                if (!response.isSuccessful()) {
                    return ServiceResult.error(mensajeDeError(mapper.readTree(body)));
                }
                return ServiceResult.success(mapper.readValue(body, Interno.class), 1);
            }
        } catch (IOException e) {
            return ServiceResult.error("Error de conexión: " + e.getMessage());
        }
    }

    // ── POST /internos ────────────────────────────────────────
    public static ServiceResult<Interno> crear(Map<String, Object> datos) {
        try {
            String json = mapper.writeValueAsString(datos);
            RequestBody body = RequestBody.create(json, JSON_TYPE);
            Request request = authBuilder(AppConfig.API_BASE_URL + "/internos").post(body).build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body().string();
                JsonNode node = mapper.readTree(responseBody);
                if (!response.isSuccessful()) {
                    return ServiceResult.error(mensajeDeError(node));
                }
                return ServiceResult.success(mapper.treeToValue(node, Interno.class), 1);
            }
        } catch (IOException e) {
            return ServiceResult.error("Error de conexión: " + e.getMessage());
        }
    }

    // ── PUT /internos/:id ─────────────────────────────────────
    public static ServiceResult<Interno> actualizar(int id, Map<String, Object> datos) {
        try {
            String json = mapper.writeValueAsString(datos);
            RequestBody body = RequestBody.create(json, JSON_TYPE);
            Request request = authBuilder(AppConfig.API_BASE_URL + "/internos/" + id).put(body).build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body().string();
                JsonNode node = mapper.readTree(responseBody);
                if (!response.isSuccessful()) {
                    return ServiceResult.error(mensajeDeError(node));
                }
                return ServiceResult.success(mapper.treeToValue(node, Interno.class), 1);
            }
        } catch (IOException e) {
            return ServiceResult.error("Error de conexión: " + e.getMessage());
        }
    }

    // ── PATCH /internos/:id/estado ────────────────────────────
    public static ServiceResult<Void> cambiarEstado(int id, String estado, String motivo, String fechaEgreso) {
        try {
            Map<String, Object> datos = new java.util.HashMap<>();
            datos.put("estado", estado);
            if (motivo     != null) datos.put("motivo",      motivo);
            if (fechaEgreso != null) datos.put("fecha_egreso", fechaEgreso);

            String json = mapper.writeValueAsString(datos);
            RequestBody body = RequestBody.create(json, JSON_TYPE);
            Request request = authBuilder(AppConfig.API_BASE_URL + "/internos/" + id + "/estado")
                    .patch(body).build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return ServiceResult.error(mensajeDeError(mapper.readTree(response.body().string())));
                }
                return ServiceResult.success(null, 0);
            }
        } catch (IOException e) {
            return ServiceResult.error("Error de conexión: " + e.getMessage());
        }
    }

    // ── GET /internos/:id/historial ───────────────────────────
    public static ServiceResult<List<JsonNode>> historial(int id) {
        try {
            Request request = authBuilder(AppConfig.API_BASE_URL + "/internos/" + id + "/historial")
                    .get().build();

            try (Response response = client.newCall(request).execute()) {
                String body = response.body().string();
                if (!response.isSuccessful()) {
                    return ServiceResult.error(mensajeDeError(mapper.readTree(body)));
                }
                List<JsonNode> lista = new ArrayList<>();
                for (JsonNode item : mapper.readTree(body)) lista.add(item);
                return ServiceResult.success(lista, lista.size());
            }
        } catch (IOException e) {
            return ServiceResult.error("Error de conexión: " + e.getMessage());
        }
    }

    // ── Helper ────────────────────────────────────────────────
    private static String mensajeDeError(JsonNode node) {
        return node != null && node.has("mensaje") ? node.get("mensaje").asText() : "Error desconocido";
    }

    // ── Resultado genérico ────────────────────────────────────
    public static class ServiceResult<T> {
        public final boolean success;
        public final T       data;
        public final int     total;
        public final String  errorMensaje;

        private ServiceResult(boolean success, T data, int total, String errorMensaje) {
            this.success      = success;
            this.data         = data;
            this.total        = total;
            this.errorMensaje = errorMensaje;
        }

        public static <T> ServiceResult<T> success(T data, int total) {
            return new ServiceResult<>(true, data, total, null);
        }

        public static <T> ServiceResult<T> error(String mensaje) {
            return new ServiceResult<>(false, null, 0, mensaje);
        }
    }
}
