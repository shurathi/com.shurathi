/*
 ***********************************************************************
 *                Confidentiality Information:
 *
 * This module is the confidential and proprietary information of
 * RIA Advisory; it is not to be copied, reproduced, or
 * transmitted in any form, by any means, in whole or in part,
 * nor is it to be used for any purpose other than that for which
 * it is expressly provided without the written permission of
 * RIA Advisory.
 *
 * 
 ***********************************************************************
 *
 * PROGRAM DESCRIPTION:
 * 
 *Business Component Extract Header File 
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:          by:           Reason:
 * YYYY-MM-DD    IN     		Reason text.  
 * 2020-05-27     SPatil	    Initial Version.  
***********************************************************************
 */
package com.splwg.cm.domain.accounting.batch;

import com.splwg.base.api.GenericBusinessComponent;


/**
 * @author SPatil
 *
@BusinessComponent ()
 */
public class CmApReqExtractHeaderRecord_Impl extends GenericBusinessComponent implements
CmApReqExtractHeaderRecord {


	// Constants
	private static final String DELIMITER = ",";
	private static final String HEADER1 = "SOURCE";
	private static final String HEADER2 = "CUSTOMER_NUM";
	private static final String HEADER3 = "CUSTOMER_NAME";
	private static final String HEADER4 = "ADDRESS1";
	private static final String HEADER5 = "ADDRESS2";
	private static final String HEADER6 = "ADDRESS3";
	private static final String HEADER7 = "ADDRESS4";
	private static final String HEADER8 = "CITY";
	private static final String HEADER9 = "STATE";
	private static final String HEADER10 = "COUNTRY";
	private static final String HEADER11 = "ZIP";
	private static final String HEADER12 = "INVOICE_NUM";
	private static final String HEADER13 = "INVOICE_DATE";
	private static final String HEADER14 = "INVOICE_AMT";
	private static final String HEADER15 = "CONTROL_AMOUNT";
	private static final String HEADER16 = "INVOICE_CURRENCY_CODE";
	private static final String HEADER17 = "PAYMENT_TERMS";
	private static final String HEADER18 = "PAYMENT_METHOD_LOOKUP_CODE";
	private static final String HEADER20 = "DIST_CODE_CONCATENATED";
	private static final String HEADER21 = "RECEIPT_NUMBER";
	
	/**
	 * Method to construct the header record
	 * @return String header record
	 */
	public String cmCreateRecordString() {
		
		StringBuilder stringBuilder = new StringBuilder();
		
		stringBuilder.append(HEADER1);
		stringBuilder.append(DELIMITER);
		stringBuilder.append(HEADER2);
		stringBuilder.append(DELIMITER);
		stringBuilder.append(HEADER3);
		stringBuilder.append(DELIMITER);
		stringBuilder.append(HEADER4);
		stringBuilder.append(DELIMITER);
		stringBuilder.append(HEADER5);
		stringBuilder.append(DELIMITER);
		stringBuilder.append(HEADER6);
		stringBuilder.append(DELIMITER);
		stringBuilder.append(HEADER7);
		stringBuilder.append(DELIMITER);
		stringBuilder.append(HEADER8);
		stringBuilder.append(DELIMITER);
		stringBuilder.append(HEADER9);
		stringBuilder.append(DELIMITER);
		stringBuilder.append(HEADER10);
		stringBuilder.append(DELIMITER);
		stringBuilder.append(HEADER11);
		stringBuilder.append(DELIMITER);
		stringBuilder.append(HEADER12);
		stringBuilder.append(DELIMITER);
		stringBuilder.append(HEADER13);
		stringBuilder.append(DELIMITER);
		stringBuilder.append(HEADER14);
		stringBuilder.append(DELIMITER);
		stringBuilder.append(HEADER15);
		stringBuilder.append(DELIMITER);
		stringBuilder.append(HEADER16);
		stringBuilder.append(DELIMITER);
		stringBuilder.append(HEADER17);
		stringBuilder.append(DELIMITER);
		stringBuilder.append(HEADER18);
		stringBuilder.append(DELIMITER);
		stringBuilder.append(HEADER20);
		stringBuilder.append(DELIMITER);
		stringBuilder.append(HEADER21);
		return stringBuilder.toString();
	}
}
