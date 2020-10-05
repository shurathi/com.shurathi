package com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm;

/*
 **********************************************************************************************************************************************
 * 
 * PROGRAM DESCRIPTION:
 * 
 * This algorithm monitors all policies of the customer that were transitioned to Pending Termination by the delinquency process and waits until
 * all policies have left this status
 * These policies are stored as delinquency process characteristics
 * 
 **********************************************************************************************************************************************
 * 
 * 
 * CHANGE HISTORY:
 * 
 * Date:       	by:         Reason:
 * 2020-05-06   MugdhaP		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework
 **********************************************************************************************************************************************
 */
import java.util.List;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.BusinessObjectStatusCode;
import com.splwg.base.api.lookup.BusinessObjectStatusTransitionConditionLookup;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatusAutoTransitionAlgorithmSpot;
import com.splwg.base.domain.common.businessObjectStatusReason.BusinessObjectStatusReason_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicEntity_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.cm.api.lookup.CharacteristicEntityLookup;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;

/**
 * @author MugdhaP
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = characteristicType, name = policyCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = policyPendingTerminationStatus, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = policyPendingTerminationOverrideStatus, required = true, type = string)})
 */
public class CmWaitPolicyStatusAlgComp_Impl extends CmWaitPolicyStatusAlgComp_Gen implements BusinessObjectStatusAutoTransitionAlgorithmSpot {

	private BusinessObjectInstanceKey boInstanceKey;
	private Boolean skipAutoTransition;

	@Override
	public void invoke() {

		skipAutoTransition = false;
		CmDelinquencyProcess_Id delinquencyProcId = new CmDelinquencyProcess_Id(boInstanceKey.getString("delinquencyProcessId"));

		List<SQLResultRow> sqlResultRowList = getPolicies(delinquencyProcId);
		if (notNull(sqlResultRowList) && !sqlResultRowList.isEmpty())
		{
			skipAutoTransition = true;
		}

	}

	/**
	 * Performs parameter validation.
	 * 
	 * @param forAlgorithmValidation
	 */
	protected void extraSoftParameterValidations(boolean forAlgorithmValidation) {

		// Check if Termination Date Characteristic Type parameter value is a
		// valid Characteristic for Delinquency Process entity
		validateCharTypeForEntity(getPolicyCharacteristicType(), CharacteristicEntityLookup.constants.CM_DELINQUENCY_PROCESS_LOG);
	}

	/**
	 * Validates that the Characteristic Type is valid for the given entity.
	 * 
	 * @param charType
	 * @param charEntity
	 */
	private void validateCharTypeForEntity(CharacteristicType charType, CharacteristicEntityLookup charEntity) {
		// Validate If Characteristic Type is Valid Entity
		CharacteristicEntity_Id charEntityId = new CharacteristicEntity_Id(charType, charEntity);
		// If Characteristic Entity is not found raise an error
		if (isNull(charEntityId.getEntity())) {
			addError(MessageRepository.charTypeIsInvalidForEntity(charType.getId(), charEntity.getLookupValue().getEffectiveDescription()));
		}

	}

	// Retrieve all policies stored on the Delinquency Process using parameter
	// Policy Characteristic Type and whose status matches either
	// parameter Policy Pending Termination Status or parameter Policy Pending
	// Termination Override Status
	protected List<SQLResultRow> getPolicies(CmDelinquencyProcess_Id delinquencyProcId)
	{

		PreparedStatement preparedStatement = null;
		StringBuilder RETRIEVE_ACTIVE_POLICIES = null;
		List<SQLResultRow> sqlResultRowList = null;

		RETRIEVE_ACTIVE_POLICIES = new StringBuilder()
				.append(" SELECT PO.POLICY_ID FROM ")
				.append(" CM_DELIN_PROC_LOG DPL, CI_POLICY PO ")
				.append(" WHERE CM_DELIN_PROC_ID = :delinquencyProcessId ")
				.append(" AND DPL.CHAR_TYPE_CD = :parameterPolicyCharacteristicType ")
				.append(" AND DPL.CHAR_VAL_FK1 = PO.POLICY_ID ")
				.append(" AND (PO.BO_STATUS_CD = :parameterPolicyPendingTermination ")
				.append(" OR PO.BO_STATUS_CD = :parameterPolicyPendingTerminationOverride) ");
		preparedStatement = createPreparedStatement(RETRIEVE_ACTIVE_POLICIES.toString(), "CmWaitPolicyStatusAlgComp_Impl");

		preparedStatement.bindId("delinquencyProcessId", delinquencyProcId);
		preparedStatement.bindEntity("parameterPolicyCharacteristicType", getPolicyCharacteristicType());
		preparedStatement.bindString("parameterPolicyPendingTermination", getPolicyPendingTerminationStatus(), "BO_STATUS_CD");
		preparedStatement.bindString("parameterPolicyPendingTerminationOverride", getPolicyPendingTerminationOverrideStatus(), "BO_STATUS_CD");

		if (notNull(preparedStatement))
			sqlResultRowList = preparedStatement.list();

		preparedStatement.close();
		return sqlResultRowList;

	}

	@Override
	public void setBusinessObject(BusinessObject paramBusinessObject) {
	}

	@Override
	public void setBusinessObjectKey(BusinessObjectInstanceKey paramBusinessObjectInstanceKey) {
		boInstanceKey = paramBusinessObjectInstanceKey;

	}

	@Override
	public BusinessObjectStatusCode getNextStatus() {
		return null;
	}

	@Override
	public BusinessObjectStatusTransitionConditionLookup getNextStatusCondition() {
		return null;
	}

	@Override
	public boolean getUseDefaultNextStatus() {
		return false;
	}

	@Override
	public boolean getSkipAutoTransitioning() {
		return skipAutoTransition;
	}

	@Override
	public BusinessObjectStatusReason_Id getStatusChangeReasonId() {
		return null;
	}

}

