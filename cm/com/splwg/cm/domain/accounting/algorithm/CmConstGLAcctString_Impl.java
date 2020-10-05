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
 * Construct GL Account String
 * 
 * This algorithm type will be responsible for constructing the GL Account String.
 *
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:                      Reason:
 * 2020-7-24  IshitaGarg/SAnart         CB-224.Initial Version.
 * 2020-9-15  KGhuge      				CB-393
 * 2020-9-24  SAnart                    CB-444.GL Assignment inconsistent error message.
 *
 *
 *********************************************************************************************
 */

package com.splwg.cm.domain.accounting.algorithm;

import java.math.BigInteger;

import com.splwg.base.api.ListFilter;
import com.splwg.base.api.Query;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.characteristicType.CharacteristicEntity_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.characteristicType.CharacteristicType_Id;
import com.splwg.base.domain.security.user.UserCharacteristic;
import com.splwg.base.domain.security.user.User_Id;
import com.splwg.ccb.domain.admin.bank.BankAccount;
import com.splwg.ccb.domain.admin.generalLedgerDistributionCode.GeneralLedgerDistributionCode;
import com.splwg.ccb.domain.admin.generalLedgerDistributionCode.GeneralLedgerDistributionCodeGlAccountConstructionAlgorithmSpot;
import com.splwg.ccb.domain.admin.generalLedgerDistributionCode.GeneralLedgerDistributionEff;
import com.splwg.ccb.domain.admin.tenderSource.TenderSource_Id;
import com.splwg.ccb.domain.billing.billSegment.BillSegmentCalculationHeader;
import com.splwg.ccb.domain.billing.billSegment.BillSegmentCalculationHeaders;
import com.splwg.ccb.domain.billing.billSegment.BillSegment_Id;
import com.splwg.ccb.domain.billing.billableCharge.BillableCharge_Id;
import com.splwg.ccb.domain.billing.trialBilling.TrialFinancialTransaction;
import com.splwg.ccb.domain.customerinfo.account.Account;
import com.splwg.ccb.domain.customerinfo.account.AccountCharacteristic;
import com.splwg.ccb.domain.financial.financialTransaction.FinancialTransaction;
import com.splwg.ccb.domain.pricing.priceitem.PriceItem;
import com.splwg.ccb.domain.pricing.priceitem.PriceItemChar;
import com.splwg.ccb.domain.pricing.priceitem.PriceItem_Id;
import com.splwg.cm.api.lookup.CharacteristicEntityLookup;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;

//Start Change CB-393
///**
// * @author IshitaGarg
// *
//@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (name = salesReprentativeCharType, required = true, type = string)
// *            , @AlgorithmSoftParameter (name = glProductSegmentCharType, required = true, type = string)
// *            , @AlgorithmSoftParameter (name = glCostCentreSegmentCharType, required = true, type = string)
// *            , @AlgorithmSoftParameter (name = glAccountSegmentCharacteristicType, required = true, type = string)
// *            , @AlgorithmSoftParameter (name = priceItemProductSegmentCharacter, required = true, type = string)
// *            , @AlgorithmSoftParameter (name = salesRepCostCenterSegmentCharacter, required = true, type = string)
// *            , @AlgorithmSoftParameter (name = tenderAccountSegmentCharacter, required = true, type = string)
// *            , @AlgorithmSoftParameter (name = refundAccountSegmentCharacter, required = true, type = string)
// *            , @AlgorithmSoftParameter (name = priceItemAccountSegmentCharacter, required = true, type = string)
// *            , @AlgorithmSoftParameter (name = businessUnitSegmentCharacter, required = true, type = string)
// *            , @AlgorithmSoftParameter (name = segmentDelimiter, type = string)})
// */
/**
 * @author IshitaGarg
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (name = segmentDelimiter, type = string)
 *            , @AlgorithmSoftParameter (name = businessUnitSegmentCharacter, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = priceItemAccountSegmentCharacter, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = refundAccountSegmentCharacter, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = tenderAccountSegmentCharacter, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = salesReprentativeCharType, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = priceItemProductSegmentCharacter, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = glAccountSegmentCharacteristicType, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = glCostCentreSegmentCharType, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = glProductSegmentCharType, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = salesRepCostCenterSegmentCharacter, required = true, type = string)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = relatedBillSegmentCharType, required = true, type = entity)})
 */
 //End Change CB-393
public class CmConstGLAcctString_Impl extends CmConstGLAcctString_Gen implements
		GeneralLedgerDistributionCodeGlAccountConstructionAlgorithmSpot {

	private GeneralLedgerDistributionCode distCd;
	private FinancialTransaction ft;
	String custClass;
	String glAccountSegment1;
	String glAccountSegment2;
	String glAccountSegment3;
	String glAccountSegment4;
	String glAccount;
	String tndrSrc;
	
	TenderSource_Id tndrsrcid ;
	
	String salesRepCharVal;
	String userCharVal;
	PriceItem_Id priceItemCdId;
	
	Logger LOGGER = LoggerFactory.getLogger(CmConstGLAcctString_Impl.class);

	

	@Override
	public void invoke() {
		
		CharacteristicType_Id glAccountSegmentCharType = new CharacteristicType_Id(getGlAccountSegmentCharacteristicType());
		CharacteristicType_Id glProductSegmentCharTypeVar = new CharacteristicType_Id(getGlProductSegmentCharType());
		CharacteristicType_Id salesRepCharType = new CharacteristicType_Id(getSalesReprentativeCharType());
		CharacteristicType_Id glCostCentreSegmentCharType = new CharacteristicType_Id(getGlCostCentreSegmentCharType());
		
		ListFilter<GeneralLedgerDistributionEff> distCdEffIter = distCd.getGeneralLedgerDistributionEffs().createFilter("where this.effectiveStatus = 'A' order by this.id.effectiveDate desc");
		 GeneralLedgerDistributionEff distCdEff = distCdEffIter.firstRow();
		 fetchCustomerClass(ft);
		 Account acct = ft.getServiceAgreement().getAccount();
		 glAccount = distCdEff.getGlAccount();
		 
		 
		 String[] segmentArray;
		 
		 if(isBlankOrNull(getSegmentDelimiter()))
		 {
			 segmentArray = glAccount.split("\\.");
			 
		 }else
		 {
			 segmentArray = glAccount.split(getSegmentDelimiter()); 
		 }
		 
		/* glAccount=glAccount.replace('.','-');
		 
		 String[] segmentArray = glAccount.split(getSegmentDelimiter());*/

		 if(segmentArray[0].equals(getBusinessUnitSegmentCharacter().trim()))
		 {
			// glAccountSegment1 = custClass;
			 int num =segmentArray[0].length();
			 String custCLass=custClass;
			 
			 segmentArray[0]=custCLass.substring(0, num);
			 
		 }
		 
		 if(segmentArray[1].equals(getPriceItemAccountSegmentCharacter().trim())){
			 //Start Change CB-393
//			 if(ft.getFinancialTransactionType().getLookupValue().getId().getFieldValue().trim().equals("BS") || ft.getFinancialTransactionType().getLookupValue().getId().getFieldValue().trim().equals("BX"))
//			 {
//				String siblingId = ft.getSiblingId();
//				BillSegment_Id bsegId = new BillSegment_Id(siblingId);
//				BillSegmentCalculationHeaders bsegCalcList = bsegId.getEntity().getCalculationHeaders();
//				BillSegmentCalculationHeader bsegCalc = bsegCalcList.iterator().next();
//				BillableCharge_Id billableChargeId = bsegCalc.getBillableChargeId();
//				priceItemCdId = billableChargeId.getEntity().getPriceItemCodeId();
//				PriceItemChar priceItemChar = priceItemCdId.getEntity().getEffectiveChar(glAccountSegmentCharType.getEntity());
				//End Change CB-393
				PriceItem priceItem = retrivePriceItem("2");
				
				PriceItemChar priceItemChar = priceItem.getEffectiveChar(glAccountSegmentCharType.getEntity());							
				if(isNull(priceItemChar))
				{
					//CB-444 ADD-START
					//addError(CmMessageRepository.noSegmentValueFound("Price Item ",priceItemCdId.getTrimmedValue()," 2"));
					addError(CmMessageRepository.glstrInCharacteristicCannotBeFound("Price Item",priceItem.getId().getIdValue(),"2",ft.getId().getIdValue()));
					//CB-444 ADD-END
				}
				else
				{
					//glAccountSegment2 = priceItemChar.getSearchCharacteristicValue();
					segmentArray[1]=priceItemChar.getSearchCharacteristicValue();
				}
//			 }
		 }
		 
		 if(segmentArray[1].equals(getTenderAccountSegmentCharacter().trim()))
		 {
			 if(ft.getFinancialTransactionType().getLookupValue().getId().getFieldValue().trim().equals("PS") || ft.getFinancialTransactionType().getLookupValue().getId().getFieldValue().trim().equals("PX"))
			 {
				 
				 LOGGER.info("Lookup "+ft.getFinancialTransactionType().getLookupValue().asLookup());
				StringBuilder getTndrSrcString = new StringBuilder();
				getTndrSrcString.append(" FROM FinancialTransaction ft,Payment pay,PaymentTender pt,TenderControl tc "+
				//"WHERE ft.financialTransactionType='PS' " +
				"WHERE ft.financialTransactionType= :ftType " +
				"AND ft.parentId =  pay.id "+
				"AND pay.paymentEvent.id = pt.paymentEvent.id "+
				"AND pt.tenderControlId = tc.id "+
				"AND ft.id = :ftId");
				
				//FinancialTransaction
				Query getTndrSrcQry = createQuery(getTndrSrcString.toString(),"");
				getTndrSrcQry.bindId("ftId", ft.getId());
				getTndrSrcQry.bindLookup("ftType", ft.getFinancialTransactionType());
				getTndrSrcQry.addResult("tndrSrc","tc.tenderSource.id");
				
			   
				if(isNull(getTndrSrcQry.firstRow()))
				{
					//CB-444 ADD-START
					//addError(CmMessageRepository.noValueFound("Payment ",ft.getFinancialTransactionType().getLookupValue().getEffectiveDescription()," 2"));
					addError(CmMessageRepository.cannotFindTndrSrcOfPayment("2",ft.getId().getIdValue()));
					//CB-444 ADD-END
				}
				else
				{
					 //tndrSrc = getTndrSrcQry.firstRow();
					 tndrsrcid = (TenderSource_Id) getTndrSrcQry.firstRow();
					 
				}
				// TenderSource_Id tndrSrcId = new TenderSource_Id(tndrSrc);
				 BankAccount bankAcct = tndrsrcid.getEntity().getBankAccount();
				 String charVal="";
				 
				 PreparedStatement preparedStatement=null;
			
			            preparedStatement = createPreparedStatement("Select A.SRCH_CHAR_VAL from ci_bank_account_char a"
			            		+ " where a.bank_cd=:bankCd AND A.CHAR_TYPE_CD=:charType" + 
                                     " and a.effdt=(select max(b.effdt) from ci_bank_account_char b" +
                                     " where a.bank_cd=b.bank_cd AND A.CHAR_TYPE_CD=B.CHAR_TYPE_CD)","");
			            preparedStatement.bindId("bankCd", bankAcct.getId().getBankId());
			            preparedStatement.bindId("charType", glAccountSegmentCharType);
			            preparedStatement.setAutoclose(false);
			            SQLResultRow row = preparedStatement.firstRow();
			            if (notNull(row)) {
			                charVal = row.getString("SRCH_CHAR_VAL");
			            }
			            if (preparedStatement != null) {
			                preparedStatement.close();
			            }
	 
				 
				 if(isBlankOrNull(charVal))
				 {
					//CB-444 ADD-START
					 //addError(CmMessageRepository.noSegmentValueFound("Bank Account",bankAcct.getId().getBankId().getIdValue()," 2"));
					 addError(CmMessageRepository.glstrInCharacteristicCannotBeFound("Bank Account",bankAcct.getId().getBankId().getIdValue(),"2",ft.getId().getIdValue()));
					//CB-444 ADD-END 
				 }
				 else
				 {	 					 
					 //glAccountSegment2 = bankAcct.getEffectiveCharacteristic(glAccountSegmentCharType.getEntity()).getSearchCharacteristicValue();
					 segmentArray[1]= charVal;
				 }
			 }
			 //Start Add CB-393
			 else{
				 addError(CmMessageRepository.invalidFtType("Payment",ft.getFinancialTransactionType().getLookupValue().fetchLanguageDescription(), "2", ft.getId().getTrimmedValue()));
			 }
			 //End Add CB-393
			
		 }
		 
		 if(segmentArray[2].equals(getSalesRepCostCenterSegmentCharacter().trim()))
		 {
			 AccountCharacteristic acctChar = acct.getEffectiveCharacteristic(salesRepCharType.getEntity());
			 if(isNull(acctChar))
					 {
				 addError(CmMessageRepository.noCharFound("Sales Representative","Account",acct.getId().getIdValue(),"3"));
					 }
			 else
			 {
				 salesRepCharVal = acct.getEffectiveCharacteristic(salesRepCharType.getEntity()).getCharacteristicValueForeignKey1();
			 }
			 
			 User_Id userId = new User_Id(salesRepCharVal.trim());
			ListFilter<UserCharacteristic> userChar = userId.getEntity().getCharacteristics().createFilter("where this.id.characteristicType = :charType order by this.id.sequence desc");
			userChar.bindEntity("charType",glCostCentreSegmentCharType.getEntity());
			if(isNull(userChar.firstRow()))
			{
				
				//addError(CmMessageRepository.noSegmentValueFound("Sales Representative",userId.getTrimmedValue(),"3"));
				//CB-444 ADD-START
				addError(CmMessageRepository.glstrInCharacteristicCannotBeFound("Sales Rep",userId.getTrimmedValue(),"3",ft.getId().getIdValue()));
				//CB-444 ADD-END
			}
			else
			{
				//glAccountSegment3 = userChar.firstRow().getCharacteristicValue().trim();
				segmentArray[2]= userChar.firstRow().getCharacteristicValue().trim();
			}
		 }
		 if(segmentArray[3].equals(getPriceItemProductSegmentCharacter().trim()))
		 {
			 //Start Change CB-393
//			 if(ft.getFinancialTransactionType().getLookupValue().getId().getFieldValue().trim().equals("BS") || ft.getFinancialTransactionType().getLookupValue().getId().getFieldValue().trim().equals("BX"))
//			 {
//				 if(isNull(priceItemCdId))
//				 {
//					 addError(CmMessageRepository.noValueFound("Bill Segment", ft.getFinancialTransactionType().getLookupValue().getEffectiveDescription(),"4"));
//				 }
//				 PriceItemChar priceItemChar = priceItemCdId.getEntity().getEffectiveChar(glProductSegmentCharTypeVar.getEntity());
			 //End Change CB-393
			 	PriceItem priceItem = retrivePriceItem("4");
				PriceItemChar priceItemChar = priceItem.getEffectiveChar(glProductSegmentCharTypeVar.getEntity());	
				if(isNull(priceItemChar)){
					
					//CB-444 ADD-START
					//addError(CmMessageRepository.noSegmentValueFound("Price Item",priceItemCdId.getTrimmedValue(),"4"));
					addError(CmMessageRepository.glstrInCharacteristicCannotBeFound("Price Item",priceItem.getId().getIdValue(),"4",ft.getId().getIdValue()));
					//CB-444 ADD-END
					
				}else{
					//glAccountSegment4 = priceItemChar.getSearchCharacteristicValue();
					segmentArray[3]= priceItemChar.getSearchCharacteristicValue();
				}
				 
//			 }
			 
		 }
		 LOGGER.debug("glAccount"+segmentArray.toString());
		 
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
		 glAccount=segmentArray1.toString();
		 
		// glAccount = glAccountSegment1+"."+glAccountSegment2+"."+glAccountSegment3+"."+glAccountSegment4;
	}
	
	//Start Add CB-393
	
	/*
	 * This method will return PriceItem
	 * @param currrentSegment
	 * @return PriceItem
	 * */
	private PriceItem retrivePriceItem(String currentSegment){
		PriceItem priceItem = null;
		BillSegment_Id billSegmentId = null;
		 if(ft.getFinancialTransactionType().getLookupValue().getId().getFieldValue().trim().equals("BS") || ft.getFinancialTransactionType().getLookupValue().getId().getFieldValue().trim().equals("BX"))
		 {
			String siblingId = ft.getSiblingId();
			billSegmentId = new BillSegment_Id(siblingId);
			priceItem = retrivePriceItemFromBillSegment(billSegmentId);
			if(isNull(priceItem))
			{
				addError(CmMessageRepository.priceItemIsNotFoundIInBillSegment(ft.getId().getTrimmedValue(), billSegmentId.getTrimmedValue(), currentSegment));
			}
		 }else if(ft.getFinancialTransactionType().getLookupValue().getId().getFieldValue().trim().equals("AD") || 
				 ft.getFinancialTransactionType().getLookupValue().getId().getFieldValue().trim().equals("AX")){
			 validateCharacteristicType(this.getRelatedBillSegmentCharType(),CharacteristicEntityLookup.constants.ADJUSTMENT_TYPE,currentSegment);
			 String bilSegId = retriveAdjustmentCharValue();
			 if(isBlankOrNull(bilSegId)){
				 addError(CmMessageRepository.noBillSegmentFound(ft.getId().getTrimmedValue(), ft.getParentId(), currentSegment));
			 }else{
				 billSegmentId = new BillSegment_Id(bilSegId);
				 priceItem = retrivePriceItemFromBillSegment(billSegmentId);
				if(isNull(priceItem))
				{
					addError(CmMessageRepository.priceItemIsNotFoundIInBillSegment(ft.getId().getTrimmedValue(), billSegmentId.getTrimmedValue(), currentSegment));
				}
			}
		}else{
			addError(CmMessageRepository.invalidFtType("Bill Segment or Adjustment",ft.getFinancialTransactionType().getLookupValue().fetchLanguageDescription(), currentSegment, ft.getId().getTrimmedValue()));
		}
		return priceItem;
	}
	
	/*
	 * This method will returns PriceItem from BillSegment
	 * @param BillSegment_Id
	 * @return PriceItem
	 * */
	private PriceItem retrivePriceItemFromBillSegment(BillSegment_Id bsegId){
		BillSegmentCalculationHeaders bsegCalcList = bsegId.getEntity().getCalculationHeaders();
		BillSegmentCalculationHeader bsegCalc = bsegCalcList.iterator().next();
		BillableCharge_Id billableChargeId = bsegCalc.getBillableChargeId();
		if(!isNull(billableChargeId.getEntity())){
			
			//CB-444 ADD-START
			return isNull(billableChargeId.getEntity().getPriceItemCodeId())? null : billableChargeId.getEntity().getPriceItemCodeId().getEntity();			
			//CB-444 ADD-END
		
		}else{
			return null;
		}
	}
	
	/*
	 * This method will returns Adjustment Characteristic Value.
	 * @return String
	 * */
	private String retriveAdjustmentCharValue(){
		PreparedStatement retriveAdjustmentCharValueStatement = null;
		StringBuilder retriveAdjustmentCharValueQuery = new StringBuilder();
		retriveAdjustmentCharValueQuery.append(" SELECT SRCH_CHAR_VAL FROM CI_ADJ_CHAR WHERE ");
		retriveAdjustmentCharValueQuery.append(" ADJ_ID=:siblingId AND CHAR_TYPE_CD=:billSegCharType ");
		retriveAdjustmentCharValueStatement = createPreparedStatement(retriveAdjustmentCharValueQuery.toString(), "retrive_Adjustment_Char_Value");
		retriveAdjustmentCharValueStatement.bindString("siblingId",ft.getSiblingId().trim(),"ADJ_ID");
		retriveAdjustmentCharValueStatement.bindId("billSegCharType",this.getRelatedBillSegmentCharType().getId());
		SQLResultRow result = retriveAdjustmentCharValueStatement.firstRow();
		return isNull(result) ? null : result.getString("SRCH_CHAR_VAL");
	}
	
	/**
	 * This method checks if the Characteristic Type is valid for an Entity.
	 * @param Characteristic Type to Validate
	 * @param Entity to be checked on
	 * @param Description of the Soft Parameter
	 */
	private void validateCharacteristicType(CharacteristicType charType, CharacteristicEntityLookup charEntLkup,String currentSegment){
		CharacteristicEntity_Id charEntityId = new CharacteristicEntity_Id(charType, charEntLkup);
		if(isNull(charEntityId.getEntity())){
			addError(CmMessageRepository.charTypeIsNotValidForAdjType(charType.getId().getTrimmedValue(),ft.fetchParentAdjustmentType().getId().getTrimmedValue(), ft.getId().getTrimmedValue(), currentSegment));
		}
	}
	//End Add CB-393

	private void fetchCustomerClass(FinancialTransaction ft2) {
		custClass = ft2.getServiceAgreement().getAccount().getCustomerClass().getId().getIdValue();
		
	}

	@Override
	public String getGlAccount() {
		LOGGER.debug("glAccount"+glAccount);
		return glAccount;
	}

	@Override
	public void setFinancialTransaction(FinancialTransaction ft) {
		this.ft = ft;
	}

	@Override
	public void setGlDistribution(GeneralLedgerDistributionCode distCd) {
		this.distCd = distCd;

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
