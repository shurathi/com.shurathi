/*                                                                
 ****************************************************************************************************************
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Define constants
 * This code includes the SQLs used for real time Services
 *                                                             
 ****************************************************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 * 2020-05-06   VINODW		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework       
 *********************************************************************************************************************
 */

package com.splwg.cm.domain.delinquency.interfaces.customerInformation;

import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObject_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.characteristicType.CharacteristicType_Id;
import com.splwg.base.domain.common.featureConfiguration.FeatureConfiguration;
import com.splwg.base.domain.common.featureConfiguration.FeatureConfiguration_Id;
import com.splwg.ccb.domain.admin.idType.IdType;
import com.splwg.ccb.domain.admin.idType.IdType_Id;
import com.splwg.ccb.domain.admin.serviceQuantityIdentifier.ServiceQuantityIdentifier;
import com.splwg.ccb.domain.admin.serviceQuantityIdentifier.ServiceQuantityIdentifier_Id;
import com.splwg.ccb.domain.common.c1RequestType.C1RequestType;
import com.splwg.ccb.domain.common.c1RequestType.C1RequestType_Id;
import com.splwg.cm.api.lookup.CmCustomerManagementOptionsLookup;

public class CmServiceConstants {

	public static final String BO_OLD_STATUS_MSG = "Business Object Old Status";
	public static final CharacteristicType INVOICE_TYPE_CHAR_TYPE = new CharacteristicType_Id("CMINVTYP").getEntity();
	public static final String BILL_GROUP_ACCOUNT_NBR_TYPE_CD = "BILGRPID";
	public static final CharacteristicType CONTRACT_POLICY_PLAN_CHAR_TYPE = new CharacteristicType_Id("CMPOLPLN").getEntity();
	public static final CharacteristicType PLAN_DESCR_CHAR_TYPE = new CharacteristicType_Id("CMPLDESC").getEntity();
	public static final CharacteristicType COVERAGE_TYPE_CHAR_TYPE = new CharacteristicType_Id("CMPLDESC").getEntity();
	public static final CharacteristicType INVOICE_CAT_CHAR_TYPE = new CharacteristicType_Id("CMINVCAT").getEntity();
	public static final CharacteristicType BILLABLE_CHARGE_TEMP_CHAR_TYPE = new CharacteristicType_Id("CMBCHGTP").getEntity();
	public static final CharacteristicType PRODUCT_TYP_CHAR_TYPE = new CharacteristicType_Id("CMPRODTY").getEntity();
	public static final String PREM_HOLIDAY_DIST_CD = "PREMHOL";
	public static final String FEECREDIT = "FEECREDIT";
	public static final String ACCTADJ = "ACCTADJ";
	public static final String PAYMENT_REQUEST_BO = "CM-PaymentRequest";
	public static final String INVOICE_REQUEST_BO = "CM-InvoiceRequest";
	public static final String COVERAGE_TYPE_FOR_BILL_WITH_BSEG_HAS_NOT_BILLED_BEFORE = "ELG";
	public static final String COVERAGE_TYPE_FOR_BILL_WITH_ALL_BSEG_BILLED_BEFORE = "ELA";
	public static final CharacteristicType OVERDUE_BILL_CHAR_TYPE = new CharacteristicType_Id("C1_OVDBL").getEntity();
	public static final CharacteristicType COVERAGE_TIER_CHAR_TYPE = new CharacteristicType_Id("CMCVTIER").getEntity();
	public static final CharacteristicType MEMEBER_CHAR_TYPE = new CharacteristicType_Id("C1MMBRSH").getEntity();
	public static final CharacteristicType TOTAL_VOLUME_CHAR_TYPE = new CharacteristicType_Id("CMVOLINS").getEntity();
	public static final CharacteristicType FEE_TYPE_CHAR_TYPE = new CharacteristicType_Id("CMTRNCAT").getEntity();
	public static final CharacteristicType PREVIOUS_MAIN_CUSTOMER_TYPE_CHAR_TYPE = new CharacteristicType_Id("CMPRVMNC").getEntity();	
	public static final CharacteristicType SDD_REQUEST_ID_CHAR_TYPE = new CharacteristicType_Id("CMSDDREQ").getEntity();
	public static final String FEE_TYPE_CHAR_VALUE = "FEE";
	public static final String LATE_PAYMENT_FEE_ADJUSTMENT_TYPE = "LPC";
	public static final CharacteristicType STATE_ASSESSMENT_FEE_CHAR_TYPE = new CharacteristicType_Id("CMBLSTFE").getEntity();
	public static final String STATE_ASSESSMENT_FEE_CHAR_VALUE = "Y";
	public static final String TRANSACTION_DETAILS_INVOICE_ACTIVITY_TYPE = "Invoice";
	public static final String TRANSACTION_DETAILS_PAYMENT_APPLIED_ACTIVITY_TYPE = "Payment Applied";
	public static final String TRANSACTION_DETAILS_PAYMENT_CANCELLED_ACTIVITY_TYPE = "Payment Cancelled";
	public static final String TRANSACTION_DETAILS_ADJ_APPLIED_ACTIVITY_TYPE = "Adjustment Applied";
	public static final String TRANSACTION_DETAILS_ADJ_CANCELLED_ACTIVITY_TYPE = "Adjustment Cancelled";
	public static final String PAYMENT_NOT_ALLOCATED_ACTIVITY_TYPE = "Payment Not Allocated";
	public static final String PAYMENT_FULLY_APPLIED_ACTIVITY_TYPE = "Payment Applied";
	public static final String PAYMENT_PARTIALLY_APPLIED_ACTIVITY_TYPE = "Payment Partially Applied";
	public static final String PAYMENT_CANCELLED_ACTIVITY_TYPE = "Payment Cancelled";
	public static final String PAYMENT_TRANSFERRED_ACTIVITY_TYPE = "Payment Transferred";
	public static final String PAYMENT_PARTIALLY_TRANSFERRED_ACTIVITY_TYPE = "Payment Partially Transferred";
	public static final String PAYMENT_APPLIED_TO_EXCESS_ACTIVITY_TYPE = "Payment Applied";
	public static final String INTERCOMPANY_TRANSFER_MATCH_TYPE = "ICTRANSF";
	public static final String ACCOUNT_ENTITY = "Account";
	public static final String DIVISION_ENTITY = "Division";
	public static final CharacteristicType STATE_OF_ISSUE_CHAR_TYPE = new CharacteristicType_Id("CMSTISSE").getEntity();
	public static final CharacteristicType LOCKBOX_CHAR_TYPE = new CharacteristicType_Id("CMLCKBX").getEntity();
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final String PACKAGE_SAVING_CONTRACT_TYPE = "PACKSAV";
	public static final CharacteristicType BILLABLE_CHARGE_POLICY_ID_CHARACTERRISTIC_TYPE = new CharacteristicType_Id("CMPOLICY").getEntity();
	public static final C1RequestType INVOICE_REQUEST_TYPE_FOR_ADJUSTMENT_INVOICE = new C1RequestType_Id("INVREQOFFSCHADJ").getEntity();
	public static final String INVOICE_REQUEST_STATUS_REASON_CODE = "ADJUSTMENT_INVOICE_REQUESTED";
	public static final String ADJUSTMENT_INVOICE_SUCCESSFULLY_CREATED_MSG = "Adjustment Invoice Request Created:";
	public static final String ADJUSTMENT_INVOICE_CREATED_IN_PENDING_MSG = "Invoice Created In Pending Status:";
	public static final CharacteristicType RATE_COVERAGE_CHAR_TYPE = new CharacteristicType_Id("CMRATCOV").getEntity();
	public static final String RX_COVERAGE_TYPE = "R";
	public static final String MEDICAL_COVERAGE_TYPE = "M";
	public static final IdType SUBSCRIBER_ID_TYPE = new IdType_Id("ALT").getEntity();
	public static final CharacteristicType BILLABLE_CHARGE_TEMPLATE_CHAR_TYPE = new CharacteristicType_Id("CMBCHGTP").getEntity();
	public static final String ADD_ACTION = "Create";
	public static final String CUSTOMER_NUMBER_IDENTIFIER = "PRIMECID";
	public static final String ONLINE_TRANSACTION_TYPE = "Online";
	public static final String SDD_ACCOUNT_TYPE = "SDD";
	public static final String SYSTEM_PAYMENT_METHOD_TYPE = "System";
	public static final String CHECK_PAYMENT_METHOD_TYPE = "Check";
	public static final String PENDING_PAYMENT_EXISTS_CHARACTER = "X";
	public static final String MODIFY_ACTION = "Modify";
	public static final String CANCEL_ACTION = "Cancel";
	public static final String DELETE_ACTION = "Delete";
	public static final String PAYMENT_REQUEST_TYPE_FOR_ONLINE_PAYMENT = "ONLINEPAYREQ";
	public static final String PAYMENT_REQUEST_TYPE_FOR_IVR_PAYMENT = "IVRPAYREQ";
	public static final String PAYMENT_REQUEST_TYPE_FOR_SDD_PAYMENT = "SDDPAYREQ";
	public static final String EXTERNAL_SYSTEM_FOR_OBS = "OBS";
	public static final String EXTERNAL_SYSTEM_FOR_IVR = "IVR";
	public static final String PAYMENT_REQUEST_PENDING_STATUS = "DRAFT";
	public static final String PAYMENT_REQUEST_PAYMENT_CREATED_BY_EXTERNAL_USER_STATUS = "OPEN-EXTRNL";
	public static final String PAYMENT_REQUEST_PAYMENT_USER_CANCELLED_STATUS = "CANCELLED";
	public static final String PAYMENT_REQUEST_BRMS_INCOMPLETE_PAYMENT_EVENT_CREATED_STATUS = "PAYEVENTCRET";
	public static final String PAYMENT_REQUEST_PAYMENT_APPLIED_STATUS = "CLOSED";
	public static final String PAYMENT_REQUEST_PAYMENT_SYSTEM_CANCELLED_STATUS = "SYSCANCELLED";
	public static final String PAYMENT_REQUEST_DUMMY_EXT_REF_ID = "DUMMY";
	public static final String PAYMENT_REQUEST_PAYOR_ACCT_DEFAULT_BILL_GROUP = "1";
	public static final CharacteristicType PAYMENT_REQUEST_EXTERNAL_SYSTEM_CHAR_TYPE = new CharacteristicType_Id("CMEXTSYS").getEntity();
	public static final CharacteristicType PAYMENT_REQUEST_LOG_USER_CHAR_TYPE = new CharacteristicType_Id("CMUSER").getEntity();
	public static final CharacteristicType PAYMENT_REQUEST_EXTERNAL_REFERENCE_ID_CHAR_TYPE = new CharacteristicType_Id("CMEXRFID").getEntity();
	public static final CharacteristicType PAYMENT_REQUEST_TENDER_AMOUNT_CHAR_TYPE = new CharacteristicType_Id("CMTNDAMT").getEntity();
	public static final CharacteristicType LEGACY_PAYMENT_CONFIRMATION_NUMBER_CHAR_TYPE = new CharacteristicType_Id("CMCNFNBR").getEntity();
	public static final String ACTIVITY_FOR_SUBMITTED_PAYMENT_REQUEST = "Pending";
	public static final String ACTIVITY_FOR_INCOMPLET_PAYMENTS = "Scheduled";
	public static final String ACTIVITY_FOR_FROZEN_PAYMENTS = "Applied";
	public static final String ACTIVITY_FOR_ERROR_PAYMENTS = "Error";
	public static final String ACTIVITY_FOR_CANCEL_PAYMENTS = "Cancelled";
	public static final String MODIFY_PAYMENT_REQUEST_LOG_ENTRY = "Payment Modified";
	public static final String CANCEL_PAYMENT_REQUEST_LOG_ENTRY = "Payment Cancelled";
	public static final String SDD_PAYMENT_REQUEST_BO = "CM-SddPaymentRequest";
	public static final String SDD_PAYMENT_REQUEST_PENDING_STATUS = "PENDING";
	public static final String PEROSN_DOES_NOT_EXISTS_ERROR_PARM1 = "Person";
	public static final String PEROSN_DOES_NOT_EXISTS_ERROR_PARM2 = "Customer:";
	public static final String ACCOUNT_DOES_NOT_EXISTS_ERROR_PARM1 = "Account";
	public static final String ACCOUNT_DOES_NOT_EXISTS_ERROR_PARM2 = "Combination:";
	public static final String ACCOUNT_APAY_DOES_NOT_EXISTS_ERROR_PARM1 = "Active SDD";
	public static final String ACCOUNT_APAY_DOES_NOT_EXISTS_ERROR_PARM2 = "Combination:";
	public static final String BILL_DOES_NOT_EXISTS_FOR_PERSON_ERROR_PARM1 = "Invoice:";
	public static final String BILL_DOES_NOT_EXISTS_FOR_PERSON_ERROR_PARM2 = "Customer:";
	public static final String BRMS_SYSTEM_USER = "SYSUSER";
	public static final String SAVINGS_TENDER_TYPE = "ACHS";
	public static final String CHECKING_TENDER_TYPE = "ACHC";
	public static final FeatureConfiguration EXCESS_CREDIT_FEATURE_CONFIG = new FeatureConfiguration_Id("CM_CMO").getEntity();
	public static final String EXCESS_CREDIT_FEATURE_CONFIG_OPTION_TYPE = "CMOA";
	public static final String EXT_LOOKUP_BUS_OBJ_CD = "bo";
	public static final String EXT_LOOKUP_LOOKUP_VALUE = "lookupValue";
	public static final String EXT_LOOKUP_DIRECT_DEBIT_DAY = "directDebitDay";
	public static final String EXT_LOOKUP_SELTTLEMENT_DAYS_AFTER_EXTRACT_DATE = "settlementDaysAfterExtractDate";
	public static final String BO_NAME = "CM-DirectDebitDayLookup";
	public static final CharacteristicType BILL_TYPE_CHAR_TYPE = new CharacteristicType_Id("CMBILLTY").getEntity();
	public static final String BILL_TYPE_ONE = "1";
	public static final String BILL_TYPE_TWO = "2";
	public static final String BILL_TYPE_FIVE = "5";
	public static final CharacteristicType BILL_GROUP_NAME_CHAR_TYPE = new CharacteristicType_Id("CMBGNAME").getEntity();
	public static final CharacteristicType BILL_GROUP_TERMED_DATE_CHAR_TYPE = new CharacteristicType_Id("CMGTRMDT").getEntity();
	public static final String ORACLE_INVALID_NUMBER = "ORA-01722";
	public static final CharacteristicType ELGIBILITY_STATUS_CHAR_TYPE = new CharacteristicType_Id("CMELGSTA").getEntity();
	public static final String MIGRATED_INVOICE_TYPE = "MGR";
	public static final CharacteristicType MIGRATED_INVOICE_CHAR_TYPE = new CharacteristicType_Id("CMLEGINV").getEntity();
	public static final String PACKAGED_SAVINGS_CREDIT_PERSON_ID_NBR = "000000000";
	public static final String PACKAGED_SAVINGS_CREDIT_ENTITY_NAME = "Packaged Savings Credit";
	public static final String PACKAGED_SAVINGS_CREDIT_CHARGE_LINE_CODE_LIST = "01441,01442";
	public static final String ELEGBL = "ELEGBL";
	public static final String RETRO = "RETRO";
	public static final String BRMSFEE = "BRMSFEE";
	public static final IdType CES_ID_TYPE = new IdType_Id("CESID").getEntity();
	public static final String ACIS_SOURCE_SYSTEM = "AC";
	public static final IdType ACIS_ID_TYPE = new IdType_Id("ACISCID").getEntity();
	public static final CharacteristicType PURCHASE_ORDER_NUMBER_CHAR_TYPE = new CharacteristicType_Id("CMPONUMB").getEntity();
	public static final CharacteristicType INVOICE_SUMMARY_OPTION_CHAR_TYPE = new CharacteristicType_Id("CMINVSUM").getEntity();
	public static final CharacteristicType INVOICE_DETAIL_OPTION_CHAR_TYPE = new CharacteristicType_Id("CMINVDET").getEntity();
	public static final CharacteristicType BENEFIT_GROUP_NAME_CHAR_TYPE = new CharacteristicType_Id("CMGRPPNM").getEntity();
	public static final CharacteristicType CUSTOMER_DEFINED_SORT_CHAR_TYPE = new CharacteristicType_Id("CMCSDFST").getEntity();
	public static final String DEFAULT_INVOICE_SUMMARY_TYPE = "POLICY";
	public static final CharacteristicType GROUP_NAME_CHAR_TYPE = new CharacteristicType_Id("CMGRPNME").getEntity();
	public static final ServiceQuantityIdentifier PREMIUM_AMOUNT_SQI_CODE = new ServiceQuantityIdentifier_Id("PREMAMT").getEntity();
	public static final CmCustomerManagementOptionsLookup EXCESS_CREDIT_FEATURE_CONFIG_OPTION_TYPE_LOOKUP_VALUE = CmCustomerManagementOptionsLookup.constants.CM_ADMINISTRATIVE_CONTRACT_TYPE;
	public static final CharacteristicType PLAN_RATE_CHARACTERISTIC_TYPE = new CharacteristicType_Id("CMPLRATE").getEntity();
	public static final CharacteristicType ACIS_TIER_SET_CHARACTERISTIC_TYPE = new CharacteristicType_Id("CMACISTS").getEntity();
	public static final BusinessObject ACIS_TIER_MAPPING_BO = new BusinessObject_Id("CM-ACISTierMapping").getEntity();
	public static final BusinessObject CUSTOMER_CLASS_LOOKUP_BO = new BusinessObject_Id("CM-CustomerClassLookup").getEntity();
	public static final String MEMBER_ID_TYPE_NODE = "invoiceWebServicesSubscriberIdType";
	public static final StringBuilder RETRIEVE_LATEST_PAY_EVENT = new StringBuilder()
			.append(" FROM Payment payment ,").append(" PaymentEvent paymentEvent")
			.append(" WHERE payment.account IN (:accountList) ")
			.append(" AND paymentEvent.id = payment.paymentEvent ")
			.append(" AND payment.paymentStatus    = :frozen");

	public static final StringBuilder RETRIEVE_LATEST_PAY_AMOUNT = new StringBuilder()
			.append(" FROM Payment payment,").append(" PaymentEvent paymentEvent ")
			.append(" WHERE payment.account IN (:accountList) ")
			.append(" AND paymentEvent.id = payment.paymentEvent ")
			.append(" AND payment.paymentStatus    = :frozen")
			.append(" AND paymentEvent.id = :payEventId");

	public static final StringBuilder RETRIEVE_LATEST_BILL = new StringBuilder()
			.append("FROM Bill bill")
			.append(" WHERE bill.account = :account ")
			.append(" AND bill.billStatus = :complete");

	public static final StringBuilder RETRIEVE_LATEST_BILL_AMOUNT = new StringBuilder()
			.append(" From FinancialTransaction financialTransaction ")
			.append(" WHERE financialTransaction.billId = :latestBill");

	public static final StringBuilder RETRIEVE_CUSTOMER_CURRENT_BALANCE = new StringBuilder()
			.append(" From FinancialTransaction financialTransaction, ServiceAgreement serviceAgreement  ")
			.append(" WHERE serviceAgreement.account IN (SELECT accountPerson.id.account ")
			.append(" FROM AccountPerson accountPerson ")
			.append(" WHERE accountPerson.id.person = :customer")
			.append(" AND accountPerson.isMainCustomer = :isMainCustomer )")
			.append(" AND financialTransaction.serviceAgreement = serviceAgreement.id ").append(" AND financialTransaction.isFrozen = :frozen");

	public static final StringBuilder RETRIEVE_OPEN_INVOICE_LIST = new StringBuilder()
			.append(" SELECT  BLCR.BILL_ID ")
			.append("       , BLCR.CURRENCY_CD ")
			.append("       , BLCR.ORIG_AMT ")
			.append("       , BLCR.UNPAID_AMT ")
			.append("       , BLCR.BILL_DT ")
			.append("       , BLCR.DUE_DT ")
			.append("       , BLCR.BILL_STAT_FLG ")
			.append("       , BLCR.ACCT_ID ")
			.append("       , BLCR.AGE_DAYS ")
			.append("       , CASE WHEN BLCR.AGE_DAYS <= 30  ")
			.append("                  THEN BLCR.UNPAID_AMT  ")
			.append("              ELSE 0  ")
			.append("         END AS AGE30 ")
			.append("      , CASE WHEN BLCR.AGE_DAYS > 30 AND BLCR.AGE_DAYS <= 60  ")
			.append("                  THEN BLCR.UNPAID_AMT  ")
			.append("              ELSE 0  ")
			.append("         END AS AGE60 ")
			.append("       , CASE WHEN BLCR.AGE_DAYS > 60 AND BLCR.AGE_DAYS <= 90  ")
			.append("                  THEN BLCR.UNPAID_AMT  ")
			.append("              ELSE 0  ")
			.append("         END AS AGE90 ")
			.append("       , CASE WHEN BLCR.AGE_DAYS > 90  ")
			.append("                  THEN BLCR.UNPAID_AMT  ")
			.append("              ELSE 0  ")
			.append("         END AS AGEOVER90 ")
			.append("       , (SELECT OD.OD_PROC_ID FROM CI_OD_PROC OD, CI_OD_PROC_OBJ ODO ")
			.append("          WHERE  OD.ACCT_ID = BLCR.ACCT_ID AND OD.OD_STATUS_FLG = '10'  ")
			.append("                 AND OD.OD_STAT_RSN_FLG = '10' AND ODO.OD_PROC_ID = OD.OD_PROC_ID ")
			.append("                 AND ODO.CHAR_TYPE_CD = 'C1_OVDBL' AND :charValue = BLCR.BILL_ID ")
			.append("                 AND ROWNUM = 1) AS OD_PROC_ID  ")
			.append(" FROM ")
			.append(" (SELECT  BLCRAMT.BILL_ID ")
			.append("        , BLCRAMT.CURRENCY_CD ")
			.append("        , BLCRAMT.ORIG_AMT ")
			.append("        , BLCRAMT.UNPAID_AMT ")
			.append("        , BL.BILL_DT ")
			.append("        , BL.DUE_DT ")
			.append("        , BL.BILL_STAT_FLG ")
			.append("        , BL.ACCT_ID ")
			.append("        , (TO_DATE(SYSDATE)- TO_DATE(BL.BILL_DT)) as AGE_DAYS ")
			.append(" FROM ")
			.append(" /** Get original and unpaid amounts by bill, currency (BLCRAMT) **/ ")
			.append(" (SELECT  BLCRME.BILL_ID ")
			.append("        , BLCRME.CURRENCY_CD ")
			.append("        , SUM(BLCRME.ORIG_AMT) AS ORIG_AMT ")
			.append("        , SUM(BLCRME.UNPAID_AMT) AS UNPAID_AMT ")
			.append(" FROM ")
			.append(" /** Get FT amounts by bill, currency, match event (BLCRME) **/ ")
			.append(" (SELECT   BL.BILL_ID ")
			.append("         , FT.CURRENCY_CD ")
			.append("         , FT.MATCH_EVT_ID ")
			.append("        /** If no match event, unpaid is orig amount **/  ")
			.append("        /** If match event balanced, unpaid is 0 **/ ")
			.append("        /** Else unpaid is ft total for match event **/ ")
			.append("        , CASE WHEN FT.MATCH_EVT_ID = ' ' ")
			.append("                  THEN SUM(FT.CUR_AMT) ")
			.append("               WHEN (SELECT MEVT.MEVT_STATUS_FLG FROM CI_MATCH_EVT MEVT ")
			.append("                    WHERE  MEVT.MATCH_EVT_ID = FT.MATCH_EVT_ID) = 'B' ")
			.append("                  THEN 0 ")
			.append("               ELSE (SELECT SUM(FT2.CUR_AMT) FROM CI_FT FT2 ")
			.append("                    WHERE  FT2.MATCH_EVT_ID = FT.MATCH_EVT_ID  ")
			.append("                           AND FT2.FREEZE_SW = 'Y' ")
			.append("                           AND FT2.CURRENCY_CD = FT.CURRENCY_CD) ")
			.append("          END AS UNPAID_AMT ")
			.append("         , SUM(FT.CUR_AMT) AS ORIG_AMT ")
			.append("   FROM    CI_FT FT ")
			.append("         , CI_BILL BL ")
			.append("   WHERE   BL.ACCT_ID IN (:accountList)   ")
			.append("           AND BL.BILL_STAT_FLG = 'C' ")
			.append("           AND FT.BILL_ID = BL.BILL_ID ")
			.append("           AND FT.FREEZE_SW = 'Y' ")
			.append("           AND FT.FT_TYPE_FLG IN ('BS','BX','AD','AX') ")
			.append(" GROUP BY BL.BILL_ID, FT.CURRENCY_CD, FT.MATCH_EVT_ID) BLCRME ")
			.append(" GROUP BY BLCRME.BILL_ID, BLCRME.CURRENCY_CD) BLCRAMT ")
			.append(" , CI_BILL BL ")
			.append(" WHERE BL.BILL_ID = BLCRAMT.BILL_ID ")
			.append("             AND BLCRAMT.UNPAID_AMT <> 0	 ")
			.append("                    ) BLCR						 ");

	public static final StringBuilder RETRIEVE_CLOSED_INVOICE_LIST = new StringBuilder()
			.append(" SELECT  BLCR.BILL_ID ")
			.append("       , BLCR.CURRENCY_CD ")
			.append("       , BLCR.ORIG_AMT ")
			.append("       , BLCR.UNPAID_AMT ")
			.append("       , BLCR.BILL_DT ")
			.append("       , BLCR.DUE_DT ")
			.append("       , BLCR.BILL_STAT_FLG ")
			.append("       , BLCR.ACCT_ID ")
			.append("       , BLCR.AGE_DAYS ")
			.append("       , CASE WHEN BLCR.AGE_DAYS <= 30  ")
			.append("                  THEN BLCR.UNPAID_AMT  ")
			.append("              ELSE 0  ")
			.append("         END AS AGE30 ")
			.append("      , CASE WHEN BLCR.AGE_DAYS > 30 AND BLCR.AGE_DAYS <= 60  ")
			.append("                  THEN BLCR.UNPAID_AMT  ")
			.append("              ELSE 0  ")
			.append("         END AS AGE60 ")
			.append("       , CASE WHEN BLCR.AGE_DAYS > 60 AND BLCR.AGE_DAYS <= 90  ")
			.append("                  THEN BLCR.UNPAID_AMT  ")
			.append("              ELSE 0  ")
			.append("         END AS AGE90 ")
			.append("       , CASE WHEN BLCR.AGE_DAYS > 90  ")
			.append("                  THEN BLCR.UNPAID_AMT  ")
			.append("              ELSE 0  ")
			.append("         END AS AGEOVER90 ")
			.append("       , (SELECT OD.OD_PROC_ID FROM CI_OD_PROC OD, CI_OD_PROC_OBJ ODO ")
			.append("          WHERE  OD.ACCT_ID = BLCR.ACCT_ID AND OD.OD_STATUS_FLG = '10'  ")
			.append("                 AND OD.OD_STAT_RSN_FLG = '10' AND ODO.OD_PROC_ID = OD.OD_PROC_ID ")
			.append("                 AND ODO.CHAR_TYPE_CD = 'C1_OVDBL' AND :charValue = BLCR.BILL_ID ")
			.append("                 AND ROWNUM = 1) AS OD_PROC_ID  ")
			.append(" FROM ")
			.append(" (SELECT  BLCRAMT.BILL_ID ")
			.append("        , BLCRAMT.CURRENCY_CD ")
			.append("        , BLCRAMT.ORIG_AMT ")
			.append("        , BLCRAMT.UNPAID_AMT ")
			.append("        , BL.BILL_DT ")
			.append("        , BL.DUE_DT ")
			.append("        , BL.BILL_STAT_FLG ")
			.append("        , BL.ACCT_ID ")
			.append("        , (TO_DATE(SYSDATE)- TO_DATE(BL.BILL_DT)) as AGE_DAYS ")
			.append(" FROM ")
			.append(" /** Get original and unpaid amounts by bill, currency (BLCRAMT) **/ ")
			.append(" (SELECT  BLCRME.BILL_ID ")
			.append("        , BLCRME.CURRENCY_CD ")
			.append("        , SUM(BLCRME.ORIG_AMT) AS ORIG_AMT ")
			.append("        , SUM(BLCRME.UNPAID_AMT) AS UNPAID_AMT ")
			.append(" FROM ")
			.append(" /** Get FT amounts by bill, currency, match event (BLCRME) **/ ")
			.append(" (SELECT   BL.BILL_ID ")
			.append("         , FT.CURRENCY_CD ")
			.append("         , FT.MATCH_EVT_ID ")
			.append("        /** If no match event, unpaid is orig amount **/  ")
			.append("        /** If match event balanced, unpaid is 0 **/ ")
			.append("        /** Else unpaid is ft total for match event **/ ")
			.append("        , CASE WHEN FT.MATCH_EVT_ID = ' ' ")
			.append("                  THEN SUM(FT.CUR_AMT) ")
			.append("               WHEN (SELECT MEVT.MEVT_STATUS_FLG FROM CI_MATCH_EVT MEVT ")
			.append("                    WHERE  MEVT.MATCH_EVT_ID = FT.MATCH_EVT_ID) = 'B' ")
			.append("                  THEN 0 ")
			.append("               ELSE (SELECT SUM(FT2.CUR_AMT) FROM CI_FT FT2 ")
			.append("                    WHERE  FT2.MATCH_EVT_ID = FT.MATCH_EVT_ID  ")
			.append("                           AND FT2.FREEZE_SW = 'Y' ")
			.append("                           AND FT2.CURRENCY_CD = FT.CURRENCY_CD) ")
			.append("          END AS UNPAID_AMT ")
			.append("         , SUM(FT.CUR_AMT) AS ORIG_AMT ")
			.append("   FROM    CI_FT FT ")
			.append("         , CI_BILL BL ")
			.append("   WHERE   BL.ACCT_ID IN (:accountList)  ")
			.append("           AND BL.BILL_STAT_FLG = 'C' ")
			.append("           AND FT.BILL_ID = BL.BILL_ID ")
			.append("           AND FT.FREEZE_SW = 'Y' ")
			.append("           AND FT.FT_TYPE_FLG IN ('BS','BX','AD','AX') ")
			.append(" GROUP BY BL.BILL_ID, FT.CURRENCY_CD, FT.MATCH_EVT_ID) BLCRME ")
			.append(" GROUP BY BLCRME.BILL_ID, BLCRME.CURRENCY_CD) BLCRAMT ")
			.append(" , CI_BILL BL ")
			.append(" WHERE BL.BILL_ID = BLCRAMT.BILL_ID ")
			.append("             AND BLCRAMT.UNPAID_AMT = 0	 ")
			.append("          AND BL.BILL_DT BETWEEN :startDate AND :endDate ) BLCR");

	public static final StringBuilder RETRIEVE_INVOICE_POLICIES = new StringBuilder()
			.append("SELECT  distinct polcy.POLICY_NBR ")
			// Bug 10773 Start Add
			.append(" FROM   CI_FT FT ")
			.append("     , C1_FT_EXT FTX ")
			.append("	 , CI_POLICY polcy ")
			.append(" WHERE  polcy.POLICY_ID = FTX.POLICY_ID ")
			.append("	   	AND FT.BILL_ID = :invoiceNumber ")
			.append("       AND FT.FREEZE_SW = 'Y' ")
			.append("       AND FTX.FT_ID = FT.FT_ID ")
			.append("       AND FTX.POLICY_ID <> ' ' ");

	// .append(" FROM ci_sa_char sachar, ")
	// .append("   ci_bseg bseg, ")
	// .append("   ci_policy_plan policyplan, ")
	// .append("   ci_policy polcy ")
	// .append(" WHERE bseg.bill_id      = :invoiceNumber ")
	// .append(" AND sachar.sa_id        = bseg.sa_id ")
	// .append(" AND sachar.char_type_cd = :policyPlanCharType ")
	// .append(" AND sachar.EFFDT        = ")
	// .append("   (SELECT MAX(effdt) ")
	// .append("   FROM ci_sa_char ")
	// .append("   WHERE sa_id      = bseg.sa_id ")
	// .append("   AND char_type_cd = sachar.char_type_cd ")
	// .append("   AND effdt <= :processDate )  ")
	// .append(" AND sachar.srch_char_val = trim(policyplan.PLAN_ID) ")
	// .append(" AND sachar.srch_char_val = policyplan.PLAN_ID ")
	// .append(" AND policyplan.POLICY_ID = polcy.POLICY_ID ");



	public static final StringBuilder RETRIEVE_INVOICE_COVERAGE_TYPES = new StringBuilder()
			.append(" SELECT DISTINCT POLPLCH.ADHOC_CHAR_VAL coverage_type ")
			.append(" FROM CI_FT FT, C1_FT_EXT FTX, CI_POLICY_PLAN_CHAR POLPLCH ")
			.append(" WHERE FT.BILL_ID		= :invoiceNumber ")
			.append(" AND FTX.FT_ID 		= FT.FT_ID ")
			.append(" AND POLPLCH.PLAN_ID 	= FTX.PLAN_ID ")
			.append(" AND POLPLCH.CHAR_TYPE_CD = :coverageTypeCharType ")
			.append(" AND POLPLCH.EFFDT 	=	(SELECT MAX(EFFDT) ")
			.append(" 					   		FROM CI_POLICY_PLAN_CHAR ")
			.append(" 							WHERE CHAR_TYPE_CD	=  POLPLCH.CHAR_TYPE_CD ")
			.append(" 							AND PLAN_ID 		=  POLPLCH.PLAN_ID ")
			.append(" 							AND EFFDT 			<= :processDate) ");



	// public static final StringBuilder RETRIEVE_INVOICE_COVERAGE_TYPES = new
	// StringBuilder()
	// .append(" SELECT  DISTINCT policyPlanChar.ADHOC_CHAR_VAL coverage_type ")
	// .append(" FROM ci_sa_char sachar, ")
	// .append("   ci_bseg bseg, ")
	// .append("   ci_policy_plan_char policyPlanChar ")
	// .append(" WHERE bseg.bill_id      = :invoiceNumber ")
	// .append(" AND sachar.sa_id        = bseg.sa_id ")
	// .append(" AND sachar.char_type_cd = :policyPlanCharType ")
	// .append(" AND sachar.EFFDT        = ")
	// .append("   (SELECT MAX(effdt) ")
	// .append("   FROM ci_sa_char ")
	// .append("   WHERE sa_id      = bseg.sa_id ")
	// .append("   AND char_type_cd = sachar.char_type_cd ")
	// .append("   AND effdt <= :processDate) ")
	// // .append(" AND sachar.srch_char_val = trim(policyPlanChar.PLAN_ID) ")
	// .append(" AND sachar.srch_char_val = policyPlanChar.PLAN_ID ")
	// .append(" and policyPlanChar.CHAR_TYPE_CD = :coverageTypeCharType ")
	// .append(" and policyPlanChar.EFFDT = (select max(effdt) from ci_policy_plan_char where  ")
	// .append(" char_type_cd =  policyPlanChar.CHAR_TYPE_CD  ")
	// .append(" and plan_id =  policyPlanChar.PLAN_ID AND effdt <= :processDate) ");


	/*
	 * public static final StringBuilder
	 * RETRIEVE_PENDING_PAYMENT_REQUESTS_FOR_PERSON_AND_EXTERNAL_SYSTEM = new
	 * StringBuilder()
	 * .append("from C1Request request , C1RequestRelatedObject requestRelatedObj "
	 * ) .append(" , C1RequestCharacteristic c1RequestCharacteristic ")
	 * .append(" WHERE request.id               = requestRelatedObj.id.c1Request "
	 * ) .append(
	 * " AND requestRelatedObj.id.maintenanceObject            = :maintenanceObject "
	 * ) .append(
	 * " AND requestRelatedObj.id.c1RequestRelationshipObjectType = :requestRelationshipObjTypeFlg "
	 * ) .append(" AND requestRelatedObj.primaryKeyValue1 = :personId  ")
	 * .append(
	 * " AND requestRelatedObj.id.c1Request = c1RequestCharacteristic.id.c1Request "
	 * ) .append(
	 * " AND c1RequestCharacteristic.id.characteristicType = :externalSystemCharType "
	 * ) .append(
	 * " AND c1RequestCharacteristic.searchCharacteristicValue = :externalSystemCd "
	 * ) .append(" AND request.businessObject  = :paymentRequestBO ") .append(
	 * " AND EXISTS ( From C1Request requestInn, BusinessObjectStatus boStatus where requestInn.id = request.id "
	 * )
	 * .append(" AND 	requestInn.businessObject = boStatus.id.businessObject ")
	 * .append(" AND boStatus.condition             = :interim )");
	 */
	
	/*
	 * public static final StringBuilder
	 * RETRIEVE_PENDING_ADJUSTMENT_INVOICE_REQUESTS_FOR_EXTERNAL_SYSTEM = new
	 * StringBuilder()
	 * .append("from C1Request request,  C1RequestRelatedObject requestRelatedObj "
	 * ) .append(" , C1RequestCharacteristic c1RequestCharacteristic ")
	 * .append(" WHERE request.id               = requestRelatedObj.id.c1Request "
	 * ) .append(
	 * " AND requestRelatedObj.id.maintenanceObject            = :maintenanceObject "
	 * ) .append(
	 * " AND requestRelatedObj.id.c1RequestRelationshipObjectType = :requestRelationshipObjTypeFlg "
	 * ) .append(" AND requestRelatedObj.primaryKeyValue1 = :billId  ") .append(
	 * " AND requestRelatedObj.id.c1Request = c1RequestCharacteristic.id.c1Request "
	 * ) .append(
	 * " AND c1RequestCharacteristic.id.characteristicType = :externalSystemCharType "
	 * ) .append(
	 * " AND c1RequestCharacteristic.searchCharacteristicValue = :externalSystemCd "
	 * ) .append(" AND request.businessObject                = :invoiceReqBO ")
	 * .append(
	 * " AND EXISTS ( From C1Request requestInn, BusinessObjectStatus boStatus where requestInn.id = request.id "
	 * )
	 * .append(" AND 	requestInn.businessObject = boStatus.id.businessObject ")
	 * .append(" AND boStatus.condition             = :interim )");
	 */
	
	public static final StringBuilder RETRIEVE_PENDING_PAYMENT_REQUESTS_FOR_PERSON_AND_EXTERNAL_SYSTEM = new StringBuilder()
			.append("from C1Request request , C1RequestRelatedObject requestRelatedObj, BusinessObjectStatus boStatus ")
			.append(" , C1RequestCharacteristic c1RequestCharacteristic ")
			.append(" WHERE request.id               = requestRelatedObj.id.c1Request ")
			.append(" AND requestRelatedObj.id.maintenanceObject            = :maintenanceObject ")
			.append(" AND requestRelatedObj.id.c1RequestRelationshipObjectType = :requestRelationshipObjTypeFlg ")
			.append(" AND requestRelatedObj.primaryKeyValue1 = :personId  ")
			.append(" AND requestRelatedObj.id.c1Request = c1RequestCharacteristic.id.c1Request ")
			.append(" AND c1RequestCharacteristic.id.characteristicType = :externalSystemCharType ")
			.append(" AND c1RequestCharacteristic.searchCharacteristicValue = :externalSystemCd ")
			.append(" AND request.status = boStatus.id.status  ")
			.append(" AND request.businessObject = boStatus.id.businessObject ")
			.append(" AND request.businessObject                = :paymentRequestBO ")
			.append(" AND boStatus.condition             = :interim ");

	public static final StringBuilder RETRIEVE_PENDING_ADJUSTMENT_INVOICE_REQUESTS_FOR_EXTERNAL_SYSTEM = new StringBuilder()
			.append("from C1Request request,  C1RequestRelatedObject requestRelatedObj ")
			.append(" , C1RequestCharacteristic c1RequestCharacteristic , BusinessObjectStatus boStatus ")
			.append(" WHERE request.id               = requestRelatedObj.id.c1Request ")
			.append(" AND requestRelatedObj.id.maintenanceObject            = :maintenanceObject ")
			.append(" AND requestRelatedObj.id.c1RequestRelationshipObjectType = :requestRelationshipObjTypeFlg ")
			.append(" AND requestRelatedObj.primaryKeyValue1 = :billId  ")
			.append(" AND requestRelatedObj.id.c1Request = c1RequestCharacteristic.id.c1Request ")
			.append(" AND c1RequestCharacteristic.id.characteristicType = :externalSystemCharType ")
			.append(" AND c1RequestCharacteristic.searchCharacteristicValue = :externalSystemCd ")
			.append(" AND request.status = boStatus.id.status  ")
			.append(" AND request.businessObject = boStatus.id.businessObject ")
			.append(" AND request.businessObject                = :invoiceReqBO ")
			.append(" AND boStatus.condition             = :interim ");


	public static final StringBuilder RETRIEVE_BILL_ID_FOR_PAYMENT_MATCH_EVENT_ID = new StringBuilder()
			.append("from FinancialTransaction financialTransaction ")
			.append(" WHERE financialTransaction.matchEventId IN ")
			.append(" ( select financialTransaction.matchEventId  ")
			.append(" from FinancialTransaction financialTransaction, Payment payment   where payment.id = financialTransaction.parentId and payment.paymentStatus    = :frozenPaymentStatus  ")
			.append(" and financialTransaction.parentId = :paymentId and financialTransaction.isFrozen = :isFrozen  ")
			.append(" and financialTransaction.matchEventId <> ' ' ) AND financialTransaction.billId <> ' ' ");

	public static final StringBuilder RETRIEVE_INVOICE_FT = new StringBuilder()
			.append(" From FinancialTransaction financialTransaction ")
			.append(" WHERE financialTransaction.billId = :currentBillId");

	/*
	 * public static final StringBuilder RETRIEVE_INVOICE_CLOSE_DATE = new
	 * StringBuilder() .append(
	 * " From FinancialTransaction billFinancialTransaction,  FinancialTransaction paymentFinancialTransaction,Payment payment "
	 * ) .append(" WHERE billFinancialTransaction.billId = :currentBillId")
	 * .append
	 * (" AND paymentFinancialTransaction.financialTransactionType = :payment ")
	 * .append(
	 * " AND paymentFinancialTransaction.matchEventId = billFinancialTransaction.matchEventId "
	 * ) .append(" AND paymentFinancialTransaction.parentId = payment.id ")
	 * .append(" AND payment.paymentStatus = :frozen") .append(
	 * " AND (paymentFinancialTransaction.matchEventId is NOT NULL AND paymentFinancialTransaction.matchEventId!=' ') "
	 * );
	 */

	public static final StringBuilder RETRIEVE_INVOICE_CLOSE_DATE = new StringBuilder()
			.append(" From FinancialTransaction financialTransaction where financialTransaction.matchEventId in  ")
			.append(" (select  innerFt.matchEventId from FinancialTransaction innerFt ")
			.append(" WHERE innerFt.isFrozen = 'Y' AND innerFt.billId = :billId  ")
			.append(" AND innerFt.matchEventId is NOT NULL AND innerFt.matchEventId!=' ') ");

	public static final StringBuilder RETRIEVE_COVERAGE_PERIOD = new StringBuilder()
			.append(" From BillSegment  billSegment")
			.append(" WHERE billSegment.billId = :currentBillId");

	public static final StringBuilder RETRIEVE_PREVIOUS_BILL = new StringBuilder()
			.append(" From Bill  bill ")
			.append(" WHERE bill.account = :account")
			.append(" AND bill.id <> :currentBillId")
			.append(" AND bill.completedDatetime < (select bill.completedDatetime from Bill bill where bill.id = :currentBillId)	");

	public static final StringBuilder RETRIEVE_PREVIOUS_BALANCE = new StringBuilder()
			.append(" From FinancialTransaction financialTransaction ")
			.append(" WHERE financialTransaction.serviceAgreement in (select serviceAgreement.id from ServiceAgreement serviceAgreement where serviceAgreement.account = :account)")
			.append(" AND financialTransaction.billId != :currentBillId ")
			.append(" AND ((financialTransaction.financialTransactionType IN ('PS','PX') AND financialTransaction.freezeDateTime < :previousBillDateTime) ")
			.append(" OR (financialTransaction.financialTransactionType IN ('BS','BX','AD','AX') AND financialTransaction.freezeDateTime <= :previousBillDateTime))");

	public static final StringBuilder RETRIEVE_PAYMENTS = new StringBuilder()
			.append(" From FinancialTransaction financialTransaction, Payment payment ")
			.append(" WHERE financialTransaction.serviceAgreement in (select serviceAgreement.id from ServiceAgreement serviceAgreement where serviceAgreement.account = :account)")
			.append(" AND (financialTransaction.financialTransactionType = :payment OR financialTransaction.financialTransactionType = :paymentCancelled) ")
			.append(" AND financialTransaction.parentId = payment.id ")
			.append(" AND (payment.paymentStatus = :frozen OR payment.paymentStatus = :cancelled) ")
			.append(" AND financialTransaction.freezeDateTime >= :previousCompleteDateTime")
			.append(" AND financialTransaction.freezeDateTime <= :currentCompleteDateTime ");

	public static final StringBuilder RETRIEVE_BILL_GROUP_ADJUSTMENTS = new StringBuilder()
			.append(" From FinancialTransaction financialTransaction, Adjustment  adjustment   ")
			.append(" WHERE financialTransaction.serviceAgreement in (select serviceAgreement.id from ServiceAgreement serviceAgreement where serviceAgreement.account = :account)")
			.append(" AND (financialTransaction.financialTransactionType = :adjustment OR financialTransaction.financialTransactionType = :adjustmentCancelled) ")
			.append(" AND financialTransaction.siblingId = adjustment.id ")
			.append(" AND financialTransaction.shouldShowOnBill = :shouldShowOnBill ")
			.append(" AND (adjustment.adjustmentStatus = :frozen OR adjustment.adjustmentStatus = :cancelled) ")
			.append(" AND financialTransaction.freezeDateTime >= :previousCompleteDateTime")
			.append(" AND financialTransaction.freezeDateTime <= :currentCompleteDateTime ");

	public static final StringBuilder RETRIEVE_CURRENT_CHARGES_FOR_CURRENT_PERIOD = new StringBuilder()
			.append(" From FinancialTransaction financialTransaction, ")
			.append("      BillSegment billSegment, Bill billouter ")
			.append(" WHERE  ")
			.append("     financialTransaction.billId  = :currentBillId ")
			.append("     AND financialTransaction.isFrozen = :isfrozen ")
			.append("     AND billouter.id  = billSegment.billId ")
			.append("     AND financialTransaction.financialTransactionType IN (:bseg,:bsegCancelled) ")
			.append("     AND billSegment.id = financialTransaction.siblingId ")
			.append("	  AND billSegment.startDate >= :coverageStartDt ")
			.append("	  AND billSegment.endDate <= :coverageEndDt ")
			.append(" AND NOT EXISTS ")
			.append("   (SELECT billSegmentInn.id ")
			.append("   FROM BillSegment billSegmentInn, Bill billinner  ")
			.append("   WHERE  ")
			.append("   billSegmentInn.billId <> financialTransaction.billId ")
			.append(" 	AND billSegmentInn.billId = billinner.id ")
			.append("   AND (billSegmentInn.billSegmentStatus = :frozen or billSegmentInn.billSegmentStatus = :cancelled)  ")
			.append("   AND billinner.completedDatetime <= billouter.completedDatetime ")
			.append("   AND billSegmentInn.serviceAgreement = financialTransaction.serviceAgreement ")
			.append("   AND billSegmentInn.startDate = billSegment.startDate ")
			.append("   AND billSegmentInn.endDate = billSegment.endDate )  ");

	public static final StringBuilder RETRIEVE_CURRENT_INVOICE_PREVIOUSLY_NON_BILLED_CHARGES = new StringBuilder()
			.append(" From FinancialTransaction financialTransaction, ")
			.append("      BillSegment billSegment, Bill billouter ")
			.append(" WHERE  ")
			.append("     financialTransaction.billId  = :currentBillId ")
			.append("     AND financialTransaction.isFrozen = :isfrozen ")
			.append("     AND billouter.id  = billSegment.billId ")
			.append("     AND financialTransaction.financialTransactionType IN (:bseg,:bsegCancelled) ")
			.append("     AND billSegment.id = financialTransaction.siblingId ")
			.append(" AND NOT EXISTS ")
			.append("   (SELECT billSegmentInn.id ")
			.append("   FROM BillSegment billSegmentInn, Bill billinner  ")
			.append("   WHERE  ")
			.append("   billSegmentInn.billId <> financialTransaction.billId ")
			.append(" 	AND billSegmentInn.billId = billinner.id ")
			.append("   AND (billSegmentInn.billSegmentStatus = :frozen or billSegmentInn.billSegmentStatus = :cancelled)  ")
			.append("   AND billinner.completedDatetime <= billouter.completedDatetime ")
			.append("   AND billSegmentInn.serviceAgreement = financialTransaction.serviceAgreement ")
			.append("   AND billSegmentInn.startDate = billSegment.startDate ")
			.append("   AND billSegmentInn.endDate = billSegment.endDate )  ");

	public static final StringBuilder RETRIEVE_CURRENT_ADJUSTMENTS = new StringBuilder()
			.append(" From FinancialTransaction financialTransaction, ")
			.append("      BillSegment billSegment, Bill billouter ")
			.append(" WHERE  ")
			.append("     financialTransaction.billId  = :currentBillId ")
			.append("     AND financialTransaction.isFrozen = :isfrozen ")
			.append("     AND billouter.id  = billSegment.billId ")
			.append("     AND financialTransaction.financialTransactionType IN (:bseg,:bsegCancelled) ")
			.append("     AND billSegment.id = financialTransaction.siblingId ")
			.append(" AND  EXISTS ")
			.append("   (SELECT billSegmentInn.id ")
			.append("   FROM BillSegment billSegmentInn, Bill billinner  ")
			.append("   WHERE  ")
			.append("   billSegmentInn.billId <> financialTransaction.billId ")
			.append(" 	AND billSegmentInn.billId = billinner.id ")
			.append("   AND (billSegmentInn.billSegmentStatus = :frozen or billSegmentInn.billSegmentStatus = :cancelled)  ")
			.append("   AND billinner.completedDatetime <= billouter.completedDatetime ")
			.append("   AND billSegmentInn.serviceAgreement = financialTransaction.serviceAgreement ")
			.append("   AND billSegmentInn.startDate = billSegment.startDate ")
			.append("   AND billSegmentInn.endDate = billSegment.endDate )  ");

	public static final StringBuilder IS_LATEST_INVOICE = new StringBuilder()
			.append("FROM Bill bill")
			.append(" WHERE bill.id = :billId ")
			.append(" AND bill.completedDatetime = (select max(billinn.completedDatetime) from Bill billinn where billinn.account = :account ) ")
			.append(" AND bill.billStatus = :complete");

	public static final StringBuilder HAS_PENDING_SDD = new StringBuilder()
			.append(" SELECT  BILL_ID FROM CI_BILL_ACH ")
			.append("   WHERE BILL_ID      = :billId ")	
			.append("   AND APAY_CRE_DT IS NOT NULL ")
			.append(" AND (APAY_STOP_USER_ID IS NULL OR APAY_STOP_USER_ID = ' ') ");

	public static final StringBuilder HAS_APPLIED_SDD = new StringBuilder()
			.append("from  AutopayClearingStaging apayClrStg ")
			// Bug 10725 Start Change
			// .append(" WHERE apayClrStg.billId = :billId");
			.append(" WHERE apayClrStg.billId = :billId ")
			.append(" AND apayClrStg.accountAutoPay.account.ruleAutoPaySw = 'N' ");

	public static final StringBuilder RETRIEVE_INVOICE_TRANSACTION_DETAILS =
			new StringBuilder()
					.append(" From FinancialTransaction financialTransaction, Payment payment ")
					.append(" WHERE financialTransaction.financialTransactionType = :payment  ")
					.append(" AND financialTransaction.parentId = payment.id ")
					.append(" AND payment.matchValue = :matchValue ")
					.append(" AND payment.paymentStatus = :frozen  ")
					.append(" AND financialTransaction.serviceAgreement.id IN (SELECT ft.serviceAgreement.id FROM FinancialTransaction ft WHERE ft.billId = :billId) ");
	// .append(" AND payment.matchValue = :billId ")
	// .append(" AND (payment.paymentStatus = :frozen OR payment.paymentStatus = :cancelled) ")
	// .append(" AND financialTransaction.serviceAgreement in (select billSegment.serviceAgreement from BillSegment billSegment where billSegment.billId = :billId ) ");


	// Invoice Transaction Details Query Ends

	// Payment Services Query Starts
	public static final StringBuilder RETRIEVE_ACCOUNT_FROM_BILL_GROUP_AND_PERSON = new StringBuilder()
			.append(" From AccountNumber accountNumber, AccountPerson accountPerson ")
			.append(" WHERE accountPerson.id.person = :customer")
			.append(" AND accountPerson.isMainCustomer = :isMainCustomer ")
			.append(" AND accountNumber.id.account = accountPerson.id.account ")
			.append(" AND accountNumber.id.accountIdentifierType = :accountIdentifierType ")
			.append(" AND accountNumber.accountNumber = :billGroup ")
			.append(" and not EXISTS (select acctChar.id.account from AccountCharacteristic acctChar where accountPerson.id.account=acctChar.id.account and acctChar.id.characteristicType=:previousMainCustomerCharacteristicType) ");

	public static final StringBuilder RETRIEVE_ALL_ACCOUNTS_FOR_BILLING_CUSTOMER = new StringBuilder()
			.append(" From  AccountPerson accountPerson ")
			.append(" WHERE accountPerson.id.person = :customer")
			.append(" AND accountPerson.isMainCustomer = :isMainCustomer ");

	public static final StringBuilder RETRIEVE_ACTIVE_PAYMENTS_LINKED_TO_ACCOUNT = new StringBuilder()
			.append("  From  PaymentTender paymentTender, Payment payment ")
			.append(" WHERE paymentTender.paymentEvent = payment.paymentEvent ")
			.append(" AND paymentTender.tenderStatus = '25' ")		
			.append(" AND (paymentTender.payorAccount IN (:accountList) ")
			.append(" OR ( payment.account IN (:accountList) AND payment.paymentStatus = '50' )) ")
			.append(" AND  payment.paymentEvent.paymentDate between :startDate and :endDate  ");


	public static final StringBuilder RETRIEVE_CANCELLED_PAYMENTS_LINKED_TO_ACCOUNT = new StringBuilder()
			.append("  From  PaymentTender paymentTender, Payment payment ")
			.append(" WHERE paymentTender.paymentEvent = payment.paymentEvent ")
			.append(" AND paymentTender.tenderStatus = '60' ")
			.append(" AND (paymentTender.payorAccount IN (:accountList) ")
			.append(" OR ( payment.account IN (:accountList)  AND  payment.cancelReasonId not in (:peMisAppliedPaymentCancelReasonCodes)))  ");
	
	public static final StringBuilder RETRIEVE_ALL_ONLINE_PAYMENT_REQUESTS_FOR_PERSON_AND_EXTERNAL_SYSTEM = new StringBuilder()
			.append("from C1Request request,")
			.append(" C1RequestRelatedObject requestRelatedObj , C1RequestCharacteristic c1RequestCharacteristic")
			.append(" WHERE request.id               = requestRelatedObj.id.c1Request ")
			.append(" AND requestRelatedObj.id.maintenanceObject            = :maintenanceObject ")
			.append(" AND requestRelatedObj.id.c1RequestRelationshipObjectType = :requestRelationshipObjTypeFlg ")
			.append(" AND requestRelatedObj.primaryKeyValue1 = :personId ")
			.append(" AND requestRelatedObj.id.c1Request = c1RequestCharacteristic.id.c1Request ")
			.append(" AND c1RequestCharacteristic.id.characteristicType = :externalSystemCharType ")
			.append(" AND request.businessObject                = :paymentRequestBO ")
			.append(" AND c1RequestCharacteristic.searchCharacteristicValue = :externalSystemCdOBS  ")
			.append(" AND trunc(request.creationDateTime) between :startDate and :endDate ");

	public static final StringBuilder RETRIEVE_ALL_IVR_PAYMENT_REQUESTS_FOR_ACCOUNT_AND_EXTERNAL_SYSTEM = new StringBuilder()
			.append("from C1Request request,")
			.append(" C1RequestRelatedObject requestRelatedObj , C1RequestCharacteristic c1RequestCharacteristic")
			.append(" WHERE request.id               = requestRelatedObj.id.c1Request ")
			.append(" AND requestRelatedObj.id.maintenanceObject            = :maintenanceObject ")
			.append(" AND requestRelatedObj.id.c1RequestRelationshipObjectType = :requestRelationshipObjTypeFlg ")
			.append(" AND requestRelatedObj.primaryKeyValue1 IN (:accountList) ")
			.append(" AND requestRelatedObj.id.c1Request = c1RequestCharacteristic.id.c1Request ")
			.append(" AND c1RequestCharacteristic.id.characteristicType = :externalSystemCharType ")
			.append(" AND request.businessObject                = :paymentRequestBO ")
			.append(" AND c1RequestCharacteristic.searchCharacteristicValue = :externalSystemCdIVR  ")
			.append(" AND trunc(request.creationDateTime) between :startDate and :endDate ");

	public static final StringBuilder RETRIEVE_ALL_PAYMENT_REQUESTS_FOR_ACCOUNT_AND_EXTERNAL_SYSTEM = new StringBuilder()
			.append("from C1Request request,")
			.append(" C1RequestRelatedObject requestRelatedObj , C1RequestCharacteristic c1RequestCharacteristic")
			.append(" WHERE request.id               = requestRelatedObj.id.c1Request ")
			.append(" AND requestRelatedObj.id.maintenanceObject            = :maintenanceObject ")
			.append(" AND requestRelatedObj.id.c1RequestRelationshipObjectType = :requestRelationshipObjTypeFlg ")
			.append(" AND requestRelatedObj.primaryKeyValue1 = :accountId ")
			.append(" AND requestRelatedObj.id.c1Request = c1RequestCharacteristic.id.c1Request ")
			.append(" AND c1RequestCharacteristic.id.characteristicType = :externalSystemCharType ")
			.append(" AND request.businessObject                = :paymentRequestBO ")
			.append(" AND c1RequestCharacteristic.searchCharacteristicValue = :externalSystemCd ");

	public static final StringBuilder RETRIEVE_PAYMENTS_FROM_PAYEVENT = new StringBuilder()
			.append("from Payment payment ")
			.append(" WHERE payment.paymentEvent = :payEvent");

	public static final StringBuilder RETRIEVE_PAYMENTS_LINKED_TO_AUTOPAY = new StringBuilder()
			.append("from AccountAutopay accountAutoPay, ")
			.append(" AutopayClearingStaging apayClrStg, ")
			.append(" PaymentTender paymentTender ")
			.append(" WHERE accountAutoPay.account IN (:accountList) ")
			.append(" AND accountAutoPay.id = apayClrStg.accountAutoPay.id ")
			.append(" AND apayClrStg.paymentTenderId = paymentTender.id ")
			.append(" AND ( apayClrStg.scheduleExtractDate is null OR (apayClrStg.scheduleExtractDate between :startDate and :endDate)) ");

	public static final StringBuilder RETRIEVE_APAY_STAGING_UPLOAD_FROM_APAY_CLR_STG = new StringBuilder()
			.append(" from AutopayClearingStaging apayClrStg, ")
			.append("  AutoPayStagingUpload apayStagingUpload ")
			.append(" WHERE apayStagingUpload.autopayClearing.id = apayClrStg.id")
			.append(" AND apayClrStg.id = :apayClrStgId ");

	public static final StringBuilder RETRIEVE_BILLING_CUSTOMER_FROM_ID_TYPE_AND_VALUE = new StringBuilder()
			.append(" from PersonId personId ")
			.append(" WHERE personId.id.idType = :idType")
			.append(" AND personId.personIdNumber = :idValue ");

	public static final StringBuilder GET_PAYMENT_STATUS = new StringBuilder()
			.append(" from Payment payment   ")
			.append(" WHERE payment.paymentEvent = :paymentEvent ")
			.append(" AND (payment.paymentStatus = :frozen OR payment.paymentStatus = :error OR payment.paymentStatus = :cancelled)   ");

	public static final StringBuilder CAN_DELETE_OR_MODIFY_ONLINE_PAYMENT_METHOD = new StringBuilder("SELECT CR.C1_REQ_ID FROM C1_REQUEST CR, F1_BUS_OBJ_STATUS BO ")
			.append(" WHERE CR.BUS_OBJ_CD = :businessObject AND CR.BO_DATA_AREA IS NOT NULL ")
			.append(" AND (EXTRACTVALUE(XMLTYPE(CR.BO_DATA_AREA) ,'/paymentDetails/input/paymentMethodId')) = (SELECT 'paymentMethodIdValue' FROM DUAL)")
			.append(" AND CR.BUS_OBJ_CD = BO.BUS_OBJ_CD AND CR.BO_STATUS_CD = BO.BO_STATUS_CD ")
			.append(" AND BO.BO_STATUS_COND_FLG = :intrimStatus ");

	public static final StringBuilder RETRIEVE_PROCESSED_PAYMENT_REQUESTS_FOR_PAYEVENT = new StringBuilder()
			.append("from C1Request request  ")
			.append(" , C1RequestCharacteristic c1RequestCharacteristic ")
			.append(" WHERE request.id = c1RequestCharacteristic.id.c1Request ")
			.append(" AND c1RequestCharacteristic.id.characteristicType = :externalRefIdCharType ")
			.append(" AND c1RequestCharacteristic.searchCharacteristicValue = :payEventId ")
			.append(" AND request.status                = :processedStatus ");

	public static final StringBuilder IS_FIRST_BILL = new StringBuilder()
			.append("FROM Bill bill")
			.append(" WHERE bill.id = :billId ")
			.append(" AND bill.completedDatetime = (select min(billinn.completedDatetime) from Bill billinn where billinn.account = :account AND billinn.billStatus = :complete) ")
			.append(" AND bill.billStatus = :complete");
	
	public static final StringBuilder GET_EXCESS_CREDIT_AND_BINDER_PAYMENT_BALANCE = new StringBuilder()
			.append("FROM ServiceAgreement sa, FinancialTransaction ft")
			.append(" WHERE sa.account = :account ")	
			.append(" AND sa.serviceAgreementType.id.saType in (select RPAD(wfmOpt.value,8,' ') ")	
			.append(" from FeatureConfigurationOption wfmOpt  where wfmOpt.id.workforceManagementSystem = :featureConfiguration AND wfmOpt.id.optionType = :featureConfigOptionType) ")
			.append(" AND sa.id = ft.serviceAgreement.id AND ft.isFrozen = :frozen ");
	
	//below query retrieves the match event ids associated to payment event
	public static final StringBuilder GET_PAYMENT_MATCH_EVENTS = new StringBuilder()
			.append("FROM Payment payment, FinancialTransaction ft ")
			.append(" WHERE payment.paymentEvent = :paymentEvent ")
			.append(" AND payment.paymentStatus = :frozenPaymentStatus ")
			.append(" AND ft.parentId = payment.id ")
			.append(" AND ft.financialTransactionType = :payment ")
			.append(" AND ft.isFrozen = :isFrozen ")
			.append(" AND ft.matchEventId <> ' ' ");
	
	//below query checks if any rev FT exists that has ME matching to requested ME
	public static final StringBuilder CHECK_IF_PAYMENT_APPLIED_ON_RECEIVABLES = new StringBuilder()
			.append("FROM FinancialTransaction revft ")
			.append(" WHERE revft.matchEventId = :matchEventId ")
			.append(" AND  revft.billId        <> ' ' ");
	
	public static final StringBuilder CHECK_IF_PAYMENT_APPLIED_ON_EXCESS = new StringBuilder()
			.append("FROM Payment payment,  FinancialTransaction ft , ServiceAgreement serviceAgreement ")
			.append(" WHERE payment.id = ft.parentId ")
			.append(" AND ft.financialTransactionType = :payment ")
			.append(" AND ft.isFrozen = :isFrozen")
			.append(" AND ft.serviceAgreement = serviceAgreement.id ")
			.append(" AND serviceAgreement.serviceAgreementType.id.saType  in (select RPAD(wfmOpt.value,8,' ') ")
			.append(" from FeatureConfigurationOption wfmOpt  where wfmOpt.id.workforceManagementSystem = :featureConfiguration AND wfmOpt.id.optionType = :featureConfigOptionType) ")
			.append(" AND payment.paymentEvent = :paymentEvent")
			.append(" AND payment.paymentStatus    = :frozenPaymentStatus");

	public static final StringBuilder CHECK_IF_PAYMENT_IS_PARTIALLY_TRANSFERRED = new StringBuilder()
			.append("FROM Payment payment,  FinancialTransaction ft ")
			.append(" WHERE payment.id = ft.parentId ")
			.append(" AND ft.financialTransactionType = :payment ")
			.append(" AND ft.isFrozen = :isFrozen")
			.append(" AND payment.account not in (select paymentTender.payorAccount from PaymentTender paymentTender  ")
			.append(" where paymentTender.paymentEvent = payment.paymentEvent ) ")
			.append(" AND payment.matchTypeId = :interCompanyTransferMatchType")
			.append(" AND payment.paymentEvent = :paymentEvent")
			.append(" AND payment.paymentStatus    = :frozenPaymentStatus");

	public static final StringBuilder GET_MOST_CURRENT_ACTIVITY_DATE_ON_PAY_EVENT = new StringBuilder()
			.append("FROM Payment payment,  FinancialTransaction ft ")
			.append(" WHERE payment.id = ft.parentId ")
			.append(" AND ft.financialTransactionType = :payment ")
			.append(" AND ft.isFrozen = :isFrozen")
			.append(" AND payment.paymentEvent = :paymentEvent")
			.append(" AND payment.paymentStatus    = :frozenPaymentStatus");

	public static final StringBuilder GET_PAY_EVENT_FROM_TENDER_REFERENCE_ID = new StringBuilder()
			.append("  From  PaymentTender paymentTender ")
			.append(" WHERE paymentTender.externalReferenceId = :externalReferenceId");

	
	public static final StringBuilder GET_REFUND_WRITEOFF_ADJUSTMENTS = new StringBuilder()
			.append(" SELECT ")
			.append("	   TRUNC(BLPDRW.PAID_ACT_FREEZE_DT) TRANSACTION_DATE,")
			.append("	   CASE")
			.append("	      WHEN BLPDRW.FT_TYPE_FLG = :adjCancelled AND RQT.REF_WO_ACTION_FLG = :refundRequestTypeFlag ")
			.append("	     THEN 'Refund Voided'")
			.append("      WHEN BLPDRW.FT_TYPE_FLG = :adjustment AND RQT.REF_WO_ACTION_FLG = :refundRequestTypeFlag")
			.append("	     THEN 'Refund Issued'")
			.append("       WHEN BLPDRW.FT_TYPE_FLG = :adjustment AND RQT.REF_WO_ACTION_FLG = :woRequestTypeFlag ")
			.append("	     THEN 'Write Off Issued'")
			.append("       ELSE")
			.append("       'Write Off Cancelled'")
			.append("	   END AS TRANSACTION_TYPE ,")
			.append("	   SUM(BLPDRW.PAID_ACT_AMT) PAID_ACT_AMT,")
			.append("	   BLPDRW.PAID_ACT_ID,")
			.append("	   RQT.REF_WO_ACTION_FLG,")
			.append("                   BLPDRW.BILL_DT")
			.append(" FROM C1_REF_WO_REQ RQ,")
			.append("         C1_REF_WO_REQ_TYPE_L RQTL,")
			.append("         C1_REF_WO_REQ_TYPE RQT,")
			.append("         (SELECT RFWOD.REF_WO_REQ_ID AS PAID_ACT_ID ,")
			.append("	      FT.FREEZE_DTTM AS PAID_ACT_FREEZE_DT ,")
			.append("                      FT.CUR_AMT AS PAID_ACT_AMT ,")
			.append("                      BILL.BILL_ID AS BILL_ID,")
			.append("                      BILL.DUE_DT,")
			.append("                      BILL.BILL_DT,")
			.append("                      BILL.ACCT_ID,")
			.append("                      FT.FT_ID ,")
			.append("                      FT.FT_TYPE_FLG,")
			.append("                      FT.SIBLING_ID")
			.append("            FROM CI_FT FT ,")
			.append("	     C1_REF_WO_REQ_DTLS RFWOD ,")
			.append("	     C1_FT_EXT FTEXT,")
			.append("	     CI_BILL BILL")
			.append("          WHERE FT.FT_TYPE_FLG  IN ('AD','AX')")
			.append("             AND FT.FREEZE_SW   = 'Y'")
			.append("             AND FT.SHOW_ON_BILL_SW = 'N'")
			.append("             AND FT.BILL_ID  = ' '")
			.append("             AND FT.FT_ID  = FTEXT.FT_ID(+)")
			.append("             AND BILL.BILL_ID  =")
			.append("                    (SELECT FT1.BILL_ID")
			.append("                       FROM CI_FT FT1")
			.append("                      WHERE FT1.FT_ID = FTEXT.C1_REL_REV_FT_ID")
			.append("                         AND FT1.BILL_ID <> ' '")
			.append("                      UNION")
			.append("                     SELECT BILL_ID")
			.append("                       FROM CI_FT FT1")
			.append("                      WHERE FT1.MATCH_EVT_ID = FT.MATCH_EVT_ID")
			.append("                         AND FT1.FT_TYPE_FLG IN ('BX','BS')")
			.append("                         AND FT1.BILL_ID <> ' ')")
			.append("              AND FT.SA_ID IN")
			.append("                    (SELECT SA.SA_ID ")
			.append("                       FROM CI_SA SA ")
			.append("                       , CI_BSEG BSEG")
			.append("                      WHERE ")
			.append("                      SA.SA_ID = BSEG.SA_ID AND BSEG.BILL_ID = :billId ")
			.append("                      )")
			.append("             AND FT.MATCH_EVT_ID <> ' '  ")
			.append("             AND RFWOD.ADJ_ID = FT.SIBLING_ID")
			.append("        ) BLPDRW")
			.append(" WHERE RQ.REF_WO_REQ_ID  = BLPDRW.PAID_ACT_ID")
			.append(" AND BLPDRW.BILL_ID=:billId")
			.append("  AND RQTL.C1_REF_WO_REQ_TYPE_CD = RQ.C1_REF_WO_REQ_TYPE_CD")
			.append("  AND RQ.C1_REF_WO_REQ_TYPE_CD = RQT.C1_REF_WO_REQ_TYPE_CD")
			.append("  AND RQTL.LANGUAGE_CD  = :language ")
			.append(" GROUP BY BLPDRW.ACCT_ID,")
			.append("  BLPDRW.BILL_ID,")
			.append("  BLPDRW.DUE_DT,")
			.append("  BLPDRW.DUE_DT ,")
			.append("  TRUNC(BLPDRW.PAID_ACT_FREEZE_DT),")
			.append("  BLPDRW.FT_TYPE_FLG,")
			.append("  RQTL.DESCR,")
			.append("  BLPDRW.PAID_ACT_ID,")
			.append("  RQT.REF_WO_ACTION_FLG,")
			.append("  BLPDRW.BILL_DT ")
			.append(" ORDER BY TRUNC(BLPDRW.PAID_ACT_FREEZE_DT) ASC ");
	
	public static final CharacteristicType HAS_SERVICE_FEE_AGREEMENT_CHAR_TYPE = new CharacteristicType_Id("CMHASFA").getEntity();
	
	public static final StringBuilder GET_LETTER = new StringBuilder(" SELECT A.CC_ID AS letterId , ")
			// read letters associated to requested bill group and not
			// associated at customer level templates defined in FC
			.append("   TRUNC(A.LETTER_PRINT_DTTM) letterPrintDate, ")
			.append("   B.DESCR description ")
			.append(" FROM CI_LETTER_TMPL_L B, ")
			.append("   CI_CC_CHAR CCHAR, ")
			.append("   CI_CC A, ")
			.append("   CI_ACCT_NBR AN, ")
			.append("   CI_ACCT_PER AP ")
			.append(" WHERE B.LANGUAGE_CD     = :language ")
			.append(" AND B.LTR_TMPL_CD       = A.LTR_TMPL_CD ")
			// check overdue char linked to bill group
			.append(" AND((CCHAR.char_type_cd = :overdueProcChar ")
			.append(" AND CCHAR.CHAR_VAL_FK1 IN ")
			.append("   (SELECT OD_PROC_ID FROM CI_OD_PROC OD WHERE OD.ACCT_ID = AN.ACCT_ID ")
			.append("   )) ")	
			// Check Delinquency Process linked with bill group
			.append(" OR (CCHAR.CHAR_TYPE_CD = :delinquencyProcChar  AND CCHAR.CHAR_VAL_FK1 IN ( SELECT CM_DELIN_PROC_ID ")
			.append("  FROM CM_DELIN_PROC_REL_OBJ DRO, CI_BILL B WHERE B.ACCT_ID=AN.ACCT_ID AND B.BILL_ID =DRO.PK_VALUE1 ")
			.append(" AND MAINT_OBJ_CD='BILL' and CM_DEL_REL_OBJ_TYPE_FLG='CMCO'))  ")
			// check pay tender char linked to bill group
			.append(" OR (CCHAR.char_type_cd    = :payTenderChar ")
			.append(" AND CCHAR.ADHOC_CHAR_VAL IN ")
			.append("   (SELECT PAY_TENDER_ID ")
			.append("   FROM CI_PAY_TNDR PT ")
			.append("   WHERE PT.PAYOR_ACCT_ID = AN.ACCT_ID ")
			.append("   )) ")
			.append(" OR (CCHAR.char_type_cd    = :billGroupIdCharType ")
			.append(" AND CCHAR.ADHOC_CHAR_VAL = :billGrpId ) ")
			// check bill char linked to bill group
			.append(" OR (CCHAR.char_type_cd  = :overdueBillChar ")
			.append(" AND CCHAR.CHAR_VAL_FK1 IN ")
			.append("   (SELECT BILL_ID FROM CI_BILL BL WHERE BL.ACCT_ID = AN.ACCT_ID ")
			.append("   )) ")
			// check policy char linked to bill group
			.append(" OR (CCHAR.char_type_cd  = :policyChar ")
			.append(" AND CCHAR.CHAR_VAL_FK1 IN ")
			.append("   (SELECT POLICY_ID ")
			.append("   FROM CI_POLICY_PER P, ")
			.append("     CI_ACCT_PER AP ")
			.append("   WHERE P.PER_ID = AP.PER_ID ")
			.append("   AND AP.ACCT_ID = AN.ACCT_ID ")
			.append("   ))) ")
			.append(" AND CCHAR.CC_ID        = A.CC_ID ")
			.append(" AND A.PER_ID           =AP.PER_ID ")
			// check cc is not related to letter template at customer level
			.append(" AND A.LTR_TMPL_CD NOT IN ")
			.append("   (SELECT rpad(WOPT.WFM_OPT_VAL,12,' ') ")
			.append("   FROM CI_WFM_OPT WOPT ")
			.append("   WHERE WOPT.WFM_NAME  = :webServicesFeatureConfig ")
			.append("   AND WOPT.EXT_OPT_TYPE=:optionType ")
			.append("   ) ")
			.append(" AND AN.ACCT_NBR     = :billGrpId ")
			.append(" AND AN.ACCT_ID      = AP.ACCT_ID ")
			.append(" AND AP.MAIN_CUST_SW = :isMainCustomer ")
			.append(" AND AP.PER_ID       = :perId ")
			// check cc is eligible for extract
			.append(" AND NOT EXISTS ")
			.append("   (SELECT CC.CC_TYPE_CD ")
			.append("   FROM CI_CHTY_CCTY CC ")
			.append("   WHERE A.CC_TYPE_CD  = CC.CC_TYPE_CD ")
			.append("   AND CC.CHAR_TYPE_CD = :obppExcludedChar ")
			.append("   AND CC.CHAR_VAL     = :obppExcludedCharVal ")
			.append("   ) ")
			.append(" AND TRUNC(A.LETTER_PRINT_DTTM) >= :startDate ")
			.append(" AND TRUNC(A.LETTER_PRINT_DTTM) <= :endDate ")
			.append(" AND A.LETTER_PRINT_DTTM          IS NOT NULL ")
			// read letters associated to customer level templates defined in FC
			.append(" UNION ")
			.append(" SELECT A.CC_ID AS letterId , ")
			.append("   TRUNC(A.LETTER_PRINT_DTTM) letterPrintDate, ")
			.append("   B.DESCR description ")
			.append(" FROM CI_LETTER_TMPL_L B, ")
			.append("   CI_CC A ")
			.append(" WHERE B.LANGUAGE_CD = :language ")
			.append(" AND B.LTR_TMPL_CD   = A.LTR_TMPL_CD ")
			.append(" AND A.PER_ID        = :perId ")
			// check cc is related to letter template at customer level
			.append(" AND A.LTR_TMPL_CD  IN ")
			.append("   (SELECT rpad(WOPT.WFM_OPT_VAL,12,' ') ")
			.append("   FROM CI_WFM_OPT WOPT ")
			.append("   WHERE WOPT.WFM_NAME  = :webServicesFeatureConfig ")
			.append("   AND WOPT.EXT_OPT_TYPE=:optionType ")
			.append("   ) ")
			// check cc is eligible for extract
			.append(" AND NOT EXISTS ")
			.append("   (SELECT CC.CC_TYPE_CD ")
			.append("   FROM CI_CHTY_CCTY CC ")
			.append("   WHERE A.CC_TYPE_CD  = CC.CC_TYPE_CD ")
			.append("   AND CC.CHAR_TYPE_CD = :obppExcludedChar ")
			.append("   AND CC.CHAR_VAL     = :obppExcludedCharVal ")
			.append("   ) ")
			.append(" AND TRUNC(A.LETTER_PRINT_DTTM) >= :startDate ")
			.append(" AND TRUNC(A.LETTER_PRINT_DTTM) <= :endDate ")
			.append(" AND A.LETTER_PRINT_DTTM          IS NOT NULL ")
			.append(" ORDER BY letterPrintDate ASC ");


	public static final CharacteristicType OVERDUE_PROC_CHAR_TYPE = new CharacteristicType_Id("CMODPROC").getEntity();
	public static final CharacteristicType PAY_TENDER_CHAR_TYPE = new CharacteristicType_Id("CMPYTNDR").getEntity();
	public static final CharacteristicType POLICY_CHAR_TYPE = new CharacteristicType_Id("CMPOLICY").getEntity();
	public static final CharacteristicType OBPP_EXCLUDED_CHAR_TYPE = new CharacteristicType_Id("CMOBPPEX").getEntity();

	public static final CharacteristicType CC_BG_ID_CHAR_TYPE = new CharacteristicType_Id("CMBGRPID").getEntity();
	
	public static final CharacteristicType DELINQUENCY_PROC_CHAR_TYPE = new CharacteristicType_Id("CMDQPROC").getEntity();
	
	public static final String OBPP_EXCLUDED_CHAR_VALUE = "Y";
	public static final String WEB_SERVICES_FEATURE_CONFIG = "CM_WSCONFIG";
	public static final String WEB_SERVICES_FEATURE_CONFIG_OPTION_TYPE = "CMCL";

	public static final StringBuilder FETCH_RETRO_DETAILS = new StringBuilder()
			.append(" FROM CmRetroactivityEventPresentationDetail DTL, Policy POL, FinancialTransaction FT, FinancialTransactionExtension FTX ")			
			.append(" WHERE POL.policyNumber = DTL.policyNumber AND POL.id = FTX.policyId ")		
			.append(" AND DTL.entityName <> :pkgSavingsEntityName AND DTL.personIdNumber <> :pkgSavingsPersonIdNbr  ")
			.append(" AND FTX.id = FT.id AND FT.billId = :billId AND FT.isFrozen = 'Y'  AND ( EXISTS (SELECT RETRORELOBJ.id ")
			.append(" FROM CmRetroactivityEventTransactionRelatedObject RETRORELOBJ WHERE RETRORELOBJ.primaryKeyValue1 = FT.siblingId AND FT.financialTransactionType IN ('AD','AX')  ")
			.append(" AND RETRORELOBJ.id.relatedObjectType ='CMAD' AND RETRORELOBJ.id.maintenanceObject = 'ADJUSTMENT'  ")
			.append(" AND RETRORELOBJ.id.cmRetroactivityEventTransaction.id.externalSource = DTL.id.externalSource ) ")
			.append(" OR EXISTS (SELECT RETRORELOBJ.id FROM CmRetroactivityEventTransactionRelatedObject RETRORELOBJ, BillSegmentCalculationHeader BC ")
			.append(" WHERE BC.id.billSegment = FT.siblingId AND FT.financialTransactionType IN ('BS','BX') AND BC.id.headerSequence = 1  AND RETRORELOBJ.primaryKeyValue1 =  BC.billableChargeId ")
			.append(" AND RETRORELOBJ.id.relatedObjectType ='CMBC' AND RETRORELOBJ.id.maintenanceObject = 'BILL CHARGE' ")
			.append(" AND RETRORELOBJ.id.cmRetroactivityEventTransaction.id.externalSource = DTL.id.externalSource )) ");
	
	public static final StringBuilder FETCH_APAY_SRC = new StringBuilder()
			.append(" from AutopaySource ap where ap.autopayRouteType=:autopayRouteType and rownum=1 ");
	
	public static final StringBuilder GET_FINAL_INVOICE_REQ_FROM_BILL = new StringBuilder()
			.append(" from C1RequestLog log where log.characteristicTypeId = :billCharType and log.characteristicValueForeignKey1 = :billId ");
	
	public static final StringBuilder NON_MEMBER_FEE_FROM_ADJ_TYP_ON_BILL_QUERY = new StringBuilder()
			.append(" From FinancialTransaction financialTransaction,FinancialTransactionExtension extension, ")
			.append(" AdjustmentType_Language adjustLang, AdjustmentTypeCharacteristic adjustChar ")
			.append(" WHERE financialTransaction.financialTransactionType in ('AD','AX') ")
			.append(" AND financialTransaction.isFrozen = 'Y' AND extension.policyId IN ( :policyIdList )  ")
			.append(" AND financialTransaction.parentId = adjustLang.id.parent AND adjustLang.id.language = :languageCd ")
			.append(" AND adjustLang.id.parent = adjustChar.id.adjustmentType AND adjustChar.id.characteristicType = :invoiceCategoryCharType ")
			.append(" AND adjustChar.characteristicValue = :invoiceCategoryFeeCreditCharValue ")
			.append(" AND adjustChar.id.sequence = (select max(adChar.id.sequence)  FROM AdjustmentTypeCharacteristic adChar ")
			// .append(" WHERE adChar.id.adjustmentType = adjustChar.id.adjustmentType AND  adChar.id.characteristicType = adjustChar.id.characteristicType )")
			.append(" WHERE adChar.id.adjustmentType = financialTransaction.parentId AND  adChar.id.characteristicType = :invoiceCategoryCharType )")
			// non member fees from adjustments shown on bill
			.append(" AND (( financialTransaction.billId =:currentBillId AND financialTransaction.id = extension.id )")
			// non member fees from adjustments not shown on bill
			.append(" OR  (financialTransaction.billId =' ' AND financialTransaction.matchEventId <> ' ' AND financialTransaction.freezeDateTime <= :currentBillDateTime ")
			.append("  AND financialTransaction.serviceAgreement in (select service.id from ServiceAgreement service where service.account = :account) ")
			.append(" AND extension.id in (select min(extensionInn.id) From FinancialTransaction financialTransactionInn, ")
			.append(" FinancialTransactionExtension extensionInn WHERE financialTransactionInn.matchEventId = financialTransaction.matchEventId ")
			// .append(" CmFinancialTransactionExtension extensionInn WHERE financialTransactionInn.matchEventId = financialTransaction.matchEventId ")
			// .append(" AND extensionInn.startDate is not null AND extensionInn.endDate is not null AND financialTransactionInn.id = extensionInn.id AND extensionInn.policyId = extension.policyId)");
			.append(" AND extensionInn.startDate is not null AND extensionInn.endDate is not null AND financialTransactionInn.id = extensionInn.id AND extensionInn.policyId IN ( :policyIdList ) )");
	
	public static final StringBuilder NON_MEMBER_FEE_FROM_BSEG_QUERY = new StringBuilder()
			.append(" From FinancialTransaction financialTransaction,FinancialTransactionExtension extension, ")
			.append(" BillSegment billSeg, BillSegmentCalculationHeader billSegCalc, ")
			.append(" BillableCharge billCharge WHERE financialTransaction.billId =:currentBillId ")
			.append(" AND financialTransaction.financialTransactionType in ('BS','BX') ")
			.append(" AND financialTransaction.isFrozen = 'Y' AND financialTransaction.id = extension.id ")
			.append(" AND extension.policyId IN ( :policyIdList ) ")
			.append(" AND financialTransaction.siblingId = billSeg.id ")
			.append(" AND billSegCalc.id.billSegment =  billSeg.id AND extension.membershipId = ' ' ")
			.append(" AND billSegCalc.billableChargeId = billCharge.id AND billSegCalc.id.headerSequence = 1  ")
			// read package saving fees
			.append(" AND ((EXISTS (SELECT priceItemChar.id FROM PriceItemChar priceItemChar WHERE  ")
			.append(" priceItemChar.id.priceItemCode = billCharge.priceItemCodeId  ")
			.append("AND priceItemChar.id.characteristicType = :productCatCharType ")
			.append("AND priceItemChar.searchCharacteristicValue = :packagedSavingCharVal ")
			.append("AND priceItemChar.id.effectiveDate = (select max(priceChar.id.effectiveDate) ")
			.append("FROM PriceItemChar priceChar ")
			.append("WHERE priceChar.id.priceItemCode = priceItemChar.id.priceItemCode ")
			.append("AND  priceChar.id.characteristicType = priceItemChar.id.characteristicType ")
			.append("AND priceChar.id.effectiveDate <= billSeg.startDate ) ) ) OR (EXISTS ")


			// read non member fee from billable charge
			.append(" (SELECT billChargeChar.id FROM BilllableChargeCharacteristic billChargeChar ")
			.append(" WHERE billCharge.id = billChargeChar.id.billableCharge.id ")
			.append(" AND billChargeChar.id.characteristicType = :invoiceCategoryCharType ")
			.append(" AND billChargeChar.id.effectiveDate = (select max(billChar.id.effectiveDate) ")
			.append(" FROM BilllableChargeCharacteristic billChar ")
			.append(" WHERE billChar.id.billableCharge = billChargeChar.id.billableCharge ")
			.append(" AND  billChar.id.characteristicType = billChargeChar.id.characteristicType ")
			.append(" AND billChar.id.effectiveDate <= billSeg.startDate )")
			.append(" AND billChargeChar.searchCharacteristicValue = :invoiceCategoryFeeCreditCharValue )))");
	public static final StringBuilder PENDING_INVOICE_QUERY = new StringBuilder()
			.append(" FROM Bill BILL, Account ACCT  ")
			.append(" WHERE BILL.billStatus = 'C'    ")
			.append(" AND BILL.id between :low and :high  ")
			// .append(" AND BILL.billDate >= :cutOffDate ")
			.append(" AND BILL.billDate >= :billDateFrom ")
			.append(" AND NOT EXISTS  ")
			.append(" (SELECT I.id FROM CmBillAccountSummary I WHERE BILL.id = I.id)   ")
			.append(" AND ACCT.id = BILL.account AND ACCT.divisionId IN ( :divisionListFromBatchParameter ) ")
			.append(" AND NOT EXISTS  ")
			.append(" ( SELECT ACCHAR.id FROM AccountCharacteristic ACCHAR ")
			.append(" WHERE ACCHAR.id.account = ACCT.id AND ACCHAR.id.characteristicType = 'CMINVTP' ")
			.append(" AND ACCHAR.characteristicValue IN ('FEE','SELF'))  ");

	public static final StringBuilder MEMBERSHIP_ADJ_TXN_PR_POL = new StringBuilder()
			.append(" SELECT DTL.POLICY_ID, DTL.PLAN_ID, DTL.PER_ID PERSONID, BS.START_DT, BS.END_DT, ")
			.append(" DTL.CM_GRP_PLAN_TXT GROUPNAME , DTL.CM_PLAN_DESCRIPTION PLANNAME,PID.PER_ID_NBR MEMBERIDNUMBER, ")
			.append(" PN.ENTITY_NAME PERSONNAME, DTL.CM_COVERAGE_TYPE_CD COVERAGETIER, DTL.CM_RATE_COVRG_CNTRACT_TYPE_FLG RATECOVERAGE, ")
			// .append(" DTL. CM_RETROTYPE_FLG, DTL.CM_ELIGIBILITY_STAT_TYPE_CD, SUM(DTL.CUR_AMT) CUR_AMT, SUM(DTL.COUNT) COUNT, SUM(DTL.CM_VOLUME) CM_VOLUME")
			.append(" DTL. CM_RETROTYPE_FLG, DTL.CM_ELIGIBILITY_STAT_TYPE_CD, SUM(DTL.CUR_AMT) CUR_AMT, SUM(DTL.CM_VOLUME) CM_VOLUME")
			.append(" FROM CM_BILL_DETAIL DTL, CM_BILL_ACCT_SUMMARY SUMMARY, CI_BSEG BS,  CI_PER_ID PID, CI_PER_NAME PN, (SELECT DTLSTR.POLICY_ID ")
			.append(" ||DTLSTR.PER_ID ||DTLSTR.START_DT ||DTLSTR.END_DT STR FROM (SELECT DTLIN.POLICY_ID, DTLIN.PER_ID, DTLIN.START_DT, DTLIN.END_DT , ")
			.append(" SUM(DTLIN.CUR_AMT) AMOUNT FROM CM_BILL_DETAIL DTLIN, CM_BILL_ACCT_SUMMARY SUMMARYIN WHERE DTLIN.CM_CHARGE_CATEGORY_FLG = 'CMMT' AND SUMMARYIN.BILL_ID = DTLIN.BILL_ID ")
			.append(" AND ((DTLIN.START_DT <> :coverageStartDt AND SUMMARYIN.CM_INVOICE_TYPE_FLG = 'ELEG') OR ( SUMMARYIN.CM_INVOICE_TYPE_FLG <> 'ELEG'))  AND DTLIN.BILL_ID = :billId   ")
			.append(" GROUP BY DTLIN.POLICY_ID, DTLIN.PER_ID, DTLIN.START_DT, DTLIN.END_DT ) DTLSTR WHERE DTLSTR.AMOUNT <> 0 ) COMPARECOMBINATION ")
			.append(" WHERE DTL.bill_id = :billId AND DTL.POLICY_ID ||DTL.PER_ID ||DTL.START_DT||DTL.END_DT = COMPARECOMBINATION.STR ")
			.append(" AND SUMMARY.BILL_ID = DTL.BILL_ID AND DTL.CM_CHARGE_CATEGORY_FLG  = 'CMMT' AND DTL.CM_TRANSACTION_TYPE_FLG = 'PREM' ")
			.append(" AND ((DTL.START_DT <> :coverageStartDt AND SUMMARY.CM_INVOICE_TYPE_FLG = 'ELEG') OR ( SUMMARY.CM_INVOICE_TYPE_FLG <> 'ELEG')) ")
			.append(" AND PN.PER_ID = DTL.PER_ID AND PN.PRIM_NAME_SW  = 'Y' AND PID.PER_ID = DTL.PER_ID AND PID.ID_TYPE_CD =:memberIdType ")
			.append(" AND BS.BSEG_ID = DTL.BSEG_ID AND  DTL.CUR_AMT <> 0 ")
			.append(" GROUP BY DTL.POLICY_ID, DTL.PLAN_ID, DTL.PER_ID, BS.START_DT, BS.END_DT, DTL.CM_COVERAGE_TYPE_CD, DTL.CM_GRP_PLAN_TXT, ")
			.append(" DTL.CM_PLAN_DESCRIPTION, PID.PER_ID_NBR, PN.ENTITY_NAME, DTL.CM_RATE_COVRG_CNTRACT_TYPE_FLG, DTL.CM_RETROTYPE_FLG, ")
			.append(" DTL.CM_ELIGIBILITY_STAT_TYPE_CD ORDER BY PN.ENTITY_NAME, BS.START_DT ");
	public static final StringBuilder RX_OVERLAPING_MEDICAL_ADJUSTMENTS_QUERY = new StringBuilder()
			.append(" FROM CmBillDetail bdrx, CmBillAccountSummary ba, PolicyPlan pprx, CmBillDetail bdmd, PolicyPlan ppmd WHERE bdrx.bill = :billId ")
			.append(" AND ba.id = bdrx.bill AND bdrx.planId = pprx.id  AND ppmd.id =  bdmd.planId AND bdmd.bill = bdrx.bill  ")
			// .append(" AND bdmd.startDate = bdrx.startDate AND bdmd.cmChargeCategory = bdrx.cmChargeCategory AND bdmd.cmTransactionTypeFlag = bdrx.cmTransactionTypeFlag  ")
			.append(" AND bdmd.startDate = bdrx.startDate AND bdmd.id.cmChargeCategory = bdrx.id.cmChargeCategory AND bdmd.cmTransactionTypeFlag = bdrx.cmTransactionTypeFlag  ")
			.append(" AND bdmd.cmRateCvrgContractType = 'M' AND ppmd.startDate <= pprx.startDate ")
			// .append(" AND ( ppmd.endDate IS NULL OR ppmd.endDate >= pprx.endDate ) AND bdrx.cmChargeCategory = 'CMMT' AND bdrx.cmTransactionTypeFlag = 'PREM'  ")
			.append(" AND ( ppmd.endDate IS NULL OR ppmd.endDate >= pprx.endDate ) AND bdrx.id.cmChargeCategory = 'CMMT' AND bdrx.cmTransactionTypeFlag = 'PREM'  ")
			.append(" AND  bdrx.cmRateCvrgContractType = 'R' AND ((bdrx.startDate <> ba.startDate AND ba.invoiceTypeFlag = 'ELEG') OR (ba.invoiceTypeFlag <> 'ELEG')) AND bdrx.personId = bdmd.personId ");

	public static final StringBuilder RX_OVERLAPING_MEDICAL_SUB_FEE_QUERY = new StringBuilder()
			.append(" FROM CmBillDetail bdrx, PolicyPlan pprx, CmBillDetail bdmd, PolicyPlan ppmd WHERE bdrx.bill = :billId ")
			.append(" AND bdrx.planId = pprx.id  AND ppmd.id =  bdmd.planId AND bdmd.bill = bdrx.bill  ")
			// .append(" AND bdmd.startDate = bdrx.startDate AND bdmd.cmChargeCategory = bdrx.cmChargeCategory AND bdmd.cmTransactionTypeFlag = bdrx.cmTransactionTypeFlag  ")
			.append(" AND bdmd.startDate = bdrx.startDate AND bdmd.id.cmChargeCategory = bdrx.id.cmChargeCategory AND bdmd.cmTransactionTypeFlag = bdrx.cmTransactionTypeFlag  ")
			.append(" AND bdmd.descriptionOnBill = bdrx.descriptionOnBill AND bdmd.cmRateCvrgContractType = 'M' AND ppmd.startDate <= pprx.startDate ")
			.append(" AND ( ppmd.endDate IS NULL OR ppmd.endDate >= pprx.endDate ) AND bdrx.cmTransactionTypeFlag = 'FCRD'  ")
			.append(" AND  bdrx.cmRateCvrgContractType = 'R' AND bdrx.personId = bdmd.personId ")
			.append(" AND bdrx.personId <> '          ' AND bdmd.personId <> '          ' AND bdrx.currentAmount <> 0 ");
	
	public static final StringBuilder FETCH_UNPAID_INVOICE_DETAILS = new StringBuilder()
			.append(" SELECT SUM(BLBAL.UNPAID_AMT)	AS UNPAID_AMT ")
			.append(" FROM (SELECT BL.BILL_ID, BL.DUE_DT, ")
			.append(" 	 		   CASE WHEN FT.MATCH_EVT_ID = ' ' THEN FT.CUR_AMT ")
			.append(" 			   ELSE (SELECT SUM(FT2.CUR_AMT) ")
			.append(" 					 FROM CI_FT FT2 ")
			.append(" 		             WHERE FT2.MATCH_EVT_ID = FT.MATCH_EVT_ID ")
			.append(" 			               AND FT2.FREEZE_SW = 'Y') END AS UNPAID_AMT ")
			.append(" 		FROM CI_FT FT, CI_BILL BL ")
			.append("       WHERE BL.ACCT_ID IN (:accountList) ")
			.append("			  AND BL.BILL_STAT_FLG  = :billCompleteStatus ")
			.append("   		  AND FT.BILL_ID = BL.BILL_ID AND FT.FREEZE_SW = 'Y' ")
			.append("             AND BL.DUE_DT <= :processDate AND FT.FT_TYPE_FLG IN ('BS','BX','AD','AX') ")
			.append("   		  AND ((FT.MATCH_EVT_ID = ' ') ")
			.append("   		  OR ((SELECT MEVT.MEVT_STATUS_FLG ")
			.append("			  	   FROM CI_MATCH_EVT MEVT ")
			.append("			       WHERE MEVT.MATCH_EVT_ID = FT.MATCH_EVT_ID) <> 'B'))) BLBAL ");
	
	public static final StringBuilder FETCH_CUSTOMER_ID_NUMBER = new StringBuilder(" FROM PersonId personId ")
			.append(" WHERE personId.id.idType = :cesIdType and personId.id.person = :person ");

	public static final StringBuilder FETCH_RELATED_CUSTOMER_DETAILS = new StringBuilder(" FROM PersonId personId, PersonName personName ")
			.append(" WHERE personId.id.person = personName.id.person.id ")
			.append(" AND personName.isPrimaryName = :true ")
			.append(" AND personId.id.idType = :acisIdType ")
			.append(" AND personId.id.person <> :mainPerson ")
			.append(" AND EXISTS (SELECT perId.id.person ")
			.append(" 			  FROM PersonId perId ")
			.append(" 			  WHERE perId.id.idType = :cesIdType ")
			.append(" 			  AND perId.personIdNumber = :cesIdNumber ")
			.append("             AND perId.id.person = personId.id.person) ");
	
	public static final StringBuilder FETCH_APAY_ROUTE_TYPE_FROM_DIRECT_DEBIT_DAY = new StringBuilder(" select f1_ext_lookup_value from f1_ext_lookup_val ")
			.append(" where f1_ext_lookup_usage_flg = :active and bus_obj_cd = :bo ")
			.append("and extractvalue(xmlType('<root>'||bo_data_area||'</root>'),'root/directDebitDay') = :directDebitDay ");

	public static final StringBuilder CHECK_IF_RULE_BASED_APAY_EXIST_ON_ACCOUNT = new StringBuilder("  from Account account where account.ruleAutoPaySw = 'Y' and  account.id in (:accountIds) ");

	public static final StringBuilder FETCH_FUTURE_BILL_QUERY = new StringBuilder()
			.append(" FROM BillSegment FUTUREBSEG,Bill FUTUREBILL ")
			.append(" WHERE FUTUREBSEG.billId = FUTUREBILL.id AND FUTUREBILL.account = :currentAccount ")
			.append(" AND FUTUREBILL.completedDatetime > :currentCompletedDatetime ")
			.append("  AND EXISTS (SELECT CURRENTBSEG.id FROM BillSegment CURRENTBSEG ")
			.append(" 			WHERE CURRENTBSEG.billId = :currentBill ")
			.append(" 			AND CURRENTBSEG.startDate = FUTUREBSEG.startDate)");
	public static final StringBuilder FETCH_PENDING_HEAD_COUNT_REQUEST_QUERY = new StringBuilder()
			.append(" FROM C1Request request, C1RequestCharacteristic c1RequestCharacteristic, ")
			.append(" BusinessObjectStatus boStatus, C1RequestRelatedObject  requestRelobj ")
			.append(" WHERE request.c1RequestType = :headcountRequestType ")
			.append(" AND request.id    = c1RequestCharacteristic.id.c1Request ")
			.append(" AND c1RequestCharacteristic.id.characteristicType  = :coveragePeriodMonthYearCharType  ")
			.append(" AND c1RequestCharacteristic.searchCharacteristicValue    = :coveragePeriodMonthYear ")
			.append(" AND request.status           = boStatus.id.status ")
			.append(" AND request.businessObject      = boStatus.id.businessObject ")
			.append(" AND boStatus.condition         = :interim ")
			.append(" AND  requestRelobj.id.c1Request = request.id AND requestRelobj.id.maintenanceObject ='CM-CSTHS' ")
			.append(" AND requestRelobj.id.c1RequestRelationshipObjectType = :requestRelationshipObjectType ")
			.append(" AND requestRelobj.primaryKeyValue1 = :headCountStructure ");

	public static final StringBuilder FETCH_OBPP_HEADCOUNT_ADJUST_INVOICE_HEADCOUNT_REQUEST_QUERY = new StringBuilder()
			.append("	FROM C1Request request ")
			.append("	WHERE Exists ( Select c1RequestCharacteristic.searchCharacteristicValue ")
			.append("	 	FROM C1RequestCharacteristic c1RequestCharacteristic ")
			.append("		WHERE request.id    = c1RequestCharacteristic.id.c1Request ")
			.append("	AND c1RequestCharacteristic.id.characteristicType  = :billIdCharType  ")
			.append("	AND c1RequestCharacteristic.searchCharacteristicValue    = :currentBill )")
			.append("	AND request.c1RequestType = :headcountRequestType");

	public static final StringBuilder PENDING_BILL_OF_ACCT_QUERY = new StringBuilder()
			.append(" FROM Bill B WHERE B.account = :account AND B.billStatus = 'P' ");
	
	public static final String HEADCOUNT_STRUCTURE_NODE = "customerHdcntStructure";
	public static final String COVERAGE_PERIOD_MONTH_YEAR_NODE = "coveragePeriodMonthYear";
	
	public static final String BILL_ID_NODE = "obppSelfAdjRequestOriginalBill";

	public static final String GENERATE_OFFSCHEDULE_INVOICE_REQ_NODE = "generateOffScheduleInvoiceRequest";
	public static final String OFFSCHEDULE_INVOICE_REQ_CUTOFF_DT_NODE = "offScheduleInvoiceRequestCutoffDate";
	public static final String REQUEST_TYPE_NODE = "requestType";
	public static final String BO_STATUS_NODE = "boStatus";
	public static final String BUSINESS_OBJECT_NODE = "bo";
	public static final String CUSTOMER_HEADCOUNT_GROUPS_NODE = "customerHeadcountGroups";
	public static final String DESCRIPTION_ON_BILL_NODE = "descriptionOnBill";
	public static final String EMPLOYEE_COUNT_NODE = "headcount";
	public static final String NEW_VOLUME_NODE = "volume";
	public static final String SUCCESS_STATUS_DESC = "Success";
	public static final String CUST_HEADCOUNT_REQUEST_NODE = "request";
	public static final StringBuilder CUST_STRUCT_FROM_BILL_QUERY = new StringBuilder()
			.append(" FROM CmCustomerHeadcountGroupChargeRelatedObject CHGCRO, CmCustomerHeadcountStructure HS, CmCustomerHeadcountGroup HG, BillableCharge BCHG ")
			.append(" WHERE HS.id = HG.cmCustomerHeadcountStructure AND HS.status IN ( :validStructureStatuses) ")
			.append(" AND CHGCRO.id.cmCustomerHeadcountGroupCharge.id.cmCustomerHeadcountGroup.id =  HG.id ")
			.append(" AND CHGCRO.id.maintenanceObject = 'BILL CHARGE' AND CHGCRO.primaryKeyValue1 = BCHG.id ")
			.append(" AND BCHG.billableChargeStatus = '10' AND EXISTS (  ")
			.append(" 	SELECT BCALC.id FROM BillSegment BSEG, BillSegmentCalculationHeader BCALC ")
			.append(" 	WHERE BSEG.billId = :currentBillId AND BSEG.id = BCALC.id.billSegment ")
			.append(" 	AND BCALC.id.headerSequence = '1' AND BCHG.id = BCALC.billableChargeId ")
			.append("   AND ROWNUM = 1) ");

	public static final StringBuilder PENDING_SDD_QUERY = new StringBuilder()
			.append(" FROM BillAch BA, AccountAutopay AP ")
			.append(" WHERE BA.id.billId = :billId ")
			.append(" AND BA.autopayCreationDate IS NOT NULL ")
			.append(" AND BA.autopayStopUserId =' '  ")
			.append(" AND BA.id.accountAutoPayId = AP.id ")
			.append(" AND AP.account.ruleAutoPaySw = 'N' ");
	public static final StringBuilder BILL_ACH_RECORDS_QUERY = new StringBuilder()
			.append(" From BillAch billAch where bill_id = :billId ");
	public static final StringBuilder SELF_BILL_ADJUSTED_INVOICE_QUERY = new StringBuilder()
			
			.append(" FROM BillSegment BS, Bill BI ")
			.append(" WHERE BI.account = :currentAccount ")
			.append(" AND BI.completedDatetime < :currentCompletedDatetime ")
			.append(" AND BI.id = BS.billId AND BS.endDate = :currentCovEndDt ");
	
	public static final StringBuilder ACIS_COVERAGE_PERIOD = new StringBuilder()
			.append("From FinancialTransaction financialTransaction,FinancialTransactionExtension extension ")
			.append("WHERE financialTransaction.billId=:billId ")
			.append("AND financialTransaction.financialTransactionType in('BS','BX','AD','AX') ")
			.append("AND financialTransaction.isFrozen = 'Y' ")
			.append("AND financialTransaction.id = extension.id ");
	
	public static final StringBuilder STATE_ASSESSMENT_FOR_POLICIES = new StringBuilder()
			.append(" From FinancialTransaction financialTransaction,FinancialTransactionExtension extension, ")
			.append(" BillSegment billSeg, BillSegmentCalculationHeader billSegCalc ")
			.append(" WHERE financialTransaction.billId =:currentBillId ")
			.append(" AND financialTransaction.financialTransactionType in ('BS','BX') ")
			.append(" AND financialTransaction.isFrozen = 'Y' AND financialTransaction.id = extension.id ")
			.append(" AND extension.policyId IN ( :policyIdList ) ")
			.append(" AND financialTransaction.siblingId = billSeg.id ")
			.append(" AND billSegCalc.id.billSegment =  billSeg.id AND extension.membershipId = ' ' ")
			.append(" AND billSegCalc.id.headerSequence = 1  ")
			.append(" AND EXISTS (select bsegLineChar.id from BillCalculationLineCharacteristic bsegLineChar ")
			.append(" WHERE bsegLineChar.id.billCalculationLine.id.billSegmentCalculationHeader.id.billSegment = billSegCalc.id.billSegment ")
			.append(" AND bsegLineChar.id.billCalculationLine.id.billSegmentCalculationHeader.id.headerSequence = billSegCalc.id.headerSequence ")
			.append(" AND bsegLineChar.id.characteristicType = :billedStateAssCharType ")
			.append(" AND bsegLineChar.characteristicValue = :billedStateAssCharValue ) ");
	
	public static final StringBuilder RETRIEVE_BILLGRP_DUE_AMT_BAL_BY_PERIOD_MAP = new StringBuilder()
			.append("	SELECT SUM(PERBAL.UNPAID_AMT) AS UNPAID_AMT,	")
			.append("	  PERBAL.BALANCE_PERIOD	")
			.append("	FROM	")
			.append("	  (SELECT AP.PER_ID ,	")
			.append("	    CASE	")
			.append("	      WHEN FT.MATCH_EVT_ID = ' '	")
			.append("	      THEN FT.CUR_AMT	")
			.append("	      ELSE	")
			.append("	        (SELECT SUM(FT2.CUR_AMT)	")
			.append("	        FROM CI_FT FT2	")
			.append("	        WHERE FT2.MATCH_EVT_ID = FT.MATCH_EVT_ID	")
			.append("	        AND FT2.FREEZE_SW      = 'Y'	")
			.append("	        )	")
			.append("	    END AS UNPAID_AMT,	")
			.append("	    CASE	")
			.append("	      WHEN (BL.DUE_DT <= :processDate	")
			.append("	      AND BL.DUE_DT   >= :thresholdDate)	")
			.append("	      THEN 'CURRENT'	")
			.append("	      WHEN (BL.DUE_DT <= :processDate)	")
			.append("	      THEN 'PAST'	")
			.append("	      ELSE 'FUTURE'	")
			.append("	    END AS BALANCE_PERIOD	")
			.append("	  FROM CI_FT FT ,	")
			.append("	    CI_BILL BL ,	")
			.append("	    CI_ACCT_PER AP ,	")
			.append("	    CI_ACCT_NBR ACN	")
			.append("	  WHERE AP.PER_ID          = :personId	")
			.append("	  AND AP.MAIN_CUST_SW      = 'Y'	")
			.append("	  AND AP.ACCT_ID           = ACN.ACCT_ID	")
			.append("	  AND ACN.ACCT_NBR_TYPE_CD = :billGroupIdType	")
			.append("	  AND ACN.ACCT_NBR         = :billGroupIdValue	")
			.append("	  AND AP.ACCT_ID           = BL.ACCT_ID	")
			.append("	  AND BL.BILL_STAT_FLG     = :completeBillStatus	")
			.append("	  AND FT.BILL_ID           = BL.BILL_ID	")
			.append("	  AND FT.FREEZE_SW         = 'Y'	")
			.append("	  AND FT.FT_TYPE_FLG      IN ('BS','BX','AD','AX')	")
			.append("	  AND ((FT.MATCH_EVT_ID    = ' ')	")
			.append("	  OR ((SELECT MEVT.MEVT_STATUS_FLG	")
			.append("	    FROM CI_MATCH_EVT MEVT	")
			.append("	    WHERE MEVT.MATCH_EVT_ID = FT.MATCH_EVT_ID) <> 'B'))	")
			.append("	  ) PERBAL	")
			.append("	GROUP BY PERBAL.BALANCE_PERIOD 	");

	public static final StringBuilder RETRIEVE_BILLGRP_LEVEL_DUE_AMT_ACCT_LIST = new StringBuilder()
			.append("	SELECT ACBAL.ACCT_ID ,	")
			.append("	  SUM(ACBAL.UNPAID_AMT) AS UNPAID_AMT	")
			.append("	FROM	")
			.append("	  (SELECT AP.ACCT_ID ,	")
			.append("	    CASE	")
			.append("	      WHEN FT.MATCH_EVT_ID = ' '	")
			.append("	      THEN FT.CUR_AMT	")
			.append("	      ELSE	")
			.append("	        (SELECT SUM(FT2.CUR_AMT)	")
			.append("	        FROM CI_FT FT2	")
			.append("	        WHERE FT2.MATCH_EVT_ID = FT.MATCH_EVT_ID	")
			.append("	        AND FT2.FREEZE_SW      = 'Y'	")
			.append("	        )	")
			.append("	    END AS UNPAID_AMT	")
			.append("	  FROM CI_FT FT ,	")
			.append("	    CI_BILL BL ,	")
			.append("	    CI_ACCT_PER AP ,	")
			.append("	    CI_ACCT_NBR ACN	")
			.append("	  WHERE AP.PER_ID          = :personId	")
			.append("	  AND AP.MAIN_CUST_SW      = 'Y'	")
			.append("	  AND AP.ACCT_ID           = ACN.ACCT_ID	")
			.append("	  AND ACN.ACCT_NBR_TYPE_CD = :billGroupIdType	")
			.append("	  AND ACN.ACCT_NBR         = :billGroupIdValue	")
			.append("	  AND AP.ACCT_ID           = BL.ACCT_ID	")
			.append("	  AND BL.BILL_STAT_FLG     = 'C'	")
			.append("	  AND FT.BILL_ID           = BL.BILL_ID	")
			.append("	  AND FT.FREEZE_SW         = 'Y'	")
			.append("	  AND FT.FT_TYPE_FLG      IN ('BS','BX','AD','AX')	");

	public static final StringBuilder RETRIEVE_ACCT_FROM_ACCT_NBR_AND_PERSON = new StringBuilder()
			.append("	from AccountNumber accountNumber,	")
			.append("	  AccountPerson accountPerson	")
			.append("	where accountPerson.id.person.id = :personId	")
			.append("	and accountPerson.isMainCustomer = :isMainCustomer	")
			.append("	and accountPerson.id.account.id = accountNumber.id.account.id	")
			.append("	and accountNumber.id.accountIdentifierType = :accountNumberType	")
			.append("	and accountNumber.accountNumber = :accountNumber	");

	public static final String CURRENT = "CURRENT";
	public static final String PAST = "PAST";
	public static final String TOTAL = "TOTAL";
	public static final String FUTURE = "FUTURE";

	public static final StringBuilder DETERMINE_BILLGROUP_ACCT_HIERARCHY = new StringBuilder()
			.append(" from AccountCharacteristic accountChar ")
			.append(" where accountChar.id.characteristicType = :invoiceTypeCharType ")
			.append(" and accountChar.id.account.id in ( ");

}
