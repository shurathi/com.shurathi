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
 *   - Person-Person Relationship
 *  
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:           Reason:
 * 2020-04-13   VLaksh        CB-10 Initial Version. 
 * 2020-05-12   KGhuge		  CB-50	Remove Soft Parameter(WAITING)	
 * 2020-05-26	DDejes		  CB-86 Added Alert Creation
 * 2020-06-26   JFerna        CB-133 Updated person person
 *                            add and update logic
 * 2020-07-01	DDejes/JFerna CB-132 Added Next Bo Status Logic  
 * 2020-07-07   JFerna        CB-176 Updated logic that transitions
 *                            Parent Inbound Message to next default
 *                            status 
 ***********************************************************************
 */
package com.splwg.cm.domain.customer.businessObject;

import java.math.BigInteger;
import java.util.List;

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
import com.splwg.base.api.lookup.BusinessObjectActionLookup;
import com.splwg.base.api.lookup.LogEntryTypeLookup;
import com.splwg.base.api.lookup.YesNoOptionLookup;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectEnterStatusAlgorithmSpot;
import com.splwg.base.domain.common.businessObjectStatusReason.BusinessObjectStatusReason_Id;
import com.splwg.base.domain.common.lookup.LookupValue;
import com.splwg.base.support.context.SessionHolder;
import com.splwg.ccb.domain.admin.alertType.AlertType_Id;
import com.splwg.ccb.domain.admin.personRelationshipType.PersonRelationshipType_Id;
import com.splwg.ccb.domain.customerinfo.account.Account;
import com.splwg.ccb.domain.customerinfo.account.AccountAlert;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.customerinfo.person.Person;
import com.splwg.ccb.domain.customerinfo.person.PersonPerson;
import com.splwg.ccb.domain.customerinfo.person.PersonPerson_DTO;
import com.splwg.ccb.domain.customerinfo.person.PersonPerson_Id;
import com.splwg.ccb.domain.interfaces.inboundMessage.InboundMessage;
import com.splwg.ccb.domain.interfaces.inboundMessage.InboundMessage_Id;
import com.splwg.cm.api.lookup.BusinessObjectStatusTransitionConditionLookup;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;
import com.splwg.cm.domain.customer.utility.CmCustInterfaceUtilityBussComp;
import com.splwg.cm.domain.customer.utility.CmCustomerInterfaceConstants;
import com.splwg.shared.common.ApplicationError;


/**
 * @author vguddeti
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = alertType, name = alertType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = personOrBusiness, name = personType, required = true, type = lookup)})
 */
public class CmManageCustomerRelationAlgComp_Impl 
	extends CmManageCustomerRelationAlgComp_Gen
	implements BusinessObjectEnterStatusAlgorithmSpot {

	private BusinessObject businessObject = null;
    private BusinessObjectInstanceKey businessObjectInstKey = null;
	private BusinessObjectInstance boInstance = null;
	private BusinessObjectStatusCode nextBoStatus = null;
	private boolean useDefaultNextStatus;
	private BusinessObjectStatusTransitionConditionLookup nextStatusTransitionConditionLookup;
	private InboundMessage inboundMessage = null;
	
	private CmCustInterfaceUtilityBussComp custIntfUtility = null; 

	//Start Add-CB86
	private BusinessObjectInstance acctBoInstance;
	private COTSInstanceList accountAlertList;
	private COTSInstanceListNode accountAlertNode;
	private String alertType;
	
	//Start Add CB-133
	private PersonPerson_Id perPerId;
	private PersonPerson_DTO perPerDTO;
	private Bool isPerPerRelationAdded;
	//End Add - CB-133
	
	//Constants
	private static final String ACCTBO = "C1-AccountBO";
	//End Add-CB86	
	//Start Add - CB-133
	//Start Delete - CB-176
	//private static final BatchControl_Id BATCH_CD = new BatchControl_Id("CMCUSIN");
	//End Delete - CB-176
	//End Add - CB-133
	
	//Start Add CB-132
	private static final String WAITING = "WAITING";
	private static final String PROCESSED = "PROCESSED";
	//End Add CB-132

	@SuppressWarnings("rawtypes")
	public void invoke() {
		//Start Add-CB86
		alertType = getAlertType().getId().getIdValue();
		//End Add-CB86
		this.useDefaultNextStatus = true;
		SessionHolder.getSession().getRequestContext().ignoreWarnings(true);
		SavepointExecutable spExecutable = new SavepointExecutable(){
	      protected void execute(){
	    	  CmManageCustomerRelationAlgComp_Impl.this.processCustomerInboundMessage();
	      }
	    };
	    SavepointResult result = spExecutable.doIt("c1InboundMessage");
	   
	    if (result.hasError()){
	    	custIntfUtility.addLogEntries(result.getMessage(), this.inboundMessage, LogEntryTypeLookup.constants.EXCEPTION,businessObject.getMaintenanceObject());
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
	    	useDefaultNextStatus = false;
			//CB-50 - Start Change
			//nextBoStatus = new BusinessObjectStatusCode(businessObject.getId(), getNextStatusIfNoChildPerson());
	    	nextStatusTransitionConditionLookup = BusinessObjectStatusTransitionConditionLookup.constants.WAITING;
	    	//CB-50 - End Change
	    	
	    	//CB-86 Start Add
	    	COTSInstanceNode messageDataNode =messageNode.getGroupFromPath(CmCustomerInterfaceConstants.MESSAGE_DATA_ELE);
			COTSInstanceNode mainCustomerNode = messageDataNode.getGroupFromPath(CmCustomerInterfaceConstants.MAIN_CUSTOMER_ELE);
			COTSInstanceNode accountsNode = mainCustomerNode.getGroup(CmCustomerInterfaceConstants.ACCOUNTS_ELE);
			COTSInstanceList accountsNodeList = notNull(accountsNode)?accountsNode.getList(CmCustomerInterfaceConstants.ACCOUNT_ELE):null;
	    	addAccountAlert(accountsNodeList);
	    	//CB-86 End Add

	    	
	    }
	}
	
	
	
	private void processCustomerInboundMessage(){
		this.boInstance = BusinessObjectDispatcher.read(this.businessObjectInstKey, true);
		InboundMessage_Id inboundMessageId = new InboundMessage_Id(this.businessObjectInstKey.getString(CmCustomerInterfaceConstants
				.INBOUNDMESSAGEID_ELE));
	    this.inboundMessage = inboundMessageId.getEntity();
		COTSInstanceNode messageNode = this.boInstance.getGroupFromPath(CmCustomerInterfaceConstants.MESSAGE_ELE);
		COTSInstanceNode messageDataNode =messageNode.getGroupFromPath(CmCustomerInterfaceConstants.MESSAGE_DATA_ELE);
		COTSInstanceNode mainCustomerNode = messageDataNode.getGroupFromPath(CmCustomerInterfaceConstants.MAIN_CUSTOMER_ELE);
		
		//process child entities
		COTSInstanceNode personsNode = mainCustomerNode.getGroup(CmCustomerInterfaceConstants.PERSONS_ELE);
		COTSInstanceList personNodeList = notNull(personsNode)?personsNode.getList(CmCustomerInterfaceConstants.PERSON_ELE):null;
		//Start Add-CB-86
		COTSInstanceNode accountsNode = mainCustomerNode.getGroup(CmCustomerInterfaceConstants.ACCOUNTS_ELE);
		COTSInstanceList accountsNodeList = notNull(accountsNode)?accountsNode.getList(CmCustomerInterfaceConstants.ACCOUNT_ELE):null;
		//End Add-CB-86
		
		//CB-133 - Start Add
		LookupValue personType = mainCustomerNode.getLookup(CmCustomerInterfaceConstants.PERSON_TYPE_ELE).getLookupValue();
		COTSInstanceNode identifiersNode = mainCustomerNode.getGroup(CmCustomerInterfaceConstants.IDENTIFIERS_ELE);
		COTSInstanceList identifiersNodeList = notNull(identifiersNode)?identifiersNode.getList(CmCustomerInterfaceConstants.ID_ELE):null;
		String childPersonPrimaryIdType = null;
		String childPersonPrimaryIdValue = null;
		
		List<COTSInstanceNode> personIds = identifiersNodeList.getElementsWhere("[isPrimary = '"+YesNoOptionLookup.constants.YES.trimmedValue()+"' ]");
		if(notNull(personIds)&&!personIds.isEmpty()){
			COTSInstanceNode primaryIdNode = personIds.get(0);
			childPersonPrimaryIdType = primaryIdNode.getString(CmCustomerInterfaceConstants.ID_TYPE_ELE); 
			childPersonPrimaryIdValue = primaryIdNode.getString(CmCustomerInterfaceConstants.ID_VALUE_ELE);
		}
		//CB-133 - End Add
		
		if(notNull(personNodeList)&&!personNodeList.isEmpty()){
			Person person =  mainCustomerNode.getEntity(CmCustomerInterfaceConstants.PERSONID_ELE, Person.class);
			if(notNull(person)){
				//Start Change-CB-86
				//addOrUpdatePersonPerson(person, personsNode);
				//CB-133 - Start Change
				//addOrUpdatePersonPerson(person, personsNode, accountsNodeList);
				addOrUpdatePersonPerson(person, personsNode, accountsNodeList, personType, childPersonPrimaryIdType, childPersonPrimaryIdValue);
				//CB-133 - End Change
				//End Change-CB-86
			}else{
				addError(CmMessageRepository.mainCustomerNotExistsToAddPersonPersonRelation());
			}
		}
		//Start Add CB-132
	    if (useDefaultNextStatus) {
	    	mainCustomerNode.set(CmCustomerInterfaceConstants.BO_NEXT_STATUS, PROCESSED);
	    }else{
	    	mainCustomerNode.set(CmCustomerInterfaceConstants.BO_NEXT_STATUS, WAITING);	
	    }
	    
		BusinessObjectDispatcher.fastUpdate(this.boInstance.getDocument());
		//End Add CB-132

	}

	//Start Change - CB-133
	///**
	// * 
	// * @param person
	// * @param personsNode
	// */
	//Start Change-CB-86
	//private void addOrUpdatePersonPerson(Person person,COTSInstanceNode personsNode){
	/*private void addOrUpdatePersonPerson(Person person,COTSInstanceNode personsNode, COTSInstanceList accountsNodeList){
	//End Change-CB-86
		String personPersonRelationshipType = null;
		String primaryPersonIdType = null;
		String primaryPersonIdValue = null;
		Person parentPerson = null;
		Date startDate = null;
		Date endDate = null;
		PersonRelationshipType_Id personRelTypeId =null;
		Bool hasFinancialRelationship =null;
		Bool hadBoSchemaUpdated = Bool.FALSE;
		custIntfUtility = CmCustInterfaceUtilityBussComp.Factory.newInstance();
		//Start Add CB86
		String acctId = CmCustomerInterfaceConstants.BLANK_VALUE;
		String acctAlertType = CmCustomerInterfaceConstants.BLANK_VALUE;
		Account_Id accountId = null;
		//End Add CB86

		COTSInstanceList personNodeList = personsNode.getList(CmCustomerInterfaceConstants.PERSON_ELE);
		for(COTSInstanceNode personNode:personNodeList ){
			
			//create child person if already exists else move to waiting status
			personPersonRelationshipType = personNode.getString(CmCustomerInterfaceConstants.PERSONPERSONRELATIONSHIPTYPE_ELE);
			personRelTypeId = new PersonRelationshipType_Id(personPersonRelationshipType);
			primaryPersonIdType = personNode.getString(CmCustomerInterfaceConstants.PRIMARYPERSONIDTYPE_ELE);
			primaryPersonIdValue = personNode.getString(CmCustomerInterfaceConstants.PRIMARYPERSONIDVALUE_ELE);
			parentPerson =  personNode.getEntity(CmCustomerInterfaceConstants.PERSONID1_ELE, Person.class);
			if(notNull(parentPerson)){
				continue;
			}else{
				parentPerson =  custIntfUtility.fetchPersonById(primaryPersonIdType, primaryPersonIdValue, Bool.TRUE);
			}
			
			if(isNull(parentPerson)){
				useDefaultNextStatus = false;
				//CB-50 - Start Change
				//nextBoStatus = new BusinessObjectStatusCode(businessObject.getId(), getNextStatusIfNoChildPerson());
				nextStatusTransitionConditionLookup = BusinessObjectStatusTransitionConditionLookup.constants.WAITING;
				//CB-50 - End Change
				//CB-86 Start Add
				addAccountAlert(accountsNodeList);
				//CB-86 End Add
			}else{

				// add child relation
				startDate = personNode.getDate(CmCustomerInterfaceConstants.RELATIONSHIPSTARTDATE_ELE);
				endDate = personNode.getDate(CmCustomerInterfaceConstants.RELATIONSHIPENDDATE_ELE);

				hasFinancialRelationship =  (!isBlankOrNull(personNode.getString(CmCustomerInterfaceConstants.HASFINANCIALRELALATIONSHIP_ELE))
						&&YesNoOptionLookup.constants.YES.trimmedValue().equals(personNode
								.getString(CmCustomerInterfaceConstants.HASFINANCIALRELALATIONSHIP_ELE)))?Bool.TRUE:Bool.FALSE;

				Bool isPerPerRelationAdded = addOrUpdatePersonRelationship(parentPerson, person, startDate, endDate, personRelTypeId, hasFinancialRelationship);
				if(isPerPerRelationAdded.isTrue()){
					personNode.set(CmCustomerInterfaceConstants.PERSONID1_ELE, parentPerson);
					hadBoSchemaUpdated = Bool.TRUE;

					//Start Add-CB86
					if(!accountsNodeList.isEmpty()){
						for(COTSInstanceNode acctNode:accountsNodeList){
							acctId = acctNode.getString(CmCustomerInterfaceConstants.ACCOUNTID_ELE).trim();
							accountId = new Account_Id(acctId); 
							if(notNull(accountId.getEntity())){
							acctBoInstance = BusinessObjectInstance.create(ACCTBO);
							acctBoInstance.set(CmCustomerInterfaceConstants.ACCOUNTID_ELE, acctId);
							acctBoInstance = BusinessObjectDispatcher.read(acctBoInstance);
								if(isActiveAlertExisting(accountId.getEntity()).isTrue()){
									accountAlertList = acctBoInstance.getList(CmCustomerInterfaceConstants.ACCT_ALERT);
									if(!accountAlertList.isEmpty()){
										for(COTSInstanceNode acctAlertNode:accountAlertList){
											acctAlertType = acctAlertNode.getString(CmCustomerInterfaceConstants.ALERT_TYPE).trim();
											endDate = notNull(acctAlertNode.getDate(CmCustomerInterfaceConstants.ENDDATE_ELE))
													? acctAlertNode.getDate(CmCustomerInterfaceConstants.ENDDATE_ELE):null;
											if(acctAlertType.compareTo(alertType) == 0 && 
													(isNull(endDate) || endDate.isAfter(getProcessDateTime().getDate()))){
												acctAlertNode.set("endDate", getProcessDateTime().getDate());
											}
										}
										BusinessObjectDispatcher.fastUpdate(acctBoInstance.getDocument());
									}
								}
							}	

						}
					}
					//End Add-CB86

				}else{
					useDefaultNextStatus = false;
					//CB-50 - Start Change
					//nextBoStatus = new BusinessObjectStatusCode(businessObject.getId(), getNextStatusIfNoChildPerson());
					nextStatusTransitionConditionLookup = BusinessObjectStatusTransitionConditionLookup.constants.WAITING;
					//CB-50 - End Change
					//CB-86 Start Add
					addAccountAlert(accountsNodeList);
					//CB-86 End Add
				}
			}
		}
			
		if(hadBoSchemaUpdated.isTrue()){
			BusinessObjectDispatcher.fastUpdate(this.boInstance.getDocument());
		}
		
	}*/
	
	/**
	 * This method will process customer relationship
	 * @param person
	 * @param personsNode
	 * @param accountsNodeList
	 * @param personType
	 * @param childIdType
	 * @param childIdValue
	 */
	private void addOrUpdatePersonPerson(Person person,COTSInstanceNode personsNode, COTSInstanceList accountsNodeList, LookupValue personType, String childIdType, String childIdValue){
		//Initialize variables
		custIntfUtility = CmCustInterfaceUtilityBussComp.Factory.newInstance();
		Bool isWaiting = Bool.FALSE;
		String personPersonRelationshipType = CmCustomerInterfaceConstants.BLANK_VALUE;
		String primaryPersonIdType = CmCustomerInterfaceConstants.BLANK_VALUE;;
		String primaryPersonIdValue = CmCustomerInterfaceConstants.BLANK_VALUE;;
		Person parentPerson = null;
		Date startDate = null;
		Date endDate = null;
		PersonRelationshipType_Id personRelTypeId = null;
		Bool hasFinancialRelationship = null;
		Bool isPerPerRelationAdded = Bool.FALSE;
		//Start Delete CB-132
		//Bool hadBoSchemaUpdated = Bool.FALSE;
		//End Delete CB-132

		InboundMessage parentInboundMessage = null;
		String acctId = CmCustomerInterfaceConstants.BLANK_VALUE;
		String acctAlertType = CmCustomerInterfaceConstants.BLANK_VALUE;
		Account_Id accountId = null;

		//Retrieve Persons List
		COTSInstanceList personNodeList = personsNode.getList(CmCustomerInterfaceConstants.PERSON_ELE);
		for(COTSInstanceNode personNode:personNodeList ){
			//Person Type is Customer Contact
			if(!personType.equals(this.getPersonType().getLookupValue())){				
				//Retrieve Person Person Details
				personPersonRelationshipType = personNode.getString(CmCustomerInterfaceConstants.PERSONPERSONRELATIONSHIPTYPE_ELE);
				personRelTypeId = new PersonRelationshipType_Id(personPersonRelationshipType);
				primaryPersonIdType = personNode.getString(CmCustomerInterfaceConstants.PRIMARYPERSONIDTYPE_ELE);
				primaryPersonIdValue = personNode.getString(CmCustomerInterfaceConstants.PRIMARYPERSONIDVALUE_ELE);
				
				//Retrieve Parent Person from personId1
				if (notNull(personNode.getEntity(CmCustomerInterfaceConstants.PERSONID1_ELE, Person.class))){
					parentPerson =  personNode.getEntity(CmCustomerInterfaceConstants.PERSONID1_ELE, Person.class);
				}
							
				//If personId1 is null, retrieve Parent Person using Identifier Type and Value combination
				if(isNull(parentPerson)){
					parentPerson =  custIntfUtility.fetchPersonById(primaryPersonIdType, primaryPersonIdValue, Bool.TRUE);
				}
				
				//If Parent Person exists, create Customer Relationship record.
				//Otherwise, set isWaiting to true.
				if(notNull(parentPerson)){
					startDate = personNode.getDate(CmCustomerInterfaceConstants.RELATIONSHIPSTARTDATE_ELE);
					endDate = personNode.getDate(CmCustomerInterfaceConstants.RELATIONSHIPENDDATE_ELE);
					hasFinancialRelationship =  (!isBlankOrNull(personNode.getString(CmCustomerInterfaceConstants.HASFINANCIALRELALATIONSHIP_ELE))
							&& YesNoOptionLookup.constants.YES.trimmedValue().equals(personNode
									.getString(CmCustomerInterfaceConstants.HASFINANCIALRELALATIONSHIP_ELE)))?Bool.TRUE:Bool.FALSE;
					
					//Create Customer Relationship
					isPerPerRelationAdded = isPersonRelationshipAddedOrUpdated(parentPerson, person, startDate, endDate, personRelTypeId, hasFinancialRelationship);
					
					//If Customer Relationship is created, set personId1 to Parent Person
					if(isPerPerRelationAdded.isTrue()){
						personNode.set(CmCustomerInterfaceConstants.PERSONID1_ELE, parentPerson);
						//Start Delete CB-132
						//hadBoSchemaUpdated = Bool.TRUE;
						//End Delete CB-132
					}
				}else{
					isWaiting = Bool.TRUE;
				}				
			//Person Type is Main Customer
			}else{
				//If personId2 is blank, set isWaiting to true.
				if (isBlankOrNull(personNode.getString(CmCustomerInterfaceConstants.PERSONID2_ELE))){
					isWaiting = Bool.TRUE;
				}
			}
		}
		
		//If isWaiting is false and Person Type is Customer Contact
		if (isWaiting.isFalse() && !personType.equals(this.getPersonType().getLookupValue())){
			//Determine Inbound Message of child person's parent
			parentInboundMessage = retrieveParentInboundMessage(parentPerson);
			
			//Retrieve Parent Inbound Message Details
			BusinessObjectInstance parentInbMsgBoInstance = BusinessObjectInstance.create(parentInboundMessage.getBusinessObject());
			parentInbMsgBoInstance.set(CmCustomerInterfaceConstants.INBOUNDMESSAGEID_ELE, parentInboundMessage.getId().getIdValue());
			parentInbMsgBoInstance = BusinessObjectDispatcher.read(parentInbMsgBoInstance, true);
			
			//Start Add - CB-176
			if (parentInbMsgBoInstance.getString(CmCustomerInterfaceConstants.BO_STATUS_CD_ELE).equals(WAITING)){
			//End Add - CB-176
				COTSInstanceNode parentInbMsgMessageNode = parentInbMsgBoInstance.getGroupFromPath(CmCustomerInterfaceConstants.MESSAGE_ELE);
				COTSInstanceNode parentInbMsgMessageDataNode = parentInbMsgMessageNode.getGroupFromPath(CmCustomerInterfaceConstants.MESSAGE_DATA_ELE);
				COTSInstanceNode parentInbMsgMainCustomerNode = parentInbMsgMessageDataNode.getGroupFromPath(CmCustomerInterfaceConstants.MAIN_CUSTOMER_ELE);
				COTSInstanceNode parentInbMsgPersonsNode = parentInbMsgMainCustomerNode.getGroup(CmCustomerInterfaceConstants.PERSONS_ELE);
				COTSInstanceList parentInbMsgPersonNodeList = notNull(parentInbMsgPersonsNode)?parentInbMsgPersonsNode.getList(CmCustomerInterfaceConstants.PERSON_ELE):null;				
				COTSInstanceNode parentInbMsgAccountsNode = parentInbMsgMainCustomerNode.getGroup(CmCustomerInterfaceConstants.ACCOUNTS_ELE);
				COTSInstanceList parentInbMsgAccountNodeList = notNull(parentInbMsgAccountsNode)?parentInbMsgAccountsNode.getList(CmCustomerInterfaceConstants.ACCOUNT_ELE):null;
				
				//Loop thru parent persons node and find matching primary person id type and value with the child person
				for(COTSInstanceNode parentInbMsgPersonNode : parentInbMsgPersonNodeList){		
					if (parentInbMsgPersonNode.getString(CmCustomerInterfaceConstants.PRIMARYPERSONIDTYPE_ELE).equals(childIdType)
							&& parentInbMsgPersonNode.getString(CmCustomerInterfaceConstants.PRIMARYPERSONIDVALUE_ELE).equals(childIdValue)){
						//Set personId2 with child person id
						parentInbMsgPersonNode.set(CmCustomerInterfaceConstants.PERSONID2_ELE, person.getId().getIdValue());
						BusinessObjectDispatcher.fastUpdate(parentInbMsgBoInstance.getDocument());
						break;
					}		
				}
				
				//Loop thru the parent persons node and check if all personId2 are populated
				Bool blankPersonId2IsFound = Bool.FALSE;
				for(COTSInstanceNode parentInbMsgPersonNode : parentInbMsgPersonNodeList){												
					if (isBlankOrNull(parentInbMsgPersonNode.getString(CmCustomerInterfaceConstants.PERSONID2_ELE))){
						blankPersonId2IsFound = Bool.TRUE;
						break;
					}	
				}
				
				//If all personId2 of Parent Inbound Message is populated, transition the parent inbound message to next default status
				//and populate end date of parent's account's active alert
				if (blankPersonId2IsFound.isFalse()){
					//Start Change - CB-176
					//Transition Parent Inbound Message to LINK
					//BusinessObjectDispatcher.autotransition(parentInboundMessage, parentInboundMessage.getBusinessObject().getMaintenanceObject().getId(), BATCH_CD);				
					
					//Read Parent Inbound Message
					BusinessObjectInstance parentTransitionToLinkBoInstance = BusinessObjectInstance.create(parentInboundMessage.getBusinessObject());
					parentTransitionToLinkBoInstance.set(CmCustomerInterfaceConstants.INBOUNDMESSAGEID_ELE, parentInboundMessage.getId().getIdValue());
					parentTransitionToLinkBoInstance = BusinessObjectDispatcher.read(parentTransitionToLinkBoInstance, true);
					
					//Set BO Status to LINK and invoke for update
					parentTransitionToLinkBoInstance.set(CmCustomerInterfaceConstants.BO_STATUS_CD_ELE, CmCustomerInterfaceConstants.LINK_STATUS);
					BusinessObjectDispatcher.update(parentTransitionToLinkBoInstance);
					//End Change - CB-176
					
					//Populate end date of parent inbound message account's active alert
					if(!parentInbMsgAccountNodeList.isEmpty()){
						for(COTSInstanceNode acctNode:parentInbMsgAccountNodeList){
							acctId = acctNode.getString(CmCustomerInterfaceConstants.ACCOUNTID_ELE).trim();
							accountId = new Account_Id(acctId); 
							if(notNull(accountId.getEntity())){
							acctBoInstance = BusinessObjectInstance.create(ACCTBO);
							acctBoInstance.set(CmCustomerInterfaceConstants.ACCOUNTID_ELE, acctId);
							acctBoInstance = BusinessObjectDispatcher.read(acctBoInstance);
								if(isActiveAlertExisting(accountId.getEntity()).isTrue()){
									accountAlertList = acctBoInstance.getList(CmCustomerInterfaceConstants.ACCT_ALERT);
									if(!accountAlertList.isEmpty()){
										for(COTSInstanceNode acctAlertNode:accountAlertList){
											acctAlertType = acctAlertNode.getString(CmCustomerInterfaceConstants.ALERT_TYPE).trim();
											endDate = notNull(acctAlertNode.getDate(CmCustomerInterfaceConstants.ENDDATE_ELE))
													? acctAlertNode.getDate(CmCustomerInterfaceConstants.ENDDATE_ELE):null;
											if(acctAlertType.compareTo(alertType) == 0 && 
													(isNull(endDate) || endDate.isAfter(getProcessDateTime().getDate()))){
												acctAlertNode.set("endDate", getProcessDateTime().getDate());
											}
										}
										BusinessObjectDispatcher.fastUpdate(acctBoInstance.getDocument());
									}
								}
							}	
						}
					}				
				}				
			//Start Add - CB-176
			}
			//End Add -CB-176
		}
		
		if (isWaiting.isTrue()){
			//If Person Type is Main Customer, create Account Alert.
			if (personType.equals(this.getPersonType().getLookupValue())){
				addAccountAlert(accountsNodeList);
			}
	
			//Transition to WAITING
			useDefaultNextStatus = false;
			nextStatusTransitionConditionLookup = BusinessObjectStatusTransitionConditionLookup.constants.WAITING;
		}
		//Start Delete CB-132
		//if(hadBoSchemaUpdated.isTrue()){
		//	BusinessObjectDispatcher.fastUpdate(this.boInstance.getDocument());
		//}		
		//End Delete CB-132
	}
	//End Change - CB-133
	
	/**
	 * 
	 * @param parentPerson
	 * @param childPerson
	 * @param startDate
	 * @param endDate
	 * @param personRelTypeId
	 * @param hasFinancialRelationship
	 */
	
	//Start Change - CB-133
	//private  Bool addOrUpdatePersonRelationship(Person parentPerson,Person childPerson,Date startDate,Date endDate
	//		,PersonRelationshipType_Id personRelTypeId,Bool hasFinancialRelationship){
	//	PersonPerson_Id perPerId = null;
	//	PersonPerson_DTO perPerDTO = null;
	//	Bool isPerPerRelationAdded = Bool.FALSE;
	private  Bool isPersonRelationshipAddedOrUpdated(Person parentPerson,Person childPerson,Date startDate,Date endDate
			,PersonRelationshipType_Id personRelTypeId,Bool hasFinancialRelationship){
		perPerId = null;
		perPerDTO = null;
		isPerPerRelationAdded = Bool.FALSE;
	//End Change - CB-133
	
		try {
			perPerId = new PersonPerson_Id(personRelTypeId,parentPerson.getId(),childPerson.getId(),startDate);

			//get existing person persons 
			ListFilter<PersonPerson> listFilter  = parentPerson.getPersons().createFilter(" where this.id.personId2=:perId2 "
					+ " and this.id.personRelationshipType=:relType "
					+ " and this.id.startDate<=:startDate and nvl(this.endDate,:startDate)>=:startDate","");
			listFilter.bindId("perId2", childPerson.getId());
			listFilter.bindId("relType", personRelTypeId);
			listFilter.bindDate("startDate",startDate);
			List<PersonPerson> perPerList =listFilter.list();
			if(notNull(perPerList)&&!perPerList.isEmpty()){
				for(PersonPerson perper1 :perPerList ){
					if(perper1.getId().equals(perPerId)){
						perPerDTO = perper1.getDTO();
						perPerDTO.setEndDate(endDate);
						perPerDTO.setHasFinancialRelationship(hasFinancialRelationship);
						perper1.setDTO(perPerDTO);
					}else{
						//end date existing one
						perPerDTO =  perper1.getDTO();
						perPerDTO.setEndDate(startDate.addDays(-1));
						perper1.setDTO(perPerDTO);
						//add new relation start
						perPerDTO = createDTO(PersonPerson.class);
						perPerDTO.setEndDate(endDate);
						perPerDTO.setId(perPerId);
						perPerDTO.setHasFinancialRelationship(hasFinancialRelationship);
						perPerDTO.newEntity();					
					} 
				}
			}else{
				perPerDTO = createDTO(PersonPerson.class);
				perPerDTO.setEndDate(endDate);
				perPerDTO.setId(perPerId);
				perPerDTO.setHasFinancialRelationship(hasFinancialRelationship);
				perPerDTO.newEntity();
			}
			isPerPerRelationAdded= Bool.TRUE;
		} catch (ApplicationError e) {
			custIntfUtility.addLogEntries(e.getServerMessage(), this.inboundMessage, LogEntryTypeLookup.constants.EXCEPTION,businessObject.getMaintenanceObject());
			isPerPerRelationAdded= Bool.FALSE;
					
		}
		
		return isPerPerRelationAdded;
	}
	
	/**
	 * Add Alert on Account
	 * @param accountsNodeList
	 */
	private void addAccountAlert(COTSInstanceList accountsNodeList){
    	String acctId = CmCustomerInterfaceConstants.BLANK_VALUE;
    	Account_Id accountId;
    	
    	if(!accountsNodeList.isEmpty()){
    		for(COTSInstanceNode acctNode:accountsNodeList){
    			acctId = acctNode.getString(CmCustomerInterfaceConstants.ACCOUNTID_ELE).trim();
    			accountId = new Account_Id(acctId); 
    			if(notNull(accountId.getEntity())){
    				if(isActiveAlertExisting(accountId.getEntity()).isFalse()){
    					acctBoInstance = BusinessObjectInstance.create(ACCTBO);
    					acctBoInstance.set(CmCustomerInterfaceConstants.ACCOUNTID_ELE, acctId);
    					acctBoInstance = BusinessObjectDispatcher.read(acctBoInstance);
    					accountAlertList = acctBoInstance.getList(CmCustomerInterfaceConstants.ACCT_ALERT);

    					createNewAlert(acctId, accountAlertList);

    					BusinessObjectDispatcher.fastUpdate(acctBoInstance.getDocument());
    				}
    			}
    		}
    	}	    	
	}
	
	/**
	 * Check if there is an Active Alert on the Account
	 * @param account
	 * @return
	 */
	private Bool isActiveAlertExisting(Account account){
		ListFilter<AccountAlert> alerts = account.getAlerts().createFilter(" where this.id.startDate<=:processDate  "
				+ " and (this.endDate is null or this.endDate>:processDate ) "
				+ " and this.id.alertType=:alertType ",this.getClass().getSimpleName());
		alerts.bindDate("processDate", getProcessDateTime().getDate());
		alerts.bindId("alertType", new AlertType_Id(alertType));
		AccountAlert accountAlert = alerts.firstRow();
		
		if(notNull(accountAlert)){
			return Bool.TRUE;
		}else{
			return Bool.FALSE;
		}
	}
	
	/**
	 * Create new Alert
	 * @param acctId
	 * @param accountAlertList
	 */
	private void createNewAlert(String acctId, COTSInstanceList accountAlertList){
		accountAlertNode = accountAlertList.newChild();
		accountAlertNode.set(CmCustomerInterfaceConstants.ACCOUNTID_ELE, acctId);
		accountAlertNode.set(CmCustomerInterfaceConstants.ALERT_TYPE, alertType);
		accountAlertNode.set(CmCustomerInterfaceConstants.STARTDATE_ELE, getProcessDateTime().getDate());
	}
	
	//Stard Add - CB-133
	/**
	 * Retrieve Parent Inbound Message
	 * @param person
	 * @return
	 */
	private InboundMessage retrieveParentInboundMessage(Person person){
		Query<InboundMessage> getParentInbMsgQry = createQuery(
				"FROM InboundMessage inbMsg " +
				"WHERE inbMsg.personId = :personId ", "");
		
		getParentInbMsgQry.bindId("personId", person.getId());
		getParentInbMsgQry.addResult("inbMsg", "inbMsg");
		
		return getParentInbMsgQry.firstRow();
	}
	//End Add - CB-133

	protected void extraSoftParameterValidations() {
		
	}
	
	public BusinessObjectStatusCode getNextStatus() {
		return nextBoStatus;
	}


	@Override
	protected void extraSoftParameterValidations(boolean forAlgorithmValidation) {
		
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