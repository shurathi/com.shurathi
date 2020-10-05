/*
 **************************************************************************************************************************
 * 
 * PROGRAM DESCRIPTION:
 * 
 * This algorithm creates separate customer contact for each Bill group of the customer which has its unpaid
 * past due balance over the defined threshold. 
 **************************************************************************************************************************
 **************************************************************************************************************************
 * 
 * CHANGE HISTORY:
 * 
 * Date:       	by:          Reason:
 * 2020-05-06   MugdhaP		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework
 **************************************************************************************************************************
 */

package com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.Query;
import com.splwg.base.api.QueryResultRow;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.BusinessObjectStatusCode;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.lookup.BusinessObjectActionLookup;
import com.splwg.base.api.lookup.BusinessObjectStatusTransitionConditionLookup;
import com.splwg.base.api.lookup.LogEntryTypeLookup;
import com.splwg.base.api.lookup.OutboundMessageProcessingMethodLookup;
import com.splwg.base.api.maintenanceObject.MaintenanceObjectLogHelper;
import com.splwg.base.domain.batch.batchControl.BatchControl;
import com.splwg.base.domain.batch.batchControl.BatchControl_Id;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectEnterStatusAlgorithmSpot;
import com.splwg.base.domain.common.businessObjectStatusReason.BusinessObjectStatusReason_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.maintenanceObject.MaintenanceObject;
import com.splwg.base.domain.outboundMessage.outboundMessage.OutboundMessage;
import com.splwg.base.domain.outboundMessage.outboundMessage.OutboundMessage_Id;
import com.splwg.base.domain.outboundMessage.outboundMessageType.OutboundMessageType;
import com.splwg.base.domain.workflow.notificationExternalId.OutboundMessageProfile;
import com.splwg.ccb.domain.billing.bill.Bill_Id;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.ccb.domain.insurance.policy.Policy_Id;
import com.splwg.ccb.domain.outboundMessage.outboundMessage.OutboundMessageCharacteristic;
import com.splwg.ccb.domain.outboundMessage.outboundMessage.OutboundMessageCharacteristic_DTO;
import com.splwg.ccb.domain.outboundMessage.outboundMessage.OutboundMessageCharacteristic_Id;
import com.splwg.cm.api.lookup.CmDelinquencyRelatedObjTypeLookup;
import com.splwg.cm.domain.delinquency.admin.overdueEventType.CmCreatePreTermNotificationConstants;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessLog;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.cm.domain.delinquency.collection.delinquencyProcess.CmSystematicStatementDelinquencyDataObject;
import com.splwg.cm.domain.delinquency.collection.delinquencyProcess.CmSystematicStatementDelinquencyHelper;
import com.splwg.cm.domain.delinquency.collection.delinquencyProcess.CmSystematicStatementDelinquencyHelper_Impl;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;
//import com.splwg.cm.domain.outboundMessage.outboundMessage.CmOutboundMessageCharacteristic;
//import com.splwg.cm.domain.outboundMessage.outboundMessage.CmOutboundMessageCharacteristic_DTO;
//import com.splwg.cm.domain.outboundMessage.outboundMessage.CmOutboundMessageCharacteristic_Id;
import com.splwg.shared.common.ServerMessage;

/**
 * @author MugdhaP
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = characteristicType, name = billGroupIdCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = includeOnAccountPayments, type = lookup)
 *            , @AlgorithmSoftParameter (entityName = featureConfiguration, name = adminstrativeContractTypeFeatureConfig, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = customerManagementOption, name = adminstrativeContractTypeOptionType, type = lookup)
 *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = useCurrentRevenuePeriodBilled, required = true, type = lookup)
 *            , @AlgorithmSoftParameter (entityName = outboundMessageType, name = preTermNotificationOutboundMsgType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = notificationExternalId, name = externalSystem, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = idType, name = externalSourceCustomerIdType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = outMessageIdCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = accountNumberType, name = billGroupAccountIdentifierType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = unPaidAmountAndPercentageRequired, required = true, type = lookup)
 *            , @AlgorithmSoftParameter (name = processBillsWithUnpaidAmount, type = decimal)
 *            , @AlgorithmSoftParameter (name = processBillsWithUnpaidPercentage, type = decimal)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = deliquencyProcessCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = billIdListCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = cancelledBusinessObjectStatus, required = true, type = string)
 *            , @AlgorithmSoftParameter (entityName = businessObjectStatusReason, name = cancelledBoStatusReason, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = brokerIdCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = outboundMessageType, name = terminationNotificationOutboundMsgType, required = true, type = entity)})
 */


public class CmSystematicStatementCreateOutboundMessageForBrokerNotificationAlgComp_Impl extends CmSystematicStatementCreateOutboundMessageForBrokerNotificationAlgComp_Gen
		implements BusinessObjectEnterStatusAlgorithmSpot {

	private BusinessObjectInstanceKey businessObjectInstKey = null;
	private BusinessObjectInstance boInstance = null;
	private BusinessObjectStatusCode nextBoStatus;
	private BusinessObjectStatusReason_Id nextBoStatusReasonId;
	private boolean defaultNextStatus;
	private CmSystematicStatementDelinquencyHelper helper = CmSystematicStatementDelinquencyHelper_Impl.Factory
			.newInstance();

	@Override
	public boolean getForcePostProcessing() {
		return false;
	}

	@Override
	public BusinessObjectStatusTransitionConditionLookup getNextStatusCondition() {
		return null;
	}

	@Override
	public void setAction(BusinessObjectActionLookup arg0) {

	}

	@Override
	public void setBusinessObject(BusinessObject arg0) {

	}

	@Override
	public void setBusinessObjectKey(BusinessObjectInstanceKey paramBusinessObjectInstanceKey) {
		this.businessObjectInstKey = paramBusinessObjectInstanceKey;

	}

	@Override
	public BusinessObjectStatusCode getNextStatus() {
		return this.nextBoStatus;
	}

	@Override
	public BusinessObjectStatusReason_Id getStatusChangeReasonId() {
		return this.nextBoStatusReasonId;
	}

	@Override
	public boolean getUseDefaultNextStatus() {
		return this.defaultNextStatus;
	}

	@Override
	protected void extraSoftParameterValidations(boolean forAlgorithmValidation) {

		// if on account payments is Y validates feature configuration and
		// option type
		if (notNull(getIncludeOnAccountPayments()) && getIncludeOnAccountPayments().isYes()) {
			if (isNull(getAdminstrativeContractTypeFeatureConfig())){
				reportRequiredParameter("adminstrativeContractTypeFeatureConfig", 2);
			}
			if (isNull(getAdminstrativeContractTypeOptionType())){
				reportRequiredParameter("adminstrativeContractTypeOptionType", 3);
			}
		}

		// if unpaid Amount and Percentage flag is Y validates the presence of
		// the Unpaid Amount and Unpaid Percentage thresholds
		if (notNull(getUnPaidAmountAndPercentageRequired())) {

			if (notNull(getProcessBillsWithUnpaidPercentage())) {
				if (getProcessBillsWithUnpaidPercentage().compareTo(BigDecimal.ZERO) < 0
						|| getProcessBillsWithUnpaidPercentage().compareTo(new BigDecimal("100")) > 0) {
					addError(MessageRepository.invalidAlgorithmParameterValue(this.getAlgorithm(),
							getAlgorithm().getAlgorithmType().getParameterAt(10).fetchLanguageParameterLabel(),
							getProcessBillsWithUnpaidPercentage().toString()));
				}
			}

			if (getUnPaidAmountAndPercentageRequired().isYes()) {

				if (isNull(getProcessBillsWithUnpaidAmount())) {
					reportRequiredParameter("processBillsWithUnpaidAmount", 11);
				}
				if (isNull(getProcessBillsWithUnpaidPercentage())) {
					reportRequiredParameter("processBillsWithUnpaidPercentage", 12);
				}
			}
			// if unpaid Amount and Percentage flag is N validates the presence
			// of either the Unpaid Amount or the Unpaid Percentage thresholds
			else if (getUnPaidAmountAndPercentageRequired().isNo()) {

				if (isNull(getProcessBillsWithUnpaidAmount()) && isNull(getProcessBillsWithUnpaidPercentage())) {

					addError(MessageRepository.unpaidAmountAndPercentageMissing());
				}
			}
		}
	}

	@Override
	public void invoke() {
		
		defaultNextStatus = false;
		// Fetch Delinquency process Id
		CmDelinquencyProcess_Id delinquencyProcess_Id = new CmDelinquencyProcess_Id(
				businessObjectInstKey.getString("delinquencyProcessId"));

		// Fetch Person Id
		Person_Id person_Id = fetchPersonIdFromDelinquencyProcess(delinquencyProcess_Id);

		if (notNull(person_Id)) {
			boolean outboundMsgStatusFlag = false;
			String strPersonNbr = "";
			// Compute Person Number based on External Source Person identifier
			// Soft parameter
			
			if (notNull(person_Id)) {
				strPersonNbr = fetchPersonNbr(person_Id);
			}
			String strBrokerId = "";
			if (notBlank(strPersonNbr)) {
				// Fetch Broker Id for the Customer take first row
				strBrokerId = helper.retrieveBrokerId(strPersonNbr);
			}

			// If Broker Id Found
			if (notBlank(strBrokerId)) {

				// Store the retrieved bill group Ids and list of accounts
				// related to it in Map
				HashMap<String, ArrayList<String>> hmBillGrpRelatedAccounts = helper.getBillGroupRelatedAccounts(person_Id,
						getBillGroupAccountIdentifierType().getId());
				Iterator<Map.Entry<String, ArrayList<String>>> it = hmBillGrpRelatedAccounts.entrySet().iterator();

				String strBillGrpKey = "";
				// Iterate the Map for each Bill Group
				while (it.hasNext()) {

					Map.Entry<String, ArrayList<String>> pair = it.next();

					strBillGrpKey = pair.getKey();
					ArrayList<String> listFinalKeyValue = pair.getValue();

					// Populate Systematic Statement delinquency data object
					CmSystematicStatementDelinquencyDataObject systematicStatementDelinquencyDO = new CmSystematicStatementDelinquencyDataObject();
					systematicStatementDelinquencyDO
							.setUnpaidAmtAndPrctRequired(getUnPaidAmountAndPercentageRequired());
					systematicStatementDelinquencyDO.setUnpaidAmount(getProcessBillsWithUnpaidAmount());
					systematicStatementDelinquencyDO.setUnpaidPercentage(getProcessBillsWithUnpaidPercentage());
					systematicStatementDelinquencyDO
							.setDelinquencyProcessType(delinquencyProcess_Id.getEntity().getCmDelinquencyProcessType());
					systematicStatementDelinquencyDO.setPersonId(person_Id);
					systematicStatementDelinquencyDO.setAccountNumberIdType(getBillGroupAccountIdentifierType());
					systematicStatementDelinquencyDO.setIncludeOnAccountPayments(getIncludeOnAccountPayments());
					systematicStatementDelinquencyDO
							.setAdminstrativeContractTypeFeatureConfig(getAdminstrativeContractTypeFeatureConfig());
					systematicStatementDelinquencyDO
							.setAdminstrativeContractTypeOptionType(getAdminstrativeContractTypeOptionType());
					systematicStatementDelinquencyDO
							.setUseCurrentRevenuePeriodBilled(getUseCurrentRevenuePeriodBilled());
					systematicStatementDelinquencyDO.setAccountIdList(listFinalKeyValue);

					// Invoke method to compute if Customer Contact Creation is
					// eligible
					boolean isEligibleForDelinquency = helper
							.isCustomerEligibleForDelinquency(systematicStatementDelinquencyDO);

					List<QueryResultRow> overdueBillRecordList = helper.retrieveBillsForBillGroup(listFinalKeyValue,
							getProcessDateTime().getDate());

					if (isEligibleForDelinquency) {
						// Create Outbound Message
						// Bug 10819 Change Start
						//createOutboundMessage(person_Id, strBrokerId, overdueBillRecordList,
						//		strBillGrpKey, delinquencyProcess_Id, strPersonNbr);
						//outboundMsgStatusFlag = true;
						outboundMsgStatusFlag = createOutboundMessage(person_Id, strBrokerId, overdueBillRecordList,
								strBillGrpKey, delinquencyProcess_Id, strPersonNbr);
					}
				}
			} else {
				outboundMsgStatusFlag = true;

				if (notBlank(strPersonNbr)) {

					// Create Delinquency Process Log Entry for Broker Not Found
					ServerMessage message = MessageRepository.brokerIdNotFound(strPersonNbr);
					MaintenanceObjectLogHelper<CmDelinquencyProcess, CmDelinquencyProcessLog> logHelper = new MaintenanceObjectLogHelper<CmDelinquencyProcess, CmDelinquencyProcessLog>(
							delinquencyProcess_Id.getEntity().getBusinessObject().getMaintenanceObject(),
							delinquencyProcess_Id.getEntity());
					logHelper.addLogEntry(LogEntryTypeLookup.constants.SYSTEM, message, "");
				}

			}

			// Update Delinquency Process Status
			updateDelinquencyProcessStatus(outboundMsgStatusFlag);
		}
	}

	/**
	 * Creates an Outbound Message and an Outbound Message Characteristic.
	 * 
	 * @param outboundMessageType
	 * @param externalSystem
	 * @param batchControl
	 * @param accountId
	 * @param action
	 * @param charType
	 */
	//public void createOutboundMessage(Person_Id personId, String strBrokerId, List<QueryResultRow> lstBillData,
	//		String strBillGrpKey, CmDelinquencyProcess_Id delinProcId, String strSourceSystemCustomerNumber) {
	public boolean createOutboundMessage(Person_Id personId, String strBrokerId, List<QueryResultRow> lstBillData,
			String strBillGrpKey, CmDelinquencyProcess_Id delinProcId, String strSourceSystemCustomerNumber) {

		//OutboundMessageType outboundMessageType = null;

		// Create Outbound Message
		//if (notNull(getOutboundMsgType())) {
		//	outboundMessageType = getOutboundMsgType();
		// Retrieve Outbound Message BO
		//	BusinessObjectInstance boInstance = BusinessObjectInstance.create(outboundMessageType.getBusinessObject());
		boolean isCreateOutboundMessage = false;


		StringBuilder strBillIdList = new StringBuilder();
		StringBuilder strCharBillIdList = new StringBuilder();
		// Compute Comma separated Bill Id list
		if (lstBillData.size() > 0) {
			for (QueryResultRow overdueBillRecord : lstBillData) {
				strBillIdList.append("'");
				strBillIdList.append(((Bill_Id) overdueBillRecord.get("billId")).getIdValue());
				strBillIdList.append("',");

				strCharBillIdList.append(((Bill_Id) overdueBillRecord.get("billId")).getIdValue());
				strCharBillIdList.append(",");
			}
		}

		// logical and physical adjustment to remove trailing comma
		if (strBillIdList.length() > 0) {
			strBillIdList.setLength(strBillIdList.length() - 1);
		}

		// logical and physical adjustment to remove trailing comma
		if (strCharBillIdList.length() > 0) {
			strCharBillIdList.setLength(strCharBillIdList.length() - 1);
		}
		String strPolicyId = "";
		Date dtTerminationDate = null;
		// Fetch Policy Id
		strPolicyId = helper.retrievePolicyId(strBillIdList.toString());
		if (notBlank(strPolicyId)) {
			//Policy_Id pId = new Policy_Id(strPolicyId);
			Policy_Id policyId = new Policy_Id(strPolicyId);
			OutboundMessageType outboundMessageType = null;
			if (notNull(policyId.getEntity())) {
				// if (pId.getEntity().getStatus().trim().equals("TERMINATED")) {
				//	dtTerminationDate = pId.getEntity().getEndDate();
				if (policyId.getEntity().getStatus().trim().equals(CmDelinquencyProcessConstant.TERMINATED)) {
					dtTerminationDate = policyId.getEntity().getEndDate();

					outboundMessageType = getTerminationNotificationOutboundMsgType();

				} else {
					dtTerminationDate = null;

					outboundMessageType = getPreTermNotificationOutboundMsgType();

				}

			}
			// Check Outbound Message
			if (notNull(outboundMessageType)) {
				
				// Retrieve Outbound Message BO
				BusinessObjectInstance boInstance = BusinessObjectInstance
						.create(outboundMessageType.getBusinessObject());

				OutboundMessageProfile outMsgProfile = null;
				outMsgProfile = getExternalSystem().getProfileForType(outboundMessageType);
				BatchControl_Id batchCtrlId = outMsgProfile.getBatchControlId();
				BatchControl batchControl = batchCtrlId.getEntity();

				boInstance.set(CmCreatePreTermNotificationConstants.OUT_BOUND_MSG_TYPE,
						outboundMessageType.getId().getIdValue());
				boInstance.set(CmCreatePreTermNotificationConstants.NOTIFICATION_EXTERNAL_ID,
						getExternalSystem().getId().getIdValue());
				boInstance.set(CmCreatePreTermNotificationConstants.PROCESSING_METHOD,
						OutboundMessageProcessingMethodLookup.constants.BATCH);
				boInstance.set(CmCreatePreTermNotificationConstants.CREATE_DATE, getProcessDateTime());
				boInstance.set(CmCreatePreTermNotificationConstants.BATCH_CNTRL, batchControl.getId().getIdValue());
				boInstance.set(CmCreatePreTermNotificationConstants.BATCH_NUMBER,
						BigDecimal.valueOf(batchControl.getNextBatchNumber().longValue()));
				// Set XML Source
				boInstance.getGroup(CmCreatePreTermNotificationConstants.XML_SOURCE)
						.set(CmCreatePreTermNotificationConstants.DELINQUENCY_PROCESS_ID, delinProcId.getIdValue());
				boInstance.getGroup(CmCreatePreTermNotificationConstants.XML_SOURCE)
						.set(CmCreatePreTermNotificationConstants.CUSTOMER_ID, strSourceSystemCustomerNumber);

				boInstance.getGroup(CmCreatePreTermNotificationConstants.XML_SOURCE).set(
						CmCreatePreTermNotificationConstants.CUSTOMER_ID_TYPE,
						getExternalSourceCustomerIdType().getId().getIdValue());

				//boInstance.getGroup(CmCreatePreTermNotificationConstants.XML_SOURCE)
				//		.set(CmCreatePreTermNotificationConstants.POLICY_ID, pId.getIdValue());
				boInstance.getGroup(CmCreatePreTermNotificationConstants.XML_SOURCE)
						.set(CmCreatePreTermNotificationConstants.POLICY_ID, policyId.getIdValue());

				boInstance.getGroup(CmCreatePreTermNotificationConstants.XML_SOURCE)
						.set(CmCreatePreTermNotificationConstants.BROKER_ID, strBrokerId);
				if (notNull(dtTerminationDate)) {
					boInstance.getGroup(CmCreatePreTermNotificationConstants.XML_SOURCE)
							.set(CmCreatePreTermNotificationConstants.TERMINATE_DATE, dtTerminationDate);
				}

				boInstance = BusinessObjectDispatcher.add(boInstance);

				OutboundMessage outboundMessage = new OutboundMessage_Id(
						boInstance.getString(CmCreatePreTermNotificationConstants.OUT_BOUND_MSG_ID)).getEntity();

				// Create Outbound Message Characteristics
				if (notNull(outboundMessage)) {
					// Delinquency process Id Char
					createOutboundMessageChar(outboundMessage, getDeliquencyProcessCharType(),
							delinProcId.getTrimmedValue(), getProcessDateTime().getDate());

					// Bill group Id Char
					createOutboundMessageChar(outboundMessage, getBillGroupIdCharType(), strBillGrpKey,
							getProcessDateTime().getDate());

					// Bill Id List Char
					createOutboundMessageChar(outboundMessage, getBillIdListCharType(), strCharBillIdList.toString(),
							getProcessDateTime().getDate());

					// Broker Id Char
					createOutboundMessageChar(outboundMessage, getBrokerIdCharType(), strBrokerId,
							getProcessDateTime().getDate());
				}

				// Add log to delinquency process
				MaintenanceObjectLogHelper<CmDelinquencyProcess, CmDelinquencyProcessLog> delProcLogHelper = new MaintenanceObjectLogHelper<CmDelinquencyProcess, CmDelinquencyProcessLog>(
						delinProcId.getEntity().getBusinessObject().getMaintenanceObject(), delinProcId.getEntity());
				delProcLogHelper.addLogEntry(LogEntryTypeLookup.constants.SYSTEM,
						MessageRepository.outboundMessageForSystematicStmtProcess(strBrokerId), "",
						getOutMessageIdCharType(), outboundMessage);

				isCreateOutboundMessage = true;

			}
		}

		return isCreateOutboundMessage;

	}

	/**
	 * @param outBoundMsg
	 * @param characteristicType
	 * @param charValue
	 * @param effectiveDate
	 */
	private void createOutboundMessageChar(OutboundMessage outBoundMsg, CharacteristicType characteristicType,
			String charValue, Date effectiveDate) {

		OutboundMessageCharacteristic_DTO outMsgCharDto = createDTO(OutboundMessageCharacteristic.class);
		OutboundMessageCharacteristic_Id outMsgCharId = new OutboundMessageCharacteristic_Id(outBoundMsg,
				characteristicType, effectiveDate);
		if (characteristicType.getCharacteristicType().isAdhocValue()) {
			outMsgCharDto.setAdhocCharacteristicValue(charValue);
		} else if (characteristicType.getCharacteristicType().isPredefinedValue()) {
			outMsgCharDto.setCharacteristicValue(charValue);
		} else if (characteristicType.getCharacteristicType().isForeignKeyValue()) {
			outMsgCharDto.setCharacteristicValueForeignKey1(charValue);
		}
		outMsgCharDto.setId(outMsgCharId);
		outMsgCharDto.newEntity();
	}

	/**
	 * @param delinquencyBoId
	 * @param outboundMsgStatusFlag
	 */
	private void updateDelinquencyProcessStatus(boolean outboundMsgStatusFlag) {

		if (outboundMsgStatusFlag) {
			this.nextBoStatus = null;
			this.nextBoStatusReasonId = null;
			this.defaultNextStatus = true;
		} else {
			this.nextBoStatus = new BusinessObjectStatusCode(this.boInstance.getBusinessObject().getId(),
					getCancelledBusinessObjectStatus());
			this.nextBoStatusReasonId = getCancelledBoStatusReason().getId();
			this.defaultNextStatus = false;
		}

	}
	
	/**
	 * This method fetches level and maintenance object for delinquencyProcessId
	 * 
	 * @param delinquencyProcessId
	 * @return QueryResultRow resultRow
	 */
	private Person_Id fetchPersonIdFromDelinquencyProcess(CmDelinquencyProcess_Id delinquencyProcessId) {

		Person_Id personId = null;
		Query<QueryResultRow> query = createQuery(
				CmSystematicStatementDelinquencyHelper_Impl.FETCH_DELIQ_LVL_ID.toString(),
				"fetchPersonIdFromDelinquencyProcess");
		query.bindId("delinProcId", delinquencyProcessId);
		query.bindLookup("relatedObjTypeFlag", CmDelinquencyRelatedObjTypeLookup.constants.CM_DELINQUENCY_LEVEL);
		query.addResult("maintenanceObject", "dpo.id.maintenanceObject");
		query.addResult("primaryKeyValue1", "dpo.id.primaryKeyValue1");

		if (notNull(query.firstRow())) {
			QueryResultRow resultRow = query.firstRow();

			if (notNull(resultRow.getEntity("maintenanceObject", MaintenanceObject.class))) {
				MaintenanceObject maintenanceObject = resultRow.getEntity("maintenanceObject", MaintenanceObject.class);

				if (maintenanceObject.getId().getTrimmedValue()
						.equalsIgnoreCase(CmSystematicStatementDelinquencyHelper_Impl.MAINTENANCE_OBJ_PERSON)) {
					if (notNull(resultRow.getString("primaryKeyValue1"))) {
						personId = new Person_Id(resultRow.getString("primaryKeyValue1"));
					}
				}
			}
		}

		return personId;

	}
	
	/**
	 * This method fetches person Number from Person id and Identifier Type
	 * 
	 * @param Person_Id per_Id
	 * @return String
	 */
	private String fetchPersonNbr(Person_Id per_Id) {
		
		String personNbr = "";
		Query<String> query = createQuery(CmSystematicStatementDelinquencyHelper_Impl.FETCH_PERNBR_FROM_ID_TYPE.toString(), "CmBrokerNotificationExtractBatch");
		query.bindId("idTypeCd", getExternalSourceCustomerIdType().getId());
		query.bindId("personId", per_Id);
		query.addResult("personIdNumber", "pid.personIdNumber");

		if(notNull( query.firstRow())){
			personNbr = query.firstRow();
		}

		return personNbr;
	}
}
