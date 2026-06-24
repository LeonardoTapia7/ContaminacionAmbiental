package com.contaminacion.service;

import com.contaminacion.model.ZonaUrbana;
import java.util.List;

public interface ReporteService {
    String generarCsvResumen(List<ZonaUrbana> zonas);
}

