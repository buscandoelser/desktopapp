package com.gestion.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Egreso {

    private int    id;
    @JsonProperty("concepto")         private String descripcion;
    private String monto;
    private String categoria;
    @JsonProperty("medio_pago")       private String medioPago;
    private String fecha;
    @JsonProperty("created_at")       private String createdAt;
    @JsonProperty("registrado_por")   private String registradoPor;

    public int    getId()             { return id; }
    public String getDescripcion()    { return descripcion    != null ? descripcion    : "—"; }
    public String getMonto()          { return monto          != null ? monto          : "0"; }
    public String getCategoria()      { return categoria      != null ? categoria      : "—"; }
    public String getMedioPago()      { return medioPago      != null ? medioPago      : "—"; }
    public String getCreatedAt()      { return createdAt      != null ? createdAt      : "—"; }
    public String getRegistradoPor()  { return registradoPor  != null ? registradoPor  : "—"; }

    public String getFecha() {
        if (fecha == null) return "—";
        int idx = fecha.indexOf('T');
        return idx > 0 ? fecha.substring(0, idx) : fecha;
    }
}
