package com.gestion.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.gestion.services.CobranzasService;
import com.gestion.services.ConfigService;
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
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class DashboardController {

    @FXML private Label     lblFecha;
    @FXML private Label     lblCobranzasMes;
    @FXML private Label     lblCobranzasDelta;
    @FXML private Label     lblPlazas;
    @FXML private Label     lblPlazasDelta;
    @FXML private VBox      listCobranzas;

    // Ring 1: ocupación
    @FXML private StackPane capacityRingHost;
    @FXML private Label     lblOcupadasValor;
    @FXML private Label     lblLibresValor;
    @FXML private Label     lblCapacidadTotal;

    // Ring 2: deuda
    @FXML private StackPane deudaRingHost;
    @FXML private Label     lblConDeudaValor;
    @FXML private Label     lblAlDiaValor;
    @FXML private Label     lblTotalInternosDeuda;

    private static final int CAPACIDAD_DEFAULT = 40;
    private int capacidadTotal = CAPACIDAD_DEFAULT;

    private CapacityRing capacityRing;
    private CapacityRing deudaRing;

    // Coordinated state for deuda ring (loaded asynchronously from 2 sources)
    private Integer totalInternosActivos = null;
    private Integer cantidadDeudores     = null;

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d 'de' MMMM yyyy", Locale.of("es", "AR"));
        lblFecha.setText("Hoy, " + LocalDate.now().format(fmt));

        buildRings();

        cargarCapacidadTotal();
        cargarIngresosMes();
        cargarDeudaTotal();
        cargarCuotasRecientes();
    }

    private void cargarCapacidadTotal() {
        CompletableFuture
            .supplyAsync(ConfigService::getCamas)
            .thenAcceptAsync(result -> {
                if (result.success && result.data != null) {
                    capacidadTotal = result.data.path("total").asInt(
                            result.data.path("valor").asInt(CAPACIDAD_DEFAULT));
                }
                lblCapacidadTotal.setText("Capacidad total: " + capacidadTotal + " plazas");
                cargarInternosActivos();
            }, Platform::runLater);
    }

    // ── Rings setup ───────────────────────────────────────────────────────

    private void buildRings() {
        // Capacity ring (amber)
        capacityRing = new CapacityRing(190, 18);
        capacityRing.setSubText("ocupación");
        capacityRingHost.getChildren().setAll(capacityRing);
        lblCapacidadTotal.setText("Capacidad total: " + capacidadTotal + " plazas");

        // Deuda ring (danger / coral red gradient)
        deudaRing = new CapacityRing(190, 18);
        deudaRing.setSubText("con deuda");
        deudaRing.setProgressPaint(new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.web("#E07B58")),
            new Stop(1.0, Color.web("#A8412E"))
        ));
        deudaRingHost.getChildren().setAll(deudaRing);
    }

    // ── Data loaders ──────────────────────────────────────────────────────

    private void cargarInternosActivos() {
        CompletableFuture
            .supplyAsync(() -> InternoService.listar("activo", null, 1))
            .thenAcceptAsync(result -> {
                if (result.success) {
                    int activos = result.total;
                    int libres  = Math.max(0, capacidadTotal - activos);
                    double pct  = capacidadTotal > 0 ? (double) activos / capacidadTotal : 0;

                    lblOcupadasValor.setText(String.valueOf(activos));
                    lblLibresValor.setText(String.valueOf(libres));
                    if (capacityRing != null) capacityRing.setProgress(pct, true);

                    totalInternosActivos = activos;
                    actualizarDeudaRing();
                } else {
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
                    String deuda = data.has("deuda_total") ? "$" + data.get("deuda_total").asText() : "—";
                    lblPlazas.setText(deuda);
                    lblPlazasDelta.setText("monto pendiente de cobro");

                    cantidadDeudores = data.has("cantidad_deudores") ? data.get("cantidad_deudores").asInt() : 0;
                    actualizarDeudaRing();
                } else {
                    lblPlazas.setText("—");
                    lblPlazasDelta.setText("Sin datos");
                }
            }, Platform::runLater);
    }

    /** Recalculates the deuda ring once both internos count and deudores count have arrived. */
    private void actualizarDeudaRing() {
        if (totalInternosActivos == null || cantidadDeudores == null) return;
        if (deudaRing == null) return;

        int total = totalInternosActivos;
        int conDeuda = Math.min(cantidadDeudores, total);
        int alDia    = Math.max(0, total - conDeuda);
        double pct   = total > 0 ? (double) conDeuda / total : 0;

        lblConDeudaValor.setText(String.valueOf(conDeuda));
        lblAlDiaValor.setText(String.valueOf(alDia));
        lblTotalInternosDeuda.setText("Total de internos: " + total);
        deudaRing.setProgress(pct, true);
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

    @FXML
    private void onVerCobranzas() {
        if (mainController != null) mainController.navegarACobranzas();
    }
}
