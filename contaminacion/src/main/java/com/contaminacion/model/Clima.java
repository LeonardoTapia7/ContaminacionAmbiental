package com.contaminacion.model;

public class Clima {
    private double temperatura;
    private double velocidadViento;
    private double humedad;

    public Clima() {
    }

    public Clima(double temperatura, double velocidadViento, double humedad) {
        setTemperatura(temperatura);
        setVelocidadViento(velocidadViento);
        setHumedad(humedad);
    }

    public double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(double temperatura) {
        // Temperatura razonable entre -50 y 60 C
        if (temperatura < -50 || temperatura > 60) {
            throw new IllegalArgumentException("Temperatura fuera de rango");
        }
        this.temperatura = temperatura;
    }

    public double getVelocidadViento() {
        return velocidadViento;
    }

    public void setVelocidadViento(double velocidadViento) {
        // No negativo
        if (velocidadViento < 0) {
            throw new IllegalArgumentException("Velocidad del viento no puede ser negativa");
        }
        this.velocidadViento = velocidadViento;
    }

    public double getHumedad() {
        return humedad;
    }

    public void setHumedad(double humedad) {
        if (humedad < 0 || humedad > 100) {
            throw new IllegalArgumentException("Humedad debe estar entre 0 y 100");
        }
        this.humedad = humedad;
    }
}

