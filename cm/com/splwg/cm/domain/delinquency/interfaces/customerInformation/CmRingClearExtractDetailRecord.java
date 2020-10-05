/* 
 **************************************************************************
 *           	     Confidentiality Information:
 *
 * This module is the confidential and proprietary information of
 * RIA Advisory; it is not to be copied, reproduced, or
 * transmitted in any form, by any means, in whole or in part,
 * nor is it to be used for any purpose other than that for which
 * it is expressly provided without the written permission of
 * RIA Advisory.
 *                 
 **************************************************************************
 * 
 * PROGRAM DESCRIPTION:
 * 
 * Ring Clear Extract Detail Record
 * 
 * This program constructs the ring clear extract detail record
 * 
 **************************************************************************
 * 
 * CHANGE HISTORY:
 *
 * Date:        by:        Reason:
 * YYYY-MM-DD   IN         Reason text. 
 * 2020-08-26   JFerna     CB-267. Initial	
 **************************************************************************
 */

package com.splwg.cm.domain.delinquency.interfaces.customerInformation;

public class CmRingClearExtractDetailRecord {
	
	// Constants
	private static final String EMPTY_STRING = "";
	private static final String DELIMITER = ",";
	private static final String QUOTE = "\"";
	
	// Work Variables
	private String customerNumber = EMPTY_STRING;
	private String phoneNumber = EMPTY_STRING;
	private String customerName = EMPTY_STRING;
	private StringBuilder stringBuilder = new StringBuilder();

	/**
	 * Method to construct the detail record
	 * @return String detail record
	 */
	public String cmCreateRecordString() {
		stringBuilder.append(customerNumber);
		stringBuilder.append(DELIMITER);
		stringBuilder.append(phoneNumber);
		stringBuilder.append(DELIMITER);
		stringBuilder.append(customerName);
		
		return stringBuilder.toString();
	}
	
	/**
	 * This method will set the Phone Number of the detail record
	 * @param phoneNumberStr - Phone Number
	 */
	public void setPhoneNumber(String phoneNumberStr) {
		phoneNumber = phoneNumberStr;
	}
	
	/**
	 * This method will set the Customer Number of the detail record
	 * @param customerNumberStr - Customer Number
	 */
	public void setCustomerNumber(String customerNumberStr) {
		customerNumber = customerNumberStr;
	}
	
	/**
	 * This method will set the Customer Name of the detail record
	 * @param customerNameStr - Customer Name
	 */
	public void setCustomerName(String customerNameStr) {
		customerName = QUOTE + customerNameStr + QUOTE;
	}
}
