package com.axelor.rh.web;

/**
 * Created by HB on 10/05/2018.
 */
public class SQLQueries {

    public static final String GET_LAST_MATRICULE_MYSQL = "SELECT CAST(MAX(e.matricule+0) as UNSIGNED) AS matricule FROM rh_employe e";

    public static String GET_AFFECATATION_DECSION_BY_CODE(String code) {
        return " SELECT COUNT(afd.rh_affectation) AS nbr FROM gesper.config_decision AS d " +
                " JOIN gesper.rh_affectation_decision AS afd ON d.id=afd.decision " +
                " WHERE d.decision_code='" + code + "'";
    }
}
