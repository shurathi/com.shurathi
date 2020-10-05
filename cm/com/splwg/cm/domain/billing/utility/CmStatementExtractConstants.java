/*
 ***********************************************************************
 *                Confidentiality Information:
 *
 * This module is the confidential and proprietary information of
 * RIA Advisory LLC; it is not to be copied, reproduced, or
 * transmitted in any form, by any means, in whole or in part,
 * nor is it to be used for any purpose other than that for which
 * it is expressly provided without the written permission of
 * RIA Advisory LLC.
 ***********************************************************************
 *
 * PROGRAM DESCRIPTION:
 *
 * Statement Extract Constants
 *
 * This class contains all constants that are required for statement extract
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:                  Reason:
 * 2020-07-06	DDejes		CB-157. Initial Version.
 * 2020-09-09	KGhuge		CB-368.	CUSTNUM Change - Statement
 ***********************************************************************
 */
package com.splwg.cm.domain.billing.utility;

public class CmStatementExtractConstants {
	
	public static final String SAVE_POINT = "SAVE_POINT";
	public static final String EMPTY_STRING = " ";
	public static final String STATEMENT_DA = "CM-StatementExtractRecord";
	public static final String FILEEXTENSION = ".";
	public static final String XMLEXTENSION=".xml";
	public static final String QUESTIONMARKS = "??";
	public static final String COMMA = ",";
	public static final String DIVISION = "CLUS";
	public static final String XML_CHAR = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	public static final String SINGLE_QUOTE = "'";
	public static final String FRONTSLASH="/";	
	public static final String BACKSLASH="\\" ;
	public static final String WRITING_FILE = "writing";
	public static final String CLOSING_FILE = "closing";
	public static final String OPENING_FILE = "opening";
	public static final int TWO = 2;
	
	public static final String PAY_TENDER_CHAR = "CMRECNBR";
	public static final String BATCH_ACTION = "BATCH_ACTION";
	public static final String WARNING = "WARN";
	public static final String NON_ADHOC = "NON-ADHOC";
	public static final String ADHOC_PER = "ADHOC-PER";
	public static final String ADHOC_ACCT = "ADHOC-ACCT";
	public static final String ORDER_NUMBER_CHAR = "CMORDNBR";
	public static final String LOAN_NUMBER_CHAR = "CMLOANBR";
	public static final String BORROWER_CHAR = "CMBRWNME";
	public static final String ADDRESS_CHAR = "CMPROADR";
	public static final String STATE_CHAR = "CMPROSTE";
	public static final String ZIP_CHAR = "CMPROZIP";
	
	public static final String TRANSACTION_ID = "TXN_ID";
	public static final String TRANSACTION_DATE = "TXN_DT";
	public static final String BS_DESC = "BS_CALC_DESC";
	public static final String BILLABLE_CHG_ID = "BILL_CHG_ID";
	public static final String CUR_BAL = "TOT_CUR_BAL";
	public static final String FT_TYPE = "FT_TYPE";
	public static final String ADJUSTMENT = "ADJ";
	public static final String BS = "BSG";
	public static final String PAY = "PAY";

	public static final String CONTRACT_TYPE = "Contract Type";
	public static final String ADJUSTMENT_TYPE = "Adjustment Type";
	public static final String ACCOUNT_ID = "Account Id";
	public static final String ACCOUNT_ID_TYPE = "Account Id Type";
	public static final String CUSTOMER_ID_TYPE = "Customer Id Type";
	public static final String BLANK = "BLANK";
	
	//Data Area Fields
	public static final String STATEMENTS_GROUP = "statements";
	public static final String STATEMENT_LIST = "statement";
	public static final String TEMPLATE_LIST = "template";
	public static final String TEMPLATE_CD = "templateCode";
	public static final String LOGO = "logo";
	public static final String ST_DATE = "statementDate";
	public static final String CUST_NAME = "customerName";
	public static final String CUST_NUM = "customerNumber";
	public static final String BILL_TO_GROUP = "billTo";
	public static final String ADDRESS1 = "addressLine1";
	public static final String ADDRESS2 = "addressLine2";
	public static final String ADDRESS3 = "addressLine3";
	public static final String ADDRESS4 = "addressLine4";
	public static final String CITY = "city";
	public static final String STATE = "state";
	public static final String COUNTY = "county";
	public static final String POSTAL = "postal";
	public static final String COUNTRY = "country";
	public static final String EMAILID = "emailId";
	public static final String REMIT_TO_GROUP = "remitTo";
	public static final String NAME = "name";
	public static final String BUS_UNIT = "businessUnit";
	public static final String PHONE = "phone";
	public static final String ST_DETAILS_GROUP = "statementDetails";
	public static final String ST_DETAIL_LIST = "statementDetail";
	public static final String DATE = "date";
	public static final String CONSOLIDATED_INV_NUM = "consolidatedInvoiceNumber";
	public static final String ORDER_NUM = "orderNumber";
	public static final String DESCR = "description";
	public static final String REM_BAL = "remainingBalance";
	public static final String LOAN_NUM = "loanNumber";
	public static final String BORROWER = "borrower";
	public static final String ADDRESS = "address";
	public static final String SUBTOTAL = "subTotal";
	public static final String GRANDTOTAL = "grandTotal";

	//Start Add CB-368
	public static final String PER_WARNING = "PER";
	public static final String ACCT_WARNING = "ACCT";
	//Start End CB-368


}
