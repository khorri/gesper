package com.axelor.rh.web;

import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.config.db.Decision;
import com.axelor.config.db.Entite;
import com.axelor.config.db.Exercice;
import com.axelor.config.db.GradeEchelon;
import com.axelor.config.db.repo.DecisionRepository;
import com.axelor.config.db.repo.ExerciceRepository;
import com.axelor.config.db.repo.GradeEchelonRepository;
import com.axelor.config.db.repo.GradeRepository;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rh.annotation.Avancement;
import com.axelor.rh.db.DroitAvancement;
import com.axelor.rh.db.Situation;
import com.axelor.rh.db.repo.DroitAvancementRepository;
import com.axelor.rh.db.repo.EmployeRepository;
import com.axelor.rh.db.repo.NoteRepository;
import com.axelor.rh.db.repo.SituationRepository;
import com.axelor.rh.report.IReport;
import com.axelor.rh.service.AvancementReportService;
import com.axelor.rh.service.ReportServiceGenerator;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.persist.Transactional;
import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.Entity;
import javax.persistence.Query;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class DroitAvancementController {


    @Inject
    private DroitAvancementRepository droitAvancementRepository;

    @Inject
    private SituationRepository situationRepository;

    @Inject
    DecisionRepository decisionRepository;

    @Inject
    MetaFileRepository metaFileRepository;

    @Inject
    @Avancement
    private ReportServiceGenerator reportService;

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
        User user= AuthUtils.getUser();
        Query query= JPA.em().createNativeQuery(SQLQueries.GENERATE_DROIT_AVANCEMENT_LIST_BY_EXERCICE(exerciceId,user.getId(),getCurrentDateString()));
        query.executeUpdate();
        JPA.em().createNativeQuery(SQLQueries.UPDATE_DROIT_AVANCEMENT_SEQUENCE).executeUpdate();
    }

    private String getCurrentDateString(){
        Date d=new Date();
        DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return dateFormat.format(d);
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
            avancement.setStatus(1);
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

    /**
     * Methode pour generer les decision des avancement
     * @param request
     * @param response
     * @autor AYOUB
     * @Date 24/07/2018
     */
    @Transactional
    public void generateDecisions(ActionRequest request, ActionResponse response) throws AxelorException {
        ObjectMapper objectMapper=new ObjectMapper();
        Exercice exercice=objectMapper.convertValue(request.getContext().get("exercice"),Exercice.class);
        int startNumero=objectMapper.convertValue(request.getContext().get("decisionNum"),Integer.class);
        LocalDate dateDecision=convertString2Date(objectMapper.convertValue(request.getContext().get("date"),String.class),"yyyy-MM-dd");

        List<DroitAvancement> avancements=JPA.em().createQuery("SELECT da FROM DroitAvancement AS da WHERE da.exercice.id=:exercice AND da.status=1")
                .setParameter("exercice",exercice.getId()).getResultList();

        avancements=createDecisions(avancements,startNumero,dateDecision);

        String name="Decisions";
        String fileLink = reportService.generateMany(IReport.AVANCEMENT_DECISION,name, ReportSettings.FORMAT_DOC,generateDecisionParameters(exercice));

        response.setView(ActionView
                .define(name)
                .add("html", fileLink).map());
    }

    private LocalDate convertString2Date(String date, String format) {
        DateTimeFormatter formatter= DateTimeFormat.forPattern(format);
        return formatter.parseLocalDate(date);
    }

    private List<DroitAvancement> createDecisions(List<DroitAvancement> avancements, int startNumber, LocalDate dateDecision){
        List<DroitAvancement> avancementsToValidate=new ArrayList<>();
        Entite entite= (Entite) JPA.em().createQuery("select en from Entite as en where en.shortName='SAF'").getSingleResult();
        int startCode=startNumber;
        for (DroitAvancement avancement:avancements) {
            DroitAvancement temp=avancement.getDecision()==null?createAvancementDecision(dateDecision, entite, startCode, avancement):avancement;
            avancementsToValidate.add(temp);
            startCode++;
        }
        return avancementsToValidate;
    }

    private DroitAvancement createAvancementDecision(LocalDate dateDecision, Entite entite, int startCode, DroitAvancement avancement) {
        Decision decision=new Decision();
        decision.setDecisionCode(startCode+"/"+avancement.getExercice().getName());
        decision.setDecisionDate(dateDecision);
        decision.setStatus(DecisionRepository.STATUS_VERIFIED);
        decision.setEmitteur(entite);
        decision=decisionRepository.save(decision);
        avancement.setDecision(decision);
        avancement.setStatus(DroitAvancementRepository.STATUS_ATTENT_VALIDATION);
        avancement=droitAvancementRepository.save(avancement);
        createSituation(avancement,decision);
        return avancement;
    }

    private void createSituation(DroitAvancement avancement,Decision decision){
        Situation situation=new Situation();
        situation.setActive(false);
        situation.setStatus(DecisionRepository.STATUS_VERIFIED);
        situation.setAvancement(avancement);
        situation.setDecision(decision);
        situation.setNature("AVANCEMENT");
        situation.setType("INT");
        situation.setEmployee(avancement.getEmployee());
        situation.setEchelonDate(avancement.getDateEchelon());
        situation.setGradeDate(avancement.getDateGrade());
        situationRepository.save(situation);
    }

    private Map<String,Object> generateDecisionParameters(Exercice exercice){
        Map<String,Object> params=new HashMap<String,Object>();
        params.put("exercice",exercice.getId().intValue());
        params.put("status",DroitAvancementRepository.STATUS_ATTENT_VALIDATION);
        return params;
    }

    /**
     * Method pour valider les avancements en masse
     * @param request
     * @param response
     * @Author AYOUB
     * @Date 27/07/2018
     */
    @Transactional
    public void validateAvancements(ActionRequest request, ActionResponse response){
        List<Integer> Ids=(List)request.getContext().get("ids");
        Map<String,Object> file=(HashMap)request.getContext().get("decisionFiles");
        List<DroitAvancement> avancements=getAvancementsToValidate(Ids);
        MetaFile metaFile= metaFileRepository.find((Long.valueOf((Integer)file.get("id"))));
        validateDecision(avancements,metaFile);
    }

    private List<DroitAvancement> getAvancementsToValidate(List<Integer> ids){
        List<DroitAvancement> avancements=new ArrayList<>();
        for (Integer id:ids) {
            DroitAvancement avancement=droitAvancementRepository.find(Long.valueOf(id));
            avancements.add(avancement);
        }
        return avancements;
    }

    @Transactional
    private void validateDecision(List<DroitAvancement> avancements, MetaFile file){
        for (DroitAvancement avancement:avancements) {
            Decision decision=decisionRepository.find(avancement.getDecision().getId());
            decision.setAttachement(file);
            decision.setStatus(DecisionRepository.STATUS_VALIDATED);
            decisionRepository.save(decision);
            avancement.setStatus(DroitAvancementRepository.STATUS_VALIDATED);
            droitAvancementRepository.save(avancement);
            List<Situation> situations=situationRepository.all().filter("self.decision.id=?1",decision.getId()).fetch();
            validateSituations(situations);
        }
    }

    @Transactional
    private void validateSituations(List<Situation> situations){
        for (Situation situation:situations) {
            situation.setDecisionStatus(DecisionRepository.STATUS_VALIDATED);
            situationRepository.save(situation);
        }
    }

    private Situation getSituationByAvancement(Integer id){
        return situationRepository.all().filter("self.avancement.id=?1",id).fetchOne();
    }

    /**
     * method qui test le type de validation des avancements
     * @param request
     * @param response
     * @author Ayoub
     * @Date 27/07/2018
     */
    public void validateAvancementsDecisions(ActionRequest request, ActionResponse response){
        List<Integer> Ids=(List)request.getContext().get("_ids");
        if(Ids==null || Ids.size()==0){
            response.setError("Aucun avancement à valider selectionner!!");
            return;
        }
        if(Ids.size()==1){
            Situation situation=getSituationByAvancement(Ids.get(0));
            response.setView(ActionView.define("Situations")
                    .model(Situation.class.getName())
                    .add("form", "situation-rh-form")
                    .param("popup","true")
                    .param("show-toolbar","false")
                    .param("show-confirm","false")
                    .param("popup-save","false")
                    .param("forceTitle", "true")
                    .context("_showRecord", situation.getId().toString())
                    .domain("self.id = " + situation.getId())
                    .map() );
            return;
        }

        response.setView(ActionView.define("Décisions").model(MetaFile.class.getName())
                .add("form","avancement-decision-validation-form")
                .param("popup","true")
                .param("show-toolbar","false")
                .param("show-confirm","false")
                .param("popup-save","false")
                .context("ids", Ids)
                .map()
        );
    }

}