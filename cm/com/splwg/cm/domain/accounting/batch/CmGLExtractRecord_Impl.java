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
 *Business Component GLExtract Record File
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
public class CmGLExtractRecord_Impl extends GenericBusinessComponent implements
		CmGLExtractRecord {

	//Constants
		private static final String EMPTY_STRING = "";
		private static final String DELIMITER = ",";
		private static final int LEDGER_ID_LENGTH = 15;
		private static final int STATUS_LENGTH = 15;
		private static final int SET_OF_BOOKS_ID_LENGTH = 15;
		private static final int USER_JE_SOURCE_NAME_LENGTH = 50;
		private static final int USER_JE_CATEGORY_NAME_LENGTH = 50;
		private static final int ACCOUNTING_DATE_LENGTH = 20;
		private static final int CURRENCY_CODE_LENGTH = 3;
		private static final int DATE_CREATED_LENGTH  = 20;
		private static final int CREATED_BY_LENGTH = 8;
		private static final int ACTUAL_FLAG_LENGTH = 1;
		private static final int SEGMENT1_LENGTH = 4;
		private static final int SEGMENT2_LENGTH = 6;
		private static final int SEGMENT3_LENGTH = 6;
		private static final int SEGMENT4_LENGTH = 5;
		private static final int SEGMENT5_LENGTH = 3;
		private static final int SEGMENT6_LENGTH= 4;
		private static final int SEGMENT7_LENGTH= 2;
		private static final int SEGMENT8_LENGTH= 4;
		private static final int ENTERED_DEBIT_LENGTH=15;
		private static final int ENTERED_CREDIT_LENGTH=15;
		private static final int ACCOUNTED_DEBIT_LENGTH=15;
		private static final int ACCOUNTED_CREDIT_LENGTH=15;
		private static final int PERIOD_NAME_LENGTH=20;
		private static final int DECRIPTION_LENGTH=256;
	
		//Work Variables
	    private String ledgerId = EMPTY_STRING;
	    private String status = EMPTY_STRING;
	    private String setOfBookId = EMPTY_STRING;
		private String userJESourceName = EMPTY_STRING;
		private String userJECategoryName = EMPTY_STRING;
		private String accountingDate = EMPTY_STRING;
		private String currencyCode = EMPTY_STRING;
		private String dateCreated = EMPTY_STRING;
		private String createdBy = EMPTY_STRING;
		private String actualFlag = EMPTY_STRING;
		private String segment1 = EMPTY_STRING;
		private String segment2 = EMPTY_STRING;
		private String segment3 = EMPTY_STRING;
		private String segment4 = EMPTY_STRING;
		private String segment5 = EMPTY_STRING;
		private String segment6 = EMPTY_STRING;
		private String segment7 = EMPTY_STRING;
		private String segment8 = EMPTY_STRING;
		private String enteredDebit = EMPTY_STRING;
		private String enteredCredit = EMPTY_STRING;
		private String accountedDebit = EMPTY_STRING;
		private String accountedCredit = EMPTY_STRING;
		private String periodName = EMPTY_STRING;
		private String description = EMPTY_STRING;
	    
	    
	    
		public void setLedgerId(String ledgerIdVal) {
			this.ledgerId = formatString(ledgerIdVal, LEDGER_ID_LENGTH).trim();
		}

		public void setStatus(String statusVal) {
			this.status = formatString(statusVal, STATUS_LENGTH).trim();
		}

		public void setSetOfBookId(String setOfBookIdVal) {
			this.setOfBookId = formatString(setOfBookIdVal, SET_OF_BOOKS_ID_LENGTH).trim();
		}

		public void setUserJESourceName(String userJESourceNameVal) {
			this.userJESourceName = formatString(userJESourceNameVal, USER_JE_SOURCE_NAME_LENGTH).trim();
		}

		public void setUserJECategoryName(String userJECategoryNameVal) {
			this.userJECategoryName = formatString(userJECategoryNameVal, USER_JE_CATEGORY_NAME_LENGTH).trim();
		}

		public void setAccountingDate(String accountingDateVal) {
			this.accountingDate = formatString(accountingDateVal, ACCOUNTING_DATE_LENGTH).trim();
		}

		public void setCurrencyCode(String currencyCodeVal) {
			this.currencyCode = formatString(currencyCodeVal, CURRENCY_CODE_LENGTH).trim();
		}

		public void setDateCreated(String dateCreatedVal) {
			this.dateCreated = formatString(dateCreatedVal, DATE_CREATED_LENGTH).trim();
		}

		public void setCreatedBy(String createdByVal) {
			this.createdBy = formatString(createdByVal, CREATED_BY_LENGTH).trim();
		}

		public void setActualFlag(String actualFlagVal) {
			this.actualFlag = formatString(actualFlagVal, ACTUAL_FLAG_LENGTH).trim();
		}

		public void setSegment1(String segment1Val) {
			this.segment1 = formatString(segment1Val, SEGMENT1_LENGTH).trim();
		}

		public void setSegment2(String segment2Val) {
			this.segment2 = formatString(segment2Val, SEGMENT2_LENGTH).trim();
		}

		public void setSegment3(String segment3Val) {
			this.segment3 = formatString(segment3Val, SEGMENT3_LENGTH).trim();
		}

		public void setSegment4(String segment4Val) {
			this.segment4 = formatString(segment4Val, SEGMENT4_LENGTH).trim();
		}

		public void setSegment5(String segment5Val) {
			this.segment5 = formatString(segment5Val, SEGMENT5_LENGTH).trim();
		}

		public void setSegment6(String segment6Val) {
			this.segment6 = formatString(segment6Val, SEGMENT6_LENGTH).trim();
		}

		public void setSegment7(String segment7Val) {
			this.segment7 = formatString(segment7Val, SEGMENT7_LENGTH).trim();
		}

		public void setSegment8(String segment8Val) {
			this.segment8 = formatString(segment8Val, SEGMENT8_LENGTH).trim();
		}

		public void setEnteredDebit(String enteredDebitVal) {
			this.enteredDebit = formatString(enteredDebitVal, ENTERED_DEBIT_LENGTH).trim();
		}

		public void setEnteredCredit(String enteredCreditVal) {
			this.enteredCredit = formatString(enteredCreditVal, ENTERED_CREDIT_LENGTH).trim();
		}

		public void setAccountedDebit(String accountedDebitVal) {
			this.accountedDebit = formatString(accountedDebitVal, ACCOUNTED_DEBIT_LENGTH).trim();
		}

		public void setAccountedCredit(String accountedCreditVal) {
			this.accountedCredit = formatString(accountedCreditVal, ACCOUNTED_CREDIT_LENGTH).trim();
		}

		public void setPeriodName(String periodNameVal) {
			this.periodName = formatString(periodNameVal, PERIOD_NAME_LENGTH).trim();
		}

		public void setDescription(String descriptionVal) {
			this.description = formatString(descriptionVal, DECRIPTION_LENGTH).trim();
		}

		/**
		 * Method to construct the customer record
		 * @return String customer record
		 */
		public String cmCreateRecordString() 
		{
			
			StringBuilder stringBuilder = new StringBuilder();
		
			stringBuilder.append(ledgerId);
			stringBuilder.append(DELIMITER);
			stringBuilder.append(status);
			stringBuilder.append(DELIMITER);
			stringBuilder.append(setOfBookId);
			stringBuilder.append(DELIMITER);
			stringBuilder.append(userJESourceName);
			stringBuilder.append(DELIMITER);
			stringBuilder.append(userJECategoryName);
			stringBuilder.append(DELIMITER);
			stringBuilder.append(accountingDate);
			stringBuilder.append(DELIMITER);
			stringBuilder.append(currencyCode);
			stringBuilder.append(DELIMITER);
			stringBuilder.append(dateCreated);
			stringBuilder.append(DELIMITER);
			stringBuilder.append(createdBy);
			stringBuilder.append(DELIMITER);
			stringBuilder.append(actualFlag);
			stringBuilder.append(DELIMITER);
			stringBuilder.append(segment1);
			stringBuilder.append(DELIMITER);
			stringBuilder.append(segment2);
			stringBuilder.append(DELIMITER);
			stringBuilder.append(segment3);
			stringBuilder.append(DELIMITER);
			stringBuilder.append(segment4);
			stringBuilder.append(DELIMITER);
			stringBuilder.append(segment5);
			stringBuilder.append(DELIMITER);
			stringBuilder.append(segment6);
			stringBuilder.append(DELIMITER);
			stringBuilder.append(segment7);
			stringBuilder.append(DELIMITER);
			stringBuilder.append(segment8);
			stringBuilder.append(DELIMITER);
			stringBuilder.append(enteredDebit);
			stringBuilder.append(DELIMITER);
			stringBuilder.append(enteredCredit);
			stringBuilder.append(DELIMITER);
			stringBuilder.append(accountedDebit);
			stringBuilder.append(DELIMITER);
			stringBuilder.append(accountedCredit);
			stringBuilder.append(DELIMITER);
			stringBuilder.append(periodName);
			stringBuilder.append(DELIMITER);
			stringBuilder.append(description);
			
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
