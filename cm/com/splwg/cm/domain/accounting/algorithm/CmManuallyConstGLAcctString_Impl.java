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
 * GL Assignment for Miscellaneous Receipts
 * 
 * This algorithm type will be responsible for manually constructing the GL Account String. 
 * The segments will be taken from the characteristic of the Adjustment.
 *
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:                  Reason:
 * 2020-09-1  SAnart         CB-264.Initial Version.
 */


package com.splwg.cm.domain.accounting.algorithm;

import java.math.BigInteger;

import com.splwg.base.api.ListFilter;
import com.splwg.base.api.Query;
import com.splwg.base.api.QueryResultRow;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.characteristicType.CharacteristicType_Id;
import com.splwg.ccb.domain.adjustment.adjustment.Adjustment_Id;
import com.splwg.ccb.domain.admin.generalLedgerDistributionCode.GeneralLedgerDistributionCode;
import com.splwg.ccb.domain.admin.generalLedgerDistributionCode.GeneralLedgerDistributionCodeGlAccountConstructionAlgorithmSpot;
import com.splwg.ccb.domain.admin.generalLedgerDistributionCode.GeneralLedgerDistributionEff;
import com.splwg.ccb.domain.billing.trialBilling.TrialFinancialTransaction;
import com.splwg.ccb.domain.customerinfo.account.Account;
import com.splwg.ccb.domain.financial.financialTransaction.FinancialTransaction;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;

/**
 * @author SAnarthe
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (name = glBusinessUnitSegmentCharacteristicType, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = glAccountSegmentCharacteristicType, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = glCostCentreSegmentCharType, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = glProductSegmentCharacteristicType, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = glCustomerSegmentCharacteristicType, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = glProjectSegmentCharacteristicType, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = glFutureUseSegmentCharacteristicType, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = glIntercompanySegmentCharacteristicType, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = segmentDelimiter, type = string)})
 */

public class CmManuallyConstGLAcctString_Impl extends
		CmManuallyConstGLAcctString_Gen implements
		GeneralLedgerDistributionCodeGlAccountConstructionAlgorithmSpot {
	
	
	private GeneralLedgerDistributionCode distCd;
	private FinancialTransaction ft;
	String glAccount;
	
	

	@Override
	public void invoke() {
		
		ListFilter<GeneralLedgerDistributionEff> distCdEffIter = distCd.getGeneralLedgerDistributionEffs().createFilter("where this.effectiveStatus = 'A' order by this.id.effectiveDate desc");
		GeneralLedgerDistributionEff distCdEff = distCdEffIter.firstRow();
		glAccount = distCdEff.getGlAccount();
		
		 String[] segmentArray;
		 
		 if(isBlankOrNull(getSegmentDelimiter()))
		 {
			 segmentArray = glAccount.split("\\.");
			 
		 }else
		 {
			 segmentArray = glAccount.split(getSegmentDelimiter()); 
		 }

		 if(ft.getFinancialTransactionType().getLookupValue().getId().getFieldValue().trim().equals("AD") || ft.getFinancialTransactionType().getLookupValue().getId().getFieldValue().trim().equals("AX"))
		{
			 String siblingId = ft.getSiblingId();
			 Adjustment_Id adjId=new Adjustment_Id(siblingId);
			 glAccount=adjustmentGLString(adjId,segmentArray); 
		}
		 else
		 {
			 addError(CmMessageRepository.expectedFtTypeNotFound(ft.getFinancialTransactionType().getLookupValue().getId().getFieldValue().trim(), ft.getId().getIdValue()));
		 }
	}
	
	/**
	 * This Method will create GLAcct String
	 * @return GLAcct String
	 */
	
	private String adjustmentGLString( Adjustment_Id adjId,String[] segmentArray) {
		
		
		//Segment 1
		 segmentArray[0]=retrieveAdjustmentCharacteristic( new CharacteristicType_Id(getGlBusinessUnitSegmentCharacteristicType()), adjId ,1);
		
		//Segment 2 
		 segmentArray[1]=retrieveAdjustmentCharacteristic( new CharacteristicType_Id(getGlAccountSegmentCharacteristicType()), adjId ,2);
		 
		//Segment 3 
		 segmentArray[2]=retrieveAdjustmentCharacteristic( new CharacteristicType_Id(getGlCostCentreSegmentCharType()), adjId ,3);
		
		//Segment 4 
		 segmentArray[3]=retrieveAdjustmentCharacteristic( new CharacteristicType_Id(getGlProductSegmentCharacteristicType()), adjId ,4);
		
		//Segment 5  
		 segmentArray[4]=retrieveAdjustmentCharacteristic( new CharacteristicType_Id(getGlCustomerSegmentCharacteristicType()), adjId ,5);
		
		//Segment 6
		 segmentArray[5]=retrieveAdjustmentCharacteristic( new CharacteristicType_Id(getGlProjectSegmentCharacteristicType()), adjId ,6);
		
		//Segment 7
		 segmentArray[6]=retrieveAdjustmentCharacteristic( new CharacteristicType_Id(getGlFutureUseSegmentCharacteristicType()), adjId ,7);
		
		//Segment 8
		 segmentArray[7]=retrieveAdjustmentCharacteristic( new CharacteristicType_Id(getGlIntercompanySegmentCharacteristicType()), adjId ,8);
		
		StringBuilder segmentArray1 = new StringBuilder();
		 if(isBlankOrNull(getSegmentDelimiter()))
		 {
			 for (String n :segmentArray) {
				 segmentArray1.append(n).append(".");
			 }
			 
		 }else
		 {
			 for (String n :segmentArray) {
				 segmentArray1.append(n).append(getSegmentDelimiter());
		     }
		 }
		 segmentArray1.deleteCharAt(segmentArray1.length() - 1);
		 
		 return segmentArray1.toString();
	}
	
	/**
	 * This Method will Fetch the Characteristics Values
	 * @return sqlResultRow
	 */
	
	private String retrieveAdjustmentCharacteristic( CharacteristicType_Id charType, Adjustment_Id adjId , int sgementNumber) {
		
		PreparedStatement preparedStatement=null;
		preparedStatement = createPreparedStatement("SELECT A.SRCH_CHAR_VAL FROM CI_ADJ_CHAR A WHERE "
        		+ " A.ADJ_ID =:adjId" + 
                  " AND A.CHAR_TYPE_CD=:charTypeCd " +
                  " AND A.SEQ_NUM=(SELECT MAX(SEQ_NUM) FROM CI_ADJ_CHAR " +
                  " WHERE ADJ_ID=:adjId " +
                  " AND CHAR_TYPE_CD=:charTypeCd )" ,"");
		preparedStatement.bindId("adjId",adjId);
		preparedStatement.bindId("charTypeCd",(charType));
		preparedStatement.setAutoclose(false);
		SQLResultRow row = preparedStatement.firstRow();
		
		String charVal = null;
		
		 if (notNull(row)) {
			 
			 charVal = row.getString("SRCH_CHAR_VAL");
		 }
		 else
		 {
			 addError(CmMessageRepository.noSegmentCharacteristicFound(Integer.toString(sgementNumber),ft.getId().getIdValue())); 
		 }
		 
		 return charVal;
		
	}
	
	@Override
	public String getGlAccount() {
		
		return glAccount;
	}

	@Override
	public void setFinancialTransaction(FinancialTransaction arg0) {
		this.ft = arg0;
	}

	@Override
	public void setGlDistribution(GeneralLedgerDistributionCode arg0) {
		
		this.distCd = arg0;
		
	}

	@Override
	public void setGlSequenceNumber(BigInteger arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTrialFinancialTransaction(TrialFinancialTransaction arg0) {
		// TODO Auto-generated method stub

	}

}
