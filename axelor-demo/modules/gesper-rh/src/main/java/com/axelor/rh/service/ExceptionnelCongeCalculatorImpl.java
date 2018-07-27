package com.axelor.rh.service;

import com.axelor.rh.db.Employe;
import com.axelor.rh.db.TypeConge;

/**
 * Created by HORRI on 25/07/2018.
 */
public class ExceptionnelCongeCalculatorImpl implements CongeCalculator{

    public static  final String TYPE_CONGE="EXC";

    @Override
    public Integer getSolde(Employe employee, TypeConge typeConge) {
        return typeConge.getDroitJour();
    }

    @Override
    public String getCode() {
        return TYPE_CONGE;
    }
}
