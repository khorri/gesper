package com.axelor.rh.service;

import com.axelor.rh.db.JourFerie;
import com.axelor.rh.db.repo.JourFerieRepository;
import com.google.inject.Inject;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.util.List;

/**
 * Created by HORRI on 27/07/2018.
 */
public class CongeService {

    @Inject
    JourFerieRepository jourFerieRepository;

    public int getDuration(LocalDate start, LocalDate end) {
        List<JourFerie> jourFeries = jourFerieRepository.all().filter("self.dateDebut > ?1 and self.dateFin < ?2", start, end).fetch();
        int days = Days.daysBetween(start, end).getDays() + 1;
        while (!start.isAfter(end)) {
            if (isWeekDayOrHoliday(start, jourFeries)) {
                days--;
            }
            start = start.plusDays(1);
        }
        return days;

    }

    private boolean isWeekDayOrHoliday(LocalDate start, List<JourFerie> jourFeries) {
        
        if (start.getDayOfWeek() == DateTimeConstants.SATURDAY || start.getDayOfWeek() == DateTimeConstants.SUNDAY)
            return true;
        for (JourFerie jf : jourFeries) {
            if (start.isEqual(jf.getDateDebut()) || start.isEqual(jf.getDateFin()))
                return true;
        }
        return false;
    }
}
