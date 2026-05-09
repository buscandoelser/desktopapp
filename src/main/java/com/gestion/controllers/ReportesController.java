package com.gestion.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.gestion.services.ReportesService;
import com.gestion.utils.AlertHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

public class ReportesController {

    @FXML private void onBalanceMensual() {
        LocalDate hoy = LocalDate.now();
        CompletableFuture
            .supplyAsync(() -> ReportesService.balanceMensual(hoy.getYear(), hoy.getMonthValue()))
            .thenAcceptAsync(result -> {
                if (!result.success) { AlertHelper.error("Error: " + result.errorMensaje); return; }
                JsonNode d = result.data;
                String msg = String.format(
                    "Balance %d/%d%nIngresos:  $%s%nEgresos:   $%s%nBalance:   $%s",
                    hoy.getMonthValue(), hoy.getYear(),
                    d.path("ingresos").asText("—"),
                    d.path("egresos").asText("—"),
                    d.path("balance").asText("—")
                );
                AlertHelper.info(msg);
            }, Platform::runLater);
    }

    @FXML private void onCuentaCorriente() {
        CompletableFuture
            .supplyAsync(ReportesService::controlDeuda)
            .thenAcceptAsync(result -> {
                if (!result.success) { AlertHelper.error("Error: " + result.errorMensaje); return; }
                JsonNode d = result.data;
                String total = d.path("total").asText("0");
                String deuda = d.has("datos") && d.get("datos").isArray() && d.get("datos").size() > 0
                        ? d.get("datos").get(0).path("deuda_total").asText("—")
                        : "—";
                AlertHelper.info("Control de deuda: " + total + " interno(s) con deuda activa.\nMayor deuda: $" + deuda);
            }, Platform::runLater);
    }

    @FXML private void onListadoJudicial() {
        CompletableFuture
            .supplyAsync(() -> ReportesService.internosActivos(null))
            .thenAcceptAsync(result -> {
                if (!result.success) { AlertHelper.error("Error: " + result.errorMensaje); return; }
                JsonNode datos = result.data.path("datos");
                long judicializados = 0;
                if (datos.isArray()) {
                    for (JsonNode i : datos) {
                        if (i.path("es_judicializado").asBoolean()) judicializados++;
                    }
                }
                AlertHelper.info("Internos judicializados activos: " + judicializados);
            }, Platform::runLater);
    }

    @FXML private void onCapacidad() {
        LocalDate hoy = LocalDate.now();
        CompletableFuture
            .supplyAsync(() -> ReportesService.comparativo(
                    hoy.getMonthValue() > 1 ? hoy.getMonthValue() - 1 : 12,
                    hoy.getMonthValue() > 1 ? hoy.getYear()           : hoy.getYear() - 1,
                    hoy.getMonthValue(), hoy.getYear()))
            .thenAcceptAsync(result -> {
                if (!result.success) { AlertHelper.error("Error: " + result.errorMensaje); return; }
                JsonNode d = result.data;
                String msg = String.format(
                    "Comparativo mensual%nMes anterior — Ingresos: $%s%nMes actual  — Ingresos: $%s%nVariación: $%s",
                    d.path("periodo1").path("ingresos").asText("—"),
                    d.path("periodo2").path("ingresos").asText("—"),
                    d.path("variacion").path("ingresos").asText("—")
                );
                AlertHelper.info(msg);
            }, Platform::runLater);
    }

    @FXML private void onObrasSociales() {
        LocalDate hoy = LocalDate.now();
        CompletableFuture
            .supplyAsync(() -> ReportesService.clasificacionPago(hoy.getYear(), hoy.getMonthValue()))
            .thenAcceptAsync(result -> {
                if (!result.success) { AlertHelper.error("Error: " + result.errorMensaje); return; }
                JsonNode d = result.data;
                StringBuilder msg = new StringBuilder("Clasificación de pago — ")
                        .append(hoy.getMonthValue()).append("/").append(hoy.getYear())
                        .append("\nTotal: $").append(d.path("total_general").asText("—")).append("\n");
                if (d.has("datos")) {
                    for (JsonNode item : d.get("datos")) {
                        msg.append(String.format("%n%s: $%s (%s%%)",
                                item.path("tipo_pago_clasificacion").asText(),
                                item.path("total_cobrado").asText(),
                                item.path("porcentaje").asText()));
                    }
                }
                AlertHelper.info(msg.toString());
            }, Platform::runLater);
    }

    @FXML private void onPagosProveedores() {
        LocalDate hoy = LocalDate.now();
        CompletableFuture
            .supplyAsync(() -> ReportesService.egresosMes(hoy.getYear(), hoy.getMonthValue()))
            .thenAcceptAsync(result -> {
                if (!result.success) { AlertHelper.error("Error: " + result.errorMensaje); return; }
                JsonNode d = result.data;
                AlertHelper.info(String.format(
                    "Egresos %d/%d%nTotal: $%s",
                    hoy.getMonthValue(), hoy.getYear(),
                    d.path("total_general").asText("—")
                ));
            }, Platform::runLater);
    }
}
