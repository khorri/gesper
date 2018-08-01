package com.axelor.rh.service;

import com.axelor.rh.db.DroitConge;
import com.axelor.rh.db.Employe;
import com.axelor.rh.db.TypeConge;
import com.axelor.rh.db.repo.DroitCongeRepository;
import com.google.inject.Inject;
import ma.nawar.config.util.ParametersLoader;
import org.joda.time.LocalDate;

/**
 * Created by HORRI on 31/07/2018.
 */
public abstract class GenericCongeCalculator {

    @Inject
    private DroitCongeRepository droitCongeRepository;

    @Inject
    private ParametersLoader parametersLoader;

    /**
     * @param employee
     * @param typeConge
     * @return the remaining leave days for an employee.
     */
    public Integer getSolde(Employe employee, TypeConge typeConge) {
        LocalDate now = new LocalDate();

        DroitConge droitConge = droitCongeRepository.all()
                .filter("self.employee.id=?1 AND self.typeConge.id=?2 AND self.exercice=?3 ", employee.getId(), typeConge.getId(), now.getYear())
                .fetchOne();
        if (droitConge == null) {
            droitConge = new DroitConge();
            droitConge.setEmployee(employee);
            droitConge.setTypeConge(typeConge);
            droitConge.setSolde(getCalculatedSolde(employee, typeConge));
            droitConge.setExercice(now.getYear());
            droitCongeRepository.save(droitConge);
        }
        return droitConge.getSolde();
    }

    public abstract Integer getCalculatedSolde(Employe employee, TypeConge typeConge);

    public DroitCongeRepository getDroitCongeRepository() {
        return droitCongeRepository;
    }

    public void setDroitCongeRepository(DroitCongeRepository droitCongeRepository) {
        this.droitCongeRepository = droitCongeRepository;
    }

    public ParametersLoader getParametersLoader() {
        return parametersLoader;
    }

    public void setParametersLoader(ParametersLoader parametersLoader) {
        this.parametersLoader = parametersLoader;
    }
}
