package com.gestion.controllers;

import com.gestion.config.AppConfig;
import com.gestion.services.AuthService;
import com.gestion.ui.WindowControls;
import com.gestion.utils.AlertHelper;
import com.gestion.utils.ThemeManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.concurrent.CompletableFuture;

public class LoginController {

    @FXML private TextField      txtUsuario;
    @FXML private PasswordField  txtPassword;
    @FXML private TextField      txtPasswordVisible;
    @FXML private CheckBox       chkMostrarPassword;
    @FXML private Button         btnLogin;
    @FXML private Label          lblError;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private HBox           loginTitleBar;
    @FXML private HBox           loginWindowControlsHost;

    @FXML
    public void initialize() {
        lblError.setVisible(false);
        progressIndicator.setVisible(false);

        setupWindowChrome();

        // El campo visible comienza oculto
        txtPasswordVisible.setVisible(false);
        txtPasswordVisible.setManaged(false);

        // Enter en cualquier campo dispara el login
        txtUsuario.setOnAction(e -> onLogin());
        txtPassword.setOnAction(e -> onLogin());
        txtPasswordVisible.setOnAction(e -> onLogin());
    }

    private void setupWindowChrome() {
        Stage stage = AppConfig.getPrimaryStage();
        if (stage == null) return;
        // Login is non-resizable → no maximize button
        loginWindowControlsHost.getChildren().setAll(
                WindowControls.createControls(stage, false)
        );
        WindowControls.attachDragHandler(loginTitleBar, stage);
    }

    @FXML
    private void onEyeClick() {
        chkMostrarPassword.setSelected(!chkMostrarPassword.isSelected());
        onTogglePassword();
    }

    @FXML
    private void onTogglePassword() {
        if (chkMostrarPassword.isSelected()) {
            txtPasswordVisible.setText(txtPassword.getText());
            txtPassword.setVisible(false);
            txtPassword.setManaged(false);
            txtPasswordVisible.setVisible(true);
            txtPasswordVisible.setManaged(true);
            txtPasswordVisible.requestFocus();
            txtPasswordVisible.positionCaret(txtPasswordVisible.getLength());
        } else {
            txtPassword.setText(txtPasswordVisible.getText());
            txtPasswordVisible.setVisible(false);
            txtPasswordVisible.setManaged(false);
            txtPassword.setVisible(true);
            txtPassword.setManaged(true);
            txtPassword.requestFocus();
            txtPassword.positionCaret(txtPassword.getLength());
        }
    }

    @FXML
    private void onLogin() {
        String usuario  = txtUsuario.getText().trim();
        String password = chkMostrarPassword.isSelected()
                ? txtPasswordVisible.getText()
                : txtPassword.getText();

        if (usuario.isEmpty() || password.isEmpty()) {
            mostrarError("Ingresá usuario y contraseña");
            return;
        }

        setLoading(true);
        ocultarError();

        CompletableFuture
            .supplyAsync(() -> AuthService.login(usuario, password))
            .thenAcceptAsync(result -> {
                setLoading(false);
                if (result.success) {
                    AppConfig.setJwtToken(result.token);
                    AppConfig.setRefreshToken(result.refreshToken);
                    AppConfig.setUsuarioNombre(result.nombre + " " + result.apellido);
                    AppConfig.setUsuarioRol(result.rol);
                    AppConfig.setUsuarioId(result.id);
                    navegarAlMain();
                } else {
                    mostrarError(result.errorMensaje);
                }
            }, Platform::runLater);
    }

    private void navegarAlMain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            ThemeManager.apply(scene);

            Stage stage = AppConfig.getPrimaryStage();
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.setMinWidth(1200);
            stage.setMinHeight(700);
        } catch (Exception e) {
            AlertHelper.error("Error al cargar la pantalla principal: " + e.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
    }

    private void ocultarError() {
        lblError.setVisible(false);
    }

    private void setLoading(boolean loading) {
        btnLogin.setDisable(loading);
        progressIndicator.setVisible(loading);
    }
}
