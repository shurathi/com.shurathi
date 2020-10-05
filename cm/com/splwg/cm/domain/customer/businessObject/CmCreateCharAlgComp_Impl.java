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
 * This BO Status Exit algorithm will create Person Characteristic
 * 
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:         Reason:
 * 2020-06-30   DDejes      Initial Version. 
 * 2020-07-23   JFerna      CB-233. Added bo action flag in
 *                          account node
 ***********************************************************************
 */
package com.splwg.cm.domain.customer.businessObject;

import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.COTSInstanceList;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.Lookup;
import com.splwg.base.api.lookup.BusinessObjectActionLookup;
import com.splwg.base.api.lookup.BusinessObjectStatusTransitionConditionLookup;
import com.splwg.base.api.lookup.CharacteristicTypeLookup;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectExitStatusAlgorithmSpot;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatus;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatusTransitionRule;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatus_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.lookup.LookupValue;
import com.splwg.ccb.api.lookup.CharacteristicEntityLookup;
import com.splwg.ccb.domain.customerinfo.account.Account;
import com.splwg.ccb.domain.customerinfo.account.AccountCharacteristic;
import com.splwg.ccb.domain.customerinfo.account.AccountCharacteristic_DTO;
import com.splwg.ccb.domain.customerinfo.account.AccountCharacteristic_Id;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.customerinfo.person.Person;
import com.splwg.ccb.domain.customerinfo.person.PersonCharacteristic;
import com.splwg.ccb.domain.customerinfo.person.PersonCharacteristic_DTO;
import com.splwg.ccb.domain.customerinfo.person.PersonCharacteristic_Id;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.ccb.domain.customerinfo.serviceAgreement.ServiceAgreementCharacteristic;
import com.splwg.ccb.domain.customerinfo.serviceAgreement.ServiceAgreementCharacteristic_DTO;
import com.splwg.ccb.domain.customerinfo.serviceAgreement.ServiceAgreementCharacteristic_Id;
import com.splwg.cm.domain.customer.utility.CmCharData;
import com.splwg.cm.domain.customer.utility.CmCustInterfaceExtLookupVO;
import com.splwg.cm.domain.customer.utility.CmCustomerInterfaceConstants;
import com.splwg.cm.domain.customer.utility.CmCustomerInterfaceExtLookupCache;

/**
 * @author Denise De Jesus
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = businessObject, name = custInffDivExtLookup, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = businessObject, name = custIntfCustClassExtLookup, required = true, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = personOrBusiness, name = personType, required = true, type = lookup)})
 */
public class CmCreateCharAlgComp_Impl extends CmCreateCharAlgComp_Gen
		implements BusinessObjectExitStatusAlgorithmSpot {

		// Hard parameter
		private BusinessObjectInstanceKey businessObjectInstKey;
		private BusinessObjectInstance boInstance;
		private BusinessObject bo;
		
		// Work variables
		private CmCustInterfaceExtLookupVO custExtLukupDataByDiv;
		private CmCustInterfaceExtLookupVO custExtLukupDataByCustClass;
		
		//Constants
		private static final String LINK = "LINK";
		private static final String PROCESSED = "PROCESSED";
		//Start Delete - CB-233
		//private static final String ADD = "ADD";
		//End Delete - CB-233


	@Override
	public void invoke() {
		boInstance = BusinessObjectDispatcher.read(businessObjectInstKey, true);
		
    	COTSInstanceNode messageNode = this.boInstance.getGroupFromPath(CmCustomerInterfaceConstants.MESSAGE_ELE);
		COTSInstanceNode messageDataNode = messageNode.getGroupFromPath(CmCustomerInterfaceConstants.MESSAGE_DATA_ELE);
    	COTSInstanceNode mainCustomerNode = messageDataNode.getGroupFromPath(CmCustomerInterfaceConstants.MAIN_CUSTOMER_ELE);
    	
    	//Start Delete - CB-233
    	//String boActionFlag = !isBlankOrNull(mainCustomerNode.getString(CmCustomerInterfaceConstants.BO_ACTION_FLG))?
    	//		mainCustomerNode.getString(CmCustomerInterfaceConstants.BO_ACTION_FLG) 
		//		:CmCustomerInterfaceConstants.BLANK_VALUE;
    	//End Delete - CB-233
    	
    	String boNextStatus = !isBlankOrNull(mainCustomerNode.getString(CmCustomerInterfaceConstants.BO_NEXT_STATUS))?
    			mainCustomerNode.getString(CmCustomerInterfaceConstants.BO_NEXT_STATUS) 
				:CmCustomerInterfaceConstants.BLANK_VALUE;
    
    	LookupValue personType = mainCustomerNode.getLookup(CmCustomerInterfaceConstants.PERSONTYPE_ELE).getLookupValue();
    	
    	//Next Status and Status with Condition OK should be Processed + BO Action Flag should be Add
	    //if(retrieveNextBoStatus().trim().equalsIgnoreCase(PROCESSED) && boActionFlag.trim().equalsIgnoreCase(ADD)
	    //		&& boNextStatus.trim().equalsIgnoreCase(PROCESSED)){
	    if(retrieveNextBoStatus().trim().equalsIgnoreCase(PROCESSED) && boNextStatus.trim().equalsIgnoreCase(PROCESSED)){
	    	
	    	//Person Type should be same as Algo Parm
	    	if(personType.equals(getPersonType().getLookupValue())){
	    		
	    		//InitializeDefaultConfigData for Division from Extended Lookup
	    		initializeDivExtLookupData(mainCustomerNode);
	    		
	    		//Create Characteristic 
	    		createChar(mainCustomerNode);
	    	}
	    }
	    
	
	}
	
	/**
	 * Create Characteristic
	 * @param mainCustomerNode
	 */
	private void createChar(COTSInstanceNode mainCustomerNode){
		AccountCharacteristic_Id acctCharId = null;
		PersonCharacteristic_Id perCharId = null;
		Account_Id acctId = null;
		Account account = null;
		Person_Id personId = null;
		Person person = null;
		CharacteristicType charType = null;
		Lookup charEnt = null;
		Date effectiveDate = null;
		String personIdString = CmCustomerInterfaceConstants.BLANK_VALUE;
		String accountIdString = CmCustomerInterfaceConstants.BLANK_VALUE;
		String charVal = CmCustomerInterfaceConstants.BLANK_VALUE;

		effectiveDate = notNull(mainCustomerNode.getDate(CmCustomerInterfaceConstants.EFFECTIVEDATE_ELE))? 
				mainCustomerNode.getDate(CmCustomerInterfaceConstants.EFFECTIVEDATE_ELE):effectiveDate;		
		personIdString = !isBlankOrNull(mainCustomerNode.getString(CmCustomerInterfaceConstants.PERSONID_ELE))? 
				mainCustomerNode.getString(CmCustomerInterfaceConstants.PERSONID_ELE):CmCustomerInterfaceConstants.BLANK_VALUE;
				
		//Start Add - CB-233
		String perBoActionFlag = !isBlankOrNull(mainCustomerNode.getString(CmCustomerInterfaceConstants.BO_ACTION_FLG))?
				mainCustomerNode.getString(CmCustomerInterfaceConstants.BO_ACTION_FLG) 
				:CmCustomerInterfaceConstants.BLANK_VALUE;	
		//End Add - CB-233

		//Person Characteristic
		personId = new Person_Id(personIdString);
		person = personId.getEntity();
		//Start Change - CB-233
		//if(notNull(person)){
		if(notNull(person) && perBoActionFlag.equalsIgnoreCase(CmCustomerInterfaceConstants.ADD_ACTION)){
		//End Change - CB-233
			if(notNull(custExtLukupDataByDiv.getCharDefault()) && !custExtLukupDataByDiv.getCharDefault().isEmpty()){

				//For Every Characteristic Type
				for(CmCharData charData: custExtLukupDataByDiv.getCharDefault()){
					charType = charData.getCharType();
					charVal = charData.getCharValue();
					charEnt = charData.getCharEntity();
					perCharId = new PersonCharacteristic_Id(person,charType,effectiveDate);
					if(isNull(perCharId.getEntity())){		

						//Create Person Characteristic
						createPerChar(charType, charVal, perCharId);
					}

				}
			}
		}

		//Account/SACharacteristic
		COTSInstanceNode accountsNode = mainCustomerNode.getGroup(CmCustomerInterfaceConstants.ACCOUNTS_ELE);
		COTSInstanceList accountNodeList = accountsNode.getList(CmCustomerInterfaceConstants.ACCOUNT_ELE);
		String accountBoActionFlag;
		
		//For every Account List
		for(COTSInstanceNode accountNode:accountNodeList){
			accountIdString = accountNode.getString(CmCustomerInterfaceConstants.ACCOUNTID_ELE);
			
			//Start Add - CB-233
			accountBoActionFlag = !isBlankOrNull(accountNode.getString(CmCustomerInterfaceConstants.BO_ACTION_FLG))?
					accountNode.getString(CmCustomerInterfaceConstants.BO_ACTION_FLG) 
					:CmCustomerInterfaceConstants.BLANK_VALUE;	
			//End Add - CB-233

			//InitializeDefaultConfigData for Customer Class from Extended Lookup
			initializeCustClassExtLookupData(accountNode);
			acctId = new Account_Id(accountIdString);
			account = acctId.getEntity();
			//Start Change - CB-233
			//if(notNull(account)){
			if(notNull(account) && accountBoActionFlag.equalsIgnoreCase(CmCustomerInterfaceConstants.ADD_ACTION)){
			//End Change - CB-233
				if(notNull(custExtLukupDataByCustClass.getCharDefault()) &&
						!custExtLukupDataByCustClass.getCharDefault().isEmpty()){

					//For Every Characteristic Type
					for(CmCharData charData: custExtLukupDataByCustClass.getCharDefault()){
						charType = charData.getCharType();
						charVal = charData.getCharValue();
						charEnt = charData.getCharEntity();

						acctCharId = new AccountCharacteristic_Id(charType, account, effectiveDate);
						if(isNull(acctCharId.getEntity())){		
							if(charEnt.getLookupValue().equals(CharacteristicEntityLookup.constants.ACCOUNT.getLookupValue())){

								//Create Account Characteristic
								createAcctChar(charType, charVal, acctCharId);

								//Create SA Characteristic Placeholder
							}else if(charEnt.getLookupValue().equals(
									CharacteristicEntityLookup.constants.SERVICE_AGREEMENT.getLookupValue())){
								//createSAChar(charType, charVal, saCharId);
							}
						}

					}
				}

			}
		}

	}
	
	/**
	 * Create Person Characteristics
	 * @param charType
	 * @param charVal
	 * @param perCharId
	 */
	private void createPerChar(CharacteristicType charType, String charVal, PersonCharacteristic_Id perCharId){
		PersonCharacteristic_DTO perCharDto = null;
		CharacteristicTypeLookup charTypeLookup = null;
		
		charTypeLookup = charType.getCharacteristicType();
		perCharDto = createDTO(PersonCharacteristic.class);
		perCharDto.setId(perCharId);
		if(charTypeLookup.isAdhocValue()){
			perCharDto.setAdhocCharacteristicValue(charVal);
		}else if(charTypeLookup.isPredefinedValue()){
			perCharDto.setCharacteristicValue(charVal);
		}else if(charTypeLookup.isForeignKeyValue()){
			perCharDto.setCharacteristicValueForeignKey1(charVal);
		}

		perCharDto.newEntity();
	}
	
	/**
	 * Create Account Characteristicss
	 * @param charType
	 * @param charVal
	 * @param acctCharId
	 */
	private void createAcctChar(CharacteristicType charType, String charVal, AccountCharacteristic_Id acctCharId){
		AccountCharacteristic_DTO acctCharDto = null;
		CharacteristicTypeLookup charTypeLookup = null;
		
		charTypeLookup = charType.getCharacteristicType();
		acctCharDto = createDTO(AccountCharacteristic.class);
		acctCharDto.setId(acctCharId);
		if(charTypeLookup.isAdhocValue()){
			acctCharDto.setAdhocCharacteristicValue(charVal);
		}else if(charTypeLookup.isPredefinedValue()){
			acctCharDto.setCharacteristicValue(charVal);
		}else if(charTypeLookup.isForeignKeyValue()){
			acctCharDto.setCharacteristicValueForeignKey1(charVal);
		}

		acctCharDto.newEntity();
	}
	
	/**
	 * Create SA Characteristics
	 * @param charType
	 * @param charVal
	 * @param saCharId
	 */
	private void createSAChar(CharacteristicType charType, String charVal, ServiceAgreementCharacteristic_Id saCharId){
		ServiceAgreementCharacteristic_DTO saCharDto = null;
		CharacteristicTypeLookup charTypeLookup = null;
		
		charTypeLookup = charType.getCharacteristicType();
		saCharDto = createDTO(ServiceAgreementCharacteristic.class);
		saCharDto.setId(saCharId);
		if(charTypeLookup.isAdhocValue()){
			saCharDto.setAdhocCharacteristicValue(charVal);
		}else if(charTypeLookup.isPredefinedValue()){
			saCharDto.setCharacteristicValue(charVal);
		}else if(charTypeLookup.isForeignKeyValue()){
			saCharDto.setCharacteristicValueForeignKey1(charVal);
		}

		saCharDto.newEntity();
	}
	
	/**
	 * Retrieve Next BO Status that has Condition OK
	 * @return BO Status
	 */
	private String retrieveNextBoStatus(){
		 String nextStatus = null;
		 BusinessObjectStatusTransitionConditionLookup nextStatusTransitionCondition =
				 BusinessObjectStatusTransitionConditionLookup.constants.OK;
         BusinessObjectStatus boStatus = new BusinessObjectStatus_Id(
                 bo.fetchLifecycleBusinessObject(), LINK).getEntity();
         for (BusinessObjectStatusTransitionRule tr : boStatus.getTransitionRules()) {

             /*
              * Check if the business object status transition rule matches
              * the next status transition condition OK.
              */
             if (tr.getCondition().equals(nextStatusTransitionCondition)) {
                 nextStatus = tr.fetchIdNextStatus();
                 break;
             }
         }
         return nextStatus;
	}
	
	/**
	 * Initialize Extendable Lookup for Division
	 * @param mainCustomerNode
	 */
	private void initializeDivExtLookupData(COTSInstanceNode mainCustomerNode ){

		String division = mainCustomerNode.getString(CmCustomerInterfaceConstants.DIVISION_ELE);
		
		custExtLukupDataByDiv = CmCustomerInterfaceExtLookupCache.getCustomerInterfaceConfigLookupByDiv(
				getCustInffDivExtLookup(), division);	
	}
	
	/**
	 * Initialize Extendable Lookup for Customer Class
	 * @param mainCustomerNode
	 */
	private void initializeCustClassExtLookupData(COTSInstanceNode accountNode ){

		String customerClass = accountNode.getString(CmCustomerInterfaceConstants.CUSTOMERCLASS_ELE);

		custExtLukupDataByCustClass =  CmCustomerInterfaceExtLookupCache
				.getCustomerInterfaceConfigLookupByCustClass(getCustIntfCustClassExtLookup(), customerClass);
	}
	
	@Override
	public boolean getForcePostProcessing() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setAction(BusinessObjectActionLookup arg0) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void setBusinessObject(BusinessObject arg0) {
		// TODO Auto-generated method stub
		bo = arg0;
	}

	@Override
	public void setBusinessObjectKey(BusinessObjectInstanceKey boKey) {
		// TODO Auto-generated method stub
		businessObjectInstKey = boKey;
	}


}
