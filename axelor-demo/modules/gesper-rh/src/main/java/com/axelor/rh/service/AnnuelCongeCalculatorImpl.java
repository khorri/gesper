package com.axelor.rh.service;

import com.axelor.rh.db.DroitConge;
import com.axelor.rh.db.Employe;
import com.axelor.rh.db.TypeConge;
import org.joda.time.LocalDate;
import org.joda.time.Months;

/**
 * Created by HORRI on 25/07/2018.
 */
public class AnnuelCongeCalculatorImpl extends GenericCongeCalculator implements CongeCalculator {

    public static  final String TYPE_CONGE="ANN";
    public static final String NOMBRE_MOIS_REQUIS = "rh.Conge.nombreMoisRequis";
    public static final int defaultNombreMoisRequisConge = 11;


    @Override
    public String getCode() {
        return TYPE_CONGE;
    }

    public Integer getCalculatedSolde(Employe employee,TypeConge typeConge) {
        LocalDate now = new LocalDate();
        int lastYear = now.minusYears(1).getYear();
        LocalDate hireDate = employee.getHireDate();
        int months = Months.monthsBetween(hireDate,now).getMonths();
        String nombreMoisRequisString = getParametersLoader().get(NOMBRE_MOIS_REQUIS);
        int nombreMoisRequis = (nombreMoisRequisString!=null)?Integer.valueOf(nombreMoisRequisString):defaultNombreMoisRequisConge;
        if(months<nombreMoisRequis)
            return 0;
        if(months<nombreMoisRequis*2)
            return typeConge.getDroitJour();
        else{
            DroitConge droitConge = getDroitCongeRepository().all()
                    .filter("self.employee.id=?1 AND self.typeConge.id=?2 AND self.exercice=?3 ",employee.getId(),typeConge.getId(),lastYear)
                    .fetchOne();
            int resteConge = (droitConge==null)?typeConge.getDroitJour():droitConge.getSolde();
            return resteConge+typeConge.getDroitJour();
        }

    }
}
