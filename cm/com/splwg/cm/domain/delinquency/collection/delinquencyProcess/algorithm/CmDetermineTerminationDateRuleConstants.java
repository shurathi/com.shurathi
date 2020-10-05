/*                                                                
 **************************************************************************                                                                
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Constant File for Manage Delinquency
 * Determine Termination Date  
 *                                                             
 **************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 * 2020-05-06   MugdhaP		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework
 * **************************************************************************
 */
package com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm;

public class CmDetermineTerminationDateRuleConstants {

	public static final String DELINQUENCY_PROCESS_ID = "delinquencyProcessId";
	public static final String PERSON_ID = "personId";
	public static final String ACCOUNT_ID = "accountId";
	public static final String EXTERNAL_SYSTEM_TYPE_FLG = "CMTR";
	public static final String FEATURE_CONFIGURATION = "CMTDRLODPARM";

	public final static StringBuilder RETRIVE_FEATURE_CONFIG_VAL = new StringBuilder()
			.append(" SELECT  WFM.WFM_NAME, COUNT(1) AS COUNT from  CI_WFM WFM ")
			.append(" WHERE WFM.EXT_SYS_TYP_FLG = :extSysType GROUP BY WFM.WFM_NAME ");

	public final static StringBuilder CIS_DIVISION_CHAR_QUERY = new StringBuilder()
			.append(" from CisDivisionCharacteristic divc ")
			.append(" where divc.id.characteristicType=:terminationDateRuleCharType ")
			.append(" AND divc.id.effectiveDate=(select max(divc2.id.effectiveDate) ")
			.append(" from CisDivisionCharacteristic divc2 where divc.id.division=divc2.id.division ")
			.append(" AND divc.id.characteristicType=divc2.id.characteristicType ")
			.append(" AND divc2.id.effectiveDate<=:latestBillDueDate) ")
			.append(" AND divc.id.division IN ( ");

	public final static StringBuilder DET_PRIORITY_TERM_DT_RULE = new StringBuilder()
			.append(" from CisDivisionCharacteristic divc ")
			.append(" where divc.id.characteristicType=:terminationDateRuleCharType ")
			.append(" AND divc.id.effectiveDate=(select max(divc2.id.effectiveDate) ")
			.append(" from CisDivisionCharacteristic divc2 where divc.id.division=divc2.id.division ")
			.append(" AND divc.id.characteristicType=divc2.id.characteristicType ")
			.append(" AND divc2.id.effectiveDate<=:latestBillDueDate) ")
			.append(" AND divc.characteristicValue=:priorityTerminationDateRuleCharVal ")
			.append(" AND divc.id.division IN ( ");

	public final static StringBuilder GET_PRIM_CUST = new StringBuilder()
			.append(" SELECT PER_ID FROM CI_ACCT_PER ACP WHERE ACP.ACCT_ID=:accountId AND ACP.MAIN_CUST_SW='Y' ");

	public final static StringBuilder RET_LATEST_BILL = new StringBuilder()
			.append(" SELECT BI.DUE_DT FROM CM_DELIN_PROC_REL_OBJ RO, CI_BILL BI ")
			.append(" WHERE RO.CM_DELIN_PROC_ID = :delinquencyProcId AND RO.CM_DEL_REL_OBJ_TYPE_FLG = :collectingObjTypeFlg ")
			.append(" AND RO.PK_VALUE1 = BI.BILL_ID ORDER BY BI.DUE_DT DESC ");

	public final static StringBuilder RET_LATEST_BILL_DUE_DT = new StringBuilder()
			.append(" FROM CmDelinquencyProcessRelatedObject RO, Bill BI WHERE RO.id.delinquencyProcess = :delinquencyProcId ")
			.append(" AND RO.id.cmDelinquencyRelatedObjTypeFlg = :collectingObjTypeFlg AND RO.id.primaryKeyValue1 = BI.id ");

	public final static StringBuilder RET_TRIGGER_DATE = new StringBuilder()
			.append(" FROM CmDelinquencyProcessTriggerEvent TEVT WHERE TEVT.id.delinquencyProcess.id = :delinquencyProcessId ")
			.append(" AND TEVT.id.businessObjectStatus.id.status = :boStatusTermRequest ")
			.append(" AND TEVT.id.businessObjectStatus.id.businessObject = :busObjCdDelinqProcess ");

	public final static StringBuilder RET_TRIG_DATE = new StringBuilder()
			.append("SELECT * FROM CM_DELIN_PROC_TRIG_EVT TEVT WHERE TEVT.CM_DELIN_PROC_ID = :delinquencyProcessId AND TEVT.BO_STATUS_CD = :boStatusTermRequest AND TEVT.BUS_OBJ_CD = :busObjCdDelinqProcess");

	public final static StringBuilder RET_END_OF_GRACE = new StringBuilder()
			.append(" FROM CmDelinquencyProcessLog DL1 WHERE DL1.id.delinquencyProcess.id = :delProcId AND DL1.characteristicTypeId = :eogCharType ")
			.append(" AND DL1.id.sequence = ( SELECT MAX(DL2.id.sequence) FROM CmDelinquencyProcessLog DL2 WHERE DL2.id.delinquencyProcess.id = DL1.id.delinquencyProcess.id ")
			.append(" AND DL2.characteristicTypeId = DL1.characteristicTypeId ) ");

	public final static StringBuilder RET_CURRENT_BILL_DUE_DATE = new StringBuilder()
			.append(" SELECT BI.DUE_DT, BI.BILL_ID, FT.MATCH_EVT_ID, SUM(FT.TOT_AMT) AS TOTAL_FT_AMT, (SELECT SUM(FT2.TOT_AMT) ")
			.append(" FROM CI_FT FT2, CI_MATCH_EVT ME WHERE FT.MATCH_EVT_ID = ME.MATCH_EVT_ID AND ME.MATCH_EVT_ID = FT2.MATCH_EVT_ID GROUP BY ME.MATCH_EVT_ID) AS TOTAL_UNPAID_AMT ")
			.append(" FROM CM_DELIN_PROC_REL_OBJ RO, CI_BILL BI, CI_FT FT WHERE RO.CM_DELIN_PROC_ID = :delinquencyProcId AND RO.CM_DEL_REL_OBJ_TYPE_FLG = :collectingObjTypeFlg ")
			.append(" AND RO.PK_VALUE1 = BI.BILL_ID AND RO.PK_VALUE1 = FT.BILL_ID AND FT.BILL_ID = BI.BILL_ID AND (FT.MATCH_EVT_ID = ' ' OR ")
			.append(" (FT.MATCH_EVT_ID != ' ' AND EXISTS ( SELECT 'X' FROM CI_MATCH_EVT ME WHERE ME.MATCH_EVT_ID = FT.MATCH_EVT_ID ")
			.append(" AND ME.MEVT_STATUS_FLG = :openFlg ))) GROUP BY BI.BILL_ID, BI.DUE_DT, FT.MATCH_EVT_ID ORDER BY BI.DUE_DT DESC ");

	public final static StringBuilder FETCH_MAX_COV_END_DATE = new StringBuilder()
			//.append(" FROM FinancialTransaction ft,  CmFinancialTransactionExtension cmft ")
			.append(" FROM FinancialTransaction ft,  FinancialTransactionExtension cmft ")
			.append(" WHERE ft.serviceAgreement.id IN  (select sa.id FROM ServiceAgreement sa ")
			.append(" WHERE sa.account.id=:accountId ) ")
			.append(" AND ft.financialTransactionType IN ( :billSegment , :billCancellation ) AND ft.isFrozen='Y' ")
			.append(" AND cmft.id=ft.id AND cmft.endDate <= :endOfGrace ");
	
	public final static StringBuilder RET_CURRENT_BILL_DUE_DATE_RETRO = new StringBuilder()
			.append(" SELECT BI.DUE_DT, BI.BILL_ID, FT.MATCH_EVT_ID, SUM(FT.TOT_AMT) AS TOTAL_FT_AMT, (SELECT SUM(FT2.TOT_AMT) ")
			.append(" FROM CI_FT FT2, CI_MATCH_EVT ME WHERE FT.MATCH_EVT_ID = ME.MATCH_EVT_ID AND ME.MATCH_EVT_ID = FT2.MATCH_EVT_ID GROUP BY ME.MATCH_EVT_ID) AS TOTAL_UNPAID_AMT ")
			.append(" FROM CM_DELIN_PROC_REL_OBJ RO, CI_BILL BI, CI_FT FT WHERE RO.CM_DELIN_PROC_ID = :delinquencyProcId AND RO.CM_DEL_REL_OBJ_TYPE_FLG = :collectingObjTypeFlg ")
			.append(" AND RO.PK_VALUE1 = BI.BILL_ID AND RO.PK_VALUE1 = FT.BILL_ID AND FT.BILL_ID = BI.BILL_ID AND (FT.MATCH_EVT_ID = ' ' OR ")
			.append(" (FT.MATCH_EVT_ID != ' ' AND EXISTS ( SELECT 'X' FROM CI_MATCH_EVT ME WHERE ME.MATCH_EVT_ID = FT.MATCH_EVT_ID ")
			// .append(" AND ME.MEVT_STATUS_FLG = :openFlg ))) GROUP BY BI.BILL_ID, BI.DUE_DT, FT.MATCH_EVT_ID ORDER BY BI.DUE_DT DESC ");
			.append(" AND ME.MEVT_STATUS_FLG = :openFlg ))) GROUP BY BI.BILL_ID, BI.DUE_DT, FT.MATCH_EVT_ID ORDER BY BI.DUE_DT ASC ");
	public final static StringBuilder RETRIEVE_ACTIVE_POLICIES = new StringBuilder()
			.append(" SELECT DISTINCT PO.POLICY_ID, PO.START_DT,PO.BO_STATUS_CD FROM ")
			.append(" CI_POLICY PO, CI_POLICY_PER PPER, CI_ACCT_PER APER ")
			.append(" WHERE APER.ACCT_ID = :account AND (PO.BO_STATUS_CD = :activeStatus OR (PO.BO_STATUS_CD = :terminatedStatus AND PO.END_DT > :terminationDate)) ")
			.append(" AND PPER.POLICY_ID = PO.POLICY_ID AND PPER.MAIN_CUST_SW = 'Y' ")
			.append(" AND PPER.START_DT = (SELECT MAX(PPER2.START_DT) FROM CI_POLICY_PER PPER2 ")
			.append(" WHERE PPER2.POLICY_ID = PPER.POLICY_ID ")
			.append(" AND MAIN_CUST_SW = 'Y') ")
			.append(" AND APER.PER_ID = PPER.PER_ID AND APER.MAIN_CUST_SW = 'Y' ");

	public final static StringBuilder STATE_OF_ISSUE_CUSTOMER_LVL = new StringBuilder()
			.append(" SELECT DISTINCT PPC.SRCH_CHAR_VAL AS STATE_OF_ISSUE FROM CI_BILL_CHG BC , CI_BILL_CHG_CHAR BCC , ")
			.append(" CI_SA SA, CI_SA_CHAR SAC, CI_POLICY_PLAN PP, CI_POLICY PO, ")
			.append(" F1_BUS_OBJ_STATUS_OPT OPT , ")
			.append(" CI_POLICY_PER PPER , ")
			.append(" CI_ACCT_PER APER , ")
			.append(" CI_POLICY_PLAN_CHAR PPC ")
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

}

