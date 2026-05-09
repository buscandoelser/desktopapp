package com.gestion.controllers;

import com.gestion.config.AppConfig;
import com.gestion.models.Cuota;
import com.gestion.services.CobranzasService;
import com.gestion.utils.AlertHelper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.List;
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
    @FXML private TableColumn<Cuota, String>    colMetodo;
    @FXML private TableColumn<Cuota, String>    colEstado;

    private final ObservableList<Cuota> datos = FXCollections.observableArrayList();

    private static final String[] MESES_NOMBRE = {
        "Todos","Enero","Febrero","Marzo","Abril","Mayo","Junio",
        "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"
    };

    @FXML
    public void initialize() {
        boolean puedeEscribir = AppConfig.tieneRol("admin", "operador", "contador");
        btnRegistrar.setVisible(puedeEscribir);
        btnRegistrar.setManaged(puedeEscribir);

        cmbMes.setItems(FXCollections.observableArrayList(MESES_NOMBRE));
        cmbMes.setValue(MESES_NOMBRE[LocalDate.now().getMonthValue()]);

        cmbEstado.setItems(FXCollections.observableArrayList("Todos", "pendiente", "parcial", "pagada", "con_mora"));
        cmbEstado.setValue("Todos");

        configurarColumnas();
        tablaCobranzas.setItems(datos);

        cmbMes.setOnAction(e -> cargarDatos());
        cmbEstado.setOnAction(e -> cargarDatos());

        cargarDatos();
    }

    private void configurarColumnas() {
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaVencimiento()));
        colInterno.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getInternoNombre()));
        colConcepto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMesPeriodo()));
        colMonto.setCellValueFactory(c -> new SimpleStringProperty("$" + c.getValue().getMontoOriginal()));
        colMetodo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSaldoPendiente() != null
                ? "Saldo: $" + c.getValue().getSaldoPendiente() : "—"));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstadoDisplay()));

        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                String color;
                if ("Pagada".equals(item))        color = "-fx-text-fill: #4ade80;";
                else if ("Con mora".equals(item)) color = "-fx-text-fill: #f87171;";
                else if ("Parcial".equals(item))  color = "-fx-text-fill: #facc15;";
                else                              color = "-fx-text-fill: #94a3b8;";
                setStyle(color);
            }
        });
    }

    private void cargarDatos() {
        setLoading(true);
        datos.clear();

        int mesIdx   = cmbMes.getSelectionModel().getSelectedIndex();
        int anio     = mesIdx > 0 ? LocalDate.now().getYear() : 0;
        int mes      = mesIdx > 0 ? mesIdx : 0;
        String estado = "Todos".equals(cmbEstado.getValue()) ? null : cmbEstado.getValue();

        CompletableFuture
            .supplyAsync(() -> CobranzasService.listarCuotas(estado, anio, mes, 1))
            .thenAcceptAsync(result -> {
                setLoading(false);
                if (!result.success) {
                    AlertHelper.error("Error al cargar cobranzas: " + result.errorMensaje);
                    return;
                }
                List<Cuota> cuotas = result.data;
                datos.addAll(cuotas);
                lblTotal.setText("Total: " + result.total + " cuotas");
                actualizarStats(cuotas);
            }, Platform::runLater);
    }

    private void actualizarStats(List<Cuota> cuotas) {
        double cobrado   = 0, pendiente = 0, mora = 0;
        int    pagadas   = 0;

        for (Cuota c : cuotas) {
            double pagado   = parseDouble(c.getMontoPagado());
            cobrado   += pagado;
            pendiente += parseDouble(c.getSaldoPendiente());
            if ("con_mora".equals(c.getEstado())) mora += parseDouble(c.getSaldoPendiente());
            if ("pagada".equals(c.getEstado())) pagadas++;
        }

        double tasa = cuotas.isEmpty() ? 0 : (double) pagadas / cuotas.size() * 100;

        lblCobrado.setText(formatMonto(cobrado));
        lblCobradoDelta.setText(pagadas + " cuotas pagadas");
        lblPendiente.setText(formatMonto(pendiente));
        lblPendienteDelta.setText((cuotas.size() - pagadas) + " cuotas pendientes");
        lblVencido.setText(formatMonto(mora));
        lblVencidoDelta.setText(cuotas.stream().filter(c -> "con_mora".equals(c.getEstado())).count() + " con mora");
        lblTasa.setText(String.format("%.1f%%", tasa));
        lblTasaDelta.setText("tasa de cobro");
    }

    @FXML private void onRegistrar() {
        AlertHelper.info("Registro de cobros disponible próximamente.");
    }

    @FXML private void onExportar() {
        AlertHelper.info("Exportación CSV disponible próximamente.");
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
