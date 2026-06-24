package com.contaminacion.repository;

import com.contaminacion.model.ZonaUrbana;
import java.util.List;

public interface ZonaRepository {
    List<ZonaUrbana> findAll();
    ZonaUrbana findById(String id);
    void save(ZonaUrbana zona);
}

