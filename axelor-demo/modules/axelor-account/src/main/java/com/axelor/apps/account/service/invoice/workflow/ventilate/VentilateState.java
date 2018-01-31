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
package com.axelor.apps.account.service.invoice.workflow.ventilate;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.List;

import com.axelor.apps.account.db.*;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.JournalService;
import com.axelor.inject.Beans;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.invoice.workflow.WorkflowInvoice;
import com.axelor.apps.account.service.move.MoveService;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class VentilateState extends WorkflowInvoice {
	
	private final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Inject
	private SequenceService sequenceService;

	@Inject
	private MoveService moveService;

	@Inject
	private AccountConfigService accountConfigService;

	@Inject
	protected GeneralService generalService;
	
	@Inject
	private InvoiceRepository invoiceRepo;

	@Override
	public void init(Invoice invoice){
		this.invoice = invoice;
	}

	@Override
	public void process( ) throws AxelorException {

		Preconditions.checkNotNull(invoice.getPartner());

		setDate();
		setJournal();
		setPartnerAccount();

		Sequence sequence = this.getSequence();

		if(!InvoiceToolService.isPurchase(invoice))  {
			this.checkInvoiceDate(sequence);
		}

		setInvoiceId(sequence);
		updatePaymentSchedule( );
		setMove( );
		setStatus( );
	}

	protected void updatePaymentSchedule( ){

		if ( invoice.getPaymentSchedule() != null ) { invoice.getPaymentSchedule().addInvoiceSetItem( invoice ); }

	}

	protected void setPartnerAccount() throws AxelorException {
		AccountingSituation accountSituation = Beans.get(AccountingSituationService.class).getAccountingSituation(invoice.getPartner(), invoice.getCompany());
		if(accountSituation == null)  {
			accountSituation = Beans.get(AccountingSituationService.class).createAccountingSituation(invoice.getPartner(), invoice.getCompany());
		}

		if(invoice.getPartnerAccount() == null)  {
			Account account = InvoiceToolService.isPurchase(invoice) ? accountSituation.getSupplierAccount() : accountSituation.getCustomerAccount();

			if(account == null) {
				throw new AxelorException(I18n.get(IExceptionMessage.VENTILATE_STATE_5), IException.CONFIGURATION_ERROR);
			}

			invoice.setPartnerAccount(account);
		}
	}

	protected void setJournal() throws AxelorException {
		if(invoice.getJournal() == null)  {
			invoice.setJournal(Beans.get(JournalService.class).getJournal(invoice));
		}
	}

	protected void setDate( ) throws AxelorException{

		if(invoice.getInvoiceDate() == null)  {
			invoice.setInvoiceDate(generalService.getTodayDate());
		} else if (invoice.getInvoiceDate().compareTo(generalService.getTodayDate()) > 0) {
			throw new AxelorException(I18n.get(IExceptionMessage.VENTILATE_STATE_FUTURE_DATE),
					IException.CONFIGURATION_ERROR);
		}

		if(!invoice.getPaymentCondition().getIsFree() || invoice.getDueDate() == null)  {
			invoice.setDueDate(this.getDueDate());
		}

	}


	/**
	 *  - Without reset : assure that he doesn't exist invoice with an invoice date greater than the current invoice date.
	 *	- With monthly reset : determine the sequence using the Max number stored on ventilated invoice on the same month.
	 *	- With year reset : determine the sequence using the Max number stored on ventilated invoice on the same year.
	 *
	 * @throws AxelorException
	 */
	protected void checkInvoiceDate(Sequence sequence) throws AxelorException  {

		String query = "self.statusSelect = ?1 AND self.invoiceDate > ?2 AND self.operationTypeSelect = ?3 ";
		List<Object> params = Lists.newArrayList();
		params.add(InvoiceRepository.STATUS_VENTILATED);
		params.add(invoice.getInvoiceDate());
		params.add(invoice.getOperationTypeSelect());

		int i = 4;

		if(sequence.getMonthlyResetOk())  {

			query += String.format("AND EXTRACT (month from self.invoiceDate) = ?%d ", i++);
			params.add(invoice.getInvoiceDate().getMonthOfYear());

		}
		if(sequence.getYearlyResetOk())  {

			query += String.format("AND EXTRACT (year from self.invoiceDate) = ?%d ", i++);
			params.add(invoice.getInvoiceDate().getYear());

		}

		if(invoiceRepo.all().filter(query, params.toArray()).count() > 0)  {
			if(sequence.getMonthlyResetOk())  {
				throw new AxelorException(I18n.get(IExceptionMessage.VENTILATE_STATE_2), IException.CONFIGURATION_ERROR);
			}
			if(sequence.getYearlyResetOk())  {
				throw new AxelorException(I18n.get(IExceptionMessage.VENTILATE_STATE_3), IException.CONFIGURATION_ERROR);
			}
			throw new AxelorException(I18n.get(IExceptionMessage.VENTILATE_STATE_1), IException.CONFIGURATION_ERROR);
		}

	}


	protected LocalDate getDueDate()  {

		return InvoiceToolService.getDueDate(invoice.getPaymentCondition(),invoice.getInvoiceDate());

	}

	protected void setMove( ) throws AxelorException {

		if(invoice.getInTaxTotal().compareTo(BigDecimal.ZERO) == 0)  {  return;  }

		log.debug("In Set Move");
		// Création de l'écriture comptable
		Move move = moveService.createMove(invoice);
		if (move != null)  {

			moveService.createMoveUseExcessPaymentOrDue(invoice);

		}

	}


	/**
	 * Détermine le numéro de facture
	 *
	 * @throws AxelorException
	 */
	protected void setStatus( ) {
		invoice.setStatusSelect(InvoiceRepository.STATUS_VENTILATED);
	}

	/**
	 * Détermine le numéro de facture
	 *
	 * @param sequence
	 * @throws AxelorException
	 */
	protected void setInvoiceId( Sequence sequence ) throws AxelorException {

		if ( !Strings.isNullOrEmpty(invoice.getInvoiceId()) && !invoice.getInvoiceId().contains("*") ) { return; }

		invoice.setInvoiceId( sequenceService.setRefDate( invoice.getInvoiceDate() ).getSequenceNumber(sequence) );

		if (invoice.getInvoiceId() != null) { return; }

		throw new AxelorException(String.format(I18n.get(IExceptionMessage.VENTILATE_STATE_4), invoice.getCompany().getName()), IException.CONFIGURATION_ERROR);

	}

	protected Sequence getSequence( ) throws AxelorException{

		AccountConfig accountConfig = accountConfigService.getAccountConfig(invoice.getCompany());

		switch (invoice.getOperationTypeSelect()) {

		case InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE:

			return accountConfigService.getSuppInvSequence(accountConfig);

		case InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND:

			return accountConfigService.getSuppRefSequence(accountConfig);

		case InvoiceRepository.OPERATION_TYPE_CLIENT_SALE:

			return accountConfigService.getCustInvSequence(accountConfig);

		case InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND:

			return accountConfigService.getCustRefSequence(accountConfig);

		default:
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.JOURNAL_1), invoice.getInvoiceId()), IException.MISSING_FIELD);
		}

	}

}