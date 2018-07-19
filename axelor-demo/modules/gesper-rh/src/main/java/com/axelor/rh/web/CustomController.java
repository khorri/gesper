package com.axelor.rh.web;

import com.axelor.config.db.Exercice;
import com.axelor.config.db.repo.ExerciceRepository;
import com.axelor.db.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;

public class CustomController {

    private final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    @Inject
    private ExerciceRepository exerciceRepository;

    public Exercice getCurrentExercice(){
        Exercice exercice= Query.of(Exercice.class).order("-name").fetchOne();
        return exercice;
    }
}