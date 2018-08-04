package com.axelor.rh.service;

import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.config.db.Decision;
import com.axelor.config.db.repo.DecisionRepository;
import com.axelor.config.db.repo.EntiteRepository;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import org.joda.time.LocalDate;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/**
 * Created by HB on 02/08/2018.
 */
public class DecisionWorkFlowImpl implements IDecisionWorkFlow {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    private EntiteRepository entiteRep;

    @Inject
    private DecisionService decisionService;

    @Override
    public Model create(Model entity) {
        return null;
    }

    @Override
    public Decision verify(Model model) {
        User user = AuthUtils.getUser();
        Decision decision = new Decision();
        decision.setStatus(DecisionRepository.STATUS_VERIFIED);
        decision.setEmitteur(entiteRep.all().filter("self.shortName = ?1", "SAF").fetchOne());
        decision.setVerifiedBy(user);
        decision.setVerifiedOn(new LocalDate());
        decisionService.printDecision(decision, model.getId());
        return decision;
    }

    @Override
    public boolean validate(Model model, Context context) throws AxelorException {

        try {
            Method m = model.getClass().getMethod("getDecision");
            Decision decision = (Decision) m.invoke(model);
            decision = decisionService.updateDecision(context, decision, DecisionRepository.STATUS_VALIDATED);
            return decision != null;

        } catch (Exception e) {
            throw new AxelorException();
        }


    }

    @Override
    public boolean refuse(Model model, Context context) throws AxelorException {
        try {
            Method m = model.getClass().getMethod("getDecision");
            Decision decision = (Decision) m.invoke(model);
            decision = decisionService.updateDecision(context, decision, DecisionRepository.STATUS_REJECTED);
            return decision != null;

        } catch (Exception e) {
            throw new AxelorException();
        }
    }
}
