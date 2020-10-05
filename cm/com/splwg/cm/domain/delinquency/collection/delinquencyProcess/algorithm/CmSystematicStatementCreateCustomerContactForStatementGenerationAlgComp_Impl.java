/*
 **************************************************************************************************************************
 * 
 * PROGRAM DESCRIPTION:
 * 
 * This algorithm creates separate customer contact for each Bill group of the customer which has its unpaid
 * past due balance over the defined threshold
 **************************************************************************************************************************
 * 
 * CHANGE HISTORY:
 * 
 * Date:       	by:          Reason:
 * 2020-05-06   MugdhaP		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework
 **************************************************************************************************************************
 */

package com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.Query;
import com.splwg.base.api.QueryResultRow;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.BusinessObjectStatusCode;
import com.splwg.base.api.lookup.BusinessObjectActionLookup;
import com.splwg.base.api.lookup.BusinessObjectStatusTransitionConditionLookup;
import com.splwg.base.api.lookup.LogEntryTypeLookup;
import com.splwg.base.api.maintenanceObject.MaintenanceObjectLogHelper;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectEnterStatusAlgorithmSpot;
import com.splwg.base.domain.common.businessObjectStatusReason.BusinessObjectStatusReason_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.maintenanceObject.MaintenanceObject;
import com.splwg.ccb.domain.admin.customerContactType.CustomerContactType_Id;
import com.splwg.ccb.domain.customerinfo.customerContact.CustomerContact;
import com.splwg.ccb.domain.customerinfo.customerContact.CustomerContactCharacteristic_DTO;
import com.splwg.ccb.domain.customerinfo.customerContact.CustomerContact_DTO;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.cm.api.lookup.CmDelinquencyRelatedObjTypeLookup;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessLog;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.cm.domain.delinquency.collection.delinquencyProcess.CmSystematicStatementDelinquencyDataObject;
import com.splwg.cm.domain.delinquency.collection.delinquencyProcess.CmSystematicStatementDelinquencyHelper;
import com.splwg.cm.domain.delinquency.collection.delinquencyProcess.CmSystematicStatementDelinquencyHelper_Impl;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;

/**
 * @author MugdhaP
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = characteristicType, name = billGroupIdCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = includeOnAccountPayments, type = lookup)
 *            , @AlgorithmSoftParameter (entityName = featureConfiguration, name = adminstrativeContractTypeFeatureConfig, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = customerManagementOption, name = adminstrativeContractTypeOptionType, type = lookup)
 *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = useCurrentRevenuePeriodBilled, required = true, type = lookup)
 *            , @AlgorithmSoftParameter (entityName = customerContactClass, name = customerContactClass, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = customerContactType, required = true, type = string)
 *            , @AlgorithmSoftParameter (entityName = accountNumberType, name = billGroupAccountIdentifierType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = unPaidAmountAndPercentageRequired, required = true, type = lookup)
 *            , @AlgorithmSoftParameter (name = processBillsWithUnpaidAmount, type = decimal)
 *            , @AlgorithmSoftParameter (name = processBillsWithUnpaidPercentage, type = decimal)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = customerContactIdCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = deliquencyProcessCharType, required = true, type = entity)})
 */
public class CmSystematicStatementCreateCustomerContactForStatementGenerationAlgComp_Impl extends CmSystematicStatementCreateCustomerContactForStatementGenerationAlgComp_Gen
		implements BusinessObjectEnterStatusAlgorithmSpot {

	private BusinessObjectInstanceKey businessObjectInstKey = null;
	private CmSystematicStatementDelinquencyHelper helper = CmSystematicStatementDelinquencyHelper_Impl.Factory
			.newInstance();

	@Override
	public void setBusinessObjectKey(BusinessObjectInstanceKey paramBusinessObjectInstanceKey) {
		this.businessObjectInstKey = paramBusinessObjectInstanceKey;
	}
	
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
		
		return null;
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
	protected void extraSoftParameterValidations(boolean forAlgorithmValidation) {

		// if on account payments is Y validates feature configuration and
		// option type
		if (notNull(getIncludeOnAccountPayments()) && getIncludeOnAccountPayments().isYes()) {
			if (isNull(getAdminstrativeContractTypeFeatureConfig()))
				reportRequiredParameter("adminstrativeContractTypeFeatureConfig", 2);
			if (isNull(getAdminstrativeContractTypeOptionType()))
				reportRequiredParameter("adminstrativeContractTypeOptionType", 3);
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
					reportRequiredParameter("processBillsWithUnpaidAmount", 9);
				}
				if (isNull(getProcessBillsWithUnpaidPercentage())) {
					reportRequiredParameter("processBillsWithUnpaidPercentage", 10);
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

		String  strBillGrpKey = "";
		HashMap<String, ArrayList<String>> hmBillGrpRelatedAccounts = null;
		boolean isCustomerContactStatus = false;

		// Fetch Delinquency process Id
		CmDelinquencyProcess_Id delinProcId = new CmDelinquencyProcess_Id(
				businessObjectInstKey.getString("delinquencyProcessId"));

		// Fetch Person Id
		Person_Id person_Id = fetchPersonIdFromDelinquencyProcess(delinProcId);

		if (notNull(person_Id)) {
			hmBillGrpRelatedAccounts = new HashMap<String, ArrayList<String>>();
			// Store the retrieved bill group Ids and list of accounts related
			// to it in Map
			hmBillGrpRelatedAccounts = helper.getBillGroupRelatedAccounts(person_Id,
					getBillGroupAccountIdentifierType().getId());
			Iterator<Map.Entry<String, ArrayList<String>>> it = hmBillGrpRelatedAccounts.entrySet().iterator();

			// Iterate the Map for each Bill Group
			while (it.hasNext()) {
				Map.Entry<String, ArrayList<String>> pair = it.next();

				strBillGrpKey = pair.getKey();
				ArrayList<String> listFinalKeyValue = pair.getValue();

				// Populate Systematic Statement delinquency data object
				CmSystematicStatementDelinquencyDataObject systematicStatementDelinquencyDO = new CmSystematicStatementDelinquencyDataObject();
				systematicStatementDelinquencyDO.setUnpaidAmtAndPrctRequired(getUnPaidAmountAndPercentageRequired());
				systematicStatementDelinquencyDO.setUnpaidAmount(getProcessBillsWithUnpaidAmount());
				systematicStatementDelinquencyDO.setUnpaidPercentage(getProcessBillsWithUnpaidPercentage());
				systematicStatementDelinquencyDO
						.setDelinquencyProcessType(delinProcId.getEntity().getCmDelinquencyProcessType());
				systematicStatementDelinquencyDO.setPersonId(person_Id);
				systematicStatementDelinquencyDO.setAccountNumberIdType(getBillGroupAccountIdentifierType());
				systematicStatementDelinquencyDO.setIncludeOnAccountPayments(getIncludeOnAccountPayments());
				systematicStatementDelinquencyDO
						.setAdminstrativeContractTypeFeatureConfig(getAdminstrativeContractTypeFeatureConfig());
				systematicStatementDelinquencyDO
						.setAdminstrativeContractTypeOptionType(getAdminstrativeContractTypeOptionType());
				systematicStatementDelinquencyDO.setUseCurrentRevenuePeriodBilled(getUseCurrentRevenuePeriodBilled());
				systematicStatementDelinquencyDO.setAccountIdList(listFinalKeyValue);

				// Invoke method to compute if Customer Contact Creation is
				// eligible
				isCustomerContactStatus = helper.isCustomerEligibleForDelinquency(systematicStatementDelinquencyDO);

				if (isCustomerContactStatus) {
					createCustomerContact(person_Id, delinProcId, getBillGroupIdCharType(),
							strBillGrpKey);
				}
			}
		}
	}

	/**
	 * Creates Customer Contact
	 * 
	 * @param perId
	 * @param delinquencyProcessId
	 * @param billGroupCharType
	 * @param billGroupCharValue
	 */
	private void createCustomerContact(Person_Id perId, CmDelinquencyProcess_Id delinquencyProcess_Id,
			CharacteristicType billGroupCharType, String billGroupCharValue) {

		CustomerContact customerContact = null;
		// Get Customer Contact Type
		CustomerContactType_Id customerCCTypeId = new CustomerContactType_Id(getCustomerContactClass(),
				getCustomerContactType());

		CustomerContact_DTO ccDto = createDTO(CustomerContact.class);
		ccDto.setPersonId(perId);
		ccDto.setCustomerContactTypeId(customerCCTypeId);
		ccDto.setContactDateTime(getProcessDateTime());
		ccDto.setUserId(getActiveContextUser().getId());
		customerContact = ccDto.newEntity();

		// If Customer Contact successfully created
		if (notNull(customerContact)) {
			// Stamp Bill Group ID as Characteristic on Customer
			// Contact Created
			if (notNull(billGroupCharType)) {
				createCustomerContactChar(customerContact, billGroupCharType, billGroupCharValue);
			}

			if (notNull(delinquencyProcess_Id)) {
				createCustomerContactChar(customerContact, getDeliquencyProcessCharType(), delinquencyProcess_Id.getIdValue());
			}

			
			// Create Delinquency Process Log Entry of customer contact creation
			MaintenanceObjectLogHelper<CmDelinquencyProcess, CmDelinquencyProcessLog> logHelper = new MaintenanceObjectLogHelper<CmDelinquencyProcess, CmDelinquencyProcessLog>(
					delinquencyProcess_Id.getEntity().getBusinessObject().getMaintenanceObject(),
					delinquencyProcess_Id.getEntity());
			logHelper.addLogEntry(LogEntryTypeLookup.constants.SYSTEM,
					MessageRepository.customerContactForSystematicStmtProcess(billGroupCharValue), "",
					getCustomerContactIdCharType(), customerContact);
		}
	}

	/**
	 * This method will create the Customer Contact Characteristics
	 * 
	 * @param newCC
	 * @param charType
	 * @param value
	 */
	private void createCustomerContactChar(CustomerContact newCC, CharacteristicType charType, String value) {

		CustomerContactCharacteristic_DTO ccCharDto = newCC.getCharacteristics().newChildDTO();

		if (charType.getCharacteristicType().isPredefinedValue()) {
			ccCharDto.setCharacteristicValue(value);
		} else if (charType.getCharacteristicType().isForeignKeyValue()) {
			ccCharDto.setCharacteristicValueForeignKey1(value);
		} else {
			ccCharDto.setAdhocCharacteristicValue(value);
		}
		newCC.getCharacteristics().add(ccCharDto, charType.getId());
	}
	
	/**
	 * This method fetches level and maintenance object for delinquencyProcessId
	 * 
	 * @param delinquencyProcessId
	 * @return Person_Id
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
}
