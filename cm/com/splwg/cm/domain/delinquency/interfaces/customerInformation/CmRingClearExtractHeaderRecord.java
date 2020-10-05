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
 * Ring Clear Extract Header Record
 * 
 * This program constructs the ring clear extract header record
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

public class CmRingClearExtractHeaderRecord {
	
	// Constants
	private static final String DELIMITER = ",";
	private static final String HEADER1 = "CUSTOMER #";
	private static final String HEADER2 = "PHONE";
	private static final String HEADER3 = "CUSTOMER NAME";
	
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
	
		return stringBuilder.toString();
	}
}
