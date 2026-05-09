package com.gestion.controllers;

import com.gestion.config.AppConfig;
import com.gestion.models.Egreso;
import com.gestion.services.CobranzasService;
import com.gestion.utils.AlertHelper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PagosController {

    @FXML private ProgressIndicator  progressIndicator;
    @FXML private Button             btnNuevo;
    @FXML private TextField          txtBusqueda;
    @FXML private ComboBox<String>   cmbEstado;
    @FXML private Label              lblTotal;

    @FXML private TableView<Egreso>             tablaPagos;
    @FXML private TableColumn<Egreso, String>   colFecha;
    @FXML private TableColumn<Egreso, String>   colProveedor;
    @FXML private TableColumn<Egreso, String>   colConcepto;
    @FXML private TableColumn<Egreso, String>   colMonto;
    @FXML private TableColumn<Egreso, String>   colEstado;

    private final ObservableList<Egreso> datos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        boolean puedeEscribir = AppConfig.tieneRol("admin", "operador", "contador");
        btnNuevo.setVisible(puedeEscribir);
        btnNuevo.setManaged(puedeEscribir);

        cmbEstado.setItems(FXCollections.observableArrayList("Todos", "efectivo", "transferencia"));
        cmbEstado.setValue("Todos");

        configurarColumnas();
        tablaPagos.setItems(datos);

        cmbEstado.setOnAction(e -> cargarDatos());

        cargarDatos();
    }

    private void configurarColumnas() {
        colFecha.setCellValueFactory(c     -> new SimpleStringProperty(c.getValue().getFecha()));
        colProveedor.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRegistradoPor()));
        colConcepto.setCellValueFactory(c  -> new SimpleStringProperty(
                c.getValue().getDescripcion() + " [" + c.getValue().getCategoria() + "]"));
        colMonto.setCellValueFactory(c     -> new SimpleStringProperty("$" + c.getValue().getMonto()));
        colEstado.setCellValueFactory(c    -> new SimpleStringProperty(c.getValue().getMedioPago()));
    }

    private void cargarDatos() {
        setLoading(true);
        datos.clear();

        String medio = "Todos".equals(cmbEstado.getValue()) ? null : cmbEstado.getValue();

        CompletableFuture
            .supplyAsync(() -> CobranzasService.listarEgresos(null, null, medio, 1))
            .thenAcceptAsync(result -> {
                setLoading(false);
                if (!result.success) {
                    AlertHelper.error("Error al cargar egresos: " + result.errorMensaje);
                    return;
                }
                List<Egreso> egresos = result.data;

                if (txtBusqueda != null && !txtBusqueda.getText().trim().isEmpty()) {
                    String filtro = txtBusqueda.getText().trim().toLowerCase();
                    egresos = egresos.stream()
                            .filter(e -> e.getDescripcion().toLowerCase().contains(filtro)
                                      || e.getCategoria().toLowerCase().contains(filtro))
                            .toList();
                }

                datos.addAll(egresos);
                lblTotal.setText("Total: " + result.total + " registros");
            }, Platform::runLater);
    }

    @FXML private void onNuevo() {
        AlertHelper.info("Registro de egresos disponible próximamente.");
    }

    @FXML private void onExportar() {
        AlertHelper.info("Exportación disponible próximamente.");
    }

    private void setLoading(boolean loading) {
        progressIndicator.setVisible(loading);
        tablaPagos.setDisable(loading);
    }
}
