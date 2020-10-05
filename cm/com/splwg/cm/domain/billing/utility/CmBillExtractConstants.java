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
 * Bill Extract Constants
 *
 * This class contains all constants that are required for bill extract
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:                  Reason:
 * 2020-06-15   JFerna               CB-94. Initial Version. 
 * 2020-06-18   JFerna               CB-142. Added COMMA
 ***********************************************************************
 */
package com.splwg.cm.domain.billing.utility;

public class CmBillExtractConstants {

	public static final String BILL_GRP = "bill";
	public static final String TEMPLATE_DTL_GRP = "templateDetail";
	public static final String TEMPLATE_LIST = "templates";
	public static final String TEMPLATE_CD_ELEM = "templateCode";
	public static final String BILL_ROUTING_METHOD_ELEM = "billRoutingMethod";
	public static final String BILL_ID_ELEM = "billId";
	public static final String BILL_DT_ELEM = "billDate";
	public static final String MAIN_CUST_ELEM = "mainCustomerNumber";
	public static final String PAY_TERMS_ELEM = "paymentTerms";
	public static final String BILL_DUE_DT_ELEM = "billDueDate";
	public static final String FEDERAL_TAX_ID_NBR_ELEM = "federalTaxIdNumber";
	public static final String INVOICE_MONTH_ELEM = "invoiceMonth";
	public static final String REMITTANCE_DTL_GRP = "remittanceDetail";
	public static final String BRAND_LOGO_ELEM = "brandLogo";
	public static final String REMITTANCE_NAME_ELEM = "remittanceName";
	public static final String REMITTANCE_ADDR1_ELEM = "remittanceAddress1";
	public static final String REMITTANCE_ADDR2_ELEM = "remittanceAddress2";
	public static final String REMITTANCE_ADDR3_ELEM = "remittanceAddress3";
	public static final String REMITTANCE_ADDR4_ELEM = "remittanceAddress4";
	public static final String REMITTANCE_CITY_ELEM = "remittanceCity";
	public static final String REMITTANCE_COUNTRY_ELEM = "remittanceCountry";
	public static final String REMITTANCE_COUNTY_ELEM = "remittanceCounty";
	public static final String REMITTANCE_STATE_ELEM = "remittanceState";
	public static final String REMITTANCE_POSTAL_ELEM = "remittancePostal";	
	public static final String REMITTANCE_CONTACT_ELEM = "remittanceContact";
	public static final String BANK_NAME_ELEM = "bankName";
	public static final String BANK_ACCT_NBR_ELEM = "bankAccountNumber";
	public static final String WIRE_ROUTING_NBR_ELEM = "wireRoutingNumber";
	public static final String ACH_ROUTING_NBR_ELEM = "achRoutingNumber";
	public static final String BILL_TO_DTL_GRP = "billToDetail";
	public static final String NAME1_ELEM = "name1";
	public static final String NAME2_ELEM = "name2";
	public static final String NAME3_ELEM = "name3";
	public static final String ADDR1_ELEM = "address1";
	public static final String ADDR2_ELEM = "address2";
	public static final String ADDR3_ELEM = "address3";
	public static final String ADDR4_ELEM = "address4";
	public static final String CITY_ELEM = "city";
	public static final String COUNTRY_ELEM = "country";
	public static final String COUNTY_ELEM = "county";
	public static final String STATE_ELEM = "state";
	public static final String POSTAL_ELEM = "postal";
	public static final String EMAIL_ELEM = "email";
	public static final String TRANS_DTL_GRP = "transactionDetail";
	public static final String BILL_START_DT_ELEM = "billStartDate";
	public static final String BILL_END_DT_ELEM = "billEndDate";
	public static final String TRANS_LIST = "transactions";
	public static final String PRODUCT_DESC_ELEM = "productDescription";
	public static final String ORDER_NBR_ELEM = "orderNumber";
	public static final String TRANS_DT_ELEM = "transactionDate";
	public static final String LOAN_NBR_ELEM = "loanNumber";
	public static final String BRW_NAME_ELEM = "borrowerName";
	public static final String PROPERTY_ADDR_ELEM = "propertyAddress";
	public static final String TAX_ELEM = "tax";
	public static final String AMOUNT_ELEM = "amount";
	public static final String PRODUCT_LIST = "products";
	public static final String SUBTOTAL_TAX_ELEM = "subtotalTax";
	public static final String SUBTOTAL_AMOUNT_ELEM = "subtotalAmount";
	public static final String TRANS_SUMMARY_GRP = "transactionSummary";
	public static final String QTY_ELEM = "quantity";
	public static final String RATE_ELEM = "rate";
	public static final String TOTALS_SUMMARY_GRP = "totalsSummary";
	public static final String CURRENT_CHARGES_ELEM = "currentCharges";
	public static final String NET_DUE_AMOUNT_ELEM = "netAmountDue";	
	public static final String FILE_NAME_PREFIX_ELEM = "fileNamePrefix";
	public static final String EXTRACT_DT_FORMAT_ELEM = "extractDateFormat";
	public static final String BILL_PERIOD_DT_FORMAT_ELEM = "billPeriodDateFormat";
	public static final String BILL_TO_COUNTRY_ELEM = "billToCountry";
	public static final String TEMPLATE_MAPPINGS_LIST = "templateMappings";
	public static final String BILL_FORMAT_ELEM = "billFormat";
	public static final String CUSTNUM_ID_TYPE_ELEM = "customerNumberIdentifierType";
	public static final String TRANS_QTY_SQI_ELEM = "transactionQuantitySqi";
	public static final String CHAR_CONFIG_GRP = "characteristicConfiguration";
	public static final String LOAN_NBR_CHAR_TYPE_ELEM = "loanNumberCharacteristicType";
	public static final String ORDER_NBR_CHAR_TYPE_ELEM = "orderNumberCharacteristicType";
	public static final String BRW_NAME_CHAR_TYPE_ELEM = "borrowerNameCharacteristicType";
	public static final String PROPERTY_ADDR_CHAR_TYPE_ELEM = "propertyAddressCharacteristicType";
	public static final String PROPERTY_CITY_CHAR_TYPE_ELEM = "propertyCityCharacteristicType";
	public static final String PROPERTY_STATE_CHAR_TYPE_ELEM = "propertyStateCharacateristicType";
	public static final String PROPERTY_ZIP_CHAR_TYPE_ELEM = "propertyZipCharacteristicType";
	public static final String PAY_TERMS_CHAR_TYPE_ELEM = "paymentTermsCharacteristicType";
	public static final String CHG_CALC_LINE_CHAR_TYPE_ELEM = "chargeCalcLineCharacteristicType";
	public static final String TAX_CALC_LINE_CHAR_VAL_ELEM = "taxCalcLineCharacteristicValue";
	public static final String DISCOUNT_CALC_LINE_CHAR_VAL_ELEM = "discountCalcLineCharacteristicValue";
		
	public static final String XML_FILE_EXTENSION = ".xml";
	public static final String DEFAULT_EXTRACT_FILE_NAME = "XTRACT01.DAT";
	public static final String EMPTY_STRING = "";
	public static final String FALSE = "false";
	public static final String NO = "N";
	public static final String WRITING_FILE = "writing";
	public static final String EXTRACT_DATE_FORMAT = "Extract Date Format";
	public static final String BILL_PERIOD_DATE_FORMAT = "Bill Period Date Format";
	public static final String BILL_PERIOD_DATE = "Bill Period Date";
	public static final String BANK_BILL_HEADER_EXTRACT_BS = "C1-BnkBillHeaderExtract";
	public static final String INVOICE_SUMMARY_EXTRACT_BS = "C1-InvoiceSummaryExtract";
	public static final String CONDOSAFE_BILL_EXTRACT_RECORD_DA = "CM-CondoSafeBillExtractRecord";
	public static final String OUTPUT_FILE_PATH = "Output directory path";
	public static final String ZERO = "0";	
	public static final int TWO = 2;
	//CB-142 - Start Add
	public static final String COMMA = ",";
	//CB-142 - End Add
	//CB-148 - Start Add
	public static final int INT_ZERO = 0;
	//CB-148 - End Add
}
