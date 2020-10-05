/*                                                          
 **************************************************************************                                                                
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Define constants, 
 *                                                             
 **************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    	 Reason:                                     
 * YYYY-MM-DD  	IN     	 Reason text.                                
 * 2020-05-06   VINODW	Initial version : ANTHM-340 CAB1-9462 Delinquency Framework
 **************************************************************************
 */

package com.splwg.cm.domain.delinquency.admin.overdueEventType;

import java.math.BigInteger;

public class CmOverdueProcessConstants {

	public final static StringBuilder OVERDUE_EVENT_ACCT_REL_NOT_PROVIDED_CREATE_CUST_CONTACT = new StringBuilder(
			"	SELECT AP.PER_ID ")
			.append(" FROM   CI_ACCT_PER AP  , CI_OD_PROC OP ")
			.append(" WHERE  OP.OD_PROC_ID = :overdueProcessId")
			.append(" AND AP.ACCT_ID = OP.ACCT_ID")
			.append(" AND AP.NOTIFY_SW = 'Y' ");

	public final static StringBuilder OVERDUE_EVENT_ACCT_REL_EXISTS_CREATE_CUST_CONTACT = new StringBuilder(
			"	SELECT AP.PER_ID ")
			.append(" FROM   CI_ACCT_PER AP  , CI_OD_PROC OP ")
			.append(" WHERE  OP.OD_PROC_ID = :overdueProcessId")
			.append(" AND AP.ACCT_ID = OP.ACCT_ID")
			.append(" AND AP.NOTIFY_SW = 'Y' ")
			.append("AND AP.ACCT_REL_TYPE_CD IN (:acRelTy1, :acRelTy2, :acRelTy3, :acRelTy4, :acRelTy5)");

	public final static StringBuilder VALIDATE_CHAR_TYPE = new StringBuilder(
			"	SELECT CT.CHAR_TYPE_FLG ")
			.append(" FROM   CI_CHAR_TYPE CT , CI_FK_REF FR ")
			.append(" WHERE  CT.CHAR_TYPE_FLG = 'FKV' ")
			.append("  AND CT.CHAR_TYPE_CD = :charTypeCode ")
			.append(" AND FR.FK_REF_CD = CT.FK_REF_CD ")
			.append(" AND FR.TBL_NAME = :tableName ");

	public static final String FEATURE_CONFIGURATION = "CMTDODPARM";
	public static final String EXTERNAL_SYSTEM_LOOKUP = "CMTD";
	public static final String LOKUP_FIELD = "CMTD_OPT_TYP_FLG";

	public static final String XPATH_BO_STATUS = "";

	public static final String BILL_CHAR_TYPE = "BLID";
	public static final String DATE_OF_PROC_RULE_CHAR_VALUE = "DTPV";
	public static final String END_OF_GR_CHAR_VALUE = "EOGV";
	public static final String END_OF_GR_RULE_CHAR_TYPE = "EOGC";
	public static final String TER_DAT_CHAR = "TDRL";
	public static final String RETRO_PAID_CHAR_VALUE = "RPDV";
	public static final String POLICY_ACTIVE_STATUS = "POAC";
	public static final String MEM_CHAR_TYPE = "MBRS";
	public static final String STATE_OF_ISS_CHAR_TYPE = "STIS";

	public static final BigInteger MESSAGE_CATEGORY = new BigInteger("11001");
	public static final Integer MESSAGE_NUMBER = new Integer("1020");

	public static final int CHAR_TYPE_REQUIRED = 34030;
	public static final int MISSING_CONFIGURATION = 34031;
	public static final int CHAR_TYPE_REQUIRED_ON_STATE = 34032;
	public static final int CHAR_TYPE_REQUIRED_ON_OVD_PROC = 34033;
	public static final int UNABLE_TO_PROCESS = 34034;
	public static final int MISSING_FEATURE_CONFIG = 34035;
	public static final int MISSING_FEATURE_CONFIG_OPT_TY = 34036;
	public static final int MULTIPLE_FEATURE_CONFIG_OPT_TY = 34037;
	public static final int MULTIPLE_FEATURE_CONFIG = 34038;

	public final static StringBuilder LATEST_BILL_DUE_DATE = new StringBuilder()
			.append(" SELECT MAX(BL.DUE_DT) AS DUE_DT FROM   CI_BILL BL, CI_OD_PROC_OBJ ODO ")
			.append(" WHERE  ODO.OD_PROC_ID = :overdueProcessId ")
			.append(" AND ODO.CHAR_TYPE_CD = :billCharType ")
			.append(" AND BL.BILL_ID = ODO.CHAR_VAL_FK1 ");

	// Updated query to consider future termed policies as active
	public final static StringBuilder RETRIEVE_ACTIVE_POLICIES = new StringBuilder()
			.append(" SELECT DISTINCT PO.POLICY_ID, PO.START_DT,PO.BO_STATUS_CD FROM ")
			
			//.append(" CI_BILL_CHG BC, CI_BILL_CHG_CHAR BCC, CI_SA SA, CI_MEMBERSHIP MBR, CI_POLICY_PLAN PP, CI_POLICY PO, CI_POLICY_PER PPER, CI_ACCT_PER APER ")
			.append(" CI_POLICY PO, CI_POLICY_PER PPER, CI_ACCT_PER APER ")
		
			// .append(" WHERE  SA.ACCT_ID = :account AND BC.SA_ID = SA.SA_ID  AND BC.BILLABLE_CHG_STAT = :billable AND BC.END_DT >= :terminationDate ")
			// .append(" AND BCC.BILLABLE_CHG_ID = BC.BILLABLE_CHG_ID AND BCC.CHAR_TYPE_CD = :mbrshipCharType AND MBR.MEMBERSHIP_ID = BCC.SRCH_CHAR_VAL AND PP.PLAN_ID = MBR.PLAN_ID AND PP.POLICY_ID = PO.POLICY_ID ")
		
			//.append(" WHERE  SA.ACCT_ID = :account AND BC.SA_ID = SA.SA_ID   AND BCC.BILLABLE_CHG_ID = BC.BILLABLE_CHG_ID AND BCC.CHAR_TYPE_CD = :mbrshipCharType ")
			//.append("  AND MBR.MEMBERSHIP_ID = BCC.SRCH_CHAR_VAL AND ((BC.END_DT >= :terminationDate and BC.BILLABLE_CHG_STAT = '10') OR (BC.END_DT = (SELECT MAX(BC2.END_DT)")
			//.append(" FROM   CI_BILL_CHG BC2, CI_BILL_CHG_CHAR BCC2 , CI_MEMBERSHIP MBR2 WHERE  BC2.BILLABLE_CHG_STAT in ('20','10')  AND BCC2.BILLABLE_CHG_ID = BC2.BILLABLE_CHG_ID ")
			//.append(" AND BCC2.CHAR_TYPE_CD = :mbrshipCharType  AND BCC2.SRCH_CHAR_VAL = MBR2.MEMBERSHIP_ID AND MBR2.PLAN_ID = PP.PLAN_ID))) ")
			//.append(" AND PP.PLAN_ID = MBR.PLAN_ID AND PP.POLICY_ID = PO.POLICY_ID ")	

			.append(" WHERE APER.ACCT_ID = :account AND (PO.BO_STATUS_CD = :activeStatus OR (PO.BO_STATUS_CD = :terminatedStatus AND PO.END_DT > :terminationDate)) ")
			.append(" AND PPER.POLICY_ID = PO.POLICY_ID AND PPER.MAIN_CUST_SW = 'Y' ")

			.append(" AND PPER.START_DT = (SELECT MAX(PPER2.START_DT) FROM CI_POLICY_PER PPER2 ")
			.append(" WHERE PPER2.POLICY_ID = PPER.POLICY_ID ")
            .append(" AND MAIN_CUST_SW = 'Y') ")
    
			.append("AND APER.PER_ID = PPER.PER_ID AND APER.MAIN_CUST_SW = 'Y' ");


	public final static StringBuilder RETRIVE_FEATURE_CONFIG_VAL = new StringBuilder()
			.append(" SELECT  WFM.WFM_NAME, COUNT(1) AS COUNT from  CI_WFM WFM ")
			.append(" WHERE WFM.EXT_SYS_TYP_FLG = :extSysType GROUP BY WFM.WFM_NAME ");

	public final static StringBuilder END_OF_GRACE_RULE = new StringBuilder()
			.append(" SELECT  OPL.ADHOC_CHAR_VAL FROM    CI_OD_PROC_LOG OPL ")
			.append(" WHERE   OPL.OD_PROC_ID = :overdueProcessId AND OPL.CHAR_TYPE_CD = :endOfGraceRuleCharType ")
			.append(" AND OPL.LOG_SEQ = (SELECT MAX (OPL2.LOG_SEQ) FROM   CI_OD_PROC_LOG OPL2 ")
			.append(" WHERE  OPL2.OD_PROC_ID = OPL.OD_PROC_ID AND OPL2.CHAR_TYPE_CD = OPL.CHAR_TYPE_CD) ");

	public final static StringBuilder LATEST_COV_PER_END_DT = new StringBuilder()
			.append(" SELECT  MAX(BSEG.END_DT) AS END_DT FROM  CI_BSEG BSEG,  CI_SA SA ")
			.append(" WHERE   SA.ACCT_ID = :account AND BSEG.SA_ID = SA.SA_ID AND BSEG.BSEG_STAT_FLG = :frozen AND BSEG.BILL_ID IN (SELECT  BLPAID.BILL_ID ")
			.append(" FROM (SELECT  BLCRME.BILL_ID, SUM(BLCRME.ORIG_AMT) AS ORIG_AMT, SUM(BLCRME.UNPAID_AMT) AS UNPAID_AMT FROM (SELECT   BL.BILL_ID, FT.MATCH_EVT_ID ")
			.append(" , CASE WHEN FT.MATCH_EVT_ID = ' ' THEN SUM(FT.CUR_AMT) WHEN (SELECT MEVT.MEVT_STATUS_FLG FROM CI_MATCH_EVT MEVT WHERE  MEVT.MATCH_EVT_ID = FT.MATCH_EVT_ID) = 'B' ")
			.append(" THEN 0 ELSE (SELECT SUM(FT2.CUR_AMT) FROM CI_FT FT2 WHERE  FT2.MATCH_EVT_ID = FT.MATCH_EVT_ID AND FT2.FREEZE_SW = 'Y') END AS UNPAID_AMT ")
			.append(" , SUM(FT.CUR_AMT) AS ORIG_AMT FROM    CI_FT FT, CI_BILL BL WHERE   BL.ACCT_ID = :account AND BL.BILL_STAT_FLG = :complete AND FT.BILL_ID = BL.BILL_ID ")
			.append(" AND FT.FREEZE_SW = 'Y' AND FT.FT_TYPE_FLG IN ('BS','BX','AD','AX') GROUP BY BL.BILL_ID, FT.MATCH_EVT_ID) BLCRME ")
			.append(" GROUP BY BLCRME.BILL_ID) BLPAID WHERE BLPAID.UNPAID_AMT = 0) ");



	public final static StringBuilder COUNT_POLICIES = new StringBuilder()
			.append(" SELECT COUNT(1) AS POLICY_CNT FROM   CI_OD_PROC_LOG OPL, CI_POLICY PO ")
			.append(" WHERE  OPL.OD_PROC_ID = :overdueProcess ")
			.append(" AND OPL.OD_EVT_SEQ = :eventSeq AND OPL.CHAR_TYPE_CD = :policyCharType ")
			.append(" AND PO.POLICY_ID = OPL.CHAR_VAL_FK1 AND (PO.BO_STATUS_CD = :pendingTerminationStatus ")
			.append(" OR PO.BO_STATUS_CD = :pendingTermOverrideStatus )");

	public final static StringBuilder RETRIEVE_MIN_START_DT = new StringBuilder()
			.append(" SELECT MIN(PO.START_DT) AS START_DT FROM   CI_BILL_CHG BC, CI_BILL_CHG_CHAR BCC, CI_SA SA, CI_MEMBERSHIP MBR ,CI_POLICY_PLAN PP, CI_POLICY PO, CI_POLICY_PER PPER, CI_ACCT_PER APER ")
			.append(" WHERE  SA.ACCT_ID = :account AND BC.SA_ID = SA.SA_ID  AND BC.BILLABLE_CHG_STAT = :billable AND BC.START_DT <= :latestDueDt AND BC.END_DT >= :latestDueDt ")
			.append(" AND BCC.BILLABLE_CHG_ID = BC.BILLABLE_CHG_ID AND BCC.CHAR_TYPE_CD = :mbrshipCharType AND MBR.MEMBERSHIP_ID = BCC.SRCH_CHAR_VAL AND PP.PLAN_ID = MBR.PLAN_ID AND PP.POLICY_ID = PO.POLICY_ID ")
			.append(" AND PO.BO_STATUS_CD = :activeStatus AND PPER.POLICY_ID = PO.POLICY_ID AND PPER.MAIN_CUST_SW = 'Y' AND APER.PER_ID = PPER.PER_ID AND APER.MAIN_CUST_SW = 'Y' AND APER.ACCT_ID = SA.ACCT_ID ");

	public static final String EXTERNAL_SYSTEM_TYPE_FLG = "CMTR";

	public final static StringBuilder FETCH_LATEST_BILL_DUE_DT = new StringBuilder()
			.append(" from Bill bl,OverdueProcessObjectsBeingCollectedOn odo where odo.id.overdueProcess=:overdueProcess ")
			.append(" AND odo.characteristicType=:billCharType AND odo.characteristicValueForeignKey1=bl.id ");

	public final static StringBuilder FETCH_STATE_OF_ISSUE = new StringBuilder()
			.append(" SELECT DISTINCT PCH.SRCH_CHAR_VAL AS STATE_OF_ISSUE ")
			.append(" FROM   CI_BILL_CHG BC ")
			.append(" , CI_BILL_CHG_CHAR BCC ")
			.append(" , CI_SA SA ")
			.append(" , CI_MEMBERSHIP MBR ")
			.append(" , CI_POLICY_PLAN PP ")
			.append(" , CI_POLICY PO ")
			.append(" , F1_BUS_OBJ_STATUS_OPT OPT")
			.append(" , CI_POLICY_PER PPER ")
			.append(" , CI_ACCT_PER APER ")
			.append(" , CI_POLICY_CHAR PCH ")
			.append(" WHERE  SA.ACCT_ID =:accountId ")
			.append(" AND BC.SA_ID = SA.SA_ID  ")
			.append(" AND BC.BILLABLE_CHG_STAT =:active ")
			.append(" AND BC.START_DT <=:latestBillDueDate ")
			.append(" AND BC.END_DT >=:latestBillDueDate ")
			.append(" AND BCC.BILLABLE_CHG_ID = BC.BILLABLE_CHG_ID ")
			.append(" AND BCC.CHAR_TYPE_CD =:membershipCharType ")
			.append(" AND MBR.MEMBERSHIP_ID = BCC.SRCH_CHAR_VAL ")
			.append(" AND PP.PLAN_ID = MBR.PLAN_ID ")
			.append(" AND PP.POLICY_ID = PO.POLICY_ID ")
			.append(" AND OPT.BUS_OBJ_CD = PO.BUS_OBJ_CD ")
			.append(" AND OPT.BO_STATUS_CD = PO.BO_STATUS_CD ")
			.append(" AND OPT.BO_OPT_FLG  =:policyStatusOptionType ")
			.append(" AND OPT.BO_OPT_VAL  =:policyStatusActiveOptionVal ")
			.append(" AND OPT.SEQ_NUM = (SELECT MAX (OPT2.SEQ_NUM) ")
			.append(" FROM   F1_BUS_OBJ_STATUS_OPT OPT2 ")
			.append(" WHERE  OPT2.BUS_OBJ_CD = OPT.BUS_OBJ_CD ")
			.append(" AND OPT2.BO_STATUS_CD = OPT.BO_STATUS_CD ")
			.append(" AND OPT2.BO_OPT_FLG = OPT.BO_OPT_FLG) ")
			.append(" AND PPER.POLICY_ID = PO.POLICY_ID ")
			.append(" AND PPER.MAIN_CUST_SW = 'Y' ")
			.append(" AND APER.PER_ID = PPER.PER_ID ")
			.append(" AND APER.MAIN_CUST_SW = 'Y' ")
			.append(" AND APER.ACCT_ID = SA.ACCT_ID ")
			.append(" AND PCH.POLICY_ID = PO.POLICY_ID ")
			.append(" AND PCH.CHAR_TYPE_CD =:stateOfIssueCharType ")
			.append(" AND PCH.EFFDT = (SELECT MAX(PCH2.EFFDT) ")
			.append(" FROM   CI_POLICY_CHAR PCH2 ")
			.append(" WHERE  PCH2.POLICY_ID = PCH.POLICY_ID ")
			.append(" AND PCH2.CHAR_TYPE_CD = PCH.CHAR_TYPE_CD ")
			.append(" AND PCH2.EFFDT ")
			.append(" <=:latestBillDueDate) ");

	public final static StringBuilder DET_PRIORITY_TERM_DT_RULE = new StringBuilder()
			.append(" from CisDivisionCharacteristic divc ")
			.append(" where divc.id.characteristicType=:terminationDateRuleCharType ")
			.append(" AND divc.id.effectiveDate=(select max(divc2.id.effectiveDate) ")
			.append(" from CisDivisionCharacteristic divc2 where divc.id.division=divc2.id.division ")
			.append(" AND divc.id.characteristicType=divc2.id.characteristicType ")
			.append(" AND divc2.id.effectiveDate<=:latestBillDueDate) ")
			.append(" AND divc.characteristicValue=:priorityTerminationDateRuleCharVal ")
			.append(" AND divc.id.division IN ( ");

	public final static StringBuilder RETRIEVE_TRIGGER_DATE = new StringBuilder()
			.append(" from OverdueProcessEvent oe ")
			.append(" where oe.id.overdueProcess.id=:overdueProcessId ")
			.append(" AND oe.overdueEventTypeCode=:termReqOverdueEventType ")
			.append(" AND oe.overdueEventStatus=:pending ");

	public final static StringBuilder RETRIEVE_END_OF_GRACE_VAL = new StringBuilder()
			.append(" from OverdueProcessLog opl ")
			.append(" where opl.id.overdueProcess.id=:overduProcessId ")
			.append(" AND opl.characteristicTypeId=:eogCharType ")
			.append(" AND opl.id.logSequenceNumber=( select max(opl2.id.logSequenceNumber) ")
			.append(" from OverdueProcessLog opl2 where opl2.id.overdueProcess.id=opl.id.overdueProcess.id ")
			.append(" AND opl2.characteristicTypeId=opl.characteristicTypeId ) ");

	public final static StringBuilder FETCH_RETRO_PAID_RULE = new StringBuilder()
			
			//.append(" from FinancialTransaction ft,  CmFinancialTransactionExtension cmft, Policy pol ")
			.append(" from FinancialTransaction ft,  FinancialTransactionExtension cmft, Policy pol ")
			
			.append(" where ft.serviceAgreement.id IN ")
			.append(" (select sa.id from ServiceAgreement sa ")
			.append(" where sa.account.id=:accountId) ")
			.append(" AND ft.financialTransactionType IN ( :billSegment , :billCancellation ) ")
			.append(" AND (ft.matchEventId=' ' ")
			.append(" OR (select mevt.matchEventStatus from MatchEvent mevt ")
			.append(" where mevt.id=ft.matchEventId) <> 'B' ) ")
			.append(" AND cmft.id=ft.id ")
			.append(" AND cmft.membershipId <> ' ' ")
			.append(" AND cmft.policyId=pol.id ");

	public final static StringBuilder RETRIEVE_PERSONS_WITH_ACCT_REL_TYPE = new StringBuilder()
			.append(" from AccountPerson ap ,OverdueProcess op ")
			.append(" where op.id=:overdueProcessId ")
			.append(" AND ap.id.account.id=op.account.id")
			.append(" AND ap.receivesNotification=:notifySwitch ")
			.append(" AND ap.accountRelationshipType IN ( :acRelTy1, :acRelTy2, :acRelTy3, :acRelTy4, :acRelTy5) ");

	public final static StringBuilder RETRIEVE_PERSONS_WITHOUT_ACCT_REL_TYPE = new StringBuilder()
			.append(" from AccountPerson ap ,OverdueProcess op ")
			.append(" where op.id=:overdueProcessId ")
			.append(" AND ap.id.account.id=op.account.id")
			.append(" AND ap.receivesNotification=:notifySwitch ");

	public final static StringBuilder VAL_CHAR_TYPE = new StringBuilder()
			.append(" from CharacteristicType ct, CharacteristicEntity ce ")
			.append(" where ct.characteristicType=:charTypeFlg ")
			.append(" AND ct.id=:charTypeCode ")
			.append(" AND ce.id.characteristicType=ct.id ")
			.append(" AND ce.id.characteristicEntity=:odProcLog ");

	public static final String CUST_CONT_TBL = "CI_CC";
	public static final String OVD_PROC_TBL = "CI_OD_PROC";

	public final static StringBuilder FETCH_LATEST_DUE_DATE = new StringBuilder()
			.append(" from FinancialTransaction ft,  Bill bill ")
			.append(" where ft.serviceAgreement.id IN ")
			.append(" (select sa.id from ServiceAgreement sa ")
			.append(" where sa.account.id=:accountId) ")
			.append(" AND ft.financialTransactionType IN ( :billSegment , :billCancellation, :adjustment, :adjustmentCancellation ) ")
			.append(" AND (ft.matchEventId=' ' ")
			.append(" OR (select mevt.matchEventStatus from MatchEvent mevt ")
			.append(" where mevt.id=ft.matchEventId) <> 'B' ) ")
			.append(" AND bill.id=ft.billId ")
			.append(" AND ft.billId IN ")
			.append(" (select odo.characteristicValueForeignKey1 from OverdueProcessObjectsBeingCollectedOn odo ")
			.append(" where odo.id.overdueProcess.id=:overdueProcessId ")
			.append(" AND odo.characteristicType=:billCharType) ")
			.append(" AND ft.currentAmount > 0 ");

	public final static StringBuilder CIS_DIVISION_CHAR_QUERY = new StringBuilder()
			.append(" from CisDivisionCharacteristic divc ")
			.append(" where divc.id.characteristicType=:terminationDateRuleCharType ")
			.append(" AND divc.id.effectiveDate=(select max(divc2.id.effectiveDate) ")
			.append(" from CisDivisionCharacteristic divc2 where divc.id.division=divc2.id.division ")
			.append(" AND divc.id.characteristicType=divc2.id.characteristicType ")
			.append(" AND divc2.id.effectiveDate<=:latestBillDueDate) ")
			.append(" AND divc.id.division IN ( ");
	public final static StringBuilder FETCH_BILL_WITH_OPEN_DEBT = new StringBuilder()
			.append(" FROM Bill bill WHERE bill.account=:accountId ")
			.append(" AND bill.id IN ( SELECT ODO.characteristicValueForeignKey1 FROM OverdueProcessObjectsBeingCollectedOn ODO  ")
			.append(" WHERE ODO.id.overdueProcess =:overdueProcess AND ODO.characteristicType='C1_OVDBL' ) ")
			.append(" AND EXISTS ( SELECT FT.id FROM FinancialTransaction FT WHERE FT.billId = bill.id  ")
			.append(" AND FT.financialTransactionType IN ( :billSegment , :billCancellation, :adjustment, :adjustmentCancellation ) ")
			.append(" AND (FT.matchEventId=' '  OR ( select mevt.matchEventStatus from MatchEvent mevt ")
			.append(" where mevt.id=FT.matchEventId) <> 'B') AND FT.currentAmount > 0 )");
	public final static StringBuilder FETCH_MAX_COV_END_DATE = new StringBuilder()
		
			//.append(" from FinancialTransaction ft,  CmFinancialTransactionExtension cmft ")
			.append(" from FinancialTransaction ft,  FinancialTransactionExtension cmft ")
			
			.append(" where ft.serviceAgreement.id IN  (select sa.id from ServiceAgreement sa ")
			.append(" where sa.account.id=:accountId ) ")
			.append(" AND ft.financialTransactionType IN ( :billSegment , :billCancellation ) AND ft.isFrozen='Y' ")
			.append(" AND cmft.id=ft.id AND cmft.endDate <= :endOfGrace ");
	public final static StringBuilder RETRIEVE_MIN_POLICIES_START_DT = new StringBuilder()
			.append(" SELECT min (PO.START_DT) START_DT  FROM ")
			.append(" CI_SA SA,CI_BILL_CHG BC, CI_BILL_CHG_CHAR BCC, CI_MEMBERSHIP MBR, CI_POLICY_PLAN PP, CI_POLICY PO ")
			.append(" WHERE  SA.ACCT_ID = :account AND BC.SA_ID = SA.SA_ID  AND BC.BILLABLE_CHG_STAT = '10' AND BC.END_DT >= :terminationDate ")
			.append(" AND BCC.BILLABLE_CHG_ID = BC.BILLABLE_CHG_ID AND BCC.CHAR_TYPE_CD = :mbrshipCharType AND MBR.MEMBERSHIP_ID = BCC.SRCH_CHAR_VAL AND PP.PLAN_ID = MBR.PLAN_ID AND PP.POLICY_ID = PO.POLICY_ID ")
			.append(" AND (PO.BO_STATUS_CD = :activeStatus OR (PO.BO_STATUS_CD = :terminatedStatus AND PO.END_DT > :terminationDate)) ");

}

