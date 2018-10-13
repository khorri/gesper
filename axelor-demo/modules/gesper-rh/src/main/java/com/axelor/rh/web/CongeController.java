package com.axelor.rh.web;

import com.axelor.auth.AuthUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rh.db.Employe;
import com.axelor.rh.db.TypeConge;
import com.axelor.rh.service.CongeCalculator;
import com.axelor.rh.service.CongeService;
import com.axelor.rh.utils.SQLQueries;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
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
import java.util.ArrayList;
import java.util.List;
import java.math.BigInteger;

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
        BigInteger idEntite = congeService.getIdEntiteByMatriculeEmploye(AuthUtils.getUser().getCode());
        String leaveListIdStr = "-2";
        if (!idEntite.equals(null)) {
            List<BigInteger> EmployeListId = congeService.getListIdEmployeCongeEquipe(idEntite.intValue());
            List<Long> CongeListID = new ArrayList<>();

            if (!EmployeListId.isEmpty()) {
                for (BigInteger idEmploye : EmployeListId) {
                    CongeListID.add(congeService.getCongeByIdEmploye(idEmploye).getId());
                }
                if (!CongeListID.isEmpty()) {
                    leaveListIdStr = Joiner.on(",").join(CongeListID);
                }
            } else {

            }
        }

        response.setView(ActionView.define(I18n.get("Demandes de l'equipes"))
                .model(Conge.class.getName())
                .add("grid", "conge-grid")
                .add("form", "conge-form")
                .domain("self.id in (" + leaveListIdStr + ")")
                .map());
        logger.debug("End getlistDemandeDuCongeequipe ...");


    }


    public void employeIsResponsable(ActionRequest request, ActionResponse response) throws AxelorException {
        Boolean responsible = congeService.employeIsResponsable();
        response.setValue("responsible", responsible);
    }
}
