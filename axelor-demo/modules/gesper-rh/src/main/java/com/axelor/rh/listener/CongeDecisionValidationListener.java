package com.axelor.rh.listener;

import com.axelor.config.db.repo.DecisionRepository;
import com.axelor.db.JpaRepository;
import com.axelor.db.Repository;
import com.axelor.rh.db.Conge;

/**
 * Created by HORRI on 03/08/2018.
 */
public class CongeDecisionValidationListener implements DecisionValidationListener {
    @Override
    public void decisionValidated(DecisionValidationEvent event) {
        Conge conge = (Conge) event.getModel();
        Repository repository = JpaRepository.of(Conge.class);
        if (conge != null) {
            conge.setStatus(DecisionRepository.STATUS_VALIDATED);
            repository.save(conge);
        }
    }
}
