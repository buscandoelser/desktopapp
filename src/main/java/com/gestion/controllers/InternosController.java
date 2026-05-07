package com.gestion.controllers;

import com.gestion.config.AppConfig;
import com.gestion.models.Interno;
import com.gestion.services.InternoService;
import com.gestion.utils.AlertHelper;
import com.gestion.utils.FormatHelper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class InternosController {

    @FXML private TextField  txtBusqueda;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private Button     btnNuevo;
    @FXML private Label      lblTotal;
    @FXML private ProgressIndicator progressIndicator;

    @FXML private TableView<Interno>          tablaInternos;
    @FXML private TableColumn<Interno, String> colLegajo;
    @FXML private TableColumn<Interno, String> colNombre;
    @FXML private TableColumn<Interno, String> colDni;
    @FXML private TableColumn<Interno, String> colEstado;
    @FXML private TableColumn<Interno, String> colIngreso;
    @FXML private TableColumn<Interno, String> colTipoPago;
    @FXML private TableColumn<Interno, String> colJudicializado;
    @FXML private TableColumn<Interno, Void>   colAcciones;

    private final ObservableList<Interno> datos = FXCollections.observableArrayList();
    private int paginaActual = 1;

    @FXML
    public void initialize() {
        configurarTabla();
        configurarFiltros();

        // No mostrar botón "Nuevo" si no tiene permisos de escritura
        if (!AppConfig.tieneRol("admin", "operador")) {
            btnNuevo.setVisible(false);
            btnNuevo.setManaged(false);
        }

        cargarDatos();
    }

    // ── Configuración de la tabla ─────────────────────────────
    private void configurarTabla() {
        colLegajo.setCellValueFactory(new PropertyValueFactory<>("legajo"));
        colDni.setCellValueFactory(data ->
                new SimpleStringProperty(FormatHelper.formatearDni(data.getValue().getDni())));
        colNombre.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNombreCompleto()));
        colEstado.setCellValueFactory(data ->
                new SimpleStringProperty(FormatHelper.estadoDisplay(data.getValue().getEstado())));
        colIngreso.setCellValueFactory(data ->
                new SimpleStringProperty(FormatHelper.isoADisplay(data.getValue().getFechaIngreso())));
        colTipoPago.setCellValueFactory(data ->
                new SimpleStringProperty(FormatHelper.tipoPagoDisplay(data.getValue().getTipoPagoClasificacion())));
        colJudicializado.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().isEsJudicializado() ? "Sí" : "No"));

        // Colorear filas según estado
        tablaInternos.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Interno item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("row-activo", "row-baja", "row-mora");
                if (!empty && item != null) {
                    if ("activo".equals(item.getEstado())) getStyleClass().add("row-activo");
                    else                                   getStyleClass().add("row-baja");
                }
            }
        });

        // Columna de acciones
        colAcciones.setCellFactory(tc -> new TableCell<>() {
            private final Button btnVer    = new Button("Ver");
            private final Button btnEditar = new Button("Editar");
            private final Button btnBaja   = new Button("Baja");
            {
                btnVer.getStyleClass().add("btn-sm-info");
                btnEditar.getStyleClass().add("btn-sm-warning");
                btnBaja.getStyleClass().add("btn-sm-danger");

                btnVer.setOnAction(e -> {
                    Interno i = getTableView().getItems().get(getIndex());
                    abrirDetalle(i);
                });
                btnEditar.setOnAction(e -> {
                    Interno i = getTableView().getItems().get(getIndex());
                    abrirFormulario(i);
                });
                btnBaja.setOnAction(e -> {
                    Interno i = getTableView().getItems().get(getIndex());
                    darDeBaja(i);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Interno interno = getTableView().getItems().get(getIndex());
                    boolean activo  = "activo".equals(interno.getEstado());
                    boolean puedeEditar = AppConfig.tieneRol("admin", "operador");

                    btnEditar.setVisible(activo && puedeEditar);
                    btnEditar.setManaged(activo && puedeEditar);
                    btnBaja.setVisible(activo && puedeEditar);
                    btnBaja.setManaged(activo && puedeEditar);

                    HBox box = new HBox(4, btnVer);
                    if (puedeEditar) {
                        box.getChildren().addAll(btnEditar, btnBaja);
                    }
                    setGraphic(box);
                }
            }
        });

        tablaInternos.setItems(datos);

        // Doble click → detalle
        tablaInternos.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && tablaInternos.getSelectionModel().getSelectedItem() != null) {
                abrirDetalle(tablaInternos.getSelectionModel().getSelectedItem());
            }
        });
    }

    private void configurarFiltros() {
        cmbEstado.setItems(FXCollections.observableArrayList(
                "Todos", "activo", "alta", "abandono", "fallecido", "derivado"
        ));
        cmbEstado.setValue("Todos");
        cmbEstado.setOnAction(e -> { paginaActual = 1; cargarDatos(); });

        // Buscar al detener escritura (300ms de debounce simple)
        txtBusqueda.textProperty().addListener((obs, old, val) -> {
            paginaActual = 1;
            cargarDatos();
        });
    }

    // ── Carga de datos ────────────────────────────────────────
    private void cargarDatos() {
        setLoading(true);
        String estado   = "Todos".equals(cmbEstado.getValue()) ? null : cmbEstado.getValue();
        String busqueda = txtBusqueda.getText().trim();

        CompletableFuture
            .supplyAsync(() -> InternoService.listar(estado, busqueda, paginaActual))
            .thenAcceptAsync(result -> {
                setLoading(false);
                if (result.success) {
                    datos.setAll((List<Interno>) result.data);
                    lblTotal.setText("Total: " + result.total + " registros");
                } else {
                    AlertHelper.error("Error al cargar internos: " + result.errorMensaje);
                }
            }, Platform::runLater);
    }

    // ── Acciones ──────────────────────────────────────────────
    @FXML
    private void onNuevoInterno() {
        abrirFormulario(null);
    }

    @FXML
    private void onBuscar() {
        paginaActual = 1;
        cargarDatos();
    }

    private void abrirFormulario(Interno interno) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/InternoForm.fxml"));
            javafx.scene.Parent root = loader.load();

            InternoFormController ctrl = loader.getController();
            ctrl.setInterno(interno);
            ctrl.setOnGuardado(this::cargarDatos);

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(AppConfig.getPrimaryStage());
            modal.setTitle(interno == null ? "Nuevo Interno" : "Editar Interno");

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/css/dark-futuristic.css").toExternalForm()
            );
            modal.setScene(scene);
            modal.setMinWidth(800);
            modal.setMinHeight(640);
            modal.centerOnScreen();
            modal.showAndWait();

        } catch (Exception e) {
            AlertHelper.error("Error al abrir formulario: " + e.getMessage());
        }
    }

    private void abrirDetalle(Interno interno) {
        // Cargar detalle completo y abrir formulario en modo lectura
        CompletableFuture
            .supplyAsync(() -> InternoService.obtener(interno.getId()))
            .thenAcceptAsync(result -> {
                if (result.success) {
                    abrirFormulario(result.data);
                } else {
                    AlertHelper.error("Error al cargar detalle: " + result.errorMensaje);
                }
            }, Platform::runLater);
    }

    private void darDeBaja(Interno interno) {
        // Diálogo para seleccionar motivo y estado de baja
        Dialog<String[]> dialog = crearDialogoBaja(interno.getNombreCompleto());
        Optional<String[]> resultado = dialog.showAndWait();

        resultado.ifPresent(datos -> {
            String estado   = datos[0];
            String motivo   = datos[1];
            String fechaEgreso = FormatHelper.localDateAIso(java.time.LocalDate.now());

            CompletableFuture
                .supplyAsync(() -> InternoService.cambiarEstado(interno.getId(), estado, motivo, fechaEgreso))
                .thenAcceptAsync(result -> {
                    if (result.success) {
                        AlertHelper.exito("Estado actualizado correctamente");
                        cargarDatos();
                    } else {
                        AlertHelper.error("Error: " + result.errorMensaje);
                    }
                }, Platform::runLater);
        });
    }

    private Dialog<String[]> crearDialogoBaja(String nombreCompleto) {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Dar de baja: " + nombreCompleto);
        dialog.setHeaderText("Seleccioná el motivo de baja");

        ButtonType btnConfirmar = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnConfirmar, ButtonType.CANCEL);

        ComboBox<String> cmbTipo = new ComboBox<>(FXCollections.observableArrayList(
                "alta", "abandono", "fallecido", "derivado"
        ));
        cmbTipo.setValue("alta");

        TextArea txtMotivo = new TextArea();
        txtMotivo.setPromptText("Descripción del motivo (obligatorio)");
        txtMotivo.setPrefRowCount(3);
        txtMotivo.setWrapText(true);

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10,
                new Label("Tipo de egreso:"), cmbTipo,
                new Label("Motivo:"), txtMotivo
        );
        content.setPadding(new javafx.geometry.Insets(20));
        dialog.getDialogPane().setContent(content);

        // Deshabilitar confirmar si no hay motivo
        javafx.scene.Node btnConfirmarNode = dialog.getDialogPane().lookupButton(btnConfirmar);
        btnConfirmarNode.setDisable(true);
        txtMotivo.textProperty().addListener((obs, o, n) ->
                btnConfirmarNode.setDisable(n.trim().isEmpty())
        );

        dialog.setResultConverter(btn -> {
            if (btn == btnConfirmar) {
                return new String[]{ cmbTipo.getValue(), txtMotivo.getText().trim() };
            }
            return null;
        });

        return dialog;
    }

    private void setLoading(boolean loading) {
        progressIndicator.setVisible(loading);
        tablaInternos.setDisable(loading);
    }
}
