/*                                                              
 **************************************************************************                                                                
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * This Delinquency Process Type Cancel Criteria algorithm 
 * checks if the Customer has the Credit and Delinquency Management Referral Characteristic 
 * equal to the CDM Referral Characteristic Value soft parameter
 *                                                             
 **************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.       
 * 2020-05-06   MugdhaP		Initial Version : ANTHM-340 CAB1-9462 Delinquency Framework                    
 *           
 * **************************************************************************
 */
package com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.algorithm;

import java.util.Arrays;
import java.util.List;

import com.splwg.base.api.ListFilter;
import com.splwg.base.api.Query;
import com.splwg.base.api.QueryResultRow;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.domain.common.businessObjectStatusReason.BusinessObjectStatusReason_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicEntity_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.maintenanceObject.MaintenanceObject;
import com.splwg.ccb.domain.customerinfo.person.Person;
import com.splwg.ccb.domain.customerinfo.person.PersonCharacteristic;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.cm.api.lookup.CharacteristicEntityLookup;
import com.splwg.cm.api.lookup.CmDelinquencyRelatedObjTypeLookup;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmDelinquencyProcessCancelCriteriaAlgorithmSpot;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm.CmDelinquencyProcessCalculateTriggerDateConstants;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;

/**
 * @author MugdhaP
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = characteristicType, name = cdmReferralCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = cdmReferralCharacteristicValueList, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = cancelReason, required = true, type = string)})
 */
public class CmCancelSystematicStatementsIfReferredToCDMAlgComp_Impl 
	extends	CmCancelSystematicStatementsIfReferredToCDMAlgComp_Gen 
	implements CmDelinquencyProcessCancelCriteriaAlgorithmSpot {
	
	private CmDelinquencyProcess_Id delinquencyProcessId;
	private Bool isOkayToCancel;
	private BusinessObjectStatusReason_Id statusReason;
	

	@Override
	public void setCmDelinquencyProcessId(
			CmDelinquencyProcess_Id delinquencyProcessId) {
		this.delinquencyProcessId = delinquencyProcessId;
	}

	@Override
	public Bool getOkToCancelSwitch() {
		return this.isOkayToCancel;
	}

	@Override
	public BusinessObjectStatusReason_Id getBusinessObjectStatusReason() {
		return statusReason;
	}

	@Override
	protected void extraSoftParameterValidations(boolean forAlgorithmValidation) {
		// Validate provided CDM Referred Characteristic Type is valid characteristic type
		// for entity Person
		CharacteristicEntity_Id charEntityId = new CharacteristicEntity_Id(getCdmReferralCharacteristicType(), CharacteristicEntityLookup.constants.PERSON);
		
		if (isNull(charEntityId.getEntity())) {
			addError(MessageRepository.charTypeIsInvalidForEntity(getCdmReferralCharacteristicType().getId(), CharacteristicEntityLookup.constants.PERSON.getLookupValue().getEffectiveDescription()));
		}
		
		
	}


	@Override
	public void invoke() {
		isOkayToCancel = Bool.FALSE;
		
		// Retrieve Delinquency Process using the hard parameter Delinquency Process Id
		CmDelinquencyProcess delinquencyProcess = delinquencyProcessId.getEntity();
		
		// If Delinquency Process is Null
		if(isNull(delinquencyProcess)){
			addError(MessageRepository.delinquencyProcessRequired());
		}
		
		// Fetch level and entity for delinquency process
		QueryResultRow resultRow = fetchLevelAndEntityForDelinquencyProcess();

		// If result row is null
		if(isNull(resultRow)){
			addError(MessageRepository.delinquencyProcessRequired());
		}
		
		MaintenanceObject maintenanceObject = resultRow.getEntity("maintenanceObject", MaintenanceObject.class);
		Person_Id personId = null;

		// If maintenance object is person maintenance object
		if (maintenanceObject.getId().getTrimmedValue().equalsIgnoreCase(CmDelinquencyProcessCalculateTriggerDateConstants.MAINTENANCE_OBJ_PERSON)) {
			personId = new Person_Id(resultRow.getString("primaryKeyValue1"));
		}
		
		// If person id is not found on delinquency
		if (isNull(personId)) {
			addError(MessageRepository.deliquencyProcessCMDLReqd(delinquencyProcessId));
		}

		// Retrieve CDM Referred characteristic value from person characteristic
		String cdmReferralCharValue = retrievePersonCharacteristicValue(personId.getEntity(), getCdmReferralCharacteristicType()
				, getProcessDateTime().getDate());
		List<String> cdmReferredCharList = Arrays.asList(getCdmReferralCharacteristicValueList().split("\\s*,\\s*"));
		// If CDM Referral Char value is not null or blank and equal to CDM Referral Char value soft parameter
		if(!isBlankOrNull(cdmReferralCharValue) && cdmReferredCharList.contains(cdmReferralCharValue.trim())) {
			isOkayToCancel = Bool.TRUE;
			statusReason = new BusinessObjectStatusReason_Id(getCancelReason());
		}
	}
	
	
	/**
	 * This method fetches level and maintenance object for delinquencyProcessId
	 * 
	 * @author IlaM
	 * 
	 * @return  resultRow
	 */
	private QueryResultRow fetchLevelAndEntityForDelinquencyProcess() {

		Query<QueryResultRow> query = createQuery(CmDelinquencyProcessCalculateTriggerDateConstants.FETCH_DELIQ_LVL_ID.toString(), "CmDelinquencyProcessCancelCriteriaAlgComp_Impl");
		query.bindId("delinProcId", delinquencyProcessId);
		query.bindLookup("relatedObjTypeFlag", CmDelinquencyRelatedObjTypeLookup.constants.CM_DELINQUENCY_LEVEL);
		query.addResult("maintenanceObject", "dpo.id.maintenanceObject");
		query.addResult("primaryKeyValue1", "dpo.id.primaryKeyValue1");

		QueryResultRow resultRow = query.firstRow();

		return resultRow;

	}
	
	/**
	 * This Method Retrieves Person Characteristic value.
	 *
	 * @author IlaM
	 * 
	 * @param person
	 * @param characteristicType
	 * @param effectiveDate
	 * 
	 * @return person characteristic value
	 * 
	 */
	private String retrievePersonCharacteristicValue(Person person, CharacteristicType characteristicType, Date effectiveDate) {
		String charvalue = "";
		ListFilter<PersonCharacteristic> personCharacteristicFilter = null;
		
		
		personCharacteristicFilter = person.getCharacteristics().createFilter(" where this.id.characteristicType = :characteristicType and  this.id.effectiveDate <= :effectiveDate order by this.id.effectiveDate desc "
					, "CmSystematicStatementCancelCriteriaAlgComp_Impl");
		
		// Binding char type and effective date
		personCharacteristicFilter.bindId("characteristicType", characteristicType.getId());
		personCharacteristicFilter.bindDate("effectiveDate", effectiveDate);
		
		PersonCharacteristic personCharacteristic = personCharacteristicFilter.firstRow();

		// conditions to handle characteristic type and null check
		if (isNull(personCharacteristic)) {
			return charvalue;
		}
		else if (characteristicType.getCharacteristicType().isForeignKeyValue()) {
			charvalue = personCharacteristic.getCharacteristicValueForeignKey1();
		}
		else if (characteristicType.getCharacteristicType().isAdhocValue()) {
			charvalue = personCharacteristic.getAdhocCharacteristicValue();
		}
		else {
			charvalue = personCharacteristic.getCharacteristicValue();
		}

		return charvalue;
	}
}
