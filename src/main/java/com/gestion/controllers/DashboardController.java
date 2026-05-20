package com.gestion.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.gestion.services.CobranzasService;
import com.gestion.services.InternoService;
import com.gestion.services.ReportesService;
import com.gestion.ui.CapacityRing;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
    @FXML private VBox      listCobranzas;
    @FXML private VBox      listAltas;
    @FXML private StackPane capacityRingHost;
    @FXML private Label     lblOcupadasValor;
    @FXML private Label     lblLibresValor;
    @FXML private Label     lblCapacidadTotal;

    private static final int CAPACIDAD_TOTAL = 40;
    private CapacityRing capacityRing;

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d 'de' MMMM yyyy", Locale.of("es", "AR"));
        lblFecha.setText("Hoy, " + LocalDate.now().format(fmt));

        buildCapacityRing();

        cargarInternosActivos();
        cargarIngresosMes();
        cargarDeudaTotal();
        cargarCuotasRecientes();
        cargarProximasAltas();
    }

    private void buildCapacityRing() {
        capacityRing = new CapacityRing(190, 18);
        capacityRing.setSubText("ocupación");
        capacityRingHost.getChildren().setAll(capacityRing);
        lblCapacidadTotal.setText("Capacidad total: " + CAPACIDAD_TOTAL + " plazas");
    }

    private void cargarInternosActivos() {
        CompletableFuture
            .supplyAsync(() -> InternoService.listar("activo", null, 1))
            .thenAcceptAsync(result -> {
                if (result.success) {
                    int activos = result.total;
                    int libres  = Math.max(0, CAPACIDAD_TOTAL - activos);
                    double pct  = (double) activos / CAPACIDAD_TOTAL;

                    lblInternosActivos.setText(String.valueOf(activos));
                    lblInternosDelta.setText("activos en tratamiento");

                    lblOcupadasValor.setText(String.valueOf(activos));
                    lblLibresValor.setText(String.valueOf(libres));
                    if (capacityRing != null) capacityRing.setProgress(pct, true);
                } else {
                    lblInternosActivos.setText("—");
                    lblInternosDelta.setText("Sin conexión");
                    lblOcupadasValor.setText("—");
                    lblLibresValor.setText("—");
                }
            }, Platform::runLater);
    }

    private void cargarIngresosMes() {
        LocalDate hoy = LocalDate.now();
        CompletableFuture
            .supplyAsync(() -> ReportesService.ingresosMes(hoy.getYear(), hoy.getMonthValue()))
            .thenAcceptAsync(result -> {
                if (result.success && result.data != null) {
                    JsonNode data = result.data;
                    String total = data.has("total_general") ? "$" + data.get("total_general").asText() : "—";
                    lblCobranzasMes.setText(total);
                    lblCobranzasDelta.setText("ingresos " + hoy.getMonth().getDisplayName(
                            java.time.format.TextStyle.FULL, Locale.of("es", "AR")));
                } else {
                    lblCobranzasMes.setText("—");
                    lblCobranzasDelta.setText("Sin datos");
                }
            }, Platform::runLater);
    }

    private void cargarDeudaTotal() {
        CompletableFuture
            .supplyAsync(ReportesService::deudaTotal)
            .thenAcceptAsync(result -> {
                if (result.success && result.data != null) {
                    JsonNode data = result.data;
                    lblPendientes.setText(data.has("cantidad_deudores") ? data.get("cantidad_deudores").asText() : "—");
                    lblPendientesDelta.setText("internos con deuda");
                    String deuda = data.has("deuda_total") ? "$" + data.get("deuda_total").asText() : "—";
                    lblPlazas.setText(deuda);
                    lblPlazasDelta.setText("deuda total acumulada");
                } else {
                    lblPendientes.setText("—");
                    lblPendientesDelta.setText("Sin datos");
                    lblPlazas.setText("—");
                    lblPlazasDelta.setText("");
                }
            }, Platform::runLater);
    }

    private void cargarCuotasRecientes() {
        LocalDate hoy = LocalDate.now();
        CompletableFuture
            .supplyAsync(() -> CobranzasService.listarCuotas(null, hoy.getYear(), hoy.getMonthValue(), 1))
            .thenAcceptAsync(result -> {
                listCobranzas.getChildren().clear();
                if (!result.success || result.data == null) return;

                result.data.stream().limit(4).forEach(cuota -> {
                    String fv    = cuota.getFechaVencimiento();
                    String fecha = fv.length() >= 10 ? fv.substring(5, 10).replace("-", "/") : fv;
                    String nombre = cuota.getInternoNombre();
                    String monto  = "$" + cuota.getTotalConInteres();
                    String estado = cuota.getEstadoDisplay();
                    listCobranzas.getChildren().add(filaCobranza(fecha, nombre, cuota.getMesPeriodo(), monto, estado));
                });
            }, Platform::runLater);
    }

    private void cargarProximasAltas() {
        CompletableFuture
            .supplyAsync(() -> InternoService.listar("alta", null, 1))
            .thenAcceptAsync(result -> {
                listAltas.getChildren().clear();
                if (!result.success || result.data == null) return;

                result.data.stream().limit(3).forEach(i -> {
                    String fecha = i.getFechaEgreso() != null && i.getFechaEgreso().length() >= 10
                            ? i.getFechaEgreso().substring(5, 10).replace("-", "/") : "—";
                    listAltas.getChildren().add(filaAlta(fecha, i.getNombreCompleto(), i.getMotivoEgreso() != null ? i.getMotivoEgreso() : "Alta médica"));
                });
            }, Platform::runLater);
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
        Label lNombre = new Label(nombre);   lNombre.getStyleClass().add("mini-title");
        Label lSub    = new Label(concepto); lSub.getStyleClass().add("mini-sub");
        main.getChildren().addAll(lNombre, lSub);

        Label lMonto  = new Label(monto);  lMonto.getStyleClass().add("mini-amt");
        Label lEstado = new Label(estado);
        lEstado.getStyleClass().add(switch (estado) {
            case "Pagada"   -> "estado-activo";
            case "Parcial"  -> "estado-permiso";
            default         -> "estado-baja";
        });

        row.getChildren().addAll(lFecha, main, lMonto, lEstado);
        return row;
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
        if (mainController != null) mainController.navegarACobranzas();
    }
}
