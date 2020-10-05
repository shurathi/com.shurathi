/*
 ***********************************************************************
 *                Confidentiality Information:
 *
 * This module is the confidential and proprietary information of
 * RIA Advisory; it is not to be copied, reproduced, or
 * transmitted in any form, by any means, in whole or in part,
 * nor is it to be used for any purpose other than that for which
 * it is expressly provided without the written permission of
 * RIA Advisory.
 ***********************************************************************
 *
 * PROGRAM DESCRIPTION:
 *
 * This BO Status Monitor algorithm will check if the Person record 
 * exists in ORMB using Person Identifier
 * 
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:         Reason:
 * 2020-04-22   DDejes      Initial Version. 
 * 2020-07-03   JFerna      CB-176. Checking will only be done for
 *                                 Customer Contact requests
 ***********************************************************************
 */

package com.splwg.cm.domain.customer.businessObject;


import java.util.List;

import com.splwg.base.api.Query;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.BusinessObjectStatusCode;
import com.splwg.base.api.businessObject.COTSInstanceList;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.lookup.BusinessObjectActionLookup;
import com.splwg.base.api.lookup.BusinessObjectStatusTransitionConditionLookup;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatusAutoTransitionAlgorithmSpot;
import com.splwg.base.domain.common.businessObjectStatusReason.BusinessObjectStatusReason_Id;
import com.splwg.base.domain.common.lookup.LookupValue;
import com.splwg.ccb.domain.admin.idType.IdType_Id;
import com.splwg.ccb.domain.customerinfo.person.PersonId;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.cm.domain.customer.utility.CmCustomerInterfaceConstants;
import com.splwg.shared.common.ApplicationError;

//Start Change - CB-176
///**
// * @author DDejes
// *
//@AlgorithmComponent ()
// */
/**
 * @author DDejes
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (lookupName = personOrBusiness, name = personType, required = true, type = lookup)})
 */
//End Change - CB-176

public class CmCheckPersonExistAlgComp_Impl extends
CmCheckPersonExistAlgComp_Gen implements
BusinessObjectStatusAutoTransitionAlgorithmSpot {

	// Hard parameter
	private BusinessObjectInstanceKey businessObjectInstKey;
	private BusinessObjectInstance boInstance;
	private BusinessObjectStatusTransitionConditionLookup transConditionLookup;
	private boolean skipAutoTransitioning;
	
	//Work Parameter
    private StringBuilder queryString;
    private Query<Person_Id> query;
    private List<Person_Id> personIdList;
	private static final String BLANK_STRING = "";


	@Override
	public boolean getForcePostProcessing() {

		return false;
	}

	@Override
	public BusinessObjectStatusCode getNextStatus() {

		return null;
	}

	@Override
	public BusinessObjectStatusTransitionConditionLookup getNextStatusCondition() {
		return transConditionLookup;
	}

	@Override
	public boolean getSkipAutoTransitioning() {

		return skipAutoTransitioning;
	}

	@Override
	public BusinessObjectStatusReason_Id getStatusChangeReasonId() {

		return null;
	}

	@Override
	public boolean getUseDefaultNextStatus() {

		return false;
	}

	@Override
	public void setAction(BusinessObjectActionLookup arg0) {


	}

	@Override
	public void setBusinessObject(BusinessObject arg0) {
	}

	@Override
	public void setBusinessObjectKey(BusinessObjectInstanceKey arg0) {
		businessObjectInstKey = arg0;

	}

	@Override	
	public void invoke() {
		try{
			boInstance = BusinessObjectDispatcher.read(businessObjectInstKey, true);
			//Start Change CB-176
			//COTSInstanceNode personsGroup = boInstance.getGroup("message").getGroup("messageData").getGroup("mainCustomer").getGroup("persons");
			//COTSInstanceList personList = personsGroup.getList("person");			
			COTSInstanceNode messageNode = this.boInstance.getGroupFromPath(CmCustomerInterfaceConstants.MESSAGE_ELE);
			COTSInstanceNode messageDataNode =messageNode.getGroupFromPath(CmCustomerInterfaceConstants.MESSAGE_DATA_ELE);
			COTSInstanceNode mainCustomerNode = messageDataNode.getGroupFromPath(CmCustomerInterfaceConstants.MAIN_CUSTOMER_ELE);			
			LookupValue personType = mainCustomerNode.getLookup(CmCustomerInterfaceConstants.PERSON_TYPE_ELE).getLookupValue();			
			//End Change - CB-176

			String personId1 = BLANK_STRING;
			String perTypeString = BLANK_STRING;
			String idVal = BLANK_STRING;
			IdType_Id perType = null;
			skipAutoTransitioning = true;
			
			//Start Add - CB-176
			//Person Type is not Main Customer
			if(!personType.equals(this.getPersonType().getLookupValue())){

				//Retrieve Persons Group
				COTSInstanceNode personsGroup = mainCustomerNode.getGroup(CmCustomerInterfaceConstants.PERSONS_ELE);
				COTSInstanceList personList = notNull(personsGroup)?personsGroup.getList(CmCustomerInterfaceConstants.PERSON_ELE):null;
			//End Add - CB-176
				
				//For every Person List with blank personId1, retrieve primary person record using id type and value
				for(COTSInstanceListNode personListNode : personList){
					//Start Change - CB-176
					//personId1 = isBlankOrNull(personListNode.getString("personId1"))? BLANK_STRING : personListNode.getString("personId1").trim();
					personId1 = isBlankOrNull(personListNode.getString(CmCustomerInterfaceConstants.PERSONID1_ELE))? BLANK_STRING : personListNode.getString(CmCustomerInterfaceConstants.PERSONID1_ELE).trim();
					//End Change - CB-176
					
					// If no personId1
					if(isBlankOrNull(personId1)){
						//Retrieve Person Type and Id Value
						//Start Change - CB-176
						//perTypeString = isBlankOrNull(personListNode.getString("primaryPersonIdType"))? BLANK_STRING 
						//		: personListNode.getString("primaryPersonIdType").trim();
						//idVal = isBlankOrNull(personListNode.getString("primaryPersonIdValue"))? BLANK_STRING 
						//		: personListNode.getString("primaryPersonIdValue").trim();
						perTypeString = isBlankOrNull(personListNode.getString(CmCustomerInterfaceConstants.PRIMARYPERSONIDTYPE_ELE))? BLANK_STRING 
								: personListNode.getString(CmCustomerInterfaceConstants.PRIMARYPERSONIDTYPE_ELE).trim();
						idVal = isBlankOrNull(personListNode.getString(CmCustomerInterfaceConstants.PRIMARYPERSONIDVALUE_ELE))? BLANK_STRING 
								: personListNode.getString(CmCustomerInterfaceConstants.PRIMARYPERSONIDVALUE_ELE).trim();
						//End Change - CB-176

						if(!isBlankOrNull(perTypeString) && !isBlankOrNull(idVal)){
							perType = new IdType_Id(perTypeString) ;
							
							//Retrieve Person Id, if not null transition to next status
							if(notNull(retrievePersonIdWithIdType(perType, idVal))){
								transConditionLookup = BusinessObjectStatusTransitionConditionLookup.constants.OK;
								skipAutoTransitioning = false;
								break;
							}	
						}
					}
				}
			//Start Add - CB-176
			}
			//End Add - CB-176
			
		}catch(ApplicationError e){
			addError(e.getServerMessage());
		}
	}
	
	 /**
     * Retrieve Person ID with ID Type
     * @param ID Type ID
     * @param id value
     * @return Person ID List
     */
    private Person_Id retrievePersonIdWithIdType(IdType_Id idTypeId, String idVal) {
    	
        queryString = new StringBuilder();
        
        queryString.append("FROM PersonId perId                 ");
        queryString.append("WHERE perId.id.idType.id = :idType  ");
        queryString.append("AND perId.personIdNumber =:idValue  ");
        queryString.append("AND perId.isPrimaryId = :true       ");

        query = createQuery(queryString.toString(), "retrievePersonIdWithIdType");
        query.bindId("idType", idTypeId);
        query.bindStringProperty("idValue", PersonId.properties.personIdNumber, idVal);
        query.bindBoolean("true", Bool.TRUE);
        query.addResult("perId.id.person.id", "perId.id.person.id");
        personIdList = query.list();
        if (personIdList.size() == 1) {
        	return query.firstRow();
        }else {
        	return null;
        }        
    }
}
