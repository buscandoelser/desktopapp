package com.gestion.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestion.config.AppConfig;
import com.gestion.models.Cuota;
import com.gestion.models.Interno;
import com.gestion.services.CobranzasService;
import com.gestion.services.InternoService;
import com.gestion.utils.AlertHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RegistrarCobroModalController {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String[] MESES_NOMBRE = {
        "Enero","Febrero","Marzo","Abril","Mayo","Junio",
        "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"
    };

    // ── Tab 1: Registrar Pago ─────────────────────────────────
    @FXML private ComboBox<Interno>  cmbInterno;
    @FXML private VBox               panelCuota;
    @FXML private Label              lblPeriodo;
    @FXML private Label              lblMontoOriginal;
    @FXML private Label              lblSaldo;
    @FXML private Label              lblEstadoCuota;
    @FXML private Label              lblSinCuota;
    @FXML private TextField          txtMonto;
    @FXML private ComboBox<String>   cmbMedio;
    @FXML private VBox               panelObraSocial;
    @FXML private TextField          txtPagadorObraSocial;
    @FXML private DatePicker         dpFechaPago;
    @FXML private TextField          txtDescuento;
    @FXML private VBox               panelDescripcionDescuento;
    @FXML private TextField          txtDescripcionDescuento;
    @FXML private TextArea           txtObservacion;

    // ── Tab 2: Generar Mes ────────────────────────────────────
    @FXML private Tab                tabGenerar;
    @FXML private ComboBox<Integer>  cmbAnio;
    @FXML private ComboBox<String>   cmbMesGenerar;
    @FXML private ComboBox<Interno>  cmbInternoGenerar;
    @FXML private Label              lblResultadoGenerar;

    // ── Shared ────────────────────────────────────────────────
    @FXML private TabPane            tabPane;
    @FXML private ProgressIndicator  progressIndicator;
    @FXML private Button             btnConfirmar;

    private Cuota    cuotaPreseleccionada;
    private Runnable onExito;

    public void setOnExito(Runnable cb)                   { this.onExito = cb; }
    public void setCuotaPreseleccionada(Cuota cuota)      { this.cuotaPreseleccionada = cuota; }

    // ── Inicialización ────────────────────────────────────────
    @FXML
    public void initialize() {
        configurarConverters();
        configurarTab1();
        configurarTab2();
        configurarCambioTab();
        actualizarBoton();
        cargarInternos();
    }

    private void configurarConverters() {
        StringConverter<Interno> conv = new StringConverter<>() {
            @Override public String toString(Interno i) {
                return i == null ? "" : i.getNombreCompleto().trim() + "  ·  " + i.getLegajo();
            }
            @Override public Interno fromString(String s) { return null; }
        };
        cmbInterno.setConverter(conv);
        cmbInternoGenerar.setConverter(conv);
    }

    private void configurarTab1() {
        cmbMedio.setItems(FXCollections.observableArrayList("efectivo", "transferencia", "obra_social"));
        cmbMedio.setValue("efectivo");
        dpFechaPago.setValue(LocalDate.now());

        txtMonto.textProperty().addListener((obs, o, n) -> actualizarBoton());
        cmbMedio.valueProperty().addListener((obs, o, n) -> {
            boolean esObraSocial = "obra_social".equals(n);
            panelObraSocial.setVisible(esObraSocial);
            panelObraSocial.setManaged(esObraSocial);
            if (!esObraSocial) txtPagadorObraSocial.clear();
            actualizarBoton();
        });
        cmbInterno.valueProperty().addListener((obs, o, n) -> actualizarBoton());
        txtPagadorObraSocial.textProperty().addListener((obs, o, n) -> actualizarBoton());
        txtDescuento.textProperty().addListener((obs, o, n) -> {
            double desc = parseDescuento();
            boolean tieneDescuento = desc > 0;
            panelDescripcionDescuento.setVisible(tieneDescuento);
            panelDescripcionDescuento.setManaged(tieneDescuento);
            if (!tieneDescuento) txtDescripcionDescuento.clear();
            actualizarBoton();
        });
        txtDescripcionDescuento.textProperty().addListener((obs, o, n) -> actualizarBoton());
    }

    private void configurarTab2() {
        boolean esAdmin = AppConfig.tieneRol("admin");
        tabGenerar.setDisable(!esAdmin);

        int anioActual = LocalDate.now().getYear();
        cmbAnio.setItems(FXCollections.observableArrayList(
                anioActual - 1, anioActual, anioActual + 1));
        cmbAnio.setValue(anioActual);

        cmbMesGenerar.setItems(FXCollections.observableArrayList(MESES_NOMBRE));
        cmbMesGenerar.setValue(MESES_NOMBRE[LocalDate.now().getMonthValue() - 1]);
    }

    private void configurarCambioTab() {
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, old, tab) -> {
            if (tab != null && tab.equals(tabGenerar)) {
                btnConfirmar.setText("GENERAR CUOTAS");
            } else {
                btnConfirmar.setText("REGISTRAR COBRO");
            }
            actualizarBoton();
        });
    }

    // ── Carga de internos ─────────────────────────────────────
    private void cargarInternos() {
        CompletableFuture
            .supplyAsync(() -> InternoService.listar("activo", null, 1))
            .thenAcceptAsync(result -> {
                if (!result.success) return;
                List<Interno> lista = (List<Interno>) result.data;
                ObservableList<Interno> obs = FXCollections.observableArrayList(lista);
                cmbInterno.setItems(obs);
                cmbInternoGenerar.setItems(FXCollections.observableArrayList(lista));

                if (cuotaPreseleccionada != null) {
                    lista.stream()
                         .filter(i -> i.getId() == cuotaPreseleccionada.getInternoId())
                         .findFirst()
                         .ifPresent(i -> {
                             cmbInterno.setValue(i);
                             mostrarCuota(cuotaPreseleccionada);
                         });
                }
            }, Platform::runLater);
    }

    // ── Selección de interno → carga la cuota pendiente ───────
    @FXML
    private void onInternoSeleccionado() {
        Interno interno = cmbInterno.getValue();
        if (interno == null) {
            ocultarPanelCuota();
            return;
        }
        // Si ya tenemos la cuota preseleccionada para este interno, usarla directamente
        if (cuotaPreseleccionada != null
                && cuotaPreseleccionada.getInternoId() == interno.getId()) {
            mostrarCuota(cuotaPreseleccionada);
            return;
        }
        cuotaPreseleccionada = null;
        setLoading(true);
        CompletableFuture
            .supplyAsync(() -> CobranzasService.cuentaCorriente(interno.getId()))
            .thenAcceptAsync(result -> {
                setLoading(false);
                if (!result.success) {
                    AlertHelper.error("Error al cargar cuota: " + result.errorMensaje);
                    return;
                }
                Cuota cuota = parsearCuotaPendiente(result.data);
                if (cuota != null) {
                    mostrarCuota(cuota);
                } else {
                    ocultarPanelCuota();
                    lblSinCuota.setVisible(true);
                    lblSinCuota.setManaged(true);
                    actualizarBoton();
                }
            }, Platform::runLater);
    }

    private void mostrarCuota(Cuota cuota) {
        cuotaPreseleccionada = cuota;
        lblPeriodo.setText(cuota.getMesPeriodo());
        lblMontoOriginal.setText("$" + cuota.getTotalConInteres());
        lblSaldo.setText("$" + cuota.getSaldoPendiente());
        lblEstadoCuota.setText(cuota.getEstadoDisplay());

        panelCuota.setVisible(true);
        panelCuota.setManaged(true);
        lblSinCuota.setVisible(false);
        lblSinCuota.setManaged(false);

        txtMonto.setText(cuota.getSaldoPendiente());
        actualizarBoton();
    }

    private void ocultarPanelCuota() {
        cuotaPreseleccionada = null;
        panelCuota.setVisible(false);
        panelCuota.setManaged(false);
        lblSinCuota.setVisible(false);
        lblSinCuota.setManaged(false);
        txtMonto.clear();
        actualizarBoton();
    }

    // ── Validación del botón ──────────────────────────────────
    private void actualizarBoton() {
        Tab seleccionado = tabPane.getSelectionModel().getSelectedItem();
        boolean esTabGenerar = seleccionado != null && seleccionado.equals(tabGenerar);

        if (esTabGenerar) {
            btnConfirmar.setDisable(
                    cmbAnio.getValue() == null || cmbMesGenerar.getValue() == null);
        } else {
            double monto = parseMonto();
            double saldo = parseDouble(cuotaPreseleccionada != null
                    ? cuotaPreseleccionada.getSaldoPendiente() : null);
            boolean obraSocialOk = !"obra_social".equals(cmbMedio.getValue())
                    || !txtPagadorObraSocial.getText().trim().isEmpty();
            boolean descuentoOk = parseDescuento() <= 0
                    || !txtDescripcionDescuento.getText().trim().isEmpty();
            boolean valido = cuotaPreseleccionada != null
                    && monto > 0
                    && monto <= saldo
                    && cmbMedio.getValue() != null
                    && dpFechaPago.getValue() != null
                    && obraSocialOk
                    && descuentoOk;
            btnConfirmar.setDisable(!valido);
        }
    }

    // ── Confirmar ─────────────────────────────────────────────
    @FXML
    private void onConfirmar() {
        Tab seleccionado = tabPane.getSelectionModel().getSelectedItem();
        if (seleccionado != null && seleccionado.equals(tabGenerar)) {
            generarMes();
        } else {
            registrarPago();
        }
    }

    private void registrarPago() {
        double monto = parseMonto();
        double saldo = parseDouble(cuotaPreseleccionada.getSaldoPendiente());

        if (monto <= 0) {
            AlertHelper.error("El monto debe ser mayor a cero.");
            return;
        }
        if (monto > saldo) {
            AlertHelper.error("El monto no puede superar el saldo pendiente ($"
                    + cuotaPreseleccionada.getSaldoPendiente() + ").");
            return;
        }

        Map<String, Object> datos = new HashMap<>();
        datos.put("cuota_id",    cuotaPreseleccionada.getId());
        datos.put("interno_id",  cmbInterno.getValue().getId());
        datos.put("monto",       String.valueOf(monto));
        datos.put("medio_pago",  cmbMedio.getValue());
        datos.put("fecha_pago",  dpFechaPago.getValue().toString());

        if ("obra_social".equals(cmbMedio.getValue())) {
            datos.put("pagador_obra_social", txtPagadorObraSocial.getText().trim());
        }

        double descuento = parseDescuento();
        if (descuento > 0) {
            datos.put("descuento", String.valueOf(descuento));
            datos.put("descripcion_descuento", txtDescripcionDescuento.getText().trim());
        } else {
            datos.put("descuento", "0");
        }

        String obs = txtObservacion.getText().trim();
        if (!obs.isEmpty()) datos.put("observacion", obs);

        setLoading(true);
        CompletableFuture
            .supplyAsync(() -> CobranzasService.registrarPago(datos))
            .thenAcceptAsync(result -> {
                setLoading(false);
                if (result.success) {
                    AlertHelper.exito("Cobro registrado correctamente.");
                    if (onExito != null) onExito.run();
                    cerrar();
                } else {
                    AlertHelper.error("Error al registrar cobro: " + result.errorMensaje);
                }
            }, Platform::runLater);
    }

    private void generarMes() {
        int anio = cmbAnio.getValue();
        int mes  = indexOfMes(cmbMesGenerar.getValue()) + 1;
        Interno internoSel = cmbInternoGenerar.getValue();
        Integer internoId  = internoSel != null ? internoSel.getId() : null;
        String periodo = MESES_NOMBRE[mes - 1] + " " + anio;
        String nombreInterno = internoSel != null ? internoSel.getNombreCompleto().trim() : null;

        setLoading(true);
        lblResultadoGenerar.setText("Generando cuotas...");

        CompletableFuture
            .supplyAsync(() -> CobranzasService.generarMes(anio, mes, internoId))
            .thenAcceptAsync(result -> {
                setLoading(false);
                if (!result.success) {
                    lblResultadoGenerar.setText("Error: " + result.errorMensaje);
                    AlertHelper.error("Error al generar cuotas: " + result.errorMensaje);
                    return;
                }

                JsonNode data   = result.data;
                int creadas     = intDe(data, "creadas", "cuotas_creadas", "generadas");
                int omitidas    = intDe(data, "omitidas", "ya_existentes", "existentes", "duplicadas");
                String mensaje  = data != null && data.has("mensaje") ? data.get("mensaje").asText() : null;

                if (creadas == 0 && omitidas > 0) {
                    String txt = nombreInterno != null
                            ? "La cuota de " + periodo + " para " + nombreInterno + " ya estaba generada."
                            : "Las cuotas de " + periodo + " ya estaban generadas (" + omitidas + " omitidas).";
                    lblResultadoGenerar.setText(txt);
                    AlertHelper.info(txt);
                } else if (creadas > 0 && omitidas > 0) {
                    String txt = "Se generaron " + creadas + " cuotas. " + omitidas + " ya existían y fueron omitidas.";
                    lblResultadoGenerar.setText(txt);
                    AlertHelper.exito(txt);
                } else if (creadas > 0) {
                    String txt = nombreInterno != null
                            ? "Cuota de " + periodo + " generada para " + nombreInterno + "."
                            : "Cuotas generadas: " + creadas + " (" + periodo + ").";
                    lblResultadoGenerar.setText(txt);
                    AlertHelper.exito(txt);
                } else {
                    String txt = mensaje != null ? mensaje : "Operación completada sin cambios.";
                    lblResultadoGenerar.setText(txt);
                    AlertHelper.info(txt);
                }

                if (onExito != null) onExito.run();
            }, Platform::runLater);
    }

    private int intDe(JsonNode node, String... keys) {
        if (node == null) return 0;
        for (String k : keys) {
            if (node.has(k) && node.get(k).isNumber()) return node.get(k).asInt();
        }
        return 0;
    }

    @FXML
    private void onCancelar() {
        cerrar();
    }

    @FXML
    private void onLimpiarInternoGenerar() {
        cmbInternoGenerar.getSelectionModel().clearSelection();
        cmbInternoGenerar.setValue(null);
        lblResultadoGenerar.setText("");
        actualizarBoton();
    }

    // ── Helpers ───────────────────────────────────────────────
    private void cerrar() {
        ((Stage) btnConfirmar.getScene().getWindow()).close();
    }

    private void setLoading(boolean cargando) {
        progressIndicator.setVisible(cargando);
        btnConfirmar.setDisable(cargando);
        cmbInterno.setDisable(cargando);
        txtMonto.setDisable(cargando);
        cmbMedio.setDisable(cargando);
        txtPagadorObraSocial.setDisable(cargando);
        dpFechaPago.setDisable(cargando);
        txtDescuento.setDisable(cargando);
        txtDescripcionDescuento.setDisable(cargando);
        cmbAnio.setDisable(cargando);
        cmbMesGenerar.setDisable(cargando);
        cmbInternoGenerar.setDisable(cargando);
    }

    /** Parsea la respuesta de cuentaCorriente buscando la cuota pendiente más reciente */
    private Cuota parsearCuotaPendiente(JsonNode node) {
        if (node == null || node.isNull()) return null;
        try {
            JsonNode target = null;

            if (node.isArray()) {
                target = buscarMasReciente(node);
            } else if (node.has("cuota_activa") && !node.get("cuota_activa").isNull()) {
                target = node.get("cuota_activa");
            } else if (node.has("datos") && node.get("datos").isArray()) {
                target = buscarMasReciente(node.get("datos"));
            } else if (node.has("cuotas") && node.get("cuotas").isArray()) {
                target = buscarMasReciente(node.get("cuotas"));
            } else if (node.has("estado")) {
                String estado = node.get("estado").asText();
                if (esPendiente(estado)) target = node;
            }

            if (target == null) return null;
            return MAPPER.treeToValue(target, Cuota.class);
        } catch (Exception e) {
            return null;
        }
    }

    /** Devuelve el nodo de cuota pendiente/parcial/con_mora con período más reciente */
    private JsonNode buscarMasReciente(JsonNode array) {
        JsonNode encontrado = null;
        int mejorAnio = -1;
        int mejorMes  = -1;

        for (JsonNode item : array) {
            String estado = item.has("estado") ? item.get("estado").asText() : "";
            if (!esPendiente(estado)) continue;

            int anio = item.has("anio") ? item.get("anio").asInt() : 0;
            int mes  = item.has("mes")  ? item.get("mes").asInt()  : 0;

            if (anio > mejorAnio || (anio == mejorAnio && mes > mejorMes)) {
                mejorAnio  = anio;
                mejorMes   = mes;
                encontrado = item;
            }
        }
        return encontrado;
    }

    private boolean esPendiente(String estado) {
        return "pendiente".equals(estado)
            || "pagada_parcial".equals(estado)
            || "con_mora".equals(estado);
    }

    private String extraerMensaje(JsonNode node) {
        if (node == null) return "Operación completada.";
        if (node.has("mensaje")) return node.get("mensaje").asText();
        if (node.has("creadas")) return "Cuotas creadas: " + node.get("creadas").asInt();
        return "Operación completada.";
    }

    private int indexOfMes(String nombre) {
        for (int i = 0; i < MESES_NOMBRE.length; i++) {
            if (MESES_NOMBRE[i].equals(nombre)) return i;
        }
        return LocalDate.now().getMonthValue() - 1;
    }

    private double parseMonto() {
        try {
            return Double.parseDouble(txtMonto.getText().trim().replace(",", "."));
        } catch (Exception e) {
            return 0;
        }
    }

    private double parseDescuento() {
        try {
            String text = txtDescuento.getText().trim();
            if (text.isEmpty()) return 0;
            return Double.parseDouble(text.replace(",", "."));
        } catch (Exception e) {
            return 0;
        }
    }

    private double parseDouble(String s) {
        try { return s != null ? Double.parseDouble(s) : 0.0; } catch (Exception e) { return 0.0; }
    }
}
