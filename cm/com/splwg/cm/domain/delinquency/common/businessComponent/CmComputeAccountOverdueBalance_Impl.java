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
 * This Business Component will compute the Overdue Balance of an account.
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:                  Reason:
 * 2020-08-20   KGhuge        CB-276. Initial Version.
 * 2020-09-11	SPatil		  CB-405. Updated Version
 *
*/
package com.splwg.cm.domain.delinquency.common.businessComponent;

import java.math.BigInteger;

import com.splwg.base.api.GenericBusinessComponent;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.featureConfiguration.FeatureConfiguration_Id;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;

/**
 * @author KGhuge
 *
@BusinessComponent ()
 */
public class CmComputeAccountOverdueBalance_Impl extends
		GenericBusinessComponent implements CmComputeAccountOverdueBalance {
	//Inputs
	private String accountId = null;
	private Number daysPastDue = null;
	private boolean includeUnapplied;
	private String contractTypesFeatureConfiguration = null;
	private String contractTypesOptionType = null;
	//Output
	private Money overdueBalance;
	
	public void setAccountId(String accountId){
		this.accountId = accountId;
	}
	public void setDaysPastDue(Number daysPastDue){
		this.daysPastDue = daysPastDue;
	}
	public void setIncludeUnapplied(boolean includeUnapplied){
		this.includeUnapplied = includeUnapplied;
	}
	public void setContractTypesFeatureConfiguration(String contractTypesFeatureConfiguration){
		this.contractTypesFeatureConfiguration = contractTypesFeatureConfiguration;
	}
	public void setContractTypesOptionType(String contractTypesOptionType){
		this.contractTypesOptionType = contractTypesOptionType;
	}
	public Money getOverdueBalance(){
		processing();
		return this.overdueBalance;
	}
	
	public void processing(){
		String excessCreditContractType = null;
		StringBuilder featureConfigQuery = new StringBuilder();
		PreparedStatement featureConfigStatement = null;
		if(isBlankOrNull(this.accountId)){
			addError(CmMessageRepository.missingField("accountId"));
		}
		if(isNull(this.includeUnapplied)){
			addError(CmMessageRepository.missingField("includeUnapplied"));
		}
		if(isBlankOrNull(this.contractTypesOptionType)){
			addError(CmMessageRepository.missingField("contractTypesOptionType"));
		}
		if(isBlankOrNull(this.contractTypesFeatureConfiguration)){
			addError(CmMessageRepository.missingField("contractTypesFeatureConfiguration"));
		}else{
			FeatureConfiguration_Id featureConfigurationId = new FeatureConfiguration_Id(this.contractTypesFeatureConfiguration.trim());
			if(isNull(featureConfigurationId.getEntity())){
				addError(CmMessageRepository.invalidValueFromInput("Feature Configuration",this.contractTypesFeatureConfiguration));
			}else{
				featureConfigQuery.append(" SELECT WFM_OPT_VAL FROM CI_WFM_OPT WHERE WFM_NAME=:configurationCd ");
				featureConfigQuery.append(" AND EXT_OPT_TYPE=:optionType ");
				featureConfigStatement = createPreparedStatement(featureConfigQuery.toString(), "Retrive Feature Config Option Value");
				featureConfigStatement.bindId("configurationCd", featureConfigurationId);
				featureConfigStatement.bindString("optionType", this.contractTypesOptionType, "EXT_OPT_TYPE");
				SQLResultRow firstRow = featureConfigStatement.firstRow();
				if(!isNull(firstRow)){
					excessCreditContractType = firstRow.getString("WFM_OPT_VAL").trim();
				}else{
					addError(CmMessageRepository.unableToDetermineExcessCreditContractType(this.contractTypesFeatureConfiguration, this.contractTypesOptionType));
				}
			}
			this.overdueBalance = retriveOverDueAmount(excessCreditContractType);	
		}
	}
	
	public Money retriveOverDueAmount(String excessCreditContractType){
		StringBuilder overDueAmountQuery = new StringBuilder();
		PreparedStatement overDueAmountStatement = null;
		Money amount = null;
		if(isNull(this.daysPastDue)){
			overDueAmountQuery.append(" SELECT NVL(SUM(FT.CUR_AMT),0) AS ACCOUNT_OVERDUE_BALANCE ");
			overDueAmountQuery.append(" FROM CI_FT FT, CI_SA SA WHERE FT.SA_ID = SA.SA_ID AND SA.ACCT_ID = :acctId ");
			overDueAmountQuery.append(" AND ((FT.MATCH_EVT_ID = ' ' AND EXISTS (SELECT 'X' FROM CI_BILL BL WHERE FT.BILL_ID = BL.BILL_ID AND BL.DUE_DT < :processDate)) ");
			overDueAmountQuery.append(" OR EXISTS (SELECT 'X' FROM CI_MATCH_EVT ME WHERE FT.MATCH_EVT_ID = ME.MATCH_EVT_ID ");
			overDueAmountQuery.append(" AND EXISTS (SELECT 'X' FROM CI_BILL BL WHERE FT.BILL_ID = BL.BILL_ID AND BL.DUE_DT < :processDate) AND ME.MEVT_STATUS_FLG <> 'B') ");
			overDueAmountQuery.append(" OR EXISTS (SELECT 'X' FROM CI_MATCH_EVT ME, CI_FT FT2, CI_BILL BL WHERE FT.MATCH_EVT_ID = ME.MATCH_EVT_ID ");
			overDueAmountQuery.append(" AND FT2.MATCH_EVT_ID = ME.MATCH_EVT_ID AND FT.FT_TYPE_FLG = 'PS' AND FT2.BILL_ID = BL.BILL_ID ");
			overDueAmountQuery.append(" AND BL.DUE_DT < :processDate AND ME.MEVT_STATUS_FLG <> 'B')) ");
			overDueAmountQuery.append(" AND SA.SA_TYPE_CD <>:excessCreditContractType ");				
			overDueAmountStatement = createPreparedStatement(overDueAmountQuery.toString(),"Retrive Account Overdue Balance");
			overDueAmountStatement.bindId("acctId", new Account_Id(this.accountId.trim()));
			overDueAmountStatement.bindString("excessCreditContractType",excessCreditContractType ,"SA_TYPE_CD");
			//Start CB-405
			overDueAmountStatement.bindDate("processDate", getProcessDateTime().getDate());
			//End CB-405
			SQLResultRow firstRow = overDueAmountStatement.firstRow();
			amount = firstRow.getMoney("ACCOUNT_OVERDUE_BALANCE");
			overDueAmountStatement.close();
		}else{
			overDueAmountQuery.append(" SELECT NVL(SUM(FT.CUR_AMT),0) AS ACCOUNT_OVERDUE_BALANCE ");
			overDueAmountQuery.append(" FROM CI_FT FT, CI_SA SA WHERE FT.SA_ID = SA.SA_ID AND SA.ACCT_ID = :acctId ");
			overDueAmountQuery.append(" AND ((FT.MATCH_EVT_ID = ' ' AND EXISTS (SELECT 'X' FROM CI_BILL BL WHERE FT.BILL_ID = BL.BILL_ID AND BL.DUE_DT < (:processDate-:pastDueDays))) ");
			overDueAmountQuery.append(" OR EXISTS (SELECT 'X' FROM CI_MATCH_EVT ME WHERE FT.MATCH_EVT_ID = ME.MATCH_EVT_ID ");
			overDueAmountQuery.append(" AND EXISTS (SELECT 'X' FROM CI_BILL BL WHERE FT.BILL_ID = BL.BILL_ID AND BL.DUE_DT < (:processDate-:pastDueDays)) AND ME.MEVT_STATUS_FLG <> 'B') ");
			overDueAmountQuery.append(" OR EXISTS (SELECT 'X' FROM CI_MATCH_EVT ME, CI_FT FT2, CI_BILL BL WHERE FT.MATCH_EVT_ID = ME.MATCH_EVT_ID ");
			overDueAmountQuery.append(" AND FT2.MATCH_EVT_ID = ME.MATCH_EVT_ID AND FT.FT_TYPE_FLG = 'PS' AND FT2.BILL_ID = BL.BILL_ID ");
			overDueAmountQuery.append(" AND BL.DUE_DT < (:processDate-:pastDueDays) AND ME.MEVT_STATUS_FLG <> 'B')) ");
			overDueAmountQuery.append(" AND SA.SA_TYPE_CD <>:excessCreditContractType ");				
			overDueAmountStatement = createPreparedStatement(overDueAmountQuery.toString(),"Retrive Account Overdue Balance");
			overDueAmountStatement.bindId("acctId", new Account_Id(this.accountId.trim()));
			overDueAmountStatement.bindString("excessCreditContractType",excessCreditContractType ,"SA_TYPE_CD");
			overDueAmountStatement.bindBigInteger("pastDueDays",new BigInteger(this.daysPastDue.toString()));
			//Start CB-405
			overDueAmountStatement.bindDate("processDate", getProcessDateTime().getDate());
			//End CB-405
			amount = overDueAmountStatement.firstRow().getMoney("ACCOUNT_OVERDUE_BALANCE");
			overDueAmountStatement.close();
		}
		if(includeUnapplied){
			PreparedStatement excessCreditBalanceStatement = null;
			StringBuilder excessCreditBalanceQuery = new StringBuilder();
			excessCreditBalanceQuery.append(" SELECT NVL(SUM(FT.CUR_AMT),0) AS EXCESS_CREDIT_BALANCE FROM CI_FT FT, CI_SA SA ");
			excessCreditBalanceQuery.append(" WHERE SA.ACCT_ID =:acctId AND FT.SA_ID=SA.SA_ID AND SA.SA_TYPE_CD=:excessCreditContractType ");
			excessCreditBalanceStatement = createPreparedStatement(excessCreditBalanceQuery.toString(),"Retrive Excess credit Balance");
			excessCreditBalanceStatement.bindId("acctId",new Account_Id(this.accountId.trim()));
			excessCreditBalanceStatement.bindString("excessCreditContractType",excessCreditContractType ,"SA_TYPE_CD");
			Money excessCreditBalance = excessCreditBalanceStatement.firstRow().getMoney("EXCESS_CREDIT_BALANCE");
			excessCreditBalanceStatement.close();
			amount = amount.add(excessCreditBalance);
		}
		return amount;
	}
}
