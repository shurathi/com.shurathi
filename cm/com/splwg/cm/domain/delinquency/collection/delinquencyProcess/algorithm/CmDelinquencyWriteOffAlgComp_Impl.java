/*                                                              
 **************************************************************************************************                                                               
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Manage Delinquency  Write Off Debt
 * 
 * This algorithm creates a write off request for each of the delinquent customers accounts 
 * to systematically write off the customers open balance
 **************************************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 * 2020-05-06   MugdhaP		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework      						 
 **************************************************************************************************
 */
package com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm;

import java.util.ArrayList;
import java.util.List;

import com.splwg.base.api.Query;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.BusinessObjectStatusCode;
import com.splwg.base.api.lookup.BusinessObjectStatusTransitionConditionLookup;
import com.splwg.base.api.lookup.LogEntryTypeLookup;
import com.splwg.base.api.maintenanceObject.MaintenanceObjectLogHelper;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectEnterStatusAlgorithmSpot;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatus;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatusTransitionRule;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatus_Id;
import com.splwg.base.domain.common.businessObjectStatusReason.BusinessObjectStatusReason_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicEntity_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.ccb.domain.billing.bill.Bill;
import com.splwg.ccb.domain.billing.bill.Bill_Id;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.financial.refundWORequest.RefundWORequest;
import com.splwg.ccb.domain.financial.refundWORequest.RefundWORequestLog;
import com.splwg.cm.api.lookup.CharacteristicEntityLookup;
import com.splwg.cm.domain.delinquency.admin.customerClass.CmCashPostingToleranceWriteOffConstants;
import com.splwg.cm.domain.delinquency.admin.customerClass.CmProcessWriteOffRequestHelper;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessLog;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;

/**
 * @author MugdhaP
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = refundWORequestType, name = writeOffRequestType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = writeOffRequestCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = delinquencyProcCharType, required = true, type = entity)})
 */
public class CmDelinquencyWriteOffAlgComp_Impl extends CmDelinquencyWriteOffAlgComp_Gen implements BusinessObjectEnterStatusAlgorithmSpot {
	
	@SuppressWarnings("unused")
	private BusinessObject businessObject;
	private BusinessObjectInstanceKey businessObjectInstKey;
	private BusinessObjectInstance boInstance;
	private CmDelinquencyProcess_Id delinquencyProcess_Id;

	@Override
	protected void extraSoftParameterValidations(boolean forAlgorithmValidation) {

		// Check that parameter Write Off Request Characteristic Type is a valid
		// Characteristic Type for the Delinquency Process Log entity
		validateCharTypeForEntity(getWriteOffRequestCharType(), CharacteristicEntityLookup.constants.CM_DELINQUENCY_PROCESS_LOG);

		// Check that parameter Delinquency Process Characteristic Type is a
		// valid Characteristic Type for the Refund Write Off Request entity
		validateCharTypeForEntity(getDelinquencyProcCharType(), CharacteristicEntityLookup.constants.REFUND_W_O_REQUEST);

	}

	@Override
	public void invoke() {
		boInstance = null;
		delinquencyProcess_Id = null;
		List<Account_Id> accountIdList = null;
		
		// Read the Delinquency Process object using the hard parameter business
		// object
		boInstance = BusinessObjectDispatcher.read(businessObjectInstKey, false);
		delinquencyProcess_Id = new CmDelinquencyProcess_Id(boInstance.getString(CmDetermineTerminationDateRuleConstants.DELINQUENCY_PROCESS_ID));

		// Validate Delinquency Process Id
		if (isNull(delinquencyProcess_Id.getEntity())) {

			addError(MessageRepository.delinquencyProcessRequired());
		}
		CmDelinquencyProcess delinquencyProcess = delinquencyProcess_Id.getEntity();

		// Retrieve the distinct accounts from the Delinquency Process related bills
		accountIdList = retAcctListForDelBills();

		if (notNull(accountIdList) && !(accountIdList.isEmpty())) {

			for (Account_Id acctId : accountIdList) {
				// For each account create Write Off Request
				RefundWORequest refundWriteOffRequest = createWriteOffRequest(acctId);

				if (notNull(refundWriteOffRequest)) {
					// Add Write Off Request Log
					MaintenanceObjectLogHelper<RefundWORequest, RefundWORequestLog> writeOffLogHelper = new MaintenanceObjectLogHelper<RefundWORequest,
							RefundWORequestLog>(refundWriteOffRequest.getBusinessObject().getMaintenanceObject(), refundWriteOffRequest);
					writeOffLogHelper.addLogEntry(LogEntryTypeLookup.constants.SYSTEM, MessageRepository.delWriteOffInitiated(), null, getDelinquencyProcCharType(), delinquencyProcess);

					// Transition Write Off Request to Next Default Status
					transitionWriteOffRequest(refundWriteOffRequest);

					// Add Delinquency Process Log
					MaintenanceObjectLogHelper<CmDelinquencyProcess, CmDelinquencyProcessLog> delProcLogHelper = new MaintenanceObjectLogHelper<CmDelinquencyProcess,
							CmDelinquencyProcessLog>(delinquencyProcess.getBusinessObject().getMaintenanceObject(), delinquencyProcess);
					delProcLogHelper.addLogEntry(LogEntryTypeLookup.constants.SYSTEM, MessageRepository.delWriteOffCreated(), null, getWriteOffRequestCharType(), refundWriteOffRequest);

				}

			}

		}

	}

	/**
	 * Create Write Off Request 
	 * 
	 * @param accountId
	 * @return refundWORequest
	 */
	private RefundWORequest createWriteOffRequest(Account_Id accountId) {
		RefundWORequest refundWORequest = null;
		List<Bill_Id> billIdList = null;
		List<Bill> billList = new ArrayList<Bill>();
		List<SQLResultRow> result = null;

		// Retrieve all of accounts bill related to the Delinquency Process
		billIdList = retAcctBillFromDelProc(accountId);

		if (notNull(billIdList) && !(billIdList.isEmpty())) {

			for (Bill_Id billId : billIdList) {
				// Retrieve Open FTs from Bill
				PreparedStatement preparedStatement = createPreparedStatement(CmCashPostingToleranceWriteOffConstants.RETRIEVE_OPEN_FT_FROM_BILL.toString(), "CmDelinquencyWriteOffAlgComp_Impl");
				preparedStatement.bindId("billID", billId);
				result = preparedStatement.list();
				if (notNull(result) && (result.size() > 0)) {

					// If bill has an open ft add it to billList
					billList.add(billId.getEntity());
				}
			}
			// create write off for open ft bills
			if (notNull(billList) && (billList.size() > 0)) {

				// Create a Write Off Request
				CmProcessWriteOffRequestHelper cmProcessWriteOffRequestHelper = CmProcessWriteOffRequestHelper.Factory.newInstance();
				refundWORequest = cmProcessWriteOffRequestHelper.createWriteOffRequest(getWriteOffRequestType().getBusinessObject(), getWriteOffRequestType(), accountId.getEntity(), billList);
			}	
		}

		return refundWORequest;
	}

	/**
	 * Transition Write Off Request to Next Default Status passing refundWORequest.
	 * 
	 * @param refundWORequest
	 * 
	 */
	private void transitionWriteOffRequest(RefundWORequest refundWORequest) {
		
		//BusinessObjectStatus defaultStatus = getBONextStatus(refundWORequest.getBusinessObject());
		BusinessObjectStatus defaultStatus = getBONextStatus(refundWORequest.getBusinessObject(), refundWORequest.getStatus());

		// Create business object instance for refundWORequest
		BusinessObjectInstance businessObjectInstance = BusinessObjectInstance.create(refundWORequest.getBusinessObject());
		businessObjectInstance.set(CmDelinquencyProcessConstant.REQUEST, refundWORequest.getId().getIdValue());
		businessObjectInstance = BusinessObjectDispatcher.read(businessObjectInstance);

		if (notNull(defaultStatus)) {

			// Set refundWORequests status to defaultStatus
			businessObjectInstance.set(CmDelinquencyProcessConstant.BUSINESS_OBJ_STATUS, defaultStatus.fetchIdStatus());
			BusinessObjectDispatcher.update(businessObjectInstance);
		}

	}

	/**
	 * Gets the business object next status
	 * 
	 * @param writeOffRequestBO
	 * @param status 
	 * @return BusinessObjectDefaultNextStatus
	 */
	
	//private BusinessObjectStatus getBONextStatus(BusinessObject writeOffRequestBO) {
	private BusinessObjectStatus getBONextStatus(BusinessObject writeOffRequestBO, String status) {	
	
		
		BusinessObjectStatus boNextStatus = null;
		
		//Query<BusinessObjectStatusTransitionRule> query = createQuery(" from BusinessObjectStatusTransitionRule rule where rule.id.businessObjectStatus.id.businessObject =:businessObject and rule.shouldUseAsDefault='Y'  ", "CmDelinquencyWriteOffAlgComp_Impl".concat(".getBONextStatus"));
		//query.bindEntity("businessObject", writeOffRequestBO);
		
		Query<BusinessObjectStatusTransitionRule> query = createQuery(" from BusinessObjectStatusTransitionRule rule where "
				.concat(" rule.id.businessObjectStatus.id = :businessObjectStatus ")
				.concat(" and rule.shouldUseAsDefault = 'Y'  "), "getBONextStatus");

		BusinessObjectStatus_Id bostatusId = new BusinessObjectStatus_Id(writeOffRequestBO, status);
		query.bindId("businessObjectStatus", bostatusId);
		
		BusinessObjectStatusTransitionRule businessObjectStatusTransitionRule = query.firstRow();

		if (notNull(businessObjectStatusTransitionRule)) {

			boNextStatus = businessObjectStatusTransitionRule.fetchNextStatus();
		}
		return boNextStatus;
	}

	/**
	 * Retrieve the distinct accounts from the Delinquency Process related bills
	 * 
	 * @param 
	 * @return accountIdList
	 */
	private List<Account_Id> retAcctListForDelBills() {
		Query<Account_Id> query = createQuery(CmDelinquencyProcessConstant.FETCH_ACCT_FROM_DEL_PROC_BILLS.toString(), "CmDelinquencyWriteOffAlgComp_Impl");
		query.bindId("delinquencyProcessId", delinquencyProcess_Id);
		query.addResult("accountId", "B.account.id");
		query.selectDistinct(true);
		List<Account_Id> resultList = query.list();
		
		return resultList;
	}

	/**
	 * Retrieve all of accounts bill related to the Delinquency Process
	 * 
	 * @param accountId
	 * @return billIdList
	 */
	private List<Bill_Id> retAcctBillFromDelProc(Account_Id accountId) {
		Query<Bill_Id> query = createQuery(CmDelinquencyProcessConstant.FETCH_ACCT_BILL_FROM_DEL_PROC.toString(), "CmDelinquencyWriteOffAlgComp_Impl");
		query.bindId("delinquencyProcessId", delinquencyProcess_Id);
		query.bindId("accountId", accountId);
		query.addResult("billId", "B.id");
		List<Bill_Id> resultList = query.list();
		
		return resultList;
	}

	/**
	 * Validates that the Characteristic Type is valid for the given entity
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

	@Override
	public void setBusinessObject(BusinessObject paramBusinessObject) {
		this.businessObject = paramBusinessObject;

	}

	@Override
	public void setBusinessObjectKey(BusinessObjectInstanceKey paramBusinessObjectInstanceKey) {
		this.businessObjectInstKey = paramBusinessObjectInstanceKey;

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

