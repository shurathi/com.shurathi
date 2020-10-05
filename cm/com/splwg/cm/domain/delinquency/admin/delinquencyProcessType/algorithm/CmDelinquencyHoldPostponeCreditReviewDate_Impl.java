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
 * This algorithm will hold the delinquency process if there is a postpone credit review date
 * on the Account Credit and Collection that is in the future.
 *
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:                  Reason:
 * 2020-08-20  SAnart         CB-352.Initial Version.
 */

package com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.algorithm;

import com.splwg.base.api.Query;
import com.splwg.base.api.QueryResultRow;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.lookup.LogEntryTypeLookup;
import com.splwg.base.api.maintenanceObject.MaintenanceObjectLogHelper;
import com.splwg.base.domain.common.characteristicType.CharacteristicEntity_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.characteristicType.CharacteristicValue_Id;
import com.splwg.ccb.domain.customerinfo.account.Account;
import com.splwg.cm.api.lookup.CharacteristicEntityLookup;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmHoldDelinquencyProcessCriteriaAlgorithmSpot;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessCharacteristic;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessLog;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm.CmDelinquencyProcessHelper;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;

/**
 * @author SAnart
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = characteristicType, name = holdReasonCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = postponeCreditReviewDateHoldReason, required = true, type = string)})
 */

public class CmDelinquencyHoldPostponeCreditReviewDate_Impl extends CmDelinquencyHoldPostponeCreditReviewDate_Gen implements CmHoldDelinquencyProcessCriteriaAlgorithmSpot 
{

	// Class variables
	private Bool holdProcessSwitch;
	private CmDelinquencyProcess_Id delinquencyProcessId;
	private CmDelinquencyProcessHelper cmDelinquencyProcessHelper;

	// All getter methods 
	@Override
	public Bool getIsHoldProcessSwitch() {
		return holdProcessSwitch;
	}

	// All Setter methods
	@Override
	public void setDelinquencyProcessId(CmDelinquencyProcess_Id delinquencyProcessId) {
		this.delinquencyProcessId = delinquencyProcessId;
	}

	/**
	 * Performs parameter validation.
	 * 
	 * @param algorithm
	 */
	protected void extraSoftParameterValidations(boolean forAlgorithmValidation) {

		// Validate Characteristics Types for the Delinquency Process Log entity
		validateCharTypeForEntity(getHoldReasonCharacteristicType(), CharacteristicEntityLookup.constants.CM_DELINQUENCY_PROCESS_LOG);

		// Validate Characteristics Values for Characteristic Type
		validateCharValueForCharType(getHoldReasonCharacteristicType(), getPostponeCreditReviewDateHoldReason());
	}
	
	
	// Main processing
	@Override
	public void invoke() {

		// Initialize variable
		holdProcessSwitch = Bool.FALSE;
		cmDelinquencyProcessHelper = CmDelinquencyProcessHelper.Factory.newInstance();
		
		//Retrieve Account using Delinquency Id 
		Account account = cmDelinquencyProcessHelper.fetchAccountOfDelinquencyProcess(delinquencyProcessId.getEntity());

		if (isNull(account)) {
			// Through Error
			addError(MessageRepository.delinquencyNotLinkedToCustomer(delinquencyProcessId));
		}
		holdProcessSwitch = checkCreditReviewDate(account);

	}

	/**
	 * This method check credit review date
	 * @param person
	 * @return
	 */
	private Bool checkCreditReviewDate(Account account ) {
		Bool holdProcessSwitch = Bool.FALSE;
		Date postponeCreditReviewDate = account.getPostponeCreditReviewUntil();
		
		// If credit review date is after process date time
		if (notNull(postponeCreditReviewDate) && postponeCreditReviewDate.isAfter(getProcessDateTime().getDate())) {
			holdProcessSwitch =  Bool.TRUE;
		
			// Fetch existing delinquency process log for particular char value
			Query<QueryResultRow> existingLogQuery = createQuery(CmHoldDelinquencyProcessConstants.FETCH_EXISTING_DEL_PROC_LOG.toString(), "CmHoldDelinquencyProcessCriteriaDueToCreditReviewDtAlgComp_Impl");
			existingLogQuery.bindId("delProcId", delinquencyProcessId);
			existingLogQuery.bindStringProperty("boStatus", CmDelinquencyProcess.properties.status, delinquencyProcessId.getEntity().getStatus());
			existingLogQuery.bindId("msgCategoryNumber", MessageRepository.addDelProcLogForCharVal(getPostponeCreditReviewDateHoldReason()).getMessageId().getMessageCategoryId());
			existingLogQuery.bindBigInteger("msgNumber", MessageRepository.addDelProcLogForCharVal(getPostponeCreditReviewDateHoldReason()).getMessageId().getMessageNumber());
			existingLogQuery.bindEntity("charType", getHoldReasonCharacteristicType());
			existingLogQuery.bindStringProperty("charValue", CmDelinquencyProcessCharacteristic.properties.characteristicValue, getPostponeCreditReviewDateHoldReason());
			existingLogQuery.addResult("delProcessId", "DPL.id.delinquencyProcess.id");
			existingLogQuery.addResult("sequence", "DPL.id.sequence");
			
			// If no existing log entry found
			if(existingLogQuery.list().isEmpty())
			{
				// No existing log entry add new log entry 
				CharacteristicValue_Id charValId = new CharacteristicValue_Id(getHoldReasonCharacteristicType(), getPostponeCreditReviewDateHoldReason());
				MaintenanceObjectLogHelper<CmDelinquencyProcess, CmDelinquencyProcessLog> logHelper = new MaintenanceObjectLogHelper<CmDelinquencyProcess, CmDelinquencyProcessLog>(
						delinquencyProcessId.getEntity().getBusinessObject().getMaintenanceObject(), delinquencyProcessId.getEntity());
				logHelper.addLogEntry(LogEntryTypeLookup.constants.SYSTEM, MessageRepository.addDelProcLogForCharVal(charValId.getEntity().fetchLanguageDescription()), null, charValId.getEntity());
			} 
			
		}
		
		return holdProcessSwitch;
	}
	
	/**
	 * Validates that the Characteristic Type is valid for the given entity.
	 * 
	 * @param charType
	 * @param charEntity
	 */
	private void validateCharTypeForEntity(CharacteristicType charType, CharacteristicEntityLookup charEntity) {

		CharacteristicEntity_Id charEntityId = new CharacteristicEntity_Id(charType, charEntity);
		if (isNull(charEntityId.getEntity())) {

			addError(MessageRepository.charTypeIsInvalidForEntity(charType.getId(), charEntity.getLookupValue().getEffectiveDescription()));

		}
	}
	
	/**
	 * Validate Char Value For Char Type
	 * @param charType
	 * @param charValue
	 */
	private void validateCharValueForCharType(CharacteristicType charType, String charValue) {
		if (charType.getCharacteristicType().isPredefinedValue()) {
			
			CharacteristicValue_Id charValId = new CharacteristicValue_Id(charType, charValue);
			if (isNull(charValId.getEntity())) {

				addError(MessageRepository.charValueIsInvalidForCharType(charType.getId(), charValue));
			}
		}
	}


}

