/*                                                                
 ************************************************************************************************************                                                               
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Calculate Unpaid & Original Amount for a Bill Algorithm
 *                                                             
 ************************************************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 *           
 * 2020-05-06   MugdhaP		Initial Version : ANTHM-340 CAB1-9462 Delinquency Framework
 * **********************************************************************************************************
 */
package com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.algorithm;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.Query;
import com.splwg.base.api.QueryResultRow;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.domain.common.maintenanceObject.MaintenanceObject;
import com.splwg.ccb.domain.billing.bill.Bill_Id;
import com.splwg.ccb.domain.billing.bill.OpenItemBillAmountResults;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.cm.api.lookup.CmDelinquencyRelatedObjTypeLookup;
import com.splwg.cm.domain.delinquency.admin.collectionClassOverdueRules.CmDetermineOpenBillItemAmounts;
import com.splwg.cm.domain.delinquency.admin.delinquencyCustomerMonitor.CmDelinquencyCustomerMonitorRuleConstants;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmCalculateUnpaidOriginalAmountAlgorithmSpot;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessRelatedObject;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;

/**
 * @author MugdhaP
 *
@AlgorithmComponent ()
 */
public class CmCalculateUnpaidOriginalAmountAlgComp_Impl extends CmCalculateUnpaidOriginalAmountAlgComp_Gen implements CmCalculateUnpaidOriginalAmountAlgorithmSpot {

	private CmDelinquencyProcessRelatedObject cmDelinquencyProcessRelatedObject;
	private CmDelinquencyProcess_Id cmDelinquencyProcessId;
	private BigDecimal billUnpaidAmount;
	private BigDecimal billOriginalAmount;
	private Bool indeterminateSwitch;

	@Override
	public void setDelinquencyProcessId(CmDelinquencyProcess_Id delProcId) {
		this.cmDelinquencyProcessId = delProcId;

	}

	@Override
	public void setDelinquencyProcessRelatedObject(CmDelinquencyProcessRelatedObject delProcRelObject) {
		this.cmDelinquencyProcessRelatedObject = delProcRelObject;
	}

	@Override
	public BigDecimal getUnpaidAmount() {
		return billUnpaidAmount;
	}

	@Override
	public BigDecimal getOriginalAmount() {
		return billOriginalAmount;
	}

	@Override
	public Bool getIndeterminateSwitch() {
		return indeterminateSwitch;
	}

	@Override
	public void invoke() {

		billUnpaidAmount = BigDecimal.ZERO;
		billOriginalAmount = BigDecimal.ZERO;

		// Validate Bill Id for Delinquency
		validateInputs();

		// Calculate Unpaid and Original Amount for Bill Id
		CmDetermineOpenBillItemAmounts openBillItemAmt = CmDetermineOpenBillItemAmounts.Factory.newInstance();

		OpenItemBillAmountResults output = openBillItemAmt.getBillAmounts(new Bill_Id(cmDelinquencyProcessRelatedObject.fetchIdPrimaryKeyValue1()).getEntity(), null);

		billUnpaidAmount = output.getUnpaidBillAmount().getAmount();
		billOriginalAmount = output.getOriginalBillAmount().getAmount();
		indeterminateSwitch = output.getIsUnpaidAmountIndeterminate();

	}

	private void validateInputs() {

		Bill_Id bilId = null;
		String delLevelMaintObj = null;
		String perId = null;
		String acctId = null;
		QueryResultRow result = null;

		// Throw error if Delinquency Process Id is Null
		if (isNull(cmDelinquencyProcessId)) {
			addError(MessageRepository.delProcIdRequiredForPlugInSpot());
		}

		// Throw error if delinquency Process Related Obeject is Null
		if (isNull(cmDelinquencyProcessRelatedObject))
		{
			addError(MessageRepository.delProcRelObjRequiredForPlugInSpot());
		}

		// Throw error if Maintenance Object is not BILL or
		// Related Object Type flag is not CMCO
		if (!(cmDelinquencyProcessRelatedObject.fetchIdMaintenanceObject().getId().getTrimmedValue().compareTo(CmDelinquencyCustomerMonitorRuleConstants.MAINTENANCE_OBJ_BILL) == 0)
				|| !(cmDelinquencyProcessRelatedObject.fetchIdCmDelinquencyRelatedObjTypeFlg().trimmedValue().compareTo(CmDelinquencyRelatedObjTypeLookup.constants.CM_COLLECTING_ON.trimmedValue()) == 0)) {
			addError(MessageRepository.billIdRequired());
		}

		bilId = new Bill_Id(cmDelinquencyProcessRelatedObject.fetchIdPrimaryKeyValue1());

		// Determine Delinquency Level of Delinquency Process
		Query<QueryResultRow> hqlQuery = createQuery(CmDelinquencyCustomerMonitorRuleConstants.DETERMINE_DEL_LVL.toString(), "CmCalculateUnpaidOriginalAmountAlgComp_Impl");
		hqlQuery.bindId("delinProcId", cmDelinquencyProcessId);
		hqlQuery.bindLookup("relObjTypeFlg", CmDelinquencyRelatedObjTypeLookup.constants.CM_DELINQUENCY_LEVEL);
		hqlQuery.addResult("MAINT_OBJ_CD", "DPRO.id.maintenanceObject");
		hqlQuery.addResult("PK_VALUE1", "DPRO.id.primaryKeyValue1");
		result = hqlQuery.firstRow();

		if (notNull(result)) {
			// Determine Maintenance Object of corresponding Delinquency Process
			//this will indirectly determine Delinquency Level
			delLevelMaintObj = result.getEntity("MAINT_OBJ_CD", MaintenanceObject.class).getId().getTrimmedValue();

			// Customer Level Delinquency
			if (notNull(delLevelMaintObj) && (delLevelMaintObj.trim().compareTo(CmDelinquencyCustomerMonitorRuleConstants.MAINTENANCE_OBJ_PERSON) == 0)) {
				perId = result.getString("PK_VALUE1");
				Person_Id personId = new Person_Id(perId);
				Query<QueryResultRow> query = createQuery(CmDelinquencyCustomerMonitorRuleConstants.CHK_DEL_PROC_CUST_LVL.toString(), "CmCalculateUnpaidOriginalAmountAlgComp_Impl");
				query.bindId("person_id", personId);
				query.bindId("bill_id", bilId);
				query.addResult("person", "AP.id.person");

				// Throw error if Bill Id does not belongs to Delinquency Process Person
				if (isNull(query.firstRow())) {

					addError(MessageRepository.bilNotBelongsToDelProcPer(bilId, personId, cmDelinquencyProcessId));
				}

			}
			// Account Level Delinquency
			else if (notNull(delLevelMaintObj) && (delLevelMaintObj.trim().compareTo(CmDelinquencyCustomerMonitorRuleConstants.MAINTENANCE_OBJ_ACCOUNT) == 0)) {
				acctId = result.getString("PK_VALUE1");
				Account_Id accountId = new Account_Id(acctId);
				Query<QueryResultRow> query = createQuery(CmDelinquencyCustomerMonitorRuleConstants.CHK_DEL_PROC_ACCT_LVL.toString(), "CmCalculateUnpaidOriginalAmountAlgComp_Impl");
				query.bindId("account_id", accountId);
				query.bindId("bill_id", bilId);
				query.addResult("account", "B.account");

				// Throw error if Bill Id does not belongs to Delinquency Process Account
				if (isNull(query.firstRow())) {

					addError(MessageRepository.bilNotBelongsToDelProcAcct(bilId, accountId, cmDelinquencyProcessId));
				}
			}

		}
	}

}

