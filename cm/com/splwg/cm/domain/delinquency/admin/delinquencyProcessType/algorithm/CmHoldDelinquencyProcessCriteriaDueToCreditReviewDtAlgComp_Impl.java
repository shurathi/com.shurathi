/*                                                               
 *******************************************************************************************************************                                                                
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * This algorithm will hold the delinquency process if there is a postpone credit review date
 * on the Person-Collections that is in the future.
 *                                                             
 *******************************************************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 * 2020-05-06   MugdhaP		Initial Version : ANTHM-340 CAB1-9462 Delinquency Framework        
 * ******************************************************************************************************************
 */
package com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.algorithm;

import com.splwg.base.api.ListFilter;
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
import com.splwg.ccb.domain.customerinfo.account.AccountPerson;
import com.splwg.ccb.domain.customerinfo.person.Person;
import com.splwg.cm.api.lookup.CharacteristicEntityLookup;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmHoldDelinquencyProcessCriteriaAlgorithmSpot;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessCharacteristic;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessLog;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm.CmDelinquencyProcessConstant;
import com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm.CmDelinquencyProcessHelper;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;

/**
 * @author MugdhaP
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = characteristicType, name = holdReasonCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = postponeCreditReviewDateHoldReason, required = true, type = string)})
 */

public class CmHoldDelinquencyProcessCriteriaDueToCreditReviewDtAlgComp_Impl extends CmHoldDelinquencyProcessCriteriaDueToCreditReviewDtAlgComp_Gen implements CmHoldDelinquencyProcessCriteriaAlgorithmSpot {

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
		
		// Retrieve Person and account from related object
		Person person = cmDelinquencyProcessHelper.fetchPersonOfDelinquencyProcess(delinquencyProcessId.getEntity());
		Account account = cmDelinquencyProcessHelper.fetchAccountOfDelinquencyProcess(delinquencyProcessId.getEntity());

		// If both person and account is null for delinquency through an error
		if (isNull(person) && isNull(account)) {
			// Through Error
			addError(MessageRepository.delinquencyNotLinkedToCustomer(delinquencyProcessId));
		}
		
		// If delinquency process is person level check for credit review date
		if (notNull(person)) {
			holdProcessSwitch = checkCreditReviewDate(person);
		}
		
		// If delinquency is account level check credit review date on main
		// person of account
		else {
			Person mainPerson = null;

			// Get Primary Person
			ListFilter<AccountPerson> acctPerListFilter = account.getPersons().createFilter(" where this.isMainCustomer =:mainCustSw", "CmHoldDelinquencyProcessCriteriaDueToCreditReviewDtAlgComp_Impl");
			acctPerListFilter.bindBoolean("mainCustSw", Bool.TRUE);
			AccountPerson accountPerson = acctPerListFilter.firstRow();
			
			// if account person is not null
			if (notNull(accountPerson)) {
				mainPerson = accountPerson.fetchIdPerson();
			}

			// If main person retrieved from account check for credit review
			// date
			if (notNull(mainPerson)) {
				holdProcessSwitch = checkCreditReviewDate(mainPerson);
			}
		}
	}

	/**
	 * This method check credit review date
	 * @param person
	 * @return
	 */
	private Bool checkCreditReviewDate(Person person) {
		Bool holdProcessSwitch = Bool.FALSE;
		Query<Date> query = createQuery(CmDelinquencyProcessConstant.PER_COL_FROM_PERSON_QUERY.toString(), "CmHoldDelinquencyProcessCriteriaDueToCreditReviewDtAlgComp_Impl");
		query.bindEntity("person", person);
		query.addResult("postponeCreditRevieDate", "PERCOL.postponeCreditReviewUntil");
		Date postponeCreditRevieDate = query.firstRow();
		
		// If credit review date is after process date time
		if (notNull(postponeCreditRevieDate) && postponeCreditRevieDate.isAfter(getProcessDateTime().getDate())) {
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
			if(existingLogQuery.list().isEmpty()){
				
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

