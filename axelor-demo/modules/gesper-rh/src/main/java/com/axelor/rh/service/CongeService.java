package com.axelor.rh.service;

import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.config.db.Entite;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.rh.db.*;
import com.axelor.rh.db.repo.AffectationRepository;
import com.axelor.rh.db.repo.CongeRepository;
import com.axelor.rh.db.repo.JourFerieRepository;
import com.axelor.rh.db.repo.ValidationcongeRepository;
import com.axelor.rh.utils.SQLQueries;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import com.google.common.collect.Lists;

import java.util.*;
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
    @Inject
    ValidationcongeRepository validatecongeRepository;
    @Inject
    AffectationRepository affectationRepository;

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
    public List<Conge> getCongeByIdEmploye(BigInteger idsEmploye) {

        return Query.of(Conge.class)
                .filter("self.employee.id = :employee")
                .bind("employee", idsEmploye)
                .fetch();

    }

    public List<BigInteger> getListIdEmployeCongeEquipe(int idEntite,Long idemploye) {

        return JPA.em().createNativeQuery(SQLQueries.getListIDEmployeCongeDuEquipe(idEntite,idemploye)).getResultList();
    }

    public Long getIdEntiteByMatriculeEmploye(String idEmploye) {
        BigInteger idEntite = (BigInteger) JPA.em().createNativeQuery(SQLQueries.getIdEntite(idEmploye)).getSingleResult();
        if (!idEntite.equals(null))
            return idEntite.longValue();
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


    static Set<Long>  idsSubordonner= new HashSet<>();
    public Set<Long> getAllSubordonnerByParentIds(String identite){
        List<BigInteger> result = JPA.em().createNativeQuery(SQLQueries.getAllSubordonnerByParentIds(identite)).getResultList();
        if(!result.isEmpty()){
            Set<BigInteger> set =new HashSet<>(result);
            for (BigInteger id:set) {
                CongeService.idsSubordonner.add(id.longValue());
            }
            return getAllSubordonnerByParentIds(Joiner.on(",").join(set));
        }else {
            return  CongeService.idsSubordonner;
        }
    }

    private Set<Long> setIdsConge(Long idEntite){
        Set<Long> ids=new HashSet<>();
        Long idemploye = employeService.getEmployeByCodeUser(AuthUtils.getUser()).getId();
        List<BigInteger> EmployeListId = getListIdEmployeCongeEquipe(idEntite.intValue(),idemploye);
        for (BigInteger idEmploye:EmployeListId) {
            List<Conge> conges =getCongeByIdEmploye( idEmploye );
            if (!conges.isEmpty())
                for (Conge  c: conges) {
                    ids.add(c.getId());
                }
        }

        return ids;
    }

    public Set<Long> getlistDemandeDuCongeEquipeService() {
//        Affectation affectation = affectationRepository.findByEmployee(AuthUtils.getUser().getCode());
//        List<Affectation> affectations = affectationRepository.findByEntite(affectation.getEntite().getId(),affectation.getEmployee().getId()).fetch();
//        System.out.println("===========================> Affectation "+affectation.getId());
        Long idEntiteUser = getIdEntiteByMatriculeEmploye(AuthUtils.getUser().getCode());
        Set<Long> IdsConge = setIdsConge(idEntiteUser);
//        if(!CongeService.idsSubordonner.isEmpty()){
//            CongeService.idsSubordonner = new HashSet<>();
//        }else{
//            CongeService.idsSubordonner.addAll(IdsConge);
//        }
        Set<Long> idsSub = getAllSubordonnerByParentIds(idEntiteUser.toString());
        if(!idsSub.isEmpty()){
            for (Long sub:idsSub)
                    IdsConge.addAll(setIdsConge(sub));
            CongeService.idsSubordonner = new HashSet<>();
        }
        return IdsConge;
    }

    public Employe getEmployeByCodeCurrentUser(){
        return employeService.getEmployeByCodeUser(AuthUtils.getUser());
    }

    public Boolean employeIsResponsable() {
        Boolean respo = false;
        try {
            Long idemploye = getEmployeByCodeCurrentUser().getId();
            if (!idemploye.equals(null))
                respo = (Boolean) JPA.em().createNativeQuery(SQLQueries.employeIsResponsable(Long.toString(idemploye))).getSingleResult();
        } catch (Exception e) {
        }
        return respo;
    }
    public Boolean employeIsDRH() {
        Boolean drh = false;
        try {

                Object user= JPA.em().createNativeQuery(SQLQueries.employeIsDRH(AuthUtils.getUser().getCode())).getSingleResult();
                if(!JPA.em().createNativeQuery(SQLQueries.employeIsDRH(AuthUtils.getUser().getCode())).getSingleResult().equals(null))
                    drh= true;
        } catch (Exception e) {
        }
        return drh;
    }

    public Validationconge valider(Employe employe,Conge conge){
        Validationconge validateconge=new Validationconge();
        validateconge.setEmployee((employe.getId()).intValue());
        validateconge.setConge((conge.getId()).intValue());
        validateconge.setMotif("null");
        return validatecongeRepository.save(validateconge) ;
    }
}
