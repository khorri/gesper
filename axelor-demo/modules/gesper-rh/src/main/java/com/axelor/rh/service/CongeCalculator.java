package com.axelor.rh.service;

import com.axelor.rh.db.Employe;
import com.axelor.rh.db.TypeConge;

/**
 * Created by HORRI on 25/07/2018.
 */
public interface CongeCalculator {

    /**
     *
     * @param employee
     * @param typeConge
     * @return the remaining leave days for an employee.
     */
    Integer getSolde(Employe employee, TypeConge typeConge);
    String getCode();
}
