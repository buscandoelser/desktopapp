package com.gestion.config;

import javafx.stage.Stage;

public final class AppConfig {

    // URL base de la API — sobreescribible con variable de entorno
    public static final String API_BASE_URL =
            System.getenv().getOrDefault("API_BASE_URL", "https://backend-t07u.onrender.com/api");

    // ── Sesión activa (en memoria, nunca en disco) ────────────
    private static String  jwtToken;
    private static String  refreshToken;
    private static int     usuarioId;
    private static String  usuarioNombre;
    private static String  usuarioRol;

    private static Stage primaryStage;

    private AppConfig() {}

    // ── Getters / Setters ─────────────────────────────────────
    public static String getJwtToken()           { return jwtToken; }
    public static void   setJwtToken(String t)   { jwtToken = t; }

    public static String getRefreshToken()            { return refreshToken; }
    public static void   setRefreshToken(String t)    { refreshToken = t; }

    public static int  getUsuarioId()          { return usuarioId; }
    public static void setUsuarioId(int id)    { usuarioId = id; }

    public static String getUsuarioNombre()              { return usuarioNombre; }
    public static void   setUsuarioNombre(String nombre) { usuarioNombre = nombre; }

    public static String getUsuarioRol()            { return usuarioRol; }
    public static void   setUsuarioRol(String rol)  { usuarioRol = rol; }

    public static Stage getPrimaryStage()           { return primaryStage; }
    public static void  setPrimaryStage(Stage s)    { primaryStage = s; }

    // ── Limpia la sesión al cerrar ────────────────────────────
    public static void clearSession() {
        jwtToken      = null;
        refreshToken  = null;
        usuarioId     = 0;
        usuarioNombre = null;
        usuarioRol    = null;
    }

    // ── Verifica si el rol activo tiene acceso ────────────────
    public static boolean esAdmin()    { return "admin".equals(usuarioRol); }
    public static boolean esOperador() { return "admin".equals(usuarioRol) || "operador".equals(usuarioRol); }
    public static boolean esContador() { return "admin".equals(usuarioRol) || "contador".equals(usuarioRol); }

    public static boolean tieneRol(String... roles) {
        if (usuarioRol == null) return false;
        for (String r : roles) {
            if (usuarioRol.equals(r)) return true;
        }
        return false;
    }
}
