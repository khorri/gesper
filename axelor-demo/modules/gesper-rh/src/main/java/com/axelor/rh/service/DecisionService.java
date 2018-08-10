package com.axelor.rh.service;

/**
 * Created by HB on 26/07/2018.
 */

import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.config.db.Decision;
import com.axelor.config.db.Entite;
import com.axelor.config.db.repo.DecisionRepository;
import com.axelor.config.db.repo.EntiteRepository;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Map;

public class DecisionService implements Serializable {
    @Inject
    private DecisionRepository decisionRep;
    @Inject
    private MetaFileRepository fileRep;
    @Inject
    private EntiteRepository entiteRep;

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Transactional

    public Decision updateDecision(Context context, Decision d, String status) {
        Decision decision = decisionRep.find(d.getId());


        Map<String, Object> dataFile = (Map) context.get("attachement");
        if (dataFile != null) {
            MetaFile file = fileRep.find(Long.valueOf((Integer) dataFile.get("id")));
            decision.setAttachement(file);
        }
        Map<String, Object> entiteData = (Map) context.get("emitteur");
        if (entiteData != null) {
            Entite entite = entiteRep.find(Long.valueOf((Integer) entiteData.get("id")));
            decision.setEmitteur(entite);
        }
        decision.setStatus(status);
        decision.setMotifRejet((String) context.get("motifRejet"));
        decision.setDecisionDate(new LocalDate(context.get("decisionDate")));
        if (!DecisionRepository.STATUS_REJECTED.equals(status))
            decision.setDecisionCode((String) context.get("decisionCode"));

        decision.setEntreprise((String) context.get("entreprise"));

        setAuditableFields(status, decision);

        return decisionRep.save(decision);
    }

    private void setAuditableFields(String status, Decision decision) {
        User user = AuthUtils.getUser();
        if (DecisionRepository.STATUS_VALIDATED.equals(status)) {
            decision.setValidatedBy(user);
            decision.setValidatedOn(new LocalDate());
        }

        if (DecisionRepository.STATUS_REJECTED.equals(status)) {
            decision.setRejectedBy(user);
            decision.setRejectedOn(new LocalDate());
        }
    }

    public String refuserDecisionValidation(Context context) {
        String errorMessage = "Les champ suivant sont requis pour le refus: ";
        boolean hasError = false;
        if (context.get("motifRejet") == null) {
            if (hasError)
                errorMessage += ", ";
            hasError = true;
            errorMessage += "Motif de rejet";
        }
        if (context.get("emitteur") == null) {
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

    public String validerDecisionValidation(Context context) {
        String errorMessage = "Les champs suivants sont requis pour la validation: ";
        boolean hasError = false;
        if (context.get("decisionCode") == null) {
            if (hasError)
                errorMessage += ", ";
            hasError = true;
            errorMessage += "Code decision";
        }
        if (context.get("emitteur") == null) {
            if (hasError)
                errorMessage += ", ";
            hasError = true;
            errorMessage += "Emitteur";
        }
        if (context.get("decisionDate") == null) {
            if (hasError)
                errorMessage += ", ";
            hasError = true;
            errorMessage += "Date decision";
        }
        if (hasError) {
            errorMessage += ".";
            return errorMessage;
        } else
            return null;

    }


    public void printSituationDecision(Decision decision, Long id) {
    }

    public void printMedDecision(Decision decision, Long id) {

    }

    public void printDecision(Decision decision, Long id) {

    }

    public void printSanctionDecision(Decision decision, Long id) {
    }

    public boolean isValid(Context context, ActionResponse response) {
        String errorMessage = validerDecisionValidation(context);
        if (errorMessage != null) {
            response.setError(errorMessage);
            return false;
        }
        return true;
    }
}
