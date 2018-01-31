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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.MoveLineReport;
import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;
import org.joda.time.LocalDate;

import java.io.IOException;

public interface MoveLineExportService {

	public void exportMoveLine(MoveLineReport moveLineReport) throws AxelorException, IOException;
	public void replayExportMoveLine(MoveLineReport moveLineReport) throws AxelorException, IOException;
	public MoveLineReport createMoveLineReport(Company company, int exportTypeSelect, LocalDate startDate, LocalDate endDate) throws AxelorException;
	public void exportMoveLineTypeSelect1006(MoveLineReport mlr, boolean replay) throws AxelorException, IOException;
	public void exportMoveLineTypeSelect1007(MoveLineReport moveLineReport, boolean replay) throws AxelorException, IOException;
	public void exportMoveLineTypeSelect1008(MoveLineReport moveLineReport, boolean replay) throws AxelorException, IOException;
	public void exportMoveLineTypeSelect1009(MoveLineReport moveLineReport, boolean replay) throws AxelorException, IOException;
}
