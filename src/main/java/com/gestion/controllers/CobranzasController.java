package com.gestion.controllers;

import com.gestion.config.AppConfig;
import com.gestion.models.Cuota;
import com.gestion.services.CobranzasService;
import com.gestion.utils.AlertHelper;
import com.gestion.utils.ThemeManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CobranzasController {

    @FXML private ProgressIndicator   progressIndicator;
    @FXML private Button              btnRegistrar;
    @FXML private TextField           txtBusqueda;
    @FXML private ComboBox<String>    cmbMes;
    @FXML private ComboBox<String>    cmbEstado;
    @FXML private Label               lblTotal;

    @FXML private Label lblCobrado;
    @FXML private Label lblCobradoDelta;
    @FXML private Label lblPendiente;
    @FXML private Label lblPendienteDelta;
    @FXML private Label lblVencido;
    @FXML private Label lblVencidoDelta;
    @FXML private Label lblTasa;
    @FXML private Label lblTasaDelta;

    @FXML private TableView<Cuota>              tablaCobranzas;
    @FXML private TableColumn<Cuota, String>    colFecha;
    @FXML private TableColumn<Cuota, String>    colInterno;
    @FXML private TableColumn<Cuota, String>    colConcepto;
    @FXML private TableColumn<Cuota, String>    colMonto;
    @FXML private TableColumn<Cuota, String>    colSaldo;
    @FXML private TableColumn<Cuota, String>    colEstado;
    @FXML private TableColumn<Cuota, Void>      colAcciones;

    private final ObservableList<Cuota> datos = FXCollections.observableArrayList();
    private List<Cuota> todosLosDatos = List.of();

    private static final String[] MESES_NOMBRE = {
        "Todos","Enero","Febrero","Marzo","Abril","Mayo","Junio",
        "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"
    };

    // Label visible → valor API
    private static final Map<String, String> ESTADOS = new LinkedHashMap<>();
    static {
        ESTADOS.put("Todos",    null);
        ESTADOS.put("Pendiente","pendiente");
        ESTADOS.put("Parcial",  "pagada_parcial");
        ESTADOS.put("Pagada",   "pagada");
        ESTADOS.put("Con mora", "con_mora");
    }

    @FXML
    public void initialize() {
        boolean puedeEscribir = AppConfig.tieneRol("admin", "operador", "contador");
        btnRegistrar.setVisible(puedeEscribir);
        btnRegistrar.setManaged(puedeEscribir);

        cmbMes.setItems(FXCollections.observableArrayList(MESES_NOMBRE));
        cmbMes.setValue(MESES_NOMBRE[LocalDate.now().getMonthValue()]);

        cmbEstado.setItems(FXCollections.observableArrayList(ESTADOS.keySet()));
        cmbEstado.setValue("Todos");

        configurarColumnas();
        tablaCobranzas.setItems(datos);

        cmbMes.setOnAction(e -> cargarDatos());
        cmbEstado.setOnAction(e -> cargarDatos());
        txtBusqueda.textProperty().addListener((obs, o, n) -> filtrarLocal());

        cargarDatos();
    }

    private void configurarColumnas() {
        colFecha.setCellValueFactory(c    -> new SimpleStringProperty(c.getValue().getFechaVencimiento()));
        colInterno.setCellValueFactory(c  -> new SimpleStringProperty(
                c.getValue().getInternoNombre() + "  ·  " + c.getValue().getLegajo()));
        colConcepto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMesPeriodo()));
        colMonto.setCellValueFactory(c    -> new SimpleStringProperty("$" + c.getValue().getTotalConInteres()));
        colSaldo.setCellValueFactory(c    -> new SimpleStringProperty("$" + c.getValue().getSaldoPendiente()));
        colEstado.setCellValueFactory(c   -> new SimpleStringProperty(c.getValue().getEstadoDisplay()));

        colSaldo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                // Saldo cero → verde, saldo pendiente → amarillo
                boolean sinDeuda = item.equals("$0") || item.equals("$0.00") || item.equals("$0,00");
                setStyle(sinDeuda ? "-fx-text-fill: #4ade80;" : "-fx-text-fill: #facc15; -fx-font-weight: 600;");
            }
        });

        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                String color = switch (item) {
                    case "Pagada"   -> "-fx-text-fill: #4ade80;";
                    case "Con mora" -> "-fx-text-fill: #f87171;";
                    case "Parcial"  -> "-fx-text-fill: #facc15;";
                    default         -> "-fx-text-fill: #94a3b8;";
                };
                setStyle(color);
            }
        });

        boolean puedePagar = AppConfig.tieneRol("admin", "operador", "contador");
        colAcciones.setCellFactory(tc -> new TableCell<>() {
            private final Button btnPagar = new Button("Pagar");
            {
                btnPagar.getStyleClass().add("btn-sm-success");
                btnPagar.setOnAction(e -> abrirModal(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Cuota c = getTableView().getItems().get(getIndex());
                HBox box = new HBox();
                if (puedePagar && esPagable(c.getEstado())) box.getChildren().add(btnPagar);
                setGraphic(box);
            }
        });
    }

    private boolean esPagable(String estado) {
        return "pendiente".equals(estado)
            || "pagada_parcial".equals(estado)
            || "con_mora".equals(estado);
    }

    private void cargarDatos() {
        setLoading(true);
        datos.clear();

        int mesIdx = cmbMes.getSelectionModel().getSelectedIndex();
        int anio   = mesIdx > 0 ? LocalDate.now().getYear() : 0;
        int mes    = mesIdx > 0 ? mesIdx : 0;
        String estadoApi = ESTADOS.get(cmbEstado.getValue());

        CompletableFuture
            .supplyAsync(() -> CobranzasService.listarCuotas(estadoApi, anio, mes, 1))
            .thenAcceptAsync(result -> {
                setLoading(false);
                if (!result.success) {
                    AlertHelper.error("Error al cargar cobranzas: " + result.errorMensaje);
                    return;
                }
                todosLosDatos = result.data;
                actualizarStats(result.data);
                lblTotal.setText("Total: " + result.total + " cuotas");
                filtrarLocal();
            }, Platform::runLater)
            .exceptionally(ex -> {
                ex.printStackTrace();
                Platform.runLater(() -> AlertHelper.error("Excepción inesperada: " + ex.getMessage()));
                return null;
            });
    }

    private void filtrarLocal() {
        String filtro = txtBusqueda.getText().trim().toLowerCase();
        datos.clear();
        if (filtro.isEmpty()) {
            datos.addAll(todosLosDatos);
        } else {
            todosLosDatos.stream()
                    .filter(c -> c.getInternoNombre().toLowerCase().contains(filtro)
                              || c.getLegajo().toLowerCase().contains(filtro)
                              || c.getMesPeriodo().toLowerCase().contains(filtro))
                    .forEach(datos::add);
        }
    }

    private void actualizarStats(List<Cuota> cuotas) {
        double cobrado = 0, pendiente = 0, mora = 0;
        int    pagadas = 0;

        for (Cuota c : cuotas) {
            cobrado   += parseDouble(c.getMontoPagado());
            pendiente += parseDouble(c.getSaldoPendiente());
            if ("con_mora".equals(c.getEstado()))  mora += parseDouble(c.getSaldoPendiente());
            if ("pagada".equals(c.getEstado()))    pagadas++;
        }

        long conMora = cuotas.stream().filter(c -> "con_mora".equals(c.getEstado())).count();
        double tasa  = cuotas.isEmpty() ? 0 : (double) pagadas / cuotas.size() * 100;

        lblCobrado.setText(formatMonto(cobrado));
        lblCobradoDelta.setText(pagadas + " cuotas pagadas");
        lblPendiente.setText(formatMonto(pendiente));
        lblPendienteDelta.setText((cuotas.size() - pagadas) + " cuotas pendientes");
        lblVencido.setText(formatMonto(mora));
        lblVencidoDelta.setText(conMora + " con mora");
        lblTasa.setText(String.format("%.1f%%", tasa));
        lblTasaDelta.setText("tasa de cobro");
    }

    @FXML private void onRegistrar() { abrirModal(null); }

    @FXML private void onExportar() {
        AlertHelper.info("Exportación CSV disponible próximamente.");
    }

    private void abrirModal(Cuota cuota) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/RegistrarCobroModal.fxml"));
            javafx.scene.Parent root = loader.load();

            RegistrarCobroModalController ctrl = loader.getController();
            ctrl.setOnExito(this::cargarDatos);
            if (cuota != null) ctrl.setCuotaPreseleccionada(cuota);

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(AppConfig.getPrimaryStage());
            modal.setTitle("Registrar cobro");

            Scene scene = new Scene(root);
            ThemeManager.apply(scene);
            modal.setScene(scene);
            modal.setMinWidth(560);
            modal.setMinHeight(640);
            modal.centerOnScreen();
            modal.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.error("Error al abrir ventana de cobro: " + e.getMessage());
        }
    }

    private void setLoading(boolean loading) {
        progressIndicator.setVisible(loading);
        tablaCobranzas.setDisable(loading);
    }

    private double parseDouble(String s) {
        try { return s != null ? Double.parseDouble(s) : 0.0; } catch (Exception e) { return 0.0; }
    }

    private String formatMonto(double valor) {
        return String.format("$%,.0f", valor).replace(",", ".");
    }
}
