package com.gestion.controllers;

import com.gestion.config.AppConfig;
import com.gestion.services.AuthService;
import com.gestion.ui.InteractiveDock;
import com.gestion.ui.WindowControls;
import com.gestion.utils.AlertHelper;
import com.gestion.utils.ThemeManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class MainController {

    @FXML private BorderPane rootPane;
    @FXML private HBox       header;
    @FXML private HBox       windowControlsHost;
    @FXML private Label      lblUsuarioNombre;
    @FXML private Label      lblUsuarioRol;
    @FXML private StackPane  contenidoPrincipal;

    private InteractiveDock dock;

    @FXML
    public void initialize() {
        lblUsuarioNombre.setText(AppConfig.getUsuarioNombre());
        lblUsuarioRol.setText(rolDisplay(AppConfig.getUsuarioRol()));

        buildDock();
        setupWindowChrome();
    }

    // ── Window chrome (UNDECORATED stage) ─────────────────────────────────

    private void setupWindowChrome() {
        Stage stage = AppConfig.getPrimaryStage();
        if (stage == null) return;

        windowControlsHost.getChildren().setAll(
                WindowControls.createControls(stage, true)
        );
        WindowControls.attachDragHandler(header, stage);
    }

    // ── Dock setup ────────────────────────────────────────────────────────

    private void buildDock() {
        List<InteractiveDock.DockItem> items = new ArrayList<>();

        items.add(new InteractiveDock.DockItem("dashboard",  "Panel",     InteractiveDock.SVG_HOME));
        items.add(new InteractiveDock.DockItem("internos",   "Internos",  InteractiveDock.SVG_PEOPLE));

        if (AppConfig.tieneRol("admin", "operador", "contador")) {
            items.add(new InteractiveDock.DockItem("cobranzas", "Cobranzas", InteractiveDock.SVG_MONEY));
            items.add(new InteractiveDock.DockItem("pagos",     "Pagos",     InteractiveDock.SVG_CARD));
        }

        items.add(new InteractiveDock.DockItem("reportes", "Reportes", InteractiveDock.SVG_CHART));

        if (AppConfig.tieneRol("admin")) {
            items.add(new InteractiveDock.DockItem("configuracion", "Config", InteractiveDock.SVG_GEAR));
        }

        dock = new InteractiveDock(items);
        dock.setOnSelectionChanged(this::cargarModulo);
        rootPane.setBottom(dock);

        dock.selectItem(0);
    }

    // ── Navigation ────────────────────────────────────────────────────────

    private void cargarModulo(String modulo) {
        try {
            String fxmlPath = "/fxml/" + capitalize(modulo) + ".fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node contenido = loader.load();

            if ("dashboard".equals(modulo)) {
                DashboardController dc = loader.getController();
                dc.setMainController(this);
            }

            contenidoPrincipal.getChildren().setAll(contenido);

        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.error("Error al abrir módulo '" + modulo + "':\n" + e.getMessage());
        }
    }

    public void navegarACobranzas() {
        dock.selectById("cobranzas");
    }

    // ── Logout ────────────────────────────────────────────────────────────

    @FXML
    private void onCerrarSesion() {
        if (!AlertHelper.confirmar("Cerrar sesión", "¿Querés cerrar la sesión actual?")) return;

        AuthService.logout();
        AppConfig.clearSession();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 860, 560);
            ThemeManager.apply(scene);

            Stage stage = AppConfig.getPrimaryStage();
            // Unmaximize and reset min size BEFORE applying new dimensions —
            // otherwise the 1200×700 minimums set on login keep the stage huge.
            stage.setMaximized(false);
            stage.setResizable(false);
            stage.setMinWidth(0);
            stage.setMinHeight(0);
            stage.setScene(scene);
            stage.setWidth(860);
            stage.setHeight(560);
            stage.centerOnScreen();
        } catch (Exception e) {
            AlertHelper.error("Error al cerrar sesión: " + e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

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
