package com.axelor.rh.service;

import com.axelor.rh.db.Employe;
import com.axelor.rh.db.TypeConge;

/**
 * Created by HORRI on 25/07/2018.
 */
public class PelerinageCongeCalculatorImpl extends GenericCongeCalculator implements CongeCalculator {
    public static  final String TYPE_CONGE="PEL";


    @Override
    public Integer getCalculatedSolde(Employe employee, TypeConge typeConge) {
        return typeConge.getDroitJour();
    }

    @Override
    public String getCode() {
        return TYPE_CONGE;
    }
}
