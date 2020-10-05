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
 *Business Component Extract Record File
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 *    Date:        by:         Reason:
 * YYYY-MM-DD  	   IN     		Reason text.  
 * 2020-05-27      SPatil	    Initial Version. 
***********************************************************************
 */
package com.splwg.cm.domain.accounting.batch;


import com.splwg.base.api.GenericBusinessComponent;



/**
 * @author SPatil
 *
@BusinessComponent ()
 */
public class CmApReqExtractRecord_Impl extends GenericBusinessComponent
		implements CmApReqExtractRecord {
	
	//Constants
	private static final String EMPTY_STRING = "";
	private static final String DELIMITER = ",";
	private static final int SOURCE_LENGTH = 9;
	private static final int CUSTOMER_NUM_LENGTH = 90;
	private static final int CUSTOMER_NAME_LENGTH = 254;
	private static final int ADDRESS1_LENGTH = 254;
	private static final int ADDRESS2_LENGTH = 254;
	private static final int ADDRESS3_LENGTH = 254;
	private static final int ADDRESS4_LENGTH = 254;
	private static final int CITY_LENGTH  = 90;
	private static final int STATE_LENGTH = 6;
	private static final int COUNTRY_LENGTH = 3;
	private static final int ZIP_LENGTH = 12;
	private static final int INVOICE_NUM_LENGTH = 25;
	private static final int INVOICE_DATE_LENGTH = 10;
	private static final int INVOICE_AMT_LENGTH = 15;
	private static final int CONTROL_AMOUNT_LENGTH = 1;
	private static final int INVOICE_CURRENCY_CODE_LENGTH=3;
	private static final int PAYMENT_TERMS_LENGTH=9;
	private static final int PAYMENT_METHOD_LOOKUP_CODE_LENGTH=5;
	private static final int DIST_CODE_CONCATENATED_LENGTH=254;
	private static final int RECEIPT_NUMBER_LENGTH=12;
	

    //Work Variables
    private String source = EMPTY_STRING;
	private String customerNumber = EMPTY_STRING;
    private String customerName = EMPTY_STRING;
	private String address1 = EMPTY_STRING;
	private String address2 = EMPTY_STRING;
	private String address3 = EMPTY_STRING;
	private String address4 = EMPTY_STRING;
	private String city = EMPTY_STRING;
	private String state = EMPTY_STRING;
	private String country = EMPTY_STRING;
	private String zip = EMPTY_STRING;
	private String invoiceNumber = EMPTY_STRING;
	private String invoiceDate = EMPTY_STRING;
	private String invoiceAmount = EMPTY_STRING;
	private String controlAmount = EMPTY_STRING;
	private String invoiceCurrencyCode = EMPTY_STRING;
	private String paymentTerms = EMPTY_STRING;
	private String paymentTermsLookupCode = EMPTY_STRING;
	private String distCodeConcat = EMPTY_STRING;
	private String receiptNumber = EMPTY_STRING;

	/**
	 * This method will set the Source of Feature Configuration Option Type
	 * @param sourceVal - Source 
	 */
	public void setSource(String sourceVal) {
		source =   formatString(sourceVal, SOURCE_LENGTH).trim();
	}
	
	/**
	 * This method will set the Customer Number of the AP Extract record
	 * @param customerNumberVal - Customer Number
	 */
	public void setCustomerNum(String customerNumberVal) {
		customerNumber =formatString(customerNumberVal, CUSTOMER_NUM_LENGTH).trim();
	}
	
	/**
	 * This method will set the Customer Name of the AP Extract record
	 * @param customerNameVal - Customer Name
	 */
	public void setCustomerName(String customerNameVal) {
		customerName = formatString(customerNameVal, CUSTOMER_NAME_LENGTH).trim();
	}
	
	/**
	 * This method will set the Address1 of the AP Extract record
	 * @param address1Val - Address1 
	 */
	public void setAddress1(String address1Val) {
		address1 = formatString(address1Val, ADDRESS1_LENGTH).trim();
	}
	
	/**
	 * This method will set the Address2  of the AP Extract record
	 * @param address2Val - Address2
	 */
	public void setAddress2(String address2Val) {
		address2 = formatString(address2Val, ADDRESS2_LENGTH).trim();
	}
	
	/**
	 * This method will set the Address3 of the AP Extract record
	 * @param address3Val - Address3
	 */
	public void setAddress3(String address3Val) {
		address3 = formatString(address3Val, ADDRESS3_LENGTH).trim();
	}
	
	/**
	 * This method will set the Address4  of the AP Extract record
	 * @param address4Val - Address4
	 */
	public void setAddress4(String address4Val) {
		address4 = formatString(address4Val, ADDRESS4_LENGTH).trim();
	}
	/**
	 * This method will set the City of the AP Extract record
	 * @param cityVal - City
	 */
	public void setCity(String cityVal) {
		city =formatString(cityVal, CITY_LENGTH).trim();
	}
	
	/**
	 * This method will set the State  of the AP Extract record
	 * @param stateVal - State
	 */
	public void setState(String stateVal) {
		state = formatString(stateVal, STATE_LENGTH).trim();
	}
	
	/**
	 * This method will set the Country  of the AP Extract record
	 * @param countryVal - Country
	 */
	public void setCountry(String countryVal) {
		country = formatString(countryVal, COUNTRY_LENGTH).trim();
	}
	
	/**
	 * This method will set the Zip of the AP Extract record
	 * @param zipVal - Zip
	 */
	public void setZip(String zipVal) {
		zip = formatString(zipVal, ZIP_LENGTH).trim();
	}
	
	/**
	 * This method will set the Invoice Number  of the AP Extract record
	 * @param invoiceNumVal - Invoice Number
	 */
	public void setInvoiceNum(String invoiceNumVal) {
		invoiceNumber = formatString(invoiceNumVal, INVOICE_NUM_LENGTH).trim();
		
	}
	
	/**
	 * This method will set the Invoice Date of the AP Extract record
	 * @param invoiceDateVal - Invoice Date
	 */
	public void setInvoiceDate(String invoiceDateVal) {
		invoiceDate = formatString(invoiceDateVal, INVOICE_DATE_LENGTH).trim();
	}
	
	/**
	 * This method will set the Invoice Amount of the AP Extract record
	 * @param invoiceAmtVal - Invoice Amount
	 */
	public void setInvoiceAmt(String invoiceAmtVal) {
		invoiceAmount = formatString(invoiceAmtVal, INVOICE_AMT_LENGTH).trim();
	}
	
	/**
	 * This method will set the Control Amount of the Feature Configuration Option Type
	 * @param controlAmtVal - Control Amount
	 */
	public void setControlAmt(String controlAmtVal) {
		controlAmount = formatString(controlAmtVal, CONTROL_AMOUNT_LENGTH).trim();
	}
	/**
	 * This method will set the Invoice Currency Code  of the AP Extract record or Feature Configuration Option Type
	 * @param invoiceCurrCdVal -  Invoice Currency Code
	 */
	public void setInvoiceCurrCode(String invoiceCurrCdVal) {
		invoiceCurrencyCode =formatString(invoiceCurrCdVal, INVOICE_CURRENCY_CODE_LENGTH).trim();
	}
	/**
	 * This method will set the Payment Terms  of the AP Extract record or Feature Configuration Option Type
	 * @param paymentTermsVal - Payment Terms
	 */
	public void setPaymentTerms(String paymentTermsVal) {
		paymentTerms = formatString(paymentTermsVal, PAYMENT_TERMS_LENGTH).trim();
	}
	
	
	/**
	 * This method will set the Payment Method Lookup Code of the Feature Configuration Option Type
	 * @param payMethodLookupcdVal - Payment Method Lookup Code
	 */
	public void setPayMethodLookupCd(String payMethodLookupcdVal) {
		paymentTermsLookupCode = formatString(payMethodLookupcdVal, PAYMENT_METHOD_LOOKUP_CODE_LENGTH).trim();
	}
	
	/**
	 * This method will set the Distribution Code Concatenated of the AP Extract record
	 * @param distcdVal - Distribution Code Concatenated
	 */
	public void setDistCodeConcat(String distcdVal) {
		distCodeConcat = formatString(distcdVal, DIST_CODE_CONCATENATED_LENGTH).trim();
	}
	
	/**
	 * This method will set the Receipt Number of the AP Extract record
	 * @param receiptNumVal - Receipt Number
	 */
	public void setReceiptNumber(String receiptNumVal) {
		receiptNumber =formatString(receiptNumVal, RECEIPT_NUMBER_LENGTH).trim();
	}
	
	/**
	 * Method to construct the customer record
	 * @return String customer record
	 */
	public String cmCreateRecordString() {
		
	StringBuilder stringBuilder = new StringBuilder();
	
	stringBuilder.append(source);
	stringBuilder.append(DELIMITER);
	stringBuilder.append(customerNumber);
	stringBuilder.append(DELIMITER);
	stringBuilder.append(customerName);
	stringBuilder.append(DELIMITER);
	stringBuilder.append(address1);
	stringBuilder.append(DELIMITER);
	stringBuilder.append(address2);
	stringBuilder.append(DELIMITER);
	stringBuilder.append(address3);
	stringBuilder.append(DELIMITER);
	stringBuilder.append(address4);
	stringBuilder.append(DELIMITER);
	stringBuilder.append(city);
	stringBuilder.append(DELIMITER);
	stringBuilder.append(state);
	stringBuilder.append(DELIMITER);
	stringBuilder.append(country);
	stringBuilder.append(DELIMITER);
	stringBuilder.append(zip);
	stringBuilder.append(DELIMITER);
	stringBuilder.append(invoiceNumber);
	stringBuilder.append(DELIMITER);
	stringBuilder.append(invoiceDate);
	stringBuilder.append(DELIMITER);
	stringBuilder.append(invoiceAmount);
	stringBuilder.append(DELIMITER);
	stringBuilder.append(controlAmount);
	stringBuilder.append(DELIMITER);
	stringBuilder.append(invoiceCurrencyCode);
	stringBuilder.append(DELIMITER);
	stringBuilder.append(paymentTerms);
	stringBuilder.append(DELIMITER);
	stringBuilder.append(paymentTermsLookupCode);
	stringBuilder.append(DELIMITER);
	stringBuilder.append(distCodeConcat);
	stringBuilder.append(DELIMITER);
	stringBuilder.append(receiptNumber);
	stringBuilder.append(DELIMITER);
	
	return stringBuilder.toString();
	}
	
	/**
	 * Returns "" if text is null
	 * Truncates text if length exceeds field length
	 * @param text String variable to be formatted
	 * @param fieldLength Length of field
	 * @return formatted text
	 */
	private String formatString(String text, int fieldLength){

		if(text == null){
			text = EMPTY_STRING;
		}else {
			text = text.trim();
			if (text.length() > fieldLength){
				text = text.substring(0, fieldLength).trim();
			}
		}
		return text;
	}
}
