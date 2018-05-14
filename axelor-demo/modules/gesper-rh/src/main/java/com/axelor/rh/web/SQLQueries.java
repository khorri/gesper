package com.axelor.rh.web;

/**
 * Created by HB on 10/05/2018.
 */
public class SQLQueries  {

    public final String GET_LAST_MATRICULE_MYSQL = "SELECT CAST(MAX(e.matricule+0) as UNSIGNED) AS matricule FROM rh_employe e";

}
