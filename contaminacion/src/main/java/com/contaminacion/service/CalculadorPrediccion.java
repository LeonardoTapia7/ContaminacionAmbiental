package com.contaminacion.service;

import com.contaminacion.model.Clima;
import com.contaminacion.model.Contaminacion;
import com.contaminacion.model.ZonaUrbana;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CalculadorPrediccion implements PrediccionService {

    @Override
    public Contaminacion predecir24h(ZonaUrbana zona) {
        if (zona == null) return null;
        Contaminacion current = zona.getContaminacionActual();
        Clima clima = zona.getClimaActual();
        if (current == null || clima == null) return null;

        // coefficients from instrucciones.md
        double b1 = 0.6; // tendencia historica
        double b2 = 0.1; // temperatura
        double b3 = 0.05; // humedad
        double b4 = -0.25; // viento
        double b5 = zona instanceof com.contaminacion.model.ZonaIndustrial ? 0.5 : 0.2; // emisiones

        // For each pollutant apply a simple linear formula: b0=0 for simplicity
        double co2 = aplicarFormula(current.getCo2(), clima, b1, b2, b3, b4, b5);
        double so2 = aplicarFormula(current.getSo2(), clima, b1, b2, b3, b4, b5);
        double no2 = aplicarFormula(current.getNo2(), clima, b1, b2, b3, b4, b5);
        double pm25 = aplicarFormula(current.getPm25(), clima, b1, b2, b3, b4, b5);
        double o3 = aplicarFormula(current.getO3(), clima, b1, b2, b3, b4, b5);

        Contaminacion prediction = new Contaminacion(co2, so2, no2, pm25, o3, null);
        return prediction;
    }

    private double aplicarFormula(double anterior, Clima clima, double b1, double b2, double b3, double b4, double b5) {
        double temp = clima.getTemperatura();
        double hum = clima.getHumedad();
        double viento = clima.getVelocidadViento();
        double emisiones = 1.0; // baseline
        double val = b1 * anterior + b2 * temp + b3 * hum + b4 * viento + b5 * emisiones;
        // ensure non-negative
        return Math.max(0.0, val);
    }
}

