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
 * This algorithm will check if the financial transaction is being frozen 
 * where accounting period is closed. If closed, the financial transaction 
 * cannot be frozen and an error will be returned.
 * 
 **************************************************************************
 * 
 * CHANGE HISTORY:
 *
 * Date:        by:        Reason:
 * YYYY-MM-DD   IN         Reason text. 
 * 2020-09-07   JFerna     CB-380. Initial	
 **************************************************************************
 */

package com.splwg.cm.domain.accounting.algorithm;

import java.math.BigInteger;

import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.ccb.api.lookup.FinancialTransactionTypeLookup;
import com.splwg.ccb.domain.admin.accountingCalendar.AccountingCalendar_Id;
import com.splwg.ccb.domain.admin.customerClass.CustomerClassFtFreezeAlgorithmSpot;
import com.splwg.ccb.domain.financial.financialTransaction.FinancialTransaction;
import com.splwg.cm.api.lookup.CmCalendarStatusFlagLookupLookup;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;
import com.splwg.cm.domain.common.messageRepository.CmMessages;

/**
 * @author JFerna
 *
@AlgorithmComponent ()
 */
public class CmCheckAccountingCalendarStatusAlgComp_Impl extends
		CmCheckAccountingCalendarStatusAlgComp_Gen implements
		CustomerClassFtFreezeAlgorithmSpot {
	
	//Hard Parameters
	private FinancialTransaction financialTransaction;
	
	/**
	 * Main Processing
	 */
	public void invoke() {
		
		//Get Calendar Id
		AccountingCalendar_Id calendarId = financialTransaction.getGlDivision().getCalendar().getId();
		
		//Retrieve Calendar Status Flag
		SQLResultRow calendarStatusFlagResRow = retrieveCalendarStatusFlag(calendarId);
		if (notNull(calendarStatusFlagResRow)){
			String accountingPeriod = calendarStatusFlagResRow.getString("ACCOUNTING_PERIOD").trim();
			String calendarStatusFlag = calendarStatusFlagResRow.getString("CM_CAL_STATUS_FLG").trim();
			
			//If Calendar Status is Closed, raise an error.
			if (calendarStatusFlag.equalsIgnoreCase(CmCalendarStatusFlagLookupLookup.constants.CM_CLOSED.getLookupValue().fetchIdFieldValue())){
				addError(CmMessageRepository.getServerMessage(CmMessages.CANNOT_FT_FREEZE,
						accountingPeriod,
						calendarId.getIdValue(),
						financialTransaction.getId().getIdValue()));
			}			
		}

	}
	
	/**
	 * This method retrieves the calendar status flag.
	 * @param calendarId
	 * @return resultRow
	 */
	private SQLResultRow retrieveCalendarStatusFlag(AccountingCalendar_Id calendarId){
		
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("SELECT ");
		stringBuilder.append("	CPE.ACCOUNTING_PERIOD, ");
		stringBuilder.append("	CPE.CM_CAL_STATUS_FLG ");
		stringBuilder.append("FROM ");
		stringBuilder.append("	CM_CAL_PERIOD_EXT CPE, ");
		stringBuilder.append("	CI_CAL_PERIOD CP ");
		stringBuilder.append("WHERE ");
		stringBuilder.append("	CPE.CALENDAR_ID = :calendarId ");
		stringBuilder.append("	AND CPE.FISCAL_YEAR = :fiscalYear ");
		stringBuilder.append("	AND CPE.CALENDAR_ID = CP.CALENDAR_ID ");
		stringBuilder.append("	AND CPE.FISCAL_YEAR = CP.FISCAL_YEAR ");
		stringBuilder.append("	AND CPE.ACCOUNTING_PERIOD = CP.ACCOUNTING_PERIOD ");
		stringBuilder.append("	AND CP.BEGIN_DT <= :accountingDate ");
		stringBuilder.append("	AND CP.END_DT >= :accountingDate ");
		
		PreparedStatement retrieveClosedCalendar = createPreparedStatement(stringBuilder.toString(), "");
		retrieveClosedCalendar.bindId("calendarId", calendarId);
		retrieveClosedCalendar.bindBigInteger("fiscalYear", BigInteger.valueOf(financialTransaction.getAccountingDate().getYear()));
		retrieveClosedCalendar.bindDate("accountingDate", financialTransaction.getAccountingDate());
		
		SQLResultRow resultRow = retrieveClosedCalendar.firstRow();
		retrieveClosedCalendar.close();
		
		return resultRow;
	}

	@Override
	public Bool getFinancialTransactionProcessAdded() {
		return null;
	}

	@Override
	public void setFinancialTransaction(FinancialTransaction financialTransaction) {
		this.financialTransaction = financialTransaction;
	}

	@Override
	public void setFinancialTransactionType(FinancialTransactionTypeLookup arg0) {

	}

	@Override
	public void setRegularFinancialTransaction(FinancialTransaction arg0) {

	}

}
