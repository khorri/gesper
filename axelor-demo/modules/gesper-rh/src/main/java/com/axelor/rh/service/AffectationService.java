package com.axelor.rh.service;

import com.axelor.apps.ReportFactory;
import com.axelor.config.db.Decision;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.report.ReportGenerator;
import com.axelor.rh.report.IReport;
import com.axelor.rh.web.SQLQueries;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.math.BigInteger;

/**
 * Created by NOREDINE on 19/07/2018.
 */
public class AffectationService implements Serializable {

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

    public int decsionUsedInOtherAffectation(Decision decision) {
//        JPA.em().createQuery(
//                "SELECT af from Affectation as af where af.decsion in (SELECT d FROM DECISION where decisionCode ='" + decision.getDecisionCode() + "')";
//                        +
//                        "inner join Decision as d " +
//                        "where d in :decision and decisionCode ='" + decision.getDecisionCode() + "'", Affectation.class);
        BigInteger count = (BigInteger) JPA.em().createNativeQuery(SQLQueries.GET_AFFECATATION_DECSION_BY_CODE(decision.getDecisionCode())).getSingleResult();
        return count.intValue();
    }
}
