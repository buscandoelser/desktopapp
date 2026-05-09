package com.gestion.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Cuota {

    private int    id;
    @JsonProperty("interno_id")       private int    internoId;
    private int    anio;
    private int    mes;
    @JsonProperty("monto_original")   private String montoOriginal;
    @JsonProperty("monto_pagado")     private String montoPagado;
    @JsonProperty("monto_interes")    private String montoInteres;
    private String descuento;
    @JsonProperty("descripcion_descuento") private String descripcionDescuento;
    private String estado;
    @JsonProperty("fecha_vencimiento")     private String fechaVencimiento;
    @JsonProperty("fecha_pago_completo")   private String fechaPagoCompleto;
    @JsonProperty("saldo_pendiente")       private String saldoPendiente;
    @JsonProperty("interno_nombre")        private String internoNombre;
    private String legajo;

    public int    getId()                  { return id; }
    public int    getInternoId()           { return internoId; }
    public int    getAnio()                { return anio; }
    public int    getMes()                 { return mes; }
    public String getMontoOriginal()       { return montoOriginal; }
    public String getMontoPagado()         { return montoPagado; }
    public String getMontoInteres()        { return montoInteres; }
    public String getDescuento()           { return descuento; }
    public String getDescripcionDescuento(){ return descripcionDescuento; }
    public String getEstado()              { return estado; }
    public String getFechaVencimiento()    { return fechaVencimiento; }
    public String getFechaPagoCompleto()   { return fechaPagoCompleto; }
    public String getSaldoPendiente()      { return saldoPendiente; }
    public String getInternoNombre()       { return internoNombre; }
    public String getLegajo()              { return legajo; }

    public String getMesPeriodo() {
        String[] meses = { "", "Ene", "Feb", "Mar", "Abr", "May", "Jun",
                           "Jul", "Ago", "Sep", "Oct", "Nov", "Dic" };
        return (mes >= 1 && mes <= 12 ? meses[mes] : mes) + " " + anio;
    }

    public String getEstadoDisplay() {
        if (estado == null) return "";
        return switch (estado) {
            case "pendiente" -> "Pendiente";
            case "parcial"   -> "Parcial";
            case "pagada"    -> "Pagada";
            case "con_mora"  -> "Con mora";
            default          -> estado;
        };
    }
}
