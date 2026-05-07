package com.gestion.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ContactoFamiliar {

    private int    id;

    @JsonProperty("interno_id")
    private int    internoId;

    private String nombre;
    private String apellido;
    private String vinculo;
    private String telefono;

    @JsonProperty("telefono_alt")
    private String telefonoAlt;

    private String dni;
    private String domicilio;

    @JsonProperty("es_referente")
    private boolean esReferente;

    public ContactoFamiliar() {}

    public ContactoFamiliar(String nombre, String telefono) {
        this.nombre   = nombre;
        this.telefono = telefono;
    }

    // ── Getters / Setters ─────────────────────────────────────
    public int    getId()                    { return id; }
    public void   setId(int id)              { this.id = id; }

    public int    getInternoId()             { return internoId; }
    public void   setInternoId(int id)       { this.internoId = id; }

    public String getNombre()                { return nombre; }
    public void   setNombre(String nombre)   { this.nombre = nombre; }

    public String getApellido()              { return apellido; }
    public void   setApellido(String a)      { this.apellido = a; }

    public String getVinculo()               { return vinculo; }
    public void   setVinculo(String v)       { this.vinculo = v; }

    public String getTelefono()              { return telefono; }
    public void   setTelefono(String t)      { this.telefono = t; }

    public String getTelefonoAlt()           { return telefonoAlt; }
    public void   setTelefonoAlt(String t)   { this.telefonoAlt = t; }

    public String getDni()                   { return dni; }
    public void   setDni(String dni)         { this.dni = dni; }

    public String getDomicilio()             { return domicilio; }
    public void   setDomicilio(String d)     { this.domicilio = d; }

    public boolean isEsReferente()           { return esReferente; }
    public void    setEsReferente(boolean e) { this.esReferente = e; }
}
