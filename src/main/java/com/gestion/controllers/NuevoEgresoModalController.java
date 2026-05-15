package com.gestion.controllers;

import com.gestion.services.CobranzasService;
import com.gestion.utils.AlertHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class NuevoEgresoModalController {

    @FXML private TextField        txtDescripcion;
    @FXML private TextField        txtMonto;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private ComboBox<String> cmbMedio;
    @FXML private DatePicker       dpFecha;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Button           btnConfirmar;

    private Runnable onExito;

    public void setOnExito(Runnable cb) { this.onExito = cb; }

    @FXML
    public void initialize() {
        cmbCategoria.setItems(FXCollections.observableArrayList(
                "insumos", "servicios", "mantenimiento", "alimentacion", "medicamentos", "otros"));
        cmbCategoria.setValue("insumos");

        cmbMedio.setItems(FXCollections.observableArrayList("efectivo", "transferencia"));
        cmbMedio.setValue("efectivo");

        dpFecha.setValue(LocalDate.now());

        txtDescripcion.textProperty().addListener((obs, o, n) -> actualizarBoton());
        txtMonto.textProperty().addListener((obs, o, n) -> actualizarBoton());
        actualizarBoton();
    }

    private void actualizarBoton() {
        boolean valido = !txtDescripcion.getText().trim().isEmpty()
                && parseMonto() > 0
                && cmbCategoria.getValue() != null
                && cmbMedio.getValue() != null
                && dpFecha.getValue() != null;
        btnConfirmar.setDisable(!valido);
    }

    @FXML
    private void onConfirmar() {
        double monto = parseMonto();
        if (monto <= 0) { AlertHelper.error("El monto debe ser mayor a cero."); return; }
        if (txtDescripcion.getText().trim().isEmpty()) {
            AlertHelper.error("La descripción es obligatoria.");
            return;
        }

        Map<String, Object> datos = new HashMap<>();
        datos.put("concepto", txtDescripcion.getText().trim());
        datos.put("monto",       String.valueOf(monto));
        datos.put("categoria",   cmbCategoria.getValue());
        datos.put("medio_pago",  cmbMedio.getValue());
        datos.put("fecha",       dpFecha.getValue().toString());

        setLoading(true);
        CompletableFuture
            .supplyAsync(() -> CobranzasService.registrarEgreso(datos))
            .thenAcceptAsync(result -> {
                setLoading(false);
                if (result.success) {
                    AlertHelper.exito("Egreso registrado correctamente.");
                    if (onExito != null) onExito.run();
                    cerrar();
                } else {
                    AlertHelper.error("Error al registrar egreso: " + result.errorMensaje);
                }
            }, Platform::runLater);
    }

    @FXML
    private void onCancelar() { cerrar(); }

    private void cerrar() {
        ((Stage) btnConfirmar.getScene().getWindow()).close();
    }

    private void setLoading(boolean cargando) {
        progressIndicator.setVisible(cargando);
        btnConfirmar.setDisable(cargando);
        txtDescripcion.setDisable(cargando);
        txtMonto.setDisable(cargando);
        cmbCategoria.setDisable(cargando);
        cmbMedio.setDisable(cargando);
        dpFecha.setDisable(cargando);
    }

    private double parseMonto() {
        try {
            return Double.parseDouble(txtMonto.getText().trim().replace(",", "."));
        } catch (Exception e) {
            return 0;
        }
    }
}
