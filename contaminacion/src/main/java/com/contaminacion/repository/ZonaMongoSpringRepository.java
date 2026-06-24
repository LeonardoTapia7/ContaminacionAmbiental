package com.contaminacion.repository;

import com.contaminacion.model.ZonaUrbana;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ZonaMongoSpringRepository extends MongoRepository<ZonaUrbana, String> {

}

