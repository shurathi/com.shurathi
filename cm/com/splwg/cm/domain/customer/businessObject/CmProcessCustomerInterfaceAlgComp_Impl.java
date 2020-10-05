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
 * Process Customer Interface
 *
 * This Algorithm process the Customer Interface Inbound Message and 
 * proceeds to create:
 *   - Person
 *   - Account
 *   - Contract
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:                  Reason:
 * 2020-04-13   VLaksh/JFerna        CB-10. Initial Version. 
 * 2020-05-14	DDejes				 CB-63. Added Bill After Date
 * 2020-05-19   JFerna               CB-61. Added validation when
 *                                   no account found for given id type
 *                                   and value for an existing customer.
 * 2020-05-20	DDejes				 CB-75. Added retrieval of fields 
 * 									 from extendable lookup     
 * 2020-05-22	DDejes				 CB-69. Added account creation
 * 									 during update          
 * 2020-05-27	DDejes				 CB-97. Updated Message Log      
 * 2020-05-21 	KGhuge/JFerna	 	 CB-70. Remove Bill Route Type 
 *                                   defaulting during Customer 
 *                                   Interface 
 * 2020-06-03 	JFerna	 	         CB-105. Updated defaulting of 
 *                                   Account Relationship Type 
 * 2020-07-09   JFerna               CB-52. Added logic for address 
 *                                   entity
 * 2020-07-23   JFerna               CB-233. Added bo action flag in
 *                                   account node 
 * 2020-07-24   JFerna               CB-256. Remove logic that checks if 
 *                                   person has a primary Bill To 
 *                                   Address and use this as the person 
 *                                   address  
 *                                         . Updated logic to set person
 *                                   address equal to the first record
 *                                   from input address list    
 *                                         . Updated logic that retrieves
 *                                   bill address source and address  
 * 2020-08-07	KGhuge				CB-54 Capture Statement Construct during Customer Interface
 * 
 * 2020-09-16	SPatil				CB-396 ORMB230 - Contact Information Tab should not be populated- 
 *                                  use Customer Contact to store contact info 
 *
 * 2020-09-16	KGhuge				CB-416 Invoice Conversion - Update Customer Interface 
 *									to populate Contract Rate Schedule
 ***********************************************************************
 */
package com.splwg.cm.domain.customer.businessObject;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.ListFilter;
import com.splwg.base.api.Query;
import com.splwg.base.api.SavepointExecutable;
import com.splwg.base.api.SavepointExecutable.SavepointResult;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.BusinessObjectStatusCode;
import com.splwg.base.api.businessObject.COTSInstanceList;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.lookup.BusinessObjectActionLookup;
import com.splwg.base.api.lookup.BusinessObjectStatusTransitionConditionLookup;
import com.splwg.base.api.lookup.CharacteristicTypeLookup;
import com.splwg.base.api.lookup.EffectiveStatusLookup;
import com.splwg.base.api.lookup.LogEntryTypeLookup;
import com.splwg.base.api.lookup.YesNoOptionLookup;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectEnterStatusAlgorithmSpot;
import com.splwg.base.domain.common.businessObject.BusinessObject_Id;
import com.splwg.base.domain.common.businessObjectStatusReason.BusinessObjectStatusReason_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicEntity_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.country.Country_Id;
import com.splwg.base.domain.common.country.State_Id;
import com.splwg.base.domain.common.currency.Currency_Id;
import com.splwg.base.domain.common.lookup.LookupValue;
import com.splwg.base.domain.common.phoneType.PhoneType;
import com.splwg.base.domain.common.phoneType.PhoneType_Id;
import com.splwg.base.domain.security.accessGroup.AccessGroup_Id;
import com.splwg.base.support.context.SessionHolder;
import com.splwg.ccb.api.lookup.AcctUsageFlgLookup;
import com.splwg.ccb.api.lookup.AddressTypeFlgLookup;
import com.splwg.ccb.api.lookup.AutoPayMethodLookup;
import com.splwg.ccb.api.lookup.AutoPayTypeFlgLookup;
import com.splwg.ccb.api.lookup.BillFormatLookup;
import com.splwg.ccb.api.lookup.CharacteristicEntityLookup;
import com.splwg.ccb.api.lookup.CustomerReadLookup;
import com.splwg.ccb.api.lookup.EntityFlagLookup;
import com.splwg.ccb.api.lookup.LifeSupportSensitiveLoadLookup;
import com.splwg.ccb.api.lookup.PersonOrBusinessLookup;
import com.splwg.ccb.api.lookup.ServiceAgreementStatusLookup;
import com.splwg.ccb.api.lookup.StatementFormatLookup;
import com.splwg.ccb.domain.admin.accountRelationshipType.AccountRelationshipType_Id;
import com.splwg.ccb.domain.admin.autopayRouteType.AutopayRouteType;
import com.splwg.ccb.domain.admin.autopaySource.AutopaySource;
import com.splwg.ccb.domain.admin.billCycle.BillCycle_Id;
import com.splwg.ccb.domain.admin.billRouteType.BillRouteType_Id;
import com.splwg.ccb.domain.admin.cisDivision.CisDivision_Id;
import com.splwg.ccb.domain.admin.collectionClass.CollectionClass_Id;
import com.splwg.ccb.domain.admin.customerClass.CustomerClass_Id;
import com.splwg.ccb.domain.admin.idType.IdType_Id;
import com.splwg.ccb.domain.admin.idType.accountIdType.AccountNumberType_Id;
import com.splwg.ccb.domain.admin.serviceAgreementType.ServiceAgreementTypeRateSchedule;
import com.splwg.ccb.domain.admin.serviceAgreementType.ServiceAgreementTypeRateSchedule_Id;
import com.splwg.ccb.domain.admin.serviceAgreementType.ServiceAgreementType_Id;
import com.splwg.ccb.domain.admin.statementCycle.StatementCycle_Id;
import com.splwg.ccb.domain.admin.statementRoutingType.StatementRoutingType_Id;
import com.splwg.ccb.domain.customerinfo.account.Account;
import com.splwg.ccb.domain.customerinfo.account.AccountAutopay;
import com.splwg.ccb.domain.customerinfo.account.AccountAutopay_DTO;
import com.splwg.ccb.domain.customerinfo.account.AccountCharacteristic;
import com.splwg.ccb.domain.customerinfo.account.AccountCharacteristic_DTO;
import com.splwg.ccb.domain.customerinfo.account.AccountCharacteristic_Id;
import com.splwg.ccb.domain.customerinfo.account.AccountNumber;
import com.splwg.ccb.domain.customerinfo.account.AccountNumber_DTO;
import com.splwg.ccb.domain.customerinfo.account.AccountNumber_Id;
import com.splwg.ccb.domain.customerinfo.account.AccountPerson;
import com.splwg.ccb.domain.customerinfo.account.AccountPersonRouting;
import com.splwg.ccb.domain.customerinfo.account.AccountPersonRouting_DTO;
import com.splwg.ccb.domain.customerinfo.account.AccountPersonRouting_Id;
import com.splwg.ccb.domain.customerinfo.account.AccountPerson_DTO;
import com.splwg.ccb.domain.customerinfo.account.AccountPerson_Id;
import com.splwg.ccb.domain.customerinfo.account.Account_DTO;
import com.splwg.ccb.domain.customerinfo.account.PersonAddressOverride;
import com.splwg.ccb.domain.customerinfo.account.PersonAddressOverride_DTO;
import com.splwg.ccb.domain.customerinfo.account.PersonAddressOverride_Id;
import com.splwg.ccb.domain.customerinfo.address.Address;
import com.splwg.ccb.domain.customerinfo.address.AddressCharacteristic;
import com.splwg.ccb.domain.customerinfo.address.AddressEntity;
import com.splwg.ccb.domain.customerinfo.person.Person;
import com.splwg.ccb.domain.customerinfo.person.PersonCharacteristic;
import com.splwg.ccb.domain.customerinfo.person.PersonCharacteristic_DTO;
import com.splwg.ccb.domain.customerinfo.person.PersonCharacteristic_Id;
import com.splwg.ccb.domain.customerinfo.person.PersonId;
import com.splwg.ccb.domain.customerinfo.person.PersonId_DTO;
import com.splwg.ccb.domain.customerinfo.person.PersonId_Id;
import com.splwg.ccb.domain.customerinfo.person.PersonName;
import com.splwg.ccb.domain.customerinfo.person.PersonName_DTO;
import com.splwg.ccb.domain.customerinfo.person.PersonName_Id;
import com.splwg.ccb.domain.customerinfo.person.PersonPhone;
import com.splwg.ccb.domain.customerinfo.person.PersonPhone_DTO;
import com.splwg.ccb.domain.customerinfo.person.PersonPhone_Id;
import com.splwg.ccb.domain.customerinfo.person.Person_DTO;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.ccb.domain.customerinfo.serviceAgreement.ServiceAgreement;
import com.splwg.ccb.domain.customerinfo.serviceAgreement.ServiceAgreementRateScheduleHistory;
import com.splwg.ccb.domain.customerinfo.serviceAgreement.ServiceAgreementRateScheduleHistory_DTO;
import com.splwg.ccb.domain.customerinfo.serviceAgreement.ServiceAgreementRateScheduleHistory_Id;
import com.splwg.ccb.domain.customerinfo.serviceAgreement.ServiceAgreement_DTO;
import com.splwg.ccb.domain.customerinfo.statementConstruct.StatementConstruct;
import com.splwg.ccb.domain.customerinfo.statementConstruct.StatementConstruct_DTO;
import com.splwg.ccb.domain.customerinfo.statementConstruct.StatementConstruct_Id;
import com.splwg.ccb.domain.interfaces.inboundMessage.InboundMessage;
import com.splwg.ccb.domain.interfaces.inboundMessage.InboundMessage_Id;
import com.splwg.cm.api.lookup.BillingAddressSourceLookup;
import com.splwg.cm.api.lookup.StatementAddressSourceLookup;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;
import com.splwg.cm.domain.common.messageRepository.CmMessages;
import com.splwg.cm.domain.customer.utility.CmCustInterfaceExtLookupVO;
import com.splwg.cm.domain.customer.utility.CmCustInterfaceUtilityBussComp;
import com.splwg.cm.domain.customer.utility.CmCustomerInterfaceConstants;
import com.splwg.cm.domain.customer.utility.CmCustomerInterfaceExtLookupCache;
import com.splwg.shared.common.ApplicationWarning;

//Start Change CB-54
//Start Change - CB-256
//Start Change - CB-52
///**
// * @author vguddeti
// *
//@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = businessObject, name = custInffDivExtLookup, required = true, type = entity)
// *            , @AlgorithmSoftParameter (entityName = businessObject, name = custIntfCustClassExtLookup, type = entity)
// *            , @AlgorithmSoftParameter (entityName = characteristicType, name = inbPerIdCharType, required = true, type = entity)
// *            , @AlgorithmSoftParameter (entityName = characteristicType, name = inbAcctCharType, required = true, type = entity)})
// */
///**
// * @author vguddeti
// *
//@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = businessObject, name = custInffDivExtLookup, required = true, type = entity)
// *            , @AlgorithmSoftParameter (entityName = businessObject, name = custIntfCustClassExtLookup, type = entity)
// *            , @AlgorithmSoftParameter (entityName = characteristicType, name = inbPerIdCharType, required = true, type = entity)
// *            , @AlgorithmSoftParameter (entityName = characteristicType, name = inbAcctCharType, required = true, type = entity)
// *            , @AlgorithmSoftParameter (lookupName = addressTypeFlg, name = billToAddressType, required = true, type = lookup)
// *            , @AlgorithmSoftParameter (entityName = characteristicType, name = addressBillToIndicatorCharType, required = true, type = entity)
// *            , @AlgorithmSoftParameter (entityName = accountRelationshipType, name = billToAccountRelationshipType, required = true, type = entity)
// *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = accountOverrideAllowed, required = true, type = lookup)
// *            , @AlgorithmSoftParameter (lookupName = personOrBusiness, name = mainCustomerPersonType, required = true, type = lookup)
// *            , @AlgorithmSoftParameter (lookupName = addressTypeFlg, name = shipToAddressType, required = true, type = lookup)
// *            , @AlgorithmSoftParameter (entityName = characteristicType, name = addressShipToIndicatorCharType, required = true, type = entity)
// *            , @AlgorithmSoftParameter (entityName = accountRelationshipType, name = shipToAccountRelationshipType, required = true, type = entity)})
// */
//End Change - CB-52
///**
// * @author vguddeti
// *
//@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = businessObject, name = custInffDivExtLookup, required = true, type = entity)
// *            , @AlgorithmSoftParameter (entityName = businessObject, name = custIntfCustClassExtLookup, type = entity)
// *            , @AlgorithmSoftParameter (entityName = characteristicType, name = inbPerIdCharType, required = true, type = entity)
// *            , @AlgorithmSoftParameter (entityName = characteristicType, name = inbAcctCharType, required = true, type = entity)
// *            , @AlgorithmSoftParameter (lookupName = addressTypeFlg, name = billToAddressType, required = true, type = lookup)
// *            , @AlgorithmSoftParameter (entityName = characteristicType, name = addressBillToIndicatorCharType, required = true, type = entity)
// *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = accountOverrideAllowed, required = true, type = lookup)
// *            , @AlgorithmSoftParameter (lookupName = personOrBusiness, name = mainCustomerPersonType, required = true, type = lookup)
// *            , @AlgorithmSoftParameter (lookupName = addressTypeFlg, name = shipToAddressType, required = true, type = lookup)
// *            , @AlgorithmSoftParameter (entityName = characteristicType, name = addressShipToIndicatorCharType, required = true, type = entity)})
//*/
//End Change - CB-256
/**
 * @author vguddeti
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = businessObject, name = custInffDivExtLookup, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = businessObject, name = custIntfCustClassExtLookup, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = inbPerIdCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = inbAcctCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = addressTypeFlg, name = billToAddressType, required = true, type = lookup)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = addressBillToIndicatorCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = accountOverrideAllowed, required = true, type = lookup)
 *            , @AlgorithmSoftParameter (lookupName = personOrBusiness, name = mainCustomerPersonType, required = true, type = lookup)
 *            , @AlgorithmSoftParameter (lookupName = addressTypeFlg, name = shipToAddressType, required = true, type = lookup)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = addressShipToIndicatorCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = addressStmtIndicatorCharType, type = entity)})
 */
//End Change CB-54

public class CmProcessCustomerInterfaceAlgComp_Impl 
	extends CmProcessCustomerInterfaceAlgComp_Gen
	implements BusinessObjectEnterStatusAlgorithmSpot {
	private BusinessObject businessObject = null;
    private BusinessObjectInstanceKey businessObjectInstKey = null;
	private BusinessObjectInstance boInstance = null;
	private BusinessObjectStatusCode nextBoStatus = null;
	private boolean useDefaultNextStatus;
	private BusinessObjectStatusTransitionConditionLookup nextStatusTransitionConditionLookup;
	private InboundMessage inboundMessage = null;
	
	private CmCustInterfaceUtilityBussComp custIntfUtility = null; 
	private CmCustInterfaceExtLookupVO custExtLukupDataByDiv = null;
	private CmCustInterfaceExtLookupVO custExtLukupDataByCustClass = null;
	
	//CB-52 - Start Add
	private CharacteristicType addressBillToIndicatorCharType;
	private CharacteristicType addressShipToIndicatorCharType;
	private BusinessObject_Id addressBo = new BusinessObject_Id(CmCustomerInterfaceConstants.ADDRESS_BO);
	//CB-52 - End Add

	//Start Add - CB-54
	private String accountOverrideAddressId = null;
	private boolean isAddressGroup = false;
	private String cnstDtlAccountId = null;
	private String statementAddressId = null;
	//End Add - CB-54
	
	@SuppressWarnings("rawtypes")
	public void invoke() {
		this.useDefaultNextStatus = true;
		SessionHolder.getSession().getRequestContext().ignoreWarnings(true);
		SavepointExecutable spExecutable = new SavepointExecutable(){
	      protected void execute(){
	    	  CmProcessCustomerInterfaceAlgComp_Impl.this.processCustomerInboundMessage();
	      }
	    };
	    SavepointResult result = spExecutable.doIt("c1InboundMessage");
	   
	    
	    if (result.hasError()){
	    	custIntfUtility.addLogEntries(result.getMessage(), this.inboundMessage, LogEntryTypeLookup.constants.EXCEPTION,businessObject.getMaintenanceObject());
	    	this.useDefaultNextStatus = false;
	    	this.nextStatusTransitionConditionLookup = BusinessObjectStatusTransitionConditionLookup.constants.EXCEPTION;
	    	/*
	    	 * Add Error Message 
	    	 */
	    	COTSInstanceNode messageNode = this.boInstance.getGroupFromPath(CmCustomerInterfaceConstants.MESSAGE_ELE);
	    	COTSInstanceNode errorListNode =  messageNode.getGroup(CmCustomerInterfaceConstants.ERRORLIST_ELE);
	    	COTSInstanceList errorListNodes = errorListNode.getList(CmCustomerInterfaceConstants.ERROR_ELE);
	    	BigInteger nextSeq = null;
	    	BigInteger curSeq;
	    	for( COTSInstanceNode errorNode : errorListNodes ){
	    		curSeq = errorNode.getNumber(CmCustomerInterfaceConstants.SEQNO_ELE).toBigInteger();
	    		if(isNull(nextSeq) || (notNull(nextSeq)&&nextSeq.compareTo(curSeq)<0)){
	    			nextSeq = curSeq;
	    		}
	    	}
	    	nextSeq = notNull(nextSeq)?nextSeq.add(BigInteger.ONE):BigInteger.ONE;
	    	COTSInstanceNode newErrorNode = errorListNodes.newChild();
	    	newErrorNode.set(CmCustomerInterfaceConstants.SEQNO_ELE,new BigDecimal(nextSeq.toString()));
	    	newErrorNode.set(CmCustomerInterfaceConstants.MESSAGECATEGORY_ELE,new BigDecimal(result.getMessage().getCategory().toString()));
	    	newErrorNode.set(CmCustomerInterfaceConstants.MESSAGENBR_ELE,new BigDecimal(result.getMessage().getNumber().toString()));
	    	newErrorNode.set(CmCustomerInterfaceConstants.MESSAGETEXT_ELE,result.getMessage().getMessageText());
	    	BusinessObjectDispatcher.fastUpdate(this.boInstance.getDocument());
	    }
	}
	
	
	
	private void processCustomerInboundMessage(){
		this.boInstance = BusinessObjectDispatcher.read(this.businessObjectInstKey, true);
		InboundMessage_Id inboundMessageId = new InboundMessage_Id(this.businessObjectInstKey.getString(CmCustomerInterfaceConstants.INBOUNDMESSAGEID_ELE));
	    this.inboundMessage = inboundMessageId.getEntity();
		COTSInstanceNode messageNode = this.boInstance.getGroupFromPath(CmCustomerInterfaceConstants.MESSAGE_ELE);
		COTSInstanceNode messageDataNode =messageNode.getGroupFromPath(CmCustomerInterfaceConstants.MESSAGE_DATA_ELE);
		COTSInstanceNode mainCustomerNode = messageDataNode.getGroupFromPath(CmCustomerInterfaceConstants.MAIN_CUSTOMER_ELE);
		//initializeDefaultConfigData from Extended Lookup
		initializeExtLookupData(mainCustomerNode);
		//get Person/account/contract details for given id
		//Start Change - CB-52
		//Person person =  retrivePersonById(mainCustomerNode);
		Person person = mainCustomerNode.getString(CmCustomerInterfaceConstants.BO_ACTION_FLG).equals(CmCustomerInterfaceConstants.ADD_ACTION) ?
				null : mainCustomerNode.getEntity(CmCustomerInterfaceConstants.PERSONID_ELE, Person.class);
		//End Change - CB-52
		Bool isNewPerson = notNull(person)?Bool.FALSE:Bool.TRUE;	
		Date effDate = mainCustomerNode.getDate(CmCustomerInterfaceConstants.EFFECTIVE_DATE_ELE);
		effDate = notNull(effDate)?effDate:getProcessDateTime().getDate();
		//create or Update Person 
		person = processMainPersonEntity(mainCustomerNode, person,effDate,isNewPerson );
		
		//create or update account and Contract
		COTSInstanceNode accountsNode = mainCustomerNode.getGroup(CmCustomerInterfaceConstants.ACCOUNTS_ELE);

		//Start Change - CB-52
		//addOrUpdateAccounts(person, accountsNode, effDate, isNewPerson);
		COTSInstanceNode contractNode = mainCustomerNode.getGroup(CmCustomerInterfaceConstants.CONTRACTS_ELE);
		addOrUpdateAccounts(person, accountsNode, effDate, isNewPerson, contractNode);
		//End Change - CB-52
		
		//Start - Add - CB-54
		COTSInstanceNode statementNode = mainCustomerNode.getGroup(CmCustomerInterfaceConstants.STATEMENTS_ELE);	
		if(!isNull(statementNode)){
			addorUpdateStatementConstruct(person,statementNode,isNewPerson);			
		}
		//End - Add - CB-54
		mainCustomerNode.set(CmCustomerInterfaceConstants.PERSONID_ELE, person.getId().getTrimmedValue());
		//Start Update - CB-97
		//updateBOWithPersonEntity(person.getId(),messageNode);
		updateBOWithPersonEntity(person.getId(),messageNode, isNewPerson);
		//End Update - CB-97

	}
	
	private void initializeExtLookupData(COTSInstanceNode mainCustomerNode ){

		custIntfUtility = CmCustInterfaceUtilityBussComp.Factory.newInstance();
		String division = mainCustomerNode.getString(CmCustomerInterfaceConstants.DIVISION_ELE);
		
		custExtLukupDataByDiv = CmCustomerInterfaceExtLookupCache.getCustomerInterfaceConfigLookupByDiv(getCustInffDivExtLookup(), division);
		if(isNull(custExtLukupDataByDiv)){
			addError(CmMessageRepository.custIntfExtLukupMissingForDiv(division));
		}
	}
	
	//Start Delete - CB-52
	///**
	// * 
	// * @param mainCustomerNode
	// * @return
	// */
	/*private Person retrivePersonById(COTSInstanceNode mainCustomerNode){
		Person person = null;
		COTSInstanceNode identifiersNode = mainCustomerNode.getGroup(CmCustomerInterfaceConstants.IDENTIFIERS_ELE);
		COTSInstanceList identifierNodeList = identifiersNode.getList(CmCustomerInterfaceConstants.ID_ELE);
		String idType =  null;
		String idValue = null;
		Bool isPrimaryId = Bool.FALSE;
		
		List<COTSInstanceNode> personIds = identifierNodeList.getElementsWhere("[isPrimary = '"+YesNoOptionLookup.constants.YES.trimmedValue()+"' ]");
		if(notNull(personIds)&&!personIds.isEmpty()){
			COTSInstanceNode primaryIdNode = personIds.get(0);
			idType = primaryIdNode.getString(CmCustomerInterfaceConstants.ID_TYPE_ELE); 
			idValue = primaryIdNode.getString(CmCustomerInterfaceConstants.ID_VALUE_ELE);
			isPrimaryId = Bool.TRUE;
		}else{
			for(COTSInstanceNode idNode : identifierNodeList){
				idType = idNode.getString(CmCustomerInterfaceConstants.ID_TYPE_ELE);
				idValue = idNode.getString(CmCustomerInterfaceConstants.ID_VALUE_ELE);
				if(!isBlankOrNull(idType)&&!isBlankOrNull(idValue)){
					isPrimaryId = Bool.TRUE;
					break;
				}
			}
		}
		person = custIntfUtility.fetchPersonById(idType, idValue, isPrimaryId);
		return person;
	}*/
	//End Delete - CB-52
	
	
	
	/**
	 * 
	 * @param mainCustomerNode
	 * @param person
	 * @param effDate
	 * @param isNewPerson
	 * @return
	 */
	private Person processMainPersonEntity(COTSInstanceNode mainCustomerNode,Person person,Date effDate,Bool isNewPerson){
		//person Entity
		person = addOrUpdatePerson(mainCustomerNode, person, effDate,isNewPerson);
		
		//Start Add - CB-52
		COTSInstanceNode addressNode = mainCustomerNode.getGroup(CmCustomerInterfaceConstants.ADDRESSES_ELE);
		//Start Delete - CB-256
		//addOrUpdatePersonAddress(mainCustomerNode, person);
		//End Add - CB-256
		addOrUpdatePersonAddressEntity(person,addressNode,effDate);
		//End Add - CB-52
		
		//person Name 
		String customerName = mainCustomerNode.getString(CmCustomerInterfaceConstants.NAME_ELE);
		addOrUpdatePersonName(person, customerName,isNewPerson);
		//IDS
		COTSInstanceNode identifiersNode = mainCustomerNode.getGroup(CmCustomerInterfaceConstants.IDENTIFIERS_ELE);
		addOrUpdatePersonIds(person, identifiersNode, isNewPerson);
		COTSInstanceNode phonesNode = mainCustomerNode.getGroup(CmCustomerInterfaceConstants.PHONES_ELE);
		addOrUpdatePersonPhones(person, phonesNode, isNewPerson);
	
		COTSInstanceNode characteristicsNode = mainCustomerNode.getGroup(CmCustomerInterfaceConstants.CHARACTERISTICS_ELE);
		addOrUpdatePersonCharacteristics(person, characteristicsNode, effDate, isNewPerson);
		
		return person;
	}

	
	/**
	 * 
	 * @param mainCustomerNode
	 * @param person
	 * @param effDate
	 * @return
	 */
	
	private Person addOrUpdatePerson(COTSInstanceNode mainCustomerNode,Person person,Date effDate,Bool isNewPerson){
		Person_DTO perDto = notNull(person)? person.getDTO():createDTO(Person.class);		
		perDto.setAccessGroupId(new AccessGroup_Id(custExtLukupDataByDiv.getAccessGroup()));
		if(isNewPerson.isTrue()){
			perDto.setDivision(!isBlankOrNull(mainCustomerNode.getString(CmCustomerInterfaceConstants.DIVISION_ELE))?
					mainCustomerNode.getString(CmCustomerInterfaceConstants.DIVISION_ELE):custExtLukupDataByDiv.getDivision());
		}
		perDto.setLifeSupportSensitiveLoad(LifeSupportSensitiveLoadLookup.constants.NONE);
		perDto.setLanguageId(getActiveContextLanguage().getId());
		perDto.setSinceDate(effDate);
		
		Date birthDate = mainCustomerNode.getDate(CmCustomerInterfaceConstants.BIRTH_DT_ELE);
		if(notNull(birthDate)){
			perDto.setBirthDate(birthDate);			
		}
		
		if(!isBlankOrNull(mainCustomerNode.getString(CmCustomerInterfaceConstants.EMAIL_ELE))){
			perDto.setEmailAddress(mainCustomerNode.getString(CmCustomerInterfaceConstants.EMAIL_ELE));
		}
	
		PersonOrBusinessLookup personType = notNull(mainCustomerNode.getLookup(CmCustomerInterfaceConstants.PERSON_TYPE_ELE))?
				(PersonOrBusinessLookup)mainCustomerNode.getLookup(CmCustomerInterfaceConstants.PERSON_TYPE_ELE)
				:custExtLukupDataByDiv.getPersonTypeLookup();
		
		perDto.setPersonOrBusiness(personType);
		
		//Start Delete - CB-52
		/*COTSInstanceNode addressNode = mainCustomerNode.getGroup(CmCustomerInterfaceConstants.ADDRESS_ELE);
		
		if(notNull(addressNode)&&!isBlankOrNull(addressNode.getString(CmCustomerInterfaceConstants.ADDRESS1_ELE))){
			perDto.setAddress1(addressNode.getString(CmCustomerInterfaceConstants.ADDRESS1_ELE));
		}
		
		if(notNull(addressNode)&&!isBlankOrNull(addressNode.getString(CmCustomerInterfaceConstants.ADDRESS2_ELE))){
			perDto.setAddress2(addressNode.getString(CmCustomerInterfaceConstants.ADDRESS2_ELE));
		}
		if(notNull(addressNode)&&!isBlankOrNull(addressNode.getString(CmCustomerInterfaceConstants.ADDRESS3_ELE))){
			perDto.setAddress3(addressNode.getString(CmCustomerInterfaceConstants.ADDRESS3_ELE));
		}
		if(notNull(addressNode)&&!isBlankOrNull(addressNode.getString(CmCustomerInterfaceConstants.CITY_ELE))){
			perDto.setCity(addressNode.getString(CmCustomerInterfaceConstants.CITY_ELE));
		}
		
		if(notNull(addressNode)&&!isBlankOrNull(addressNode.getString(CmCustomerInterfaceConstants.COUNTY_ELE))){
			perDto.setCounty(addressNode.getString(CmCustomerInterfaceConstants.COUNTY_ELE));
		}
		
		String country = addressNode.getString(CmCustomerInterfaceConstants.COUNTRY_ELE);
		if(notNull(addressNode)&&!isBlankOrNull(country)){
			perDto.setCountry(country);
		} 

		String state = notNull(addressNode)?addressNode.getString(CmCustomerInterfaceConstants.STATE_ELE):CmCustomerInterfaceConstants.BLANK_VALUE;
		Bool isStateCountryValid = isValidStateCountryCombination(state, country);
		if(isStateCountryValid.isTrue()) {
			perDto.setState(state);	
		} 

		if(!isBlankOrNull(addressNode.getString(CmCustomerInterfaceConstants.ZIP_ELE))){
			perDto.setPostal(addressNode.getString(CmCustomerInterfaceConstants.ZIP_ELE));
		}*/
		//End Delete - CB-52
		
		//Start Delete -CB-396
		/*//Start Add - CB-256
		COTSInstanceNode addressNode = mainCustomerNode.getGroup(CmCustomerInterfaceConstants.ADDRESSES_ELE);
		COTSInstanceList addressList = addressNode.getList(CmCustomerInterfaceConstants.ADDRESS_ELE);
		Bool isStateCountryValid = Bool.FALSE;
		String address1 = null;
		String address2 = null;
		String address3 = null;
		String address4 = null;
		String city = null;
		String county = null;
		String country = null;
		String state = null;
		String zip = null;
		
    	for (COTSInstanceListNode addressListNode: addressList){
    		address1 = addressListNode.getString(CmCustomerInterfaceConstants.ADDRESS1_ELE);
    		address2 = addressListNode.getString(CmCustomerInterfaceConstants.ADDRESS2_ELE);
    		address3 = addressListNode.getString(CmCustomerInterfaceConstants.ADDRESS3_ELE);
    		address4 = addressListNode.getString(CmCustomerInterfaceConstants.ADDRESS4_ELE);
    		city = addressListNode.getString(CmCustomerInterfaceConstants.CITY_ELE);
    		county = addressListNode.getString(CmCustomerInterfaceConstants.COUNTY_ELE);
    		country = addressListNode.getString(CmCustomerInterfaceConstants.COUNTRY_ELE);
    		state = addressListNode.getString(CmCustomerInterfaceConstants.STATE_ELE);
    		zip = addressListNode.getString(CmCustomerInterfaceConstants.ZIP_ELE);
    		isStateCountryValid = isValidStateCountryCombination(state, country);
    		
    		if(!isBlankOrNull(address1)){
    			perDto.setAddress1(address1);
    		}
    		
    		if(!isBlankOrNull(address2)){
    			perDto.setAddress2(address2);
    		}
    		
    		if(!isBlankOrNull(address3)){
    			perDto.setAddress3(address3);
    		}
    		
    		if(!isBlankOrNull(address4)){
    			perDto.setAddress4(address4);
    		}
    		
    		if(!isBlankOrNull(city)){
    			perDto.setCity(city);
    		}
    		
    		if(!isBlankOrNull(county)){
    			perDto.setCounty(county);
    		}
    		
    		if(!isBlankOrNull(country)){
    			perDto.setCountry(country);
    		} 

    		if(isStateCountryValid.isTrue()) {
    			perDto.setState(state);	
    		} 

    		if(!isBlankOrNull(zip)){
    			perDto.setPostal(zip);
    		} 
    		
    		break;    		
    	}
		//End Add - CB-256
*/				
		//End Delete -CB-396
		
		if(isNewPerson.isTrue()){
			perDto.setCreatedBy(getActiveContextUser().getId().getTrimmedValue());
			perDto.setCreationDateTime(getProcessDateTime());
			person = perDto.newEntity();
		}else{
			perDto.setLastUpdatedBy(getActiveContextUser().getId().getTrimmedValue());
			perDto.setLastUpdatedDTTM(getProcessDateTime());
			person.setDTO(perDto);
		}
		
		return person;
	}
	
	
	/**
	 * 
	 * @param person
	 * @param customerName
	 */
	
	private void addOrUpdatePersonName(Person person,String customerName,Bool isNewPerson){
		PersonName_Id personNameId = new PersonName_Id(person, BigInteger.ONE);
		PersonName personName = isNewPerson.isTrue()?null:personNameId.getEntity();
		PersonName_DTO perNameDto = notNull(personName)?personName.getDTO():createDTO(PersonName.class);
		perNameDto.setEntityName(customerName);
		perNameDto.setId(new PersonName_Id(person, BigInteger.ONE));
		perNameDto.setIsPrimaryName(Bool.TRUE);
		perNameDto.setNameType(custExtLukupDataByDiv.getNameTypeLookup());
		perNameDto.setUppercaseEntityName(customerName.toUpperCase());
		if(notNull(personName)){
			personName.setDTO(perNameDto);
		}else{
			perNameDto.newEntity();
		}
	}
	
	/**
	 * 
	 * @param person
	 * @param customerIdData
	 */
	private void addOrUpdatePersonIds(Person person,COTSInstanceNode identifiersNode,Bool isNewPerson){

		String idType = null;
		String idValue =  null;
		Bool isPrimary = null;
		PersonId_Id personIdId = null;
		PersonId personId =  null;
		PersonId_DTO personIdDto = null;
		COTSInstanceList identifierNodeList = identifiersNode.getList(CmCustomerInterfaceConstants.ID_ELE);
		String isPrimaryS = null;
		for(COTSInstanceNode idNode :identifierNodeList ){
			try {
				idType = idNode.getString(CmCustomerInterfaceConstants.ID_TYPE_ELE);
				idValue = idNode.getString(CmCustomerInterfaceConstants.ID_VALUE_ELE) ;
				isPrimaryS = idNode.getString(CmCustomerInterfaceConstants.IS_PRIMARY_ELE);
				isPrimary = !isBlankOrNull(isPrimaryS)&&YesNoOptionLookup.constants.YES.trimmedValue().equals(isPrimaryS)?Bool.TRUE:Bool.FALSE;
				personIdId = new PersonId_Id(person.getId(),new IdType_Id(idType));
				personId = isNewPerson.isTrue()?null:personIdId.getEntity();
				personIdDto = notNull(personId)?personId.getDTO():createDTO(PersonId.class);
				personIdDto.setId(personIdId);
				personIdDto.setPersonIdNumber(idValue);
				personIdDto.setIsPrimaryId(notNull(isPrimary)?isPrimary:Bool.FALSE);
				if(notNull(personId)){
					personId.setDTO(personIdDto);
				}else{
					personIdDto.newEntity();
				}
			} catch (ApplicationWarning w) {
			}
		}
	}
	
	/**
	 * 
	 * @param person
	 * @param customerPhoneData
	 */
	
	private void addOrUpdatePersonPhones(Person person,COTSInstanceNode phonesNode,Bool isNewPerson){

		if(isNewPerson.isFalse()){
			person.getPhones().asSet().clear();
		}
		COTSInstanceList phoneNodeList = phonesNode.getList(CmCustomerInterfaceConstants.PHONE_ELE);
		PhoneType phoneType = null;
		PersonPhone_DTO personPhoneDto = null;
		PersonPhone_Id personPhoneId = null;
		BigInteger sequence = BigInteger.ZERO;
		
		for(COTSInstanceNode phoneNode :phoneNodeList ){
			sequence = sequence.add(BigInteger.TEN);
			phoneType =phoneNode.getEntity(CmCustomerInterfaceConstants.PHONETYPE_ELE,PhoneType.class);
			personPhoneId = new PersonPhone_Id(person.getId(),sequence);
			personPhoneDto =createDTO(PersonPhone.class);
			personPhoneDto.setId(personPhoneId);
			personPhoneDto.setPhoneTypeId(notNull(phoneType)?phoneType.getId():new PhoneType_Id(custExtLukupDataByDiv.getPhoneType()));
			personPhoneDto.setPhone(phoneNode.getString(CmCustomerInterfaceConstants.PHONEVALUE_ELE));
			personPhoneDto.setExtension(!isBlankOrNull(phoneNode.getString(CmCustomerInterfaceConstants.PHONEEXTENSION_ELE))
					?phoneNode.getString(CmCustomerInterfaceConstants.PHONEEXTENSION_ELE):CmCustomerInterfaceConstants
							.BLANK_VALUE);
			personPhoneDto.newEntity();
		}
	}
	
	/**
	 * 
	 * @param person
	 * @param characteristicsNode
	 * @param effectiveDate
	 * @param isNewPerson
	 */
	private void addOrUpdatePersonCharacteristics(Person person,COTSInstanceNode characteristicsNode,Date effectiveDate,Bool isNewPerson){

		COTSInstanceList characteristicList = characteristicsNode.getList(CmCustomerInterfaceConstants.CHARACTERISTIC_ELE);
		CharacteristicType charType = null;
		PersonCharacteristic_DTO pcharDto = null;
		PersonCharacteristic perChar = null;
		CharacteristicTypeLookup charTypeLookup = null;
		for(COTSInstanceNode characteristicNode : characteristicList){
			perChar = null;
			charType = characteristicNode.getEntity(CmCustomerInterfaceConstants.CHARACTERISTICTYPE_ELE,CharacteristicType.class);
			charTypeLookup = charType.getCharacteristicType();
			effectiveDate = notNull(characteristicNode.getDate(CmCustomerInterfaceConstants.EFFECTIVEDATE_ELE))
					?characteristicNode.getDate(CmCustomerInterfaceConstants.EFFECTIVEDATE_ELE)
							:effectiveDate;
			PersonCharacteristic_Id perCharId = new PersonCharacteristic_Id(person,charType,effectiveDate);
			perChar = isNewPerson.isTrue()?null:perCharId.getEntity();
			pcharDto = notNull(perChar)?perChar.getDTO(): createDTO(PersonCharacteristic.class);
			pcharDto.setId(perCharId);
			if(charTypeLookup.isAdhocValue()){
				pcharDto.setAdhocCharacteristicValue(characteristicNode.getString(CmCustomerInterfaceConstants.CHARACTERISTICVALUE_ELE));
			}else if(charTypeLookup.isPredefinedValue()){
				pcharDto.setCharacteristicValue(characteristicNode.getString(CmCustomerInterfaceConstants.CHARACTERISTICVALUE_ELE));
			}else if(charTypeLookup.isForeignKeyValue()){
				pcharDto.setCharacteristicValueForeignKey1(characteristicNode.getString(CmCustomerInterfaceConstants.CHARACTERISTICVALUE_ELE));
			}
			
			if(notNull(perChar)){
				perChar.setDTO(pcharDto);
			}else{
				pcharDto.newEntity();
			}
			
		}
	}
	
	/**
	 * 
	 * @param person
	 * @param accountsNode
	 * @param effectiveDate
	 * @param isNewPerson
	 */
	//Start Change - CB-52
	//private void addOrUpdateAccounts(Person person,COTSInstanceNode accountsNode,Date effectiveDate,Bool isNewPerson){
	private void addOrUpdateAccounts(Person person,COTSInstanceNode accountsNode,Date effectiveDate,
			Bool isNewPerson, COTSInstanceNode contractNode){
	//End Change - CB-52

		COTSInstanceList accountNodeList = accountsNode.getList(CmCustomerInterfaceConstants.ACCOUNT_ELE);
		Account account = null;
		for(COTSInstanceNode accountNode:accountNodeList ){
			//create or update account
			//Start Delete-CB-97
			//if(isNewPerson.isTrue()){

				//addOrUpdateAccountEntity(accountNode, person, account, effectiveDate, Bool.TRUE);
			//} else{
			//End Delete-CB-97
				//fetch account id for given number
				account = retriveAccountById(accountNode);
				//Start Change-CB-69
				if(isNull(account)){
					//Start Change CB-52
					//addOrUpdateAccountEntity(accountNode, person, account, effectiveDate, Bool.TRUE);
					addOrUpdateAccountEntity(accountNode, person, account, effectiveDate, Bool.TRUE,contractNode);
					//End Change CB-52
				}else{
					//Start Change CB-52
					//addOrUpdateAccountEntity(accountNode, person, account, effectiveDate, Bool.FALSE);
					addOrUpdateAccountEntity(accountNode, person, account, effectiveDate, Bool.FALSE,contractNode);
					//End Change CB-52
				}
				//addOrUpdateAccountEntity(accountNode, person, account, effectiveDate, Bool.FALSE);
				//End Change-CB-69
			//Start Delete-CB-97
			//}
			//End Delete-CB-97
		}
	}
	
	/**
	 * 
	 * @param accountNode
	 * @return
	 */
	private Account retriveAccountById(COTSInstanceNode accountNode){

		Account account = null;
		COTSInstanceNode identifiersNode = accountNode.getGroup(CmCustomerInterfaceConstants.IDENTIFIERS_ELE);
		COTSInstanceList identifierNodeList = identifiersNode.getList(CmCustomerInterfaceConstants.ID_ELE);
		String idType =  null;
		String idValue = null;
		Bool isPrimaryId = Bool.FALSE;
		
		List<COTSInstanceNode> accountIds = identifierNodeList.getElementsWhere("[isPrimary = '"+YesNoOptionLookup.constants.YES.trimmedValue()+"' ]");
		if(notNull(accountIds)&&!accountIds.isEmpty()){
			COTSInstanceNode primaryIdNode = accountIds.get(0);
			idType = primaryIdNode.getString(CmCustomerInterfaceConstants.ID_TYPE_ELE); 
			idValue = primaryIdNode.getString(CmCustomerInterfaceConstants.ID_VALUE_ELE);
			isPrimaryId = Bool.TRUE;
		}else{
			for(COTSInstanceNode idNode : identifierNodeList){
				idType = idNode.getString(CmCustomerInterfaceConstants.ID_TYPE_ELE);
				idValue = idNode.getString(CmCustomerInterfaceConstants.ID_VALUE_ELE);
				if(!isBlankOrNull(idType)&&!isBlankOrNull(idValue)){
					isPrimaryId = Bool.TRUE;
					break;
				}
			}
		}
		account = custIntfUtility.fetchAccountById(idType, idValue, isPrimaryId);
		
		//CB-69 - Start Delete
		//CB-61 - Start Add
		//if (isNull(account)){
		//	addError(CmMessageRepository.accountNotFoundForIdTypeAndValueCombination(idType, idValue));
		//}
		//CB-61 - End Add
		//CB-69 - End Delete
		
		return account;
	}
	
	/**
	 * 
	 * @param accountNode
	 * @param person
	 * @param account
	 * @param effectiveDate
	 * @param isNewAccount
	 * @return
	 */
	//Start Change - CB-52
	//private Account addOrUpdateAccountEntity(COTSInstanceNode accountNode,Person person,Account account,Date effectiveDate,Bool isNewAccount){
	private Account addOrUpdateAccountEntity(COTSInstanceNode accountNode,Person person,Account account,
			Date effectiveDate,Bool isNewAccount,COTSInstanceNode contractNode){
	//End Change - CB-52
		Account_DTO acctDto = isNewAccount.isFalse()?account.getDTO():createDTO(Account.class);
		acctDto.setAccessGroupId(new AccessGroup_Id(custExtLukupDataByDiv.getAccessGroup()));
		String customerClass = accountNode.getString(CmCustomerInterfaceConstants.CUSTOMERCLASS_ELE);
		//Start Add-CB-63
		Date billAfterDate = accountNode.getDate(CmCustomerInterfaceConstants.BILLAFTER_ELE);
		//End Add-CB-63
		//Start Change CB-75	
		if(isNull(getCustIntfCustClassExtLookup())){
			custExtLukupDataByCustClass = custExtLukupDataByDiv;
		}else{
			custExtLukupDataByCustClass =  CmCustomerInterfaceExtLookupCache
					.getCustomerInterfaceConfigLookupByCustClass(getCustIntfCustClassExtLookup(), customerClass);
		}
		//custExtLukupDataByCustClass =  CmCustomerInterfaceExtLookupCache
		//		.getCustomerInterfaceConfigLookupByCustClass(getCustIntfCustClassExtLookup(), customerClass);
		//if(isNull(custExtLukupDataByCustClass)){
		//	custExtLukupDataByCustClass = custExtLukupDataByDiv;
		//}
		//End Change CB-75
		
		if(isNewAccount.isTrue()){
			CisDivision_Id divisionId =  new CisDivision_Id(!isBlankOrNull(accountNode.getString(CmCustomerInterfaceConstants.DIVISION_ELE))?
					accountNode.getString(CmCustomerInterfaceConstants.DIVISION_ELE):custExtLukupDataByCustClass.getDivision());
			acctDto.setDivisionId(divisionId);
			acctDto.setSetUpDate(notNull(accountNode.getDate(CmCustomerInterfaceConstants.SETUPDATE_ELE))
					?accountNode.getDate(CmCustomerInterfaceConstants.SETUPDATE_ELE):effectiveDate);
			acctDto.setCurrencyId(new Currency_Id(!isBlankOrNull(accountNode.getString(CmCustomerInterfaceConstants.CURRENCY_ELE))
					?accountNode.getString(CmCustomerInterfaceConstants.CURRENCY_ELE):custExtLukupDataByCustClass.getCurrency()) );
			acctDto.setCustomerClassId(new CustomerClass_Id(customerClass));
			acctDto.setCreatedBy(getActiveContextUser().getId().getTrimmedValue());
			acctDto.setCreationDateTime(getProcessDateTime());
			//Start Add-CB-63
			acctDto.setBillAfter(billAfterDate);
			//End Add-CB-63
			AcctUsageFlgLookup acctType = (AcctUsageFlgLookup)accountNode.getLookup(CmCustomerInterfaceConstants.ACCOUNTUSAGETYPE_ELE);
			if(notNull(acctType)){
				acctDto.setAcctUsageFlg(acctType.trimmedValue());
			}
			/*else{
				acctDto.setAcctUsageFlg(AcctUsageFlgLookup.constants.USAGEACCT.trimmedValue());
			}*/
		}else{
			acctDto.setLastUpdatedBy(getActiveContextUser().getId().getTrimmedValue());
			acctDto.setLastUpdatedDTTM(getProcessDateTime());
			//Start Add-CB-63
			acctDto.setBillAfter(billAfterDate);
			//End Add-CB-63
		}

		if(!isBlankOrNull(custExtLukupDataByCustClass.getBillCycle())) {
			acctDto.setBillCycleId(new BillCycle_Id(custExtLukupDataByCustClass.getBillCycle()));
		}
		if(!isBlankOrNull(custExtLukupDataByCustClass.getCollectionClass())) {
			acctDto.setCollectionClassId(new CollectionClass_Id(custExtLukupDataByCustClass.getCollectionClass()));
		} 

		if(isNewAccount.isTrue()){
			account = acctDto.newEntity();
		}else{
			account.setDTO(acctDto);
		}
		
		
		//add child persons
		COTSInstanceNode accountPersonsNode = accountNode.getGroup(CmCustomerInterfaceConstants.ACCOUNTPERSONS_ELE);
		COTSInstanceList accountPersonsNodeList = accountPersonsNode.getList(CmCustomerInterfaceConstants.PERSON_ELE);
		
		
		if(notNull(accountPersonsNode)&&notNull(accountPersonsNodeList)&&!accountPersonsNodeList.isEmpty()){
			//Start Change - CB-52
			//addOrUpdateAccountPersons(accountPersonsNode, account,isNewAccount);
			addOrUpdateAccountPersons(accountPersonsNode, account,isNewAccount,effectiveDate);
			//Start Change - CB-52
		}
		//CB-70 - Start Delete
		/*else{
			addOrUpdateAccountPerson(account,person,custExtLukupDataByCustClass.getBillRouteType()
					,custExtLukupDataByCustClass.getBillAddressSource(),custExtLukupDataByCustClass.getBillFormat()
					,BigInteger.ONE, Bool.TRUE,Bool.TRUE,custExtLukupDataByCustClass.getAccountRelationshipType(),
					Bool.TRUE,Bool.TRUE,null,isNewAccount);
		}*/
		//CB-70 - End Delete
		
		//Set Account Identifier
		COTSInstanceNode accountIdsNode = accountNode.getGroup(CmCustomerInterfaceConstants.IDENTIFIERS_ELE);
		addOrUpdateAccountIdentifiers(accountIdsNode, account, isNewAccount);
		COTSInstanceNode accountCharsNode = accountNode.getGroup(CmCustomerInterfaceConstants.CHARACTERISTICS_ELE);
		addOrUpdateAccountCharacteristics(accountCharsNode, account, effectiveDate, isNewAccount);
		
		COTSInstanceNode accountAutoPaysNode = accountNode.getGroup(CmCustomerInterfaceConstants.ACCOUNTAUTOPAY_ELE);
		
		addOrUpdateAccountAutoPays(accountAutoPaysNode, account, isNewAccount);
		
		
		
		if(isNewAccount.isTrue()){
			//Start Change - CB-52
			//createContractEntity(account, custExtLukupDataByCustClass.getSaTypeList(), isNewAccount);
			createContractEntity(account, custExtLukupDataByCustClass.getSaTypeList(), isNewAccount,contractNode);
			//End Change - CB-52
		//Start Change- CB-97	
		//}
		//custIntfUtility.addFkCharLogEntries(CmMessageRepository.accountEntityCreated(account.getId().getTrimmedValue())
		//		, this.inboundMessage, LogEntryTypeLookup.constants.SYSTEM,businessObject.getMaintenanceObject()
		//		,getInbAcctCharType(),account.getId().getTrimmedValue());
			custIntfUtility.addFkCharLogEntries(CmMessageRepository.getServerMessage(CmMessages.ACCOUNT_ENTITY_CREATED, account.getId().getTrimmedValue())
					, this.inboundMessage, LogEntryTypeLookup.constants.SYSTEM,businessObject.getMaintenanceObject()
					,getInbAcctCharType(),account.getId().getTrimmedValue());
		}else{
			custIntfUtility.addFkCharLogEntries(CmMessageRepository.getServerMessage(CmMessages.ACCOUNT_UPDATED, account.getId().getTrimmedValue())
					, this.inboundMessage, LogEntryTypeLookup.constants.SYSTEM,businessObject.getMaintenanceObject()
					,getInbAcctCharType(),account.getId().getTrimmedValue());
		}
		//End Change- CB-97
		
		accountNode.set(CmCustomerInterfaceConstants.ACCOUNTID_ELE, account.getId().getTrimmedValue());
		
		//Start Add - CB-233
		if(isNewAccount.isTrue()){
			accountNode.set(CmCustomerInterfaceConstants.BO_ACTION_FLG, CmCustomerInterfaceConstants.ADD_ACTION);
		}else{
			accountNode.set(CmCustomerInterfaceConstants.BO_ACTION_FLG, CmCustomerInterfaceConstants.UPDATE_ACTION);
		}
		//End Add - CB-233

		//Start Add - CB-54
		  cnstDtlAccountId = account.getId().getIdValue();
		//End Add - CB-54
		return account;
	}
	
	/**
	 * 
	 * @param accountPersonsNode
	 * @param account
	 * @param isNewAccount
	 */
	//Start Change - CB-52
	//private void addOrUpdateAccountPersons(COTSInstanceNode accountPersonsNode,Account account,Bool isNewAccount){
	private void addOrUpdateAccountPersons(COTSInstanceNode accountPersonsNode,Account account,Bool isNewAccount, Date effectiveDate){
	//End Change - CB-52

		COTSInstanceList accountPersonsNodeList = accountPersonsNode.getList(CmCustomerInterfaceConstants.PERSON_ELE);
		Person person = null;
		String billRouteType =  null;
		BillingAddressSourceLookup 	billAddrSource = null;
		BillFormatLookup billFormat = null;
		BigInteger noOfCopies = null;
		Bool shouldRcvNotif =   null;
		Bool shouldRcvCopyOfBill =null;
		Bool isMainCustomer =null;
		Bool isFinanciallyResponsible =null;
		String accountRelationshipType = null;
		String primaryPersonIdType = null;
		String primaryPersonIdValue = null;

		AccountPerson accountPerson =  null;
		AccountPerson_Id  accountPersonId = null;
		AccountPerson_DTO acctperDto =  null;
		//Start Add CB-75
		String shouldReceiveNotifNode;
		String shouldRcvCopyOfBillNode;
		//End Add CB-75
		
		//Start Add - CB-52
		Address address = null;
		//End Add - CB-52
		for( COTSInstanceNode accountPersonNode:accountPersonsNodeList ){
			
			primaryPersonIdType = accountPersonNode.getString(CmCustomerInterfaceConstants.PRIMARYPERSONIDTYPE_ELE);
			primaryPersonIdValue = accountPersonNode.getString(CmCustomerInterfaceConstants.PRIMARYPERSONIDVALUE_ELE);
			
			//Start Add - CB-177
			address = null;
			//End Add -CB-177
			
			if(!isBlankOrNull(primaryPersonIdType)&&!isBlankOrNull(primaryPersonIdValue)){
			
				person = custIntfUtility.fetchPersonById(primaryPersonIdType, primaryPersonIdValue, Bool.TRUE);
				if(notNull(person)) {
					//get Person Id
                    //CB-70 - Start Change					
					/*billRouteType = !isBlankOrNull(accountPersonNode.getString(CmCustomerInterfaceConstants.BILLROUTETYPE_ELE))
							?accountPersonNode.getString(CmCustomerInterfaceConstants.BILLROUTETYPE_ELE)
							:custExtLukupDataByCustClass.getBillRouteType() ;*/
                    billRouteType = accountPersonNode.getString(CmCustomerInterfaceConstants.BILLROUTETYPE_ELE);
					//CB-70 - End Change
					
                    //CB-52 - Start Change
					//billAddrSource =
					//		notNull(accountPersonNode.getLookup(CmCustomerInterfaceConstants.BILLADDRESSSOURCE_ELE))?
					//				(BillingAddressSourceLookup)accountPersonNode.getLookup(CmCustomerInterfaceConstants.BILLADDRESSSOURCE_ELE)
					//				:custExtLukupDataByCustClass.getBillAddressSource();
					if (this.getAccountOverrideAllowed().isYes()){
						billAddrSource = BillingAddressSourceLookup.constants.ACCOUNT_OVERRIDE;
					}else{
						//CB-256 - Start Change
						//billAddrSource = BillingAddressSourceLookup.constants.PERSON;
						
						//if(accountPersonNode.getString(CmCustomerInterfaceConstants.ACCOUNTRELATIONSHIPTYPE_ELE).equals(this.getBillToAccountRelationshipType().getId().getIdValue().trim())){
						//	address = getEffectiveAddressEntity(person.getId().getIdValue(), 
						//			this.getBillToAddressType(),
						//			EntityFlagLookup.constants.PERSON);
						//}
						//
						//if(accountPersonNode.getString(CmCustomerInterfaceConstants.ACCOUNTRELATIONSHIPTYPE_ELE).equals(this.getShipToAccountRelationshipType().getId().getIdValue().trim())){
						//	address = getEffectiveAddressEntity(person.getId().getIdValue(), 
						//			this.getShipToAddressType(),
						//			EntityFlagLookup.constants.PERSON);
						//}
						
						billAddrSource =
								notNull(accountPersonNode.getLookup(CmCustomerInterfaceConstants.BILLADDRESSSOURCE_ELE))?
										(BillingAddressSourceLookup)accountPersonNode.getLookup(CmCustomerInterfaceConstants.BILLADDRESSSOURCE_ELE)
										:(BillingAddressSourceLookup) custExtLukupDataByCustClass.getBillAddressSource();
										
						if (billAddrSource.equals(BillingAddressSourceLookup.constants.BILL_TO)){
							//Start Change - CB-177
							//address = getEffectiveAddressEntity(person.getId().getIdValue(), 
							//		this.getBillToAddressType(),
							//		EntityFlagLookup.constants.PERSON,
							//		null);
							address = getPersonPrimaryAddress(person, this.getBillToAddressType(), this.getAddressBillToIndicatorCharType());
							//End Change - CB-177
						}
						
						if (billAddrSource.equals(BillingAddressSourceLookup.constants.SHIP_TO)){
							//Start Change - CB-177
							//address = getEffectiveAddressEntity(person.getId().getIdValue(), 
							//		this.getShipToAddressType(),
							//		EntityFlagLookup.constants.PERSON,
							//		null);
							address = getPersonPrimaryAddress(person, this.getShipToAddressType(), this.getAddressShipToIndicatorCharType());
							//End Change - CB-177
						}
						//CB-256 - End Change						

					}
                    //CB-52 - End Change
					
					billFormat =
							notNull(accountPersonNode.getLookup(CmCustomerInterfaceConstants.BILLFORMAT_ELE))?
									(BillFormatLookup)accountPersonNode.getLookup(CmCustomerInterfaceConstants.BILLFORMAT_ELE)
									:custExtLukupDataByCustClass.getBillFormat();
					noOfCopies = notNull(accountPersonNode.getNumber(CmCustomerInterfaceConstants.NUMBEROFBILLCOPIES_ELE))?
							accountPersonNode.getNumber(CmCustomerInterfaceConstants.NUMBEROFBILLCOPIES_ELE).toBigInteger():BigInteger.ONE;
					//Start Change CB-75
					//shouldRcvNotif =  (!isBlankOrNull(accountPersonNode.getString(CmCustomerInterfaceConstants.RECEIVESNOTIFICATION_ELE))
					//		&&YesNoOptionLookup.constants.YES.trimmedValue().equals(accountPersonNode
					//				.getString(CmCustomerInterfaceConstants.RECEIVESNOTIFICATION_ELE)))?Bool.TRUE:Bool.FALSE;
					
					//shouldRcvCopyOfBill =  (!isBlankOrNull(accountPersonNode.getString(CmCustomerInterfaceConstants.SHOULDRECEIVECOPYOFBILL_ELE))
					//		&&YesNoOptionLookup.constants.YES.trimmedValue().equals(accountPersonNode
					//				.getString(CmCustomerInterfaceConstants.SHOULDRECEIVECOPYOFBILL_ELE)))?Bool.TRUE:Bool.FALSE;
					
					shouldReceiveNotifNode = accountPersonNode.getString(CmCustomerInterfaceConstants.RECEIVESNOTIFICATION_ELE);

					if(isBlankOrNull(shouldReceiveNotifNode)){
						shouldRcvNotif = CmCustomerInterfaceConstants.TRUE.equalsIgnoreCase(custExtLukupDataByCustClass.getShouldReceiveNotification())
								?Bool.TRUE:Bool.FALSE;
					}else{
						shouldRcvNotif = YesNoOptionLookup.constants.YES.trimmedValue().equals(shouldReceiveNotifNode)?Bool.TRUE:Bool.FALSE;
					}
		
					shouldRcvCopyOfBillNode = accountPersonNode.getString(CmCustomerInterfaceConstants.SHOULDRECEIVECOPYOFBILL_ELE);

					if(isBlankOrNull(shouldRcvCopyOfBillNode)){
						shouldRcvCopyOfBill = CmCustomerInterfaceConstants.TRUE.equalsIgnoreCase(custExtLukupDataByCustClass.getShouldReceiveCopyOfBill())
								?Bool.TRUE:Bool.FALSE;
					}else{
						shouldRcvCopyOfBill = YesNoOptionLookup.constants.YES.trimmedValue().equals(shouldRcvCopyOfBillNode)?Bool.TRUE:Bool.FALSE;
					}
					//End Change CB-75		
					
					isMainCustomer =  (!isBlankOrNull(accountPersonNode.getString(CmCustomerInterfaceConstants.ISMAINCUSTOMER_ELE))
							&&YesNoOptionLookup.constants.YES.trimmedValue().equals(accountPersonNode
									.getString(CmCustomerInterfaceConstants.ISMAINCUSTOMER_ELE)))?Bool.TRUE:Bool.FALSE;
					
					isFinanciallyResponsible =  (!isBlankOrNull(accountPersonNode.getString(CmCustomerInterfaceConstants.ISFINANCIALLYRESPONSIBLE_ELE))
							&&YesNoOptionLookup.constants.YES.trimmedValue().equals(accountPersonNode
									.getString(CmCustomerInterfaceConstants.ISFINANCIALLYRESPONSIBLE_ELE)))?Bool.TRUE:Bool.FALSE;
					
					//CB-105 - Start Change
					//accountRelationshipType = isBlankOrNull(accountPersonNode.getString(CmCustomerInterfaceConstants.ACCOUNTRELATIONSHIPTYPE_ELE))
					accountRelationshipType = !isBlankOrNull(accountPersonNode.getString(CmCustomerInterfaceConstants.ACCOUNTRELATIONSHIPTYPE_ELE))
					//CB-105 - End Change
							?accountPersonNode.getString(CmCustomerInterfaceConstants.ACCOUNTRELATIONSHIPTYPE_ELE)
							:custExtLukupDataByCustClass.getAccountRelationshipType();

					COTSInstanceNode addressNode = accountPersonNode.getGroup(CmCustomerInterfaceConstants.ADDRESS_ELE);
					
					accountPersonId = new AccountPerson_Id(person.getId(), account.getId());

					accountPerson = isNewAccount.isTrue()?null:accountPersonId.getEntity();
					acctperDto	= notNull(accountPerson) ?accountPerson.getDTO():createDTO(AccountPerson.class);
					
					acctperDto.setId(accountPersonId);
					acctperDto.setIsMainCustomer(isMainCustomer);
					acctperDto.setReceivesNotification(shouldRcvNotif);
					acctperDto.setIsFinanciallyResponsible(isFinanciallyResponsible);
					
					acctperDto.setBillAddressSource(billAddrSource);
					acctperDto.setShouldReceiveCopyOfBill(shouldRcvCopyOfBill);
					acctperDto.setBillRouteTypeId(new BillRouteType_Id(billRouteType));
					acctperDto.setBillFormat(billFormat);
					acctperDto.setNumberOfBillCopies(noOfCopies);
					acctperDto.setAccountRelationshipTypeId(new AccountRelationshipType_Id(accountRelationshipType));
					if(isNull(accountPerson)){
						accountPerson =  acctperDto.newEntity();
					}else{
						accountPerson.setDTO(acctperDto);
					}
						
					//Start Change CB-75
					if(!billAddrSource.isPerson()){
						//addOrUpdateAccountOverrideAddress(accountPerson, addressNode, isNewAccount);
						addOrUpdateAccountOverrideAddress(accountPerson, addressNode, isNewAccount,effectiveDate);
					}
					//addOrUpdateAccountOverrideAddress(accountPerson, addressNode, isNewAccount);
					//End Change CB-75

					//Retrieve Address of person if bill address source is person
		
					
					if(shouldRcvCopyOfBill.isTrue()){
						AccountPersonRouting_Id apRoutingId = new AccountPersonRouting_Id(accountPersonId, new BillRouteType_Id(billRouteType),BigInteger.ONE);
						AccountPersonRouting_DTO apRoutingDTO = null;
						AccountPersonRouting apRouting = null;
						apRouting = isNewAccount.isTrue()?null:apRoutingId.getEntity();
						
						if(isNull(apRouting)|| (notNull(apRouting)&&apRouting.getBillAddressSource().compareTo(billAddrSource)!=0)){
							apRoutingDTO = notNull(apRouting)?apRouting.getDTO():createDTO(AccountPersonRouting.class); 
							apRoutingDTO.setId(apRoutingId);
							apRoutingDTO.setBillAddressSource(billAddrSource);
							apRoutingDTO.setBillFormat(billFormat);
							apRoutingDTO.setNumberOfBillCopies(noOfCopies);
							apRoutingDTO.setReceivesNotification(shouldRcvNotif);
							apRoutingDTO.setShouldReceiveCopyOfBill(shouldRcvCopyOfBill);
							
							//Start Add - CB-52
							if(notNull(address)){
								apRoutingDTO.setAddressId(address.getId().getIdValue());
							}
							//End Add - CB-52

							if(notNull(apRouting)){
								apRouting.setDTO(apRoutingDTO);
							}else{
								apRoutingDTO.newEntity();
							}
						}
						
					}
				}
				/*else{
					useDefaultNextStatus = false;
					nextBoStatus = new BusinessObjectStatusCode(businessObject.getId(), getNextStatusIfNoChildPerson());
					continue;
				}*/
			}
		}
	}
	
	//CB-70 - Start Delete
	///**
	// * 
	// * @param account
	// * @param person
	// * @param billRouteType
	// * @param billAddrSource
	// * @param billFormat
	// * @param noOfCopies
	// * @param shouldRcvNotif
	// * @param shouldRcvCopyOfBill
	// * @param accountRelationshipType
	// * @param isMainCustomer
	// * @param isFinanciallyResponsible
	// * @param addressNode
	// * @param isNewAccount
	// * @return
	// */
	/*private AccountPerson addOrUpdateAccountPerson(Account account,Person person,String billRouteType
			,BillingAddressSourceLookup billAddrSource,BillFormatLookup billFormat,BigInteger noOfCopies,
			Bool shouldRcvNotif,Bool shouldRcvCopyOfBill,String accountRelationshipType,
			Bool isMainCustomer,Bool isFinanciallyResponsible,COTSInstanceNode addressNode,Bool isNewAccount){
		AccountPerson accountPerson =  null;
		AccountPerson_Id  accountPersonId = new AccountPerson_Id(person.getId(), account.getId());
		AccountPerson_DTO acctperDto =  null;

		accountPerson = isNewAccount.isTrue()?null:accountPersonId.getEntity();
		acctperDto	= notNull(accountPerson) ?accountPerson.getDTO():createDTO(AccountPerson.class);
		
		acctperDto.setId(accountPersonId);
		acctperDto.setIsMainCustomer(isMainCustomer);
		acctperDto.setReceivesNotification(shouldRcvNotif);
		acctperDto.setIsFinanciallyResponsible(isFinanciallyResponsible);
		
		acctperDto.setBillAddressSource(billAddrSource);
		acctperDto.setShouldReceiveCopyOfBill(shouldRcvCopyOfBill);
		acctperDto.setBillRouteTypeId(new BillRouteType_Id(billRouteType));
		acctperDto.setBillFormat(billFormat);
		acctperDto.setNumberOfBillCopies(noOfCopies);
		acctperDto.setAccountRelationshipTypeId(new AccountRelationshipType_Id(accountRelationshipType));
		if(isNull(accountPerson)){
			accountPerson =  acctperDto.newEntity();
		}else{
			accountPerson.setDTO(acctperDto);
		}
			
		//Start Change CB-75
		//if(notNull(addressNode)){
		if(notNull(addressNode) && !billAddrSource.isPerson()){
			addOrUpdateAccountOverrideAddress(accountPerson, addressNode, isNewAccount);
		}
		//End Change CB-75
	
		addOrUpdateAccountPersonRouting(accountPerson.getId(), billRouteType, billAddrSource, billFormat, noOfCopies, shouldRcvNotif
				, shouldRcvCopyOfBill, isNewAccount);
		return accountPerson;
	}*/
	
	///**
	// * 
	// * @param accountPersonId
	// * @param billRouteType
	// * @param billAddrSource
	// * @param billFormat
	// * @param noOfCopies
	// * @param shouldRcvNotif
	// * @param shouldRcvCopyOfBill
	// * @param isNewAccount
	// */
	/*private void addOrUpdateAccountPersonRouting(AccountPerson_Id accountPersonId,String billRouteType
			,BillingAddressSourceLookup billAddrSource,BillFormatLookup billFormat,BigInteger noOfCopies,
			Bool shouldRcvNotif,Bool shouldRcvCopyOfBill,Bool isNewAccount){
		
		if(shouldRcvCopyOfBill.isTrue()){
			try {
				AccountPersonRouting_Id apRoutingId = new AccountPersonRouting_Id(accountPersonId, new BillRouteType_Id(billRouteType),BigInteger.ONE);
				AccountPersonRouting_DTO apRoutingDTO = null;
				AccountPersonRouting apRouting = null;
				apRouting = isNewAccount.isTrue()?null:apRoutingId.getEntity();
				
				if(notNull(apRouting)&&apRouting.getBillAddressSource().compareTo(billAddrSource)==0){
					return;
				}
				
				apRoutingDTO = notNull(apRouting)?apRouting.getDTO():createDTO(AccountPersonRouting.class); 
				apRoutingDTO.setId(apRoutingId);
				apRoutingDTO.setBillAddressSource(billAddrSource);
				apRoutingDTO.setBillFormat(billFormat);
				apRoutingDTO.setNumberOfBillCopies(noOfCopies);
				apRoutingDTO.setReceivesNotification(shouldRcvNotif);
				apRoutingDTO.setShouldReceiveCopyOfBill(shouldRcvCopyOfBill);
				if(notNull(apRouting)){
					apRouting.setDTO(apRoutingDTO);
				}else{
					apRoutingDTO.newEntity();
				}
			} catch (ApplicationError e) {
			}
		}
	}*/
	//CB-70 - End Delete
	
	/**
	 * 
	 * @param accountPerson
	 * @param addressNode
	 * @param isNewAccount
	 */
	//Start Change - CB-52
	//private void addOrUpdateAccountOverrideAddress(AccountPerson accountPerson,COTSInstanceNode addressNode,Bool isNewAccount ){
	private void addOrUpdateAccountOverrideAddress(AccountPerson accountPerson,COTSInstanceNode addressNode,Bool isNewAccount, Date effectiveDate){
	//End Change - CB-52
		if(notNull(addressNode)&&(!isBlankOrNull(addressNode.getString(CmCustomerInterfaceConstants.ADDRESS1_ELE))||
				!isBlankOrNull(addressNode.getString(CmCustomerInterfaceConstants.COUNTRY_ELE))) ){
		
			PersonAddressOverride_DTO acctOvrdAddrDTO = null;
			PersonAddressOverride_Id acctOvrdAddrId = new PersonAddressOverride_Id(accountPerson);
			PersonAddressOverride acctOvrdAddr = null;
			acctOvrdAddr = isNewAccount.isTrue()?null:acctOvrdAddrId.getEntity();
			acctOvrdAddrDTO = notNull(acctOvrdAddr)?acctOvrdAddr.getDTO():createDTO(PersonAddressOverride.class);
			
			acctOvrdAddrDTO.setId(acctOvrdAddrId);
			if(notNull(addressNode)&&!isBlankOrNull(addressNode.getString(CmCustomerInterfaceConstants.ADDRESS1_ELE))){
				acctOvrdAddrDTO.setAddress1(addressNode.getString(CmCustomerInterfaceConstants.ADDRESS1_ELE));
			}
			
			if(notNull(addressNode)&&!isBlankOrNull(addressNode.getString(CmCustomerInterfaceConstants.ADDRESS2_ELE))){
				acctOvrdAddrDTO.setAddress2(addressNode.getString(CmCustomerInterfaceConstants.ADDRESS2_ELE));
			}
			if(notNull(addressNode)&&!isBlankOrNull(addressNode.getString(CmCustomerInterfaceConstants.ADDRESS3_ELE))){
				acctOvrdAddrDTO.setAddress3(addressNode.getString(CmCustomerInterfaceConstants.ADDRESS3_ELE));
			}
			
			//Start Add - CB-256
			if(notNull(addressNode)&&!isBlankOrNull(addressNode.getString(CmCustomerInterfaceConstants.ADDRESS4_ELE))){
				acctOvrdAddrDTO.setAddress4(addressNode.getString(CmCustomerInterfaceConstants.ADDRESS4_ELE));
			}
			//End Add - CB-256
			
			if(notNull(addressNode)&&!isBlankOrNull(addressNode.getString(CmCustomerInterfaceConstants.CITY_ELE))){
				acctOvrdAddrDTO.setCity(addressNode.getString(CmCustomerInterfaceConstants.CITY_ELE));
			}
			
			if(notNull(addressNode)&&!isBlankOrNull(addressNode.getString(CmCustomerInterfaceConstants.COUNTY_ELE))){
				acctOvrdAddrDTO.setCounty(addressNode.getString(CmCustomerInterfaceConstants.COUNTY_ELE));
			}
			
			String country = addressNode.getString(CmCustomerInterfaceConstants.COUNTRY_ELE);
			if(notNull(addressNode)&&!isBlankOrNull(country)){
				acctOvrdAddrDTO.setCountry(country);
			} 
	
			String state = notNull(addressNode)?addressNode.getString(CmCustomerInterfaceConstants.STATE_ELE):CmCustomerInterfaceConstants.BLANK_VALUE;
			Bool isStateCountryValid = isValidStateCountryCombination(state, country);
			if(isStateCountryValid.isTrue()) {
				acctOvrdAddrDTO.setState(state);	
			} 
	
			if(!isBlankOrNull(addressNode.getString(CmCustomerInterfaceConstants.ZIP_ELE))){
				acctOvrdAddrDTO.setPostal(addressNode.getString(CmCustomerInterfaceConstants.ZIP_ELE));
			}
			
			if(isNull(acctOvrdAddr)){
				acctOvrdAddr = 	acctOvrdAddrDTO.newEntity();
			}else{
				acctOvrdAddr.setDTO(acctOvrdAddrDTO);
			}
			
			//Start Add - CB-52
			//Add Address Characteristics
			COTSInstanceNode addressCharsBoListNode = null;
			CharacteristicType charType = null;
			CharacteristicTypeLookup charTypeLookup = null;
			String charVal = null;
			Date charEffDate = null;
			Bool isFoundAddressChar;
			
			//Retrieve Effective Address Entity for Account Override Address
			//Start Change - CB-256
			//Address accountAddress = getEffectiveAddressEntity(accountPerson.fetchIdAccount().getId().getIdValue(), 
			//		AddressTypeFlgLookup.constants.ACCOUNT_OVERRIDE,
			//		EntityFlagLookup.constants.ACCOUNT);
			Address accountAddress = getEffectiveAddressEntity(accountPerson.fetchIdAccount().getId().getIdValue(), 
					AddressTypeFlgLookup.constants.ACCOUNT_OVERRIDE,
					EntityFlagLookup.constants.ACCOUNT,
					null);
			//End Change - CB-256
			
			if(notNull(accountAddress)){
				//Set Address Id
				addressNode.set(CmCustomerInterfaceConstants.ADDRESSES_ID_ELE, accountAddress.getId().getIdValue());
				
				//Create Address BO Instance
				BusinessObjectInstance addressBoInstance = BusinessObjectInstance.create(addressBo.getEntity());
				
				//Retrieve Address BO Details
				addressBoInstance.set(CmCustomerInterfaceConstants.REQUEST_BO_ELE, accountAddress.getId().getIdValue());
				addressBoInstance = BusinessObjectDispatcher.read(addressBoInstance, true);
				COTSInstanceNode addressCharsBoNode = addressBoInstance.getGroup(CmCustomerInterfaceConstants.CHARACTERISTICS_ELE);
				COTSInstanceList addressCharsBoList = addressCharsBoNode.getList(CmCustomerInterfaceConstants.ADDRESS_CHARS_BO_ELE);
				
				//For each characteristics from input, add or update address characteristics
				COTSInstanceNode addressCharNode = addressNode.getGroup(CmCustomerInterfaceConstants.CHARACTERISTICS_ELE);
				COTSInstanceList addressCharNodeList = addressCharNode.getList(CmCustomerInterfaceConstants.CHARACTERISTIC_ELE);
				for (COTSInstanceNode addressChar : addressCharNodeList){		
					isFoundAddressChar = Bool.FALSE;
					charType = addressChar.getEntity(CmCustomerInterfaceConstants.CHARACTERISTICTYPE_ELE,CharacteristicType.class);
					charTypeLookup = charType.getCharacteristicType();
					charVal = addressChar.getString(CmCustomerInterfaceConstants.CHARACTERISTICVALUE_ELE);
					charEffDate = addressChar.getDate(CmCustomerInterfaceConstants.EFFECTIVE_DATE_ELE);
					
					//Loop thru existing address characteristics
					for (COTSInstanceNode addressCharBo : addressCharsBoList){
						//If characteristic type and effective date matches the input, update the characteristic value
						if (addressCharBo.getString(CmCustomerInterfaceConstants.CHARACTERISTICTYPE_ELE).equals(charType.getId().getIdValue())
								&& addressCharBo.getDate(CmCustomerInterfaceConstants.EFFECTIVE_DATE_ELE).equals(charEffDate)){
							isFoundAddressChar = Bool.TRUE;
							if(charTypeLookup.isAdhocValue()){
								addressCharBo.set(CmCustomerInterfaceConstants.ADHOC_VAL_BO_ELE, charVal);
							}else if(charTypeLookup.isPredefinedValue()){
								addressCharBo.set(CmCustomerInterfaceConstants.CHARACTERISTICVALUE_ELE, charVal);
							}else if(charTypeLookup.isForeignKeyValue()){
								addressCharBo.set(CmCustomerInterfaceConstants.CHAR_VAL_FK1_BO_ELE, charVal);
							}
						}
					}
					
					//If no existing address characteristic is found, create new characteristic instance
					if (isFoundAddressChar.isFalse()){
						addressCharsBoListNode = addressCharsBoList.newChild();
						addressCharsBoListNode.set(CmCustomerInterfaceConstants.CHARACTERISTICTYPE_ELE, charType.getId().getIdValue());
						
						if(charTypeLookup.isAdhocValue()){
							addressCharsBoListNode.set(CmCustomerInterfaceConstants.ADHOC_VAL_BO_ELE, charVal);
						}else if(charTypeLookup.isPredefinedValue()){
							addressCharsBoListNode.set(CmCustomerInterfaceConstants.CHARACTERISTICVALUE_ELE, charVal);
						}else if(charTypeLookup.isForeignKeyValue()){
							addressCharsBoListNode.set(CmCustomerInterfaceConstants.CHAR_VAL_FK1_BO_ELE, charVal);
						}
						
						if(notNull(charEffDate)){
							addressCharsBoListNode.set(CmCustomerInterfaceConstants.EFFECTIVE_DATE_ELE, charEffDate);
						}else{
							addressCharsBoListNode.set(CmCustomerInterfaceConstants.EFFECTIVE_DATE_ELE, effectiveDate);
						}	
					}								
				}
				BusinessObjectDispatcher.update(addressBoInstance);
			}
			//End Add - CB-52
			
			//Start Add - CB-54
			accountOverrideAddressId = acctOvrdAddr.getId().toString();
		    isAddressGroup = true;
			//End Add - CB - 54
		}
	}
	
	/**
	 * 
	 * @param accountIdsNode
	 * @param account
	 * @param isNewAccount
	 */
	
	private void addOrUpdateAccountIdentifiers(COTSInstanceNode accountIdsNode,Account account,Bool isNewAccount){

		COTSInstanceList accountIdNodeList = accountIdsNode.getList(CmCustomerInterfaceConstants.ID_ELE);
		String idType = null;
		String idValue =  null;
		Bool isPrimary = null;
		AccountNumber_Id accountNumberId = null;
		AccountNumber accountNumber =  null;
		AccountNumber_DTO accountNumberDto = null;
		String isPrimaryS = null;
		
		for(COTSInstanceNode accountIdNode :accountIdNodeList ){
			try {
				idType = !isBlankOrNull(accountIdNode.getString(CmCustomerInterfaceConstants.ID_TYPE_ELE))?
						accountIdNode.getString(CmCustomerInterfaceConstants.ID_TYPE_ELE):custExtLukupDataByCustClass.getAccountIdType();
				idValue = accountIdNode.getString(CmCustomerInterfaceConstants.ID_VALUE_ELE) ;
				isPrimaryS = accountIdNode.getString(CmCustomerInterfaceConstants.IS_PRIMARY_ELE);
				isPrimary = !isBlankOrNull(isPrimaryS)&&YesNoOptionLookup.constants.YES.trimmedValue().equals(isPrimaryS)?Bool.TRUE:Bool.FALSE;
				accountNumberId = new AccountNumber_Id(new AccountNumberType_Id(idType),account.getId());
				accountNumber = isNewAccount.isTrue()?null:accountNumberId.getEntity();
				accountNumberDto = notNull(accountNumber)?accountNumber.getDTO():createDTO(AccountNumber.class);
				accountNumberDto.setId(accountNumberId);
				accountNumberDto.setAccountNumber(idValue);
				accountNumberDto.setIsPrimaryId(notNull(isPrimary)?isPrimary:Bool.FALSE);
				if(notNull(accountNumber)){
					accountNumber.setDTO(accountNumberDto);
				}else{
					accountNumberDto.newEntity();
				}
			} catch (ApplicationWarning w) {
			}
		}
		
	}
	
	/**
	 * 
	 * @param accountAutoPaysNode
	 * @param account
	 * @param isNewAccount
	 */
	private void addOrUpdateAccountAutoPays(COTSInstanceNode accountAutoPaysNode,Account account,Bool isNewAccount){

		COTSInstanceList accountAutoPayNodeList = accountAutoPaysNode.getList(CmCustomerInterfaceConstants.AUTOPAY_ELE);
		AutopaySource apaySource = null;
		AutopayRouteType autopayRouteType = null;
		Date startDate = null;
		
		StringBuilder apayQueryFilter= new StringBuilder();
		apayQueryFilter.append(" where this.autopaySource=:autopaySource and this.autopayRouteType=:autopayRouteType ");
		apayQueryFilter.append(" and this.externalAccountId=:externalAccountId and :startDate between this.startDate and nvl(this.endDate,:startDate)" );
		
		for( COTSInstanceNode accountAutoPayNode:accountAutoPayNodeList ){
			startDate = accountAutoPayNode.getDate(CmCustomerInterfaceConstants.STARTDATE_ELE);
			apaySource = accountAutoPayNode.getEntity(CmCustomerInterfaceConstants.APAYSOURCECODE_ELE, AutopaySource.class);
			autopayRouteType =accountAutoPayNode.getEntity(CmCustomerInterfaceConstants.APAYROUTETYPE_ELE, AutopayRouteType.class);
			
			if(isNewAccount.isTrue()){
				addAccountAutopay(account, accountAutoPayNode, apaySource, autopayRouteType);
			}else{
				ListFilter<AccountAutopay> autopaysFilter = account.getAutopays().createFilter(apayQueryFilter.toString(),"apayQueryFilter");
				autopaysFilter.bindId("autopaySource", apaySource.getId());
				autopaysFilter.bindId("autopayRouteType", autopayRouteType.getId());
				autopaysFilter.bindStringProperty("externalAccountId", AccountAutopay.properties.externalAccountId, accountAutoPayNode.getString(CmCustomerInterfaceConstants.EXTERNALACCOUNTID_ELE));
				autopaysFilter.bindDate("startDate", startDate);
				AccountAutopay accountAutopay =	autopaysFilter.firstRow();
				if(isNull(accountAutopay)){
					addAccountAutopay(account, accountAutoPayNode, apaySource, autopayRouteType);
				}else{
					if(accountAutopay.getStartDate().equals(startDate)){
						updateAccountAutopay(accountAutopay, account, accountAutoPayNode, apaySource, autopayRouteType);
					}else{
						AccountAutopay_DTO apayDTO=accountAutopay.getDTO();
						apayDTO.setEndDate(startDate.addDays(-1));
						accountAutopay.setDTO(apayDTO);
						addAccountAutopay(account, accountAutoPayNode, apaySource, autopayRouteType);
					}
				}
			}
		}
		
	}
	
	/**
	 * 
	 * @param account
	 * @param accountAutoPayNode
	 * @param apaySource
	 * @param autopayRouteType
	 */
	private void addAccountAutopay(Account account,COTSInstanceNode accountAutoPayNode,AutopaySource apaySource
			,AutopayRouteType autopayRouteType){
		
		Money maxWithDrawl = accountAutoPayNode.getMoney(CmCustomerInterfaceConstants.AUTOPAYMAXWITHDRAWALAMOUNT_ELE);
		String expMonth = accountAutoPayNode.getString(CmCustomerInterfaceConstants.EXPMONTH_ELE);
		String expYear = accountAutoPayNode.getString(CmCustomerInterfaceConstants.EXPYEAR_ELE);
		
		Date expiryDate = null;
		if(!isBlankOrNull(expYear)&&!isBlankOrNull(expMonth)){
			expiryDate = new Date(Integer.parseInt(expYear),Integer.parseInt(expMonth),1);
			expiryDate = expiryDate.getMonthValue().getLastDayOfMonth(expiryDate.getYear());
		}
		
		AccountAutopay_DTO apayDTO= createDTO(AccountAutopay.class);
		apayDTO.setAccountId(account.getId());
	
		if(notNull(maxWithDrawl)){
			apayDTO.setAutopayMaxWithdrawalAmount(new Money(maxWithDrawl.getAmount(),account.getCurrency().getId()));
		}
		apayDTO.setAutoPayMethod(notNull(accountAutoPayNode.getLookup(CmCustomerInterfaceConstants.AUTOPAYMETHOD_ELE))?
				(AutoPayMethodLookup)accountAutoPayNode.getLookup(CmCustomerInterfaceConstants.AUTOPAYMETHOD_ELE)
				:custExtLukupDataByCustClass.getAutopayMethod());
		apayDTO.setAutopayRouteTypeId(autopayRouteType.getId());
		apayDTO.setAutopaySourceId(apaySource.getId());
		apayDTO.setAutoPayTypeFlg(notNull(accountAutoPayNode.getLookup(CmCustomerInterfaceConstants.AUTOPAYTYPE_ELE))?
				((AutoPayTypeFlgLookup)accountAutoPayNode.getLookup(CmCustomerInterfaceConstants.AUTOPAYTYPE_ELE )).trimmedValue()
				:custExtLukupDataByCustClass.getAutopayType().trimmedValue());
		apayDTO.setStartDate(accountAutoPayNode.getDate(CmCustomerInterfaceConstants.STARTDATE_ELE));
		if(notNull(accountAutoPayNode.getDate(CmCustomerInterfaceConstants.ENDDATE_ELE))){
			apayDTO.setEndDate(accountAutoPayNode.getDate(CmCustomerInterfaceConstants.ENDDATE_ELE));
		}
		apayDTO.setEntityName(accountAutoPayNode.getString(CmCustomerInterfaceConstants.ENTITYNAME_ELE));
		
		if(notNull(expiryDate))
			apayDTO.setExpireDate(expiryDate);
		apayDTO.setExternalAccountId(accountAutoPayNode.getString(CmCustomerInterfaceConstants.EXTERNALACCOUNTID_ELE));
		apayDTO.setPriorityNum(notNull(accountAutoPayNode.getNumber(CmCustomerInterfaceConstants.PRIORITYNUMBER_ELE))?
				accountAutoPayNode.getNumber(CmCustomerInterfaceConstants.PRIORITYNUMBER_ELE).toBigInteger():BigInteger.TEN);
		apayDTO.setPercent(notNull(accountAutoPayNode.getNumber(CmCustomerInterfaceConstants.PERCENT_ELE))?
				new BigDecimal(accountAutoPayNode.getNumber(CmCustomerInterfaceConstants.PERCENT_ELE).toString()):new BigDecimal("100"));
		apayDTO.newEntity();
	}
	
	private void updateAccountAutopay(AccountAutopay accountAutopay,Account account,COTSInstanceNode accountAutoPayNode,AutopaySource apaySource
			,AutopayRouteType autopayRouteType){

		Money maxWithDrawl = accountAutoPayNode.getMoney(CmCustomerInterfaceConstants.AUTOPAYMAXWITHDRAWALAMOUNT_ELE);
		String expMonth = accountAutoPayNode.getString(CmCustomerInterfaceConstants.EXPMONTH_ELE);
		String expYear = accountAutoPayNode.getString(CmCustomerInterfaceConstants.EXPYEAR_ELE);
		
		Date expiryDate = null;
		if(!isBlankOrNull(expYear)&&!isBlankOrNull(expMonth)){
			expiryDate = new Date(Integer.parseInt(expYear),Integer.parseInt(expMonth),1);
			expiryDate = expiryDate.getMonthValue().getLastDayOfMonth(expiryDate.getYear());
		}
		
		
		AccountAutopay_DTO apayDTO=  accountAutopay.getDTO();
		if(notNull(maxWithDrawl)){
			apayDTO.setAutopayMaxWithdrawalAmount(new Money(maxWithDrawl.getAmount(),account.getCurrency().getId()));
		}
		apayDTO.setAutoPayMethod(notNull(accountAutoPayNode.getLookup(CmCustomerInterfaceConstants.AUTOPAYMETHOD_ELE))?
				(AutoPayMethodLookup)accountAutoPayNode.getLookup(CmCustomerInterfaceConstants.AUTOPAYMETHOD_ELE)
				:custExtLukupDataByCustClass.getAutopayMethod());
		apayDTO.setAutopayRouteTypeId(autopayRouteType.getId());
		apayDTO.setAutopaySourceId(apaySource.getId());
		apayDTO.setAutoPayTypeFlg(notNull(accountAutoPayNode.getLookup(CmCustomerInterfaceConstants.AUTOPAYTYPE_ELE))?
				((AutoPayTypeFlgLookup)accountAutoPayNode.getLookup(CmCustomerInterfaceConstants.AUTOPAYTYPE_ELE )).trimmedValue()
				:custExtLukupDataByCustClass.getAutopayType().trimmedValue());
		apayDTO.setStartDate(accountAutoPayNode.getDate(CmCustomerInterfaceConstants.STARTDATE_ELE));
		if(notNull(accountAutoPayNode.getDate(CmCustomerInterfaceConstants.ENDDATE_ELE))){
			apayDTO.setEndDate(accountAutoPayNode.getDate(CmCustomerInterfaceConstants.ENDDATE_ELE));
		}
		apayDTO.setEntityName(accountAutoPayNode.getString(CmCustomerInterfaceConstants.ENTITYNAME_ELE));
		
		if(notNull(expiryDate))
			apayDTO.setExpireDate(expiryDate);
		apayDTO.setExternalAccountId(accountAutoPayNode.getString(CmCustomerInterfaceConstants.EXTERNALACCOUNTID_ELE));
		apayDTO.setPriorityNum(notNull(accountAutoPayNode.getNumber(CmCustomerInterfaceConstants.PRIORITYNUMBER_ELE))?
				accountAutoPayNode.getNumber(CmCustomerInterfaceConstants.PRIORITYNUMBER_ELE).toBigInteger():BigInteger.TEN);
		apayDTO.setPercent(notNull(accountAutoPayNode.getNumber(CmCustomerInterfaceConstants.PERCENT_ELE))?
				new BigDecimal(accountAutoPayNode.getNumber(CmCustomerInterfaceConstants.PERCENT_ELE).toString()):new BigDecimal("100"));
		accountAutopay.setDTO(apayDTO);
		
	}
	
	/**
	 * 
	 * @param accountCharsNode
	 * @param account
	 * @param effectiveDate
	 * @param isNewAccount
	 */
	private void addOrUpdateAccountCharacteristics(COTSInstanceNode accountCharsNode,Account account,Date effectiveDate,Bool isNewAccount){
		COTSInstanceList characteristicList = accountCharsNode.getList(CmCustomerInterfaceConstants.CHARACTERISTIC_ELE);
		CharacteristicType charType = null;
		AccountCharacteristic_DTO acctCharDTO = null;
		AccountCharacteristic accountChar = null;
		CharacteristicTypeLookup charTypeLookup = null;
		for(COTSInstanceNode characteristicNode : characteristicList){
			charType = characteristicNode.getEntity(CmCustomerInterfaceConstants.CHARACTERISTICTYPE_ELE,CharacteristicType.class);
			charTypeLookup = charType.getCharacteristicType();
			effectiveDate = notNull(characteristicNode.getDate(CmCustomerInterfaceConstants.EFFECTIVEDATE_ELE))
					?characteristicNode.getDate(CmCustomerInterfaceConstants.EFFECTIVEDATE_ELE)
							:effectiveDate;
					AccountCharacteristic_Id acctCharId = new AccountCharacteristic_Id(charType,account,effectiveDate);
			accountChar = isNewAccount.isTrue()?null:acctCharId.getEntity();
			acctCharDTO = notNull(accountChar)?accountChar.getDTO(): createDTO(AccountCharacteristic.class);
			acctCharDTO.setId(acctCharId);
			
			if(charTypeLookup.isAdhocValue()){
				acctCharDTO.setAdhocCharacteristicValue(characteristicNode.getString(CmCustomerInterfaceConstants.CHARACTERISTICVALUE_ELE));
			}else if(charTypeLookup.isPredefinedValue()){
				acctCharDTO.setCharacteristicValue(characteristicNode.getString(CmCustomerInterfaceConstants.CHARACTERISTICVALUE_ELE));
			}else if(charTypeLookup.isForeignKeyValue()){
				acctCharDTO.setCharacteristicValueForeignKey1(characteristicNode.getString(CmCustomerInterfaceConstants.CHARACTERISTICVALUE_ELE));
			}
			
			if(notNull(accountChar)){
				accountChar.setDTO(acctCharDTO);
			}else{
				acctCharDTO.newEntity();
			}
		}
	}
	
	/**
	 * 
	 * @param account
	 * @param saTypes
	 * @param isNewAccount
	 */
	//Start Change - CB-52
	//private void createContractEntity(Account account,List<String> saTypes,Bool isNewAccount){
	private void createContractEntity(Account account,List<String> saTypes,Bool isNewAccount,COTSInstanceNode contractNode){		
		COTSInstanceList contractList = contractNode.getList(CmCustomerInterfaceConstants.CONTRACT_ELE);
		COTSInstanceNode contractListNode = null;
	//End Change - CB-52

		Date startDate = account.getSetUpDate();
		ServiceAgreement_DTO saDto ;
		for(String saType:saTypes){
			saDto =  createDTO(ServiceAgreement.class);
			saDto = new ServiceAgreement_DTO();
			saDto.setAccountId(account.getId());
			saDto.setServiceAgreementTypeId(new ServiceAgreementType_Id(account.getDivisionId(),saType));
			saDto.setStatus(ServiceAgreementStatusLookup.constants.PENDING_START);
			saDto.setStartDate(startDate);
			saDto.setCustomerRead(CustomerReadLookup.constants.NO);
			ServiceAgreement serviceAgreement = saDto.newEntity();	
			serviceAgreement.activateOrTerminate(startDate);
			
			//Start Add CB-416
			ServiceAgreementTypeRateSchedule_Id rateScheduleId = null;
			ServiceAgreementType_Id saTypeId = new ServiceAgreementType_Id(account.getDivisionId(), saType);
			Iterator<ServiceAgreementTypeRateSchedule> rateSchedules = saTypeId.getEntity().getRateSchedules().iterator();
			while(rateSchedules.hasNext()){
				ServiceAgreementTypeRateSchedule saTypeRateSchedule = rateSchedules.next();
				if(saTypeRateSchedule.getUseRateAsDefault().isTrue()){
					rateScheduleId = saTypeRateSchedule.getId();
				}
			}
			ServiceAgreementRateScheduleHistory_Id rateHistory = new ServiceAgreementRateScheduleHistory_Id(serviceAgreement, startDate);
			ServiceAgreementRateScheduleHistory saRateSchHist = rateHistory.getEntity();
			if(isNull(saRateSchHist)){
				ServiceAgreementRateScheduleHistory_DTO rateHstDto = new ServiceAgreementRateScheduleHistory_DTO();
				if(!isNull(rateScheduleId.getEntity())){
					rateHstDto.setId(rateHistory);
					rateHstDto.setRateScheduleId(rateScheduleId.getEntity().fetchIdRateSchedule().getId());		
					rateHstDto.newEntity();					
				}
			}
			//End Add CB-416
			
			//Start Add - CB-52
			contractListNode = contractList.newChild();
			contractListNode.set(CmCustomerInterfaceConstants.SA_ID_ELE, serviceAgreement.getId().getIdValue());
			//End Add - CB-52
		}
	}
	
	/**
	 * 
	 * @param state
	 * @param country
	 * @return
	 */
	private Bool isValidStateCountryCombination(String state, String country) {
		Bool isStateCountryValid = Bool.FALSE;
		if(isBlankOrNull(state) || isBlankOrNull(country)) {
			return isStateCountryValid;
		} 
		State_Id stateId = new State_Id(new Country_Id(country), state);
		isStateCountryValid = isNull(stateId.getEntity())?Bool.FALSE:Bool.TRUE;
		return isStateCountryValid;
	}
	
	/**
	 * 
	 * @param personId
	 * @param messageNode
	 */
	//Start Update - CB-97
	//private void updateBOWithPersonEntity(Person_Id personId,COTSInstanceNode messageNode){
	private void updateBOWithPersonEntity(Person_Id personId,COTSInstanceNode messageNode, Bool isNewPerson){
	//End Update - CB-97	
		this.boInstance = BusinessObjectDispatcher.read(this.businessObjectInstKey, true);
		Element message2 = this.boInstance.getDocument().getRootElement().element(CmCustomerInterfaceConstants.MESSAGE_ELE);
		this.boInstance.getDocument().getRootElement().remove(message2);
		this.boInstance.getDocument().getRootElement().add(messageNode.getElement().createCopy());
		this.boInstance.set("person", personId.getTrimmedValue());
		BusinessObjectDispatcher.fastUpdate(this.boInstance.getDocument());
		
		//Start Update - CB-97
		//custIntfUtility.addFkCharLogEntries(CmMessageRepository.customerEntityCreated(personId.getTrimmedValue())
	    //		, this.inboundMessage, LogEntryTypeLookup.constants.SYSTEM,businessObject.getMaintenanceObject()
	    //		,getInbPerIdCharType(),personId.getTrimmedValue());
		if(isNewPerson.isTrue()){
			custIntfUtility.addFkCharLogEntries(CmMessageRepository.getServerMessage(CmMessages.PERSON_ENTITY_CREATED, personId.getTrimmedValue())
		    		, this.inboundMessage, LogEntryTypeLookup.constants.SYSTEM,businessObject.getMaintenanceObject()
		    		,getInbPerIdCharType(),personId.getTrimmedValue());
		}else{
			custIntfUtility.addFkCharLogEntries(CmMessageRepository.getServerMessage(CmMessages.PERSON_UPDATED, personId.getTrimmedValue())
		    		, this.inboundMessage, LogEntryTypeLookup.constants.SYSTEM,businessObject.getMaintenanceObject()
		    		,getInbPerIdCharType(),personId.getTrimmedValue());
		}
		//End Update - CB-97

	}
	
	//Start Add - CB-52
	/**
	 * This method retrieves effective address of person
	 * @param person
	 * @return address
	 */
	//Start Change - CB-256
	//private Address getEffectiveAddressEntity(String entityId, AddressTypeFlgLookup addressType, EntityFlagLookup entityFlag){
	private Address getEffectiveAddressEntity(String entityId, AddressTypeFlgLookup addressType, EntityFlagLookup entityFlag, Date effectiveDate){
	//End Change - CB-256
		
		//Start Change - CB-256
		/*Query<Address> getAddressQry = createQuery(
				"FROM Address address, " +
				"     AddressEntity addressEntity " +
				"WHERE address.id = addressEntity.id.address " +
				"  AND addressEntity.id.collectionEntityId = :entityId " +
				"  AND addressEntity.id.addressTypeFlg = :addressTypeFlag " +
				"  AND addressEntity.id.entityType = :entityType " +
				"  AND addressEntity.id.effectiveDate = ( " +
				"		SELECT MAX(addressEntity2.id.effectiveDate) " +
				"		FROM AddressEntity addressEntity2 " +
				"		WHERE addressEntity2.id.address = addressEntity.id.address " +
				"		AND addressEntity2.id.collectionEntityId = addressEntity.id.collectionEntityId " +
				"		AND addressEntity2.id.effectiveDate <= :processDate " +
				"  ) " , "");*/
		
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("FROM");
		stringBuilder.append(" Address address, ");
		stringBuilder.append(" AddressEntity addressEntity ");
		stringBuilder.append("WHERE");
		stringBuilder.append(" address.id = addressEntity.id.address ");
		stringBuilder.append(" AND addressEntity.id.collectionEntityId = :entityId ");
		stringBuilder.append(" AND addressEntity.id.addressTypeFlg = :addressTypeFlag ");
		stringBuilder.append(" AND addressEntity.id.entityType = :entityType ");
		
		if (isNull(effectiveDate)){
			stringBuilder.append(" AND addressEntity.id.effectiveDate = ( ");
			stringBuilder.append("     SELECT MAX(addressEntity2.id.effectiveDate) ");
			stringBuilder.append("     FROM AddressEntity addressEntity2 ");
			stringBuilder.append("     WHERE addressEntity2.id.address = addressEntity.id.address ");
			stringBuilder.append("     AND addressEntity2.id.collectionEntityId = addressEntity.id.collectionEntityId ");
			stringBuilder.append("     AND addressEntity2.id.effectiveDate <= :processDate) ");
		}else{
			stringBuilder.append(" AND addressEntity.id.effectiveDate = :effectiveDate ");
		}	
		
		Query<Address> getAddressQry = createQuery(stringBuilder.toString(), "");
		//End Change - CB-256	
		
		getAddressQry.bindStringProperty("entityId", AddressEntity.properties.collectionEntityId, entityId);
		getAddressQry.bindLookup("addressTypeFlag", addressType);
		getAddressQry.bindLookup("entityType", entityFlag);	
		
		//Start Add - CB-256
		if (isNull(effectiveDate)){
		//End Add - CB-256
			getAddressQry.bindDate("processDate", this.getProcessDateTime().getDate());
		//Start Add - CB-256
		}else{
			getAddressQry.bindDate("effectiveDate", effectiveDate);
		}
		//End Add - CB-256
			
		getAddressQry.addResult("address", "address");
		
		return getAddressQry.firstRow();
	}
	
	/**
	 * This method will add or update Person Address Entity
	 * @param person
	 * @param addressNode
	 * @param effectiveDate
	 */
	private void addOrUpdatePersonAddressEntity(Person person,COTSInstanceNode addressNode,Date effectiveDate){
		//Initialize
		COTSInstanceNode addressEntityBoListNode = null;
		COTSInstanceNode addressCharsBoListNode = null;
		COTSInstanceNode addressEntities =  null;
		COTSInstanceList addressEntityList = null;
		COTSInstanceNode addressCharacteristic=  null;
		COTSInstanceList addressCharacteristicList = null;
		CharacteristicType charType = null;
		CharacteristicTypeLookup charTypeLookup = null;
		Bool isNewAddress = null;
		String entityId = null;
		Date entityEffDate = null;
		//Start Change - CB-256
		//Lookup entityFlag = null;
		EntityFlagLookup entityFlag = null;
		//End Change - CB-256
		String charVal = null;
		Date charEffDate = null;
		COTSInstanceNode addressEntityBoNode = null;
		COTSInstanceList addressEntityBoList = null;		
		COTSInstanceNode addressCharsBoNode = null;
		COTSInstanceList addressCharsBoList = null;
		BusinessObjectInstance addressBoInstance;
		Bool isFoundAddressChar;
		//Start Add - CB-256
		Bool effectiveAddressTypeFound;
		Address address;
		String addressId;
		//End Add - CB-256
		
		//Start Add CB-54
		Bool first = Bool.TRUE;
		Bool isStatementAddress = Bool.FALSE;
		//End Add CB-54
				
		//Loop thru addresses and create address entity
		COTSInstanceList addressList = addressNode.getList(CmCustomerInterfaceConstants.ADDRESS_ELE);
		for(COTSInstanceNode addressListNode : addressList){
			//Start Add - CB-256
			address = null;
			addressId = CmCustomerInterfaceConstants.BLANK_VALUE;
			//End Add - CB-256
			addressBoInstance = null;
			addressBoInstance = BusinessObjectInstance.create(addressBo.getEntity());
			
			//Start Change - CB-256
			/*//Check if address id is provided.
			//If not, set isNewAddress to true.
			if (isBlankOrNull(addressListNode.getString(CmCustomerInterfaceConstants.ADDRESSES_ID_ELE))){
				isNewAddress = Bool.TRUE;
			//Otherwise, set isNewAdress to false and retrieve address details
			}else{
				isNewAddress = Bool.FALSE;
				addressBoInstance.set(CmCustomerInterfaceConstants.REQUEST_BO_ELE, addressListNode.getString(CmCustomerInterfaceConstants.ADDRESSES_ID_ELE));
				addressBoInstance = BusinessObjectDispatcher.read(addressBoInstance, true);
			}*/
			
			//Start Add - CB-256
			addressEntities = addressListNode.getGroup(CmCustomerInterfaceConstants.ADDRESS_ENTITIES_ELE);
			addressEntityList = addressEntities.getList(CmCustomerInterfaceConstants.ADDRESS_ENTITY_ELE);
			
			for (COTSInstanceNode addressEntityListNode : addressEntityList){
				entityEffDate = notNull(addressEntityListNode.getDate(CmCustomerInterfaceConstants.EFFECTIVE_DATE_ELE)) ?
						addressEntityListNode.getDate(CmCustomerInterfaceConstants.EFFECTIVE_DATE_ELE) : effectiveDate;
				entityFlag = notNull(addressEntityListNode.getLookup(CmCustomerInterfaceConstants.ENTITY_FLG_ELE)) ?
						(EntityFlagLookup) addressEntityListNode.getLookup(CmCustomerInterfaceConstants.ENTITY_FLG_ELE) : EntityFlagLookup.constants.PERSON;
				
				//Retrieve existing Address associated with input Effective Date, Address Type, and Person
				address = getEffectiveAddressEntity(person.getId().getIdValue(), 
						(AddressTypeFlgLookup) addressEntityListNode.getLookup(CmCustomerInterfaceConstants.ADDRESS_TYPE_ELE), 
						entityFlag,
						//Start Change - CB-177
						//null);
						addressEntityListNode.getDate(CmCustomerInterfaceConstants.EFFECTIVE_DATE_ELE));
						//End Change - CB-177
				
				if (notNull(address)){
					break;
				}
			}
			
			//If input Address is not provided, check if there's an existing address associated with the address input details
			if (isBlankOrNull(addressListNode.getString(CmCustomerInterfaceConstants.ADDRESSES_ID_ELE))){
				//If no existing address record, set isNewAddress to true
				if (isNull(address)){
					isNewAddress = Bool.TRUE;
				//Otherwise, set to false and retrieve Address Id
				}else{
					isNewAddress = Bool.FALSE;
					addressId = address.getId().getIdValue();
				}
			//Otherwise, set isNewAddress to false and retrieve Address Id
			}else{
				addressId = addressListNode.getString(CmCustomerInterfaceConstants.ADDRESSES_ID_ELE);
				isNewAddress = Bool.FALSE;
			}
			
			//If isNewAddress is false, retrieve address details
			if (isNewAddress.isFalse()){
				addressBoInstance.set(CmCustomerInterfaceConstants.REQUEST_BO_ELE, addressId);
				addressBoInstance = BusinessObjectDispatcher.read(addressBoInstance, true);
			}			
			//End Change - CB-256
			
			//Retrieve Address BO Elements
			addressEntityBoNode = addressBoInstance.getGroup(CmCustomerInterfaceConstants.ADDRESS_ENTITIES_BO_ELE);
			addressEntityBoList = addressEntityBoNode.getList(CmCustomerInterfaceConstants.ADDRESS_ENTITY_BO_ELE);		
			addressCharsBoNode = addressBoInstance.getGroup(CmCustomerInterfaceConstants.CHARACTERISTICS_ELE);
			addressCharsBoList = addressCharsBoNode.getList(CmCustomerInterfaceConstants.ADDRESS_CHARS_BO_ELE);
						
			//Set Business Object
			addressBoInstance.set(CmCustomerInterfaceConstants.BO_ELE, addressBo.getIdValue());
						
			//Set main address details
			addressBoInstance.set(CmCustomerInterfaceConstants.ADDRESS1_ELE, addressListNode.getString(CmCustomerInterfaceConstants.ADDRESS1_ELE));
			addressBoInstance.set(CmCustomerInterfaceConstants.ADDRESS2_ELE, addressListNode.getString(CmCustomerInterfaceConstants.ADDRESS2_ELE));
			addressBoInstance.set(CmCustomerInterfaceConstants.ADDRESS3_ELE, addressListNode.getString(CmCustomerInterfaceConstants.ADDRESS3_ELE));
			addressBoInstance.set(CmCustomerInterfaceConstants.ADDRESS4_ELE, addressListNode.getString(CmCustomerInterfaceConstants.ADDRESS4_ELE));
			addressBoInstance.set(CmCustomerInterfaceConstants.CITY_ELE, addressListNode.getString(CmCustomerInterfaceConstants.CITY_ELE));
			addressBoInstance.set(CmCustomerInterfaceConstants.STATE_ELE, addressListNode.getString(CmCustomerInterfaceConstants.STATE_ELE));
			addressBoInstance.set(CmCustomerInterfaceConstants.POSTAL_BO_ELE, addressListNode.getString(CmCustomerInterfaceConstants.ZIP_ELE));
			addressBoInstance.set(CmCustomerInterfaceConstants.COUNTY_ELE, addressListNode.getString(CmCustomerInterfaceConstants.COUNTY_ELE));
			addressBoInstance.set(CmCustomerInterfaceConstants.COUNTRY_ELE, addressListNode.getString(CmCustomerInterfaceConstants.COUNTRY_ELE));
			
			//Set address entities
			//Start Change - CB-256
			//addressEntities = addressListNode.getGroup(CmCustomerInterfaceConstants.ADDRESS_ENTITIES_ELE);
			//addressEntityList = addressEntities.getList(CmCustomerInterfaceConstants.ADDRESS_ENTITY_ELE);	
			entityEffDate = null;
			entityFlag = null;
			//End Change - CB-256
			for (COTSInstanceNode addressEntityListNode : addressEntityList){
				entityId = addressEntityListNode.getString(CmCustomerInterfaceConstants.ENTITY_ID_ELE);
				//Start Change - CB-256
				//entityEffDate = addressEntityListNode.getDate(CmCustomerInterfaceConstants.EFFECTIVE_DATE_ELE);
				//entityFlag = addressEntityListNode.getLookup(CmCustomerInterfaceConstants.ENTITY_FLG_ELE);
				entityEffDate = notNull(addressEntityListNode.getDate(CmCustomerInterfaceConstants.EFFECTIVE_DATE_ELE)) ?
						addressEntityListNode.getDate(CmCustomerInterfaceConstants.EFFECTIVE_DATE_ELE) : effectiveDate;
				entityFlag = notNull(addressEntityListNode.getLookup(CmCustomerInterfaceConstants.ENTITY_FLG_ELE)) ?
						(EntityFlagLookup) addressEntityListNode.getLookup(CmCustomerInterfaceConstants.ENTITY_FLG_ELE) : EntityFlagLookup.constants.PERSON;
				effectiveAddressTypeFound = Bool.FALSE;
				//End Change - CB-256
				
				//Start Add - CB-256
				for (COTSInstanceNode existingAddressEntityBoListNode : addressEntityBoList){					
					if (existingAddressEntityBoListNode.getDate(CmCustomerInterfaceConstants.EFFECTIVE_DATE_ELE).equals(entityEffDate)
							&& existingAddressEntityBoListNode.getLookup(CmCustomerInterfaceConstants.ADDRESS_TYPE_CD_BO_ELE).equals(addressEntityListNode.getLookup(CmCustomerInterfaceConstants.ADDRESS_TYPE_ELE))
							&& existingAddressEntityBoListNode.getString(CmCustomerInterfaceConstants.COLL_ENTITY_ID_BO_ELE).equals(person.getId().getIdValue())){
						effectiveAddressTypeFound = Bool.TRUE;
						break;
					}					
				}
				
				if (effectiveAddressTypeFound.isFalse()){
				//End Add - CB-256
					addressEntityBoListNode = addressEntityBoList.newChild();
					addressEntityBoListNode.set(CmCustomerInterfaceConstants.ADDRESS_TYPE_CD_BO_ELE, addressEntityListNode.getLookup(CmCustomerInterfaceConstants.ADDRESS_TYPE_ELE));
					
					if(!isBlankOrNull(entityId)){
						addressEntityBoListNode.set(CmCustomerInterfaceConstants.COLL_ENTITY_ID_BO_ELE, entityId);
					}else{
						addressEntityBoListNode.set(CmCustomerInterfaceConstants.COLL_ENTITY_ID_BO_ELE, person.getId().getIdValue());
					}
					
					if(notNull(entityEffDate)){
						addressEntityBoListNode.set(CmCustomerInterfaceConstants.EFFECTIVE_DATE_ELE, entityEffDate);
					}else{
						addressEntityBoListNode.set(CmCustomerInterfaceConstants.EFFECTIVE_DATE_ELE, effectiveDate);
					}
					
					if(notNull(entityFlag)){
						addressEntityBoListNode.set(CmCustomerInterfaceConstants.ENTITY_TYPE_BO_ELE, entityFlag);
					}else{
						addressEntityBoListNode.set(CmCustomerInterfaceConstants.ENTITY_TYPE_BO_ELE, EntityFlagLookup.constants.PERSON);
					}				
				//Start Add - CB-256
				}
				//End Add - CB-256
			
			}
			
			//Set address characteristics
			addressCharacteristic = addressListNode.getGroup(CmCustomerInterfaceConstants.CHARACTERISTICS_ELE);
			addressCharacteristicList = addressCharacteristic.getList(CmCustomerInterfaceConstants.CHARACTERISTIC_ELE);			
			for (COTSInstanceNode addressCharacteristicListNode : addressCharacteristicList){				
				charType = addressCharacteristicListNode.getEntity(CmCustomerInterfaceConstants.CHARACTERISTICTYPE_ELE,CharacteristicType.class);
				charTypeLookup = charType.getCharacteristicType();
				charVal = addressCharacteristicListNode.getString(CmCustomerInterfaceConstants.CHARACTERISTICVALUE_ELE);
				charEffDate = addressCharacteristicListNode.getDate(CmCustomerInterfaceConstants.EFFECTIVE_DATE_ELE);
				isFoundAddressChar = Bool.FALSE;
				
				//Start Add CB-54
				if(notNull(this.getAddressStmtIndicatorCharType()) && first.isTrue()){
					if(charType.equals(this.getAddressStmtIndicatorCharType()) && charVal.equalsIgnoreCase("Y")){
						isStatementAddress = Bool.TRUE;
						first = Bool.FALSE;
					}
				}
				//End Add CB-54
				
				for (COTSInstanceNode addressCharBo : addressCharsBoList){
					if (addressCharBo.getString(CmCustomerInterfaceConstants.CHARACTERISTICTYPE_ELE).equals(charType.getId().getIdValue())
							&& addressCharBo.getDate(CmCustomerInterfaceConstants.EFFECTIVE_DATE_ELE).equals(charEffDate)){
						isFoundAddressChar = Bool.TRUE;
						if(charTypeLookup.isAdhocValue()){
							addressCharBo.set(CmCustomerInterfaceConstants.ADHOC_VAL_BO_ELE, charVal);
						}else if(charTypeLookup.isPredefinedValue()){
							addressCharBo.set(CmCustomerInterfaceConstants.CHARACTERISTICVALUE_ELE, charVal);
						}else if(charTypeLookup.isForeignKeyValue()){
							addressCharBo.set(CmCustomerInterfaceConstants.CHAR_VAL_FK1_BO_ELE, charVal);
						}
					}
				}
				
				if (isFoundAddressChar.isFalse()){
					addressCharsBoListNode = addressCharsBoList.newChild();
					addressCharsBoListNode.set(CmCustomerInterfaceConstants.CHARACTERISTICTYPE_ELE, charType.getId().getIdValue());
					
					if(charTypeLookup.isAdhocValue()){
						addressCharsBoListNode.set(CmCustomerInterfaceConstants.ADHOC_VAL_BO_ELE, charVal);
					}else if(charTypeLookup.isPredefinedValue()){
						addressCharsBoListNode.set(CmCustomerInterfaceConstants.CHARACTERISTICVALUE_ELE, charVal);
					}else if(charTypeLookup.isForeignKeyValue()){
						addressCharsBoListNode.set(CmCustomerInterfaceConstants.CHAR_VAL_FK1_BO_ELE, charVal);
					}
					
					if(notNull(charEffDate)){
						addressCharsBoListNode.set(CmCustomerInterfaceConstants.EFFECTIVE_DATE_ELE, charEffDate);
					}else{
						addressCharsBoListNode.set(CmCustomerInterfaceConstants.EFFECTIVE_DATE_ELE, effectiveDate);
					}	
				}			
			}
			
			//If this is a new address, invoke BO for add and return Address Id
			if(isNewAddress.isTrue()){
				BusinessObjectInstance newAddressBOInstance = BusinessObjectDispatcher.add(addressBoInstance);
			
				//Start Add CB-54
				if(isStatementAddress.isTrue()){
					statementAddressId = newAddressBOInstance.getString(CmCustomerInterfaceConstants.REQUEST_BO_ELE);
					isStatementAddress = Bool.FALSE;
				}
				//End Add CB-54				
				
				addressListNode.set(CmCustomerInterfaceConstants.ADDRESSES_ID_ELE, newAddressBOInstance.getString(CmCustomerInterfaceConstants.REQUEST_BO_ELE));				
			//Otherwise, invoke BO for update.
			}else{
				BusinessObjectDispatcher.update(addressBoInstance);
				//Start Add - CB-256
				addressListNode.set(CmCustomerInterfaceConstants.ADDRESSES_ID_ELE, addressId);
				//End Add - CB-256
			}			
		}
	}
	
	/**
	 * This method will add or update Person Address
	 * @param mainCustomerNode
	 * @param person
	 */
	public void addOrUpdatePersonAddress(COTSInstanceNode mainCustomerNode, Person person){
		//Initialize
        LookupValue personType = mainCustomerNode.getLookup(CmCustomerInterfaceConstants.PERSON_TYPE_ELE).getLookupValue();
		COTSInstanceNode addressNode = mainCustomerNode.getGroup(CmCustomerInterfaceConstants.ADDRESSES_ELE);
		COTSInstanceList addressList = addressNode.getList(CmCustomerInterfaceConstants.ADDRESS_ELE);
		COTSInstanceNode addressEntity = null;
		COTSInstanceList addressEntityList = null;
		COTSInstanceNode addressCharacteristics = null;
		COTSInstanceList addressCharacteristicList = null;
		List<COTSInstanceNode> addressTypeEntityList = null;
		Bool isBillToAddressTypeFound = Bool.FALSE;
		Bool isBillToAddressIndicatorFound = Bool.FALSE;
		Bool isStateCountryValid = Bool.FALSE;
		String address1 = null;
		String address2 = null;
		String address3 = null;
		String address4 = null;
		String city = null;
		String county = null;
		String country = null;
		String state = null;
		String zip = null;
		Person_DTO perDto = notNull(person)? person.getDTO():createDTO(Person.class);
		
    	for (COTSInstanceListNode addressListNode: addressList){
        	//Retrieve Address
    		address1 = addressListNode.getString(CmCustomerInterfaceConstants.ADDRESS1_ELE);
    		address2 = addressListNode.getString(CmCustomerInterfaceConstants.ADDRESS2_ELE);
    		address3 = addressListNode.getString(CmCustomerInterfaceConstants.ADDRESS3_ELE);
    		address4 = addressListNode.getString(CmCustomerInterfaceConstants.ADDRESS4_ELE);
    		city = addressListNode.getString(CmCustomerInterfaceConstants.CITY_ELE);
    		county = addressListNode.getString(CmCustomerInterfaceConstants.COUNTY_ELE);
    		country = addressListNode.getString(CmCustomerInterfaceConstants.COUNTRY_ELE);
    		state = addressListNode.getString(CmCustomerInterfaceConstants.STATE_ELE);
    		zip = addressListNode.getString(CmCustomerInterfaceConstants.ZIP_ELE);
    		isStateCountryValid = isValidStateCountryCombination(state, country);
    		
    		//Retrieve Address Entities and Characteristics
    		addressEntity = addressListNode.getGroup(CmCustomerInterfaceConstants.ADDRESS_ENTITIES_ELE);
    		addressEntityList = addressEntity.getList(CmCustomerInterfaceConstants.ADDRESS_ENTITY_ELE);
        	addressCharacteristics = addressListNode.getGroup(CmCustomerInterfaceConstants.CHARACTERISTICS_ELE);  	
        	addressCharacteristicList = addressCharacteristics.getList(CmCustomerInterfaceConstants.CHARACTERISTIC_ELE);
        	
        	//If Person Type is Main Customer, set person address using Bill To Address
    		if (personType.equals(this.getMainCustomerPersonType().getLookupValue())){
            	//Get Bill To Address Type List
            	addressTypeEntityList = addressEntityList.getElementsWhere("[addressType = '"+ this.getBillToAddressType().getLookupValue().fetchIdFieldValue() +"' ]");
            	
            	//Check if Bill To Address Type is provided
            	if (addressTypeEntityList.size() == 1){
            		isBillToAddressTypeFound = Bool.TRUE;
            	}
            	
            	//Check if Primary Bill To Address Indicator is provided
            	for (COTSInstanceListNode addressCharacteristicListNode: addressCharacteristicList) {
            		if (addressCharacteristicListNode.getString(CmCustomerInterfaceConstants.CHARACTERISTICTYPE_ELE).equals(this.getAddressBillToIndicatorCharType().getId().getIdValue())
            				&& addressCharacteristicListNode.getString(CmCustomerInterfaceConstants.CHARACTERISTICVALUE_ELE).equals(CmCustomerInterfaceConstants.PRIMARY)){
            			isBillToAddressIndicatorFound = Bool.TRUE;        			
            			break;
            		}
            	}
            	
            	//If Bill To Address Type and Primary Bill To Address Indicator are provided, set address
            	if (isBillToAddressTypeFound.isTrue() && isBillToAddressIndicatorFound.isTrue()){
            		
            		if(!isBlankOrNull(address1)){
            			perDto.setAddress1(address1);
            		}
            		
            		if(!isBlankOrNull(address2)){
            			perDto.setAddress2(address2);
            		}
            		
            		if(!isBlankOrNull(address3)){
            			perDto.setAddress3(address3);
            		}
            		
            		if(!isBlankOrNull(address4)){
            			perDto.setAddress4(address4);
            		}
            		
            		if(!isBlankOrNull(city)){
            			perDto.setCity(city);
            		}
            		
            		if(!isBlankOrNull(county)){
            			perDto.setCounty(county);
            		}
            		
            		if(!isBlankOrNull(country)){
            			perDto.setCountry(country);
            		} 

            		if(isStateCountryValid.isTrue()) {
            			perDto.setState(state);	
            		} 

            		if(!isBlankOrNull(zip)){
            			perDto.setPostal(zip);
            		}   
            		 
            		//Set Person DTO
            		perDto.setLastUpdatedBy(getActiveContextUser().getId().getTrimmedValue());
            		perDto.setLastUpdatedDTTM(getProcessDateTime());
            		person.setDTO(perDto);
            		
            		break;
            	}
            //Otherwise, set person address using first record from list
    		}else{
        		if(!isBlankOrNull(address1)){
        			perDto.setAddress1(address1);
        		}
        		
        		if(!isBlankOrNull(address2)){
        			perDto.setAddress2(address2);
        		}
        		
        		if(!isBlankOrNull(address3)){
        			perDto.setAddress3(address3);
        		}
        		
        		if(!isBlankOrNull(address4)){
        			perDto.setAddress4(address4);
        		}
        		
        		if(!isBlankOrNull(city)){
        			perDto.setCity(city);
        		}
        		
        		if(!isBlankOrNull(county)){
        			perDto.setCounty(county);
        		}
        		
        		if(!isBlankOrNull(country)){
        			perDto.setCountry(country);
        		} 

        		if(isStateCountryValid.isTrue()) {
        			perDto.setState(state);	
        		} 

        		if(!isBlankOrNull(zip)){
        			perDto.setPostal(zip);
        		} 
        		
        		//Set Person DTO
        		perDto.setLastUpdatedBy(getActiveContextUser().getId().getTrimmedValue());
        		perDto.setLastUpdatedDTTM(getProcessDateTime());
        		person.setDTO(perDto);
        		break;
    		}
    	}
	}
	
	/**
	 * This method checks if the Characteristic Type is valid for an Entity.
	 * @param Characteristic Type to Validate
	 * @param Entity to be checked on
	 * @param Description of the Soft Parameter
	 */
	private void validateCharacteristicType(CharacteristicType charType, CharacteristicEntityLookup charEntLkup,
			String parmDesc){
		CharacteristicEntity_Id charEntityId = new CharacteristicEntity_Id(charType, charEntLkup);
		
		if(isNull(charEntityId.getEntity())){			
			addError(CmMessageRepository.getServerMessage(CmMessages.CHAR_TYPE_INVALID_FOR_ENTITY,
					parmDesc,charType.getId().getIdValue(),
					charEntLkup.getLookupValue().fetchLanguageDescription()));
		}
	}
	//End Add - CB-52
	
	//Start Add - CB-177
	/**
	 * This method determine if person has existing primary bill to/ship to address
	 * @param person
	 * @return address
	 */
	private Address getPersonPrimaryAddress(Person person, AddressTypeFlgLookup addressType, CharacteristicType addressCharType){
		Query<Address> getAddressQry = createQuery(
				"FROM Address address, " +
				"     AddressEntity addressEntity, " +
				"     AddressCharacteristic addressChar " +
				"WHERE address.id = addressChar.id.address " +
				"  AND addressEntity.id.address = addressChar.id.address " +
				"  AND addressChar.id.characteristicType = :addressCharType " +
				"  AND addressChar.searchCharacteristicValue= :primary " +
				"  AND addressEntity.id.collectionEntityId = :entityId " +
				"  AND addressEntity.id.addressTypeFlg = :addressTypeFlag " +
				"  AND addressEntity.id.entityType = :entityType ", "");
		
		getAddressQry.bindEntity("addressCharType", addressCharType);
		getAddressQry.bindStringProperty("primary", AddressCharacteristic.properties.searchCharacteristicValue, CmCustomerInterfaceConstants.PRIMARY);
		getAddressQry.bindStringProperty("entityId", AddressEntity.properties.collectionEntityId, person.getId().getIdValue());
		getAddressQry.bindLookup("addressTypeFlag", addressType);
		getAddressQry.bindLookup("entityType", EntityFlagLookup.constants.PERSON);
		getAddressQry.addResult("address", "address");
		
		return getAddressQry.firstRow();
		
	}
	//End Add - CB-177
	
	//Start Add CB-54
	/*
	 * This method calls either addStatementConstructEntity() or updateStatementConstructEntity() 
	 * @param Person Entity
	 * @Statement Node
	 * Boolean isNew
	 * */
	public void addorUpdateStatementConstruct(Person person,COTSInstanceNode statementNode, Bool isNew){	
		if(isNew.isTrue() && !isBlankOrNull(statementNode.getString(CmCustomerInterfaceConstants.STATEMENT_ROUTE_TYPE_ELE))){
				addStatementConstructEntity(person,statementNode);
		}else{
				if(!isBlankOrNull(statementNode.getString(CmCustomerInterfaceConstants.STATEMENT_CNST_ID_ELE))){
					updateStatementConstructEntity(person,statementNode);					
				}
			}	
	}
	
	/*
	 * This method will create Statement Construct Entity
	 * @param Person Entity
	 * @param Statement Node
	 * */
	public void addStatementConstructEntity(Person person,COTSInstanceNode statementNode){
		BusinessObjectInstance statementConstructInstance = BusinessObjectInstance.create("CM-StatementConstruct");
		String description = isBlankOrNull(statementNode.getString(CmCustomerInterfaceConstants.DESCRIPTION_ELE))?
				custExtLukupDataByCustClass.getStatementDescription():statementNode.getString(CmCustomerInterfaceConstants.DESCRIPTION_ELE);
		statementConstructInstance.set(CmCustomerInterfaceConstants.DESCRIPTION_ELE,description);
		if(isNull(statementNode.getNumber(CmCustomerInterfaceConstants.NBR_OF_COPIES))){
			if(!isNull(this.getCustIntfCustClassExtLookup())){
				if(isNull(custExtLukupDataByCustClass.getNbrOfCopies())){
					statementConstructInstance.set(CmCustomerInterfaceConstants.NBROFCOPIES_ELE,BigDecimal.valueOf(1));
				}else{
					statementConstructInstance.set(CmCustomerInterfaceConstants.NBROFCOPIES_ELE,new BigDecimal(custExtLukupDataByCustClass.getNbrOfCopies().toString()));
				}
			}else{
				statementConstructInstance.set(CmCustomerInterfaceConstants.NBROFCOPIES_ELE,new BigDecimal(custExtLukupDataByCustClass.getNbrOfCopies().toString()));
			}
		}else{
			statementConstructInstance.set(CmCustomerInterfaceConstants.NBROFCOPIES_ELE,statementNode.getNumber(CmCustomerInterfaceConstants.NBR_OF_COPIES));
		}
		if(!isBlankOrNull(statementNode.getString(CmCustomerInterfaceConstants.STATEMENT_CYCLE_ELE))){
			statementConstructInstance.set(CmCustomerInterfaceConstants.STATEMENT_CYCLE_ELE,statementNode.getString(CmCustomerInterfaceConstants.STATEMENT_CYCLE_ELE).trim());
		}
		if(!isNull(statementNode.getLookup(CmCustomerInterfaceConstants.STATEMENT_FORMAT_ELE))){
			statementConstructInstance.set(CmCustomerInterfaceConstants.STATEMENT_FORMAT_ELE, statementNode.getLookup(CmCustomerInterfaceConstants.STATEMENT_FORMAT_ELE));
		}else{
			statementConstructInstance.set(CmCustomerInterfaceConstants.STATEMENT_FORMAT_ELE, custExtLukupDataByCustClass.getStatementFormat());
		}
		statementConstructInstance.set(CmCustomerInterfaceConstants.PERSONID_ELE, person.getId().getTrimmedValue());
		statementConstructInstance.set(CmCustomerInterfaceConstants.EFF_STATUS_ELE,EffectiveStatusLookup.constants.ACTIVE);
		if(!isBlankOrNull(statementNode.getString(CmCustomerInterfaceConstants.STATEMENT_ROUTE_TYPE_ELE))){
			statementConstructInstance.set(CmCustomerInterfaceConstants.STATEMENT_ROUTE_TYPE_ELE, statementNode.getString(CmCustomerInterfaceConstants.STATEMENT_ROUTE_TYPE_ELE).trim());
		}
		if(this.getAccountOverrideAllowed().isYes() && isAddressGroup){
			statementConstructInstance.set(CmCustomerInterfaceConstants.STATEMENT_ADDRESS_SOURCE_ELE,StatementAddressSourceLookup.constants.ACCOUNT_OVERRIDE);
			statementConstructInstance.set(CmCustomerInterfaceConstants.ADDRESSES_ID_ELE,accountOverrideAddressId);
		}
		else{
				if(notNull(this.getAddressStmtIndicatorCharType())){
					statementConstructInstance.set(CmCustomerInterfaceConstants.ADDRESSES_ID_ELE,statementAddressId);
					String addressTypeFlg = getAddressSource();
					if(!isBlankOrNull(addressTypeFlg)){
						if(addressTypeFlg.equalsIgnoreCase("BLTO")){
						statementConstructInstance.set(CmCustomerInterfaceConstants.STATEMENT_ADDRESS_SOURCE_ELE,StatementAddressSourceLookup.constants.BILL_TO);
					}
					else if(addressTypeFlg.equalsIgnoreCase("SHTO")){
						statementConstructInstance.set(CmCustomerInterfaceConstants.STATEMENT_ADDRESS_SOURCE_ELE,StatementAddressSourceLookup.constants.SHIP_TO);
					}
				}
			}else{
				Address address = getPersonPrimaryAddress(person,this.getBillToAddressType(),this.getAddressBillToIndicatorCharType());
				statementConstructInstance.set(CmCustomerInterfaceConstants.STATEMENT_ADDRESS_SOURCE_ELE,StatementAddressSourceLookup.constants.BILL_TO);
				statementConstructInstance.set(CmCustomerInterfaceConstants.ADDRESSES_ID_ELE,address.getId().getTrimmedValue());
			}
		}
		COTSInstanceNode statementConstDtlsList = statementNode.getGroup(CmCustomerInterfaceConstants.STATEMENT_CNST_DETAILS_ELE);
		if(!isNull(statementConstDtlsList)){
			COTSInstanceListNode stmtDetail = null;
			COTSInstanceList stmDtlList = statementConstructInstance.getList(CmCustomerInterfaceConstants.STATEMENT_CNST_DETAIL_ELE);
			COTSInstanceList stmtConstDtlLst = statementConstDtlsList.getList(CmCustomerInterfaceConstants.STATEMENT_CNST_DETAIL_ELE);
			for(COTSInstanceNode stmtConstDtls:stmtConstDtlLst){
			stmtDetail = stmDtlList.newChild();
			stmtDetail.set(CmCustomerInterfaceConstants.ACCOUNTID_ELE,cnstDtlAccountId);
			if(!isNull(stmtConstDtls.getDate(CmCustomerInterfaceConstants.STARTDATE_ELE))){
				stmtDetail.set(CmCustomerInterfaceConstants.STARTDATE_ELE,stmtConstDtls.getDate(CmCustomerInterfaceConstants.STARTDATE_ELE));				
			}
			if(!isNull(stmtConstDtls.getDate(CmCustomerInterfaceConstants.ENDDATE_ELE))){
				stmtDetail.set(CmCustomerInterfaceConstants.ENDDATE_ELE,stmtConstDtls.getDate(CmCustomerInterfaceConstants.ENDDATE_ELE));				
			}
			stmtDetail.set(CmCustomerInterfaceConstants.PRINT_ORDER_ELE,stmtConstDtls.getNumber(CmCustomerInterfaceConstants.PRINT_ORDER_ELE));
			if(isBlankOrNull(stmtConstDtls.getString(CmCustomerInterfaceConstants.STATEMENT_PRINT_DESC_ELE))){
				stmtDetail.set(CmCustomerInterfaceConstants.STATEMENT_PRINT_DESC_ELE,custExtLukupDataByCustClass.getStatementDescription());
			}else{
				stmtDetail.set(CmCustomerInterfaceConstants.STATEMENT_PRINT_DESC_ELE,stmtConstDtls.getString(CmCustomerInterfaceConstants.STATEMENT_PRINT_DESC_ELE));
			}
			if(isNull(stmtConstDtls.getLookup(CmCustomerInterfaceConstants.CNST_DETAIL_TYPE_ELE))){
				stmtDetail.set(CmCustomerInterfaceConstants.CNST_DETAIL_TYPE_ELE, custExtLukupDataByCustClass.getStatementDetailType());				
			}
			else{
				stmtDetail.set(CmCustomerInterfaceConstants.CNST_DETAIL_TYPE_ELE, stmtConstDtls.getLookup(CmCustomerInterfaceConstants.CNST_DETAIL_TYPE_ELE));
			}
			}
		}
		statementConstructInstance = BusinessObjectDispatcher.execute(statementConstructInstance, BusinessObjectActionLookup.constants.ADD);
		statementNode.set(CmCustomerInterfaceConstants.STATEMENT_CNST_ID_ELE, statementConstructInstance.getString(CmCustomerInterfaceConstants.STATEMENT_CNST_ID_ELE));
	}
	/*
	 * This method will Returns ADDRESS_TYPE_FLG 
	 * @return  ADDRESS_TYPE_FLG
	 * */
	public String getAddressSource(){
	PreparedStatement addressSourceStatement = null;
	StringBuilder addressSourceQuery = new StringBuilder();
	addressSourceQuery.append(" SELECT ADDRESS_TYPE_FLG FROM C1_ADDRESS_ENTITY WHERE ADDRESS_ID=:addressId AND ADDRESS_TYPE_FLG <> 'MAIN' ");
	addressSourceStatement = createPreparedStatement(addressSourceQuery.toString(),"Fetching Address Type Flag");
	addressSourceStatement.bindString("addressId",statementAddressId.trim(),"ADDRESS_ID");
	if(notNull(addressSourceStatement.firstRow())){
		return addressSourceStatement.firstRow().getString("ADDRESS_TYPE_FLG").trim();
	}else{
		return null;
	}
	}
	
	/*
	 * This method will Update Statement Construct
	 * @param Person Entity
	 * @param Statement Node
	 * */
	public void updateStatementConstructEntity(Person person,COTSInstanceNode statementNode){
	BusinessObjectInstance statementConstructInstance = BusinessObjectInstance.create("CM-StatementConstruct");
	String statementConstructId = statementNode.getString(CmCustomerInterfaceConstants.STATEMENT_CNST_ID_ELE);
	if(!isBlankOrNull(statementConstructId)){
		statementConstructInstance.set(CmCustomerInterfaceConstants.STATEMENT_CNST_ID_ELE,statementConstructId);
		statementConstructInstance = BusinessObjectDispatcher.read(statementConstructInstance, true);
		StatementConstruct_Id statementCnId = new StatementConstruct_Id(statementConstructId);
		StatementConstruct statementConstruct = statementCnId.getEntity();
		StatementConstruct_DTO statementCnstDto = statementConstruct.getDTO();
		String personIdFromStmCnst = statementConstructInstance.getString(CmCustomerInterfaceConstants.PERSONID_ELE);
		if(notNull(statementConstructInstance) && personIdFromStmCnst.trim().equals(person.getId().getTrimmedValue())){
			if(!isBlankOrNull(statementNode.getString(CmCustomerInterfaceConstants.DESCRIPTION_ELE))){
				statementCnstDto.setDescription(statementNode.getString(CmCustomerInterfaceConstants.DESCRIPTION_ELE));
			}
			if(!isNull(statementNode.getNumber(CmCustomerInterfaceConstants.NBR_OF_COPIES))){
				statementCnstDto.setNumberOfCopies(statementNode.getNumber(CmCustomerInterfaceConstants.NBR_OF_COPIES).toBigInteger());
			}
			if(!isBlankOrNull(statementNode.getString(CmCustomerInterfaceConstants.STATEMENT_ROUTE_TYPE_ELE))){
				statementConstructInstance.set(CmCustomerInterfaceConstants.STATEMENT_ROUTE_TYPE_ELE,statementNode.getString(CmCustomerInterfaceConstants.STATEMENT_ROUTE_TYPE_ELE).trim());
				StatementRoutingType_Id stmtRouteId = new StatementRoutingType_Id(statementNode.getString(CmCustomerInterfaceConstants.STATEMENT_ROUTE_TYPE_ELE).trim());
				statementCnstDto.setStatementRouteTypeId(stmtRouteId);
			}
			if(!isNull(statementNode.getLookup(CmCustomerInterfaceConstants.STATEMENT_FORMAT_ELE))){
				statementCnstDto.setStatementFormat((StatementFormatLookup) statementNode.getLookup(CmCustomerInterfaceConstants.STATEMENT_FORMAT_ELE));
			}
			if(!isBlankOrNull(statementNode.getString(CmCustomerInterfaceConstants.STATEMENT_CYCLE_ELE))){
				StatementCycle_Id stmtCycleId = new StatementCycle_Id(statementNode.getString(CmCustomerInterfaceConstants.STATEMENT_CYCLE_ELE));
				statementCnstDto.setStatementCycleId(stmtCycleId);
			}
			statementConstruct.setDTO(statementCnstDto);
		}
	}
	}

	//End Add CB-54
	
	public BusinessObjectStatusCode getNextStatus() {
		return nextBoStatus;
	}


	@Override
	protected void extraSoftParameterValidations(boolean forAlgorithmValidation) {
		/*if(!isBlankOrNull(getNextStatusIfNoChildPerson())){
			BusinessObjectStatus_Id id = new BusinessObjectStatus_Id(businessObject.getId(), getNextStatusIfNoChildPerson());
				//BusinessObjectStatusCode nextBoStatus1 = new BusinessObjectStatusCode(businessObject.getId(), getNextStatusIfNoChildPerson());
				if(isNull(id.getEntity()))
					addError(CmMessageRepository.invalidNextBOStatus(businessObject.getId().getTrimmedValue(), getNextStatusIfNoChildPerson()));
		}*/
		
		//Start Add - CB-52
    	//Retrieve Algorithm Parameter Descriptions
    	String addressBillToIndicatorCharTypeDesc = getAlgorithm().getAlgorithmType().getParameterAt(CmCustomerInterfaceConstants.PARM_SETUP_IDX_ADDR_BILLTO_IND_CHAR_TYPE).fetchLanguageParameterLabel().trim();
    	String addressShipToIndicatorCharTypeDesc = getAlgorithm().getAlgorithmType().getParameterAt(CmCustomerInterfaceConstants.PARM_SETUP_IDX_ADDR_SHPTO_IND_CHAR_TYPE).fetchLanguageParameterLabel().trim();
    	
    	//Retrieve Soft Parameters
    	addressBillToIndicatorCharType = getAddressBillToIndicatorCharType();
    	addressShipToIndicatorCharType = getAddressShipToIndicatorCharType();
    	
    	//Check that Address Bill To Characteristic Type is valid for Address Characteristic entity
    	validateCharacteristicType(addressBillToIndicatorCharType,CharacteristicEntityLookup.constants.ADDRESS_CHARACTERISTIC,addressBillToIndicatorCharTypeDesc);
    	
    	//Check that Address Ship To Characteristic Type is valid for Address Characteristic entity
    	validateCharacteristicType(addressShipToIndicatorCharType,CharacteristicEntityLookup.constants.ADDRESS_CHARACTERISTIC,addressShipToIndicatorCharTypeDesc);
    	//End Add - CB-52
	}



	public BusinessObjectStatusTransitionConditionLookup getNextStatusCondition() {
		return nextStatusTransitionConditionLookup;
	}



	public boolean getForcePostProcessing() {
		return false;
	}
	

	public BusinessObjectStatusReason_Id getStatusChangeReasonId() {
		return null;
	}


	public boolean getUseDefaultNextStatus() {
		return useDefaultNextStatus;
	}

	BusinessObjectActionLookup boLookup;
	public void setAction(BusinessObjectActionLookup arg0) {
		boLookup = arg0;
	}


	public void setBusinessObject(BusinessObject arg0) {
		businessObject = arg0;
	}


	public void setBusinessObjectKey(BusinessObjectInstanceKey boKey) {
		businessObjectInstKey = boKey;
	}

}