package com.gestion.controllers;

import com.gestion.config.AppConfig;
import com.gestion.models.ContactoFamiliar;
import com.gestion.models.Interno;
import com.gestion.services.InternoService;
import com.gestion.utils.AlertHelper;
import com.gestion.utils.FormatHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class InternoFormController {

    // ── Paso 1: Datos personales ──────────────────────────────
    @FXML private TextField    txtNombre;
    @FXML private TextField    txtApellido;
    @FXML private TextField    txtDni;
    @FXML private DatePicker   dpFechaNacimiento;
    @FXML private Label        lblEdad;
    @FXML private TextField    txtDireccion;
    @FXML private CheckBox     chkHijos;
    @FXML private Spinner<Integer> spnCantHijos;
    @FXML private CheckBox     chkJudicializado;
    @FXML private DatePicker   dpFechaIngreso;

    // ── Paso 2: Salud y contexto ──────────────────────────────
    @FXML private CheckBox  chkInternadoAntes;
    @FXML private TextField txtLugarAnterior;
    @FXML private CheckBox  chkMedicacion;
    @FXML private TextArea  txtDetalleMedicacion;
    @FXML private CheckBox  chkPatologia;
    @FXML private TextArea  txtDetallePatologia;
    @FXML private ComboBox<String> cmbNivelEstudios;

    // ── Paso 3: Cobertura y contactos ─────────────────────────
    @FXML private CheckBox  chkObraSocial;
    @FXML private TextField txtObraSocial;
    @FXML private CheckBox  chkPension;
    @FXML private TextField txtTipoPension;
    @FXML private ComboBox<String> cmbTipoPago;
    @FXML private VBox      vboxContactos;
    @FXML private Button    btnAgregarContacto;

    // ── Navegación del wizard ─────────────────────────────────
    @FXML private Button    btnAnterior;
    @FXML private Button    btnSiguiente;
    @FXML private Button    btnGuardar;
    @FXML private HBox      step1Indicator;
    @FXML private HBox      step2Indicator;
    @FXML private HBox      step3Indicator;
    @FXML private StackPane panelPaso1;
    @FXML private StackPane panelPaso2;
    @FXML private StackPane panelPaso3;
    @FXML private Label     lblTituloPaso;
    @FXML private ProgressIndicator progressGuardar;

    private int    pasoActual = 1;
    private Interno internoEditando;
    private Runnable onGuardado;
    private boolean modoEdicion   = false;
    private boolean modoSoloLectura = false;
    private final List<HBox> filasContacto = new ArrayList<>();

    @FXML
    public void initialize() {
        // Nivel de estudios
        cmbNivelEstudios.setItems(FXCollections.observableArrayList(
                "ninguno", "primario", "secundario", "terciario", "universitario"
        ));

        // Tipo de pago
        cmbTipoPago.setItems(FXCollections.observableArrayList("particular", "obra_social"));
        cmbTipoPago.setValue("particular");

        // Spinner hijos
        spnCantHijos.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20, 0));
        spnCantHijos.setDisable(true);
        chkHijos.selectedProperty().addListener((obs, o, n) -> spnCantHijos.setDisable(!n));

        // Campos condicionales
        txtLugarAnterior.setDisable(true);
        chkInternadoAntes.selectedProperty().addListener((obs, o, n) -> txtLugarAnterior.setDisable(!n));

        txtDetalleMedicacion.setDisable(true);
        chkMedicacion.selectedProperty().addListener((obs, o, n) -> txtDetalleMedicacion.setDisable(!n));

        txtDetallePatologia.setDisable(true);
        chkPatologia.selectedProperty().addListener((obs, o, n) -> txtDetallePatologia.setDisable(!n));

        txtObraSocial.setDisable(true);
        chkObraSocial.selectedProperty().addListener((obs, o, n) -> txtObraSocial.setDisable(!n));

        txtTipoPension.setDisable(true);
        chkPension.selectedProperty().addListener((obs, o, n) -> txtTipoPension.setDisable(!n));

        // Calcular edad al elegir fecha de nacimiento
        dpFechaNacimiento.valueProperty().addListener((obs, o, fecha) -> {
            if (fecha != null) {
                int edad = FormatHelper.calcularEdad(FormatHelper.localDateAIso(fecha));
                lblEdad.setText(edad + " años");
            }
        });

        // Dos contactos vacíos por defecto
        agregarFilaContacto(true);
        agregarFilaContacto(false);

        progressGuardar.setVisible(false);
        irAPaso(1);
    }

    // ── API pública ───────────────────────────────────────────
    public void setInterno(Interno interno, boolean soloLectura) {
        this.internoEditando   = interno;
        this.modoSoloLectura   = soloLectura;
        if (interno != null) {
            modoEdicion = true;
            rellenarFormulario(interno);
        }
    }

    public void setOnGuardado(Runnable callback) {
        this.onGuardado = callback;
    }

    // ── Rellenar en modo edición ──────────────────────────────
    private void rellenarFormulario(Interno i) {
        txtNombre.setText(i.getNombre());
        txtApellido.setText(i.getApellido());
        txtDni.setText(i.getDni());

        if (i.getFechaNacimiento() != null && !i.getFechaNacimiento().isBlank()) {
            try {
                String clean = i.getFechaNacimiento().length() > 10
                        ? i.getFechaNacimiento().substring(0, 10)
                        : i.getFechaNacimiento();
                dpFechaNacimiento.setValue(LocalDate.parse(clean));
            } catch (Exception ignored) {}
        }
        if (i.getFechaIngreso() != null && !i.getFechaIngreso().isBlank()) {
            try {
                String clean = i.getFechaIngreso().length() > 10
                        ? i.getFechaIngreso().substring(0, 10)
                        : i.getFechaIngreso();
                dpFechaIngreso.setValue(LocalDate.parse(clean));
            } catch (Exception ignored) {}
        }

        txtDireccion.setText(safe(i.getDireccion()));
        chkHijos.setSelected(i.isTieneHijos());
        spnCantHijos.getValueFactory().setValue(i.getCantidadHijos());
        chkJudicializado.setSelected(i.isEsJudicializado());

        chkInternadoAntes.setSelected(i.isEstuvoInternadoAntes());
        txtLugarAnterior.setText(safe(i.getLugarInternacionAnterior()));
        chkMedicacion.setSelected(i.isTomaMedicacion());
        txtDetalleMedicacion.setText(safe(i.getDetalleMedicacion()));
        chkPatologia.setSelected(i.isTienePatologia());
        txtDetallePatologia.setText(safe(i.getDetallePatologia()));
        if (i.getNivelEstudios() != null) cmbNivelEstudios.setValue(i.getNivelEstudios());

        chkObraSocial.setSelected(i.isTieneObraSocial());
        txtObraSocial.setText(safe(i.getNombreObraSocial()));
        chkPension.setSelected(i.isCobraPension());
        txtTipoPension.setText(safe(i.getTipoPension()));
        if (i.getTipoPagoClasificacion() != null) cmbTipoPago.setValue(i.getTipoPagoClasificacion());

        // Cargar contactos
        if (i.getContactos() != null && !i.getContactos().isEmpty()) {
            vboxContactos.getChildren().removeAll(filasContacto);
            filasContacto.clear();
            for (ContactoFamiliar c : i.getContactos()) {
                agregarFilaContacto(c.isEsReferente());
                HBox fila = filasContacto.get(filasContacto.size() - 1);
                ((TextField) fila.lookup("#tfNombreC")).setText(c.getNombre() != null ? c.getNombre() : "");
                ((TextField) fila.lookup("#tfTelC")).setText(c.getTelefono() != null ? c.getTelefono() : "");
                TextField tfVinculo = (TextField) fila.lookup("#tfVinculo");
                if (tfVinculo != null) tfVinculo.setText(c.getVinculo() != null ? c.getVinculo() : "");
            }
        }

        // Bloquear si es vista de detalle o si el rol no permite editar
        setFormReadOnly(modoSoloLectura || !AppConfig.tieneRol("admin", "operador"));
        // fecha_ingreso no se puede modificar una vez creado el legajo
        if (modoEdicion) dpFechaIngreso.setDisable(true);
    }

    private void setFormReadOnly(boolean readOnly) {
        txtNombre.setEditable(!readOnly);
        txtApellido.setEditable(!readOnly);
        txtDni.setEditable(!readOnly);
        dpFechaNacimiento.setDisable(readOnly);
        dpFechaIngreso.setDisable(readOnly);
        chkHijos.setDisable(readOnly);
        spnCantHijos.setDisable(readOnly || !chkHijos.isSelected());
        chkJudicializado.setDisable(readOnly);
        chkInternadoAntes.setDisable(readOnly);
        txtLugarAnterior.setDisable(readOnly || !chkInternadoAntes.isSelected());
        chkMedicacion.setDisable(readOnly);
        txtDetalleMedicacion.setDisable(readOnly || !chkMedicacion.isSelected());
        chkPatologia.setDisable(readOnly);
        txtDetallePatologia.setDisable(readOnly || !chkPatologia.isSelected());
        cmbNivelEstudios.setDisable(readOnly);
        chkObraSocial.setDisable(readOnly);
        txtObraSocial.setDisable(readOnly || !chkObraSocial.isSelected());
        chkPension.setDisable(readOnly);
        txtTipoPension.setDisable(readOnly || !chkPension.isSelected());
        cmbTipoPago.setDisable(readOnly);
        txtDireccion.setEditable(!readOnly);
        btnAgregarContacto.setVisible(!readOnly);
        btnAgregarContacto.setManaged(!readOnly);
        // El botón Guardar lo controla irAPaso; no lo tocamos aquí
    }

    // ── Wizard: navegación ────────────────────────────────────
    @FXML private void onAnterior()  { irAPaso(pasoActual - 1); }
    @FXML private void onSiguiente() {
        if (!validarPasoActual()) return;
        irAPaso(pasoActual + 1);
    }

    private void irAPaso(int paso) {
        pasoActual = paso;

        panelPaso1.setVisible(paso == 1); panelPaso1.setManaged(paso == 1);
        panelPaso2.setVisible(paso == 2); panelPaso2.setManaged(paso == 2);
        panelPaso3.setVisible(paso == 3); panelPaso3.setManaged(paso == 3);

        btnAnterior.setDisable(paso == 1);
        btnSiguiente.setVisible(paso < 3);
        btnSiguiente.setManaged(paso < 3);
        boolean mostrarGuardar = (paso == 3) && !modoSoloLectura;
        btnGuardar.setVisible(mostrarGuardar);
        btnGuardar.setManaged(mostrarGuardar);

        lblTituloPaso.setText(switch (paso) {
            case 1 -> "Paso 1 de 3 — Datos Personales";
            case 2 -> "Paso 2 de 3 — Salud y Contexto";
            case 3 -> "Paso 3 de 3 — Cobertura y Contactos";
            default -> "";
        });

        actualizarIndicadoresPaso();
    }

    private void actualizarIndicadoresPaso() {
        setIndicadorActivo(step1Indicator, pasoActual == 1);
        setIndicadorActivo(step2Indicator, pasoActual == 2);
        setIndicadorActivo(step3Indicator, pasoActual == 3);
    }

    private void setIndicadorActivo(HBox indicator, boolean activo) {
        indicator.getStyleClass().removeAll("step-active", "step-done", "step-pending");
        indicator.getStyleClass().add(activo ? "step-active" : "step-pending");
    }

    // ── Validaciones ──────────────────────────────────────────
    private boolean validarPasoActual() {
        return switch (pasoActual) {
            case 1 -> validarPaso1();
            case 2 -> true;
            case 3 -> validarPaso3();
            default -> true;
        };
    }

    private boolean validarPaso1() {
        if (txtNombre.getText().trim().isEmpty()) {
            AlertHelper.advertencia("El nombre es obligatorio");
            txtNombre.requestFocus();
            return false;
        }
        if (txtApellido.getText().trim().isEmpty()) {
            AlertHelper.advertencia("El apellido es obligatorio");
            txtApellido.requestFocus();
            return false;
        }
        if (txtDni.getText().trim().isEmpty()) {
            AlertHelper.advertencia("El DNI es obligatorio");
            txtDni.requestFocus();
            return false;
        }
        if (dpFechaNacimiento.getValue() == null) {
            AlertHelper.advertencia("La fecha de nacimiento es obligatoria");
            return false;
        }
        if (dpFechaIngreso.getValue() == null) {
            AlertHelper.advertencia("La fecha de ingreso es obligatoria");
            return false;
        }
        return true;
    }

    private boolean validarPaso3() {
        long contactosValidos = filasContacto.stream().filter(this::filaContactoTieneNombreYTelefono).count();
        if (contactosValidos < 2) {
            AlertHelper.advertencia("Se requieren al menos 2 contactos con nombre y teléfono");
            return false;
        }
        return true;
    }

    private boolean filaContactoTieneNombreYTelefono(HBox fila) {
        TextField tfNombre   = (TextField) fila.lookup("#tfNombreC");
        TextField tfTelefono = (TextField) fila.lookup("#tfTelC");
        return tfNombre   != null && !tfNombre.getText().trim().isEmpty()
            && tfTelefono != null && !tfTelefono.getText().trim().isEmpty();
    }

    // ── Contactos dinámicos ───────────────────────────────────
    @FXML
    private void onAgregarContacto() {
        agregarFilaContacto(false);
    }

    private void agregarFilaContacto(boolean esReferente) {
        TextField tfNombre   = new TextField(); tfNombre.setId("tfNombreC");   tfNombre.setPromptText("Nombre *");
        TextField tfTelefono = new TextField(); tfTelefono.setId("tfTelC");    tfTelefono.setPromptText("Teléfono *");
        TextField tfVinculo  = new TextField(); tfVinculo.setId("tfVinculo");  tfVinculo.setPromptText("Vínculo");
        CheckBox  chkRef     = new CheckBox("Referente");
        chkRef.setSelected(esReferente);

        Button btnEliminar = new Button("✕");
        btnEliminar.getStyleClass().add("btn-sm-danger");

        HBox fila = new HBox(8, tfNombre, tfTelefono, tfVinculo, chkRef, btnEliminar);
        fila.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox.setHgrow(tfNombre,   Priority.ALWAYS);
        HBox.setHgrow(tfTelefono, Priority.ALWAYS);

        btnEliminar.setOnAction(e -> {
            if (filasContacto.size() <= 2) {
                AlertHelper.advertencia("Debe haber al menos 2 contactos");
                return;
            }
            vboxContactos.getChildren().remove(fila);
            filasContacto.remove(fila);
        });

        filasContacto.add(fila);
        // Insertar antes del botón "Agregar contacto"
        int idx = vboxContactos.getChildren().indexOf(btnAgregarContacto);
        if (idx >= 0) vboxContactos.getChildren().add(idx, fila);
        else          vboxContactos.getChildren().add(fila);
    }

    // ── Guardar ───────────────────────────────────────────────
    @FXML
    private void onGuardar() {
        if (!validarPasoActual()) return;

        Map<String, Object> datos = construirPayload();
        progressGuardar.setVisible(true);
        btnGuardar.setDisable(true);

        CompletableFuture
            .supplyAsync(() -> modoEdicion
                    ? InternoService.actualizar(internoEditando.getId(), datos)
                    : InternoService.crear(datos))
            .thenAcceptAsync(result -> {
                progressGuardar.setVisible(false);
                btnGuardar.setDisable(false);

                if (result.success) {
                    AlertHelper.exito(modoEdicion ? "Interno actualizado correctamente" : "Interno dado de alta correctamente");
                    if (onGuardado != null) onGuardado.run();
                    cerrarVentana();
                } else {
                    AlertHelper.error("Error: " + result.errorMensaje);
                }
            }, Platform::runLater);
    }

    private Map<String, Object> construirPayload() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("nombre",     txtNombre.getText().trim());
        m.put("apellido",   txtApellido.getText().trim());
        m.put("dni",        txtDni.getText().trim());
        m.put("fecha_nacimiento", FormatHelper.localDateAIso(dpFechaNacimiento.getValue()));
        if (!modoEdicion) {
            m.put("fecha_ingreso", FormatHelper.localDateAIso(dpFechaIngreso.getValue()));
        }
        m.put("edad", FormatHelper.calcularEdad(FormatHelper.localDateAIso(dpFechaNacimiento.getValue())));
        m.put("direccion",   txtDireccion.getText().trim());
        m.put("tiene_hijos", chkHijos.isSelected());
        m.put("cantidad_hijos", spnCantHijos.getValue());
        m.put("es_judicializado", chkJudicializado.isSelected());

        m.put("estuvo_internado_antes",       chkInternadoAntes.isSelected());
        m.put("lugar_internacion_anterior",   txtLugarAnterior.getText().trim());
        m.put("toma_medicacion",              chkMedicacion.isSelected());
        m.put("detalle_medicacion",           txtDetalleMedicacion.getText().trim());
        m.put("tiene_patologia",              chkPatologia.isSelected());
        m.put("detalle_patologia",            txtDetallePatologia.getText().trim());
        m.put("nivel_estudios",               cmbNivelEstudios.getValue());

        m.put("tiene_obra_social",  chkObraSocial.isSelected());
        m.put("nombre_obra_social", txtObraSocial.getText().trim());
        m.put("cobra_pension",      chkPension.isSelected());
        m.put("tipo_pension",       txtTipoPension.getText().trim());
        m.put("tipo_pago_clasificacion", cmbTipoPago.getValue());

        // Contactos
        List<Map<String, Object>> contactos = new ArrayList<>();
        for (HBox fila : filasContacto) {
            TextField tfN = (TextField) fila.lookup("#tfNombreC");
            TextField tfT = (TextField) fila.lookup("#tfTelC");
            TextField tfV = (TextField) fila.lookup("#tfVinculo");
            CheckBox  chR = (CheckBox)  fila.getChildren().stream()
                    .filter(n -> n instanceof CheckBox).findFirst().orElse(null);

            String textN = tfN != null ? safe(tfN.getText()) : "";
            String textT = tfT != null ? safe(tfT.getText()) : "";
            if (!textN.trim().isEmpty() && !textT.trim().isEmpty()) {
                Map<String, Object> c = new LinkedHashMap<>();
                c.put("nombre",       textN.trim());
                c.put("telefono",     textT.trim());
                c.put("vinculo",      tfV != null ? safe(tfV.getText()).trim() : "");
                c.put("es_referente", chR != null && chR.isSelected());
                contactos.add(c);
            }
        }
        m.put("contactos", contactos);
        return m;
    }

    @FXML
    private void onCancelar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnGuardar.getScene().getWindow();
        stage.close();
    }

    private static String safe(String s) {
        return s != null ? s : "";
    }
}
