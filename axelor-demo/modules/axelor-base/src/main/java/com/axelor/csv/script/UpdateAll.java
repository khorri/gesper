/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.csv.script;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;


public class UpdateAll {
	
		@Inject
		private YearRepository yearRepo;
		
		@Inject
		private PeriodRepository periodRepo;
		
		@Transactional
		public Object updatePeriod(Object bean, Map<String,Object> values) {
			try {
				assert bean instanceof Company;
				Company company = (Company) bean;
				List<? extends Period> periods = periodRepo.all().filter("self.company.id = ?1",company.getId()).fetch();
				if(periods == null || periods.isEmpty()) {
					for(Year year : yearRepo.all().filter("self.company.id = ?1 AND self.typeSelect = 1",company.getId()).fetch()) {
						for(Integer month : Arrays.asList(new Integer[]{1,2,3,4,5,6,7,8,9,10,11,12})) {
							Period period = new Period();
							LocalDate dt = new LocalDate(year.getFromDate().getYear(),month,1);
							period.setToDate(dt.dayOfMonth().withMaximumValue());
							period.setFromDate(dt.dayOfMonth().withMinimumValue());
							period.setYear(year);
							period.setStatusSelect(PeriodRepository.STATUS_OPENED);
							period.setCode((dt.toString().split("-")[1]+"/"+year.getCode().split("_")[0]+"_"+company.getCode()).toUpperCase());
							period.setName(dt.toString().split("-")[1]+'/'+year.getName());
							periodRepo.save(period);
						}
					}
				}
				
				return company;
			}catch(Exception e) {
				e.printStackTrace();
			}
			return bean;
		}
		
}



