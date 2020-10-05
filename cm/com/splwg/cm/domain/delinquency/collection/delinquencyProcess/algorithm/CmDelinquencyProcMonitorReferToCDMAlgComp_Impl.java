/*                                                             
 *****************************************************************************                                                                
 *                                                                
 * PROGRAM DESCRIPTION:   
 *                                         
 * This algorithm checks whether the Person related to the Delinquency process 
 * either has effective Referral characteristic with value N or the Referral 
 * characteristic is no longer present for the person. In that case the 
 * Delinquency process is canceled                                                           
 *                                                             
 *****************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    	   	Reason:                                     
 * YYYY-MM-DD  	IN     	   	Reason text.                                
 *           
 * 2020-05-06   MugdhaP		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework
 *
 *****************************************************************************
 */
package com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm;

import com.splwg.base.api.ListFilter;
import com.splwg.base.api.Query;
import com.splwg.base.api.QueryResultRow;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.domain.common.businessObjectStatusReason.BusinessObjectStatusReason_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicEntity_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.characteristicType.CharacteristicValue;
import com.splwg.base.domain.common.characteristicType.CharacteristicValue_Id;
import com.splwg.base.domain.common.maintenanceObject.MaintenanceObject;
import com.splwg.ccb.api.lookup.CharacteristicEntityLookup;
import com.splwg.ccb.domain.customerinfo.account.AccountPerson;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.customerinfo.person.Person;
import com.splwg.ccb.domain.customerinfo.person.PersonCharacteristic;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.cm.api.lookup.CmDelinquencyRelatedObjTypeLookup;
import com.splwg.cm.domain.delinquency.admin.delinquencyCustomerMonitor.CmDelinquencyProcessCalculateTriggerDateConstants;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmDelinquencyProcessCancelCriteriaAlgorithmSpot;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;

/**
 * @author MugdhaP
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = characteristicType, name = referralFlagCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = referralFlagCharacteristicValue, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = cancelReason, required = true, type = string)})
 */
public class CmDelinquencyProcMonitorReferToCDMAlgComp_Impl 
extends	CmDelinquencyProcMonitorReferToCDMAlgComp_Gen 
implements	CmDelinquencyProcessCancelCriteriaAlgorithmSpot {

	private CmDelinquencyProcess_Id delinquencyProcessId;
	private Bool isOkToCancelSwitch = Bool.FALSE;
	private BusinessObjectStatusReason_Id statusReason;
	
	@Override
	public void setCmDelinquencyProcessId(
			CmDelinquencyProcess_Id delinquencyProcessId) {
		this.delinquencyProcessId = delinquencyProcessId;
	}

	@Override
	public Bool getOkToCancelSwitch() {
		return isOkToCancelSwitch;
	}
	
	@Override
	public BusinessObjectStatusReason_Id getBusinessObjectStatusReason() {
		return statusReason;
	}

	

	@Override
	protected void extraSoftParameterValidations(boolean forAlgorithmValidation) {
		
		// Validate if referral char value is a valid char value for referral flag char type
		if (!isValidCharVal(getReferralFlagCharacteristicType(), getReferralFlagCharacteristicValue())) {
				addError(MessageRepository.invalidCharValue(getReferralFlagCharacteristicValue(),
					getReferralFlagCharacteristicType().getId().getTrimmedValue()));
		}
		
		CharacteristicEntity_Id charEntityId = new CharacteristicEntity_Id(getReferralFlagCharacteristicType(), CharacteristicEntityLookup.constants.PERSON);
		
		// if Characteristic Entity is null
		if(isNull(charEntityId) || isNull(charEntityId.getEntity())) {
			// Throw Error - Char type is not a valid char for the person entity.
			addError(MessageRepository.invalidCharTypeForEntity(getReferralFlagCharacteristicType().getId().getIdValue(), "Person"));
		}
	}

	@Override
	public void invoke() {
		
		CmDelinquencyProcess delinquencyProcess = delinquencyProcessId.getEntity();
		
		// If delinquency process not found.
		if (isNull(delinquencyProcess)){
			// Throw Error - Delinquency Process Required
			addError(MessageRepository.delinquencyProcessRequired());
		}
		
		// Fetch Level and entity for delinquency process
		QueryResultRow resultRow = fetchLevelAndEntityForDelinquecnyProcess();

		MaintenanceObject maintenanceObject = resultRow.getEntity("maintenanceObject", MaintenanceObject.class);
		Person_Id personId = null;
		Account_Id accountId = null;

		// If maintenance object is of person
		if (maintenanceObject.getId().getTrimmedValue().equalsIgnoreCase(CmDelinquencyProcessCalculateTriggerDateConstants.MAINTENANCE_OBJ_PERSON)) {
			personId = new Person_Id(resultRow.getString("primaryKeyValue1"));
		} 
		// If maintenance object is of account
		else if (maintenanceObject.getId().getTrimmedValue().equalsIgnoreCase(CmDelinquencyProcessCalculateTriggerDateConstants.MAINTENANCE_OBJ_ACCOUNT)) {
			accountId = new Account_Id(resultRow.getString("primaryKeyValue1"));
			
			// Retrieving main person of account.
			ListFilter<AccountPerson> accountPersonListFilter = accountId.getEntity().getPersons().createFilter
					(" where this.isMainCustomer = :isMainCustomer ", "CmDelinquencyProcMonitorReferToCDMAlgComp_Impl.invoke()");
			
			// Binding reference variable
			accountPersonListFilter.bindBoolean("isMainCustomer", Bool.TRUE);
			
			AccountPerson accountPerson = accountPersonListFilter.firstRow();
			personId = notNull(accountPerson)?accountPerson.getId().getPersonId():null;
		}
		
		// If person is not retrieved from delinquency process
		if (isNull(personId) || isNull(personId.getEntity())) {
			// Throw error No Person found related to Delinquency Process %1
			addError(MessageRepository.noPersonFoundForDelinquencyProcess(delinquencyProcessId.getIdValue()));
			
		}
		
		Person person = personId.getEntity();
		
		// Retrieve referral flag characteristic of person
		PersonCharacteristic referralFlagChar = person.getEffectiveCharacteristic(getReferralFlagCharacteristicType());		
		
		// If referral flag is not set to person characteristic
		if (isNull(referralFlagChar)) {
			isOkToCancelSwitch = Bool.TRUE;
			statusReason = new BusinessObjectStatusReason_Id(getCancelReason());
		}
		else {
			// If referral flag char is equal to the referral flag char value soft parameter.
			if (referralFlagChar.getCharacteristicValue().trim().equals(getReferralFlagCharacteristicValue().trim())) {
				isOkToCancelSwitch = Bool.TRUE;
				statusReason = new BusinessObjectStatusReason_Id(getCancelReason());
			}
		}
	}
	
	/**
	 * This method fetches level and maintenance object for delinquencyProcessId
	 * 
	 * @author IlaM
	 * 
	 * @return resultRow
	 */
	private QueryResultRow fetchLevelAndEntityForDelinquecnyProcess() {

		Query<QueryResultRow> query = createQuery(CmDelinquencyProcessCalculateTriggerDateConstants.FETCH_DELIQ_LVL_ID.toString(), "CmDelinquencyProcessCancelCriteriaAlgComp_Impl");
		query.bindId("delinProcId", delinquencyProcessId);
		query.bindLookup("relatedObjTypeFlag", CmDelinquencyRelatedObjTypeLookup.constants.CM_DELINQUENCY_LEVEL);
		query.addResult("maintenanceObject", "dpo.id.maintenanceObject");
		query.addResult("primaryKeyValue1", "dpo.id.primaryKeyValue1");

		QueryResultRow resultRow = query.firstRow();

		return resultRow;

	}
	
	/**
	 * This method checks if char value provided is valid for characteristic
	 * type
	 * 
	 * @author IlaM
	 * 
	 * @param charType
	 * @param inputCharVal
	 * 
	 * @return is valid char val
	 * 
	 */
	private boolean isValidCharVal(CharacteristicType charType, String inputCharVal) {

		CharacteristicValue_Id charValId = null;
		CharacteristicValue charVal = null;

		charValId = new CharacteristicValue_Id(charType, inputCharVal);
		charVal = charValId.getEntity();

		if(isNull(charVal)) {
			return false;
		}

		return true;

	}

	
}
