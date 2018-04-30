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
left Join gesper.config_cadre ON grh_2208.grades.CAD_COD = gesper.config_cadre.code;
--------------------------------------------------------------------------------------------------------
-------------------- GRADE  ECHELON MIGRATION ------------------------------------
set @id:=1;
INSERT INTO gesper.config_grade_echelon (id,echelon,indice,rythme_lent,rythme_moyen,rythme_rapide,grade) SELECT
(@id:=@id+1),
grh_2208.grille_indiciaire.ECHE_COD,
grh_2208.grille_indiciaire.GRII_IND,
grh_2208.echelons_grade.EPG_LEN,
grh_2208.echelons_grade.EPG_MOY,
grh_2208.echelons_grade.EPG_RAP,
grh_2208.echelons_grade.GRA_COD
FROM
grh_2208.grille_indiciaire
LEFT Join grh_2208.echelons_grade ON grh_2208.grille_indiciaire.GRA_COD = grh_2208.echelons_grade.GRA_COD AND grh_2208.grille_indiciaire.ECHE_COD = grh_2208.echelons_grade.ECHE_COD

#----- insert morocco if no exist
INSERT INTO gesper.base_country ( `id`, `name`, `version`, `alpha2code`, `alpha3code`, `full_name`, `name_ar`) 
SELECT @rownum := @rownum + 1 AS position, pseudo.*
	from (SELECT 'MAROC' as n, '1' as v, 'MA' as al2, 'MAR' as al3, 'MAROC - المغرب' as fn, 'المغرب' as na) pseudo
	JOIN (SELECT @rownum := (select next_val from gesper.base_country_seq)-1) as r
	where pseudo.n not in (select c.name from gesper.base_country c);

#----- updating sequence ALSO VERY IMPORTANT
UPDATE gesper.base_country_seq  set next_val = (SELECT id+1 as seq from gesper.base_country ORDER BY id desc LIMIT 1);

set @country:= (SELECT c.id from gesper.base_country  c where c.`name`='MAROC');

#----- inserting cities

INSERT INTO gesper.base_city 
(`id`,full_name,`name`,country,version,created_on) 
SELECT @rownum := @rownum + 1 AS position, a.AGE_VIL as fn, a.AGE_VIL as n,  c.country as ctr, '0' as version, now() as co 
FROM(
	SELECT AGE_VIL FROM grh.agents 
	where AGE_VIL is not null and AGE_VIL not like '' 
	and AGE_VIL not in (SELECT gc.`name` FROM gesper.base_city gc)
	GROUP BY AGE_VIL) as a
JOIN (SELECT @rownum := (select next_val from gesper.base_city_seq)-1) as r
JOIN (SELECT @country as country) as c;
#----- updating sequence ALSO VERY IMPORTANT
UPDATE gesper.base_city_seq  set next_val = (SELECT id+1 as seq from gesper.base_city ORDER BY id desc LIMIT 1);

#----- inserting addresses
INSERT INTO gesper.base_address (id,addressl4,city,addressl6,`full_name`,`version`,`created_on`)
SELECT @rownumm := @rownumm + 1 AS id, ag.AGE_ADR as l4, gcity.id as city, gcity.`name` as l6, CONCAT_WS('',ag.AGE_ADR,' - ',gcity.`name`) as fullname, '0' as version, now() as co
FROM(
	SELECT AGE_ADR, AGE_VIL  FROM grh.agents 
	where AGE_ADR is not null and AGE_ADR not like ''
	and AGE_ADR not in (SELECT ga.`addressl4` FROM gesper.base_address ga)
	GROUP BY AGE_ADR) as ag
left JOIN gesper.base_city as gcity ON ag.AGE_VIL = gcity.`name`
JOIN (SELECT @rownumm := (select next_val from gesper.base_address_seq)-1) as r;
#----- updating sequence ALSO VERY IMPORTANT
UPDATE gesper.base_address_seq  set next_val = (SELECT id+1 as seq from gesper.base_address ORDER BY id desc LIMIT 1);

#----- inserting employees
INSERT INTO gesper.rh_employe (
	id,	
	matricule,	cin,	title,	lastname,	firstname,	hire_date, 
	sex,	birthday,	birthplace,	nationality,	address,
	phone,	mobile,	activated,	radiation_date,	reprise_date,
	sex_ar ,`version`,`created_on`
) SELECT
	@rownum := @rownum + 1 AS id,
	ag.AGE_MAT, ag.AGE_CIN, ag.AGE_PREF, ag.AGE_PRE, ag.AGE_NOM, ag.AGE_DREC,
	ag.AGE_SEX, ag.AGE_DNAI, ag.AGE_LNAI, ag.AGE_NAT, gadr.id AS adrId,
	ag.AGE_TEL, ag.AGE_GSM, ag.AGE_ACT, ag.AGE_DRAD, ag.AGE_DREP,
	ag.AGE_SEX,	'0' as version, now() as co
FROM
	grh.agents ag
LEFT JOIN gesper.base_address gadr ON ag.age_adr = gadr.addressl4
JOIN (SELECT @rownum := (select next_val from gesper.rh_employe_seq)-1) as r
where ag.AGE_MAT not in (select rhe.matricule from gesper.rh_employe rhe);
#----- updating sequence ALSO VERY IMPORTANT
UPDATE gesper.rh_employe_seq  set next_val = (SELECT id+1 as seq from gesper.rh_employe ORDER BY id desc LIMIT 1);

