/*
 ************************************************************************************************************
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Constant File for Delinquency Customer Monitor Rule Algorithm
 *                                                             
 ************************************************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 *           
 * 2020-05-06   MugdhaP		ANTHM-340 CAB1-9462 Delinquency Framework
 * **********************************************************************************************************
 */

package com.splwg.cm.domain.delinquency.admin.delinquencyCustomerMonitor;

/**
 * @author MugdhaP
 *
 */
public class CmDelinquencyCustomerMonitorRuleConstants {

	public final static String CONST_A = "A";

	public final static String CONST_B = "B";

	public final static String CONST_C = "C";

	public final static String CONST_D = "D";

	public final static String CONST_N = "N";

	public final static String CONST_P = "P";

	public final static String CONST_S = "S";

	public final static String CONST_Y = "Y";

	public final static String BAND_1 = "Band 1";

	public final static String BAND_2 = "Band 2";

	public final static String BAND_3 = "Band 3";

	public final static String BAND_4 = "Band 4";

	public final static String BAND_5 = "Band 5";

	public final static String BO_STATUS = "status";

	public final static String BO_INITIAL_STATUS = "boStatus";

	public final static String DEL_REL_OBJ_LIST_NODE = "cmDelinquencyProcessRelatedObject";

	public final static String DEL_PROC_ID = "delinquencyProcessId";

	public static final String MAINTENANCE_OBJ_BILL = "BILL";

	public static final String MAINTENANCE_OBJ_PERSON = "PERSON";

	public static final String MAINTENANCE_OBJ_ACCOUNT = "ACCOUNT";

	public static final String RELATED_OBJECT_TYPE_FLAG = "CMCO";

	public static final String DEL_PROC_RELATED_OBJECT_TYPE_FLAG = "CMDL";

	public final static String CREDIT_RATING_DESC = "Credit Rating Value";

	public final static String DELINQUENCY_LEVEL_DESC = "Delinquency Level";

	public final static String DELINQUENCY_LEVEL_DESC_VAL = "'A' or 'B' or 'P'";

	public final static String DATE_DETERMINE_AGE_DESC = "Determine Age Date";

	public final static String DATE_DETERMINE_AGE_DESC_VAL = "'B' or 'D'";

	public final static String BILL_ID_CHAR_TYPE_DESC = "Bill ID Characteristic Type";

	public final static String MSG_CAT_DESC = "Message Category";

	public final static String MSG_NUM_DESC = "Message Number";

	public final static String LMT_CHAR_ENT_DESC = "Limiting Char Entity";

	public final static String LMT_CHAR_TYPE_DESC = "Limiting Char Type";

	public final static String LMT_CHAR_VAL_DESC = "Limiting Char Value";

	public final static String LMT_CHAR_EFF_WITHIN_XDAYS_DESC = "Limiting Char Effective Within X Days";

	public final static String POST_DT_CHAR_TYPE_DESC = "Postpone Date Char Type";

	public final static String DELINQ_PROC_TYPE_DESC = "Delinquency Process Type";

	public final static String DT_FORMAT = "yyyy-MM-dd";

	public final static String PROC_BIIL_WITH_UNPAID_AMT_PARM_DESC = "Process Bills with unpaid Amount";

	public final static String UNPAID_AMT_AND_PER_PARM_DESC = "Unpaid Amount and Percentage";

	public final static String PROC_BIIL_WITH_AN_AGE_PARM_DESC = "Process Bills with an Age";

	public final static String PROC_BIIL_WITH_UNPAID_PER_PARM_DESC = "Process Bills with unpaid Percentage";

	public final static String CUSTOMER_STATUS_PARM_DESC = "Customer Status";

	public final static String OPT_TYP_PARM = "Customer Status Option Type";

	public final static String OPT_VAL_PARM = "Customer Status Option Value";

	public final static StringBuilder FETCH_CUSTOMERS_HAVING_NEXT_CREDIT_REVIEW_ON_PROCESS_DATE = new StringBuilder()
			.append(" SELECT DISTINCT CRS.PER_ID, PC.COLL_CL_CD FROM CM_CUS_RVW_SCH CRS, CM_PER_COLL PC WHERE CRS.NEXT_CR_RVW_DT <= :batchProcessDate AND CRS.PER_ID = PC.PER_ID ");

	public final static StringBuilder FETCH_CUSTOMERS_NOT_MONITORED = new StringBuilder()
			.append(" SELECT PER_ID, COLL_CL_CD FROM ( SELECT DISTINCT PC.PER_ID PER_ID, (PC.CR_REVIEW_DT + DC.CM_MIN_CR_RVW_FREQ) CALC_DATE, PC.COLL_CL_CD COLL_CL_CD ")
			.append(" FROM CM_PER_COLL PC, CM_DELIN_CNTL DC WHERE DC.COLL_CL_CD = PC.COLL_CL_CD AND (PC.POSTPONE_CR_RVW_DT IS NULL OR PC.POSTPONE_CR_RVW_DT <= :batchProcessDate) ")
			.append(" AND PC.CR_REVIEW_DT IS NOT NULL) WHERE CALC_DATE <= :batchProcessDate ");

	public final static StringBuilder PER_CRT_RATE_SQL = new StringBuilder()
			.append(" SELECT NVL(SUM(HI.CR_RATING_PTS), 0) CR_RATING_PTS, NVL(SUM(HI.CASH_ONLY_PTS), 0) CASH_ONLY_PTS FROM CI_CR_RAT_HIST HI, CI_ACCT_PER AP ")
			.append(" WHERE HI.START_DT <= :processDate AND (HI.END_DT IS NULL OR HI.END_DT >= :processDate) ")
			.append(" AND HI.ACCT_ID = AP.ACCT_ID AND AP.PER_ID = :personId AND AP.MAIN_CUST_SW = 'Y' ");

	public final static StringBuilder ACCT_CRT_RATE_SQL = new StringBuilder()
			.append(" SELECT NVL(SUM(HI.CR_RATING_PTS), 0) CR_RATING_PTS FROM CI_CR_RAT_HIST HI ")
			.append(" WHERE HI.START_DT <= :processDate AND (HI.END_DT IS NULL OR HI.END_DT >= :processDate) AND HI.ACCT_ID = :accountId ");

	public final static StringBuilder GET_CUST_ACCT = new StringBuilder()
			.append(" SELECT ACCT_ID FROM CI_ACCT_PER ACP ")
			.append(" WHERE ACP.PER_ID = :personId AND ACP.MAIN_CUST_SW = 'Y' ");

	public final static StringBuilder CRT_RATE_THS_INST_OPTS_SQL = new StringBuilder()
			.append(" SELECT INS.CR_RAT_THRS, INS.CASH_ONLY_PTS_THRS FROM C0_INSTALLATION INS ");

	public final static StringBuilder LMT_CHAR_FOR_PER_SQL = new StringBuilder()
			.append(" SELECT DISTINCT 'X' FROM CI_PER_CHAR CH ")
			.append(" WHERE CH.PER_ID = :personId AND CH.CHAR_TYPE_CD = :limitingCharTypeAlgoParm AND CH.EFFDT >= :effectiveDate AND ")
			.append(" ((CH.CHAR_VAL = :limitingCharValAlgoParm AND (SELECT CHAR_TYPE_FLG FROM CI_CHAR_TYPE WHERE CHAR_TYPE_CD = :limitingCharTypeAlgoParm) = 'DFV') OR ")
			.append(" (CH.ADHOC_CHAR_VAL = :limitingCharValAlgoParm AND (SELECT CHAR_TYPE_FLG FROM CI_CHAR_TYPE WHERE CHAR_TYPE_CD = :limitingCharTypeAlgoParm) = 'ADV')) ");

	public final static StringBuilder LMT_CHAR_FOR_ACCT_SQL = new StringBuilder()
			.append(" SELECT DISTINCT 'X' FROM CI_ACCT_CHAR CH ")
			.append(" WHERE CH.ACCT_ID = :accountId AND CH.CHAR_TYPE_CD = :limitingCharTypeAlgoParm AND CH.EFFDT >= :effectiveDate AND ")
			.append(" ((CH.CHAR_VAL = :limitingCharValAlgoParm AND (SELECT CHAR_TYPE_FLG FROM CI_CHAR_TYPE WHERE CHAR_TYPE_CD = :limitingCharTypeAlgoParm) = 'DFV') OR ")
			.append(" (CH.ADHOC_CHAR_VAL = :limitingCharValAlgoParm AND (SELECT CHAR_TYPE_FLG FROM CI_CHAR_TYPE WHERE CHAR_TYPE_CD = :limitingCharTypeAlgoParm) = 'ADV'))");

	public final static StringBuilder LMT_CHAR_FOR_SA_SQL = new StringBuilder()
			.append(" SELECT DISTINCT 'X' FROM CI_SA_CHAR CH, CI_SA SA WHERE SA.ACCT_ID = :accountId AND CH.SA_ID = SA.SA_ID ")
			.append(" AND CH.CHAR_TYPE_CD = :limitingCharTypeAlgoParm AND CH.EFFDT >= :effectiveDate AND ")
			.append(" ((CH.SRCH_CHAR_VAL = :limitingCharValAlgoParm AND (SELECT CHAR_TYPE_FLG FROM CI_CHAR_TYPE WHERE CHAR_TYPE_CD = :limitingCharTypeAlgoParm) = 'DFV') OR ")
			.append(" (CH.SRCH_CHAR_VAL = :limitingCharValAlgoParm AND (SELECT CHAR_TYPE_FLG FROM CI_CHAR_TYPE WHERE CHAR_TYPE_CD = :limitingCharTypeAlgoParm) = 'ADV'))");

	public final static StringBuilder PP_CRT_REW_DT_SQL = new StringBuilder()
			.append(" SELECT BC.SRCH_CHAR_VAL, BC.ADHOC_CHAR_VAL, CT.CHAR_TYPE_FLG FROM CI_BILL_CHAR BC, CI_CHAR_TYPE CT ")
			.append(" WHERE BC.BILL_ID = :billId AND BC.CHAR_TYPE_CD = :postPoneDateCharType AND BC.CHAR_TYPE_CD = CT.CHAR_TYPE_CD ");

	public final static StringBuilder GET_COMPLT_BILL_CUST_SQL = new StringBuilder()
			.append(" SELECT B.BILL_ID, B.DUE_DT, B.BILL_DT FROM CI_BILL B, CI_ACCT_PER AP ")
			.append(" WHERE B.ACCT_ID = AP.ACCT_ID AND AP.PER_ID = :personId AND AP.MAIN_CUST_SW = 'Y' AND B.BILL_STAT_FLG = 'C' ")
			.append(" AND NOT EXISTS (SELECT DISTINCT 'X' FROM CM_DELIN_PROC_REL_OBJ DPO, CM_DELIN_PROC DP, F1_BUS_OBJ_STATUS BOS ")
			.append(" WHERE DP.CM_DELIN_PROC_ID = DPO.CM_DELIN_PROC_ID AND DPO.CM_DEL_REL_OBJ_TYPE_FLG = 'CMCO' AND DPO.MAINT_OBJ_CD = 'BILL' ")
			.append(" AND DPO.PK_VALUE1 = B.BILL_ID AND DP.BUS_OBJ_CD = BOS.BUS_OBJ_CD AND DP.BO_STATUS_CD = BOS.BO_STATUS_CD AND BOS.BO_STATUS_COND_FLG <> 'F1FL') ")
			.append(" AND EXISTS (SELECT 'X' FROM CI_FT FT WHERE (FT.MATCH_EVT_ID = ' ' OR ( FT.MATCH_EVT_ID <> ' ' AND EXISTS (SELECT 'X' ")
			.append(" FROM CI_MATCH_EVT MEVT WHERE MEVT.MATCH_EVT_ID = FT.MATCH_EVT_ID AND MEVT.MEVT_STATUS_FLG = 'O')) ) ")
			.append(" AND FT.BILL_ID = B.BILL_ID) ");

	public final static StringBuilder GET_COMPLT_BILL_ACCT_SQL = new StringBuilder()
			.append(" SELECT B.BILL_ID, B.DUE_DT, B.BILL_DT FROM CI_BILL B ")
			.append(" WHERE B.ACCT_ID = :accountId AND B.BILL_STAT_FLG = 'C' ")
			.append(" AND NOT EXISTS (SELECT DISTINCT 'X' FROM CM_DELIN_PROC_REL_OBJ DPO, CM_DELIN_PROC DP, F1_BUS_OBJ_STATUS BOS ")
			.append(" WHERE DP.CM_DELIN_PROC_ID = DPO.CM_DELIN_PROC_ID AND DPO.CM_DEL_REL_OBJ_TYPE_FLG = 'CMCO' AND DPO.MAINT_OBJ_CD = 'BILL' ")
			.append(" AND DPO.PK_VALUE1 = B.BILL_ID AND DP.BUS_OBJ_CD = BOS.BUS_OBJ_CD AND DP.BO_STATUS_CD = BOS.BO_STATUS_CD AND BOS.BO_STATUS_COND_FLG <> 'F1FL' ) ")
			.append(" AND EXISTS (SELECT 'X' FROM CI_FT FT, CI_MATCH_EVT ME WHERE (FT.MATCH_EVT_ID = ' ' OR (FT.MATCH_EVT_ID = ME.MATCH_EVT_ID ")
			.append(" AND ME.MEVT_STATUS_FLG = 'O')) AND FT.BILL_ID = B.BILL_ID ) ");

	public final static StringBuilder EXISTS_DELINQ_PROC_PER_LVL = new StringBuilder()
			.append(" SELECT DP.CM_DELIN_PROC_ID FROM CM_DELIN_PROC_REL_OBJ DPO, CM_DELIN_PROC DP, F1_BUS_OBJ_STATUS BOS ")
			.append(" WHERE DP.CM_DELIN_PROC_ID = DPO.CM_DELIN_PROC_ID AND DPO.MAINT_OBJ_CD = 'PERSON' AND DPO.CM_DEL_REL_OBJ_TYPE_FLG = 'CMDL' ")
			.append(" AND DPO.PK_VALUE1 = :personId AND DP.CM_DELIN_PROC_TYP_CD = :delinProcTypeCd AND DP.BUS_OBJ_CD = BOS.BUS_OBJ_CD ")
			.append(" AND DP.BO_STATUS_CD = BOS.BO_STATUS_CD AND BOS.BO_STATUS_COND_FLG <> 'F1FL' ");

	public final static StringBuilder EXISTS_DELINQ_PROC_ACCT_LVL = new StringBuilder()
			.append(" SELECT DP.CM_DELIN_PROC_ID FROM CM_DELIN_PROC_REL_OBJ DPO, CM_DELIN_PROC DP, F1_BUS_OBJ_STATUS BOS ")
			.append(" WHERE DP.CM_DELIN_PROC_ID = DPO.CM_DELIN_PROC_ID AND DPO.MAINT_OBJ_CD = 'ACCOUNT' AND DPO.CM_DEL_REL_OBJ_TYPE_FLG = 'CMDL' ")
			.append(" AND DPO.PK_VALUE1 = :accountId AND DP.CM_DELIN_PROC_TYP_CD = :delinProcTypeCd AND DP.BUS_OBJ_CD = BOS.BUS_OBJ_CD ")
			.append(" AND DP.BO_STATUS_CD = BOS.BO_STATUS_CD AND BOS.BO_STATUS_COND_FLG <> 'F1FL' ");

	public final static StringBuilder FETCH_DELINQ_PROC_TYP_ALG = new StringBuilder()
			.append(" FROM CmDelinquencyProcessTypeAlgorithm DPTA WHERE DPTA.id.cmDelinquencyProcessType = :delinquencyProcessType  ")
			.append(" AND DPTA.id.cmDelinquencyProcessTypeSystemEvent = 'CMCA' ");

	public final static StringBuilder ON_ACCOUNT_PAYMENTS_QUERY_CUSTOMER_LVL = new StringBuilder()
			.append(" FROM FinancialTransaction FT, ServiceAgreement SA, AccountPerson AP WHERE SA.account = AP.id.account ")
			.append(" AND AP.id.person = :personId AND AP.isMainCustomer = 'Y' AND EXISTS ( SELECT WO.id FROM FeatureConfigurationOption WO ")
			.append(" WHERE WO.id.workforceManagementSystem = :adminContrFeatureConfig AND WO.id.optionType = :adminContrOptionType AND SA.serviceAgreementType.id.saType = RPAD(WO.value,8)  ) ")
			.append(" AND FT.serviceAgreement = SA.id AND FT.isFrozen = 'Y' ");

	public final static StringBuilder TOT_BILLED_AMOUNT_CUSTOMER_LVL = new StringBuilder()
			.append(" FROM FinancialTransaction FT, Bill BL, FinancialTransactionExtension CMFT, AccountPerson AP WHERE BL.account = AP.id.account ")
			.append(" AND AP.id.person = :personId AND AP.isMainCustomer = 'Y' AND BL.billStatus = 'C' AND BL.dueDate = :latestDueDate AND FT.billId = BL.id ")
			.append(" AND FT.isFrozen = 'Y' AND FT.shouldShowOnBill = 'Y' AND FT.financialTransactionType IN ('AD', 'AX', 'BS', 'BX') AND CMFT.id = FT.id AND CMFT.startDate <= BL.dueDate ");

	public final static StringBuilder ON_ACCOUNT_PAYMENTS_QUERY_ACCOUNT_LVL = new StringBuilder()
			.append(" FROM FinancialTransaction FT, ServiceAgreement SA WHERE SA.account = :account AND FT.serviceAgreement = SA.id ")
			.append(" AND FT.isFrozen = 'Y' AND EXISTS (SELECT WO.id FROM FeatureConfigurationOption WO WHERE WO.id.workforceManagementSystem = :adminContrFeatureConfig  ")
			.append(" AND WO.id.optionType = :adminContrOptionType AND SA.serviceAgreementType.id.saType = RPAD(WO.value,8) ) ");

	public final static StringBuilder TOT_BILLED_AMOUNT_ACCOUNT_LVL = new StringBuilder()
			.append(" FROM FinancialTransaction FT, Bill BL, FinancialTransactionExtension CMFT WHERE BL.account = :account AND BL.billStatus = 'C' ")
			.append(" AND BL.dueDate = :latestDueDate AND FT.billId = BL.id AND FT.isFrozen = 'Y' AND FT.shouldShowOnBill = 'Y' AND FT.financialTransactionType IN ('AD', 'AX', 'BS', 'BX') ")
			.append(" AND CMFT.id = FT.id AND CMFT.startDate <= BL.dueDate ");

	public final static StringBuilder DETERMINE_DEL_LVL = new StringBuilder()
			.append(" FROM CmDelinquencyProcessRelatedObject DPRO WHERE DPRO.id.cmDelinquencyRelatedObjTypeFlg = :relObjTypeFlg AND DPRO.id.delinquencyProcess = :delinProcId ");

	public final static StringBuilder EXIST_CUST_CRED_REVW_SCHDULE = new StringBuilder()
			.append(" SELECT PER_ID, NEXT_CR_RVW_DT FROM CM_CUS_RVW_SCH WHERE PER_ID = :personId AND NEXT_CR_RVW_DT = :retrievedAdhocCharVal ");

	public final static StringBuilder RET_BILL_DEL_PROC_REL_OBJ = new StringBuilder()
			.append(" SELECT DPRO.PK_VALUE1 FROM CM_DELIN_PROC_REL_OBJ DPRO WHERE DPRO.MAINT_OBJ_CD = 'BILL' AND DPRO.CM_DEL_REL_OBJ_TYPE_FLG = 'CMCO' AND DPRO.CM_DELIN_PROC_ID = :delinProcId ");

	public final static StringBuilder CHK_DEL_PROC_CUST_LVL = new StringBuilder()
			.append(" FROM Bill B, AccountPerson AP WHERE AP.id.account = B.account AND AP.id.person = :person_id AND AP.isMainCustomer = 'Y' ")
			.append(" AND B.billStatus = 'C' AND B.id = :bill_id ");

	public final static StringBuilder CHK_DEL_PROC_ACCT_LVL = new StringBuilder()
			.append(" FROM Bill B WHERE B.account = :account_id AND B.billStatus = 'C' AND B.id = :bill_id ");

	public final static StringBuilder CHECK_FOR_LIMITING_CHAR = new StringBuilder()
			.append("	from PersonCharacteristic personChar		")
			.append("	where personChar.id.person.id = :personId		")
			.append("	and personChar.id.characteristicType = :limitingCharType		")
			.append("	and personChar.id.effectiveDate >= :limitingCharEffDate		")
			.append("	and personChar.id.effectiveDate = (		")
			.append("			select max(personChar2.id.effectiveDate)	")
			.append("			from PersonCharacteristic personChar2	")
			.append("			where personChar2.id.characteristicType = personChar.id.characteristicType	")
			.append("			and personChar2.id.person = personChar.id.person	")
			.append("			and personChar2.id.effectiveDate <= :processDate	")
			.append("		)		");
	public static int SYSTEMATIC_STMT_DAY_LOWER_LIMIT = 1;
	public static int SYSTEMATIC_STMT_DAY_UPPER_LIMIT = 28;

}
