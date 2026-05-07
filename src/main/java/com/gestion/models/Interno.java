package com.gestion.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Interno {

    private int     id;
    private String  legajo;
    private String  nombre;
    private String  apellido;
    private String  dni;

    @JsonProperty("fecha_nacimiento")
    private String fechaNacimiento;
    private Integer edad;

    @JsonProperty("tiene_hijos")
    private boolean tieneHijos;
    @JsonProperty("cantidad_hijos")
    private int     cantidadHijos;
    private String  direccion;

    @JsonProperty("es_judicializado")
    private boolean esJudicializado;

    @JsonProperty("estuvo_internado_antes")
    private boolean estuvoInternadoAntes;
    @JsonProperty("lugar_internacion_anterior")
    private String  lugarInternacionAnterior;

    @JsonProperty("toma_medicacion")
    private boolean tomaMedicacion;
    @JsonProperty("detalle_medicacion")
    private String  detalleMedicacion;

    @JsonProperty("tiene_patologia")
    private boolean tienePatologia;
    @JsonProperty("detalle_patologia")
    private String  detallePatologia;

    @JsonProperty("nivel_estudios")
    private String  nivelEstudios;

    @JsonProperty("tiene_obra_social")
    private boolean tieneObraSocial;
    @JsonProperty("nombre_obra_social")
    private String  nombreObraSocial;

    @JsonProperty("cobra_pension")
    private boolean cobraPension;
    @JsonProperty("tipo_pension")
    private String  tipoPension;

    @JsonProperty("tipo_pago_clasificacion")
    private String  tipoPagoClasificacion;

    private String  estado;

    @JsonProperty("fecha_ingreso")
    private String  fechaIngreso;
    @JsonProperty("fecha_egreso")
    private String  fechaEgreso;
    @JsonProperty("motivo_egreso")
    private String  motivoEgreso;

    @JsonProperty("created_at")
    private String  createdAt;

    private List<ContactoFamiliar> contactos;

    // ── Constructor vacío requerido por Jackson ───────────────
    public Interno() {}

    // ── Helpers ───────────────────────────────────────────────
    public String getNombreCompleto() {
        return (nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "");
    }

    public String getEstadoDisplay() {
        if (estado == null) return "";
        return switch (estado) {
            case "activo"   -> "Activo";
            case "alta"     -> "Alta médica";
            case "abandono" -> "Abandono";
            case "fallecido"-> "Fallecido";
            case "derivado" -> "Derivado";
            default         -> estado;
        };
    }

    // ── Getters / Setters ─────────────────────────────────────
    public int getId()                            { return id; }
    public void setId(int id)                     { this.id = id; }

    public String getLegajo()                     { return legajo; }
    public void setLegajo(String legajo)          { this.legajo = legajo; }

    public String getNombre()                     { return nombre; }
    public void setNombre(String nombre)          { this.nombre = nombre; }

    public String getApellido()                   { return apellido; }
    public void setApellido(String apellido)      { this.apellido = apellido; }

    public String getDni()                        { return dni; }
    public void setDni(String dni)                { this.dni = dni; }

    public String getFechaNacimiento()                          { return fechaNacimiento; }
    public void setFechaNacimiento(String fechaNacimiento)      { this.fechaNacimiento = fechaNacimiento; }

    public Integer getEdad()                      { return edad; }
    public void setEdad(Integer edad)             { this.edad = edad; }

    public boolean isTieneHijos()                 { return tieneHijos; }
    public void setTieneHijos(boolean tieneHijos) { this.tieneHijos = tieneHijos; }

    public int getCantidadHijos()                         { return cantidadHijos; }
    public void setCantidadHijos(int cantidadHijos)       { this.cantidadHijos = cantidadHijos; }

    public String getDireccion()                  { return direccion; }
    public void setDireccion(String direccion)    { this.direccion = direccion; }

    public boolean isEsJudicializado()                        { return esJudicializado; }
    public void setEsJudicializado(boolean esJudicializado)   { this.esJudicializado = esJudicializado; }

    public boolean isEstuvoInternadoAntes()                         { return estuvoInternadoAntes; }
    public void setEstuvoInternadoAntes(boolean estuvoInternadoAntes) { this.estuvoInternadoAntes = estuvoInternadoAntes; }

    public String getLugarInternacionAnterior()                             { return lugarInternacionAnterior; }
    public void setLugarInternacionAnterior(String lugarInternacionAnterior) { this.lugarInternacionAnterior = lugarInternacionAnterior; }

    public boolean isTomaMedicacion()                         { return tomaMedicacion; }
    public void setTomaMedicacion(boolean tomaMedicacion)     { this.tomaMedicacion = tomaMedicacion; }

    public String getDetalleMedicacion()                          { return detalleMedicacion; }
    public void setDetalleMedicacion(String detalleMedicacion)    { this.detalleMedicacion = detalleMedicacion; }

    public boolean isTienePatologia()                         { return tienePatologia; }
    public void setTienePatologia(boolean tienePatologia)     { this.tienePatologia = tienePatologia; }

    public String getDetallePatologia()                       { return detallePatologia; }
    public void setDetallePatologia(String detallePatologia)  { this.detallePatologia = detallePatologia; }

    public String getNivelEstudios()                          { return nivelEstudios; }
    public void setNivelEstudios(String nivelEstudios)        { this.nivelEstudios = nivelEstudios; }

    public boolean isTieneObraSocial()                        { return tieneObraSocial; }
    public void setTieneObraSocial(boolean tieneObraSocial)   { this.tieneObraSocial = tieneObraSocial; }

    public String getNombreObraSocial()                       { return nombreObraSocial; }
    public void setNombreObraSocial(String nombreObraSocial)  { this.nombreObraSocial = nombreObraSocial; }

    public boolean isCobraPension()                   { return cobraPension; }
    public void setCobraPension(boolean cobraPension) { this.cobraPension = cobraPension; }

    public String getTipoPension()                    { return tipoPension; }
    public void setTipoPension(String tipoPension)    { this.tipoPension = tipoPension; }

    public String getTipoPagoClasificacion()                              { return tipoPagoClasificacion; }
    public void setTipoPagoClasificacion(String tipoPagoClasificacion)    { this.tipoPagoClasificacion = tipoPagoClasificacion; }

    public String getEstado()                     { return estado; }
    public void setEstado(String estado)          { this.estado = estado; }

    public String getFechaIngreso()               { return fechaIngreso; }
    public void setFechaIngreso(String f)         { this.fechaIngreso = f; }

    public String getFechaEgreso()                { return fechaEgreso; }
    public void setFechaEgreso(String f)          { this.fechaEgreso = f; }

    public String getMotivoEgreso()               { return motivoEgreso; }
    public void setMotivoEgreso(String m)         { this.motivoEgreso = m; }

    public String getCreatedAt()                  { return createdAt; }
    public void setCreatedAt(String c)            { this.createdAt = c; }

    public List<ContactoFamiliar> getContactos()              { return contactos; }
    public void setContactos(List<ContactoFamiliar> contactos){ this.contactos = contactos; }
}
