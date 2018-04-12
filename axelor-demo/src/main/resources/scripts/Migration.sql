------ --------------------------GRADE MIGRATION -----------------------------------
INSERT INTO gesper.config_grade (id,name, echelle,tri,cadre,filiere) SELECT
grh_2208.grades.GRA_COD,
grh_2208.grades.GRA_LIB,
grh_2208.grades.GRA_ECH,
grh_2208.grades.GRA_TRI,
gesper.config_cadre.id as cadre,
gesper.config_filiere.id as filiere
FROM
grh_2208.grades
left Join gesper.config_filiere ON grh_2208.grades.FIL_COD = gesper.config_filiere.code
left Join gesper.config_cadre ON grh_2208.grades.CAD_COD = gesper.config_cadre.code
--------------------------------------------------------------------------------------------------------
-------------------- GRADE  ECHELON MIGRATION ------------------------------------
INSERT INTO gesper.config_grade_echelon (echelon,indice,rythme_lent,rythme_moyen,rythme_rapide,grade) SELECT
grh_2208.grille_indiciaire.ECHE_COD,
grh_2208.grille_indiciaire.GRII_IND,
grh_2208.echelons_grade.EPG_LEN,
grh_2208.echelons_grade.EPG_MOY,
grh_2208.echelons_grade.EPG_RAP,
grh_2208.echelons_grade.GRA_COD
FROM
grh_2208.grille_indiciaire
LEFT Join grh_2208.echelons_grade ON grh_2208.grille_indiciaire.GRA_COD = grh_2208.echelons_grade.GRA_COD AND grh_2208.grille_indiciaire.ECHE_COD = grh_2208.echelons_grade.ECHE_COD


