/*
 **************************************************************************************************************************
 * 
 * PROGRAM DESCRIPTION:
 * 
 * Customer Contact Helper
 * 
 * This business component has common methods that can be used by
 * Delinquency Process modules that need it
 **************************************************************************************************************************
 * 
 * 
 * CHANGE HISTORY:
 * 
 * Date:       	by:         Reason:
 * 2020-05-06   VINODW		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework
 **************************************************************************************************************************
 */
package com.splwg.cm.domain.delinquency.collection.delinquencyProcess;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.GenericBusinessComponent;
import com.splwg.base.api.Query;
import com.splwg.base.api.QueryResultRow;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.lookup.YesNoOptionLookup;
import com.splwg.base.domain.common.featureConfiguration.FeatureConfiguration;
import com.splwg.base.domain.common.featureConfiguration.FeatureConfigurationOption;
import com.splwg.ccb.domain.admin.idType.accountIdType.AccountNumberType_Id;
import com.splwg.ccb.domain.billing.bill.Bill;
import com.splwg.ccb.domain.billing.bill.Bill_Id;
import com.splwg.ccb.domain.billing.bill.OpenItemBillAmountResults;
import com.splwg.ccb.domain.customerinfo.account.Account;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.customerinfo.person.PersonId;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.cm.domain.delinquency.admin.collectionClassOverdueRules.CmDetermineOpenBillItemAmounts;

/**
 * @author MugdhaP
 *
@BusinessComponent (customizationCallable = true, customizationReplaceable = true)
 */
public class CmSystematicStatementDelinquencyHelper_Impl
extends GenericBusinessComponent
		implements CmSystematicStatementDelinquencyHelper {

	public final static String CONST_Y = "Y";
	public final static String CONST_N = "N";
	public final static String MAINTENANCE_OBJ_PERSON = "PERSON";
	
	public final static StringBuilder FETCH_DELIQ_LVL_ID = new StringBuilder("from CmDelinquencyProcessRelatedObject dpo ")
			.append(" where dpo.id.delinquencyProcess.id = :delinProcId ")
			.append(" and dpo.id.cmDelinquencyRelatedObjTypeFlg = :relatedObjTypeFlag ");
	
	//fetch person Id from source system cd
	public final static StringBuffer FETCH_PERNBR_FROM_ID_TYPE = new StringBuffer(" from PersonId pid where pid.id.idType.id = :idTypeCd and pid.isPrimaryId = 'Y' ")
			.append(" and  pid.id.person.id = :personId ");
	
	
	/**
	 * Populates a HashMap containing list of Account Ids in an arraylist for
	 * each Bill Group Id
	 * 
	 * @param personId
	 * @param accountIdType
	 * @return HashMap
	 */
	public HashMap<String, ArrayList<String>> getBillGroupRelatedAccounts(Person_Id personId,
			AccountNumberType_Id accountIdType) {

		StringBuilder queryString = new StringBuilder();
		queryString.append("FROM AccountNumber accNbr, AccountPerson accPer ");
		queryString.append("WHERE accPer.id.account = accNbr.id.account ");
		queryString.append("AND accPer.isMainCustomer = 'Y' ");
		queryString.append("AND accNbr.isPrimaryId = 'Y' ");
		queryString.append("AND accPer.id.person.id = :personId ");
		queryString.append("AND accNbr.id.accountIdentifierType.id = :accountIdType ");

		Query<QueryResultRow> query = createQuery(queryString.toString(), (new StringBuilder())
				.append("CmCustomerContactHelper_Impl").append(".").append("getBillGroupRelatedAccounts").toString());
		query.bindId("personId", personId);
		query.bindId("accountIdType", accountIdType);

		query.addResult("accNbr", "accNbr.accountNumber");
		query.addResult("accId", "accNbr.id.account.id");
		query.orderBy("accNbr", Query.ASCENDING);

		HashMap<String, ArrayList<String>> billGrpRelatedAccountsMap = new HashMap<String, ArrayList<String>>();

		List<QueryResultRow> billGrpRelatedAccountsList = query.list();
		Account_Id accountId;
		// For each row in bill group related account list result row
		for (QueryResultRow row : billGrpRelatedAccountsList) {
			// Bill Group level grouping of Account Ids
			String key = row.getString("accNbr");
			ArrayList<String> existingAccountList = billGrpRelatedAccountsMap.get(key);
			accountId = (Account_Id) row.getId("accId", Account.class);

			if (isNull(existingAccountList) || existingAccountList.isEmpty()) {
				existingAccountList = new ArrayList<String>();
			}
			existingAccountList.add(accountId.getIdValue());
			billGrpRelatedAccountsMap.put(key, existingAccountList);
		}
		return billGrpRelatedAccountsMap;
	}

	/**
	 * Retrieves the QueryResultRow from the Bill Information.
	 * 
	 * @param listAccountIds
	 * @param processDate
	 * @return List
	 */
	public List<QueryResultRow> retrieveBillsForBillGroup(ArrayList<String> listAccountIds, Date processDate) {
		StringBuilder strAccountList = new StringBuilder();
		StringBuilder queryString = new StringBuilder();

		if (listAccountIds.size() > 0) {
			for (String strAccountId : listAccountIds) {
				strAccountList.append("'");
				strAccountList.append(strAccountId);
				strAccountList.append("',");
			}
		}
		// logical and physical adjustment to remove trailing comma
		if (strAccountList.length() > 0) {
			strAccountList.setLength(strAccountList.length() - 1);
		}
		/*queryString.append("FROM Bill bill ");
		queryString.append("WHERE bill.billStatus = 'C' ");
		queryString.append("AND bill.account in (");
		queryString.append(strAccountList).append(") ");
		queryString.append("AND bill.dueDate < :processDate ");
		queryString
				.append("AND EXISTS (SELECT finTran.matchEventId FROM FinancialTransaction finTran, MatchEvent match ");
		queryString.append(
				"WHERE ( finTran.matchEventId = ' ' OR ( finTran.matchEventId = match.id AND match.matchEventStatus = 'O')) ");
		queryString.append("AND finTran.billId = bill.id ) ");*/
		queryString.append("	FROM Bill bill ");
		queryString.append("	WHERE  bill.billStatus = 'C' ");
		queryString.append("	AND bill.account in (");
		queryString.append(strAccountList).append(") ");
		queryString.append("	AND bill.dueDate < :processDate ");
		queryString
				.append(" AND EXISTS (SELECT finTran.matchEventId FROM FinancialTransaction finTran  where (finTran.matchEventId = ' ' ");
		queryString.append(" OR (finTran.matchEventId <> ' ' ");
		queryString.append("	AND	EXISTS ( select match.id from MatchEvent match where  finTran.matchEventId = match.id AND match.matchEventStatus = 'O')) )");
		queryString.append("AND finTran.billId = bill.id ) ");

		// Create HQL Query
		Query<QueryResultRow> queryBillData = createQuery(queryString.toString(), (new StringBuilder())
				.append("CmCustomerContactHelper_Impl").append(".").append("retrieveBillsForBillGroup").toString());
		queryBillData.bindDate("processDate", processDate);

		queryBillData.addResult("billId", "bill.id");
		queryBillData.addResult("billDate", "bill.billDate");
		queryBillData.addResult("dueDate", "bill.dueDate");
		queryBillData.orderBy("dueDate", Query.DESCENDING);
		// Bind Variables
		return queryBillData.list();
	}

	/**
	 * Retrieves the QueryResultRow from the Aggregated payments at the Bill
	 * Group level
	 * 
	 * @param listAccountIds
	 * @param adminContrFeatureConfig
	 * @param adminContrOptionType
	 * @return Money
	 */
	private Money retrieveOnAccountPayments(ArrayList<String> listAccountIds,
			FeatureConfiguration adminContrFeatureConfig, String adminContrOptionType) {
		StringBuilder strAccountList = new StringBuilder();
		StringBuilder queryString = new StringBuilder();

		if (listAccountIds.size() > 0) {
			for (String strAccountId : listAccountIds) {
				strAccountList.append("'");
				strAccountList.append(strAccountId);
				strAccountList.append("',");
			}
		}

		// logical and physical adjustment to remove trailing comma
		if (strAccountList.length() > 0) {
			strAccountList.setLength(strAccountList.length() - 1);
		}

		queryString.append("FROM FinancialTransaction finTran, ServiceAgreement srvAgreement ");
		queryString.append("WHERE finTran.serviceAgreement.id = srvAgreement.id ");
		queryString.append("AND finTran.isFrozen = 'Y'  ");
		queryString.append("AND srvAgreement.account in (");
		queryString.append(strAccountList).append(") ");
		queryString.append("AND srvAgreement.serviceAgreementType.id.saType IN ( ");
		queryString.append(" SELECT fcOption.value FROM FeatureConfigurationOption fcOption ");
		queryString.append("WHERE fcOption.id.workforceManagementSystem = :adminContrFeatureConfig ");
		queryString.append("AND fcOption.id.optionType = :adminContrOptionType ) ");
		queryString.append("AND (finTran.matchEventId = ' ' ");
		queryString.append("OR finTran.matchEventId IN ( ");
		queryString.append("SELECT matchEvt.id FROM MatchEvent matchEvt ");
		queryString.append("WHERE matchEvt.id = finTran.matchEventId  ");
		queryString.append("AND matchEvt.matchEventStatus = 'O'))  ");

		// Create HQL Query
		Query<Money> queryAccPayment = createQuery(queryString.toString(), (new StringBuilder())
				.append("CmCustomerContactHelper_Impl").append(".").append("retrieveAccountPayments").toString());

		// Bind Variables
		queryAccPayment.bindEntity("adminContrFeatureConfig", adminContrFeatureConfig);
		queryAccPayment.bindStringProperty("adminContrOptionType", FeatureConfigurationOption.properties.optionType,
				adminContrOptionType);

		// Add Result
		queryAccPayment.addResult("overPay", "NVL(SUM(finTran.currentAmount),0)");

		return queryAccPayment.firstRow();

	}

	/**
	 * Retrieves the QueryResultRow from the Aggregated Pending Amount at the
	 * Bill Group level
	 * 
	 * @param listAccountIds
	 * @param dtLatestDueDate
	 * @return Money
	 */
	private Money retrieveSumBillGroupOriginalAmount(ArrayList<String> listAccountIds, Date dtLatestDueDate) {
		StringBuilder strAccountList = new StringBuilder();
		StringBuilder queryString = new StringBuilder();

		if (listAccountIds.size() > 0) {
			for (String strAccountId : listAccountIds) {
				strAccountList.append("'");
				strAccountList.append(strAccountId);
				strAccountList.append("',");
			}
		}

		// logical and physical adjustment to remove trailing comma
		if (strAccountList.length() > 0) {
			strAccountList.setLength(strAccountList.length() - 1);
		}

		queryString.append("FROM FinancialTransaction finTran, Bill bill, FinancialTransactionExtension finTranExt ");
		queryString.append("WHERE bill.billStatus = 'C' ");
		queryString.append("AND bill.dueDate = :latestDueDate  ");
		queryString.append("AND bill.account in (");
		queryString.append(strAccountList).append(") ");
		queryString.append("AND finTran.billId = bill.id ");
		queryString.append("AND finTran.isFrozen = 'Y' ");
		queryString.append("AND finTran.shouldShowOnBill = 'Y' ");
		queryString.append("AND finTran.financialTransactionType IN ('AD', 'AX', 'BS', 'BX') ");
		queryString.append("AND finTranExt.id = finTran.id ");
		queryString.append("AND finTranExt.startDate <= bill.dueDate ");

		// Create HQL Query
		Query<Money> queryAccPayment = createQuery(queryString.toString(),
				(new StringBuilder()).append("CmCustomerContactHelper_Impl").append(".")
						.append("retrieveSumBillGroupOriginalAmount").toString());

		// Bind Variables
		queryAccPayment.bindDate("latestDueDate", dtLatestDueDate);

		queryAccPayment.addResult("totalAmount", "NVL(SUM(finTran.currentAmount),0)");

		return queryAccPayment.firstRow();

	}

	/**
	 * Retrieves the Broker Id based on the input parameters Person Id Type and
	 * System generated Person Id
	 * 
	 * @param listAccountIds
	 * @param dtLatestDueDate
	 * @return Money
	 */
	public String retrieveBrokerId(String strPersonNbr) {
		StringBuilder queryString = new StringBuilder();
		String strBrokerId = "";

		queryString.append("FROM CmBrokerInformation bInfo ");
		queryString.append("WHERE bInfo.id.cmSourceCustomerId = :sourceCustomerId ");

		// Create HQL Query
		Query<QueryResultRow> queryBrokerId = createQuery(queryString.toString(), (new StringBuilder())
				.append("CmCustomerContactHelper_Impl").append(".").append("retrieveBrokerId").toString());
		
		queryBrokerId.bindStringProperty("sourceCustomerId", PersonId.properties.personIdNumber, strPersonNbr);
		queryBrokerId.addResult("brokerId", "bInfo.id.cmBrokerId");
		queryBrokerId.addResult("updatedDttm", "bInfo.updatedDttm");
		queryBrokerId.orderBy("updatedDttm", Query.DESCENDING);

		QueryResultRow rowBrokerId = queryBrokerId.firstRow();

		if (notNull(rowBrokerId)) {

			strBrokerId = rowBrokerId.get("brokerId").toString();
		}
		return strBrokerId;
	}
	
	/**
	 * Retrieves the Policy Id
	 * 
	 * @param strBillIdList
	 * @return String
	 */
	public String retrievePolicyId(String strBillIdList) {
		StringBuilder queryString = new StringBuilder();
		String strPolicyId = "";

		queryString.append("FROM FinancialTransactionExtension txnExt , FinancialTransaction txn ");
		queryString.append("WHERE txnExt.id = txn.id ");
		queryString.append("AND TRIM(txnExt.policyId) IS NOT NULL ");
		queryString.append("AND txn.billId IN (");
		queryString.append(strBillIdList).append(") ");

		// Create HQL Query
		Query<QueryResultRow> queryPolicyId = createQuery(queryString.toString(), (new StringBuilder())
				.append("CmCustomerContactHelper_Impl").append(".").append("retrievePolicyId").toString());

		queryPolicyId.addResult("policyId", "txnExt.policyId");
		queryPolicyId.addResult("billId", "txn.billId");
		queryPolicyId.selectDistinct(true);

		QueryResultRow rowPolicyId = queryPolicyId.firstRow();

		if (notNull(rowPolicyId)) {

			strPolicyId = rowPolicyId.get("policyId").toString();
		}
		return strPolicyId;
	}

	/**
	 * This method is to evaluate the customer for delinquency on the basis of
	 * given band parameters.
	 * 
	 * @author praratho
	 * 
	 * @param systematicStatementDelinquencyDO
	 * 
	 * @return Is Customer eligible for Delinquency
	 * 
	 */
	public boolean isCustomerEligibleForDelinquency(
			CmSystematicStatementDelinquencyDataObject systematicStatementDelinquencyDO) {

		Date latestDueDate = null;
		Bill bill = null;
		BigDecimal billUnpaidAmount = BigDecimal.ZERO;
		BigDecimal billOriginalAmount = BigDecimal.ZERO;
		BigDecimal totalBillGroupUnpaidAmount = BigDecimal.ZERO;
		BigDecimal totalBillGroupOriginalAmount = BigDecimal.ZERO;
		ArrayList<String> accountIdList = systematicStatementDelinquencyDO.getAccountIdList();

		if (isNull(systematicStatementDelinquencyDO.getAge())) {
			systematicStatementDelinquencyDO.setAge(new BigInteger("0"));
		}

		// Bills due date should be before or same as process date minus band
		// age date
		Date dateToCompare = getProcessDateTime().getDate()
				.addDays(systematicStatementDelinquencyDO.getAge().negate().intValue());

		// Retrieve all overdue Bills related to the accountList
		List<QueryResultRow> overdueBillRecordList = this.retrieveBillsForBillGroup(accountIdList, dateToCompare);

		// For each bill in over due bill list
		for (QueryResultRow overdueBillRecord : overdueBillRecordList) {

			// Retrieve bill id from query result row
			Bill_Id billId = (Bill_Id) overdueBillRecord.getId("billId", Bill.class);
			bill = billId.getEntity();

			// If latest due date is null ie. it is the first record being
			// processed.
			if (isNull(latestDueDate)) {
				latestDueDate = bill.getDueDate();
			}

			// Retrieve current bill unpaid amount and bill original amount
			CmDetermineOpenBillItemAmounts openBillItemAmt = CmDetermineOpenBillItemAmounts.Factory.newInstance();
			OpenItemBillAmountResults output = openBillItemAmt.getBillAmounts(bill, null);
			billUnpaidAmount = output.getUnpaidBillAmount().getAmount();
			billOriginalAmount = output.getOriginalBillAmount().getAmount();

			// Calculate total bill group unpaid amount
			totalBillGroupUnpaidAmount = totalBillGroupUnpaidAmount.add(billUnpaidAmount);

			// Calculate total bill group original amount
			totalBillGroupOriginalAmount = totalBillGroupOriginalAmount.add(billOriginalAmount);

		}

		// If Include On Account Payments In Threshold Evaluation soft parameter
		// is set to Y
		if (systematicStatementDelinquencyDO.getIncludeOnAccountPayments().trimmedValue()
				.equals(YesNoOptionLookup.constants.YES.trimmedValue())) {

			// Retrieve open balance of all administrative contracts of the
			// Accounts falling under same bill group.
			BigDecimal onAcctPaymentsOnAdminAcct = this
					.retrieveOnAccountPayments(accountIdList,
							systematicStatementDelinquencyDO.getAdminstrativeContractTypeFeatureConfig(),
							systematicStatementDelinquencyDO.getAdminstrativeContractTypeOptionType().trimmedValue())
					.getAmount();

			totalBillGroupUnpaidAmount = totalBillGroupUnpaidAmount.subtract(onAcctPaymentsOnAdminAcct.multiply(new BigDecimal(-1)));
		}

		// If Use Current Revenue Period Billed For Latest Overdue Due Date In
		// Threshold Evaluation
		// (algorithm parameter) = Y and Latest Due Date is not null
		if (notNull(systematicStatementDelinquencyDO.getUseCurrentRevenuePeriodBilled()) && notNull(latestDueDate)
				&& systematicStatementDelinquencyDO.getUseCurrentRevenuePeriodBilled().trimmedValue()
						.equals(YesNoOptionLookup.constants.YES.trimmedValue())) {

			// Get the total billed amount for the current revenue period (i.e.
			// coverage start date = due date) on all completed
			// bills (including fully paid) of all Accounts of the Bill Group
			// with a Due Date = Latest Due Date.
			totalBillGroupOriginalAmount = this.retrieveSumBillGroupOriginalAmount(accountIdList, latestDueDate)
					.getAmount();
		}

		BigDecimal totalBillGroupUnpaidPercentage = BigDecimal.ZERO;
		if (totalBillGroupOriginalAmount.compareTo(BigDecimal.ZERO) != 0) {
			// Calculate totalBillGroupUnpaidPercentage =
			// (totalBillGroupUnpaidAmount / totalBillGroupOriginalAmount) * 100
			totalBillGroupUnpaidPercentage = totalBillGroupUnpaidAmount.divide(totalBillGroupOriginalAmount)
					.multiply(new BigDecimal(100));
		}
		// If band unpaid amount and percentage required is set to Y
		if (systematicStatementDelinquencyDO.getUnpaidAmtAndPrctRequired().trimmedValue()
				.equals(YesNoOptionLookup.constants.YES.trimmedValue())) {

			// If totalBillGroupUnpaidAmount >= Band Unpaid Amount
			// AND totalBillGroupUnpaidPercentage >= Band Unpaid Percentage
			if (totalBillGroupUnpaidAmount.compareTo(systematicStatementDelinquencyDO.getUnpaidAmount()) >= 0
					&& totalBillGroupUnpaidPercentage
							.compareTo(systematicStatementDelinquencyDO.getUnpaidPercentage()) >= 0) {

				return true;
			}
		} else {
			// If totalBillGroupUnpaidAmount >= Band Unpaid Amount
			// OR totalBillGroupUnpaidPercentage >= Band Unpaid Percentage
			if ((notNull(systematicStatementDelinquencyDO.getUnpaidAmount())
					&& totalBillGroupUnpaidAmount.compareTo(systematicStatementDelinquencyDO.getUnpaidAmount()) >= 0)
					|| (notNull(systematicStatementDelinquencyDO.getUnpaidPercentage())
							&& totalBillGroupUnpaidPercentage
									.compareTo(systematicStatementDelinquencyDO.getUnpaidPercentage()) >= 0)) {

				return true;
			}
		}
		return false;
	}
}
