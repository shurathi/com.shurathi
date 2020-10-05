package com.splwg.cm.domain.billing.algorithm;
/*
 * This Algorithm Call rate schedule to generates Adjustment calculation lines
 */

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.ccb.domain.adjustment.adjustment.Adjustment;
import com.splwg.ccb.domain.admin.adjustmentType.AdjustmentType;
import com.splwg.ccb.domain.admin.adjustmentType.AdjustmentTypeGenerateAdjustmentAlgorithmSpot;
import com.splwg.ccb.domain.admin.adjustmentType.CustomCommonData;
import com.splwg.ccb.domain.admin.serviceAgreementType.ServiceAgreementType;
import com.splwg.ccb.domain.admin.serviceQuantityIdentifier.ServiceQuantityIdentifier_Id;
import com.splwg.ccb.domain.billing.billSegment.BillSegmentCalculationHeaderData;
import com.splwg.ccb.domain.billing.billSegment.BillSegmentServiceQuantity;
import com.splwg.ccb.domain.billing.billSegment.BillSegmentServiceQuantityData;
import com.splwg.ccb.domain.billing.billSegment.BillSegmentServiceQuantity_DTO;
import com.splwg.ccb.domain.billing.billSegment.BillSegmentServiceQuantity_Id;
import com.splwg.ccb.domain.billing.billSegment.BillSegment_Id;
import com.splwg.ccb.domain.common.characteristic.CharacteristicData;
import com.splwg.ccb.domain.customerinfo.account.Account;
import com.splwg.ccb.domain.customerinfo.serviceAgreement.SADistributionOverrideData;
import com.splwg.ccb.domain.customerinfo.serviceAgreement.ServiceAgreement;
import com.splwg.ccb.domain.rate.ApplyRateData;
import com.splwg.ccb.domain.rate.RateApplicationProcessor;
import com.splwg.ccb.domain.rate.RateApplicationProcessorData;

/**
 * @author vguddeti
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = rateSchedule, name = rateSchedule, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = adjustmentIdCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = adjustmentTypeCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = serviceQuantityIdentifier, name = baseAmountSQI, required = true, type = entity)})
 */
public class CmAdjustmentGenerationAlgComp_Impl extends
		CmAdjustmentGenerationAlgComp_Gen implements
		AdjustmentTypeGenerateAdjustmentAlgorithmSpot {

	List<BillSegmentServiceQuantityData> billSegmentServiceQuantityData = null;
	
	@Override
	public void invoke() {
		initializeData();
		addBillSegmentServiceQuantityData(getBaseAmountSQI().getId(), baseAmount);
		addCharacteristicData(getAdjustmentIdCharType(), adjustment.getId().getTrimmedValue());
		addCharacteristicData(getAdjustmentTypeCharType(), adjustmentType.getId().getTrimmedValue());
		applyAdjustmentRate();
	}
	
	private void initializeData(){
		if(isNull(charData)){
			charData = new ArrayList<CharacteristicData>();
		}
		billSegmentServiceQuantityData = new ArrayList<BillSegmentServiceQuantityData>();
		
	}
	private void applyAdjustmentRate(){
		ApplyRateData applyRateData = ApplyRateData.Factory.newInstance();
		applyRateData.setServiceAgreement(serviceAgreeement);
		applyRateData.setRateSchedule(getRateSchedule());
		applyRateData.setBillSegmentPeriodStart(calculationDate);
		applyRateData.setConsumptionPeriodStart(calculationDate);
		applyRateData.setAccountingDate(accountingDate);
		applyRateData.setBillSegmentPeriodEnd(calculationDate);
		applyRateData.setConsumptionPeriodEnd(calculationDate);
		applyRateData.setLanguage(this.getActiveContextLanguage());
		applyRateData.setShouldRetainSqRules(Bool.TRUE);
		applyRateData.setShouldRetainSqCollection(Bool.TRUE);
		RateApplicationProcessorData rateAppProcData = RateApplicationProcessorData.Factory.newInstance();
		rateAppProcData.setApplyRateData(applyRateData);
		rateAppProcData.setServiceQuantityDataList(billSegmentServiceQuantityData);
		rateAppProcData.setCharacteristicDataList(charData);

		RateApplicationProcessor rateAppProcessor = RateApplicationProcessor.Factory.newInstance();
		rateAppProcessor.applyRate(rateAppProcData);

		bsegCalcData = rateAppProcData.getCalculationHeaderDataList();
	}
	
	private void addBillSegmentServiceQuantityData(ServiceQuantityIdentifier_Id sqiId,BigDecimal sqiValue){
		int serviceQuantityIndex = 0;
		serviceQuantityIndex = findMatch(sqiId);
		if (serviceQuantityIndex >= BigInteger.ZERO.intValue()) {
			billSegmentServiceQuantityData.set(serviceQuantityIndex, populateServiceQuantity(sqiId,sqiValue));
		} else {
			/**Add Service Quantity**/
			if (billSegmentServiceQuantityData.size() < com.splwg.ccb.cobol.CobolConstants.CI_CONST_BI_MAX_BSEG_SQ_COLL) {
				billSegmentServiceQuantityData.add(populateServiceQuantity(sqiId,sqiValue));
			} else {
				//addError(MessageRepository.serviceQuantityCollectionComputationOverflow());
			}
		}
	}

	
	private int findMatch(ServiceQuantityIdentifier_Id inputSQI) {
		int i = 0;
		for (BillSegmentServiceQuantityData billSegSqData: billSegmentServiceQuantityData) {

			BillSegmentServiceQuantity_DTO billSegmentSQDto = billSegSqData.getBillSegmentServiceQuantityDto();

			BillSegmentServiceQuantity_Id id = billSegmentSQDto.getId();

			if (id == null) continue;
			if (matchSQI(id,inputSQI))
			{
				return i;
			}
		}

		return -1;
	}

	private boolean matchSQI(BillSegmentServiceQuantity_Id id,ServiceQuantityIdentifier_Id inputSQI) {
		if (notNull(id.getServiceQuantityIdentifier()) && notNull(inputSQI)
				&& id.getServiceQuantityIdentifier().trim().equals(inputSQI.getIdValue().trim())) {
			return true;
		}
		return false;
	}
	
	private BillSegmentServiceQuantityData populateServiceQuantity(ServiceQuantityIdentifier_Id inputSQIId,BigDecimal txnVal) {
		BillSegmentServiceQuantityData billSegmentSqData = BillSegmentServiceQuantityData.Factory.newInstance();
		BillSegmentServiceQuantity_DTO billSegSqDto = (BillSegmentServiceQuantity_DTO) createDTO(BillSegmentServiceQuantity.class);

		billSegSqDto.setId(new BillSegmentServiceQuantity_Id(BillSegment_Id.NULL, null, null, inputSQIId == null ? "": inputSQIId.getIdValue().trim()));
		int decimal_pos = inputSQIId.getEntity().getDecimalPositions().intValue();

		billSegSqDto.setBillableServiceQuantity(txnVal.setScale(decimal_pos,BigDecimal.ROUND_HALF_UP));
		billSegSqDto.setInitialServiceQuantity(txnVal.setScale(decimal_pos,BigDecimal.ROUND_HALF_UP));
		billSegmentSqData.setBillSegmentServiceQuantityDto(billSegSqDto);
		return billSegmentSqData;
	}
	
	
	private void addCharacteristicData(CharacteristicType charType,String value){
		CharacteristicData data = CharacteristicData.Factory.newInstance();
		data.setCharacteristicType(charType);
		data.setCharacteristicValue(value);
		charData.add(data);
		
	}

	Adjustment adjustment;
	@Override
	public void setAdjustment(Adjustment var1) {
		adjustment = var1;
	}

	SADistributionOverrideData saDistData;
	@Override
	public void setSADistributionOverrideData(SADistributionOverrideData var1) {
		saDistData = var1;

	}

	CustomCommonData commonData;
	@Override
	public void setCustomCommonData(CustomCommonData var1) {
		commonData = var1;

	}

	AdjustmentType adjustmentType;
	@Override
	public void setAdjustmentType(AdjustmentType var1) {
		adjustmentType  = var1;

	}

	@Override
	public AdjustmentType getAdjustmentType() {
		return adjustmentType;
	}

	Account account;
	@Override
	public void setAccount(Account var1) {
		account = var1;

	}

	@Override
	public Account getAccount() {
		return account;
	}

	ServiceAgreement serviceAgreeement;
	@Override
	public void setServiceAgreement(ServiceAgreement var1) {
		serviceAgreeement = var1;

	}

	@Override
	public ServiceAgreement getServiceAgreement() {
		return serviceAgreeement;
	}
	
	ServiceAgreementType saType;

	@Override
	public void setServiceAgreementType(ServiceAgreementType var1) {
		saType = var1;
	}

	@Override
	public ServiceAgreementType getServiceAgreementType() {
		return saType;
	}

	Date  accountingDate;
	@Override
	public void setAccountingDate(Date var1) {
		accountingDate = var1;

	}

	@Override
	public Date getAccountingDate() {
		return accountingDate;
	}

	Date calculationDate;
	@Override
	public void setCalculationDate(Date var1) {
		calculationDate= var1;

	}

	@Override
	public Date getCalculationDate() {
		// TODO Auto-generated method stub
		return calculationDate;
	}

	BigDecimal baseAmount;
	@Override
	public void setBaseAmount(BigDecimal var1) {
		baseAmount = var1;

	}

	@Override
	public BigDecimal getBaseAmount() {
		return baseAmount;
	}

	List<BillSegmentCalculationHeaderData> bsegCalcData;
	@Override
	public void setBillSegmentCalculationHeaderData(
			List<BillSegmentCalculationHeaderData> var1) {
		bsegCalcData = var1;

	}

	@Override
	public List<BillSegmentCalculationHeaderData> getBillSegmentCalculationHeaderData() {
		return bsegCalcData;
	}

	List<CharacteristicData> charData;
	@Override
	public void setCharacteristicData(List<CharacteristicData> var1) {
		charData = var1;

	}

	@Override
	public List<CharacteristicData> getCharacteristicData() {
		return charData;
	}

}
