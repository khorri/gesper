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
package com.axelor.apps.base.service;

import java.util.Set;

import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.General;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.inject.Beans;

public class CompanyServiceImpl implements CompanyService {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void checkMultiBanks(Company company) {
		if (countActiveBankDetails(company) > 1) {
			GeneralService generalService = Beans.get(GeneralService.class);
			General general = generalService.getGeneral();
			if (!general.getManageMultiBanks()) {
				generalService.setManageMultiBanks(true);
			}
		}

	}

	/**
	 * Count the number of active bank details on the provided company.
	 * 
	 * @param company
	 *            the company on which we count the number of active bank
	 *            details
	 * @return the number of active bank details
	 */
	private int countActiveBankDetails(Company company) {
		int count = 0;
		Set<BankDetails> bankDetailsSet = company.getBankDetailsSet();

		if (bankDetailsSet != null) {
			for (BankDetails bankDetails : bankDetailsSet) {
				if (bankDetails.getActive()) {
					++count;
				}
			}
		}

		return count;
	}

}
