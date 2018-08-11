package com.axelor.rh.web;

import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/**
 * Created by HB on 10/07/2018.
 */
public class MedController {

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    public LocalDate lastMedDateByEmployeeId(Integer id) throws AxelorException {
        if (id == null)
            return null;

        LocalDate date = (LocalDate) JPA.em().createQuery("SELECT MAX(dateFin) from Med m where m.employee.id =:id").setParameter("id", id.longValue()).getSingleResult();
        return date;
    }

}
