package com.axelor.rh.service;

import com.axelor.rh.db.DroitConge;
import com.axelor.rh.db.Employe;
import com.axelor.rh.db.TypeConge;
import org.joda.time.LocalDate;

import java.util.List;

/**
 * Created by HORRI on 25/07/2018.
 */
public class ExceptionnelCongeCalculatorImpl extends GenericCongeCalculator implements CongeCalculator {

    public static  final String TYPE_CONGE="EXC";
    public static final String NOMBRE_JOURS_LIMIT = "rh.Conge.nombreJoursLimit";


    @Override
    public Integer getCalculatedSolde(Employe employee, TypeConge typeConge) {
        LocalDate now = new LocalDate();
        List<DroitConge> droitsConge = getDroitCongeRepository().all()
                .filter("self.employee.id=?1 AND self.typeConge.annotation='EXC' AND self.exercice=?2 ", employee.getId(), now.getYear())
                .fetch();
        int solde = 0;
        for (DroitConge dc : droitsConge) {
            solde = dc.getTypeConge().getDroitJour() - dc.getSolde();
        }
        String nombreJourLimitString = getParametersLoader().get(NOMBRE_JOURS_LIMIT);
        //@TODO if  nombreJourLimitString is null throw an exception
        int nombreJourLimit = Integer.valueOf(nombreJourLimitString);
        solde = nombreJourLimit - solde;
        if (typeConge.getDroitJour() < solde)
            return typeConge.getDroitJour();
        return solde;
    }

    @Override
    public String getCode() {
        return TYPE_CONGE;
    }
}
