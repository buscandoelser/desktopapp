package com.gestion.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestion.config.AppConfig;
import com.gestion.services.InternoService.ServiceResult;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;

public class ConfigService {

    private static final OkHttpClient client    = AppConfig.HTTP_CLIENT;
    private static final ObjectMapper  mapper    = new ObjectMapper();
    private static final MediaType     JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    private static Request.Builder authBuilder(String url) {
        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + AppConfig.getJwtToken());
    }

    private static JsonNode safeReadJson(Response response) throws IOException {
        String body    = response.body().string();
        String trimmed = body.trim();
        System.out.println("[HTTP " + response.code() + "] " + response.request().url());
        System.out.println("[RESPONSE] " + trimmed.substring(0, Math.min(500, trimmed.length())));
        if (trimmed.startsWith("<")) {
            throw new IOException("El servidor devolvió HTML (HTTP " + response.code() + ").");
        }
        return mapper.readTree(trimmed);
    }

    private static String mensajeError(JsonNode node) {
        return node != null && node.has("mensaje") ? node.get("mensaje").asText() : "Error desconocido";
    }

    // ── GET /config/cuota ──────────────────────────────────────
    public static ServiceResult<JsonNode> getCuota() {
        try {
            Request req = authBuilder(AppConfig.API_BASE_URL + "/config/cuota").get().build();
            try (Response response = client.newCall(req).execute()) {
                JsonNode node = safeReadJson(response);
                if (!response.isSuccessful()) return ServiceResult.error(mensajeError(node));
                return ServiceResult.success(node, 1);
            }
        } catch (Exception e) {
            System.err.println("[getCuota] " + e.getMessage());
            return ServiceResult.error(e.getMessage());
        }
    }

    // ── POST /config/cuota ─────────────────────────────────────
    public static ServiceResult<JsonNode> setCuota(String monto) {
        try {
            Map<String, Object> datos = Map.of("monto", monto);
            RequestBody body = RequestBody.create(mapper.writeValueAsString(datos), JSON_TYPE);
            Request req = authBuilder(AppConfig.API_BASE_URL + "/config/cuota").post(body).build();
            try (Response response = client.newCall(req).execute()) {
                JsonNode node = safeReadJson(response);
                if (!response.isSuccessful()) return ServiceResult.error(mensajeError(node));
                return ServiceResult.success(node, 1);
            }
        } catch (Exception e) {
            System.err.println("[setCuota] " + e.getMessage());
            return ServiceResult.error(e.getMessage());
        }
    }

    // ── GET /config/mora ───────────────────────────────────────
    public static ServiceResult<JsonNode> getMora() {
        try {
            Request req = authBuilder(AppConfig.API_BASE_URL + "/config/mora").get().build();
            try (Response response = client.newCall(req).execute()) {
                JsonNode node = safeReadJson(response);
                if (!response.isSuccessful()) return ServiceResult.error(mensajeError(node));
                return ServiceResult.success(node, 1);
            }
        } catch (Exception e) {
            System.err.println("[getMora] " + e.getMessage());
            return ServiceResult.error(e.getMessage());
        }
    }

    // ── POST /config/mora ──────────────────────────────────────
    public static ServiceResult<JsonNode> setMora(String monto) {
        try {
            Map<String, Object> datos = Map.of("monto", monto);
            RequestBody body = RequestBody.create(mapper.writeValueAsString(datos), JSON_TYPE);
            Request req = authBuilder(AppConfig.API_BASE_URL + "/config/mora").post(body).build();
            try (Response response = client.newCall(req).execute()) {
                JsonNode node = safeReadJson(response);
                if (!response.isSuccessful()) return ServiceResult.error(mensajeError(node));
                return ServiceResult.success(node, 1);
            }
        } catch (Exception e) {
            System.err.println("[setMora] " + e.getMessage());
            return ServiceResult.error(e.getMessage());
        }
    }

    // ── GET /config/camas ──────────────────────────────────────
    public static ServiceResult<JsonNode> getCamas() {
        try {
            Request req = authBuilder(AppConfig.API_BASE_URL + "/config/camas").get().build();
            try (Response response = client.newCall(req).execute()) {
                JsonNode node = safeReadJson(response);
                if (!response.isSuccessful()) return ServiceResult.error(mensajeError(node));
                return ServiceResult.success(node, 1);
            }
        } catch (Exception e) {
            System.err.println("[getCamas] " + e.getMessage());
            return ServiceResult.error(e.getMessage());
        }
    }

    // ── POST /config/camas ─────────────────────────────────────
    public static ServiceResult<JsonNode> setCamas(int total) {
        try {
            Map<String, Object> datos = Map.of("total", total);
            RequestBody body = RequestBody.create(mapper.writeValueAsString(datos), JSON_TYPE);
            Request req = authBuilder(AppConfig.API_BASE_URL + "/config/camas").post(body).build();
            try (Response response = client.newCall(req).execute()) {
                JsonNode node = safeReadJson(response);
                if (!response.isSuccessful()) return ServiceResult.error(mensajeError(node));
                return ServiceResult.success(node, 1);
            }
        } catch (Exception e) {
            System.err.println("[setCamas] " + e.getMessage());
            return ServiceResult.error(e.getMessage());
        }
    }

    // ── GET /config/vencimiento ────────────────────────────────
    public static ServiceResult<JsonNode> getVencimiento() {
        try {
            Request req = authBuilder(AppConfig.API_BASE_URL + "/config/vencimiento").get().build();
            try (Response response = client.newCall(req).execute()) {
                JsonNode node = safeReadJson(response);
                if (!response.isSuccessful()) return ServiceResult.error(mensajeError(node));
                return ServiceResult.success(node, 1);
            }
        } catch (Exception e) {
            System.err.println("[getVencimiento] " + e.getMessage());
            return ServiceResult.error(e.getMessage());
        }
    }

    // ── POST /config/vencimiento ───────────────────────────────
    public static ServiceResult<JsonNode> setVencimiento(int dia) {
        try {
            Map<String, Object> datos = Map.of("dia", dia);
            RequestBody body = RequestBody.create(mapper.writeValueAsString(datos), JSON_TYPE);
            Request req = authBuilder(AppConfig.API_BASE_URL + "/config/vencimiento").post(body).build();
            try (Response response = client.newCall(req).execute()) {
                JsonNode node = safeReadJson(response);
                if (!response.isSuccessful()) return ServiceResult.error(mensajeError(node));
                return ServiceResult.success(node, 1);
            }
        } catch (Exception e) {
            System.err.println("[setVencimiento] " + e.getMessage());
            return ServiceResult.error(e.getMessage());
        }
    }

    // ── GET /config/usuarios ───────────────────────────────────
    public static ServiceResult<JsonNode> getUsuarios() {
        try {
            Request req = authBuilder(AppConfig.API_BASE_URL + "/config/usuarios").get().build();
            try (Response response = client.newCall(req).execute()) {
                JsonNode node = safeReadJson(response);
                if (!response.isSuccessful()) return ServiceResult.error(mensajeError(node));
                return ServiceResult.success(node, 1);
            }
        } catch (Exception e) {
            System.err.println("[getUsuarios] " + e.getMessage());
            return ServiceResult.error(e.getMessage());
        }
    }

    // ── POST /config/usuarios ──────────────────────────────────
    public static ServiceResult<JsonNode> crearUsuario(Map<String, Object> datos) {
        try {
            RequestBody body = RequestBody.create(mapper.writeValueAsString(datos), JSON_TYPE);
            Request req = authBuilder(AppConfig.API_BASE_URL + "/config/usuarios").post(body).build();
            try (Response response = client.newCall(req).execute()) {
                JsonNode node = safeReadJson(response);
                if (!response.isSuccessful()) return ServiceResult.error(mensajeError(node));
                return ServiceResult.success(node, 1);
            }
        } catch (Exception e) {
            System.err.println("[crearUsuario] " + e.getMessage());
            return ServiceResult.error(e.getMessage());
        }
    }

    // ── PUT /config/usuarios/:id ───────────────────────────────
    public static ServiceResult<JsonNode> actualizarUsuario(int id, Map<String, Object> datos) {
        try {
            RequestBody body = RequestBody.create(mapper.writeValueAsString(datos), JSON_TYPE);
            Request req = authBuilder(AppConfig.API_BASE_URL + "/config/usuarios/" + id).put(body).build();
            try (Response response = client.newCall(req).execute()) {
                JsonNode node = safeReadJson(response);
                if (!response.isSuccessful()) return ServiceResult.error(mensajeError(node));
                return ServiceResult.success(node, 1);
            }
        } catch (Exception e) {
            System.err.println("[actualizarUsuario] " + e.getMessage());
            return ServiceResult.error(e.getMessage());
        }
    }

    // ── DELETE /config/usuarios/:id ────────────────────────────
    public static ServiceResult<JsonNode> eliminarUsuario(int id) {
        try {
            Request req = authBuilder(AppConfig.API_BASE_URL + "/config/usuarios/" + id).delete().build();
            try (Response response = client.newCall(req).execute()) {
                JsonNode node = safeReadJson(response);
                if (!response.isSuccessful()) return ServiceResult.error(mensajeError(node));
                return ServiceResult.success(node, 1);
            }
        } catch (Exception e) {
            System.err.println("[eliminarUsuario] " + e.getMessage());
            return ServiceResult.error(e.getMessage());
        }
    }

    // ── GET /config/auditoria ──────────────────────────────────
    public static ServiceResult<JsonNode> getAuditoria() {
        try {
            Request req = authBuilder(AppConfig.API_BASE_URL + "/config/auditoria").get().build();
            try (Response response = client.newCall(req).execute()) {
                JsonNode node = safeReadJson(response);
                if (!response.isSuccessful()) return ServiceResult.error(mensajeError(node));
                return ServiceResult.success(node, 1);
            }
        } catch (Exception e) {
            System.err.println("[getAuditoria] " + e.getMessage());
            return ServiceResult.error(e.getMessage());
        }
    }
}
