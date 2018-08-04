package com.axelor.rh.web;

import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.config.db.Decision;
import com.axelor.config.db.repo.DecisionRepository;
import com.axelor.config.db.repo.EntiteRepository;
import com.axelor.exception.AxelorException;
import com.axelor.rh.db.Depart;
import com.axelor.rh.db.Employe;
import com.axelor.rh.db.Med;
import com.axelor.rh.db.repo.DepartRepository;
import com.axelor.rh.db.repo.EmployeRepository;
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
public class DepartController {
    @Inject
    private EntiteRepository entiteRep;
    @Inject
    private DecisionRepository decisionRepo;
    @Inject
    private DepartRepository departRepository;
    @Inject
    private DecisionService decisionService;
    @Inject
    private EmployeRepository employeRepository;
    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Transactional
    public void verifie(ActionRequest request, ActionResponse response) throws AxelorException {
        Employe employe = request.getContext().asType(Employe.class);
        Depart originDepart = departRepository.find(employe.getDepart().getId());
        Depart depart = employe.getDepart();
        User user = AuthUtils.getUser();

        originDepart.setStatus(DecisionRepository.STATUS_VERIFIED);
        originDepart.setDateDepart(depart.getDateDepart());
        originDepart.setType(depart.getType());
        originDepart.setMotif(depart.getMotif());
        originDepart.setObservation(depart.getObservation());

        Decision decision = new Decision();
        decision.setStatus(DecisionRepository.STATUS_VERIFIED);
        decision.setEmitteur(entiteRep.all().filter("self.shortName = ?1", "SAF").fetchOne());
        decision.setVerifiedBy(user);
        decision.setVerifiedOn(new LocalDate());
        originDepart.setDecision(decision);
        response.setValue("depart", originDepart);
        response.setValue("decisionStatus", DecisionRepository.STATUS_VERIFIED);
        response.setValue("decisionCode", decision.getDecisionCode());
        response.setValue("decisionDate", decision.getDecisionDate());
        response.setValue("entreprise", decision.getEntreprise());
        response.setValue("emitteur", decision.getEmitteur());
        response.setReload(true);
//        decisionService.printMedDecision(decision, med.getId());
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
        Employe employe = employeRepository.find((Long) request.getContext().get("id"));
        Depart depart = departRepository.find(employe.getDepart().getId());

        String errorMessage = decisionService.validerDecisionValidation(context);
        if (errorMessage != null) {
            response.setError(errorMessage);
            return;
        }

        if (decisionRepo.all().filter("self.decisionCode = ?1", context.get("decisionCode")).fetchOne() != null) {
            response.setError("Une décision avec le même N° exsite déjà.");
            return;
        }
        if (depart.getDecision() != null) {
            //update current decsion with new values
            Decision decision = decisionService.updateDecision(context, depart.getDecision(), DecisionRepository.STATUS_VALIDATED);
            decision.setValidatedBy(user);
            decision.setValidatedOn(new LocalDate());
            decision.setStatus(DecisionRepository.STATUS_VALIDATED);
            decisionRepo.save(decision);

            depart.setStatus(DecisionRepository.STATUS_VALIDATED);
            departRepository.save(depart);
            //Employee()
            employe.setActivated(false);
            employe.setRadiationDate(depart.getDateDepart());
            employeRepository.save(employe);

        }
        response.setReload(true);
    }


    @Transactional
    public void refuser(ActionRequest request, ActionResponse response) {
        User user = AuthUtils.getUser();
        Context context = request.getContext();
        Employe employe = employeRepository.find((Long) context.get("id"));
        Depart depart = departRepository.find(employe.getDepart().getId());
        String errorMessage = decisionService.refuserDecisionValidation(context);
        if (errorMessage != null) {
            response.setError(errorMessage);
            return;
        }

        if (depart.getDecision() != null) {
            //update current decsion with new values
            Decision decision = decisionService.updateDecision(context, depart.getDecision(), DecisionRepository.STATUS_REJECTED);
            decision.setRejectedBy(user);
            decision.setRejectedOn(new LocalDate());
            decision.setStatus(DecisionRepository.STATUS_REJECTED);
            employe.setDepart(null);
            employeRepository.save(employe);
            departRepository.remove(depart);

        }

        response.setReload(true);
    }

    @Transactional
    public void getLastDecision(ActionRequest request, ActionResponse response) {
//        Context context = request.getContext();
        Employe employe = request.getContext().asType(Employe.class);
        if (employe.getDepart() == null)
            return;
        Depart depart = departRepository.find(employe.getDepart().getId());
        if (depart.getDecision() == null)
            return;
        response.setValue("decisionCode", depart.getDecision().getDecisionCode());
        response.setValue("decisionDate", depart.getDecision().getDecisionDate());
        response.setValue("entreprise", depart.getDecision().getEntreprise());
        response.setValue("emitteur", depart.getDecision().getEmitteur());
        response.setValue("attachement", depart.getDecision().getAttachement());
        response.setValue("motifRejet", depart.getDecision().getMotifRejet());
    }

    @Transactional
    public void getAudit(ActionRequest request, ActionResponse response) {
        Context context = request.getContext();
        Employe employe = request.getContext().asType(Employe.class);
        if (employe.getDepart() == null)
            return;
        Depart depart = departRepository.find(employe.getDepart().getId());
        if (depart.getDecision() == null)
            return;
        if (depart.getDecision().getVerifiedBy() != null)
            response.setValue("verifiedBy", depart.getDecision().getVerifiedBy().getFullName());
        if (depart.getDecision().getValidatedBy() != null)
            response.setValue("validatedBy", depart.getDecision().getValidatedBy().getFullName());
        if (depart.getDecision().getRejectedBy() != null)
            response.setValue("rejectedBy", depart.getDecision().getRejectedBy().getFullName());
        response.setValue("verifiedOn", depart.getDecision().getVerifiedOn());
        response.setValue("validatedOn", depart.getDecision().getValidatedOn());
        response.setValue("rejectedOn", depart.getDecision().getRejectedOn());
        response.setValue("emitteur", depart.getDecision().getEmitteur());

    }

    public void getDummies(ActionRequest request, ActionResponse response) {
        getLastDecision(request, response);
        getAudit(request, response);
    }


    public void printDecision(ActionRequest request, ActionResponse response) throws AxelorException {
        Med med = request.getContext().asType(Med.class);
        if (med.getDecision() != null) {
            Decision decision = decisionRepo.find(med.getDecision().getId());
        }
    }


}
