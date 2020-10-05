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
 *Business Component GLExtract Control File Header
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
public class CmGLExtractControlHeaderRecord_Impl extends
		GenericBusinessComponent implements CmGLExtractControlHeaderRecord {

	// Constants
			private static final String DELIMITER = ",";
			private static final String HEADER1 = "TOTAL_DEBIT";
			private static final String HEADER2 = "TOTAL_CREDIT";
			private static final String HEADER3 = "TOTAL_COUNT";
			private static final String HEADER4 = "EXTRACTED_DATE";
			private static final String HEADER5 = "BATCH_ID";
			
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
				return stringBuilder.toString();
			}
			
	
}
