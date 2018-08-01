package com.axelor.rh.web;

import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.config.db.Decision;
import com.axelor.config.db.repo.DecisionRepository;
import com.axelor.config.db.repo.EntiteRepository;
import com.axelor.exception.AxelorException;
import com.axelor.rh.db.Med;
import com.axelor.rh.db.repo.MedRepository;
import com.axelor.rh.service.DecisionService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/**
 * Created by HB on 10/07/2018.
 */
public class MedController {
    @Inject
    private EntiteRepository entiteRep;
    @Inject
    private DecisionRepository decisionRepo;
    @Inject
    private MedRepository medRepository;
    @Inject
    private DecisionService decisionService;
    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Transactional
    public void verifie(ActionRequest request, ActionResponse response) throws AxelorException {
        Med med = request.getContext().asType(Med.class);
        User user = AuthUtils.getUser();
        response.setValue("decisionStatus", 2);
        Decision decision = new Decision();
        decision.setStatus(DecisionRepository.STATUS_VERIFIED);
        decision.setEmitteur(entiteRep.all().filter("self.shortName = ?1", "SAF").fetchOne());
        decision.setVerifiedBy(user);
        decision.setVerifiedOn(new LocalDate());
        response.setValue("decision", decision);
        response.setValue("decisionCode", decision.getDecisionCode());
        response.setValue("decisionDate", decision.getDecisionDate());
        response.setValue("entreprise", decision.getEntreprise());
        response.setValue("emitteur", decision.getEmitteur());
        decisionService.printMedDecision(decision, med.getId());
    }

    //
//    /**
//     * Validate the new employe assignment.
//     *
//     * @param request
//     * @param response
//     */
    @Transactional
    public void valider(ActionRequest request, ActionResponse response) {
        User user = AuthUtils.getUser();
        Context context = request.getContext();

        Decision d = decisionRepo.all().filter("self.decisionCode = ?1", context.get("decisionCode")).fetchOne();

//        if (d != null) {
//            int count = situationService.decsionUsedInOtherSituation(d);
//            if (count == 0) {
//                response.setError("Une décision avec le même N° existe déjà.");
//                return;
//            }
//        }

        Med med = medRepository.find((Long) context.get("id"));

        String errorMessage = decisionService.validerDecisionValidation(context);
        if (errorMessage != null) {
            response.setError(errorMessage);
            return;
        }

        if (decisionRepo.all().filter("self.decisionCode = ?1", context.get("decisionCode")).fetchOne() != null) {
            response.setError("Une décision avec le même N° exsite déjà.");
            return;
        }
        if (med.getDecision() != null) {
            //update current decsion with new values
            Decision decision = decisionService.updateDecision(context, med.getDecision(), DecisionRepository.STATUS_VALIDATED);

            decision.setValidatedBy(user);
            decision.setValidatedOn(new LocalDate());
            decision.setStatus(DecisionRepository.STATUS_VALIDATED);
            decisionRepo.save(decision);

            medRepository.save(med);
        }
        response.setReload(true);
    }


    @Transactional
    public void refuser(ActionRequest request, ActionResponse response) {
        User user = AuthUtils.getUser();
        Context context = request.getContext();
        Med med = medRepository.find((Long) context.get("id"));
        String errorMessage = decisionService.refuserDecisionValidation(context);
        if (errorMessage != null) {
            response.setError(errorMessage);
            return;
        }

        if (med.getDecision() != null) {
            //update current decsion with new values
            Decision decision = decisionService.updateDecision(context, med.getDecision(), DecisionRepository.STATUS_REJECTED);
            decision.setRejectedBy(user);
            decision.setRejectedOn(new LocalDate());
            decision.setStatus(DecisionRepository.STATUS_REJECTED);
            medRepository.save(med);

        }
        response.setReload(true);
    }

    @Transactional
    public void getLastDecision(ActionRequest request, ActionResponse response) {
        Context context = request.getContext();
        Decision decision = (Decision) context.get("decision");
        if (decision != null) {
            response.setValue("decisionCode", decision.getDecisionCode());
            response.setValue("decisionDate", decision.getDecisionDate());
            response.setValue("entreprise", decision.getEntreprise());
            response.setValue("emitteur", decision.getEmitteur());
            response.setValue("attachement", decision.getAttachement());
        }
    }

    public void getDummies(ActionRequest request, ActionResponse response) {
        getLastDecision(request, response);
    }


    public void printDecision(ActionRequest request, ActionResponse response) throws AxelorException {
        Med med = request.getContext().asType(Med.class);
        if (med.getDecision() != null) {
            Decision decision = decisionRepo.find(med.getDecision().getId());
        }
    }


}
