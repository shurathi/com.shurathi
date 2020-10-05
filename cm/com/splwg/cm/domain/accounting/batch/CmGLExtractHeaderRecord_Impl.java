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
 *Business Component GLExtract Record File Header
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

import java.io.File;

import com.splwg.base.api.GenericBusinessComponent;
import com.splwg.shared.environ.ApplicationProperties;

/**
 * @author SAnarthe 
 *
@BusinessComponent ()
 */
public class CmGLExtractHeaderRecord_Impl extends GenericBusinessComponent
		implements CmGLExtractHeaderRecord {
	
	// Constants
		private static final String DELIMITER = ",";
		private static final String HEADER1 = "LEDGER_ID";
		private static final String HEADER2 = "STATUS";
		private static final String HEADER3 = "SET_OF_BOOKS_ID";
		private static final String HEADER4 = "USER_JE_SOURCE_NAME";
		private static final String HEADER5 = "USER_JE_CATEGORY_NAME";
		private static final String HEADER6 = "ACCOUNTING_DATE";
		private static final String HEADER7 = "CURRENCY_CODE";
		private static final String HEADER8 = "DATE_CREATED";
		private static final String HEADER9 = "CREATED_BY";
		private static final String HEADER10 = "ACTUAL_FLAG";
		private static final String HEADER11 = "SEGMENT1";
		private static final String HEADER12 = "SEGMENT2";
		private static final String HEADER13 = "SEGMENT3";
		private static final String HEADER14 = "SEGMENT4";
		private static final String HEADER15 = "SEGMENT5";
		private static final String HEADER16 = "SEGMENT6";
		private static final String HEADER17 = "SEGMENT7";
		private static final String HEADER18 = "SEGMENT8";
		private static final String HEADER20 = "ENTERED_DEBIT";
		private static final String HEADER21 = "ENTERED_CREDIT";
		private static final String HEADER22 = "ACCOUNTED_DEBIT";
		private static final String HEADER23 = "ACCOUNTED_CREDIT";
		private static final String HEADER24 = "PERIOD_NAME";
		private static final String HEADER25 = "DESCRIPTION";
		
		
		public static final String SHARED_VARIABLE="@SHARED_DIR";
		public static final String INSTALLED_VARIABLE="@INSTALL_DIR";
		public static final String SHARED_DIRECTORY = ApplicationProperties
				.getNullableProperty("com.oracle.ouaf.fileupload.shared.directory");
		public static final String INSTALLED_DIRECTORY = System.getenv("SPLEBASE");
		
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
			stringBuilder.append(DELIMITER);
			stringBuilder.append(HEADER22);
			stringBuilder.append(DELIMITER);
			stringBuilder.append(HEADER23);
			stringBuilder.append(DELIMITER);
			stringBuilder.append(HEADER24);
			stringBuilder.append(DELIMITER);
			stringBuilder.append(HEADER25);
			return stringBuilder.toString();
		}
}
