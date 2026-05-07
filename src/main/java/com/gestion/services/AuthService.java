package com.gestion.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestion.config.AppConfig;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;

public class AuthService {

    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();
    static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    // ── POST /auth/login ──────────────────────────────────────
    public static LoginResult login(String username, String password) {
        try {
            String json = mapper.writeValueAsString(Map.of("username", username, "password", password));
            RequestBody body = RequestBody.create(json, JSON_TYPE);

            Request request = new Request.Builder()
                    .url(AppConfig.API_BASE_URL + "/auth/login")
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body().string();

                if (!response.isSuccessful()) {
                    try {
                        JsonNode node = mapper.readTree(responseBody);
                        String msg = node.has("mensaje") ? node.get("mensaje").asText() : "Error de autenticación (HTTP " + response.code() + ")";
                        return LoginResult.error(msg);
                    } catch (Exception ex) {
                        return LoginResult.error("Error HTTP " + response.code() + " — respuesta inesperada del servidor");
                    }
                }

                JsonNode node = mapper.readTree(responseBody);
                return LoginResult.success(
                        node.get("token").asText(),
                        node.get("refreshToken").asText(),
                        node.get("usuario").get("nombre").asText(),
                        node.get("usuario").get("apellido").asText(),
                        node.get("usuario").get("rol").asText(),
                        node.get("usuario").get("id").asInt()
                );
            }
        } catch (IOException e) {
            System.err.println("[AuthService] Error de conexión: " + e);
            return LoginResult.error("Error de conexión: " + e.getMessage());
        }
    }

    // ── POST /auth/refresh ────────────────────────────────────
    public static String refreshToken(String refreshToken) {
        try {
            String json = mapper.writeValueAsString(Map.of("refreshToken", refreshToken));
            RequestBody body = RequestBody.create(json, JSON_TYPE);

            Request request = new Request.Builder()
                    .url(AppConfig.API_BASE_URL + "/auth/refresh")
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) return null;
                JsonNode node = mapper.readTree(response.body().string());
                return node.get("token").asText();
            }
        } catch (IOException e) {
            return null;
        }
    }

    // ── POST /auth/logout ─────────────────────────────────────
    public static void logout() {
        try {
            RequestBody body = RequestBody.create("{}", JSON_TYPE);
            Request request = new Request.Builder()
                    .url(AppConfig.API_BASE_URL + "/auth/logout")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + AppConfig.getJwtToken())
                    .build();
            client.newCall(request).execute().close();
        } catch (IOException ignored) { }
    }

    // ── Resultado del login ───────────────────────────────────
    public static class LoginResult {
        public final boolean success;
        public final String  token;
        public final String  refreshToken;
        public final String  nombre;
        public final String  apellido;
        public final String  rol;
        public final int     id;
        public final String  errorMensaje;

        private LoginResult(boolean success, String token, String refreshToken,
                            String nombre, String apellido, String rol, int id, String errorMensaje) {
            this.success       = success;
            this.token         = token;
            this.refreshToken  = refreshToken;
            this.nombre        = nombre;
            this.apellido      = apellido;
            this.rol           = rol;
            this.id            = id;
            this.errorMensaje  = errorMensaje;
        }

        public static LoginResult success(String token, String refreshToken,
                                          String nombre, String apellido, String rol, int id) {
            return new LoginResult(true, token, refreshToken, nombre, apellido, rol, id, null);
        }

        public static LoginResult error(String mensaje) {
            return new LoginResult(false, null, null, null, null, null, 0, mensaje);
        }
    }
}
