package com.axelor.rh.service;

import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.config.db.Entite;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.rh.db.Conge;
import com.axelor.rh.db.Employe;
import com.axelor.rh.db.JourFerie;
import com.axelor.rh.db.repo.CongeRepository;
import com.axelor.rh.db.repo.JourFerieRepository;
import com.axelor.rh.utils.SQLQueries;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.math.BigInteger;
import java.util.logging.Logger;

/**
 * Created by HORRI on 27/07/2018.
 */
public class CongeService {

    @Inject
    JourFerieRepository jourFerieRepository;

    @Inject
    CongeRepository congeRepository;
    @Inject
    EmployeService employeService;

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

    @Transactional
    public Conge getCongeByIdEmploye(BigInteger idEmploye) {

        return Query.of(Conge.class)
                .filter("self.employee.id = :employee")
                .bind("employee", idEmploye)
                .fetchOne();

    }

    public List<BigInteger> getListIdEmployeCongeEquipe(int idEntite) {

        return JPA.em().createNativeQuery(SQLQueries.getListIDEmployeCongeDuEquipe(idEntite)).getResultList();
    }

    public Long getIdEntiteByMatriculeEmploye(String idEmploye) {
        Long idEntite = (Long) JPA.em().createNativeQuery(SQLQueries.getIdEntite(idEmploye)).getSingleResult();
        if (!idEntite.equals(null))
            return idEntite;
        return null;
    }

    public Entite getParentByEntiteId(Long idEntite) {
        Entite entite = Query.of(Entite.class).filter("self.id= :id").bind("id", idEntite).fetchOne();
        if (!entite.equals(null))
            return entite.getParent();
        return null;
    }

    public Entite getEntite(Long idEntite){
        return Query.of(Entite.class).filter("self.id= :id").bind("id", idEntite).fetchOne();
    }

    public List<Entite> getListSubordonnerByIdentite(Long identite){
        return Query.of(Entite.class).filter("self.parent = :id").bind("id",identite).fetch() ;
    }
    public List<Long> getlistDemandeDuCongeEquipeService() {
        Long idEntiteUser = getIdEntiteByMatriculeEmploye(AuthUtils.getUser().getCode());

        
        Entite parent =getParentByEntiteId(idEntiteUser);

        String leaveListIdStr = "-2";
        while (!parent.equals(null)){

            parent = getParentByEntiteId(getEntite(parent.getId()).getId());
        }

        if (!idEntiteUser.equals(null)) {
            List<BigInteger> EmployeListId = getListIdEmployeCongeEquipe(idEntiteUser.intValue());
            List<Long> CongeListID = new ArrayList<>();

            if (!EmployeListId.isEmpty()) {
                for (BigInteger idEmploye : EmployeListId) {
                    CongeListID.add(getCongeByIdEmploye(idEmploye).getId());
                }
                if (!CongeListID.isEmpty()) {
                    leaveListIdStr = Joiner.on(",").join(CongeListID);
                }
            } else {

            }
            return CongeListID;
        }
        return null;
    }

    public Boolean employeIsResponsable() {
        Boolean respo = false;

        try {
            Long idemploye = employeService.getEmployeByCodeUser(AuthUtils.getUser()).getId();
            if (!idemploye.equals(null))
                respo = (Boolean) JPA.em().createNativeQuery(SQLQueries.employeIsResponsable(Long.toString(idemploye))).getSingleResult();
        } catch (Exception e) {
        }

        return respo;

    }
}
