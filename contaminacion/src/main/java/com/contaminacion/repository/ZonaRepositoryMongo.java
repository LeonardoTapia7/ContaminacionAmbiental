package com.contaminacion.repository;

import com.contaminacion.model.*;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.*;

@Repository
@Primary
public class ZonaRepositoryMongo implements ZonaRepository {

    private final ZonaMongoSpringRepository mongoRepo;

    public ZonaRepositoryMongo(ZonaMongoSpringRepository mongoRepo) {
        this.mongoRepo = mongoRepo;
    }

    @PostConstruct
    public void initIfEmpty() {
        if (mongoRepo.count() > 0) return;
        // Initialize with same sample data as before
        ZonaUrbana norte = new ZonaIndustrial("NORTE", "Zona Norte", 5);
        ZonaUrbana centro = new ZonaResidencial("CENTRO", "Zona Centro", false);
        ZonaUrbana sur = new ZonaIndustrial("SUR", "Zona Sur", 3);
        ZonaUrbana valles = new ZonaResidencial("VALLES", "Zona Valles", false);
        ZonaUrbana norocc = new ZonaResidencial("NOROCC", "Zona Noroccidente", false);

        List<ZonaUrbana> iniciales = Arrays.asList(norte, centro, sur, valles, norocc);
        Random rnd = new Random(123);
        for (ZonaUrbana z : iniciales) {
            for (int i = 0; i < 30; i++) {
                LocalDate fecha = LocalDate.now().minusDays(i);
                double pm25 = 8 + rnd.nextDouble() * 30;
                double no2 = 10 + rnd.nextDouble() * 40;
                double so2 = 5 + rnd.nextDouble() * 30;
                double o3 = 20 + rnd.nextDouble() * 120;
                double co2 = 2 + rnd.nextDouble() * 5;
                Contaminacion c = new Contaminacion(co2, so2, no2, pm25, o3, fecha);
                z.agregarRegistroHistorico(c);
                if (i == 0) z.setContaminacionActual(c);
            }
            z.setClimaActual(new Clima(15 + rnd.nextDouble() * 10, 1 + rnd.nextDouble() * 5, 40 + rnd.nextDouble() * 40));
            mongoRepo.save(z);
        }
    }

    @Override
    public List<ZonaUrbana> findAll() {
        return mongoRepo.findAll();
    }

    @Override
    public ZonaUrbana findById(String id) {
        return mongoRepo.findById(id).orElse(null);
    }

    @Override
    public void save(ZonaUrbana zona) {
        if (zona == null) return;
        mongoRepo.save(zona);
    }
}

