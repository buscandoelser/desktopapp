package com.gestion.controllers;

import com.gestion.models.Interno;
import com.gestion.services.InternoService;
import com.gestion.utils.AlertHelper;
import com.gestion.utils.FormatHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

public class BajaModalController {

    @FXML private Label             lblNombre;
    @FXML private ComboBox<String>  cmbTipo;
    @FXML private TextArea          txtMotivo;
    @FXML private Button            btnConfirmar;
    @FXML private ProgressIndicator progressIndicator;

    private Interno  interno;
    private Runnable onConfirmado;

    public void setDatos(Interno interno, Runnable onConfirmado) {
        this.interno      = interno;
        this.onConfirmado = onConfirmado;
        lblNombre.setText(interno.getNombreCompleto().trim() + "  ·  " + interno.getLegajo());
    }

    @FXML
    public void initialize() {
        cmbTipo.setItems(FXCollections.observableArrayList(
                "Alta médica", "Abandono", "Fallecido", "Derivado"
        ));
        cmbTipo.setValue("Alta médica");

        btnConfirmar.setDisable(true);
        txtMotivo.textProperty().addListener((obs, o, n) ->
                btnConfirmar.setDisable(n.trim().isEmpty()));
    }

    @FXML
    private void onConfirmar() {
        String estado = switch (cmbTipo.getValue()) {
            case "Alta médica" -> "alta";
            case "Abandono"    -> "abandono";
            case "Fallecido"   -> "fallecido";
            default            -> "derivado";
        };
        String motivo      = txtMotivo.getText().trim();
        String fechaEgreso = FormatHelper.localDateAIso(LocalDate.now());

        setLoading(true);

        CompletableFuture
            .supplyAsync(() -> InternoService.cambiarEstado(interno.getId(), estado, motivo, fechaEgreso))
            .thenAcceptAsync(result -> {
                setLoading(false);
                if (result.success) {
                    AlertHelper.exito("Baja registrada correctamente");
                    if (onConfirmado != null) onConfirmado.run();
                    cerrar();
                } else {
                    AlertHelper.error("Error al registrar la baja: " + result.errorMensaje);
                }
            }, Platform::runLater);
    }

    @FXML
    private void onCancelar() {
        cerrar();
    }

    private void cerrar() {
        ((Stage) btnConfirmar.getScene().getWindow()).close();
    }

    private void setLoading(boolean cargando) {
        progressIndicator.setVisible(cargando);
        btnConfirmar.setDisable(cargando);
        txtMotivo.setDisable(cargando);
        cmbTipo.setDisable(cargando);
    }
}
