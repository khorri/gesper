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
package com.axelor.apps.base.service.config;

import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.administration.GeneralServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;

public class CompanyConfigService {



	public Currency getCompanyCurrency(Company company) throws AxelorException  {

		if(company.getCurrency() == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.COMPANY_CURRENCY),
					GeneralServiceImpl.EXCEPTION, company.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return company.getCurrency();

	}



	
}
