package com.axelor.rh.web;

import com.axelor.config.db.Decision;
import com.axelor.db.JPA;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.db.Repository;
import com.axelor.exception.AxelorException;
import com.axelor.rh.service.DecisionService;
import com.axelor.rh.service.IDecisionWorkFlow;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;

/**
 * Created by HB on 02/08/2018.
 */
public class DecisionWorkFlowController {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    IDecisionWorkFlow decisionWorkFlow;

    @Inject
    private DecisionService decisionService;

    @Transactional
    public void verifie(ActionRequest request, ActionResponse response) throws AxelorException {
        Model model = getModel(request);
        Decision decision = decisionWorkFlow.verify(model);
        response.setValue("decision", decision);
        response.setValue("decisionCode", decision.getDecisionCode());
        response.setValue("decisionDate", decision.getDecisionDate());
        response.setValue("entreprise", decision.getEntreprise());
        response.setValue("emitteur", decision.getEmitteur());
    }


    @Transactional
    public void valider(ActionRequest request, ActionResponse response) throws AxelorException {

        Context context = request.getContext();

        if (decisionService.isValid(context, response)) {
            Model model = getModel(request);
            decisionWorkFlow.validate(model, context);
            response.setReload(true);
        }



    }

    private Model getModel(ActionRequest request) throws AxelorException {
        try {
            String modelString = request.getModel();
            Class model = Class.forName(modelString);
            final Repository repository = JpaRepository.of(model);
            List<Object> records = request.getRecords();

            Model bean = null;
            if (records == null) {
                records = Lists.newArrayList();
                records.add(request.getData().get("context"));
            }
            for (Object record : records) {

                if (record == null) {
                    continue;
                }

                record = (Map) repository.validate((Map) record, request.getContext());



                Map<String, Object> orig = (Map) ((Map) record).get("_original");
                JPA.verify(model, orig);


                bean = JPA.edit(model, (Map) record);


                bean = JPA.manage(bean);
                if (bean != null)
                    return bean;

            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AxelorException();
        }
        return null;
    }


}
