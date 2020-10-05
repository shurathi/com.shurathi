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
 * Delinquency Cancel Status and Balance below Threshold
 * 
 * This algorithm will cancel the delinquency process if the Account is on 
 * a certain status defined in the soft parameter and the Account balance 
 * is below or equal to a set Threshold Amount. 
 * 
 **************************************************************************
 * 
 * CHANGE HISTORY:
 *
 * Date:        by:        Reason:
 * YYYY-MM-DD   IN         Reason text. 
 * 2020-08-20   JFerna     CB-278. Initial	
 * 2020-09-08   SPatil	   CB-359. Updated version
 **************************************************************************
 */

package com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.algorithm;

import com.splwg.base.api.Query;
import com.splwg.base.api.QueryResultRow;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.installation.InstallationHelper;
import com.splwg.base.api.lookup.CharacteristicTypeLookup;
import com.splwg.base.domain.common.businessObjectStatusReason.BusinessObjectStatusReason_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicEntity_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.characteristicType.CharacteristicValue_Id;
import com.splwg.base.domain.common.maintenanceObject.MaintenanceObject;
import com.splwg.ccb.api.lookup.CharacteristicEntityLookup;
import com.splwg.ccb.domain.customerinfo.account.Account;
import com.splwg.ccb.domain.customerinfo.account.AccountCharacteristic;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.cm.api.lookup.CmDelinquencyRelatedObjTypeLookup;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;
import com.splwg.cm.domain.common.messageRepository.CmMessages;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmDelinquencyProcessCancelCriteriaAlgorithmSpot;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm.CmDelinquencyProcessCalculateTriggerDateConstants;
import com.splwg.cm.domain.delinquency.common.businessComponent.CmComputeAccountOverdueBalance;

/**
 * @author JFerna
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = businessObjectStatusReason, name = statusReason, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = statusCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = statusCharacteristicValue, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = thresholdAmount, required = true, type = decimal)
 *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = includeUnapplied, type = lookup)
 *            , @AlgorithmSoftParameter (entityName = featureConfiguration, name = unappliedContractTypesFeatureConfig, required = true, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = customerManagementOption, name = unappliedContractTypesOptionType, required = true, type = lookup)})
 */
public class CmDelinquencyCancelAccountStatusBalanceAlgComp_Impl extends
		CmDelinquencyCancelAccountStatusBalanceAlgComp_Gen implements
		CmDelinquencyProcessCancelCriteriaAlgorithmSpot {

	//Hard Parameters
	private CmDelinquencyProcess_Id delinquencyProcessId;
	private Bool isOkayToCancel;
	private BusinessObjectStatusReason_Id statusReasonId;
	
	/**
	 * Validate Soft Parameters
	 * @param forAlgorithmValidation Boolean value
	 */
	protected void extraSoftParameterValidations(boolean forAlgorithmValidation) {
    	//Retrieve Algorithm Parameter Description
    	String statusCharacteristicTypeDesc = getAlgorithm().getAlgorithmType().getParameterAt(1).fetchLanguageParameterLabel().trim();
    	
    	// Start CB-359
    	/*//Check that Status Characteristic Type is valid for Person entity
    	CharacteristicType statusCharType = getStatusCharacteristicType();
    	validateCharacteristicType(statusCharType,CharacteristicEntityLookup.constants.PERSON,statusCharacteristicTypeDesc);*/
    	
    	//Check that Status Characteristic Type is valid for Person entity
    	CharacteristicType statusCharType = getStatusCharacteristicType();
    	validateCharacteristicType(statusCharType,CharacteristicEntityLookup.constants.ACCOUNT,statusCharacteristicTypeDesc);
    	//End CB-359
    	
    	//Check that Status Characteristic Value is valid for Status Characteristic Type
    	CharacteristicTypeLookup statusCharTypeLkp = statusCharType.getCharacteristicType();
    	String statusCharVal = getStatusCharacteristicValue();
    	if (statusCharTypeLkp.isPredefinedValue()){
    		validateCharValForCharType(statusCharType,statusCharVal);
    	}
	}
	
	/**
	 * Main Processing
	 */
	public void invoke() {
		
		isOkayToCancel = Bool.FALSE;
		
		// Fetch level and entity for delinquency process
		QueryResultRow resultRow = fetchLevelAndEntityForDelinquencyProcess();
		MaintenanceObject maintenanceObject = notNull(resultRow) ? resultRow.getEntity("maintenanceObject", MaintenanceObject.class) : null;
		
		//Retrieve Account
		Account_Id accountId = null;
		if (notNull(maintenanceObject) &&
				maintenanceObject.getId().getTrimmedValue().equalsIgnoreCase(CmDelinquencyProcessCalculateTriggerDateConstants.MAINTENANCE_OBJ_ACCOUNT)) {
			accountId = new Account_Id(resultRow.getString("primaryKeyValue1"));
		}
		Account account = notNull(accountId) ? accountId.getEntity() : null;
		
		// Start CB-359
		//Retrieve Main Person of Account
		/*Person person = null;
		AccountPerson accountPerson = null;*/
		/*if(notNull(account)){	
			retrieveAccountCharacteristic(account);
			ListFilter<AccountPerson> acctPerListFilter = account.getPersons().createFilter(" where this.isMainCustomer =:mainCustSw", "CmDelinquencyCancelAccountStatusBalanceAlgComp_Impl");
			acctPerListFilter.bindBoolean("mainCustSw", Bool.TRUE);
			accountPerson = acctPerListFilter.firstRow();
			person = notNull(accountPerson) ? accountPerson.fetchIdPerson() : null;
		}	*/	
		
		if (notNull(account)){			
			
			String accountStatus = retrieveAccountCharacteristic(account);
			
			if(!isBlankOrNull(accountStatus) && accountStatus.equalsIgnoreCase(this.getStatusCharacteristicValue())){
		// End CB-359		
				//Compute Account Overdue Balance
				CmComputeAccountOverdueBalance accountOverdueBalance = CmComputeAccountOverdueBalance.Factory.newInstance();
				
				//Set Account Id
				accountOverdueBalance.setAccountId(account.getId().getIdValue());
				
				//Set Include Unapplied
				Boolean includeUnapplied;
				if (notNull(this.getIncludeUnapplied())
						&& this.getIncludeUnapplied().isYes()){
					includeUnapplied =  true;
				}else{
					includeUnapplied = false;
				}			
				accountOverdueBalance.setIncludeUnapplied(includeUnapplied);
				
				//Set Contract Types Feature Configuration
				accountOverdueBalance.setContractTypesFeatureConfiguration(this.getUnappliedContractTypesFeatureConfig().getId().getIdValue());
				
				//Set Contract Types Option Type
				accountOverdueBalance.setContractTypesOptionType(this.getUnappliedContractTypesOptionType().getLookupValue().fetchIdFieldValue());
				
				//Get Overdue Balance
				Money overdueBalance = accountOverdueBalance.getOverdueBalance();
				
				//Get Threshold Amount
				Money thresholdAmount = new Money(this.getThresholdAmount(),InstallationHelper.getInstallation().getCurrency().getId());
				
				//If Overdue Balance is less than or equal to Threshold Amount, cancel the delinquency
				//and set status reason (if provided)
				if (overdueBalance.isLessThanOrEqual(thresholdAmount)){
					isOkayToCancel = Bool.TRUE;
					statusReasonId = notNull(this.getStatusReason()) ? this.getStatusReason().getId() : null;
				}	
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
	
	/**
	 * This method checks if Characteristic Value if valid for Characteristic Type
	 * @param Characteristic Type to be checked on
	 * @param Characteristic Value to validate
	 */
	private void validateCharValForCharType(CharacteristicType charType, String charVal){
		CharacteristicValue_Id charValueId = new CharacteristicValue_Id(charType, charVal);
		if(isNull(charValueId.getEntity())) {			
			addError(CmMessageRepository.getServerMessage(CmMessages.CHAR_VAL_INVALID_FOR_CHAR_TYPE,
					charVal,
					charType.getId().getIdValue()));
		}
	}
	
	/**
	 * This method fetches level and maintenance object 
	 * for Delinquency Process Id
	 * 
	 * @return resultRow
	 */
	private QueryResultRow fetchLevelAndEntityForDelinquencyProcess() {

		Query<QueryResultRow> query = createQuery(CmDelinquencyProcessCalculateTriggerDateConstants.FETCH_DELIQ_LVL_ID.toString(), "CmDelinquencyCancelAccountStatusBalanceAlgComp_Impl");
		query.bindId("delinProcId", delinquencyProcessId);
		query.bindLookup("relatedObjTypeFlag", CmDelinquencyRelatedObjTypeLookup.constants.CM_DELINQUENCY_LEVEL);
		query.addResult("maintenanceObject", "dpo.id.maintenanceObject");
		query.addResult("primaryKeyValue1", "dpo.id.primaryKeyValue1");

		QueryResultRow resultRow = query.firstRow();

		return resultRow;

	}
	
	// Start CB-359
	/**
	 * This method retrieves Person Characteristic
	 * 
	 * @return Person Characteristic
	 */
	/*private String retrievePersonCharacteristic(Person person){
		PersonCharacteristic perChar = person.getEffectiveCharacteristic(getStatusCharacteristicType());
		
		if (isNull(perChar)){
			return null;
		}
		
		if (!isBlankOrNull(perChar.getAdhocCharacteristicValue())) {				
			return perChar.getAdhocCharacteristicValue().trim();				 
		}else if (!isBlankOrNull(perChar.getCharacteristicValue())){				
			return perChar.getCharacteristicValue().trim();				
		}else{
			return perChar.getCharacteristicValueForeignKey1().trim();
		}		
	}*/
	
	/**
	 * This method retrieves Account Characteristic
	 * 
	 * @return Account Characteristic
	 */
	private String retrieveAccountCharacteristic(Account account){
		AccountCharacteristic acctChar = account.getEffectiveCharacteristic(getStatusCharacteristicType());
		
		if (isNull(acctChar)){
			return null;
		}
		
		if (!isBlankOrNull(acctChar.getAdhocCharacteristicValue())) {				
			return acctChar.getAdhocCharacteristicValue().trim();				 
		}else if (!isBlankOrNull(acctChar.getCharacteristicValue())){				
			return acctChar.getCharacteristicValue().trim();				
		}else{
			return acctChar.getCharacteristicValueForeignKey1().trim();
		}		
	}
	
	// End CB-359
	
	/**
	 * This method is responsible for setting the value 
	 * for Delinquency Process Id
	 * 
	 * @param delinquencyProcessId
	 */
	@Override
	public void setCmDelinquencyProcessId(
			CmDelinquencyProcess_Id delinquencyProcessId) {
		this.delinquencyProcessId = delinquencyProcessId;
	}

	/**
	 * This method is responsible for returning the value 
	 * for Cancel Switch
	 * 
	 * @return isOkayToCancel
	 */
	@Override
	public Bool getOkToCancelSwitch() {
		return this.isOkayToCancel;
	}

	/**
	 * This method is responsible for returning the value 
	 * for Status Reason
	 * 
	 * @return statusReasonId
	 */
	@Override
	public BusinessObjectStatusReason_Id getBusinessObjectStatusReason() {
		return statusReasonId;
	}
}
