package com.axelor.rh.utils;

/**
 * Created by HB on 10/05/2018.
 */
public class SQLQueries {
    //DATABASE TYPE MYSQL OR SQLSERVE
    private static final String TYPE = "MYSQL";

    public static final String GET_LAST_MATRICULE_MYSQL = "SELECT CAST(MAX(e.matricule+0) as UNSIGNED) AS matricule FROM rh_employe e";

    public static String GENERATE_DROIT_AVANCEMENT_LIST_BY_EXERCICE(Long exerciceID, long userID, String date) {
        String query = "";
        if (TYPE.equals("MYSQL")) {
            query = "INSERT INTO gesper.rh_droit_avancement (" +
                    " `id`, `employee`, `exercice`, `grade`, `echelon`, `coeff_avancement_rapide`, `numero`, `type_avancement`, `statut`,`created_on`,`created_by`, `version` )" +
                    " SELECT @rown\\:= @rown + 1 AS id, emp.id AS employe, ex.id AS exrcice, MAX(g.id) AS grade, MAX(ge.id) AS echellon, MAX(ge.rythme_rapide) AS rythme_rapide, CONCAT(@rownum\\:= @rownum+1,'/',ex.name) AS numero, 'INT' AS type, false AS statut,'" + date + "','" + userID + "', '0' AS version" +
                    " FROM gesper.rh_employe AS emp" +
                    " JOIN (SELECT @rown\\:= (select next_val from gesper.rh_droit_avancement_seq)-1) as r" +
                    " JOIN (SELECT @rownum\\:=0) AS t" +
                    " JOIN gesper.rh_situation AS sit ON sit.employee=emp.id" +
                    " JOIN gesper.config_grade_echelon AS ge ON ge.id=sit.grade" +
                    " JOIN gesper.config_grade AS g ON g.id=ge.grade" +
                    " JOIN gesper.config_exercice AS ex ON ex.id=" + exerciceID + "" +
                    " LEFT JOIN gesper.rh_droit_avancement AS dr ON dr.employee=emp.id" +
                    " WHERE emp.activated is true AND sit.active is true AND sit.status!='D' AND emp.id NOT IN (SELECT employee FROM gesper.rh_droit_avancement WHERE exercice=" + exerciceID + " )" +
                    " AND ge.rythme_rapide<=TIMESTAMPDIFF(MONTH,sit.echelon_date,ex.fin)" +
                    " GROUP BY emp.id" +
                    " ORDER BY @rownum;";
            return query;
        } else {
            return query;
        }
    }

    public static final String UPDATE_DROIT_AVANCEMENT_SEQUENCE = "UPDATE gesper.rh_droit_avancement_seq " +
            " SET next_val = (SELECT (IFNULL(MAX(id),0)+1) next_val FROM gesper.rh_droit_avancement);";

    public static String GET_AFFECATATION_DECSION_BY_CODE(String code) {
        return " SELECT COUNT(afd.rh_affectation) AS nbr FROM gesper.config_decision AS d " +
                " JOIN gesper.rh_affectation_decision AS afd ON d.id=afd.decision " +
                " WHERE d.decision_code='" + code + "'";
    }

    public static String getListIDEmployeCongeDuEquipe(int idEntite,Long idemploye) {
        return "select e.id from  rh_affectation as a " +
                " left join rh_employe as e on a.employee= e.id " +
                " left join config_fonction as f on a.fonction=f.id " +
                " where a.entite=" + idEntite + " and a.type_affectation=1 and a.employee != "+idemploye;
    }

    public static String getListsidEntiteByParentId(Long idParent){
        return "SELECT DISTINCT id FROM config_entite where parent="+idParent;
    }

    public static String getAllSubordonnerByParentIds(String parentIds){
        return "SELECT DISTINCT id FROM `config_entite` where parent in (" +parentIds+")";
    }

    public static String getIdEntite(String matricule) {
        return "select a.entite from rh_affectation as a " +
                "left join rh_employe as e on e.id=a.employee " +
                "where e.matricule =" + matricule +
                " order by a.updated_on desc " +
                "limit 1";
    }

    public static String employeIsResponsable(String idEmploye) {

        return "select f.is_responsable from rh_affectation as a " +
                "left join config_fonction as f on f.id = a.fonction " +
                "left join rh_employe as e on a.employee=e.id " +
                "where a.employee= " + idEmploye + " and a.type_affectation=1 ";
    }

    public static String employeIsDRH(String idEmploye) {
        return "SELECT * FROM auth_user " +
                "where name="+ idEmploye+" and group_id=1"  ;
    }
}
