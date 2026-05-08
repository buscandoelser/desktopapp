package com.gestion.controllers;

import com.gestion.utils.AlertHelper;
import javafx.fxml.FXML;

public class ReportesController {

    @FXML private void onBalanceMensual()    { AlertHelper.info("Reporte: Balance mensual — en desarrollo."); }
    @FXML private void onCuentaCorriente()   { AlertHelper.info("Reporte: Cuenta corriente — en desarrollo."); }
    @FXML private void onListadoJudicial()   { AlertHelper.info("Reporte: Listado judicial — en desarrollo."); }
    @FXML private void onCapacidad()         { AlertHelper.info("Reporte: Capacidad histórica — en desarrollo."); }
    @FXML private void onObrasSociales()     { AlertHelper.info("Reporte: Cobros por O. Social — en desarrollo."); }
    @FXML private void onPagosProveedores()  { AlertHelper.info("Reporte: Pagos a proveedores — en desarrollo."); }
}
