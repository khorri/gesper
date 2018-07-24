package com.axelor.rh.service;

import com.axelor.apps.ReportFactory;
import com.axelor.config.db.Decision;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.report.ReportGenerator;
import com.axelor.rh.report.IReport;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.List;

/**
 * Created by NOREDINE on 19/07/2018.
 */
public class SituationService implements Serializable {

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    private ReportGenerator generator;

    public String getFileName(Decision decision) {

        return I18n.get("Decision") + " " + decision.getDecisionCode();
    }

    public String getReportLink(Decision decision, String name, String language, String format) throws AxelorException {

        return ReportFactory.createReport(IReport.AFFECTATION_DECISION, name + "-${date}")
                .addParam("Locale", language)
                .addParam("DecisionId", decision.getId())
                .addFormat(format)
                .generate()
                .getFileLink();
    }

    public int decsionUsedInOtherSituation(Decision decision) {

        List<Object> affectation = JPA.em().createQuery(
                "SELECT si from Situation as si "
                        + " join si.decision d"
                        + " where d.id = :decisionId ").setParameter("decisionId", decision.getId()
        ).getResultList();
        return affectation.size();
    }
}
