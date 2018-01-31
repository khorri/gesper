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
package com.axelor.apps.account.service.invoice.generator.batch;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.google.inject.Inject;

public abstract class BatchStrategy extends AbstractBatch {

	protected InvoiceService invoiceService;
	
	@Inject
	private BatchRepository batchRepo;
	
	@Inject
	protected InvoiceRepository invoiceRepo;

	
	protected BatchStrategy( InvoiceService invoiceService ) {

		super();
		this.invoiceService = invoiceService;
		
		
	}
	
	
	protected void updateInvoice( Invoice invoice ){
		
		if (invoice != null) {
			
			invoice.addBatchSetItem( batchRepo.find( batch.getId() ) );
			incrementDone();
			
		}
		
	}
	
}
