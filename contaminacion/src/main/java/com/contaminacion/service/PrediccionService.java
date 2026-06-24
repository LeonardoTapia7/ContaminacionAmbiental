package com.contaminacion.service;

import com.contaminacion.model.Contaminacion;
import com.contaminacion.model.ZonaUrbana;

public interface PrediccionService {
    Contaminacion predecir24h(ZonaUrbana zona);
}

