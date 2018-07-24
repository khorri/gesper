package com.axelor.rh.web;

/**
 * Created by HB on 10/05/2018.
 */
public class SQLQueries  {
    //DATABASE TYPE MYSQL OR SQLSERVE
    private static final String TYPE="MYSQL";

    public static final String GET_LAST_MATRICULE_MYSQL = "SELECT CAST(MAX(e.matricule+0) as UNSIGNED) AS matricule FROM rh_employe e";

    public static String GENERATE_DROIT_AVANCEMENT_LIST_BY_EXERCICE(Long exerciceID){
        String query="";
        if(TYPE.equals("MYSQL")){
            query="INSERT INTO gesper.rh_droit_avancement ("+
                    " `id`, `employee`, `exercice`, `grade`, `echelon`, `coeff_avancement_rapide`, `numero`, `type_avancement`, `statut`, `version` )"+
                    " SELECT @rown\\:= @rown + 1 AS id, emp.id AS employe, ex.id AS exrcice, MAX(g.id) AS grade, MAX(ge.id) AS echellon, MAX(ge.rythme_rapide) AS rythme_rapide, CONCAT(@rownum\\:= @rownum+1,'/',ex.name) AS numero, 'INT' AS type, false AS statut, '0' AS version"+
                    " FROM gesper.rh_employe AS emp"+
                    " JOIN (SELECT @rown\\:= (select next_val from gesper.rh_droit_avancement_seq)-1) as r"+
                    " JOIN (SELECT @rownum\\:=0) AS t"+
                    " JOIN gesper.rh_situation AS sit ON sit.employee=emp.id"+
                    " JOIN gesper.config_grade_echelon AS ge ON ge.id=sit.grade"+
                    " JOIN gesper.config_grade AS g ON g.id=ge.grade"+
                    " JOIN gesper.config_exercice AS ex ON ex.id="+exerciceID+""+
                    " LEFT JOIN gesper.rh_droit_avancement AS dr ON dr.employee=emp.id"+
                    " WHERE emp.activated is true AND sit.active is true AND sit.status!='D' AND emp.id NOT IN (SELECT employee FROM gesper.rh_droit_avancement WHERE exercice="+exerciceID+" )"+
                    " AND ge.rythme_rapide<=TIMESTAMPDIFF(MONTH,sit.echelon_date,ex.fin)"+
                    " GROUP BY emp.id"+
                    " ORDER BY @rownum;";
            return query;
        }else{
            return query;
        }
    }

    public static final String UPDATE_DROIT_AVANCEMENT_SEQUENCE= "UPDATE gesper.rh_droit_avancement_seq "+
            " SET next_val = (SELECT (IFNULL(MAX(id),0)+1) next_val FROM gesper.rh_droit_avancement);";
}
