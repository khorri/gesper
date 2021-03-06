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
package com.axelor.apps.base.service.message;

import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.PrintingSettings;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.service.MailAccountService;
import com.axelor.apps.message.service.MessageServiceImpl;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaAttachmentRepository;
import com.axelor.tool.template.TemplateMaker;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MessageServiceBaseImpl extends MessageServiceImpl {

	private final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );
	
	@Inject
	protected UserService userService;

	@Inject
	protected GeneralService generalService;
	

	@Inject
	public MessageServiceBaseImpl( MetaAttachmentRepository metaAttachmentRepository, MailAccountService mailAccountService, UserService userService ) {
		super(metaAttachmentRepository, mailAccountService);
		this.userService = userService;
	}


	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Message createMessage(String model, int id, String subject, String content, EmailAddress fromEmailAddress, List<EmailAddress> replyToEmailAddressList, List<EmailAddress> toEmailAddressList, List<EmailAddress> ccEmailAddressList,
			List<EmailAddress> bccEmailAddressList, Set<MetaFile> metaFiles, String addressBlock, int mediaTypeSelect) {

		Message message = super.createMessage( model, id, subject, content, fromEmailAddress, replyToEmailAddressList, toEmailAddressList, ccEmailAddressList, bccEmailAddressList, metaFiles, addressBlock, mediaTypeSelect) ;

		message.setSenderUser(AuthUtils.getUser());
		message.setCompany(userService.getUserActiveCompany());

		return messageRepo.save(message);

	}

	@Override
	public String printMessage(Message message) throws AxelorException  {

		Company company = message.getCompany();
		if(company == null){ return null; }

		PrintingSettings printSettings = company.getPrintingSettings();
		if ( printSettings == null || printSettings.getDefaultMailBirtTemplate() == null ) { return null; }

		BirtTemplate birtTemplate = printSettings.getDefaultMailBirtTemplate();

		logger.debug("Default BirtTemplate : {}",birtTemplate);
		
		String language = AuthUtils.getUser().getLanguage();

		TemplateMaker maker = new TemplateMaker( new Locale(language), '$', '$');
		maker.setContext( messageRepo.find(message.getId()), "Message" );

		String fileName = "Message " + message.getSubject() + "-" + new DateTime().toString("yyyyMMdd");;
		File file = Beans.get(TemplateMessageServiceBaseImpl.class).generateBirtTemplate(maker, fileName,
				birtTemplate.getTemplateLink(), birtTemplate.getFormat(), birtTemplate.getBirtTemplateParameterList());

		return "ws/files/reports/" + file.getName() + "?name=" + fileName;
	}

	
	@Override
	@Transactional(rollbackOn = { MessagingException.class, IOException.class, Exception.class })
	public Message sendByEmail(Message message) throws MessagingException, IOException, AxelorException  {
				
		if(generalService.getGeneral().getActivateSendingEmail())  {  return super.sendByEmail(message);  }
		
		message.setSentByEmail(true);
		message.setStatusSelect(MessageRepository.STATUS_SENT);
		message.setSentDateT(LocalDateTime.now());
		message.setSenderUser(AuthUtils.getUser());
		
		return messageRepo.save(message);
		
	}
	
	public List<String> getEmailAddressNames(Set<EmailAddress> emailAddressSet)  {
        
	   List<String> recipients = Lists.newArrayList();
	   if(emailAddressSet != null){
		   for(EmailAddress emailAddress : emailAddressSet)  {
	           
	           if( Strings.isNullOrEmpty( emailAddress.getName() ) ) { continue; }
	           recipients.add( emailAddress.getName() );
	           
		   }
	   }
	   
	   return recipients;
	}
	
	public String getToRecipients(Message message)  {
		
		if(message.getToEmailAddressSet() != null && !message.getToEmailAddressSet().isEmpty())  {
			return Joiner.on(", \n").join(this.getEmailAddressNames(message.getToEmailAddressSet()));
		}
		
		return "";
	}

}