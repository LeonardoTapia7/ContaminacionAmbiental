package com.contaminacion.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "zonas")
public abstract class ZonaUrbana {
    @Id
    private String id;
    private String nombre;
    private Clima climaActual;
    private Contaminacion contaminacionActual;
    private List<Contaminacion> historico30Dias = new ArrayList<>();

    public ZonaUrbana() {
    }

    public ZonaUrbana(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Clima getClimaActual() {
        return climaActual;
    }

    public void setClimaActual(Clima climaActual) {
        this.climaActual = climaActual;
    }

    public Contaminacion getContaminacionActual() {
        return contaminacionActual;
    }

    public void setContaminacionActual(Contaminacion contaminacionActual) {
        this.contaminacionActual = contaminacionActual;
    }

    public List<Contaminacion> getHistorico30Dias() {
        return historico30Dias;
    }

    public void setHistorico30Dias(List<Contaminacion> historico30Dias) {
        this.historico30Dias = historico30Dias;
    }

    public void agregarRegistroHistorico(Contaminacion r) {
        if (r == null) return;
        historico30Dias.add(0, r); // add newest at front
        // keep max 30
        if (historico30Dias.size() > 30) {
            historico30Dias = new ArrayList<>(historico30Dias.subList(0, 30));
        }
    }

    public abstract void aplicarMedidasMitigacion();
}

