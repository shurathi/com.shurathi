/*
 * **********************************************************************
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
 *Business Component GLExtract Control File
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 *    Date:        by:         Reason:
 * YYYY-MM-DD  	   IN     		Reason text.  
 * 2020-08-11     SAnart      Initial Version. 
***********************************************************************
 */

package com.splwg.cm.domain.accounting.batch;

import com.splwg.base.api.GenericBusinessComponent;

/**
 * @author SAnarthe 
 *
@BusinessComponent ()
 */
public class CmGLExtractControlRecord_Impl extends GenericBusinessComponent
		implements CmGLExtractControlRecord {

	//Constants
	private static final String EMPTY_STRING = "";
	private static final String DELIMITER = ",";
	private static final int TOTAL_DEBIT_LENGTH = 15;
	private static final int TOTAL_CREDIT_LENGTH = 15;
	private static final int TOTAL_COUNT_LENGTH = 10;
	private static final int EXTRACTED_DATE_LENGTH = 20;
	private static final int BATCH_ID_LENGTH = 15;	
	
	//Work Variables
    private String totalDebits = EMPTY_STRING;
    private String totalCredits = EMPTY_STRING;
    private String totalCount = EMPTY_STRING;
	private String extractedDate = EMPTY_STRING;
	private String batchId = EMPTY_STRING;
	
    public void setTotalDebits(String totalDebitsVal) {
		this.totalDebits = formatString(totalDebitsVal, TOTAL_DEBIT_LENGTH).trim();
	}


	public void setTotalCredits(String totalCreditsVal) {
		this.totalCredits = formatString(totalCreditsVal, TOTAL_CREDIT_LENGTH).trim();
	}


	public void setTotalCount(String totalCountVal) {
		this.totalCount = formatString(totalCountVal, TOTAL_COUNT_LENGTH).trim();
	}


	public void setExtractedDate(String extractedDateVal) {
		this.extractedDate = formatString(extractedDateVal, EXTRACTED_DATE_LENGTH).trim();
	}


	public void setBatchId(String batchIdVal) {
		this.batchId = formatString(batchIdVal,  BATCH_ID_LENGTH).trim();
	}

	
	/**
	 * Method to construct the customer record
	 * @return String customer record
	 */
	public String cmCreateCtrRecordString() 
	{
		
		StringBuilder stringBuilder = new StringBuilder();
	
		stringBuilder.append(totalDebits);
		stringBuilder.append(DELIMITER);
		stringBuilder.append(totalCredits);
		stringBuilder.append(DELIMITER);
		stringBuilder.append(totalCount);
		stringBuilder.append(DELIMITER);
		stringBuilder.append(extractedDate);
		stringBuilder.append(DELIMITER);
		stringBuilder.append(batchId);
		
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
