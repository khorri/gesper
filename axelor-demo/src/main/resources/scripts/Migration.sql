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

#----- insert morocco if not exist
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

