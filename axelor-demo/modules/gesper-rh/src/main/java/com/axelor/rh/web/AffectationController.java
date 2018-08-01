package com.axelor.rh.web;

import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.config.db.Decision;
import com.axelor.config.db.repo.DecisionRepository;
import com.axelor.config.db.repo.EntiteRepository;
import com.axelor.exception.AxelorException;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rh.db.Affectation;
import com.axelor.rh.db.repo.AffectationRepository;
import com.axelor.rh.service.AffectationService;
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
import java.util.Iterator;

/**
 * Created by HB on 10/07/2018.
 */
public class AffectationController {
    @Inject
    private EntiteRepository entiteRep;
    @Inject
    private DecisionRepository decisionRep;
    @Inject
    private AffectationRepository affectationRepository;
    @Inject
    private AffectationService affectationService;
    @Inject
    private DecisionService decisionService;

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Transactional
    public void verifie(ActionRequest request, ActionResponse response) throws AxelorException {
        User user = AuthUtils.getUser();
        Affectation affectation = request.getContext().asType(Affectation.class);
        response.setValue("status", 2);
        Decision decision = new Decision();
        decision.setStatus(DecisionRepository.STATUS_VERIFIED);
        decision.setEmitteur(entiteRep.all().filter("self.shortName = ?1", "SAF").fetchOne());
        decision.setVerifiedBy(user);
        decision.setVerifiedOn(new LocalDate());
        affectation.addDecision(decision);
        response.setValue("decision", affectation.getDecision());
        response.setValue("decisionCode", decision.getDecisionCode());
        response.setValue("decisionDate", decision.getDecisionDate());
        response.setValue("entreprise", decision.getEntreprise());
        response.setValue("emitteur", decision.getEmitteur());
        String language = "fr";

        String name = affectationService.getFileName(decision);

        String fileLink = affectationService.getReportLink(decision, name, language, ReportSettings.FORMAT_DOC);
        logger.debug("Printing " + name);
        response.setView(ActionView
                .define(name)
                .add("html", fileLink).map());
    }

    /**
     * Validate the new employe assignment.
     *
     * @param request
     * @param response
     */
    @Transactional
    public void valider(ActionRequest request, ActionResponse response) {
        User user = AuthUtils.getUser();
        Context context = request.getContext();
        Affectation affectation = affectationRepository.find((Long) context.get("id"));
        affectation.setTypeAffectation((String) context.get("typeAffectation"));

        boolean useExising = false;
        if (context.containsKey("useExisting"))
            useExising = (boolean) context.get("useExisting");

        String errorMessage = decisionService.validerDecisionValidation(context);
        if (errorMessage != null) {
            response.setError(errorMessage);
            return;
        }

        Decision d = decisionRep.all().filter("self.decisionCode = ?1", context.get("decisionCode")).fetchOne();
        if (d != null) {
            boolean used = decisionIsUsedInAffecation(context);
            if (!used) {
                response.setError("Une décision avec le même N° existe déjà.");
                return;
            } else if (used && useExising != true) {
                response.setValue("decisionExists", true);
                return;
            }
        }


        if (!affectation.getDecision().isEmpty()) {
            //update current decsion with new values
            Decision lastDecision = getLastPendingDecision(affectation);
            Decision decision;
            if (useExising) {
                decision = decisionRep.all().filter("self.decisionCode = ?1", context.get("decisionCode")).fetchOne();
                affectation.removeDecision(lastDecision);
                affectation.addDecision(decision);
            } else {
                lastDecision.setValidatedBy(user);
                lastDecision.setValidatedOn(new LocalDate());
                decision = decisionService.updateDecision(context, lastDecision, DecisionRepository.STATUS_VALIDATED);
            }
            if (AffectationRepository.TYPE_PRINCIPAL.equals(affectation.getTypeAffectation())) {
                deactivatePrincipalAffectation(affectation, decision);
            }

            affectation.setStatus(DecisionRepository.STATUS_VALIDATED);
            affectationRepository.save(affectation);
            if (useExising && lastDecision != null)
                decisionRep.remove(lastDecision);
        }
        response.setReload(true);
    }

    private boolean decisionIsUsedInAffecation(Context context) {
        Decision decision = decisionRep.all().filter("self.decisionCode = ?1", context.get("decisionCode")).fetchOne();
        if (decision == null)
            return false;
        int count = affectationService.decsionUsedInOtherAffectation(decision);
        return count > 0;
    }


    private Decision getLastPendingDecision(Affectation affectation) {
        Decision lastDecision = null;
        Iterator<Decision> iter = affectation.getDecision().iterator();
        Decision decision = null;

        while (iter.hasNext()) {
            decision = iter.next();
            if (decision.getStatus().equals(DecisionRepository.STATUS_VERIFIED))
                lastDecision = decision;
        }

        return lastDecision;
    }


    private void deactivatePrincipalAffectation(Affectation affectation, Decision decision) {
        Affectation pricipale = affectationRepository.all().filter("self.typeAffectation = ?1 AND self.employee.id = ?2 AND self.status = ?3", AffectationRepository.TYPE_PRINCIPAL, affectation.getEmployee().getId(), DecisionRepository.STATUS_VALIDATED).fetchOne();
        if (pricipale != null) {
            pricipale.addDecision(decision);
            pricipale.setTypeAffectation(AffectationRepository.TYPE_SECONDARY);
            affectationRepository.save(pricipale);
        }
    }

    @Transactional
    public void refuser(ActionRequest request, ActionResponse response) {
        User user = AuthUtils.getUser();
        Context context = request.getContext();
        Affectation affectation = affectationRepository.find((Long) context.get("id"));

        String errorMessage = decisionService.refuserDecisionValidation(context);
        if (errorMessage != null) {
            response.setError(errorMessage);
            return;
        }

        if (!affectation.getDecision().isEmpty()) {
            //update current decsion with new values
            Decision lastDecision = getLastPendingDecision(affectation);
            lastDecision.setRejectedBy(user);
            lastDecision.setRejectedOn(new LocalDate());

            decisionService.updateDecision(context, lastDecision, DecisionRepository.STATUS_REJECTED);
            affectation.setStatus(DecisionRepository.STATUS_REJECTED);
            affectationRepository.save(affectation);

        }
        response.setReload(true);
    }

    @Transactional
    public void getLastDecision(ActionRequest request, ActionResponse response) {
        Context context = request.getContext();
        Affectation affectation = affectationRepository.find((Long) context.get("id"));
        if (affectation != null) {
            if (DecisionRepository.STATUS_VERIFIED.equals(affectation.getStatus())) {
                Decision decision = getLastPendingDecision(affectation);
                response.setValue("decisionCode", decision.getDecisionCode());
                response.setValue("decisionDate", decision.getDecisionDate());
                response.setValue("entreprise", decision.getEntreprise());
                response.setValue("emitteur", decision.getEmitteur());
                response.setValue("attachement", decision.getAttachement());
            }

        }
    }

    @Transactional
    public void getAudit(ActionRequest request, ActionResponse response) {
        Context context = request.getContext();
        Affectation affectation = affectationRepository.find((Long) context.get("id"));
        if (affectation != null) {

            Iterator<Decision> iter = affectation.getDecision().iterator();
            Decision decision = null;
            Decision lastDecision = null;
            if (iter.hasNext())
                lastDecision = iter.next();

            while (iter.hasNext()) {
                decision = iter.next();
                if (lastDecision.getUpdatedOn() != null && decision.getUpdatedOn().isAfter(lastDecision.getUpdatedOn()))
                    lastDecision = decision;
            }
            if (lastDecision == null)
                return;
            if (lastDecision.getVerifiedBy() != null)
                response.setValue("verifiedBy", lastDecision.getVerifiedBy().getFullName());
            if (lastDecision.getValidatedBy() != null)
                response.setValue("validatedBy", lastDecision.getValidatedBy().getFullName());
            if (lastDecision.getRejectedBy() != null)
                response.setValue("rejectedBy", lastDecision.getRejectedBy().getFullName());
            response.setValue("verifiedOn", lastDecision.getVerifiedOn());
            response.setValue("validatedOn", lastDecision.getValidatedOn());
            response.setValue("rejectedOn", lastDecision.getRejectedOn());
            response.setValue("emitteur", lastDecision.getEmitteur());


        }
    }

    public void getDummies(ActionRequest request, ActionResponse response) {
        getAudit(request, response);
        getLastDecision(request, response);
    }

    @Transactional
    public void abroge(ActionRequest request, ActionResponse response) {
        Affectation affectation = request.getContext().asType(Affectation.class);
        if (AffectationRepository.TYPE_PRINCIPAL.equals(affectation.getTypeAffectation())) {
            response.setError("L'affectation pricipale ne peut pas être abrogé.");
        } else {
            response.setValue("status", DecisionRepository.STATUS_VERIFIED);
            Decision decision = new Decision();
            decision.setStatus(DecisionRepository.STATUS_VERIFIED);
            decision.setEmitteur(entiteRep.all().filter("self.shortName = ?1", "SAF").fetchOne());
            affectation.addDecision(decision);
            response.setValue("typeAffectation", AffectationRepository.TYPE_CANCELED);
            response.setValue("decision", affectation.getDecision());
            response.setValue("decisionCode", decision.getDecisionCode());
            response.setValue("decisionDate", decision.getDecisionDate());
            response.setValue("entreprise", decision.getEntreprise());
            response.setValue("emitteur", decision.getEmitteur());
        }
    }


    public void printDecision(ActionRequest request, ActionResponse response) throws AxelorException {

        Decision decision = request.getContext().asType(Decision.class);
        String language = "fr";

        String name = affectationService.getFileName(decision);

        String fileLink = affectationService.getReportLink(decision, name, language, ReportSettings.FORMAT_DOC);
        logger.debug("Printing " + name);
        response.setView(ActionView
                .define(name)
                .add("html", fileLink).map());
    }

}
