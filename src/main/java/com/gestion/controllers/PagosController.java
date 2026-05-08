package com.gestion.controllers;

import com.gestion.config.AppConfig;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class PagosController {

    @FXML private ProgressIndicator progressIndicator;
    @FXML private Button             btnNuevo;
    @FXML private TextField          txtBusqueda;
    @FXML private ComboBox<String>   cmbEstado;
    @FXML private Label              lblTotal;

    @FXML private TableView<Object>           tablaPagos;
    @FXML private TableColumn<Object, String> colFecha;
    @FXML private TableColumn<Object, String> colProveedor;
    @FXML private TableColumn<Object, String> colConcepto;
    @FXML private TableColumn<Object, String> colMonto;
    @FXML private TableColumn<Object, String> colEstado;
    @FXML private TableColumn<Object, Void>   colAcciones;

    @FXML
    public void initialize() {
        boolean puedeEscribir = AppConfig.tieneRol("admin", "operador");
        btnNuevo.setVisible(puedeEscribir);
        btnNuevo.setManaged(puedeEscribir);

        cmbEstado.setItems(FXCollections.observableArrayList("Todos", "pagado", "pendiente"));
        cmbEstado.setValue("Todos");

        lblTotal.setText("Total: — registros");
        progressIndicator.setVisible(false);
    }

    @FXML private void onNuevo() {
        com.gestion.utils.AlertHelper.info("Registro de pagos disponible próximamente.");
    }

    @FXML private void onExportar() {
        com.gestion.utils.AlertHelper.info("Exportación disponible próximamente.");
    }
}
