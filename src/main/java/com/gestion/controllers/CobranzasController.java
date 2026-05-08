package com.gestion.controllers;

import com.gestion.config.AppConfig;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

public class CobranzasController {

    @FXML private ProgressIndicator progressIndicator;
    @FXML private Button             btnRegistrar;
    @FXML private TextField          txtBusqueda;
    @FXML private ComboBox<String>   cmbMes;
    @FXML private ComboBox<String>   cmbEstado;
    @FXML private Label              lblTotal;

    // Stat labels
    @FXML private Label lblCobrado;
    @FXML private Label lblCobradoDelta;
    @FXML private Label lblPendiente;
    @FXML private Label lblPendienteDelta;
    @FXML private Label lblVencido;
    @FXML private Label lblVencidoDelta;
    @FXML private Label lblTasa;
    @FXML private Label lblTasaDelta;

    @FXML private TableView<Object>            tablaCobranzas;
    @FXML private TableColumn<Object, String>  colFecha;
    @FXML private TableColumn<Object, String>  colInterno;
    @FXML private TableColumn<Object, String>  colConcepto;
    @FXML private TableColumn<Object, String>  colMonto;
    @FXML private TableColumn<Object, String>  colMetodo;
    @FXML private TableColumn<Object, String>  colEstado;
    @FXML private TableColumn<Object, Void>    colAcciones;

    @FXML
    public void initialize() {
        // Permisos
        boolean puedeEscribir = AppConfig.tieneRol("admin", "operador", "contador");
        btnRegistrar.setVisible(puedeEscribir);
        btnRegistrar.setManaged(puedeEscribir);

        // Filtros
        cmbMes.setItems(FXCollections.observableArrayList(
            "Todos", "Enero", "Febrero", "Marzo", "Abril", "Mayo",
            "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        ));
        cmbMes.setValue("Mayo");

        cmbEstado.setItems(FXCollections.observableArrayList("Todos", "cobrado", "pendiente", "vencido"));
        cmbEstado.setValue("Todos");

        // Stats demo — reemplazar con CobranzasService
        lblCobrado.setText("$8.412.000");
        lblCobradoDelta.setText("+12% vs mes anterior");
        lblPendiente.setText("$1.187.000");
        lblPendienteDelta.setText("3 cuotas pendientes");
        lblVencido.setText("$285.000");
        lblVencidoDelta.setText("1 caso");
        lblTasa.setText("87,4%");

        lblTotal.setText("Total: — movimientos");
        progressIndicator.setVisible(false);
    }

    @FXML private void onRegistrar() {
        com.gestion.utils.AlertHelper.info("Registro de cobros disponible próximamente.");
    }

    @FXML private void onExportar() {
        com.gestion.utils.AlertHelper.info("Exportación CSV disponible próximamente.");
    }
}
