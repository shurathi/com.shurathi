package com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

import com.splwg.base.api.QueryResultRow;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.COTSInstanceList;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.businessService.BusinessServiceDispatcher;
import com.splwg.base.api.businessService.BusinessServiceInstance;
import com.splwg.base.api.lookup.OutboundMessageProcessingMethodLookup;
import com.splwg.base.api.lookup.OutboundMessageStatusLookup;
import com.splwg.base.api.lookup.SendToLookup;
import com.splwg.base.api.lookup.SenderContextTypeLookup;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.characteristicType.CharacteristicEntity_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.currency.Currency_Id;
import com.splwg.base.domain.common.featureConfiguration.FeatureConfiguration;
import com.splwg.base.domain.common.message.MessageCategory_Id;
import com.splwg.base.domain.common.message.Message_Id;
import com.splwg.base.domain.outboundMessage.outboundMessage.OutboundMessage_Id;
import com.splwg.base.domain.outboundMessage.outboundMessageType.OutboundMessageType;
import com.splwg.base.domain.workflow.notificationExternalId.NotificationExternalId;
import com.splwg.base.domain.workflow.notificationExternalId.OutboundMessageProfile;
import com.splwg.base.domain.xai.xaiSender.SenderContext;
import com.splwg.base.domain.xai.xaiSender.XaiSender;
import com.splwg.base.domain.xai.xaiSender.XaiSenderContexts;
import com.splwg.base.domain.xai.xaiSender.XaiSender_Id;
import com.splwg.ccb.api.lookup.CustomerManagementOptionLookup;
import com.splwg.ccb.domain.admin.customerContactType.CollectionActionAlgorithmSpot;
import com.splwg.ccb.domain.customerinfo.account.Account;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.customerinfo.customerContact.CustomerContact;
import com.splwg.ccb.domain.customerinfo.customerContact.CustomerContactCharacteristic;
import com.splwg.ccb.domain.customerinfo.customerContact.CustomerContactCharacteristic_DTO;
import com.splwg.ccb.domain.customerinfo.customerContact.CustomerContactCharacteristic_Id;
import com.splwg.ccb.domain.customerinfo.customerContact.CustomerContactLog_DTO;
import com.splwg.ccb.domain.customerinfo.customerContact.CustomerContact_Id;
import com.splwg.ccb.domain.customerinfo.person.Person;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.ccb.domain.todo.toDoType.ToDoType;
import com.splwg.cm.api.lookup.CharacteristicEntityLookup;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;
import com.splwg.shared.common.ApplicationError;

/**
 * @author RIA-IN-L-002
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = characteristicType, name = accountCharType, required = true, type = entity)
 *			  ,	@AlgorithmSoftParameter (entityName = characteristicType, name = receiptCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = featureConfiguration, name = unappliedContractTypeFeatureConfiguration, required = true, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = customerManagementOption, name = contractTypesOptionType, required = true, type = lookup)
 *            , @AlgorithmSoftParameter (entityName = businessObject, name = extendableLookup, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = subjectCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = messageTextCategory, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = messageNumber, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = notificationExternalId, name = externalSystem, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = outboundMessageType, name = outboundMessageType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = outboundMessageCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = toDoType, name = exceptionToDoType, required = true, type = entity)})
 */
public class CmReapplicationNoticeAlgComp_Impl extends
		CmReapplicationNoticeAlgComp_Gen implements
		CollectionActionAlgorithmSpot {
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
	private static final String CURRENCY = "$";
	private static final String TABLE_DATA_OPEN = "<td style='text-align: center;' width='123'><p>";
	private static final String TABLE_DATA_CLOSE = "</p></td>";
	private static final String TABLE_ROW_OPEN = "<tr>";
	private static final String TABLE_ROW_CLOSE = "</tr>";
	
	//Hard Parameters
	private String personIdStr;
	private String contactIdStr;
	
	//Soft Parameters
	private CharacteristicType accountCharacteristicType;
	private CharacteristicType receiptCharacteristicType;
	private FeatureConfiguration unappliedFeatureConfigurationType;
	private CustomerManagementOptionLookup unappliedContractTypeOptionType;
	private BusinessObject extendableLookup;
	private CharacteristicType subjectCharacteristicType;
	private CharacteristicType messageTextCategory;
	private CharacteristicType messageNumber;
	private NotificationExternalId externalSystem;
	private OutboundMessageType outboundMessageType;
	private CharacteristicType outboundMessageCharType;
	private ToDoType exceptionToDoType;
	
	@Override
	protected void extraSoftParameterValidations(boolean forAlgorithmValidation){
		accountCharacteristicType = getAccountCharType();
		receiptCharacteristicType = getReceiptCharType();
		unappliedFeatureConfigurationType = getUnappliedContractTypeFeatureConfiguration();
		unappliedContractTypeOptionType = getContractTypesOptionType();
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
		validateCharacteristicType(receiptCharacteristicType, CharacteristicEntityLookup.constants.CUSTOMER_CONTACT);
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
		checkOnAccountContractExists(accountId);
		List<SQLResultRow> unappliedPaymentsList = getUnnappliedPayments(accountId);
		MessageCategory_Id msgCategoryId = new MessageCategory_Id(new BigInteger(getCharacteristicValue(customerContact,this.messageTextCategory)));
		Message_Id msgId = new Message_Id(msgCategoryId, new BigInteger(getCharacteristicValue(customerContact,this.messageNumber)));
		String msg = msgId.getEntity().fetchActiveLongDescription();
		StringBuilder unAppliedPaymentLists = new StringBuilder();
		for(QueryResultRow row : unappliedPaymentsList){
			unAppliedPaymentLists.append(TABLE_ROW_OPEN + TABLE_DATA_OPEN + row.getMoney("TENDER_AMT", new Currency_Id("USD")) + TABLE_DATA_CLOSE);
			unAppliedPaymentLists.append(TABLE_DATA_OPEN + row.getString("ADHOC_CHAR_VAL") + TABLE_DATA_CLOSE);
			unAppliedPaymentLists.append(TABLE_DATA_OPEN + row.getDate("PAY_DT") + TABLE_DATA_CLOSE);
			unAppliedPaymentLists.append(TABLE_DATA_OPEN + row.getMoney("PAY_AMT", new Currency_Id("USD"))+TABLE_DATA_CLOSE + TABLE_ROW_CLOSE);			
		}
		msg = msg.replace("%1",unAppliedPaymentLists.toString());
		msg = msg.replace("?", CURRENCY);
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
	
	private void checkOnAccountContractExists(Account_Id accountId){
		PreparedStatement checkOnAccountContractExistsStatement = null;
		StringBuilder checkOnAccountContractExistsQuery = new StringBuilder();
		checkOnAccountContractExistsQuery.append(" SELECT SA_ID FROM CI_SA WHERE SA_TYPE_CD IN(SELECT RPAD(WO.WFM_OPT_VAL,8) "
				+ " FROM CI_WFM_OPT WO WHERE WO.WFM_NAME =:unappliedContrFeatureConfig AND WO.EXT_OPT_TYPE =:unappliedContrOptionType) "
				+ " AND ACCT_ID =:acctId ");
		checkOnAccountContractExistsStatement = createPreparedStatement(checkOnAccountContractExistsQuery.toString()," ");
		checkOnAccountContractExistsStatement.bindId("acctId", accountId);
		checkOnAccountContractExistsStatement.bindId("unappliedContrFeatureConfig",this.unappliedFeatureConfigurationType.getId());
		checkOnAccountContractExistsStatement.bindLookup("unappliedContrOptionType", this.unappliedContractTypeOptionType);
		SQLResultRow result = checkOnAccountContractExistsStatement.firstRow();
		if(isNull(result)){
			addError(CmMessageRepository.noOnAccountContractExists(accountId.getTrimmedValue()));
		}
		
	}
	public List<SQLResultRow> getUnnappliedPayments(Account_Id accountId){
		List<SQLResultRow> queryResultRowList=null;
		StringBuilder getUnappliedPaymentsQuery = new StringBuilder();
		getUnappliedPaymentsQuery.append("SELECT DISTINCT PAY.PAY_AMT,PC.ADHOC_CHAR_VAL,PE.PAY_DT,PT.TENDER_AMT FROM ");
		getUnappliedPaymentsQuery.append("CI_FT FT, CI_PAY_TNDR PT, CI_PAY_CHAR PC, CI_PAY PAY, CI_PAY_EVENT PE,CI_SA SA ");
		getUnappliedPaymentsQuery.append("WHERE SA.ACCT_ID=:acctId AND FT.SA_ID = SA.SA_ID ");
		getUnappliedPaymentsQuery.append(" AND FT.FREEZE_SW='Y' AND SA.SA_TYPE_CD IN(SELECT RPAD(WO.WFM_OPT_VAL,8) ");
		getUnappliedPaymentsQuery.append(" FROM CI_WFM_OPT WO ");
		getUnappliedPaymentsQuery.append(" WHERE WO.WFM_NAME =:unappliedContrFeatureConfig ");
		getUnappliedPaymentsQuery.append(" AND WO.EXT_OPT_TYPE =:unappliedContrOptionType) AND PAY.ACCT_ID = SA.ACCT_ID ");
		getUnappliedPaymentsQuery.append(" AND PC.PAY_ID = PAY.PAY_ID AND PC.CHAR_TYPE_CD=:charTypeCd ");
		getUnappliedPaymentsQuery.append(" AND PT.PAY_EVENT_ID = PAY.PAY_EVENT_ID AND PE.PAY_EVENT_ID = PAY.PAY_EVENT_ID ");
		PreparedStatement getUnappliedPayments = createPreparedStatement(getUnappliedPaymentsQuery.toString(),"Retrive Unapplied Payments");
		getUnappliedPayments.bindId("acctId",accountId);
		getUnappliedPayments.bindId("unappliedContrFeatureConfig",this.unappliedFeatureConfigurationType.getId());
		getUnappliedPayments.bindLookup("unappliedContrOptionType", this.unappliedContractTypeOptionType);
		getUnappliedPayments.bindId("charTypeCd",this.receiptCharacteristicType.getId());
		queryResultRowList = getUnappliedPayments.list();
		getUnappliedPayments.close();
		if(isNull(queryResultRowList)){
			addError(CmMessageRepository.unappliedPaymentsDoNotExists(accountId.getTrimmedValue()));
		}
		return queryResultRowList;
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
		 * Fetch extendible lookup instance
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
