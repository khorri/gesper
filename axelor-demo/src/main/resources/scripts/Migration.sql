#-------------------------- Filière & cadre ---------------------
#-------------------------- By Ayoub ----------------------------
#-- Cadres
INSERT INTO gesper.config_cadre (`id`,`code`,`name`,`version`)
SELECT @rownum := @rownum + 1 AS position, c.CAD_COD, c.CAD_LIB, '0' AS v FROM grh.cadres AS c
		JOIN (SELECT @rownum := (SELECT next_val FROM gesper.config_cadre_seq)-1) as r
		WHERE c.CAD_COD NOT IN ( SELECT ex.code FROM gesper.config_cadre ex);

UPDATE gesper.config_cadre_seq  set next_val = (SELECT MAX(id)+1 as seq from gesper.config_cadre);

#-- Filieres
INSERT INTO gesper.config_filiere (`id`,`code`,`name`,`version`)
SELECT @rownum := @rownum + 1 AS position, f.FIL_COD, f.FIL_LIB, '0' AS v FROM grh.filieres AS f
		JOIN (SELECT @rownum := (SELECT next_val FROM gesper.config_filiere_seq)-1) as r
		WHERE f.FIL_COD NOT IN ( SELECT ex.code FROM gesper.config_filiere ex);

UPDATE gesper.config_filiere_seq  set next_val = (SELECT MAX(id)+1 as seq from gesper.config_filiere);
#-----------------------------------------------------------------------------------------------------
#-- ---- --------------------------GRADE MIGRATION ---------------------------------------------------
#-----------------------------------------------------------------------------------------------------
INSERT INTO gesper.config_grade (id,name, echelle,tri,cadre,filiere) SELECT
grh.grades.GRA_COD,
grh.grades.GRA_LIB,
grh.grades.GRA_ECH,
grh.grades.GRA_TRI,
gesper.config_cadre.id as cadre,
gesper.config_filiere.id as filiere
FROM
grh.grades
left Join gesper.config_filiere ON grh.grades.FIL_COD = gesper.config_filiere.code
left Join gesper.config_cadre ON grh.grades.CAD_COD = gesper.config_cadre.code;
UPDATE gesper.config_grade_seq  set next_val = (SELECT id+1 as seq from gesper.config_grade ORDER BY id desc LIMIT 1);
-- ------------------------------------------------------------------------------------------------------
-- ------------------ GRADE  ECHELON MIGRATION ------------------------------------
set @id:=1;
INSERT INTO gesper.config_grade_echelon (id,echelon,indice,rythme_lent,rythme_moyen,rythme_rapide,grade) SELECT
(@id:=@id+1),
grh.grille_indiciaire.ECHE_COD,
grh.grille_indiciaire.GRII_IND,
grh.echelons_grade.EPG_LEN,
grh.echelons_grade.EPG_MOY,
grh.echelons_grade.EPG_RAP,
grh.echelons_grade.GRA_COD
FROM
grh.grille_indiciaire
LEFT Join grh.echelons_grade ON grh.grille_indiciaire.GRA_COD = grh.echelons_grade.GRA_COD AND grh.grille_indiciaire.ECHE_COD = grh.echelons_grade.ECHE_COD;
UPDATE gesper.config_grade_echelon_seq  set next_val = (SELECT id+1 as seq from gesper.config_grade_echelon ORDER BY id desc LIMIT 1);


#----- insert morocco if not exist ------- --
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
	sex_ar ,`version`,`created_on`, `name`
) SELECT
	@rownum := @rownum + 1 AS id,
	ag.AGE_MAT, ag.AGE_CIN, ag.AGE_PREF, ag.AGE_PRE, ag.AGE_NOM, ag.AGE_DREC,
	ag.AGE_SEX, ag.AGE_DNAI, ag.AGE_LNAI, ag.AGE_NAT, gadr.id AS adrId,
	ag.AGE_TEL, ag.AGE_GSM, ag.AGE_ACT, ag.AGE_DRAD, ag.AGE_DREP,
	ag.AGE_SEX,	'0' as version, now(), CONCAT(ag.AGE_PRE,' ',ag.AGE_NOM) as co
FROM
	grh.agents ag
LEFT JOIN gesper.base_address gadr ON ag.age_adr = gadr.addressl4
JOIN (SELECT @rownum := (select next_val from gesper.rh_employe_seq)-1) as r
where ag.AGE_MAT not in (select rhe.matricule from gesper.rh_employe rhe);
#----- updating sequence ALSO VERY IMPORTANT
UPDATE gesper.rh_employe_seq  set next_val = (SELECT id+1 as seq from gesper.rh_employe ORDER BY id desc LIMIT 1);



#----- PART II




#----- inserting gespaie cities
set @country:= (SELECT c.id from gesper.base_country  c where c.`name`='MAROC');
INSERT INTO gesper.base_city 
(`id`,full_name,`name`,country,version,created_on) 
SELECT @rownum := @rownum + 1 AS position, v.VILLE_NOM as fn, v.VILLE_NOM as n, c.country as ctr, '0' as version, now() as co
FROM (SELECT v.ville_nom from gespaie.villes v  where v.VILLE_NOM not in (select UPPER(`name`) from gesper.base_city)) as v
JOIN (SELECT @rownum := (select next_val from gesper.base_city_seq)-1) as r
JOIN (SELECT @country as country) as c;

UPDATE gesper.base_city_seq  set next_val = (SELECT id+1 as seq from gesper.base_city ORDER BY id desc LIMIT 1);

#----- inserting grh organismes
INSERT INTO gesper.config_organisme ( `id`, `name`, `version`, `short_name`, `type`) 
SELECT @rownum := @rownum + 1 AS position, org.OGA_LIB libelle, '0' as v, org.OGA_COD, org.OGA_TYP  
	from grh.organisme org
	JOIN (SELECT @rownum := (select next_val from gesper.config_organisme_seq)-1) as r
	where org.OGA_COD not in (select c.short_name from gesper.config_organisme c);
#----- updating organismes sequence 
UPDATE gesper.config_organisme_seq  set next_val = (SELECT id+1 as seq from gesper.config_organisme ORDER BY id desc LIMIT 1);


#----- more organismes possible headache (for me)
#----- organisme BANQUE' this one's easy
INSERT INTO gesper.config_organisme ( `id`, `name`, `short_name`, `type`, `version`) 
SELECT @rownum := @rownum + 1 AS position, bank.BANQ_NOM, bank.BANQ_ABERV, 'BANQUE' as type,'0' as v
	from gespaie.banques bank
	where bank.BANQ_ABERV not in (select c.short_name from gesper.config_organisme c);

UPDATE gesper.config_organisme_seq  set next_val = (SELECT id+1 as seq from gesper.config_organisme ORDER BY id desc LIMIT 1);
#----- agence banqaire INSERT(ed whatever i wanted, because unique keys)
INSERT INTO gesper.config_organisme ( `id`, `name`, `short_name`, `type`, `version`, `city`, `banque`) 
SELECT @rownum := @rownum+1  AS position, CONCAT_WS(" | ",a.AGCE_NOM, gb.BANQ_ABERV) as `name` , CONCAT_WS(" | ",a.AGCE_NOM, gb.BANQ_ABERV) as short, 'AGENCE_BANCAIRE' as type, '0' as v, gc.id, bank.id FROM gespaie.`agences_bancaires` a
left join gespaie.villes v on v.VILLE_CODE = a.VILLE_CODE
left join gesper.base_city gc on gc.name = v.VILLE_NOM
left join gespaie.banques gb on gb.BANQ_CODE = a.BANQ_CODE
left join gesper.config_organisme bank on bank.short_name = gb.BANQ_ABERV
JOIN (SELECT @rownum := (select next_val from gesper.config_organisme_seq)-1) as r
where CONCAT_WS(" | ",a.AGCE_NOM, gb.BANQ_ABERV) not in (select c.short_name from gesper.config_organisme c);

UPDATE gesper.config_organisme_seq  set next_val = (SELECT id+1 as seq from gesper.config_organisme ORDER BY id desc LIMIT 1);

#----- gespaie organismes 
#----- 1 gespaie organismes angences 
INSERT INTO gesper.config_organisme ( `id`, `name`, `short_name`, `version`, `type`)
SELECT @rownum := @rownum + 1 AS position, agnc.* FROM (SELECT o.ORGA_AGENCE as n, o.ORGA_AGENCE as sn,'0' as v, 'AGENCE_BANCAIRE' as type from gespaie.organismes o
where o.ORGA_AGENCE is not null and o.ORGA_AGENCE!=''
and o.ORGA_AGENCE not in (select go.`name` from gesper.config_organisme go)
GROUP BY o.ORGA_AGENCE) agnc
JOIN (SELECT @rownum := (select next_val from gesper.config_organisme_seq)-1) as r;

UPDATE gesper.config_organisme_seq  set next_val = (SELECT id+1 as seq from gesper.config_organisme ORDER BY id desc LIMIT 1);

#----- 2 gespaie organismes
INSERT INTO gesper.config_organisme ( `id`, `name`, short_name, `rib`, `type`, `version`, `city`, `banque`,`agence_banquaire`) 
SELECT @rownum := @rownum+1 as rank, complicated.*
FROM ( SELECT 
	IF(dupe='1', CONCAT_WS('',org.ORGA_LIBELLE,'2') ,org.ORGA_LIBELLE) as label,
	IF(dupe='1', CONCAT_WS('',org.ORGA_ABREV,'2') ,org.ORGA_ABREV) as short
	, IF(org.ORGA_RIB='',null,org.ORGA_RIB) as rib, '' as type, '0' as v, gc.id as vil, bank.id as bnq
	, agce.id as agence
FROM gespaie.organismes org
left outer join gespaie.villes v on v.VILLE_CODE = org.VILLE_CODE
left outer join gesper.base_city gc on gc.`name` = v.VILLE_NOM
left outer join gespaie.banques gb on gb.BANQ_CODE = org.BANQ_CODE
left outer join gesper.config_organisme bank on bank.short_name = gb.BANQ_ABERV 
left outer join gesper.config_organisme agce on agce.`name`= org.ORGA_AGENCE
left outer join (
	SELECT o.ORGA_CODE, '1' as dupe from gespaie.organismes o 
	where o.ORGA_CODE IN(SELECT MAX(o.ORGA_CODE) from gespaie.organismes o GROUP BY ORGA_LIBELLE having count(*)>1)
) dupes on dupes.ORGA_CODE = org.ORGA_CODE 
GROUP BY org.ORGA_LIBELLE having short not in (select c.`name` from gesper.config_organisme c) AND label not in (select c.`name` from gesper.config_organisme c)) complicated
JOIN (SELECT @rownum := (select next_val from gesper.config_organisme_seq)-1) as r;

UPDATE gesper.config_organisme_seq  set next_val = (SELECT id+1 as seq from gesper.config_organisme ORDER BY id desc LIMIT 1);

#----- inserting grh retraites
INSERT INTO gesper.rh_retraite ( `id`, `affiliation_num`, `affiliation_date`, `employee`, `organisme`, `version`) 
SELECT @rownum := @rownum + 1 AS position, ret.RET_NUM , ret.RET_DDEB, emp.id as empId, org.id as orgId , '0' as v
	from grh.retraites ret 
	JOIN gesper.rh_employe as emp ON emp.matricule = ret.AGE_MAT 
	JOIN gesper.config_organisme as org ON org.short_name = ret.OGA_COD 
	JOIN (SELECT @rownum := (select next_val from gesper.rh_retraite_seq)-1) as r
where ret.RET_NUM not in (select c.affiliation_num from gesper.rh_retraite c);
#----- updating retraites sequence 
UPDATE gesper.rh_retraite_seq  set next_val = (SELECT id+1 as seq from gesper.rh_retraite ORDER BY id desc LIMIT 1);
#----- linking retraites to employee 
UPDATE gesper.rh_employe em,
(select r.id, r.employee from gesper.rh_retraite r) as rhr
set em.retraite = rhr.id
where em.id = rhr.employee;

#----- inserting grh mutuelles
INSERT INTO gesper.rh_mutuelle ( `id`, `affiliation_num`, `mutuelle_num`,`affiliation_date`, `employee`, `organisme`, `version`) 
SELECT @rownum := @rownum + 1 AS position, mut.MUT_AFF, mut.MUT_IMM , mut.MUT_DDEB, emp.id as empId, org.id as orgId , '0' as v
	from grh.mutuelles mut
	JOIN gesper.rh_employe as emp ON emp.matricule = mut.AGE_MAT
	JOIN gesper.config_organisme as org ON org.short_name = mut.OGA_COD 
	JOIN (SELECT @rownum := (select next_val from gesper.rh_mutuelle_seq)-1) as r
where mut.MUT_AFF not in (select c.affiliation_num from gesper.rh_mutuelle c);
#----- updating mutuelles sequence 
UPDATE gesper.rh_mutuelle_seq  set next_val = (SELECT id+1 as seq from gesper.rh_mutuelle ORDER BY id desc LIMIT 1);
#----- linking mutuelles to employee 
UPDATE gesper.rh_employe em,
(select m.id, m.employee from gesper.rh_mutuelle m) as rhm
set em.mutuelle = rhm.id
where em.id = rhm.employee;

#----- retraites_complementaire

INSERT INTO gesper.rh_retraite_complementaire (`id`, `date_debut`, `affiliation_num`, `employee`, `organisme`)
SELECT @rownum := @rownum + 1 AS position,
ret.REFRC_DATE, ret.REFRC_AFFILIATION, emp.id as employee, orgg.id as organisme FROM gespaie.`ref_retraite_comp` ret
join gesper.rh_employe emp on ret.AGEN_MATR = emp.matricule
join gespaie.organismes org on org.ORGA_CODE = ret.ORGA_CODE
join gesper.config_organisme orgg on orgg.short_name = org.ORGA_ABREV
JOIN (SELECT @rownum := (select next_val from gesper.rh_retraite_complementaire_seq)-1) as r
WHERE ret.REFRC_AFFILIATION not in (SELECT rhrt.affiliation_num from gesper.rh_retraite_complementaire rhrt);

UPDATE gesper.rh_retraite_complementaire_seq  
set next_val = IF((SELECT id+1 as seq from gesper.rh_retraite_complementaire ORDER BY id desc LIMIT 1) is null,1,(SELECT id+1 as seq from gesper.rh_retraite_complementaire ORDER BY id desc LIMIT 1));


#----- fonctions 
INSERT INTO gesper.config_fonction (`id`, `name`, `is_responsable`,`version`)
SELECT @rownum := @rownum + 1 AS position, foncts.*, '0' as v
FROM (
	SELECT f.FON_LIB as l, f.FON_RES as r from grh.fonctions f where f.FON_LIB not in (SELECT `name` from gesper.config_fonction)
	union 
	select ff.FONC_LIBELLE as l, ff.FLAG_RESP as r from gespaie.fonctions ff 
		where FONC_CODE not in (SELECT FON_COD from grh.fonctions) AND ff.FONC_LIBELLE not in (SELECT `name` from gesper.config_fonction)
) foncts 
JOIN (SELECT @rownum := (select next_val from gesper.config_fonction_seq)-1) as r
group by foncts.l;

UPDATE gesper.config_fonction_seq  set next_val = (SELECT id+1 as seq from gesper.config_fonction ORDER BY id desc LIMIT 1);


#----- services 
INSERT INTO gesper.config_service (`id`,`short_name`, `name`, `version`)
SELECT @rownum := @rownum + 1 AS position, serv.*, '0' as v from grh.services serv
JOIN (SELECT @rownum := (select next_val from gesper.config_service_seq)-1) as r
where serv.SER_COD not in ( select sr.short_name from gesper.config_service sr);

UPDATE gesper.config_service_seq  set next_val = (SELECT id+1 as seq from gesper.config_service ORDER BY id desc LIMIT 1);

#----- type entites

INSERT INTO gesper.config_type_entite (`id`,`short_name`, `name`, `version`)
SELECT @rownum := @rownum + 1 AS position, te.*, '0' as v from grh.types_entites te
JOIN (SELECT @rownum := (select next_val from gesper.config_type_entite_seq)-1) as r
where te.TYPENT_COD not in ( select cte.short_name from gesper.config_type_entite cte);

UPDATE gesper.config_type_entite_seq  set next_val = (SELECT id+1 as seq from gesper.config_type_entite ORDER BY id desc LIMIT 1);

#----- provinces
INSERT INTO gesper.config_province (`id`,`code`, `name`, `version`)
SELECT @rownum := @rownum + 1 AS position, pr.*, '0' as v from grh.provinces pr
JOIN (SELECT @rownum := (select next_val from gesper.config_province_seq)-1) as r
where pr.PRO_COD not in ( select gpr.`code` from gesper.config_province gpr);

UPDATE gesper.config_province_seq  set next_val = (SELECT id+1 as seq from gesper.config_province ORDER BY id desc LIMIT 1);

#----- caida 

INSERT INTO gesper.config_caida (`id`,`code`, `name`, `province`, `version`)
SELECT @rownum := @rownum + 1 AS position, ca.CAI_COD, ca.CAI_LIB, prov.id, '0' as v from grh.caida ca
left join gesper.config_province prov on ca.PRO_COD = prov.`code`
JOIN (SELECT @rownum := (select next_val from gesper.config_caida_seq)-1) as r
where ca.CAI_COD not in ( select gca.`code` from gesper.config_caida gca);

UPDATE gesper.config_caida_seq  set next_val = (SELECT id+1 as seq from gesper.config_caida ORDER BY id desc LIMIT 1);

#----- entite 
INSERT INTO gesper.config_entite(`id`,`short_name`, `name`, `organigrame`, `organigrame_officiel`, `caida`, `type`, `version`)
SELECT @rownum := @rownum + 1 AS position,
 eno.ENT_COD, eno.ENT_LIB, 
IF(eno.ENT_FLAG_ORGA='Oui',1,0) as org, IF(eno.ENT_FLAG_ORGA_OFFICIEL='Oui',1,0) as orgoff ,
ca.id as caida, te.id as type , '0' as v from grh.entites_ormvah eno
left join gesper.config_caida ca on ca.`code` = eno.CAI_COD
left join gesper.config_type_entite te on eno.TYPENT_COD = te.short_name
JOIN (SELECT @rownum := (select next_val from gesper.config_entite_seq)-1) as r
where eno.ENTENT_COD not in ( select ent.`short_name` from gesper.config_entite ent);

UPDATE gesper.config_entite_seq  set next_val = (SELECT id+1 as seq from gesper.config_entite ORDER BY id desc LIMIT 1);

UPDATE gesper.config_entite cce,
(SELECT ce.id child , ce2.id parent from gesper.config_entite ce
left join grh.entites_ormvah eo on eo.ENT_COD = ce.short_name
left join gesper.config_entite ce2 on ce2.short_name = eo.ENTENT_COD) as pce
  SET cce.parent = pce.parent
WHERE cce.id = pce.child;

#----- so entite can have multiple caidas
Update gesper.config_caida ca, gesper.config_entite en
set ca.entite = en.id
where ca.id = en.caida;
#----- enfants

INSERT INTO gesper.rh_enfant (`id`, `name`, `date_naissance`, `employee`, `scolarise`, `infirme`, `date_deces`, `version`)
SELECT @rownum := @rownum + 1 AS position, enfs.a, enfs.b, enfs.c, enfs.d, enfs.e, enfs.f, '0' as v FROM (
SELECT 
CONCAT_WS(" ",emp.id,e.ENF_NOM, DATE(e.ENF_DNAI)) as fl,
e.ENF_NOM as a, e.ENF_DNAI as b, emp.id as c, e.ENF_SCO as d, e.ENF_INF as e, e.ENF_DDEC as f FROM 
	(SELECT * FROM grh.`enfants` e
		union
	SELECT * FROM grh.enfants2) e
join gesper.rh_employe emp  on emp.matricule = e.AGE_MAT
having fl not in (SELECT CONCAT_WS(" ", rhe.employee, rhe.`name`, rhe.date_naissance) as fl from gesper.rh_enfant rhe)
) enfs
JOIN (SELECT @rownum := (select next_val from gesper.rh_enfant_seq)-1) as r;
UPDATE gesper.rh_enfant_seq  set next_val = (SELECT id+1 as seq from gesper.rh_enfant ORDER BY id desc LIMIT 1);


#----- localites 

INSERT INTO gesper.config_localite (`id`, `name`, `version`)
SELECT @rownum := @rownum + 1 AS position, pl.LOCALITE, '0' as v FROM grh.p_localite pl
JOIN (SELECT @rownum := (select next_val from gesper.config_localite_seq)-1) as r
WHERE pl.LOCALITE not in (SELECT `name` from gesper.config_localite);

UPDATE gesper.config_localite_seq  set next_val = (SELECT id+1 as seq from gesper.config_localite ORDER BY id desc LIMIT 1);


#----- zones

INSERT INTO gesper.config_zone (`id`, `name`, `version`)
SELECT @rownum := @rownum + 1 AS position, z.ZON_LIB, '0' as v FROM grh.zones z
JOIN (SELECT @rownum := (select next_val from gesper.config_zone_seq)-1) as r
WHERE z.ZON_LIB not in (SELECT `name` from gesper.config_zone);

UPDATE gesper.config_zone_seq  set next_val = (SELECT id+1 as seq from gesper.config_zone ORDER BY id desc LIMIT 1);


#----- residences

INSERT INTO gesper.config_residence (`id`, `name`, `zone`, `localite`, `version`)
SELECT @rownum := @rownum + 1 AS position, res.RES_LIB, gcz.id, gcl.id, '0' as v FROM grh.residences res
    left join grh.zones gz on gz.ZON_COD =  res.ZON_COD
    left join gesper.config_zone gcz on gcz.`name` = gz.ZON_LIB
    left join grh.p_localite gpl on gpl.CDE_LOC =  res.LOC_COD
    left join gesper.config_localite gcl on gcl.`name` = gpl.LOCALITE


JOIN (SELECT @rownum := (select next_val from gesper.config_residence_seq)-1) as r
WHERE res.RES_LIB not in (SELECT `name` from gesper.config_residence);

UPDATE gesper.config_residence_seq  set next_val = (SELECT id+1 as seq from gesper.config_residence ORDER BY id desc LIMIT 1);


#----- Exercice
# By Khalid

INSERT INTO gesper.config_exercice (`id`,`name`, `debut`, `fin`,`status`,`version`)
	SELECT @rownum := @rownum + 1 AS position, exercice.*, '0' as v from gespaie.exercice exercice
		JOIN (SELECT @rownum := (select next_val from gesper.config_exercice_seq)-1) as r
	where exercice.EXER_CODE not in ( select ex.name from gesper.config_exercice ex);

UPDATE gesper.config_exercice_seq  set next_val = (SELECT id+1 as seq from gesper.config_exercice ORDER BY id desc LIMIT 1);

# ----------- Migration des NOTES ----------------
# By Khalid
# TODO Ajouter la personne et la date de saisie

INSERT INTO gesper.`rh_note` (`id`,  `note_avancement`, `note_prime`, `note_final`,  `employee`, `entite`, `exercice`, `version`)
	SELECT
		@rownum := @rownum + 1 AS position,
		grh.notes.NOT_AVA,
		grh.notes.NOT_PRI,
		grh.notes.NOT_PRIATT,
		gesper.rh_employe.id as agent,
		gesper.config_entite.id as entite,
		gesper.config_exercice.id as exercice,
		'0'
	FROM
		grh.notes
		JOIN (SELECT @rownum := (select next_val from gesper.rh_note_seq)-1) as r
		left Join gesper.config_exercice ON grh.notes.EXE_COD = gesper.config_exercice.name
		left Join gesper.config_entite ON grh.notes.entite = gesper.config_entite.short_name
		left Join gesper.rh_employe ON grh.notes.AGE_MAT = gesper.rh_employe.matricule;


UPDATE gesper.rh_note_seq  set next_val = (SELECT id+1 as seq from gesper.rh_note ORDER BY id desc LIMIT 1);

#-------Migration des types de conge ---------------
# By Ayoub

INSERT INTO gesper.rh_type_conge (`id`,`code`, `name`,`version`)
	SELECT
	    @rownum := @rownum + 1 AS position,
	    type_conge.*,
	    '0' AS v
	FROM grh.type_conges AS type_conge
	JOIN (SELECT @rownum := (SELECT next_val FROM gesper.rh_type_conge_seq)-1) as r
	WHERE type_conge.TYPC_COD NOT IN ( SELECT ex.code FROM gesper.rh_type_conge ex);

UPDATE gesper.rh_type_conge_seq  set next_val = (SELECT MAX(id)+1 as seq from gesper.rh_type_conge);

#-----------Migration des natures conges -------------
#By Ayoub
INSERT INTO gesper.rh_nature_conge (`id`,`code`, `name`,`droit_jour`,`version`,`type_conge`)
	SELECT
	    @rownum := @rownum + 1 AS position,
	    nc.NAT_COD,nc.NAT_LIB,
	    nc.NAT_DRO,
	    '0' AS v,
	    rtc.id
  FROM grh.natures_conges AS nc
  JOIN (SELECT @rownum := (SELECT next_val FROM gesper.rh_nature_conge_seq)-1) as r
  JOIN gesper.rh_type_conge AS rtc ON rtc.code=nc.TYPC_COD
  WHERE nc.NAT_COD NOT IN ( SELECT ex.code FROM gesper.rh_nature_conge ex);

UPDATE gesper.rh_nature_conge_seq  set next_val = (SELECT MAX(id)+1 as seq from gesper.rh_nature_conge);

#-----------Migration des joures ferie ------------------------
#By Ayoub
INSERT INTO gesper.rh_jour_ferie (`id`,`date_debut`, `date_fin`,`name`,`version`)
	SELECT
	    @rownum := @rownum + 1 AS position,
	    jf.*, '0' AS v
	    FROM gespaie.jours_feriers AS jf
	JOIN (SELECT @rownum := (SELECT next_val FROM gesper.rh_jour_ferie_seq)-1) as r
	WHERE CONCAT(DATE_FORMAT(jf.JRSF_D_DEBUT, '%Y-%m-%d'),' ',DATE_FORMAT(jf.JRSF_D_FIN, '%Y-%m-%d'))
	NOT IN ( SELECT CONCAT(DATE_FORMAT(ex.date_debut, '%Y-%m-%d'),' ',DATE_FORMAT(ex.date_fin, '%Y-%m-%d')) FROM gesper.rh_jour_ferie ex);

UPDATE gesper.rh_jour_ferie_seq  set next_val = (SELECT MAX(id)+1 as seq from gesper.rh_jour_ferie);

#------------Migration des autorisation d'absence ----------------
#By Ayoub

INSERT INTO gesper.rh_autorisation_absence (`id`,`date_absence`,`duree`, `observation`,`imputable`,`employee`,`version`)
SELECT @rownum := @rownum + 1 AS position, abs.AUT_DAT, abs.AUT_DUR, abs.AUT_OBS, abs.AUT_IMP, emp.id, '0' AS v FROM grh.autorisations_absence AS abs
		JOIN (SELECT @rownum := (SELECT next_val FROM gesper.rh_autorisation_absence_seq)-1) as r
		JOIN gesper.rh_employe as emp on emp.matricule=abs.AGE_MAT
		WHERE CONCAT(DATE_FORMAT(abs.AUT_DAT, '%Y-%m-%d'),'#',emp.id,'#',abs.AUT_IMP)
		NOT IN ( SELECT CONCAT(DATE_FORMAT(ex.date_absence, '%Y-%m-%d'),'#',ex.employee,'#',ex.imputable) FROM gesper.rh_autorisation_absence ex);

UPDATE gesper.rh_autorisation_absence_seq  set next_val = (SELECT MAX(id)+1 as seq from gesper.rh_autorisation_absence);

#------------Migration des droits d'avancement------------------
#By Ayoub

INSERT INTO gesper.rh_droit_avancement (
	`id`,
	`numero`,
	`note_avancement`,
	`coeff_avancement_rapide`,
	`date_avancement`,
	`rythme_avancement`,
	`exercice_avancement`,
	`exercice`,
	`employee`,
	`grade`,
	`echelon`,
	`new_echelon`,
	`new_indice`,
	`statut`,
	`type_avancement`,
	`date_grade`,
	`date_echelon`,
	`version`
)
	SELECT
		@rownum := @rownum + 1 AS position,
		CONCAT(da.AVA_NUM,'/',da.EXE_COD),
		da.AVA_NOT,
		ech.rythme_rapide,
		da.AVA_DAT,
		da.AVA_RYT,
		da.AVA_EXE,
		ex.id exercice,
		emp.id employe,
		g.id grade,
		ech.id echelon,
		da.N_ECHE,
		da.N_IND,
		'0' statut,
		'INT' type_avancement,
		da.SIT_DGRA date_grade,
		da.SIT_DANC date_echelon,
		'0' version
	FROM grh.droit_avancement da
		JOIN (SELECT @rownum := (select next_val from gesper.rh_droit_avancement_seq)-1) as r
		LEFT JOIN gesper.rh_employe emp on da.AGE_MAT = emp.matricule
		LEFT JOIN gesper.config_exercice ex on ex.name = da.EXE_COD
		LEFT JOIN gesper.config_grade g on g.id=da.GRA_COD
		LEFT JOIN gesper.config_grade_echelon ech on ech.echelon=da.ECHE_COD AND ech.grade=g.id
	WHERE CONCAT(da.AVA_NUM,'/',da.EXE_COD) NOT IN (SELECT d.numero FROM gesper.rh_droit_avancement as d) ;

UPDATE gesper.rh_droit_avancement_seq
set next_val = IF((SELECT id+1 as seq from gesper.rh_droit_avancement ORDER BY id desc LIMIT 1) is null,1,(SELECT id+1 as seq from gesper.rh_droit_avancement ORDER BY id desc LIMIT 1));

INSERT INTO gesper.rh_droit_avancement (
	`id`,
	`numero`,
	`coeff_avancement_rapide`,
	`date_avancement`,
	`rythme_avancement`,
	`employee`,
	`grade`,
	`echelon`,
	`decision`,
	`arrete_ministerial_numero`,
	`arrete_ministerial_date`,
	`type_avancement`,
	`exercice`,
	`statut`,
	`date_grade`,
	`date_echelon`,
	`version`
)
	SELECT
		@rownum := @rownum + 1 AS position,
		da.AVA_NUM,
		ech.rythme_rapide,
		da.AVA_DAT,
		da.AVA_RYT,
		emp.id employe,
		g.id grade,
		ech.id echelon,
		dc.id decision,
		da.ARR_NUM,
		da.ARR_DAT,
		da.AVA_TYP,
		exe.id,
		'1' statut,
		da.AVA_DGRA date_grade,
		da.AVA_DECH date_echelon,
		'0' version
	FROM grh.avancements da
		JOIN (SELECT @rownum := (select next_val from gesper.rh_droit_avancement_seq)-1) as r
		LEFT JOIN gesper.rh_employe emp on da.AGE_MAT = emp.matricule
		LEFT JOIN gesper.config_grade g on g.id=da.GRA_COD
		LEFT JOIN gesper.config_grade_echelon ech on ech.echelon=da.ECHE_COD AND ech.grade=g.id
		LEFT JOIN gesper.config_decision dc on dc.decision_code=da.DEC_NUM
		LEFT JOIN gesper.config_exercice exe on exe.name=RIGHT(da.AVA_NUM,4)
	WHERE da.AVA_NUM NOT IN (SELECT d.numero FROM gesper.rh_droit_avancement as d);
UPDATE gesper.rh_droit_avancement_seq
set next_val = IF((SELECT id+1 as seq from gesper.rh_droit_avancement ORDER BY id desc LIMIT 1) is null,1,(SELECT id+1 as seq from gesper.rh_droit_avancement ORDER BY id desc LIMIT 1));

UPDATE gesper.rh_droit_avancement av, (SELECT a.AVA_NUM numero, d.id decision FROM grh.avancements as a
	JOIN gesper.config_decision as d on d.decision_code=a.DEC_NUM
WHERE a.AVA_NUM IN (SELECT AVA_NUM FROM grh.droit_avancement)) as avn set av.decision=avn.decision, av.statut=1 where av.numero=avn.numero;

#------------------------------------------------------------------------------------
#------------------------------- Migration des decisions ----------------------------
#------------------------------------------------------------------------------------
#--By ayoub
INSERT INTO gesper.config_decision (`id`,`decision_code`,`decision_date`, `emitteur`,`version`, status)
SELECT @rownum := @rownum + 1 AS position, d.DEC_NUM, d.DEC_DAT, ent.id entite, '0' AS v, '3' as status FROM grh.decisions AS d
		JOIN (SELECT @rownum := (SELECT next_val FROM gesper.config_decision_seq)-1) as r
		LEFT JOIN gesper.config_entite ent ON ent.short_name=d.DEC_EME
		WHERE d.DEC_NUM
		NOT IN ( SELECT IFNULL(ex.decision_code,"no code, lol") FROM gesper.config_decision ex);
UPDATE gesper.config_decision_seq  set next_val = (SELECT MAX(id)+1 as seq from gesper.config_decision);
#------------------------------------------------------------------------------------
#------------------------------- Migration des situation ----------------------------
#------------------------------------------------------------------------------------
#--By ayoub

INSERT INTO gesper.rh_situation (
	`id`,
	`situation_date`,
	`nature`,
	`avancement`,
	`type`,
	`active`,
	`employee`,
	`grade`,
	`decision`,
	`status`,
	`grade_date`,
	`echelon_date`,
	`version`
)
	SELECT
		@rownum := @rownum + 1 AS position,
		st.SIT_DAT date,
		st.SIT_NAT nature,
		av.id avancement,
		st.SIT_TYP type,
		st.SIT_ACT active,
		emp.id employee,
		ge.id grade,
		dc.id decision,
		sts.STA_COD status,
		st.SIT_DGRA date_grade,
		st.SIT_DANC date_echelon,
		'0' version
	FROM grh.situation_agent st
		JOIN (SELECT @rownum := (select next_val from gesper.rh_situation_seq)-1) as r
		LEFT JOIN gesper.rh_employe emp on st.AGE_MAT = emp.matricule
		LEFT JOIN gesper.config_grade_echelon ge on ge.echelon=st.ECHE_COD AND ge.grade=st.GRA_COD
		LEFT JOIN gesper.config_decision as dc on dc.decision_code=st.DEC_NUM
		LEFT JOIN gesper.rh_droit_avancement as av on av.numero=st.AVA_NUM
		LEFT JOIN grh.status_agent as sts on sts.AGE_MAT=st.AGE_MAT
	WHERE CONCAT(IF(emp.id is null,'',emp.id),IF(ge.id is null,'',ge.id),IF(DATE_FORMAT(st.SIT_DAT,'%Y-%m-%d') is null,'',DATE_FORMAT(st.SIT_DAT,'%Y-%m-%d')),IF(dc.id is null,'',dc.id))
				NOT IN (SELECT CONCAT(IF(d.employee is null,'',d.employee),IF(d.grade is null,'',d.grade),IF(DATE_FORMAT(d.situation_date,'%Y-%m-%d') is null,'',DATE_FORMAT(d.situation_date,'%Y-%m-%d')),IF(d.decision is null,'',d.decision)) as ID FROM gesper.rh_situation as d);
UPDATE gesper.rh_situation_seq
set next_val = IF((SELECT id+1 as seq from gesper.rh_situation ORDER BY id desc LIMIT 1) is null,1,(SELECT id+1 as seq from gesper.rh_situation ORDER BY id desc LIMIT 1));

#---- Affectation a nightmare . i pity the person reading this query


INSERT INTO gesper.rh_affectation (id, employee, entite, service, residence, fonction, caida, affectation_date, observation, status,version, type_affectation)
SELECT
@rownum := @rownum + 1 AS position,
emp.id as employee,
en.id as entite,
serv.id as service,
res.id as residence,
fo.id as fonction,
gcai.id as caida,
af.AFF_DAT,
af.FON_OBS,
'3' as stat,
'0' as v,
(case when af.AFF_ACT = 1 then '1'
      when af.AFF_ACT = 0 AND af.FLAG_ABROGEE like 'Non' then '2'
      else '3' 
end) as type_affectation
FROM grh.`affectations` af
JOIN (SELECT @rownum := (SELECT next_val FROM gesper.rh_affectation_seq)-1) as r
left join gesper.config_entite en on en.short_name = af.ENT_COD
left join grh.residences resid on resid.RES_COD = af.RES_COD
left join gesper.config_residence res on res.name = resid.RES_LIB
left join gesper.config_service serv on af.SER_COD = serv.short_name
left join gesper.rh_employe emp on emp.matricule = af.AGE_MAT
left join grh.fonctions gfon on gfon.FON_COD = af.FON_COD
left join gesper.config_fonction fo on fo.`name` = gfon.FON_LIB
left join grh.entites_ormvah ent on ent.ENT_COD = af.ENT_COD
left join grh.caida cai on cai.CAI_COD = ent.CAI_COD
left join gesper.config_caida gcai on gcai.`name` = cai.CAI_LIB
#--- *sigh* ↓ this is because duplicate decisions 
where CONCAT_ws("",emp.id, DATE_FORMAT(af.AFF_DAT,'%Y-%m-%d'), fo.id,en.id,serv.id) not in (select CONCAT_WS("",employee, DATE_FORMAT(affectation_date,'%Y-%m-%d'), fonction,entite,service) from gesper.rh_affectation) ;

UPDATE gesper.rh_affectation_seq  set next_val = (SELECT IF(MAX(id) is null,1,MAX(id)+1) as seq from gesper.rh_affectation);

INSERT INTO gesper.rh_affectation_decision ( decision,rh_affectation)
SELECT pseudo.decision, gaf.id FROM 
(SELECT CONCAT_ws("",emp.id, DATE_FORMAT(af.AFF_DAT,'%Y-%m-%d'), fo.id,en.id,serv.id,(case when af.AFF_ACT = 1 then '1'
      when af.AFF_ACT = 0 AND af.FLAG_ABROGEE like 'Non' then '2'
      else '3' 
end)) as unique_field,d.id as decision
from grh.affectations af
left join (select  decision_code,id from gesper.config_decision group by decision_code) d on d.decision_code = af.DEC_NUM
left join gesper.config_entite en on en.short_name = af.ENT_COD
left join gesper.config_service serv on af.SER_COD = serv.short_name
left join gesper.rh_employe emp on emp.matricule = af.AGE_MAT
left join grh.fonctions gfon on gfon.FON_COD = af.FON_COD
left join gesper.config_fonction fo on fo.`name` = gfon.FON_LIB
) pseudo
left join (select id,CONCAT_WS("",employee, DATE_FORMAT(affectation_date,'%Y-%m-%d'), fonction,entite,service,type_affectation) as unique_field from gesper.rh_affectation) gaf on pseudo.unique_field like gaf.unique_field
having decision is not null and CONCAT_WS('',gaf.id,pseudo.decision) not in (select CONCAT_WS('',rh_affectation,decision) from gesper.rh_affectation_decision);
