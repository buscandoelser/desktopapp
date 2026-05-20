package com.gestion.controllers;

import com.gestion.config.AppConfig;
import com.gestion.services.AuthService;
import com.gestion.services.InternoService;
import com.gestion.utils.AlertHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.concurrent.CompletableFuture;

public class MainController {

    @FXML private Label       lblUsuarioNombre;
    @FXML private Label       lblUsuarioRol;
    @FXML private StackPane   contenidoPrincipal;
    @FXML private VBox        sidebar;

    // Botones de navegación
    @FXML private Button btnDashboard;
    @FXML private Button btnInternos;
    @FXML private Button btnCobranzas;
    @FXML private Button btnPagos;
    @FXML private Button btnReportes;
    @FXML private Button btnConfiguracion;

    // Capacity card
    @FXML private ProgressBar pbCapacidad;
    @FXML private Label       lblCapacidadMeta;
    @FXML private Label       lblCapacidadPct;

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
        if (!AppConfig.tieneRol("admin", "operador", "contador")) {
            btnCobranzas.setVisible(false);
            btnCobranzas.setManaged(false);
            btnPagos.setVisible(false);
            btnPagos.setManaged(false);
        }

        actualizarCapacidad();

        // Pantalla inicial: Dashboard
        navegarA("dashboard", btnDashboard);
    }

    // ── Handlers de navegación ────────────────────────────────
    @FXML private void onDashboard()      { navegarA("dashboard",     btnDashboard); }
    @FXML private void onInternos()       { navegarA("internos",       btnInternos); }
    @FXML private void onCobranzas()      { navegarA("cobranzas",      btnCobranzas); }
    @FXML private void onPagos()          { navegarA("pagos",          btnPagos); }
    @FXML private void onReportes()       { navegarA("reportes",       btnReportes); }
    @FXML private void onConfiguracion()  { navegarA("configuracion",  btnConfiguracion); }

    /** Permite al DashboardController navegar a cobranzas */
    public void navegarACobranzas() { navegarA("cobranzas", btnCobranzas); }

    @FXML
    private void onCerrarSesion() {
        if (!AlertHelper.confirmar("Cerrar sesión", "¿Querés cerrar la sesión actual?")) return;

        AuthService.logout();
        AppConfig.clearSession();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 860, 560);
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

    // ── Navegación central ────────────────────────────────────
    public void navegarA(String modulo, Button boton) {
        try {
            String fxmlPath = "/fxml/" + capitalize(modulo) + ".fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node contenido = loader.load();

            // Pasar referencia al DashboardController para que pueda navegar
            if ("dashboard".equals(modulo)) {
                DashboardController dc = loader.getController();
                dc.setMainController(this);
            }

            contenidoPrincipal.getChildren().setAll(contenido);

            if (botonActivo != null) botonActivo.getStyleClass().remove("sidebar-btn-active");
            if (boton != null) {
                boton.getStyleClass().add("sidebar-btn-active");
                botonActivo = boton;
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.error("Error al abrir módulo '" + modulo + "':\n" + e.getMessage());
        }
    }

    // ── Capacity bar: carga el total de activos vs capacidad ──
    private void actualizarCapacidad() {
        final int CAPACIDAD_TOTAL = 40;
        CompletableFuture
            .supplyAsync(() -> InternoService.listar("activo", null, 1))
            .thenAcceptAsync(result -> {
                if (!result.success) return;
                int activos  = result.total;
                int libres   = Math.max(0, CAPACIDAD_TOTAL - activos);
                double pct   = (double) activos / CAPACIDAD_TOTAL;
                int pctInt   = (int) Math.round(pct * 100);

                pbCapacidad.setProgress(pct);
                lblCapacidadMeta.setText(activos + " internos · " + libres + " plazas libres");
                lblCapacidadPct.setText("Capacidad " + pctInt + "%");
            }, Platform::runLater);
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
