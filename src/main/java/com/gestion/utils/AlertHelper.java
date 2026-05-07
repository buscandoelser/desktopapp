package com.gestion.utils;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.gestion.config.AppConfig;

public final class AlertHelper {

    public enum TipoToast { EXITO, ERROR, INFO, ADVERTENCIA }

    private AlertHelper() {}

    // ── Toast (notificación esquina inferior derecha) ─────────
    public static void toast(String mensaje, TipoToast tipo) {
        Platform.runLater(() -> {
            Stage stage = AppConfig.getPrimaryStage();
            if (stage == null) return;

            Label lbl = new Label(icono(tipo) + "  " + mensaje);
            lbl.setStyle(
                "-fx-font-size: 15px;" +
                "-fx-text-fill: white;" +
                "-fx-padding: 14 20 14 20;"
            );

            StackPane container = new StackPane(lbl);
            container.setStyle(
                "-fx-background-color: " + colorFondo(tipo) + ";" +
                "-fx-background-radius: 10;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 12, 0, 0, 4);"
            );
            container.setPrefWidth(340);
            container.setAlignment(Pos.CENTER_LEFT);

            Popup popup = new Popup();
            popup.getContent().add(container);
            popup.setAutoHide(true);

            // Posición: esquina inferior derecha
            double x = stage.getX() + stage.getWidth()  - 360;
            double y = stage.getY() + stage.getHeight()  - 100;
            popup.show(stage, x, y);

            // Fade out después de 3 segundos
            FadeTransition fade = new FadeTransition(Duration.millis(600), container);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setDelay(Duration.seconds(3));
            fade.setOnFinished(e -> popup.hide());
            fade.play();
        });
    }

    public static void exito(String msg)       { toast(msg, TipoToast.EXITO); }
    public static void error(String msg)        { toast(msg, TipoToast.ERROR); }
    public static void info(String msg)         { toast(msg, TipoToast.INFO); }
    public static void advertencia(String msg)  { toast(msg, TipoToast.ADVERTENCIA); }

    // ── Diálogo de confirmación simple ───────────────────────
    public static boolean confirmar(String titulo, String mensaje) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION
        );
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        Stage stage = AppConfig.getPrimaryStage();
        if (stage != null) alert.initOwner(stage);

        var result = alert.showAndWait();
        return result.isPresent() &&
               result.get() == javafx.scene.control.ButtonType.OK;
    }

    // ── Helpers privados ──────────────────────────────────────
    private static String icono(TipoToast tipo) {
        return switch (tipo) {
            case EXITO        -> "✓";
            case ERROR        -> "✕";
            case ADVERTENCIA  -> "⚠";
            case INFO         -> "ℹ";
        };
    }

    private static String colorFondo(TipoToast tipo) {
        return switch (tipo) {
            case EXITO        -> "#2E7D32";
            case ERROR        -> "#C62828";
            case ADVERTENCIA  -> "#E65100";
            case INFO         -> "#1565C0";
        };
    }
}
