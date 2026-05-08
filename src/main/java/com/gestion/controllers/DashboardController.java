package com.gestion.controllers;

import com.gestion.services.InternoService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class DashboardController {

    @FXML private Label lblFecha;
    @FXML private Label lblInternosActivos;
    @FXML private Label lblInternosDelta;
    @FXML private Label lblCobranzasMes;
    @FXML private Label lblCobranzasDelta;
    @FXML private Label lblPendientes;
    @FXML private Label lblPendientesDelta;
    @FXML private Label lblPlazas;
    @FXML private Label lblPlazasDelta;
    @FXML private VBox  listCobranzas;
    @FXML private VBox  listAltas;

    // Referencia al MainController para navegación entre secciones
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d 'de' MMMM yyyy", Locale.of("es", "AR"));
        lblFecha.setText("Hoy, " + LocalDate.now().format(fmt));

        cargarInternosActivos();
        poblarCobranzasRecientes();
        poblarProximasAltas();

        // Pendiente hasta que exista CobranzasService
        lblCobranzasMes.setText("—");
        lblCobranzasDelta.setText("Módulo en desarrollo");
        lblPendientes.setText("—");
        lblPendientesDelta.setText("");
        lblPlazas.setText("—");
        lblPlazasDelta.setText("Pendiente config.");
    }

    private void cargarInternosActivos() {
        CompletableFuture
            .supplyAsync(() -> InternoService.listar("activo", null, 1))
            .thenAcceptAsync(result -> {
                if (result.success) {
                    lblInternosActivos.setText(String.valueOf(result.total));
                    lblInternosDelta.setText("activos en tratamiento");
                } else {
                    lblInternosActivos.setText("—");
                    lblInternosDelta.setText("Sin conexión al servidor");
                }
            }, Platform::runLater);
    }

    private void poblarCobranzasRecientes() {
        // Demo — reemplazar con CobranzasService cuando esté disponible
        Object[][] filas = {
            { "07/05", "Cristian Ramírez",  "Cuota mayo",    "$285.000", "Cobrado"   },
            { "06/05", "Mariano Ferreyra",  "Coseguro O.S.", "$42.000",  "Cobrado"   },
            { "05/05", "Lucía Domínguez",   "Cuota mayo",    "$285.000", "Pendiente" },
            { "04/05", "Daniela Ríos",      "Cuota mayo",    "$220.000", "Cobrado"   },
        };
        for (Object[] f : filas) {
            listCobranzas.getChildren().add(
                filaCobranza((String)f[0], (String)f[1], (String)f[2], (String)f[3], (String)f[4])
            );
        }
    }

    private HBox filaCobranza(String fecha, String nombre, String concepto, String monto, String estado) {
        HBox row = new HBox(12);
        row.getStyleClass().add("mini-row");
        row.setPadding(new Insets(11, 16, 11, 16));

        Label lFecha = new Label(fecha);
        lFecha.getStyleClass().add("mini-date");
        lFecha.setMinWidth(44);

        VBox main = new VBox(2);
        HBox.setHgrow(main, Priority.ALWAYS);
        Label lNombre  = new Label(nombre);  lNombre.getStyleClass().add("mini-title");
        Label lSub     = new Label(concepto); lSub.getStyleClass().add("mini-sub");
        main.getChildren().addAll(lNombre, lSub);

        Label lMonto = new Label(monto);
        lMonto.getStyleClass().add("mini-amt");

        Label lEstado = new Label(estado);
        lEstado.getStyleClass().add(switch (estado) {
            case "Cobrado"   -> "estado-activo";
            case "Pendiente" -> "estado-permiso";
            default          -> "estado-baja";
        });

        row.getChildren().addAll(lFecha, main, lMonto, lEstado);
        return row;
    }

    private void poblarProximasAltas() {
        Object[][] filas = {
            { "12/05", "Romina Barrios",  "Alta médica"      },
            { "14/05", "Camilo Espinosa", "Fin tratamiento"  },
            { "18/05", "Soledad Ibarra",  "Derivación"       },
        };
        for (Object[] f : filas) {
            listAltas.getChildren().add(filaAlta((String)f[0], (String)f[1], (String)f[2]));
        }
    }

    private HBox filaAlta(String fecha, String nombre, String motivo) {
        HBox row = new HBox(12);
        row.getStyleClass().add("mini-row");
        row.setPadding(new Insets(11, 16, 11, 16));

        Label lFecha = new Label(fecha);
        lFecha.getStyleClass().add("mini-date");
        lFecha.setMinWidth(44);

        VBox main = new VBox(2);
        HBox.setHgrow(main, Priority.ALWAYS);
        Label lNombre = new Label(nombre); lNombre.getStyleClass().add("mini-title");
        Label lMotivo = new Label(motivo); lMotivo.getStyleClass().add("mini-sub");
        main.getChildren().addAll(lNombre, lMotivo);

        row.getChildren().addAll(lFecha, main);
        return row;
    }

    @FXML
    private void onVerCobranzas() {
        if (mainController != null) {
            mainController.navegarACobranzas();
        }
    }
}
