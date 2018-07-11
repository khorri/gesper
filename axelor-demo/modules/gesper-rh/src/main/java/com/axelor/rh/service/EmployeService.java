package com.axelor.rh.service;

import com.axelor.apps.ReportFactory;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.report.ReportGenerator;
import com.axelor.rh.db.Employe;
import com.axelor.rh.db.repo.EmployeRepository;
import com.axelor.rh.report.IReport;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.util.Locale;
import java.util.Map;

/**
 * Created by HORRI on 22/05/2018.
 */
public class EmployeService implements Serializable{

    private final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    @Inject
    private EmployeRepository employeRepository;

    @Inject
    private ReportGenerator generator;

    public String getFileName(Employe agent) {
        return I18n.get("work certificate") + " " + agent.getMatricule() ;
    }

    public String getReportLink(Employe agent, String name, String language, String format) throws AxelorException {

        return ReportFactory.createReport(IReport.WORK_CERTIFICATE, name+"-${date}")
                .addParam("Locale", language)
                .addParam("SaleOrderId", agent.getId())
                .addFormat(format)
                .generate()
                .getFileLink();
    }

    public void print(String design, Map<String, Object> params) throws IOException {

        OutputStream output = new FileOutputStream(new File(""));
        try {
            //generator.generate(output, design, params, Locale.getDefault());
        } finally {
            output.close();
        }
    }
}
