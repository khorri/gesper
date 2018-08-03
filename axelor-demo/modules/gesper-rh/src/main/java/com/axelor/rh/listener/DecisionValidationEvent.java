package com.axelor.rh.listener;

import com.axelor.config.db.Decision;
import com.axelor.db.Model;

/**
 * Created by HORRI on 03/08/2018.
 */
public class DecisionValidationEvent {
    private Model model;
    private Decision decision;

    public DecisionValidationEvent(Model model, Decision decision) {
        this.model = model;
        this.decision = decision;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public Decision getDecision() {
        return decision;
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }
}
