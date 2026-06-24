package com.contaminacion.model;

import java.time.LocalDate;

public class Contaminacion {
    private double co2;
    private double so2;
    private double no2;
    private double pm25;
    private double o3;
    private LocalDate fecha;

    public Contaminacion() {
        this.fecha = LocalDate.now();
    }

    public Contaminacion(double co2, double so2, double no2, double pm25, double o3, LocalDate fecha) {
        setCo2(co2);
        setSo2(so2);
        setNo2(no2);
        setPm25(pm25);
        setO3(o3);
        this.fecha = fecha == null ? LocalDate.now() : fecha;
    }

    private void validarNoNegativo(double v, String campo) {
        if (v < 0) {
            throw new IllegalArgumentException(campo + " no puede ser negativo");
        }
    }

    public double getCo2() {
        return co2;
    }

    public void setCo2(double co2) {
        validarNoNegativo(co2, "CO2");
        this.co2 = co2;
    }

    public double getSo2() {
        return so2;
    }

    public void setSo2(double so2) {
        validarNoNegativo(so2, "SO2");
        this.so2 = so2;
    }

    public double getNo2() {
        return no2;
    }

    public void setNo2(double no2) {
        validarNoNegativo(no2, "NO2");
        this.no2 = no2;
    }

    public double getPm25() {
        return pm25;
    }

    public void setPm25(double pm25) {
        validarNoNegativo(pm25, "PM2.5");
        this.pm25 = pm25;
    }

    public double getO3() {
        return o3;
    }

    public void setO3(double o3) {
        validarNoNegativo(o3, "O3");
        this.o3 = o3;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha == null ? LocalDate.now() : fecha;
    }
}

