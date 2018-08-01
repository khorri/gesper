package com.axelor.rh.web;

import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.config.db.Decision;
import com.axelor.config.db.Grade;
import com.axelor.config.db.repo.DecisionRepository;
import com.axelor.config.db.repo.EntiteRepository;
import com.axelor.config.db.repo.GradeRepository;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rh.db.Situation;
import com.axelor.rh.db.repo.SituationRepository;
import com.axelor.rh.service.DecisionService;
import com.axelor.rh.service.SituationService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by HB on 10/07/2018.
 */
public class SituationController {
    @Inject
    private EntiteRepository entiteRep;
    @Inject
    private DecisionRepository decisionRepo;
    @Inject
    private MetaFileRepository fileRep;
    @Inject
    private SituationRepository situationRepo;
    @Inject
    private SituationService situationService;
    @Inject
    private GradeRepository gradeRepository;
    @Inject
    private DecisionService decisionService;
    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Transactional
    public void verifie(ActionRequest request, ActionResponse response) throws AxelorException {
        User user = AuthUtils.getUser();
        Context context = request.getContext();
        Situation situation = request.getContext().asType(Situation.class);
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

        String language = "fr";

        String name = situationService.getFileName(decision);

        String fileLink = situationService.getReportLink(decision, name, language, ReportSettings.FORMAT_DOC);
        logger.debug("Printing " + name);
        response.setView(ActionView
                .define(name)
                .add("html", fileLink).map());


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

        if (d != null) {
            int count = situationService.decsionUsedInOtherSituation(d);
            if (count == 0) {
                response.setError("Une décision avec le même N° existe déjà.");
                return;
            }
        }

        Situation situation = situationRepo.find((Long) context.get("id"));

        String errorMessage = decisionService.validerDecisionValidation(context);
        if (errorMessage != null) {
            response.setError(errorMessage);
            return;
        }

        if (decisionRepo.all().filter("self.decisionCode = ?1", context.get("decisionCode")).fetchOne() != null) {
            response.setError("Une décision avec le même N° exsite déjà.");
            return;
        }
        if (situation.getDecision() != null) {
            //update current decsion with new values
            Decision decision = decisionService.updateDecision(context, situation.getDecision(), DecisionRepository.STATUS_VALIDATED);

            decision.setValidatedBy(user);
            decision.setValidatedOn(new LocalDate());
            decision.setStatus(DecisionRepository.STATUS_VALIDATED);
            decisionRepo.save(decision);

            setPreviousStutationNotActive(situation.getId(), situation.getEmployee().getId());
            situation.setActive(true);
            situation.setDecisionStatus(DecisionRepository.STATUS_VALIDATED);
            situationRepo.save(situation);
        }
        response.setReload(true);
    }

    private void setPreviousStutationNotActive(Long situationId, Long employeeId) {
        List<Situation> situations = situationRepo.all().filter("self.employee.id = ?1 AND self.id != ?2 AND self.active = true", employeeId, situationId).fetch();
        for (Situation situation : situations) {
            situation.setActive(false);
            situationRepo.save(situation);
        }
    }

    //
    private boolean decisionIsUsedInAffecation(Context context) {
        Decision decision = decisionRepo.all().filter("self.decisionCode = ?1", context.get("decisionCode")).fetchOne();
        if (decision == null)
            return false;
        int count = situationService.decsionUsedInOtherSituation(decision);
        return count > 0;
    }

    @Transactional
    public void refuser(ActionRequest request, ActionResponse response) {
        User user = AuthUtils.getUser();
        Context context = request.getContext();
        Situation situation = situationRepo.find((Long) context.get("id"));
        String errorMessage = decisionService.refuserDecisionValidation(context);
        if (errorMessage != null) {
            response.setError(errorMessage);
            return;
        }

        if (situation.getDecision() != null) {
            //update current decsion with new values
            Decision decision = decisionService.updateDecision(context, situation.getDecision(), DecisionRepository.STATUS_REJECTED);
            decision.setRejectedBy(user);
            decision.setRejectedOn(new LocalDate());
            decision.setStatus(DecisionRepository.STATUS_REJECTED);
            situation.setDecisionStatus(DecisionRepository.STATUS_REJECTED);
            situationRepo.save(situation);

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
        }
    }

    @Transactional
    public void getAudit(ActionRequest request, ActionResponse response) {
        Context context = request.getContext();
        Decision decision = (Decision) context.get("decision");
        if (decision == null)
            return;
        if (decision.getVerifiedBy() != null)
            response.setValue("verifiedBy", decision.getVerifiedBy().getFullName());
        if (decision.getValidatedBy() != null)
            response.setValue("validatedBy", decision.getValidatedBy().getFullName());
        if (decision.getRejectedBy() != null)
            response.setValue("rejectedBy", decision.getRejectedBy().getFullName());
        response.setValue("verifiedOn", decision.getVerifiedOn());
        response.setValue("validatedOn", decision.getValidatedOn());
        response.setValue("rejectedOn", decision.getRejectedOn());
        response.setValue("emitteur", decision.getEmitteur());
    }

    public void getDummies(ActionRequest request, ActionResponse response) {
        Situation situation = request.getContext().asType(Situation.class);
        if (situation.getGrade() != null) {
            Grade grade = gradeRepository.find(situation.getGrade().getGrade().getId());
            if (grade.getCadre() != null)
                response.setValue("cadre", grade.getCadre().getName());
            if (grade.getFiliere() != null)
                response.setValue("filiere", grade.getFiliere().getName());
            response.setValue("grades", grade);
            response.setValue("echelle", grade.getEchelle());
            response.setValue("gradeId", grade.getId());
        }
        getAudit(request, response);
        getLastDecision(request, response);
    }

    private String refuserDecisionValidation(Decision decision) {
        String errorMessage = "Les champ suivant sont requis pour le refus: ";
        boolean hasError = false;
        if (decision.getMotifRejet() == null) {
            if (hasError)
                errorMessage += ", ";
            hasError = true;
            errorMessage += "Motif de rejet";
        }
        if (decision.getEmitteur() == null) {
            if (hasError)
                errorMessage += ", ";
            hasError = true;
            errorMessage += "Emitteur";
        }
        if (hasError) {
            errorMessage += ".";
            return errorMessage;
        } else
            return null;

    }

    public void printDecision(ActionRequest request, ActionResponse response) throws AxelorException {
        Situation situation = request.getContext().asType(Situation.class);
        if (situation.getDecision() != null) {
            Decision decision = decisionRepo.find(situation.getDecision().getId());
        }
    }

    public void changedGrade(ActionRequest request, ActionResponse response) {
        Context context = request.getContext();
        Map miniGrade = (HashMap) context.get("grades");
        if (miniGrade != null && miniGrade.get("id") != null) {
            Grade grade = gradeRepository.find(Long.valueOf((Integer) miniGrade.get("id")));
            if (grade.getCadre() != null)
                response.setValue("cadre", grade.getCadre().getName());
            if (grade.getFiliere() != null)
                response.setValue("filiere", grade.getFiliere().getName());
            response.setValue("echelle", grade.getEchelle());
            response.setValue("gradeId", grade.getId());
        }
    }

    public void defaultValues(ActionRequest request, ActionResponse response) {
        Context context = request.getContext();
        Situation previousSituation = situationRepo.all().filter("self.employee.id = ?1 AND self.active =true", context.get("_id")).fetchOne();
        if (previousSituation != null) {
            response.setValue("situationDate", new LocalDate());
            response.setValue("echelonDate", previousSituation.getEchelonDate());
            response.setValue("gradeDate", previousSituation.getGradeDate());
            response.setValue("status", previousSituation.getStatus());
        }
    }
}
