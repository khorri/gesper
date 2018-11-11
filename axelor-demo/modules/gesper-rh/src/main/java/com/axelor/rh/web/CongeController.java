package com.axelor.rh.web;

import com.axelor.config.db.repo.DecisionRepository;
import com.axelor.db.Model;
import com.axelor.meta.schema.views.MenuItem;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rh.db.Employe;
import com.axelor.rh.db.TypeConge;
import com.axelor.rh.service.CongeCalculator;
import com.axelor.rh.service.CongeService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.name.Names;
import com.google.inject.persist.Transactional;
import ma.nawar.config.util.IErrorMessage;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.axelor.exception.AxelorException;

import java.lang.invoke.MethodHandles;

import com.axelor.rh.db.Conge;

/**
 * Created by HORRI on 24/07/2018.
 */
public class CongeController {

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    CongeService congeService;

    @Transactional
    public void getDroitConge(ActionRequest request, ActionResponse response) {
        Employe employe = (Employe) request.getContext().get("employee");
        TypeConge typeConge = (TypeConge) request.getContext().get("typeConge");
        logger.info("Recupérer le droit de congé pour l'agent: " + employe.getName());
        CongeCalculator congeCalculator = Beans.get(CongeCalculator.class, Names.named(typeConge.getAnnotation()));
        if (congeCalculator == null) {
            response.setError("Le type de congé sélectionne n'est pas pris en compte");
            return;
        }
        response.setValue("droitConge", congeCalculator.getSolde(employe, typeConge));
    }

    @Transactional
    public void computeDuration(ActionRequest request, ActionResponse response) {
        logger.info("Calculate the leave request duration...");
        LocalDate fromDate = (LocalDate) request.getContext().get("dateDebut");
        LocalDate toDate = (LocalDate) request.getContext().get("dateFin");
        Integer droitConge = (Integer) request.getContext().get("droitConge");
        if (toDate.isBefore(fromDate)) {
            response.setError(I18n.get(IErrorMessage.CONGE_INVALID_DATE_1));
            response.setColor("dateFin", "#FF0000");
        }
        int duration = congeService.getDuration(fromDate, toDate);
        if (duration > droitConge) {
            response.setError(I18n.get(IErrorMessage.CONGE_INVALID_DATE_2));
            response.setValue("duree", 0);
        }
        response.setValue("duree", duration);
    }

    @Transactional
    public void getlistDemandeDuCongeequipe(ActionRequest request, ActionResponse response) throws AxelorException {

        String leaveListIdStr = Joiner.on(",").join(congeService.getlistDemandeDuCongeEquipeService());

        response.setView(ActionView.define(I18n.get("Demandes de l'equipes"))
                .model(Conge.class.getName())
                .add("grid", "conge-equipe-grid")
                .add("form", "conge-form")
                .domain("self.id in (" + leaveListIdStr + ")")
                .map());


    }

    @Transactional
    public void valider(ActionRequest request, ActionResponse response) throws AxelorException {
        Employe employe= congeService.getEmployeByCodeCurrentUser();
        Conge conge =  request.getContext().asType(Conge.class);
        if(!congeService.valider(employe,conge).equals(null)){
            response.setReload(true);
            response.setValue("isValidate",true);
        }
    }
    public void employeIsResponsable(ActionRequest request, ActionResponse response) throws AxelorException {
        Boolean responsible = congeService.employeIsResponsable();
        Boolean isDrh=congeService.employeIsDRH();
        if(!responsible)
        response.setValue("employee",congeService.getEmployeByCodeCurrentUser());
        response.setValue("responsible", responsible);
        response.setValue("isDrh", isDrh);
        logger.debug("ff");
    }
    public void showOtherMenuIfEmployeIsResponsable(ActionRequest request, ActionResponse response) throws AxelorException {
        Boolean responsible = congeService.employeIsResponsable();
        if(responsible){
            response.setView(null);
        }

    }
}
