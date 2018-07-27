package com.axelor.rh.service;

import com.axelor.config.db.Parametre;
import com.axelor.config.db.repo.ParametreRepository;
import com.axelor.rh.db.DroitConge;
import com.axelor.rh.db.Employe;
import com.axelor.rh.db.TypeConge;
import com.axelor.rh.db.repo.DroitCongeRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.joda.time.LocalDate;
import org.joda.time.Months;

/**
 * Created by HORRI on 25/07/2018.
 */
public class AnnuelCongeCalculatorImpl implements CongeCalculator{

    public static  final String TYPE_CONGE="ANN";
    public static final String NOMBRE_MOIS_REQUIS = "rh.Conge.nombreMoisRequis";
    public static final int defaultNombreMoisRequisConge = 11;

    @Inject
    DroitCongeRepository droitCongeRepository;

    @Inject
    ParametreRepository parametreRepository;



    @Override
    public Integer getSolde(Employe employee, TypeConge typeConge){
        LocalDate now = new LocalDate();

        DroitConge droitConge=  droitCongeRepository.all()
                            .filter("self.employee.id=?1 AND self.typeConge.id=?2 AND self.exercice=?3 ",employee.getId(),typeConge.getId(),now.getYear())
                            .fetchOne();
        if(droitConge==null){
            droitConge = new DroitConge();
            droitConge.setEmployee(employee);
            droitConge.setTypeConge(typeConge);
            droitConge.setSolde(getCalculatedSolde(employee,typeConge));
            droitConge.setExercice(now.getYear());
            droitCongeRepository.save(droitConge);
        }
        return droitConge.getSolde();
    }

    @Override
    public String getCode() {
        return TYPE_CONGE;
    }

    public Integer getCalculatedSolde(Employe employee,TypeConge typeConge) {
        LocalDate now = new LocalDate();
        int lastYear = now.minusYears(1).getYear();
        LocalDate hireDate = employee.getHireDate();
        int months = Months.monthsBetween(hireDate,now).getMonths();
        Parametre p = parametreRepository.findByName(NOMBRE_MOIS_REQUIS);
        int nombreMoisRequis = (p!=null && p.getValeur()!=null)?Integer.valueOf(p.getValeur()):defaultNombreMoisRequisConge;

        if(months<nombreMoisRequis)
            return 0;
        if(months<nombreMoisRequis*2)
            return typeConge.getDroitJour();
        else{
            DroitConge droitConge=  droitCongeRepository.all()
                    .filter("self.employee.id=?1 AND self.typeConge.id=?2 AND self.exercice=?3 ",employee.getId(),typeConge.getId(),lastYear)
                    .fetchOne();
            int resteConge = (droitConge==null)?typeConge.getDroitJour():droitConge.getSolde();
            return resteConge+typeConge.getDroitJour();
        }

    }
}
