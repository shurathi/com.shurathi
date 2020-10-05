/* 
 **************************************************************************
 *           	     Confidentiality Information:
 *
 * This module is the confidential and proprietary information of
 * RIA Advisory; it is not to be copied, reproduced, or
 * transmitted in any form, by any means, in whole or in part,
 * nor is it to be used for any purpose other than that for which
 * it is expressly provided without the written permission of
 * RIA Advisory.
 *                 
 **************************************************************************
 * 
 * PROGRAM DESCRIPTION:
 * 
 * This algorithm creates an outbound message record with all the required 
 * elements for the RingClear notification.
 * 
 **************************************************************************
 * 
 * CHANGE HISTORY:
 *
 * Date:        by:        Reason:
 * YYYY-MM-DD   IN         Reason text. 
 * 2020-08-24   JFerna     CB-267. Initial	
 **************************************************************************
 */

package com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.ListFilter;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.lookup.OutboundMessageProcessingMethodLookup;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.batch.batchControl.BatchControl;
import com.splwg.base.domain.batch.batchControl.BatchControl_Id;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.characteristicType.CharacteristicEntity_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.phoneType.PhoneType_Id;
import com.splwg.base.domain.outboundMessage.outboundMessageType.OutboundMessageType;
import com.splwg.base.domain.workflow.notificationExternalId.NotificationExternalId;
import com.splwg.base.domain.workflow.notificationExternalId.OutboundMessageProfile;
import com.splwg.ccb.api.lookup.CharacteristicEntityLookup;
import com.splwg.ccb.api.lookup.NameTypeLookup;
import com.splwg.ccb.domain.admin.customerContactType.CollectionActionAlgorithmSpot;
import com.splwg.ccb.domain.customerinfo.account.Account;
import com.splwg.ccb.domain.customerinfo.account.AccountNumber;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.customerinfo.customerContact.CustomerContact;
import com.splwg.ccb.domain.customerinfo.customerContact.CustomerContactCharacteristic;
import com.splwg.ccb.domain.customerinfo.customerContact.CustomerContactCharacteristic_DTO;
import com.splwg.ccb.domain.customerinfo.customerContact.CustomerContactCharacteristic_Id;
import com.splwg.ccb.domain.customerinfo.customerContact.CustomerContact_Id;
import com.splwg.ccb.domain.customerinfo.person.Person;
import com.splwg.ccb.domain.customerinfo.person.PersonName;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;
import com.splwg.shared.common.ApplicationError;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;

/**
 * @author JFerna
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = characteristicType, name = accountCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = idType, name = customerNumberIdType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = phoneType, required = true, type = string)
 *            , @AlgorithmSoftParameter (entityName = notificationExternalId, name = externalSystem, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = outboundMessageType, name = outboundMessageType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = outMsgCharType, required = true, type = entity)})
 */
public class CmRingClearNotificationAlgComp_Impl extends
		CmRingClearNotificationAlgComp_Gen implements
		CollectionActionAlgorithmSpot {
	
	//Constants
	private final static String EMPTY_STRING = "";
	private final static String COMMA = ",";
	private final static String OUT_MSG_ID = "outboundMessageId";
	private final static String SEND_DETAIL = "sendDetail";
	private static final String CUSTOMER_NBR = "customerNbr";
	private static final String PHONE_NBR = "phoneNbr";
	private static final String CUSTOMER_NAME = "customerName";
	

	//Hard Parameters
	private String personIdStr;
	private String contactIdStr;
	
	//Soft Parameters
	private String phoneType;
    private NotificationExternalId externalSystem;
    private OutboundMessageType outMsgType;
    private CharacteristicType outMsgCharType;
    private CharacteristicType accountCharacteristicType;
	
	//Work Variables
	private Person person = null;
	private OutboundMessageProfile outMsgProfile = null;
	
	/**
	 * Validate Soft Parameters
	 * @param forAlgorithmValidation Boolean value
	 */
	protected void extraSoftParameterValidations(boolean forAlgorithmValidation) {
    	//Retrieve Algorithm Parameter Descriptions
    	String phoneTypeDesc = getAlgorithm().getAlgorithmType().getParameterAt(2).fetchLanguageParameterLabel().trim();
    	
    	//Retrieve Soft Parameters
    	phoneType = getPhoneType().trim();
        externalSystem = getExternalSystem();
        outMsgType = getOutboundMessageType();
        outMsgCharType = getOutMsgCharType();
        accountCharacteristicType = getAccountCharacteristicType();
    	
    	//Validate Phone Types
		PhoneType_Id phoneTypeId;		
        for(String phoneTypeStr : phoneType.split(COMMA)){
        	phoneTypeId = new PhoneType_Id(phoneTypeStr.trim());
        	if (isNull(phoneTypeId.getEntity())){    			
    			addError(MessageRepository.invalidEntity(phoneTypeStr, phoneTypeDesc));
        	}		
		}
                
        //Validate Outbound Message Type and External System combination
		outMsgProfile = externalSystem.getProfileForType(outMsgType);		
		
		//Validate Outbound Message Characteristic Type
    	validateCharacteristicType(outMsgCharType,CharacteristicEntityLookup.constants.CUSTOMER_CONTACT);
    	
		//Validate Account Characteristic Type
    	validateCharacteristicType(accountCharacteristicType,CharacteristicEntityLookup.constants.CUSTOMER_CONTACT);       
	}
		
	/**
	 * Main Processing
	 */
	public void invoke() {
		
		//Initialize
		String customerNbr = EMPTY_STRING;
		String phoneNbr = EMPTY_STRING;
		String customerName = EMPTY_STRING;
		
		//Retrieve Customer Contact
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
		
		if (isNull(person)){
			return;
		}
		
		//Retrieve Account
		Account account = retrieveAccount(customerContact);	
		
		//Retrieve Customer Number
		if(notNull(account)){			
			ListFilter<AccountNumber> accountNbrListFilter = account.getAccountNumber().createFilter(" where this.id.accountIdentifierType = :idType", "CmRingClearNotificationAlgComp_Impl");
			accountNbrListFilter.bindEntity("idType", getCustomerNumberIdType());
			AccountNumber accountNbr = accountNbrListFilter.firstRow();
			customerNbr = notNull(accountNbr) ? accountNbr.getAccountNumber() : EMPTY_STRING;
		}
		
		//Retrieve Phone Number
		phoneNbr = retrievePersonPhoneNbr();
		
		//Retrieve Customer Name
		if (!isBlankOrNull(person.getOverrideMailingName1())){
			customerName = person.getOverrideMailingName1();
		}else if (!isBlankOrNull(person.getOverrideMailingName2())){
			customerName = person.getOverrideMailingName2();
		}else if (!isBlankOrNull(person.getOverrideMailingName3())){
			customerName = person.getOverrideMailingName3();
		}else{
			ListFilter<PersonName> personNameListFilter = person.getNames().createFilter(" where this.nameType=:primary and this.isPrimaryName=:isPrimaryNameSw", "CmRingClearNotificationAlgComp_Impl");
			personNameListFilter.bindLookup("primary", NameTypeLookup.constants.PRIMARY);
			personNameListFilter.bindBoolean("isPrimaryNameSw", Bool.TRUE);
			PersonName perName = personNameListFilter.firstRow();
			customerName = notNull(perName) ? perName.getEntityName() : EMPTY_STRING;
		}
		
		//Formulate Send Detail
		String xml = formulateSendDetail(customerNbr,phoneNbr,customerName);
		
		//Create Outbound Message
		String outboundMessage = createOutboundMessage(xml);
		
		//Store Outbound Message Id as Customer Contact Characteristic
		if(!isBlankOrNull(outboundMessage)){
			createCustomerContactChar(customerContact, outboundMessage);
		}
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
	 * This method will retrieve person phone number.
	 * @return phoneNbr
	 */
	private String retrievePersonPhoneNbr(){
		String phoneNbr = EMPTY_STRING;
		String[] phoneTypeList = phoneType.split(COMMA);
		List<SQLResultRow> preparedStatementList = null;
		
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("SELECT PHONE ");
		stringBuilder.append("FROM CI_PER_PHONE ");
		stringBuilder.append("WHERE PER_ID = :personId ");
		stringBuilder.append("AND PHONE_TYPE_CD IN ( ");
		
		for(int i=0; i<phoneTypeList.length; i++){
			if(i==0){
				stringBuilder.append(":phoneType"+i);
			}else{
				stringBuilder.append(", :phoneType"+i);
			}
		}
		
		stringBuilder.append(")");
		
		PreparedStatement retrievePerPhone = createPreparedStatement(stringBuilder.toString(), "Retrieve Person Phone");
		retrievePerPhone.bindId("personId", person.getId());
    	for(int i=0; i<phoneTypeList.length; i++){
    		retrievePerPhone.bindString("phoneType"+i, phoneTypeList[i].trim(), "PHONE_TYPE_CD");
		}
    	preparedStatementList = retrievePerPhone.list();
    	retrievePerPhone.close();
    	
    	if (notNull(preparedStatementList) || !preparedStatementList.isEmpty()){
    		phoneNbr = preparedStatementList.get(0).getString("PHONE");
    	}
    	return phoneNbr;			
	}
	
	/**
	 * This method will create outbound message.
	 * @param sendDetail
	 * @return outboundMessage
	 */
	private String createOutboundMessage(String sendDetail){
		String outboundMessage = EMPTY_STRING;
		Document doc;
		Node node = null;
		
		//Retrieve Business Object
		BusinessObject outboundMsgBusObj = outMsgType.getBusinessObject();
		
		//Retrieve Batch Control
		BatchControl_Id batchCtrlId = outMsgProfile.getBatchControlId();
		BatchControl batchControl = batchCtrlId.getEntity();
		
		try{
			//Convert Send Detail to Document
			doc = DocumentHelper.parseText(sendDetail);
			node = doc.getRootElement();
			
			//Create Business Object Instance
			BusinessObjectInstance outboundMsgBusObjInstance = BusinessObjectInstance.create(outboundMsgBusObj);
			
			//Set Outbound Message Details
			outboundMsgBusObjInstance.set("externalSystem", externalSystem.getId().getIdValue());
			outboundMsgBusObjInstance.set("outboundMessageType", outMsgType.getId().getIdValue());
			outboundMsgBusObjInstance.set("creationDateTime", this.getProcessDateTime());
			outboundMsgBusObjInstance.set("processingMethod",OutboundMessageProcessingMethodLookup.constants.BATCH);
			outboundMsgBusObjInstance.set("batchControl",batchControl.getId().getIdValue());
			outboundMsgBusObjInstance.set("batchNumber",BigDecimal.valueOf(batchControl.getNextBatchNumber().longValue()));
			Element sendDetailElem = outboundMsgBusObjInstance.getDocument().getRootElement().element("sendDetail");
			outboundMsgBusObjInstance.getDocument().getRootElement().remove(sendDetailElem);
			outboundMsgBusObjInstance.getDocument().getRootElement().add(node);
			
			//Create Outbound Message
			outboundMsgBusObjInstance = BusinessObjectDispatcher.add(outboundMsgBusObjInstance);
			outboundMessage = outboundMsgBusObjInstance.getDocument().getRootElement().element(OUT_MSG_ID).getData().toString();
		} catch (DocumentException e) {
			Logger logger = LoggerFactory.getLogger(CmRingClearNotificationAlgComp_Impl.class);
			logger.error(e.getLocalizedMessage());
		} catch (ApplicationError e) {
			addError(e.getServerMessage());
		}
		
		return outboundMessage;				
	}
	
	/**
	 * This method will store Outbound Message as Customer Contact Characteristic
	 * @param cc
	 * @param outMsg
	 */
	private void createCustomerContactChar(CustomerContact cc, String outMsg){
		CustomerContactCharacteristic_DTO ccCharDTO = createDTO(CustomerContactCharacteristic.class);
		
		//Set Characteristic Type
		ccCharDTO.setId(new CustomerContactCharacteristic_Id(outMsgCharType,cc));
		
		//Set Characteristic Value
		if(outMsgCharType.getCharacteristicType().isPredefinedValue()){
			ccCharDTO.setCharacteristicValue(outMsg);
		}else if(outMsgCharType.getCharacteristicType().isAdhocValue()){
			ccCharDTO.setAdhocCharacteristicValue(outMsg);
		}else if(outMsgCharType.getCharacteristicType().isForeignKeyValue()){
			ccCharDTO.setCharacteristicValueForeignKey1(outMsg);
		}
		ccCharDTO.newEntity();
	}
	
	/**
	 * This method will formulate the send detail in xml format.
	 * @param customerNbr
	 * @param phoneNbr
	 * @param customerName
	 * @return sendDetail
	 */
	private String formulateSendDetail(String customerNbr, String phoneNbr, String customerName){
		String sendDetail = EMPTY_STRING;
		
		//Customer Number
		String customerNbrElem = addStartElement(CUSTOMER_NBR) + customerNbr + addEndElement(CUSTOMER_NBR);
		
		//Phone Number
		String phoneNbrElem = addStartElement(PHONE_NBR) + phoneNbr + addEndElement(PHONE_NBR);
		
		//Customer Name
		String customerNameElem = addStartElement(CUSTOMER_NAME) + customerName + addEndElement(CUSTOMER_NAME);
		
		sendDetail = addStartElement(SEND_DETAIL) + customerNbrElem + phoneNbrElem + customerNameElem + addEndElement(SEND_DETAIL);
		
		return sendDetail;
	}
	
	/**
	 * This method create a start element tag.
	 * @param elementName
	 * @return xmlTag
	 */
	private String addStartElement(String elementName){
		String xmlTag = "<" + elementName + ">";
		return xmlTag;
	}

	/**
	 * This method create an end element tag.
	 * @param elementName
	 * @return xmlTag
	 */
	private String addEndElement(String elementName){
		String xmlTag = "</" + elementName + ">";
		return xmlTag;
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
	
	/**
	 * This method will return the value for Customer Contact ID.
	 * @return contactIdStr
	 */
	@Override
	public String getContactID() {
		return contactIdStr;
	}

	/**
	 * This method will return the value for Customer Contact Type.
	 */
	@Override
	public String getContactType() {
		return null;
	}

	/**
	 * This method will return the value for Person ID
	 * @return personIdStr
	 */
	@Override
	public String getPersionID() {
		return personIdStr;
	}

	/**
	 * This method will retrieve the value for Customer Contact ID.
	 * @param contactIdStr
	 */
	@Override
	public void setContactID(String contactID) {
		this.contactIdStr = contactID;

	}

	/**
	 * This method will retrieve the value for Customer Contact Type.
	 */
	@Override
	public void setContactType(String arg0) {

	}

	/**
	 * This method will retrieve the value for Person ID.
	 * @param personIdStr
	 */
	@Override
	public void setPersionID(String personId) {
		this.personIdStr = personId;
	}
}
