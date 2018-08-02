package com.axelor.rh.service;

import com.axelor.config.db.Decision;
import com.axelor.db.Model;

/**
 * Created by HB on 02/08/2018.
 */
public class DecisionWorkFlowImpl implements IDecisionWorkFlow {

    @Override
    public Model create(Model entity) {
        return null;
    }

    @Override
    public Decision verify(Model entity) {
        return null;
    }

    @Override
    public boolean validate(Model entity, Decision decision) {
        return false;
    }

    @Override
    public boolean refuse(Model entity, Decision decision) {
        return false;
    }
}
