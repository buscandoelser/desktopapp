package com.gestion.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.gestion.config.AppConfig;
import com.gestion.services.ConfigService;
import com.gestion.utils.AlertHelper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ConfiguracionController {

    // ── Parámetros ────────────────────────────────────────────
    @FXML private Label      lblCuotaActual;
    @FXML private TextField  txtCuotaMonto;

    @FXML private Label     lblInteresActual;
    @FXML private TextField txtTasa;

    @FXML private Label        lblVencimientoActual;
    @FXML private Spinner<Integer> spinnerDia;

    @FXML private Label            lblCamasActual;
    @FXML private Spinner<Integer> spinnerCamas;

    // ── Usuarios ──────────────────────────────────────────────
    @FXML private TableView<JsonNode>         tablaUsuarios;
    @FXML private TableColumn<JsonNode, String> colUsername;
    @FXML private TableColumn<JsonNode, String> colNombreCompleto;
    @FXML private TableColumn<JsonNode, String> colRol;
    @FXML private TableColumn<JsonNode, String> colEstado;
    @FXML private TableColumn<JsonNode, Void>   colAccionesUsr;

    // ── Auditoría ─────────────────────────────────────────────
    @FXML private TableView<JsonNode>         tablaAuditoria;
    @FXML private TableColumn<JsonNode, String> colAudFecha;
    @FXML private TableColumn<JsonNode, String> colAudUsuario;
    @FXML private TableColumn<JsonNode, String> colAudEntidad;
    @FXML private TableColumn<JsonNode, String> colAudAccion;
    @FXML private TableColumn<JsonNode, String> colAudDetalle;

    private final ObservableList<JsonNode> usuariosData   = FXCollections.observableArrayList();
    private final ObservableList<JsonNode> auditoriaData  = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        spinnerDia.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 28, 10));
        spinnerDia.setEditable(true);

        spinnerCamas.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 500, 40));
        spinnerCamas.setEditable(true);

        configurarTablaUsuarios();
        configurarTablaAuditoria();

        cargarParametros();
        cargarUsuarios();
        cargarAuditoria();
    }

    // ── Parámetros ────────────────────────────────────────────

    private void cargarParametros() {
        CompletableFuture.supplyAsync(ConfigService::getCuota)
            .thenAcceptAsync(r -> {
                if (!r.success) { lblCuotaActual.setText("—"); return; }
                String monto = r.data.path("monto").asText(r.data.path("valor").asText(""));
                lblCuotaActual.setText(monto.isEmpty() ? "—" : "$" + formatMonto(monto));
            }, Platform::runLater);

        CompletableFuture.supplyAsync(ConfigService::getMora)
            .thenAcceptAsync(r -> {
                if (!r.success) { lblInteresActual.setText("—"); return; }
                String monto = r.data.path("monto").asText(r.data.path("valor").asText("—"));
                lblInteresActual.setText("—".equals(monto) ? "—" : "$" + formatMonto(monto));
            }, Platform::runLater);

        CompletableFuture.supplyAsync(ConfigService::getVencimiento)
            .thenAcceptAsync(r -> {
                if (!r.success) { lblVencimientoActual.setText("—"); return; }
                String dia = r.data.path("dia").asText("—");
                lblVencimientoActual.setText(dia);
            }, Platform::runLater);

        CompletableFuture.supplyAsync(ConfigService::getCamas)
            .thenAcceptAsync(r -> {
                if (!r.success) { lblCamasActual.setText("—"); return; }
                int total = r.data.path("total").asInt(r.data.path("valor").asInt(40));
                lblCamasActual.setText(String.valueOf(total));
                spinnerCamas.getValueFactory().setValue(total);
            }, Platform::runLater);
    }

    @FXML
    private void onGuardarCamas() {
        Integer total = spinnerCamas.getValue();
        if (total == null || total < 1) { AlertHelper.error("Total de camas inválido."); return; }
        CompletableFuture.supplyAsync(() -> ConfigService.setCamas(total))
            .thenAcceptAsync(r -> {
                if (!r.success) { AlertHelper.error("Error: " + r.errorMensaje); return; }
                AlertHelper.info("Total de camas actualizado a: " + total);
                cargarParametros();
            }, Platform::runLater);
    }

    @FXML
    private void onGuardarCuota() {
        String monto = txtCuotaMonto.getText().trim();
        if (monto.isEmpty()) { AlertHelper.error("Ingresá el nuevo monto de la cuota."); return; }

        CompletableFuture.supplyAsync(() -> ConfigService.setCuota(monto))
            .thenAcceptAsync(r -> {
                if (!r.success) { AlertHelper.error("Error: " + r.errorMensaje); return; }
                AlertHelper.info("Cuota actualizada correctamente.");
                txtCuotaMonto.clear();
                cargarParametros();
            }, Platform::runLater);
    }

    @FXML
    private void onGuardarMora() {
        String monto = txtTasa.getText().trim();
        if (monto.isEmpty()) { AlertHelper.error("Ingresá el nuevo monto de mora."); return; }
        try {
            double v = Double.parseDouble(monto);
            if (v < 0) { AlertHelper.error("El monto de mora no puede ser negativo."); return; }
        } catch (NumberFormatException e) {
            AlertHelper.error("Monto inválido. Usá solo números, ej: 50000.00");
            return;
        }

        CompletableFuture.supplyAsync(() -> ConfigService.setMora(monto))
            .thenAcceptAsync(r -> {
                if (!r.success) { AlertHelper.error("Error: " + r.errorMensaje); return; }
                AlertHelper.info("Monto de mora actualizado correctamente.");
                txtTasa.clear();
                cargarParametros();
            }, Platform::runLater);
    }

    @FXML
    private void onGuardarVencimiento() {
        int dia = spinnerDia.getValue();
        CompletableFuture.supplyAsync(() -> ConfigService.setVencimiento(dia))
            .thenAcceptAsync(r -> {
                if (!r.success) { AlertHelper.error("Error: " + r.errorMensaje); return; }
                AlertHelper.info("Día de vencimiento actualizado a: " + dia);
                cargarParametros();
            }, Platform::runLater);
    }

    // ── Usuarios ──────────────────────────────────────────────

    private void configurarTablaUsuarios() {
        colUsername.setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().path("username").asText()));

        colNombreCompleto.setCellValueFactory(cd -> {
            JsonNode u = cd.getValue();
            String nombre = (u.path("nombre").asText("") + " " + u.path("apellido").asText("")).trim();
            return new SimpleStringProperty(nombre);
        });

        colRol.setCellValueFactory(cd ->
            new SimpleStringProperty(rolDisplay(cd.getValue().path("rol").asText())));

        colEstado.setCellValueFactory(cd -> {
            boolean activo = cd.getValue().path("activo").asBoolean(true);
            return new SimpleStringProperty(activo ? "Activo" : "Inactivo");
        });

        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label lbl = new Label(item);
                lbl.getStyleClass().add("Activo".equals(item) ? "estado-activo" : "estado-baja");
                setGraphic(lbl);
                setText(null);
            }
        });

        colAccionesUsr.setCellFactory(col -> new TableCell<>() {
            final Button btnEditar = new Button("Editar");
            final Button btnBaja   = new Button("Dar baja");
            final HBox   box       = new HBox(6, btnEditar, btnBaja);
            {
                btnEditar.getStyleClass().add("btn-sm-info");
                btnBaja.getStyleClass().add("btn-sm-danger");
                box.setAlignment(Pos.CENTER_LEFT);
                btnEditar.setOnAction(e -> mostrarDialogoUsuario(getTableView().getItems().get(getIndex())));
                btnBaja.setOnAction(e -> darBajaUsuario(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                JsonNode usuario = getTableView().getItems().get(getIndex());
                boolean activo = usuario.path("activo").asBoolean(true);
                btnBaja.setDisable(!activo);
                setGraphic(box);
            }
        });

        tablaUsuarios.setItems(usuariosData);
        tablaUsuarios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private void cargarUsuarios() {
        CompletableFuture.supplyAsync(ConfigService::getUsuarios)
            .thenAcceptAsync(r -> {
                usuariosData.clear();
                if (!r.success) { AlertHelper.error("Error al cargar usuarios: " + r.errorMensaje); return; }
                JsonNode datos = r.data;
                if (datos.isArray()) {
                    for (JsonNode u : datos) usuariosData.add(u);
                } else if (datos.has("datos") && datos.get("datos").isArray()) {
                    for (JsonNode u : datos.get("datos")) usuariosData.add(u);
                }
            }, Platform::runLater);
    }

    @FXML
    private void onNuevoUsuario() {
        mostrarDialogoUsuario(null);
    }

    private void mostrarDialogoUsuario(JsonNode usuarioExistente) {
        boolean esEdicion = usuarioExistente != null;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(esEdicion ? "Editar usuario" : "Nuevo usuario");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 24, 16, 24));
        grid.setMinWidth(380);

        TextField    txtUser    = new TextField();
        TextField    txtNombre  = new TextField();
        TextField    txtApellido = new TextField();
        PasswordField txtPass   = new PasswordField();
        ComboBox<String> cmbRol = new ComboBox<>(
            FXCollections.observableArrayList("admin", "operador", "contador", "readonly"));

        txtUser.setPromptText("usuario");
        txtNombre.setPromptText("Nombre");
        txtApellido.setPromptText("Apellido");
        txtPass.setPromptText(esEdicion ? "Dejar vacío para no cambiar" : "Contraseña");
        cmbRol.setPromptText("Rol");
        cmbRol.setPrefWidth(200);

        for (TextField tf : new TextField[]{txtUser, txtNombre, txtApellido}) {
            tf.getStyleClass().add("input-field-sm");
        }
        txtPass.getStyleClass().add("input-field-sm");

        if (esEdicion) {
            txtUser.setText(usuarioExistente.path("username").asText());
            txtUser.setDisable(true);
            txtNombre.setText(usuarioExistente.path("nombre").asText());
            txtApellido.setText(usuarioExistente.path("apellido").asText());
            cmbRol.setValue(usuarioExistente.path("rol").asText());
        }

        grid.addRow(0, lbl("Usuario"),   txtUser);
        grid.addRow(1, lbl("Nombre"),    txtNombre);
        grid.addRow(2, lbl("Apellido"),  txtApellido);
        grid.addRow(3, lbl("Rol"),       cmbRol);
        grid.addRow(4, lbl("Contraseña"), txtPass);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        String username  = txtUser.getText().trim();
        String nombre    = txtNombre.getText().trim();
        String apellido  = txtApellido.getText().trim();
        String pass      = txtPass.getText().trim();
        String rol       = cmbRol.getValue();

        if (!esEdicion && (username.isEmpty() || pass.isEmpty() || nombre.isEmpty() || rol == null)) {
            AlertHelper.error("Completá todos los campos obligatorios.");
            return;
        }
        if (esEdicion && (nombre.isEmpty() || rol == null)) {
            AlertHelper.error("Nombre y rol son obligatorios.");
            return;
        }

        Map<String, Object> datos = new HashMap<>();
        datos.put("nombre",   nombre);
        datos.put("apellido", apellido);
        datos.put("rol",      rol);

        if (esEdicion) {
            if (!pass.isEmpty()) datos.put("password", pass);
            int id = usuarioExistente.path("id").asInt();
            CompletableFuture.supplyAsync(() -> ConfigService.actualizarUsuario(id, datos))
                .thenAcceptAsync(r -> {
                    if (!r.success) { AlertHelper.error("Error: " + r.errorMensaje); return; }
                    AlertHelper.info("Usuario actualizado.");
                    cargarUsuarios();
                }, Platform::runLater);
        } else {
            datos.put("username", username);
            datos.put("password", pass);
            CompletableFuture.supplyAsync(() -> ConfigService.crearUsuario(datos))
                .thenAcceptAsync(r -> {
                    if (!r.success) { AlertHelper.error("Error: " + r.errorMensaje); return; }
                    AlertHelper.info("Usuario creado correctamente.");
                    cargarUsuarios();
                }, Platform::runLater);
        }
    }

    private void darBajaUsuario(JsonNode usuario) {
        int id = usuario.path("id").asInt();
        if (id == AppConfig.getUsuarioId()) {
            AlertHelper.error("No podés darte de baja a vos mismo.");
            return;
        }
        String username = usuario.path("username").asText("este usuario");
        if (!AlertHelper.confirmar("Dar de baja", "¿Dar de baja a " + username + "? El usuario no podrá iniciar sesión."))
            return;

        CompletableFuture.supplyAsync(() -> ConfigService.eliminarUsuario(id))
            .thenAcceptAsync(r -> {
                if (!r.success) { AlertHelper.error("Error: " + r.errorMensaje); return; }
                AlertHelper.info("Usuario dado de baja.");
                cargarUsuarios();
            }, Platform::runLater);
    }

    // ── Auditoría ─────────────────────────────────────────────

    private void configurarTablaAuditoria() {
        colAudFecha.setCellValueFactory(cd -> {
            JsonNode n = cd.getValue();
            String v = n.path("created_at").asText(n.path("fecha").asText("—"));
            if (v.length() > 19) v = v.substring(0, 19).replace("T", "  ");
            return new SimpleStringProperty(v);
        });

        colAudUsuario.setCellValueFactory(cd -> {
            JsonNode n = cd.getValue();
            String v = n.path("usuario_nombre").asText(n.path("username").asText(n.path("usuario").asText("—")));
            return new SimpleStringProperty(v);
        });

        colAudEntidad.setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().path("entidad").asText(
                cd.getValue().path("tabla").asText("—"))));

        colAudAccion.setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().path("accion").asText(
                cd.getValue().path("tipo").asText("—"))));

        colAudDetalle.setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().path("detalle").asText(
                cd.getValue().path("descripcion").asText("—"))));

        tablaAuditoria.setItems(auditoriaData);
        tablaAuditoria.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private void cargarAuditoria() {
        CompletableFuture.supplyAsync(ConfigService::getAuditoria)
            .thenAcceptAsync(r -> {
                auditoriaData.clear();
                if (!r.success) return;
                JsonNode datos = r.data;
                if (datos.isArray()) {
                    for (JsonNode row : datos) auditoriaData.add(row);
                } else if (datos.has("datos") && datos.get("datos").isArray()) {
                    for (JsonNode row : datos.get("datos")) auditoriaData.add(row);
                }
            }, Platform::runLater);
    }

    // ── Helpers ───────────────────────────────────────────────

    private Label lbl(String texto) {
        Label l = new Label(texto);
        l.getStyleClass().add("input-label");
        return l;
    }

    private String rolDisplay(String rol) {
        if (rol == null) return "";
        return switch (rol) {
            case "admin"    -> "Administrador";
            case "operador" -> "Operador";
            case "contador" -> "Contador";
            case "readonly" -> "Solo lectura";
            default         -> rol;
        };
    }

    private String formatMonto(String monto) {
        try {
            double v = Double.parseDouble(monto);
            return String.format("%,.2f", v);
        } catch (NumberFormatException e) {
            return monto;
        }
    }
}
