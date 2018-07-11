package com.axelor.rh.web;

import com.axelor.config.db.Exercice;
import com.axelor.config.db.repo.ExerciceRepository;
import com.axelor.db.JPA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;

public class CustomController {

    @Inject
    private ExerciceRepository exerciceRepository;

    private final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    public Exercice getCurrentExercice(){
        Exercice exercice=exerciceRepository.findMax();
        return exercice;
    }
}