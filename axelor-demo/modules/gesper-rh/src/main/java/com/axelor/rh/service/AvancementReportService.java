package com.axelor.rh.service;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.exception.AxelorException;
import com.axelor.report.ReportGenerator;
import com.google.inject.Inject;

import java.util.Map;

public class AvancementReportService implements ReportServiceGenerator{

    @Override
    public String generateMany(String rptDesign, String outputName, String format, Map<String, Object> params) throws AxelorException {
        ReportSettings reportSettings= ReportFactory.createReport(rptDesign, outputName+"-${date}");
        for (String key:params.keySet()) {
            reportSettings.addParam(key,params.get(key));
        }
        return reportSettings.addFormat(format).generate().getFileLink();
    }

    @Override
    public String generateOne(String rptDesign, String outputName, String format, Map<String, Object> params) throws AxelorException {
        ReportSettings reportSettings= ReportFactory.createReport(rptDesign, outputName+"-${date}");
        for (String key:params.keySet()) {
            reportSettings.addParam(key,params.get(key));
        }
        return reportSettings.addFormat(format).generate().getFileLink();
    }
}