package com.axelor.rh.web;

import com.axelor.rh.db.Employe;
import com.axelor.rh.db.repo.EmployeRepository;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;

import java.util.List;

/**
 * Created by HORRI on 03/05/2018.
 */
public class EmployeController {

    @Inject
    private EmployeRepository employeRepository;

    public String getLastMatricule(){
        List<Employe> employeList= this.employeRepository.all().fetch();
        Ordering<Employe> o = new Ordering<Employe>() {
            @Override
            public int compare(Employe left, Employe right) {

                return Ints.compare(Integer.valueOf(left.getMatricule()), Integer.valueOf(right.getMatricule()));
            }
        };
        Employe emp= o.max(employeList);
        Integer newMatricule = Integer.valueOf(emp.getMatricule())+1;
        return newMatricule.toString();
    }
}
