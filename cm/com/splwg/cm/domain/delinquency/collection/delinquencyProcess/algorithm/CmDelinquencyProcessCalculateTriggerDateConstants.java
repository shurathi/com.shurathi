/*                                                               
 ********************************************************************************************                                                                
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Delinquency Calculation 
 * This constant file is created for calculate trigger events
 *                                                             
 ********************************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 * 2020-05-06   MugdhaP		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework
 * ******************************************************************************************
 */

package com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm;

/**
 * @author MugdhaP
 *
 */
public class CmDelinquencyProcessCalculateTriggerDateConstants {

	public final static String DEPENDENT_ON_STATE = "CMDS";

	public final static String DAYS_ON_DELQ_PROC_TYPE = "CMPT";

	public final static String DAYS_USAGE_AFTR = "AFTR";

	public final static String DAYS_USAGE_BFR = "BEFR";

	public final static String WORK_DAYS = "W";

	public static final String Bill_CYCLE = "Bill cycles";

	public static final String INVOICE_DAY_CHAR = "Invoice Days";

	public static final String MAINTENANCE_OBJ_PERSON = "PERSON";

	public static final String MAINTENANCE_OBJ_ACCOUNT = "ACCOUNT";

	public static final StringBuilder FETCH_DELIQ_LVL_ID = new StringBuilder("from CmDelinquencyProcessRelatedObject dpo ")
			.append(" where dpo.id.delinquencyProcess.id = :delinProcId ")
			.append(" and dpo.id.cmDelinquencyRelatedObjTypeFlg = :relatedObjTypeFlag ");

	public static final StringBuilder LATEST_DUE_DATE_SQL = new StringBuilder("from CmDelinquencyProcessRelatedObject dpo, Bill bl ")
			.append(" where dpo.id.delinquencyProcess.id = :delinProcId ")
			.append(" and dpo.id.maintenanceObject = 'BILL' ")
			.append(" and dpo.id.cmDelinquencyRelatedObjTypeFlg = :relatedObjTypeFlag ")
			.append(" and dpo.id.primaryKeyValue1 = bl.id ");

	public static final StringBuilder CALENDER_WORK_DAY_QRY = new StringBuilder("FROM FeatureConfigurationOption WO ")
			.append(" WHERE WO.id.workforceManagementSystem = :deliqProcOptFeatureConfig AND WO.id.optionType = :deliqProcOptionType");

	public static final StringBuilder STATE_OF_ISSUE_CUSTOMER_LVL = new StringBuilder(" SELECT DISTINCT PPC.SRCH_CHAR_VAL AS STATE_OF_ISSUE ")
			.append(" FROM CI_BILL_CHG BC , ")
			.append("   CI_BILL_CHG_CHAR BCC , ")
			.append("  CI_SA SA , ")
			.append("  CI_SA_CHAR SAC , ")
			.append("  CI_POLICY_PLAN PP , ")
			.append("  CI_POLICY PO , ")
			.append("  F1_BUS_OBJ_STATUS_OPT OPT , ")
			.append(" CI_POLICY_PER PPER , ")
			.append("  CI_ACCT_PER APER , ")
			.append("  CI_POLICY_PLAN_CHAR PPC ")
			.append(" WHERE APER.PER_ID        = :personId ")
			.append(" AND APER.MAIN_CUST_SW    = 'Y' ")
			.append(" AND APER.ACCT_ID         = SA.ACCT_ID ")
			.append(" AND BC.SA_ID             = SA.SA_ID ")
			.append(" AND BC.BILLABLE_CHG_STAT = :billable ")
			.append(" AND BC.START_DT         <= :latestBillDueDate ")
			.append(" AND BC.END_DT           >= :latestBillDueDate ")
			.append(" AND SAC.SA_ID            = SA.SA_ID ")
			.append(" AND SAC.CHAR_TYPE_CD     = :policyPlanCharType ")
			.append(" AND SAC.SRCH_CHAR_VAL    = PP.PLAN_ID ")
			.append(" AND PP.POLICY_ID         = PO.POLICY_ID ")
			.append(" AND OPT.BUS_OBJ_CD       = PO.BUS_OBJ_CD ")
			.append(" AND OPT.BO_STATUS_CD     = PO.BO_STATUS_CD ")
			.append(" AND OPT.BO_OPT_FLG       = :polStatOptionType ")
			.append(" AND (OPT.BO_OPT_VAL      = :polStatActOptionVal ")
			.append(" OR (OPT.BO_OPT_VAL       = :polStatTermOptionVal ")
			.append(" AND PO.END_DT            > :processDate)) ")
			.append(" AND OPT.SEQ_NUM          = ")
			.append("   (SELECT MAX (OPT2.SEQ_NUM) ")
			.append("  FROM F1_BUS_OBJ_STATUS_OPT OPT2 ")
			.append("  WHERE OPT2.BUS_OBJ_CD = OPT.BUS_OBJ_CD ")
			.append("  AND OPT2.BO_STATUS_CD = OPT.BO_STATUS_CD ")
			.append("  AND OPT2.BO_OPT_FLG   = OPT.BO_OPT_FLG ")
			.append("  ) ")
			.append(" AND PPER.POLICY_ID    = PO.POLICY_ID ")
			.append(" AND PPER.MAIN_CUST_SW = 'Y' ")
			.append(" AND PPER.PER_ID       = APER.PER_ID ")
			.append(" AND PPC.PLAN_ID       = PP.PLAN_ID ")
			.append(" AND PPC.CHAR_TYPE_CD  = :stateOfIssueCharType ")
			.append(" AND PPC.EFFDT         = ")
			.append("   (SELECT MAX(PPC2.EFFDT) ")
			.append("   FROM CI_POLICY_PLAN_CHAR PPC2 ")
			.append("   WHERE PPC2.PLAN_ID  = PPC.PLAN_ID ")
			.append("   AND PPC2.CHAR_TYPE_CD = PPC.CHAR_TYPE_CD ")
			.append("   AND PPC2.EFFDT       <= :latestBillDueDate ")
			.append("   ) ");

	public static final StringBuilder GRACE_DAYS_DIVISION = new StringBuilder(" SELECT MAX(TO_NUMBER(DIVC.ADHOC_CHAR_VAL)) AS GRACE_DAYS, ")
			.append(" DIVC.CIS_DIVISION             AS STAT_ISSUE ")
			.append("FROM CI_CIS_DIV_CHAR DIVC ")
			.append("WHERE DIVC.CHAR_TYPE_CD    = :graceDaysCharType ")
			.append("AND DIVC.EFFDT           = ")
			.append("  (SELECT MAX(DIVC2.EFFDT) ")
			.append("  FROM CI_CIS_DIV_CHAR DIVC2 ")
			.append("  WHERE DIVC2.CIS_DIVISION = DIVC.CIS_DIVISION ")
			.append("  AND DIVC2.CHAR_TYPE_CD   = DIVC.CHAR_TYPE_CD ")
			.append("  AND DIVC2.EFFDT         <= :latestBillDueDate ")
			.append("  ) ");

	public static final StringBuilder DRAG_DAYS_CHAR = new StringBuilder(" SELECT MAX(TO_NUMBER(AC.ADHOC_CHAR_VAL)) DRAG_DAYS ")
			.append(" FROM CI_ACCT_CHAR AC, ")
			.append("   CI_ACCT_PER AP ")
			.append(" WHERE AC.ACCT_ID = AP.ACCT_ID ")
			.append(" AND AP.MAIN_CUST_SW   = 'Y' ")
			.append(" AND AP.PER_ID    = :personId ")
			.append(" AND AC.CHAR_TYPE_CD = :dragDaysCharType ")
			.append(" AND AC.EFFDT    = (SELECT MAX(AC2.EFFDT) FROM CI_ACCT_CHAR AC2 WHERE AC2.ACCT_ID = AC.ACCT_ID AND AC2.CHAR_TYPE_CD = AC.CHAR_TYPE_CD AND AC2.EFFDT <= :effectiveDate ) ");

	public static final StringBuilder CUST_DIVISION = new StringBuilder(" from AccountPerson ap, ")
			.append(" Account ac ")
			.append(" where ac.id = ap.id.account.id ")
			.append(" and ap.id.person = :personId ")
			.append(" and ap.isMainCustomer = 'Y' ");

	public static final StringBuilder CUST_BROKER_CHK_CUST = new StringBuilder(" from PersonId perId , CmBrokerInformation bi")
			.append(" where perId.id = :personId ")
			.append(" and perId.isPrimaryId = 'Y' ")
			.append(" and bi.id.cmSourceCustomerId = perId.personIdNumber ");

	public static final StringBuilder CUST_BROKER_CHK_ACCT = new StringBuilder(" from AccountPerson ap , PersonId perId , CmBrokerInformation bi")
			.append(" where ap.id.person = perId.id.person ")
			.append(" and ap.id.account.id =:accountId ")
			.append(" and ap.isMainCustomer='Y' ")
			.append(" and perId.isPrimaryId='Y' ")
			.append(" and bi.id.cmSourceCustomerId=perId.personIdNumber ");

	public static final StringBuilder ACCT_INV_DAY_CHAR = new StringBuilder(" from AccountCharacteristic ac, Account a, ")
			.append(" AccountPerson ap ")
			.append(" where ap.id.person = :personId ")
			.append(" and ap.id.account = ac.id.account ")
			.append(" and ap.isMainCustomer = 'Y' ")
			.append(" and ac.id.characteristicType = :invoiceDayCharType ")
			.append(" and ac.id.effectiveDate <= :processDate ")
			.append(" and a.id = ac.id.account")
			.append(" and a.customerClass.id NOT IN ( ");

	public static final StringBuilder COLL_CLS_CUST = new StringBuilder(" from CmPersonCollection pc, ")
			.append(" AccountPerson ap ")
			.append(" where ap.id.account.id = :accountId ")
			.append(" and ap.id.person = pc.id ")
			.append(" and ap.isMainCustomer = 'Y' ");

	// fetch next bill date
	public static final StringBuilder FETCH_NEXT_BILL_DATE = new StringBuilder("from BillCycleSchedule bcs where bcs.id.billCycle = :billCycle ")
			.append("and bcs.id.windowStartDate > :processDate ");

	public static final StringBuilder FETCH_DLPROC_TRIGG_EVNTS = new StringBuilder(" from CmDelinquencyProcessTriggerEvent te ")
			.append(" where te.id.delinquencyProcess.id =  :deliqProcId ")
			.append(" and te.statusDateTime is null ")
			.append(" and te.id.businessObjectStatus.id.businessObject = :businessObject ")
			.append(" and te.id.businessObjectStatus.id.status in (  ");
}

