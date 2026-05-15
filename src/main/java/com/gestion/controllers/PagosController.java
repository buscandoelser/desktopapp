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
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PagosController {

    @FXML private ProgressIndicator             progressIndicator;
    @FXML private Button                        btnNuevo;
    @FXML private TextField                     txtBusqueda;
    @FXML private ComboBox<String>              cmbEstado;
    @FXML private Label                         lblTotal;

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
        txtBusqueda.textProperty().addListener((obs, o, n) -> filtrarLocal());

        cargarDatos();
    }

    private void configurarColumnas() {
        colFecha.setCellValueFactory(c     -> new SimpleStringProperty(c.getValue().getFecha()));
        colProveedor.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRegistradoPor()));
        colConcepto.setCellValueFactory(c  -> new SimpleStringProperty(
                c.getValue().getDescripcion() + "  [" + c.getValue().getCategoria() + "]"));
        colMonto.setCellValueFactory(c     -> new SimpleStringProperty("$" + c.getValue().getMonto()));
        colEstado.setCellValueFactory(c    -> new SimpleStringProperty(c.getValue().getMedioPago()));

        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                String color = "transferencia".equals(item)
                        ? "-fx-text-fill: #60a5fa;"
                        : "-fx-text-fill: #4ade80;";
                setStyle(color);
            }
        });
    }

    private List<Egreso> todosEgresos = List.of();

    private void cargarDatos() {
        setLoading(true);
        datos.clear();

        String medio = "Todos".equals(cmbEstado.getValue()) ? null : cmbEstado.getValue();

        CompletableFuture
            .supplyAsync(() -> CobranzasService.listarEgresos(null, null, null, medio, 1))
            .thenAcceptAsync(result -> {
                setLoading(false);
                if (!result.success) {
                    AlertHelper.error("Error al cargar egresos: " + result.errorMensaje);
                    return;
                }
                todosEgresos = result.data;
                lblTotal.setText("Total: " + result.total + " registros");
                filtrarLocal();
            }, Platform::runLater);
    }

    private void filtrarLocal() {
        String filtro = txtBusqueda != null ? txtBusqueda.getText().trim().toLowerCase() : "";
        datos.clear();
        if (filtro.isEmpty()) {
            datos.addAll(todosEgresos);
        } else {
            todosEgresos.stream()
                    .filter(e -> e.getDescripcion().toLowerCase().contains(filtro)
                              || e.getCategoria().toLowerCase().contains(filtro)
                              || e.getRegistradoPor().toLowerCase().contains(filtro))
                    .forEach(datos::add);
        }
    }

    @FXML
    private void onNuevo() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/NuevoEgresoModal.fxml"));
            javafx.scene.Parent root = loader.load();

            NuevoEgresoModalController ctrl = loader.getController();
            ctrl.setOnExito(this::cargarDatos);

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(AppConfig.getPrimaryStage());
            modal.setTitle("Registrar egreso");

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/css/dark-futuristic.css").toExternalForm());
            modal.setScene(scene);
            modal.setMinWidth(520);
            modal.setMinHeight(480);
            modal.centerOnScreen();
            modal.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.error("Error al abrir ventana de egreso: " + e.getMessage());
        }
    }

    @FXML
    private void onExportar() {
        AlertHelper.info("Exportación disponible próximamente.");
    }

    private void setLoading(boolean loading) {
        progressIndicator.setVisible(loading);
        tablaPagos.setDisable(loading);
    }
}
