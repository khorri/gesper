package com.axelor.rh.web;

import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.rh.db.Employe;
import com.axelor.rh.db.repo.EmployeRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.Objects;
import com.google.inject.persist.Transactional;
import com.google.inject.Inject;

import java.util.List;

/**
 * Created by HORRI on 03/05/2018.
 */

public class EmployeController {
    @Inject
    private EmployeRepository employeRepository;

    public String getLastMatricule(){
        String results = JPA.em().createNativeQuery(new SQLQueries().GET_LAST_MATRICULE_MYSQL).getSingleResult().toString();
        String nextMatricule = (Integer.valueOf(results)+1)+"";

        return nextMatricule;
    }
    @Transactional
    public void validate(ActionRequest request, ActionResponse response) {
        Context context = request.getContext();
        String numberRegex = "\\d+";
        String matricule = (String) context.get("matricule");

        if (matricule == null) {
            return;
        }

        if(!matricule.matches(numberRegex)) {
            response.setError("Le matricule doit être un nombre");
            return;
        }
        List<Employe> emp= employeRepository.all().filter("self.matricule = ?1", matricule).fetch();

            if (emp.size()>0 && !emp.get(0).getId().equals(context.get("id"))) {
                response.setError(I18n.get("ERR_FA_002"));
                response.setValue("matricule", getLastMatricule());
            }
    }
}
