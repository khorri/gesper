package com.axelor.rh.web;

import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rh.db.Employe;
import com.axelor.rh.db.TypeConge;
import com.axelor.rh.service.CongeCalculator;
import com.axelor.rh.service.CongeService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.name.Names;
import com.google.inject.persist.Transactional;
import ma.nawar.config.util.IErrorMessage;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/**
 * Created by HORRI on 24/07/2018.
 */
public class CongeController {

    private final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    @Inject
    CongeService congeService;

    @Transactional
    public void getDroitConge(ActionRequest request, ActionResponse response){
        Employe employe = (Employe) request.getContext().get("employee");
        TypeConge typeConge = (TypeConge)request.getContext().get("typeConge");
        logger.info("Recupérer le droit de congé pour l'agent: "+employe.getName());
        CongeCalculator congeCalculator = Beans.get(CongeCalculator.class, Names.named(typeConge.getCode()));
        if(congeCalculator==null){
            response.setError("Le type de congé sélectionne n'est pas pris en compte");
            return;
        }
        response.setValue("droitConge",congeCalculator.getSolde(employe,typeConge));
    }

    @Transactional
    public void computeDuration(ActionRequest request, ActionResponse response){
        LocalDate fromDate = (LocalDate) request.getContext().get("dateDebut");
        LocalDate toDate = (LocalDate) request.getContext().get("dateFin");
        Integer droitConge = (Integer)request.getContext().get("droitConge");
        if(toDate.isBefore(fromDate)) {
            response.setError(I18n.get(IErrorMessage.CONGE_INVALID_DATE_1));
            response.setColor("dateFin", "#FF0000");
        }
        int duration = congeService.getDuration(fromDate,toDate);
        response.setValue("duree",duration);
    }

}
