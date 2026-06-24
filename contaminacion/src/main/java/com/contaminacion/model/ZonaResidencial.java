package com.contaminacion.model;

public class ZonaResidencial extends ZonaUrbana {
    private boolean restriccionVehicularActiva;

    public ZonaResidencial() {
        super();
    }

    public ZonaResidencial(String id, String nombre, boolean restriccionVehicularActiva) {
        super(id, nombre);
        this.restriccionVehicularActiva = restriccionVehicularActiva;
    }

    public boolean isRestriccionVehicularActiva() {
        return restriccionVehicularActiva;
    }

    public void setRestriccionVehicularActiva(boolean restriccionVehicularActiva) {
        this.restriccionVehicularActiva = restriccionVehicularActiva;
    }

    @Override
    public void aplicarMedidasMitigacion() {
        // Activar restriccion vehicular como medida
        this.restriccionVehicularActiva = true;
        if (getContaminacionActual() != null) {
            getContaminacionActual().setNo2(Math.max(0, getContaminacionActual().getNo2() * 0.9));
            getContaminacionActual().setPm25(Math.max(0, getContaminacionActual().getPm25() * 0.92));
        }
    }
}

