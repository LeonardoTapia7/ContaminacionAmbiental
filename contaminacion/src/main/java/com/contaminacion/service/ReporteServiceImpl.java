package com.contaminacion.service;

import com.contaminacion.model.Contaminacion;
import com.contaminacion.model.ZonaUrbana;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReporteServiceImpl implements ReporteService {

    @Override
    public String generarCsvResumen(List<ZonaUrbana> zonas) {
        StringBuilder sb = new StringBuilder();
        sb.append("id,nombre,fecha,co2,so2,no2,pm25,o3\n");
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE;
        for (ZonaUrbana z : zonas) {
            Contaminacion c = z.getContaminacionActual();
            if (c == null) continue;
            sb.append(z.getId()).append(',')
                .append(z.getNombre()).append(',')
                .append(c.getFecha() == null ? "" : c.getFecha().format(fmt)).append(',')
                .append(c.getCo2()).append(',')
                .append(c.getSo2()).append(',')
                .append(c.getNo2()).append(',')
                .append(c.getPm25()).append(',')
                .append(c.getO3()).append('\n');
        }
        return sb.toString();
    }
}

