/*
 **********************************************************************************************************************************************
 * 
 * PROGRAM DESCRIPTION:
 * 
 * This algorithm transitions active policies of the customer to the pending termination status passing in the termination date as per state 
 * rules
 * 
 **********************************************************************************************************************************************
 * 
 * 
 * CHANGE HISTORY:
 * 
 * Date:       	by:          	Reason:
 * 2020-05-06   MugdhaP			Initial version : ANTHM-340 CAB1-9462 Delinquency Framework
 **********************************************************************************************************************************************
 */
package com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.BusinessObjectStatusCode;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.lookup.BusinessObjectStatusTransitionConditionLookup;
import com.splwg.base.api.lookup.LogEntryTypeLookup;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectEnterStatusAlgorithmSpot;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatus_Id;
import com.splwg.base.domain.common.businessObjectStatusReason.BusinessObjectStatusReason_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicEntity_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.message.Message_Id;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.ccb.domain.insurance.policy.Policy;
import com.splwg.ccb.domain.insurance.policy.Policy_Id;
import com.splwg.cm.api.lookup.CharacteristicEntityLookup;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessCharacteristic;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.cm.domain.delinquency.utils.CmPolicyConstants;
import com.splwg.cm.domain.delinquency.utils.CmPolicyHelper;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;

/**
 * @author MugdhaP
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (name = policyActiveStatus, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = policyPendingTerminationStatus, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = policyPendingTerminationOverrideStatus, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = policyTerminatedStatus, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = terminationStatusReason, required = true, type = string)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = terminationDateCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = delinquencyProcessCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = messageCategory, name = policyLogMessageCategory, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = policyLogMessageNumber, required = true, type = integer)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = policyCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = messageCategory, name = delinquencyLogMessageCategory, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = delinquencyLogMessageNumber, required = true, type = integer)})
 */
public class CmTransitionPolicyToPendingTerminationAlgComp_Impl extends CmTransitionPolicyToPendingTerminationAlgComp_Gen implements BusinessObjectEnterStatusAlgorithmSpot {

	private BusinessObjectInstanceKey delinquencyProcessBusinessObjectInstKey = null;
	private BusinessObjectInstance delinquencyProcessBoInstance = null;
	private BusinessObjectInstance policyBoInstance = null;
	private Date terminationDate = null;
	private BusinessObjectStatusCode businessObjectStatusCode = null;

	@Override
	public void invoke() {

		String delinquencyPersonId = null;
		String delinquencyAccountId = null;
		String terminationDateString = null;
		Date policyTermDate = null;
		String policyIdString = null;
		CmDelinquencyProcessCharacteristic delinquencyProcessChar = null;
		
		// Fetch Delinquency process Id
		CmDelinquencyProcess_Id delinquencyProcId = new CmDelinquencyProcess_Id(delinquencyProcessBusinessObjectInstKey.getString("delinquencyProcessId"));
		this.delinquencyProcessBoInstance = BusinessObjectDispatcher.read(this.delinquencyProcessBusinessObjectInstKey, true);

		// Retrieve the Delinquency Process Person or Account

		// Fetch Person Id
		if (notNull(this.delinquencyProcessBoInstance.getElement().selectSingleNode("personId")))
			delinquencyPersonId = this.delinquencyProcessBoInstance.getElement().selectSingleNode("personId").getText();

		// Fetch Account Id
		if (notNull(this.delinquencyProcessBoInstance.getElement().selectSingleNode("accountId")))
			delinquencyAccountId = this.delinquencyProcessBoInstance.getElement().selectSingleNode("accountId").getText();

		// If both are null or both are populated issue an error
		if ((isNull(delinquencyAccountId) && isNull(delinquencyPersonId)) || (notNull(delinquencyAccountId) && notNull(delinquencyPersonId)))
			addError(MessageRepository.accoutAndPersonNotFound());

		// Retrieve the Termination Date from the Delinquency Process using
		// parameter Termination Date Characteristic Type		
		if(notNull(delinquencyProcId.getEntity()))
		{
			delinquencyProcessChar = delinquencyProcId.getEntity().getEffectiveCharacteristic(getTerminationDateCharacteristicType());
		}
		if (notNull(delinquencyProcessChar))
		{
			if (delinquencyProcessChar.fetchIdCharacteristicType().getCharacteristicType().isAdhocValue()) {
				terminationDateString =delinquencyProcessChar.getAdhocCharacteristicValue();
			} else if (delinquencyProcessChar.fetchIdCharacteristicType().getCharacteristicType().isPredefinedValue()) {
				terminationDateString = delinquencyProcessChar.getCharacteristicValue();
			} else if (delinquencyProcessChar.fetchIdCharacteristicType().getCharacteristicType().isForeignKeyValue()) {
				terminationDateString = delinquencyProcessChar.getCharacteristicValueForeignKey1();
			}

		}

		// Throw error if logs for termination date characteristic type missing
		// in Delinquency process
		if (isBlankOrNull(terminationDateString))
		{
			addError(MessageRepository.cannotDetermineterminationDateOnDelinProc(getTerminationDateCharacteristicType().getId()));
		}
		else
			terminationDate = Date.fromIso(terminationDateString.trim());

		// Retrieve all policies for the delinquent customer or account using the
		// termination date as reference
		List<SQLResultRow> sqlResultRowList = getPolicies(terminationDate, delinquencyPersonId, delinquencyAccountId);

		if (notNull(sqlResultRowList))
		{

			Iterator<SQLResultRow> policyIterator = sqlResultRowList.iterator();
			while (policyIterator.hasNext()) {
				SQLResultRow policy = (SQLResultRow) policyIterator.next();

				policyIdString = policy.getString("POLICY_ID");
				Policy_Id policyId = new Policy_Id(policyIdString.trim());

				// Validate Policy Status Parameters
				if(notNull(policyId.getEntity()))
				validatePolicyStatusParameters(policyId.getEntity());

				// Get Policys Termination date and create Policy Logs
				if(notNull(policyId.getEntity()) && notNull(delinquencyProcId.getEntity()) && notNull(terminationDate))
				policyTermDate = updatePolicy(policyId.getEntity(), terminationDate, delinquencyProcId);

				// Create Delinquency Process Logs
				if(notNull(policyId.getEntity()) && notNull(delinquencyProcId.getEntity()) && notNull(policyTermDate))
				createDelinquencyProcessLog(policyId.getEntity(), delinquencyProcId, policyTermDate);
			}

		}

	}

	/**
	 * @param policy
	 * @param delinquencyProcess_Id
	 * @param policyTermDate
	 * This method adds the log entry on Delinquency Process.
	 */
	private void createDelinquencyProcessLog(Policy policy, CmDelinquencyProcess_Id delinquencyProcId, Date policyTermDate) {

		CmDelinquencyProcessHelper cmDelinquencyProcessHelper = CmDelinquencyProcessHelper.Factory.newInstance();
		List<String> charValues = new ArrayList<String>();
		charValues.add(policy.getId().getIdValue());
		cmDelinquencyProcessHelper.addDelinquencyMOLog(MessageRepository.logMessage(businessObjectStatusCode.getDescription(), policyTermDate.toLocalizedString(), getDelinquencyLogMessageNumber().intValue()), charValues, LogEntryTypeLookup.constants.SYSTEM, getPolicyCharacteristicType(), delinquencyProcId.getEntity().getBusinessObject(), delinquencyProcId.getEntity(), null);

	}

	/**
	 * @param policy
	 * This method validates if policy status from algorithm parameter are valid BO status.
	 */
	protected void validatePolicyStatusParameters(Policy policy)
	{

		// Validate Business if statuses are valid for Policy Business Object
		BusinessObjectStatus_Id businessObjectStatus_Id = new BusinessObjectStatus_Id(policy.getBusinessObject(), getPolicyPendingTerminationStatus());
		if (isNull(businessObjectStatus_Id.getEntity()))
			addError(MessageRepository.invalidBusinessObjectStatus(getPolicyPendingTerminationStatus(), policy.getBusinessObject().getId()));

		businessObjectStatus_Id = new BusinessObjectStatus_Id(policy.getBusinessObject(), getPolicyPendingTerminationOverrideStatus());
		if (isNull(businessObjectStatus_Id.getEntity()))
			addError(MessageRepository.invalidBusinessObjectStatus(getPolicyPendingTerminationOverrideStatus(), policy.getBusinessObject().getId()));

	}

	/**
	 * @param policy
	 * This method updates policy status.
	 */
	protected Date updatePolicy(Policy policy, Date termDate, CmDelinquencyProcess_Id delinquencyProcId)
	{
		Date policyTermDate = null;
		String derivedNextStatus = null;

		if (policy.getStatus().trim().equals(getPolicyActiveStatus().trim()))
		{
			derivedNextStatus = getPolicyPendingTerminationStatus();
		}
		else
		{
			derivedNextStatus = getPolicyPendingTerminationOverrideStatus();
		}

		policyBoInstance = BusinessObjectInstance.create(policy.getBusinessObject());
		policyBoInstance.set("policy", policy.getId().getIdValue());
		policyBoInstance = BusinessObjectDispatcher.read(policyBoInstance);
		policyBoInstance.set("boStatus", derivedNextStatus);
		COTSInstanceNode terminationInformation = policyBoInstance
				.getGroup(CmPolicyConstants.TERMINATION_INFO_GROUP_ELEMENT);

		if (termDate.isBefore(policy.getStartDate()))
		{
			policyTermDate = policy.getStartDate();
			terminationInformation.set(CmPolicyConstants.TERMINATION_DATE_ELEMENT, policy.getStartDate());
		}
		else
		{
			policyTermDate = termDate;
			terminationInformation.set(CmPolicyConstants.TERMINATION_DATE_ELEMENT, termDate);

		}

		terminationInformation.set(CmPolicyConstants.TERMINATION_REASON_ELEMENT, getTerminationStatusReason());

		BusinessObjectDispatcher.fastUpdate(policyBoInstance.getDocument());

		businessObjectStatusCode = new BusinessObjectStatusCode(policyBoInstance.getBusinessObject().getId(), derivedNextStatus);

		createPolicyLog(policy, delinquencyProcId);

		return policyTermDate;
	}

	/**
	 * @param policy
	 * @param delinquencyProcess_Id
	 * This method adds the log entry on policy.
	 */

	public void createPolicyLog(Policy policy, CmDelinquencyProcess_Id delinquencyProcess_Id) {
		CmPolicyHelper cmpolicyHelper = CmPolicyHelper.Factory.newInstance();
		List<String> charValues = new ArrayList<String>();
		charValues.add(delinquencyProcess_Id.getIdValue());
		cmpolicyHelper.addPolicyMOLog(MessageRepository.logCreatedMessage(businessObjectStatusCode.getDescription(), getPolicyLogMessageNumber().intValue()), charValues, LogEntryTypeLookup.constants.SYSTEM, getDelinquencyProcessCharacteristicType(), policy.getBusinessObject(), policy, null);
	}

	/**
	 * @param termDate
	 * @param delinquencyPersonId
	 * @param delinquencyAccountId
	 * Retrieve all policies for the delinquent customer/account using the termination date as reference.
	 */

	protected List<SQLResultRow> getPolicies(Date termDate, String delinquencyPersonId, String delinquencyAccountId)
	{

		PreparedStatement preparedStatement = null;
		StringBuilder RETRIEVE_ACTIVE_POLICIES = null;
		List<SQLResultRow> sqlResultRowList = null;

		if (!isBlankOrNull(delinquencyAccountId))
		{
			Account_Id accountId = new Account_Id(delinquencyAccountId).getEntity().getId();
			RETRIEVE_ACTIVE_POLICIES = new StringBuilder()
					.append(" SELECT DISTINCT PO.POLICY_ID, PO.START_DT,PO.BO_STATUS_CD FROM ")
					.append(" CI_POLICY PO, CI_POLICY_PER PPER, CI_ACCT_PER APER ")
					.append(" WHERE APER.ACCT_ID = :delinquencyAccountId AND (PO.BO_STATUS_CD = :activeStatus OR (PO.BO_STATUS_CD = :terminatedStatus AND PO.END_DT > :terminationDate)) ")
					.append(" AND PPER.POLICY_ID = PO.POLICY_ID AND PPER.MAIN_CUST_SW = 'Y' ")
					.append(" AND PPER.START_DT = (SELECT MAX(PPER2.START_DT) FROM CI_POLICY_PER PPER2 ")
					.append(" WHERE PPER2.POLICY_ID = PPER.POLICY_ID ")
					.append(" AND MAIN_CUST_SW = 'Y') ")
					.append(" AND APER.PER_ID = PPER.PER_ID AND APER.MAIN_CUST_SW = 'Y' ");
			preparedStatement = createPreparedStatement(RETRIEVE_ACTIVE_POLICIES.toString(), "CmTransitionPolicyToPendingTerminationAlgComp_Impl");

			preparedStatement.bindId("delinquencyAccountId", accountId);
			preparedStatement.bindDate("terminationDate", termDate);
			preparedStatement.bindString("activeStatus", getPolicyActiveStatus(), "BO_STATUS_CD");
			preparedStatement.bindString("terminatedStatus", getPolicyTerminatedStatus(), "BO_STATUS_CD");
		}
		if (!isBlankOrNull(delinquencyPersonId))
		{
			Person_Id personId = new Person_Id(delinquencyPersonId).getEntity().getId();
			RETRIEVE_ACTIVE_POLICIES = new StringBuilder()
					.append(" SELECT DISTINCT PO.POLICY_ID, PO.START_DT,PO.BO_STATUS_CD FROM ")
					.append(" CI_POLICY PO, CI_POLICY_PER PPER ")
					.append(" WHERE PPER.PER_ID = :delinquencyPersonId AND (PO.BO_STATUS_CD = :activeStatus OR (PO.BO_STATUS_CD = :terminatedStatus AND PO.END_DT > :terminationDate)) ")
					.append(" AND PPER.POLICY_ID = PO.POLICY_ID AND PPER.MAIN_CUST_SW = 'Y' ")
					.append(" AND PPER.START_DT = (SELECT MAX(PPER2.START_DT) FROM CI_POLICY_PER PPER2 ")
					.append(" WHERE PPER2.POLICY_ID = PPER.POLICY_ID ")
					.append(" AND MAIN_CUST_SW = 'Y') ");
			preparedStatement = createPreparedStatement(RETRIEVE_ACTIVE_POLICIES.toString(), "CmTransitionPolicyToPendingTerminationAlgComp_Impl");

			preparedStatement.bindId("delinquencyPersonId", personId);
			preparedStatement.bindDate("terminationDate", termDate);
			preparedStatement.bindString("activeStatus", getPolicyActiveStatus(), "BO_STATUS_CD");
			preparedStatement.bindString("terminatedStatus", getPolicyTerminatedStatus(), "BO_STATUS_CD");
		}

		if (notNull(preparedStatement))
			sqlResultRowList = preparedStatement.list();

		preparedStatement.close();
		return sqlResultRowList;

	}

	/**
	 * Performs parameter validation.
	 * 
	 * @param forAlgorithmValidation
	 */
	protected void extraSoftParameterValidations(boolean forAlgorithmValidation) {

		// Check if Termination Date Characteristic Type parameter value is a valid
		// characteristic for Delinquency Process entity
		validateCharTypeForEntity(getTerminationDateCharacteristicType(), CharacteristicEntityLookup.constants.CM_DELINQUENCY_PROCESS);

		// Check if Delinquency Process Characteristic Type parameter value is a
		// valid
		// characteristic for Policy Log entity
		validateCharTypeForEntity(getDelinquencyProcessCharacteristicType(), CharacteristicEntityLookup.constants.POLICY_LOG);

		// Check that the Message Category parameter value and Message Number
		// parameter value combination is valid
		Message_Id messageId = new Message_Id(getPolicyLogMessageCategory(), getPolicyLogMessageNumber());
		if (isNull(messageId.getEntity()))
		{
			addError(MessageRepository.invalidMessageCategoryMessageNumberCombination(getAlgorithm().fetchLanguageDescription(), getAlgorithm().getAlgorithmType().getParameterAt(7).fetchLanguageParameterLabel(), getPolicyLogMessageCategory().fetchLanguageDescription(), String.valueOf(getPolicyLogMessageNumber())));
		}

		// Check if Policy Characteristic Type parameter value is a valid
		// characteristic for Delinquency Process Log entity
		validateCharTypeForEntity(getPolicyCharacteristicType(), CharacteristicEntityLookup.constants.CM_DELINQUENCY_PROCESS_LOG);

		// Check that the Message Category parameter value and Message Number
		// parameter value combination is valid
		messageId = new Message_Id(getDelinquencyLogMessageCategory(), getDelinquencyLogMessageNumber());
		if (isNull(messageId.getEntity()))
		{
			addError(MessageRepository.invalidMessageCategoryMessageNumberCombination(getAlgorithm().fetchLanguageDescription(), getAlgorithm().getAlgorithmType().getParameterAt(10).fetchLanguageParameterLabel(), getDelinquencyLogMessageCategory().fetchLanguageDescription(), String.valueOf(getDelinquencyLogMessageNumber())));
		}

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
		// If Characteristic Entity is not found,raise an error
		if (isNull(charEntityId.getEntity())) {
			addError(MessageRepository.charTypeIsInvalidForEntity(charType.getId(), charEntity.getLookupValue().getEffectiveDescription()));
		}

	}

	@Override
	public void setBusinessObject(BusinessObject paramBusinessObject) {
	}

	@Override
	public void setBusinessObjectKey(BusinessObjectInstanceKey paramBusinessObjectInstanceKey) {
		this.delinquencyProcessBusinessObjectInstKey = paramBusinessObjectInstanceKey;

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
	public BusinessObjectStatusReason_Id getStatusChangeReasonId() {
		return null;
	}

}

