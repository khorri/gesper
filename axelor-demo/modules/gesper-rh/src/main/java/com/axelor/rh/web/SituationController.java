package com.axelor.rh.web;

import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.config.db.Decision;
import com.axelor.config.db.Grade;
import com.axelor.config.db.repo.DecisionRepository;
import com.axelor.config.db.repo.EntiteRepository;
import com.axelor.config.db.repo.GradeRepository;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.rh.db.Situation;
import com.axelor.rh.db.repo.SituationRepository;
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
import java.util.Map;

/**
 * Created by HB on 10/07/2018.
 */
public class SituationController {
    @Inject
    private EntiteRepository entiteRep;
    @Inject
    private DecisionRepository decisionRep;
    @Inject
    private MetaFileRepository fileRep;
    @Inject
    private SituationRepository situationRepo;
    @Inject
    private SituationService situationService;
    @Inject
    private GradeRepository gradeRepository;
    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Transactional
    public void verifie(ActionRequest request, ActionResponse response) {
        User user = AuthUtils.getUser();
        Situation situation = request.getContext().asType(Situation.class);
        response.setValue("decisionStatus", 2);
        Decision decision = new Decision();
        decision.setStatus(DecisionRepository.STATUS_VERIFIED);
        decision.setEmitteur(entiteRep.all().filter("self.shortName = ?1", "SAF").fetchOne());
        decision.setVerifiedBy(user);
        decision.setVerifiedOn(new LocalDate());
        response.setValue("decision", decision);
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
        Situation situation = situationRepo.find((Long) context.get("id"));

        boolean useExising = false;
        if (context.containsKey("useExisting"))
            useExising = (boolean) context.get("useExisting");

        String errorMessage = validerDecisionValidation((Decision) context.get("decision"));
        if (errorMessage != null) {
            response.setError(errorMessage);
            return;
        }

        boolean used = false;
//        boolean used = decisionIsUsedInAffecation(context);
        if (used && useExising != true) {
            response.setValue("decisionExists", true);
            return;
        }

        if (situation.getDecision() != null) {
            //update current decsion with new values
            Decision decision = situation.getDecision();
            if (useExising) {
                decision = decisionRep.all().filter("self.decisionCode = ?1", context.get("decisionCode")).fetchOne();
            } else {
                decision.setValidatedBy(user);
                decision.setValidatedOn(new LocalDate());
                decision.setStatus(DecisionRepository.STATUS_VALIDATED);
                decisionRep.save(decision);
            }
//            if (AffectationRepository.TYPE_PRINCIPAL.equals(affectation.getTypeAffectation())) {
//                deactivatePrincipalAffectation(affectation, decision);
//            }

            situation.setDecisionStatus(DecisionRepository.STATUS_VALIDATED);
            situationRepo.save(situation);
        }
        response.setReload(true);
    }

    //
    private boolean decisionIsUsedInAffecation(Context context) {
        Decision decision = decisionRep.all().filter("self.decisionCode = ?1", context.get("decisionCode")).fetchOne();
        if (decision == null)
            return false;
        int count = situationService.decsionUsedInOtherSituation(decision);
        return count > 0;
    }

    //
//
//    private Decision getLastPendingDecision(Affectation affectation) {
//        Decision lastDecision = null;
//        Iterator<Decision> iter = affectation.getDecision().iterator();
//        Decision decision = null;
//
//        while (iter.hasNext()) {
//            decision = iter.next();
//            if (decision.getStatus().equals(DecisionRepository.STATUS_VERIFIED))
//                lastDecision = decision;
//        }
//
//        return lastDecision;
//    }
//
//
//    private void deactivatePrincipalAffectation(Affectation affectation, Decision decision) {
//        Affectation pricipale = affectationRepository.all().filter("self.typeAffectation = ?1 AND self.employee.id = ?2 AND self.status = ?3", AffectationRepository.TYPE_PRINCIPAL, affectation.getEmployee().getId(), DecisionRepository.STATUS_VALIDATED).fetchOne();
//        if (pricipale != null) {
//            pricipale.addDecision(decision);
//            pricipale.setTypeAffectation(AffectationRepository.TYPE_SECONDARY);
//            affectationRepository.save(pricipale);
//        }
//    }
//
    @Transactional
    public void refuser(ActionRequest request, ActionResponse response) {
        User user = AuthUtils.getUser();
        Context context = request.getContext();
        Situation situation = situationRepo.find((Long) context.get("id"));
        String errorMessage = refuserDecisionValidation((Decision) context.get("decision"));
        if (errorMessage != null) {
            response.setError(errorMessage);
            return;
        }

        if (situation.getDecision() != null) {
            //update current decsion with new values
            Decision decision = situation.getDecision();
            decision.setRejectedBy(user);
            decision.setRejectedOn(new LocalDate());
            decision.setStatus(DecisionRepository.STATUS_REJECTED);
            decisionRep.save(decision);
//            new AffectationController().updateDecision((Map) context, decision, DecisionRepository.STATUS_REJECTED);
            situation.setDecisionStatus(DecisionRepository.STATUS_REJECTED);
            situationRepo.save(situation);

        }
        response.setReload(true);
    }

    //
//    //    @Transactional
////    public void refuser(ActionRequest request, ActionResponse response){
////        ObjectMapper mapper = new ObjectMapper();
////        Context context = request.getContext();
////        Affectation affectation = request.getContext().asType(Affectation.class);
////        List<Decision> ds = new ArrayList<>();
////        ds.addAll(affectation.getDecision());
////        String errorMessage="Les champ suivant sont requis pour le refus: ";
////        boolean hasError=false;
////        if(ds.size()>0) {
////            Decision decision = ds.get(ds.size() - 1);
////            if(context.get("motifRejet")==null){
////                if(hasError)
////                    errorMessage+=",";
////                hasError=true;
////                errorMessage+="Motif de rejet";
////            }
////            if(hasError) {
////                errorMessage += ".";
////                response.setError(errorMessage);
////            }
////            if(!hasError) {
////                decision.setStatus("4");
////                decision.setMotifRejet((String) context.get("motifRejet"));
////                decision.setDecisionDate(new LocalDate(context.get("decisionDate")));
////                decision.setDecisionCode((String) context.get("decisionCode"));
////                decision.setEmitteur(mapper.convertValue(context.get("emitteur"),Entite.class));
////                decision.setAttachement(mapper.convertValue(context.get("attachement"),MetaFile.class));
////                decision.setEntreprise((String) context.get("entreprise"));
////                affectation.setDecision(new HashSet<>(ds));
////                response.setValue("status", 4);
////            }
////        }
////
////    }
//    @Transactional
//    public void getLastDecision(ActionRequest request, ActionResponse response) {
//        Context context = request.getContext();
//        Affectation affectation = affectationRepository.find((Long) context.get("id"));
//        if (affectation != null) {
//            if (DecisionRepository.STATUS_VERIFIED.equals(affectation.getStatus())) {
//                Decision decision = getLastPendingDecision(affectation);
//                affectationService.decsionUsedInOtherAffectation(decision);
//                response.setValue("decisionCode", decision.getDecisionCode());
//                response.setValue("decisionDate", decision.getDecisionDate());
//                response.setValue("entreprise", decision.getEntreprise());
//                response.setValue("emitteur", decision.getEmitteur());
//            }
//
//        }
//    }
//
//    @Transactional
//    public void getAudit(ActionRequest request, ActionResponse response) {
//        Context context = request.getContext();
//        Affectation affectation = affectationRepository.find((Long) context.get("id"));
//        if (affectation != null) {
//
//            Iterator<Decision> iter = affectation.getDecision().iterator();
//            Decision decision = null;
//            Decision lastDecision = null;
//            if (iter.hasNext())
//                lastDecision = iter.next();
//
//            while (iter.hasNext()) {
//                decision = iter.next();
//                if (lastDecision.getUpdatedOn() != null && decision.getUpdatedOn().isAfter(lastDecision.getUpdatedOn()))
//                    lastDecision = decision;
//            }
//            if (lastDecision == null)
//                return;
//            if (lastDecision.getVerifiedBy() != null)
//                response.setValue("verifiedBy", lastDecision.getVerifiedBy().getFullName());
//            if (lastDecision.getValidatedBy() != null)
//                response.setValue("validatedBy", lastDecision.getValidatedBy().getFullName());
//            if (lastDecision.getRejectedBy() != null)
//                response.setValue("rejectedBy", lastDecision.getRejectedBy().getFullName());
//            response.setValue("verifiedOn", lastDecision.getVerifiedOn());
//            response.setValue("validatedOn", lastDecision.getValidatedOn());
//            response.setValue("rejectedOn", lastDecision.getRejectedOn());
//            response.setValue("emitteur", lastDecision.getEmitteur());
//
//
//        }
//    }
//
//    public void getDummies(ActionRequest request, ActionResponse response) {
//        getAudit(request, response);
//        getLastDecision(request, response);
//    }
//
//    @Transactional
//    public void abroge(ActionRequest request, ActionResponse response) {
//        Affectation affectation = request.getContext().asType(Affectation.class);
//        if (AffectationRepository.TYPE_PRINCIPAL.equals(affectation.getTypeAffectation())) {
//            response.setError("L'affectation pricipale ne peut pas être abrogé.");
//        } else {
//            response.setValue("status", DecisionRepository.STATUS_VERIFIED);
//            Decision decision = new Decision();
//            decision.setStatus(DecisionRepository.STATUS_VERIFIED);
//            decision.setEmitteur(entiteRep.all().filter("self.shortName = ?1", "SAF").fetchOne());
//            affectation.addDecision(decision);
//            response.setValue("typeAffectation", AffectationRepository.TYPE_CANCELED);
//            response.setValue("decision", affectation.getDecision());
//            response.setValue("decisionCode", decision.getDecisionCode());
//            response.setValue("decisionDate", decision.getDecisionDate());
//            response.setValue("entreprise", decision.getEntreprise());
//            response.setValue("emitteur", decision.getEmitteur());
//        }
//    }
//    // TODO to be removed
//    /* code pour afficher une vue form sans boutton (non utinlisé)
//    @Transactional
//    public void edit(ActionRequest request, ActionResponse response){
//        Context context = request.getContext();
//        Affectation affectation = affectationRepository.all().filter("self.id = ?1",context.get("id") ).fetchOne();
//        List<Decision> ds = new ArrayList<>();
//        ds.addAll(affectation.getDecision());
//        Decision decision;
//        if(ds.size()>0)
//            decision = ds.get(ds.size()-1);
//        response.setView(ActionView
//                .define("Affectation")
//                .model(Affectation.class.getName())
//                .add("form", "affectation-rh-form")
//                .param("popup", "true")
//                .param("show-toolbar", "false")
//                .param("show-confirm", "false")
//                .param("popup-save", "false")
//                .param("forceEdit", "true")
//                .context("_showRecord", context.get("id"))
//                .map());
//    }*/
//
    private String validerDecisionValidation(Decision decision) {
        String errorMessage = "Les champs suivants sont requis pour la validation: ";
        boolean hasError = false;
        if (decision.getDecisionCode() == null) {
            if (hasError)
                errorMessage += ", ";
            hasError = true;
            errorMessage += "Code decision";
        }
        if (decision.getEmitteur() == null) {
            if (hasError)
                errorMessage += ", ";
            hasError = true;
            errorMessage += "Emitteur";
        }
        if (decision.getDecisionDate() == null) {
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

    //
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

    //
//    @Transactional
//    private Decision updateDecision(Context context, Decision d, String status) {
//        Decision decision = decisionRep.find(d.getId());
//
//        Map<String, Object> dataFile = (HashMap) context.get("attachement");
//        if (dataFile != null) {
//            MetaFile file = fileRep.find(Long.valueOf((Integer) dataFile.get("id")));
//            decision.setAttachement(file);
//        }
//        Map<String, Object> entiteData = (HashMap) context.get("emitteur");
//        if (entiteData != null) {
//            Entite entite = entiteRep.find(Long.valueOf((Integer) entiteData.get("id")));
//            decision.setEmitteur(entite);
//        }
//        decision.setStatus(status);
//        decision.setMotifRejet((String) context.get("motifRejet"));
//        decision.setDecisionDate(new LocalDate(context.get("decisionDate")));
//        if (!DecisionRepository.STATUS_REJECTED.equals(status))
//            decision.setDecisionCode((String) context.get("decisionCode"));
//
//        decision.setEntreprise((String) context.get("entreprise"));
//
//        //Who's execute the action (Verification, Validation, Rejection)
//
//        return decisionRep.save(decision);
//    }
//
//    private MetaFile saveFile(MetaFile attachment) {
//        MetaFile file = new MetaFile();
//        file.setDescription(attachment.getDescription());
//        file.setFileName(attachment.getFileName());
//        file.setFilePath(attachment.getFilePath());
//        file.setFileSize(attachment.getFileSize());
//        return file;
//    }
//
//    public void printDecision(ActionRequest request, ActionResponse response) throws AxelorException {
//
//        Decision decision = request.getContext().asType(Decision.class);
//        String language = "fr";
//
//        String name = affectationService.getFileName(decision);
//
//        String fileLink = affectationService.getReportLink(decision, name, language, ReportSettings.FORMAT_DOC);
//
//
//        logger.debug("Printing " + name);
//
//        response.setView(ActionView
//                .define(name)
//                .add("html", fileLink).map());
//    }
    public void changedGrade(ActionRequest request, ActionResponse response) {
        Context context = request.getContext();
        Map miniGrade = (HashMap) context.get("grades");
        Grade grade = gradeRepository.find(Long.valueOf((Integer) miniGrade.get("id")));
        if (grade.getCadre() != null)
            response.setValue("cadre", grade.getCadre().getName());
        if (grade.getFiliere() != null)
            response.setValue("filiere", grade.getFiliere().getName());
        response.setValue("echelle", grade.getEchelle());
    }
}
