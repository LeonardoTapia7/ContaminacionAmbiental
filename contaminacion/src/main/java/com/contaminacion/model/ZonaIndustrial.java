package com.contaminacion.model;

public class ZonaIndustrial extends ZonaUrbana {
    private int numeroFabricasActivas;

    public ZonaIndustrial() {
        super();
    }

    public ZonaIndustrial(String id, String nombre, int numeroFabricasActivas) {
        super(id, nombre);
        this.numeroFabricasActivas = numeroFabricasActivas;
    }

    public int getNumeroFabricasActivas() {
        return numeroFabricasActivas;
    }

    public void setNumeroFabricasActivas(int numeroFabricasActivas) {
        this.numeroFabricasActivas = numeroFabricasActivas;
    }

    @Override
    public void aplicarMedidasMitigacion() {
        // Ejemplo simple: reducir emisiones simulando menos fábricas activas
        if (numeroFabricasActivas > 0) {
            numeroFabricasActivas = Math.max(0, numeroFabricasActivas - 1);
        }
        // Si hay contaminacionActual, bajar ligeramente las magnitudes
        if (getContaminacionActual() != null) {
            getContaminacionActual().setCo2(Math.max(0, getContaminacionActual().getCo2() * 0.95));
            getContaminacionActual().setPm25(Math.max(0, getContaminacionActual().getPm25() * 0.9));
        }
    }
}

