package com.axelor.rh.web;

import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rh.db.Employe;
import com.axelor.rh.db.repo.EmployeRepository;
import com.axelor.rh.service.EmployeService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.persist.Transactional;
import com.google.inject.Inject;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;

/**
 * Created by HORRI on 03/05/2018.
 */

public class EmployeController {

    private final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    @Inject
    private EmployeRepository employeRepository;

    @Inject
    private EmployeService employeService;

    public String getLastMatricule(){
        String results = JPA.em().createNativeQuery(SQLQueries.GET_LAST_MATRICULE_MYSQL).getSingleResult().toString();
        String nextMatricule = (Integer.valueOf(results)+1)+"";

        return nextMatricule;
    }
    @Transactional
    public void validate(ActionRequest request, ActionResponse response) {
        Context context = request.getContext();
        String numberRegex = "\\d+";
        String matricule = (String) context.get("matricule");

        //validation matricule
        if (matricule == null) {
            return;
        }
        if(!matricule.matches(numberRegex)) {
            response.setError("Le matricule doit être un nombre");
            return;
        }
        List<Employe> emp= employeRepository.all().filter("self.matricule = ?1", matricule).fetch();

            if (emp.size()>0 && !emp.get(0).getId().equals(context.get("id"))) {
                response.setError(I18n.get("ERR_FA_002"));
                response.setValue("matricule", getLastMatricule());
                return;
            }

    }



    public void printWorkCertificateWord(ActionRequest request, ActionResponse response) throws AxelorException {

        Employe agent = request.getContext().asType(Employe.class);

        String language = "fr";

        String name = employeService.getFileName(agent);

        String fileLink = employeService.getReportLink(agent, name, language, ReportSettings.FORMAT_DOC);


        logger.debug("Printing "+name);

        response.setView(ActionView
                .define(name)
                .add("html", fileLink).map());
    }
}
