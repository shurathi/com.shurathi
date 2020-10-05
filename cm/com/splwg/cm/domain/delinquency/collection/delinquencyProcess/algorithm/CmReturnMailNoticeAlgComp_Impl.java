/*
 ***********************************************************************
 *                Confidentiality Information:
 *
 * This module is the confidential and proprietary information of
 * RIA Advisory LLC; it is not to be copied, reproduced, or
 * transmitted in any form, by any means, in whole or in part,
 * nor is it to be used for any purpose other than that for which
 * it is expressly provided without the written permission of
 * RIA Advisory LLC.
 ***********************************************************************
 *
 * PROGRAM DESCRIPTION:
 *
 * This algorithm will extract an email for Return mail Notice
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:                  Reason:
 * 2020-08-27   KGhuge        CB-283. Initial Version.
 *
*/

package com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm;

import java.math.BigInteger;
import java.util.Iterator;

import com.splwg.base.api.Query;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.COTSInstanceList;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.businessService.BusinessServiceDispatcher;
import com.splwg.base.api.businessService.BusinessServiceInstance;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Lookup;
import com.splwg.base.api.lookup.OutboundMessageProcessingMethodLookup;
import com.splwg.base.api.lookup.OutboundMessageStatusLookup;
import com.splwg.base.api.lookup.SendToLookup;
import com.splwg.base.api.lookup.SenderContextTypeLookup;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.characteristicType.CharacteristicEntity_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.message.MessageCategory_Id;
import com.splwg.base.domain.common.message.Message_Id;
import com.splwg.base.domain.outboundMessage.outboundMessage.OutboundMessage_Id;
import com.splwg.base.domain.outboundMessage.outboundMessageType.OutboundMessageType;
import com.splwg.base.domain.workflow.notificationExternalId.NotificationExternalId;
import com.splwg.base.domain.workflow.notificationExternalId.OutboundMessageProfile;
import com.splwg.base.domain.xai.xaiSender.SenderContext;
import com.splwg.base.domain.xai.xaiSender.XaiSender;
import com.splwg.base.domain.xai.xaiSender.XaiSenderContexts;
import com.splwg.ccb.api.lookup.EntityFlagLookup;
import com.splwg.ccb.domain.admin.customerContactType.CollectionActionAlgorithmSpot;
import com.splwg.ccb.domain.customerinfo.account.Account;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.customerinfo.address.Address;
import com.splwg.ccb.domain.customerinfo.address.AddressCharacteristic;
import com.splwg.ccb.domain.customerinfo.address.AddressEntity;
import com.splwg.ccb.domain.customerinfo.customerContact.CustomerContact;
import com.splwg.ccb.domain.customerinfo.customerContact.CustomerContactCharacteristic;
import com.splwg.ccb.domain.customerinfo.customerContact.CustomerContactCharacteristic_DTO;
import com.splwg.ccb.domain.customerinfo.customerContact.CustomerContactCharacteristic_Id;
import com.splwg.ccb.domain.customerinfo.customerContact.CustomerContactLog;
import com.splwg.ccb.domain.customerinfo.customerContact.CustomerContactLog_DTO;
import com.splwg.ccb.domain.customerinfo.customerContact.CustomerContact_Id;
import com.splwg.ccb.domain.customerinfo.person.Person;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.ccb.domain.todo.toDoType.ToDoType;
import com.splwg.cm.api.lookup.CharacteristicEntityLookup;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;
import com.splwg.shared.common.ApplicationError;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;

/**
 * @author RIA-IN-L-002
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = characteristicType, name = accountCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = billToPrimaryIndicatorCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = addressTypeFlg, name = billToAddressType, required = true, type = lookup)
 *            , @AlgorithmSoftParameter (entityName = businessObject, name = extendableLookup, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = subjectCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = messageTextCategory, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = messageNumber, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = notificationExternalId, name = externalSystem, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = outboundMessageType, name = outboundMessageType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = outboundMessageCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = toDoType, name = exceptionToDoType, required = true, type = entity)})
 */
public class CmReturnMailNoticeAlgComp_Impl extends
		CmReturnMailNoticeAlgComp_Gen implements CollectionActionAlgorithmSpot {

	//Constants
	private static final String EMAIL_DOCUMENT_ELE = "emailDocument";
	private static final String FROM_ELE = "from";
	private static final String INTERNET_ADDRESS_ELE = "internetAddress";
	private static final String ADDRESS_ELE = "address";
	private static final String TO_ELE = "to";
	private static final String SUBJECT_ELE = "subject";
	private static final String TEXT_ELE = "text";
	private static final String MESSAGE_TEXT_ELE = "messageText";
	private static final String EMAIL_ELE = "email";
	private static final String PRIMARY = "PRIMARY";
	
	//Hard Parameters
	private String personIdStr;
	private String contactIdStr;

	//Soft Parameters
	private CharacteristicType accountCharacteristicType;
	private CharacteristicType billToPrimaryIndicatorCharacteristicType;
	private Lookup billToAddressType;
	private BusinessObject extendableLookup;
	private CharacteristicType subjectCharacteristicType;
	private CharacteristicType messageTextCategory;
	private CharacteristicType messageNumber;
	private NotificationExternalId externalSystem;
	private OutboundMessageType outboundMessageType;
	private CharacteristicType outboundMessageCharType;
	private ToDoType exceptionToDoType;
	
	@Override
	protected void extraSoftParameterValidations(boolean forAlgorithmValidation) {
		accountCharacteristicType = getAccountCharType();
		billToPrimaryIndicatorCharacteristicType = getBillToPrimaryIndicatorCharType();
		billToAddressType = getBillToAddressType();
		extendableLookup = getExtendableLookup();
		subjectCharacteristicType = getSubjectCharType();
		messageTextCategory = getMessageTextCategory();
		messageNumber = getMessageNumber();
		externalSystem = getExternalSystem();
		outboundMessageType = getOutboundMessageType();
		outboundMessageCharType = getOutboundMessageCharType();
		exceptionToDoType = getExceptionToDoType();
		validateCharacteristicType(accountCharacteristicType,CharacteristicEntityLookup.constants.CUSTOMER_CONTACT);
		validateCharacteristicType(messageTextCategory,CharacteristicEntityLookup.constants.CUSTOMER_CONTACT);
		validateCharacteristicType(messageNumber,CharacteristicEntityLookup.constants.CUSTOMER_CONTACT);
		validateCharacteristicType(billToPrimaryIndicatorCharacteristicType, CharacteristicEntityLookup.constants.CUSTOMER_CONTACT);
		validateCharacteristicType(subjectCharacteristicType,CharacteristicEntityLookup.constants.CUSTOMER_CONTACT);
		validateCharacteristicType(outboundMessageCharType,CharacteristicEntityLookup.constants.CUSTOMER_CONTACT);
	}
	
	/*
	 * This method is used to validate Message Sender Context
	 */
	private void validateMessageSender(){
		Iterator<OutboundMessageProfile> outboundMessageProfiles = externalSystem.getOutboundMessageProfiles().iterator();
		while(outboundMessageProfiles.hasNext()){
			OutboundMessageProfile obmp = outboundMessageProfiles.next();
			if(obmp.fetchIdType().equals(outboundMessageType)){
				XaiSender xaiSender = obmp.fetchXaiSender();
			XaiSenderContexts xaiSenderContext = xaiSender.getContexts();
			if(xaiSenderContext.isEmpty()){
				addError(CmMessageRepository.messageSenderContextNotFound(xaiSender.getId().getIdValue()));
			}else{
				String respTimeOut = null;
				String smtpHost = null;
				String smtpPwd = null;
				String smtpUname = null;
				String smtpSecType = null;
				Iterator<SenderContext> senderContexts = xaiSenderContext.iterator();
				while(senderContexts.hasNext()){
					SenderContext senderContext = senderContexts.next();
					if(senderContext.getSenderContextType().equals(SenderContextTypeLookup.constants.RESPONSE_TIME_OUT)){
						respTimeOut = senderContext.getContextValue();
					}
					if(senderContext.getSenderContextType().equals(SenderContextTypeLookup.constants.SMTP_HOST_NAME)){
						smtpHost = senderContext.getContextValue();
					}else if(senderContext.getSenderContextType().equals(SenderContextTypeLookup.constants.SMTP_PASSWORD)){
						smtpPwd = senderContext.getContextValue();
					}else if(senderContext.getSenderContextType().equals(SenderContextTypeLookup.constants.SMTP_USER_NAME)){
						smtpUname = senderContext.getContextValue();
					}else if(senderContext.getSenderContextType().equals(SenderContextTypeLookup.constants.SENDER_SECURITY_TYPE)){
						smtpSecType = senderContext.getContextValue();
					}
				}
				if(isBlankOrNull(respTimeOut)){
					addError(CmMessageRepository.requiredContextTypeIsMissing(xaiSender.getId().getIdValue(),"Response Time Out"));
				}
				if(isBlankOrNull(smtpHost)){
					addError(CmMessageRepository.requiredContextTypeIsMissing(xaiSender.getId().getIdValue(),"SMTP Host Name"));
				}
				if(isBlankOrNull(smtpPwd)){
					addError(CmMessageRepository.requiredContextTypeIsMissing(xaiSender.getId().getIdValue(),"SMTP Password"));
				}
				if(isBlankOrNull(smtpUname)){
					addError(CmMessageRepository.requiredContextTypeIsMissing(xaiSender.getId().getIdValue(),"SMTP Username"));
				}
				if(isBlankOrNull(smtpSecType)){
					addError(CmMessageRepository.requiredContextTypeIsMissing(xaiSender.getId().getIdValue(),"SMTP Security Type"));
				}
			}
		}
	}
}
	@Override
	public void invoke() {
		validateMessageSender();
		Person person = null;
		Account_Id accountId = null;
		CustomerContact_Id ccId = new CustomerContact_Id(contactIdStr);
		CustomerContact customerContact = ccId.getEntity();
		if(isNull(customerContact)){
			return;
		}
		//Retrieve Person
		if (!isBlankOrNull(personIdStr)){
			Person_Id personId = new Person_Id(personIdStr);
			person = notNull(personId.getEntity()) ? personId.getEntity() : null;
		}
		Account account = retrieveAccount(customerContact);
		accountId = account.getId();
		BusinessObjectInstance extendableLookupBoInstance = fetchExtendableLookup(account.getCustomerClass().getId().getTrimmedValue(),extendableLookup);
		Address addressEntity = retriveAddressId(person);
		String billToAddress = null;
		if(!isNull(addressEntity)){
			billToAddress = addressEntity.getAddress1()+" "+addressEntity.getCity()+" "+addressEntity.getState()+" "+addressEntity.getPostal()+" "+
							addressEntity.getCountry();
		}else{
			addError(CmMessageRepository.addressDoesNotExist(accountId.getTrimmedValue(), person.getId().getTrimmedValue()));
		}
		BusinessObjectInstance emailMessageBoInstance = BusinessObjectInstance.create("F1-EmailMessage");
		COTSInstanceNode emailDocumentGroup = emailMessageBoInstance.getGroup(EMAIL_DOCUMENT_ELE);
		COTSInstanceNode fromGroup = emailDocumentGroup.getGroup(FROM_ELE);
		COTSInstanceNode internetAddressGroup = fromGroup.getGroup(INTERNET_ADDRESS_ELE);
		COTSInstanceList toList = emailDocumentGroup.getList(TO_ELE);
		COTSInstanceNode subjectGroup = emailDocumentGroup.getGroup(SUBJECT_ELE);
		COTSInstanceNode messageTextGroup = emailDocumentGroup.getGroup(MESSAGE_TEXT_ELE);
		COTSInstanceNode remittanceDetailGroup = extendableLookupBoInstance.getGroup("remittanceDetail");
		if(!isBlankOrNull(remittanceDetailGroup.getString(EMAIL_ELE))){
			internetAddressGroup.set(ADDRESS_ELE,remittanceDetailGroup.getString(EMAIL_ELE));			
		}else{
			addError(CmMessageRepository.requiredAttributeNotFoundInExtendableLookup("From Email Address",extendableLookup.entityName(), person.getId().getTrimmedValue()));
		}
		if(!isNull(person)){
			COTSInstanceListNode toLst = toList.newChild();
			String toEmailAddress = person.getEmailAddress();
			if(!isBlankOrNull(toEmailAddress)){
				COTSInstanceNode toInternetAddressGroup = toLst.getGroup(INTERNET_ADDRESS_ELE);
				toInternetAddressGroup.set(ADDRESS_ELE,toEmailAddress);
			}
			else{
				addError(CmMessageRepository.emailAddressNotFound(person.getId().getTrimmedValue()));
			}
		}
		if(!isBlankOrNull(getCharacteristicValue(customerContact,this.subjectCharacteristicType))){
			subjectGroup.set(TEXT_ELE,getCharacteristicValue(customerContact,this.subjectCharacteristicType));			
		}else{
			addError(CmMessageRepository.requiredAttributeNotFound("Subject", person.getId().getTrimmedValue()));
		}
		MessageCategory_Id msgCategoryId = new MessageCategory_Id(new BigInteger(getCharacteristicValue(customerContact,this.messageTextCategory)));
		Message_Id msgId = new Message_Id(msgCategoryId, new BigInteger(getCharacteristicValue(customerContact,this.messageNumber)));
		String msg = msgId.getEntity().fetchActiveLongDescription();
		Logger logger = LoggerFactory.getLogger(CmCourtesyNoticeAlgComp_Impl.class);
		logger.info("Original Message\t"+msg);
		msg = msg.replace("%1", billToAddress);
		if(!isNull(accountId.getEntity())){
			msg = msg.replace("%2",accountId.getTrimmedValue());
		}else{
			addError(CmMessageRepository.requiredAttributeNotFound("Account Id", person.getId().getTrimmedValue()));
		}
		if(!isBlankOrNull(remittanceDetailGroup.getString("remittanceContact"))){
			msg = msg.replace("%3",remittanceDetailGroup.getString("remittanceContact"));			
		}else{
			addError(CmMessageRepository.requiredAttributeNotFoundInExtendableLookup("Remittance Contact",extendableLookup.entityName(), person.getId().getTrimmedValue()));			
		}
		if(!isBlankOrNull(remittanceDetailGroup.getString(EMAIL_ELE))){
			msg = msg.replace("%4",remittanceDetailGroup.getString(EMAIL_ELE));			
		}else{
			addError(CmMessageRepository.requiredAttributeNotFoundInExtendableLookup("Email", extendableLookup.entityName(), person.getId().getTrimmedValue()));
		}
		messageTextGroup.set(TEXT_ELE, msg);
		String outboundMessage = "";
		try{
		emailMessageBoInstance.set("externalSystem", externalSystem.getId().getIdValue());
		emailMessageBoInstance.set("outboundMessageType", outboundMessageType.getId().getIdValue());
		emailMessageBoInstance.set("creationDateTime", this.getProcessDateTime());
		emailMessageBoInstance.set("processingMethod",OutboundMessageProcessingMethodLookup.constants.REAL_TIME);
		emailMessageBoInstance = BusinessObjectDispatcher.add(emailMessageBoInstance);
		outboundMessage = emailMessageBoInstance.getString("outboundMsgId");
		createCustomerContactChar(customerContact,outboundMessage);
		interogateResponse(outboundMessage,person);
	}catch (ApplicationError e) {
		addError(e.getServerMessage());
	}
	}
	
	private void interogateResponse(String outboundMessage,Person person){
		OutboundMessage_Id outboundMessageId = new OutboundMessage_Id(outboundMessage);
		if(outboundMessageId.getEntity().getStatus().equals(OutboundMessageStatusLookup.constants.ERROR)){
			
			CustomerContactLog_DTO custContactLogDto = new CustomerContactLog_DTO();
			custContactLogDto.setCustomerContactId(new CustomerContact_Id(contactIdStr));
			custContactLogDto.setCreationDateTime(getProcessDateTime());
			MessageCategory_Id msgCategoryId = new MessageCategory_Id(BigInteger.valueOf(92000));
			Message_Id msgId = new Message_Id(msgCategoryId, BigInteger.valueOf(100));
			String msg = msgId.getEntity().fetchActiveMessageText();
			msg = msg.replace("%1",this.externalSystem.fetchLanguageDescription());
			msg = msg.replace("%2",outboundMessageId.getEntity().getErrorDetails());
			custContactLogDto.setLogEntry(msg);
			custContactLogDto.newEntity();
			BusinessServiceInstance businessService = BusinessServiceInstance.create("F1-AddToDoEntry");
			businessService.set("toDoType",this.exceptionToDoType.getId().getTrimmedValue());
			businessService.set("drillKey1", contactIdStr);
			businessService.set("sortKey1",person.getPersonPrimaryName());
			businessService.set("sendTo",SendToLookup.constants.ROLE);
			businessService.set("toDoRole",this.exceptionToDoType.getDefaultRole().getId().getToDoRole().getId().getTrimmedValue());
			businessService.set(SUBJECT_ELE,"Error From External System");
			businessService.set("messageParm1",this.externalSystem.fetchLanguageDescription());
			businessService.set("messageParm2",outboundMessageId.getEntity().getErrorDetails());
			BusinessServiceDispatcher.execute(businessService);
		}
	}

		/**
		 * This method will store Outbound Message as Customer Contact Characteristic
		 * @param cc
		 * @param outMsg
		 */
		private void createCustomerContactChar(CustomerContact cc, String outMsg){
			CustomerContactCharacteristic_DTO ccCharDTO = createDTO(CustomerContactCharacteristic.class);
			
			//Set Characteristic Type
			ccCharDTO.setId(new CustomerContactCharacteristic_Id(outboundMessageCharType,cc));
			
			//Set Characteristic Value
			if(outboundMessageCharType.getCharacteristicType().isPredefinedValue()){
				ccCharDTO.setCharacteristicValue(outMsg);
			}else if(outboundMessageCharType.getCharacteristicType().isAdhocValue()){
				ccCharDTO.setAdhocCharacteristicValue(outMsg);
			}else if(outboundMessageCharType.getCharacteristicType().isForeignKeyValue()){
				ccCharDTO.setCharacteristicValueForeignKey1(outMsg);
			}
			ccCharDTO.newEntity();
		}
		/**
		 * Retrieve Characteristic Value
		 * */
		private String getCharacteristicValue(CustomerContact customerContact,CharacteristicType charType){
			String charValue = null;
			CustomerContactCharacteristic charEntity = customerContact.getEffectiveCharacteristic(charType);
			if (isNull(charEntity)){
				return null;
			}
			
			if (!isBlankOrNull(charEntity.getAdhocCharacteristicValue())) {				
				charValue = charEntity.getAdhocCharacteristicValue().trim();				 
			}else if (!isBlankOrNull(charEntity.getCharacteristicValue())){				
				charValue = charEntity.getCharacteristicValue().trim();				
			}else{
				charValue = charEntity.getCharacteristicValueForeignKey1().trim();
			}
			return charValue;
		}
	/**
	 * This method will retrieves Address from Address Entity
	 * @return AddressId
	 * */
	private Address retriveAddressId(Person person){
		Query<Address> getAddressQry = createQuery(
				"FROM Address address, " +
				"     AddressEntity addressEntity, " +
				"     AddressCharacteristic addressChar " +
				"WHERE address.id = addressChar.id.address " +
				"  AND addressEntity.id.address = addressChar.id.address " +
				"  AND addressChar.id.characteristicType = :addressCharType " +
				"  AND addressChar.characteristicValue= :primary " +
				"  AND addressEntity.id.collectionEntityId = :entityId " +
				"  AND addressEntity.id.addressTypeFlg = :addressTypeFlag " +
				"  AND addressEntity.id.entityType = :entityType ", "");
		
		getAddressQry.bindEntity("addressCharType", this.billToPrimaryIndicatorCharacteristicType);
		getAddressQry.bindStringProperty("primary", AddressCharacteristic.properties.characteristicValue,PRIMARY);
		getAddressQry.bindStringProperty("entityId", AddressEntity.properties.collectionEntityId, person.getId().getIdValue());
		getAddressQry.bindLookup("addressTypeFlag", this.billToAddressType);
		getAddressQry.bindLookup("entityType", EntityFlagLookup.constants.PERSON);
		getAddressQry.addResult(ADDRESS_ELE, ADDRESS_ELE);
		return getAddressQry.firstRow();
	}
	/**
	 * This method will retrieve Account Id from Customer Contact Characteristic.
	 * @param customerContact
	 * @return account
	 */
	private Account retrieveAccount(CustomerContact customerContact){
		String accountIdStr;
		CustomerContactCharacteristic accountChar = customerContact.getEffectiveCharacteristic(accountCharacteristicType);
		
		if (isNull(accountChar)){
			return null;
		}
		
		if (!isBlankOrNull(accountChar.getAdhocCharacteristicValue())) {				
			accountIdStr = accountChar.getAdhocCharacteristicValue().trim();				 
		}else if (!isBlankOrNull(accountChar.getCharacteristicValue())){				
			accountIdStr = accountChar.getCharacteristicValue().trim();				
		}else{
			accountIdStr = accountChar.getCharacteristicValueForeignKey1().trim();
		}
		
		Account_Id accountId = new Account_Id(accountIdStr);
		return accountId.getEntity();
	}
	
	/**
	 * Fetch extendable lookup instance
	 * @param lookupValue
	 * @param extendableLookupBo
	 * @return
	 */
	private BusinessObjectInstance fetchExtendableLookup(String lookupValue, BusinessObject extendableLookupBo) {
		BusinessObjectInstance boInstance = BusinessObjectInstance.create(extendableLookupBo.getId().getTrimmedValue());
		boInstance.set("bo", extendableLookupBo.getId().getTrimmedValue());
		boInstance.set("lookupValue", lookupValue);
		boInstance = BusinessObjectDispatcher.read(boInstance);
		return boInstance;
	}
	
	/**
	 * This method checks if the Characteristic Type is valid for an Entity.
	 * @param Characteristic Type to Validate
	 * @param Entity to be checked on
	 * @param Description of the Soft Parameter
	 */
	private void validateCharacteristicType(CharacteristicType charType, CharacteristicEntityLookup charEntLkup){
		CharacteristicEntity_Id charEntityId = new CharacteristicEntity_Id(charType, charEntLkup);
		if(isNull(charEntityId.getEntity())){						
			addError(MessageRepository.invalidCharTypeForEntity(charType.getId().getIdValue(), 
					charEntLkup.getLookupValue().fetchLanguageDescription()));
		}
	}
	@Override
	public String getContactID() {
		return this.contactIdStr;
	}

	@Override
	public String getContactType() {
		return null;
	}

	@Override
	public String getPersionID() {
		return this.personIdStr;
	}

	@Override
	public void setContactID(String contactId) {
		this.contactIdStr = contactId;
	}

	@Override
	public void setContactType(String arg0) {
	}

	@Override
	public void setPersionID(String personId) {
		this.personIdStr = personId;
	}
}
