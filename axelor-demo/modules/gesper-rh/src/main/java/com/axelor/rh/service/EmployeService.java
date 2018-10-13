package com.axelor.rh.service;

import com.axelor.apps.ReportFactory;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.report.ReportGenerator;
import com.axelor.rh.db.Employe;
import com.axelor.rh.db.repo.EmployeRepository;
import com.axelor.rh.report.IReport;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.model.api.IResourceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

import com.axelor.auth.db.User;
import com.axelor.auth.AuthService;

/**
 * Created by HORRI on 22/05/2018.
 */
public class EmployeService implements Serializable {

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    private EmployeRepository employeRepository;

    @Inject
    private ReportGenerator generator;

    @Inject
    private UserRepository userRepo;
    @Inject
    EmployeRepository employeRepo;

    public String getFileName(Employe agent) {
        return I18n.get("work certificate") + " " + agent.getMatricule();
    }

    public String getReportLink(Employe agent, String name, String language, String format) throws AxelorException {

        return ReportFactory.createReport(IReport.WORK_CERTIFICATE, name + "-${date}")
                .addParam("Locale", language)
                .addParam("SaleOrderId", agent.getId())
                .addFormat(format)
                .generate()
                .getFileLink();
    }

    public Employe getEmployeByCodeUser(User user){
        return Query.of(Employe.class)
                .filter("self.matricule = :matricule")
                .bind("matricule", user.getCode())
                .fetchOne();

    }

    @Transactional
    public void createUser(Employe employe) {
        logger.debug("Begin createUser service ...");
        User userExist= userRepo.findByCode(String.valueOf(employe
                .getMatricule()));

        if (employe.getGroupe().getId() != null ) {
            User user = new User();
            user.setGroup(employe.getGroupe());
            user.setCode(employe.getMatricule());
            user.setPassword(employe.getMatricule());
            user.setLanguage("fr");
            user.setName(employe.getMatricule());
            user.setFullName(employe.getMatricule());
//            user.setEmployee(employe);

            userRepo.save(AuthService.getInstance().encrypt(user));
        } else return;

        logger.debug("End createUser service ...");
    }
}
