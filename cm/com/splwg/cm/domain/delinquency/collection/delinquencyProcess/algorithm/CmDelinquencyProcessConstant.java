/*
 ****************************************************************************************************************************************************
 *
 *
 * PROGRAM DESCRIPTION:
 *
 * Delinquency Process Helper Business components
 *
 * This helper class contains common methods used for Delinquency Process across various
 * modules.
 ****************************************************************************************************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:       	by:    		Reason:
 * YYYY-MM-DD  	IN     		Reason text.
 *
 * 2020-05-06   MugdhaP		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework
 ****************************************************************************************************************************************************
 */
package com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm;

public class CmDelinquencyProcessConstant {

	public final static String EMPTY= "";
	public final static String QUOTE= "'";

	public static final String MAINTENANCE_OBJ_BILL = "BILL";
	public static final String RELATED_OBJ_FLAG = "CMCO";
	public final static String FINAL= "F1FL";
	public final static String DAYS_AGO= " day(s) ago";
	public final static String DAYS_FROM_TODAY= " day(s) from today";
	public static final int MAX_TO_DO_INFORMATION_LENGTH = 150;

	public final static String PARAM_DELINQUECNY_PROCESS_BO_STATUS_FLAG= "FINAL";
	public final static String PARAM_DELINQUECNY_PROCESS_ID= "CM_DELIN_PROC_ID";
	public final static String PARAM_DELINQUECNY_PROCESS_TYPE_DESC= "delinquencyProcessDescr";
	public final static String PARAM_BO_STATUS_DESC= "boStatusDescr";
	public final static String PARAM_AGE= "age";
	public final static String UNPAIDAMOUNT= "unpaidAmt";
	public static final String REQUEST = "request";
	public static final String BUSINESS_OBJ_STATUS = "boStatus";

	public final static StringBuilder DELINQUENCY_REL_OBJ_PERSON_QUERY = new StringBuilder()
			.append(" FROM CmDelinquencyProcessRelatedObject DELRO WHERE DELRO.id.delinquencyProcess = :delinquencyProcess ")
			.append(" AND DELRO.id.maintenanceObject = 'PERSON' AND DELRO.id.cmDelinquencyRelatedObjTypeFlg = 'CMDL' ");

	public final static StringBuilder DELINQUENCY_REL_OBJ_ACCOUNT_QUERY = new StringBuilder()
			.append(" FROM CmDelinquencyProcessRelatedObject DELRO WHERE DELRO.id.delinquencyProcess = :delinquencyProcess ")
			.append(" AND DELRO.id.maintenanceObject = 'ACCOUNT' AND DELRO.id.cmDelinquencyRelatedObjTypeFlg = 'CMDL' ");

	public final static StringBuilder CHECK_ON_ACCOUNT_PAYMENT_FOR_PERSON = new StringBuilder()
			.append(" from ServiceAgreement SA, FinancialTransaction FT, AccountPerson AP   WHERE SA.account.id = AP.id.account AND SA.serviceAgreementType.id.saType IN ")
			.append(" (SELECT RPAD(WFMOPT.value,8,' ') FROM FeatureConfigurationOption WFMOPT WHERE WFMOPT.id.workforceManagementSystem = :featureConfig ")
			.append(" AND WFMOPT.id.optionType = :featureConfigOptionType) AND SA.id = FT.serviceAgreement.id AND FT.isFrozen = :isFrozen ")
		        .append(" AND AP.id.person = :person  AND AP.isMainCustomer = 'Y' ");

	public final static StringBuilder ACCOUNT_PERSONS_FOR_PERSON = new StringBuilder()
			.append(" from AccountPerson AP   WHERE AP.id.person = :person  AND AP.isMainCustomer = 'Y' ");

	public final static StringBuilder PER_COL_FROM_PERSON_QUERY = new StringBuilder()
			.append(" FROM CmPersonCollection PERCOL WHERE PERCOL.id.person = :person ");

	public final static StringBuilder ACTIVE_PAY_PLAN_PERSON_QUERY = new StringBuilder()
			.append(" FROM PaymentPlan PP, AccountPerson AP WHERE AP.id.person = :person AND PP.paymentPlanStatus = '20' ")
			.append(" AND PP.account = AP.id.account AND AP.isMainCustomer = 'Y' ");

	public final static StringBuilder ACTIVE_PAY_PLAN_ACCOUNT_QUERY = new StringBuilder()
			.append(" FROM PaymentPlan PP WHERE PP.account = :account AND PP.paymentPlanStatus = '20' ");

	public final static StringBuilder PENDIN_PAYMENTS_ON_PERSON_QUERY = new StringBuilder()
			.append(" FROM C1Request REQ, C1RequestRelatedObject REQOBJ ")
			.append(" WHERE REQ.id = REQOBJ.id.c1Request AND REQOBJ.primaryKeyValue1 = :person ")
			.append(" AND REQOBJ.id.c1RequestRelationshipObjectType = :c1RequestRelationshipObjectTypeLookupPerson ")
			.append(" AND REQ.c1RequestType in (:paymentRequestTypeParmVal) ")
			.append(" AND REQ.status in (:paymentStausCodes) ");

	public final static StringBuilder TOT_BILLED_AMOUNT_CUSTOMER_LVL = new StringBuilder()
			.append(" FROM FinancialTransaction FT, Bill BL, FinancialTransactionExtension CMFT, AccountPerson AP WHERE BL.account = AP.id.account ")
			.append(" AND AP.id.person = :personId AND AP.isMainCustomer = 'Y' AND BL.billStatus = 'C' AND BL.dueDate = :latestDueDate AND FT.billId = BL.id ")
			.append(" AND FT.isFrozen = 'Y' AND FT.shouldShowOnBill = 'Y' AND FT.financialTransactionType IN ('AD', 'AX', 'BS', 'BX') AND CMFT.id = FT.id AND CMFT.startDate <= BL.dueDate ");
	public final static StringBuilder FETCH_DEL_PROC_TRIGG_EVNTS = new StringBuilder()
			.append(" FROM CmDelinquencyProcessTriggerEvent TE ")
			.append(" WHERE TE.statusDateTime is null ")
			.append(" AND TE.id.businessObjectStatus.id.status IN ( ");
	public final static StringBuilder FETCH_ACCT_FROM_DEL_PROC_BILLS = new StringBuilder()
			.append(" FROM CmDelinquencyProcessRelatedObject DPO, Bill B WHERE DPO.id.delinquencyProcess.id = :delinquencyProcessId ")
			.append(" AND DPO.id.primaryKeyValue1 = B.id ");

	public final static StringBuilder FETCH_ACCT_BILL_FROM_DEL_PROC = new StringBuilder()
			.append(" FROM CmDelinquencyProcessRelatedObject DPO, Bill B ")
			.append(" WHERE DPO.id.delinquencyProcess.id = :delinquencyProcessId ")
			.append(" AND B.account.id = :accountId AND DPO.id.primaryKeyValue1 = B.id ");

	public static final String BASIS_UNPAID_AMT_C = "C";
	public static final String BASIS_UNPAID_AMT_D = "D";
	public static final String TERMINATED = "TERMINATED";
}

