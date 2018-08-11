package com.axelor.rh.listener;

import com.axelor.rh.db.Med;


public class MedDecisionValidationListener implements DecisionValidationListener {
    @Override
    public void decisionValidated(DecisionValidationEvent event) {
        if (event.getModel() instanceof Med) {
            Med med = (Med) event.getModel();
            //nothing so far
//            Repository repository = JpaRepository.of(Med.class);
//            if (med != null) {
//
//                repository.save(med);
//            }
        }
    }
}
