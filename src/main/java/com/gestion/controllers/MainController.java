package com.gestion.controllers;

import com.gestion.config.AppConfig;
import com.gestion.services.AuthService;
import com.gestion.utils.AlertHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MainController {

    @FXML private Label       lblUsuarioNombre;
    @FXML private Label       lblUsuarioRol;
    @FXML private StackPane   contenidoPrincipal;
    @FXML private VBox        sidebar;

    // Botones de navegación del sidebar
    @FXML private Button btnInternos;
    @FXML private Button btnCobranzas;
    @FXML private Button btnReportes;
    @FXML private Button btnConfiguracion;

    private Button botonActivo;

    @FXML
    public void initialize() {
        lblUsuarioNombre.setText(AppConfig.getUsuarioNombre());
        lblUsuarioRol.setText(rolDisplay(AppConfig.getUsuarioRol()));

        // Ocultar módulos según rol
        if (!AppConfig.tieneRol("admin")) {
            btnConfiguracion.setVisible(false);
            btnConfiguracion.setManaged(false);
        }

        // Cargar internos por defecto
        navegarA("internos", btnInternos);
    }

    @FXML private void onInternos()       { navegarA("internos",       btnInternos); }
    @FXML private void onCobranzas()      { navegarA("cobranzas",      btnCobranzas); }
    @FXML private void onReportes()       { navegarA("reportes",       btnReportes); }
    @FXML private void onConfiguracion()  { navegarA("configuracion",  btnConfiguracion); }

    @FXML
    private void onCerrarSesion() {
        if (!AlertHelper.confirmar("Cerrar sesión", "¿Querés cerrar la sesión actual?")) return;

        AuthService.logout();
        AppConfig.clearSession();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 640, 520);
            scene.getStylesheets().add(
                    getClass().getResource("/css/dark-futuristic.css").toExternalForm()
            );

            Stage stage = AppConfig.getPrimaryStage();
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setMaximized(false);
            stage.centerOnScreen();
        } catch (Exception e) {
            AlertHelper.error("Error al cerrar sesión: " + e.getMessage());
        }
    }

    // ── Navegación ────────────────────────────────────────────
    public void navegarA(String modulo, Button boton) {
        try {
            String fxmlPath = "/fxml/" + capitalize(modulo) + ".fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node contenido = loader.load();

            contenidoPrincipal.getChildren().setAll(contenido);

            // Marcar botón activo
            if (botonActivo != null) botonActivo.getStyleClass().remove("sidebar-btn-active");
            if (boton != null) {
                boton.getStyleClass().add("sidebar-btn-active");
                botonActivo = boton;
            }
        } catch (Exception e) {
            AlertHelper.info("Módulo '" + modulo + "' disponible próximamente.");
        }
    }

    // ── Helpers ───────────────────────────────────────────────
    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }

    private String rolDisplay(String rol) {
        if (rol == null) return "";
        return switch (rol) {
            case "admin"    -> "Administrador";
            case "operador" -> "Operador";
            case "contador" -> "Contador";
            case "readonly" -> "Solo lectura";
            default         -> rol;
        };
    }
}
