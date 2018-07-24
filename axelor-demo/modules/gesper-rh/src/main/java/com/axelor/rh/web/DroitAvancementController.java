package com.axelor.rh.web;

import com.axelor.config.db.Exercice;
import com.axelor.config.db.GradeEchelon;
import com.axelor.config.db.repo.ExerciceRepository;
import com.axelor.config.db.repo.GradeEchelonRepository;
import com.axelor.config.db.repo.GradeRepository;
import com.axelor.db.JPA;
import com.axelor.rh.db.DroitAvancement;
import com.axelor.rh.db.Situation;
import com.axelor.rh.db.repo.DroitAvancementRepository;
import com.axelor.rh.db.repo.EmployeRepository;
import com.axelor.rh.db.repo.NoteRepository;
import com.axelor.rh.db.repo.SituationRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.persist.Transactional;
import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.Query;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

public class DroitAvancementController {


    @Inject
    private DroitAvancementRepository droitAvancementRepository;

    @Inject
    private EmployeRepository employeReposotory;

    @Inject
    private ExerciceRepository exerciceRepository;

    @Inject
    private GradeRepository gradeRepository;

    @Inject
    private GradeEchelonRepository gradeEchelonRepository;

    @Inject
    private SituationRepository situationRepository;

    @Inject
    private NoteRepository noteRepository;

    private final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    public void getDroitAvancementByExercice(ActionRequest request, ActionResponse response){
        ObjectMapper objectMapper=new ObjectMapper();
        Exercice ex=objectMapper.convertValue(request.getContext().get("exercice"),Exercice.class);
        Long id=ex.getId();
        List<DroitAvancement> avancements=droitAvancementRepository.all().filter("self.exercice.id= ?1",id).fetch();
        response.setValue("droitAvancement",new HashSet<>(avancements));
    }

    /**
     *
     * @param request
     * @param response
     */

    @Transactional
    public void generateDroitAvancementList(ActionRequest request, ActionResponse response){
        ObjectMapper objectMapper=new ObjectMapper();
        Exercice ex=objectMapper.convertValue(request.getContext().get("exercice"),Exercice.class);
        Long exerciceId=ex.getId();
        Long alreadyExist= droitAvancementRepository.all().filter("self.exercice.id=?1",exerciceId).count();
        if(alreadyExist>0) {
            response.setError("List des doites d'avancements est déja génerer!!");
            return;
        }
        Query query= JPA.em().createNativeQuery(SQLQueries.GENERATE_DROIT_AVANCEMENT_LIST_BY_EXERCICE(exerciceId));
        query.executeUpdate();
        JPA.em().createNativeQuery(SQLQueries.UPDATE_DROIT_AVANCEMENT_SEQUENCE).executeUpdate();
    }

    /**
     * Methode de calcule des avancement selon l'exercice et les notes et les droits d'avancement
     * @param request
     * @param response
     * @author AYOUB
     * @Date 24/07/2018
     */
    @Transactional
    public void calculerDroitAvancement(ActionRequest request, ActionResponse response){
        ObjectMapper objectMapper=new ObjectMapper();
        Exercice exercice=objectMapper.convertValue(request.getContext().get("exercice"),Exercice.class);
        List<DroitAvancement> avancements = getDroitAvancements(response, exercice);
        if (avancements == null || avancements.isEmpty())
            return;

        calculateAvancement(avancements);
    }

    private void calculateAvancement(List<DroitAvancement> avancements) {

        for (DroitAvancement avancement: avancements) {
            LocalDate dateEchelon = getDateAncienEchelon(avancement);
            Double moyenne = getNoteMoyenne(avancement, dateEchelon);
            avancement.setNoteAvancement(new BigDecimal(moyenne));
            String rythme = getRythme(moyenne);
            avancement.setRythmeAvancement(rythme);
            avancement.setDateAvancement(getDateAvancement(rythme,dateEchelon,avancement));
            avancement.setNewEchelon(getNewEchelon(avancement).getEchelon());
            avancement.setNewIndice(getNewEchelon(avancement).getIndice());
            avancement.setExerciceAvancement(getExerciceAvancement(avancement,avancement.getDateAvancement()));
            droitAvancementRepository.save(avancement);
        }
    }

    private Integer getExerciceAvancement(DroitAvancement avancement, LocalDate dateAvancement) {
        if(dateAvancement.compareTo(avancement.getExercice().getFin())>=0){
            return Integer.valueOf(avancement.getExercice().getName());
        }else{
            return dateAvancement.get(DateTimeFieldType.year());
        }
    }

    private GradeEchelon getNewEchelon(DroitAvancement avancement) {
        GradeEchelon echelon= (GradeEchelon) JPA.em()
                .createQuery("SELECT ge FROM GradeEchelon AS ge WHERE ge.grade=:grade AND ge.echelon=:echelon")
                .setParameter("grade",avancement.getGrade()).setParameter("echelon",avancement.getEchelon().getEchelon()+1).getSingleResult();
        return echelon==null?avancement.getEchelon():echelon;
    }

    private LocalDate getDateAvancement(String rythme, LocalDate dateEchelon,DroitAvancement avancement) {
        if(rythme.equalsIgnoreCase(DroitAvancementRepository.STATUS_LENT)){
            return dateEchelon.plusMonths(avancement.getEchelon().getRythmeLent());
        }
        if(rythme.equalsIgnoreCase(DroitAvancementRepository.STATUS_MOYEN)){
            return dateEchelon.plusMonths(avancement.getEchelon().getRythmeMoyen());
        }
        return dateEchelon.plusMonths(avancement.getEchelon().getRythmeRapide());
    }

    private String getRythme(Double moyenne) {
        if(moyenne<DroitAvancementRepository.NOTE_LENT){
            return DroitAvancementRepository.STATUS_LENT;
        }
        if(moyenne<DroitAvancementRepository.NOTE_MOYEN){
            return DroitAvancementRepository.STATUS_MOYEN;
        }
        return DroitAvancementRepository.STATUS_RAPIDE;
    }

    private LocalDate getDateAncienEchelon(DroitAvancement avancement) {
        Situation situation=situationRepository.all().filter("self.active is true AND self.status!='D' AND self.employee.id="+avancement.getEmployee().getId()+"").fetchOne();
        return situation.getEchelonDate();
    }

    private Double getNoteMoyenne(DroitAvancement avancement, LocalDate dateEchelon) {
        List<String> exercices=new ArrayList<>();
        for (int i = dateEchelon.get(DateTimeFieldType.year())+1; i<=avancement.getExercice().getFin().get(DateTimeFieldType.year()); i++){
            exercices.add(String.valueOf(i));
        }
        return (Double) JPA.em()
                .createQuery("SELECT AVG(n.noteAvancement)FROM Note AS n  WHERE n.employee.id=:employeeID AND n.exercice.name IN :exercices")
                .setParameter("employeeID",avancement.getEmployee().getId())
                .setParameter("exercices",exercices).getSingleResult();
    }

    private List<DroitAvancement> getDroitAvancements(ActionResponse response, Exercice exercice) {
        List<DroitAvancement> avancements= JPA.em()
                .createQuery("SELECT da FROM DroitAvancement AS da, Note AS n WHERE da.exercice.id="+exercice.getId()+" AND n.exercice.id="+
                        exercice.getId()+" AND n.employee.id=da.employee.id ").getResultList();
        if(avancements.size()==0) {
            response.setError("Les droit d'avancement d'exercice "+exercice.getName()+" n'exist pas!");
            return null;
        }
        return avancements;
    }
}