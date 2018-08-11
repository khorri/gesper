package com.axelor.rh.service;

import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.config.db.Decision;
import com.axelor.config.db.repo.DecisionRepository;
import com.axelor.config.db.repo.EntiteRepository;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.rh.listener.DecisionValidationEvent;
import com.axelor.rh.listener.DecisionValidationListener;
import com.axelor.rpc.Context;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.joda.time.LocalDate;
import org.reflections.Reflections;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

/**
 * Created by HB on 02/08/2018.
 */
public class DecisionWorkFlowImpl implements IDecisionWorkFlow {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private List<DecisionValidationListener> listeners = Lists.newArrayList();

    @Inject
    private EntiteRepository entiteRep;

    @Inject
    private DecisionRepository decisionRepository;
    @Inject
    private DecisionService decisionService;

    public DecisionWorkFlowImpl() {
        registerListeners();
    }


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
        return decisionRepository.save(decision);
    }

    @Override
    public boolean validate(Model model, Context context) throws AxelorException {

        try {
            Method m = model.getClass().getMethod("getDecision");
            Decision decision = (Decision) m.invoke(model);
            decision = decisionService.updateDecision(context, decision, DecisionRepository.STATUS_VALIDATED);
            if (decision != null)
                notifyListeners(model, decision);
            return decision != null;


        } catch (Exception e) {
            throw new AxelorException();
        }


    }

    private void notifyListeners(Model model, Decision decision) {
        DecisionValidationEvent event = new DecisionValidationEvent(model, decision);
        for (DecisionValidationListener listener : listeners) {
            listener.decisionValidated(event);
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

    private void registerListeners() {
        try {
            Reflections reflections = new Reflections("com.axelor.rh.listener");

            Set<Class<? extends DecisionValidationListener>> allClasses =
                    reflections.getSubTypesOf(DecisionValidationListener.class);
            for (Class clazz : allClasses) {

                DecisionValidationListener listener = (DecisionValidationListener) clazz.newInstance();
                listeners.add(listener);

            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }


}
