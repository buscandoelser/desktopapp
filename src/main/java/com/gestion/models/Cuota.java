package com.gestion.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Cuota {

    private int    id;
    @JsonProperty("interno_id")              private int    internoId;
    private int    anio;
    private int    mes;

    @JsonProperty("valor_base")              private String valorBase;
    private String descuento;
    @JsonProperty("bonificacion_descripcion") private String bonificacionDescripcion;
    @JsonProperty("interes_aplicado")        private String interesAplicado;
    @JsonProperty("total_con_interes")       private String totalConInteres;
    private String estado;
    @JsonProperty("fecha_vencimiento")       private String fechaVencimiento;
    @JsonProperty("monto_pagado")            private String montoPagado;
    @JsonProperty("saldo_pendiente")         private String saldoPendiente;
    @JsonProperty("interno_nombre")          private String internoNombre;
    private String legajo;

    public int    getId()                       { return id; }
    public int    getInternoId()                { return internoId; }
    public int    getAnio()                     { return anio; }
    public int    getMes()                      { return mes; }
    public String getValorBase()                { return valorBase                != null ? valorBase                : "0"; }
    public String getDescuento()                { return descuento                != null ? descuento                : "0"; }
    public String getBonificacionDescripcion()  { return bonificacionDescripcion  != null ? bonificacionDescripcion  : ""; }
    public String getInteresAplicado()          { return interesAplicado          != null ? interesAplicado          : "0"; }
    public String getTotalConInteres()          { return totalConInteres          != null ? totalConInteres          : getValorBase(); }
    public String getMontoOriginal()            { return getValorBase(); }
    public String getEstado()                   { return estado                   != null ? estado                   : "pendiente"; }
    public String getMontoPagado()              { return montoPagado              != null ? montoPagado              : "0"; }
    public String getSaldoPendiente()           { return saldoPendiente           != null ? saldoPendiente           : getTotalConInteres(); }
    public String getInternoNombre()            { return internoNombre            != null ? internoNombre            : "—"; }
    public String getLegajo()                   { return legajo                   != null ? legajo                   : "—"; }

    public String getFechaVencimiento() {
        if (fechaVencimiento == null) return "—";
        int idx = fechaVencimiento.indexOf('T');
        return idx > 0 ? fechaVencimiento.substring(0, idx) : fechaVencimiento;
    }

    public String getMesPeriodo() {
        String[] meses = { "", "Ene", "Feb", "Mar", "Abr", "May", "Jun",
                           "Jul", "Ago", "Sep", "Oct", "Nov", "Dic" };
        return (mes >= 1 && mes <= 12 ? meses[mes] : mes) + " " + anio;
    }

    public String getEstadoDisplay() {
        if (estado == null) return "";
        return switch (estado) {
            case "pendiente"      -> "Pendiente";
            case "pagada_parcial" -> "Parcial";
            case "pagada"         -> "Pagada";
            case "con_mora"       -> "Con mora";
            default               -> estado;
        };
    }
}
