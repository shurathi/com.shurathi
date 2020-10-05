/*	
 **************************************************************************                                                                
 *                            	     Confidentiality Information:
 *
 * This module is the confidential and proprietary information of
 * RIA Advisory; it is not to be copied, reproduced, or
 * transmitted in any form, by any means, in whole or in part,
 * nor is it to be used for any purpose other than that for which
 * it is expressly provided without the written permission of
 * RIA Advisory.
 *                                    
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * This is a custom Rate Component - Criteria field algorithm which wil be used
 * to determine whether we need to apply Tax or not based on the Price Item.
 * ************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 *           
 * 2020-08-13   SSapre		Initial Version
 **************************************************************************
 */

package com.splwg.cm.domain.rate.rateComponent.algorithm.criteriaField;

import java.util.List;

import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.LookupHelper;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.characteristicType.CharacteristicEntity;
import com.splwg.base.domain.common.characteristicType.CharacteristicTypeEntities;
import com.splwg.ccb.api.lookup.CriteriaResultLookup;
import com.splwg.ccb.domain.billing.billSegment.BillSegmentCalculationHeaderData;
import com.splwg.ccb.domain.billing.billSegment.BillSegmentItemData;
import com.splwg.ccb.domain.billing.billSegment.BillSegmentReadData;
import com.splwg.ccb.domain.billing.billSegment.BillSegmentServiceQuantityData;
import com.splwg.ccb.domain.common.characteristic.CharacteristicData;
import com.splwg.ccb.domain.pricing.priceitem.PriceItem_Id;
import com.splwg.ccb.domain.rate.ApplyRateData;
import com.splwg.ccb.domain.rate.rateComponent.RateComponentCriteriaFieldAlgorithmSpot;
import com.splwg.ccb.domain.rate.rateComponent.RateComponentEligibilityCriteria;
import com.splwg.ccb.domain.rate.rateComponent.RateComponent_DTO;
import com.splwg.ccb.domain.rate.rateVersion.ApplyRateVersionData;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;

/**
 * @author ShreyasSapre
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = characteristicType, name = taxCategoryCharacteristicType, required = true, type = entity)})
 */
public class CmGetTaxCategory_Impl extends CmGetTaxCategory_Gen implements
		RateComponentCriteriaFieldAlgorithmSpot {
	
	private ApplyRateData applyRateData;
	private RateComponent_DTO rateComponentDto;
	private CriteriaResultLookup singlCriteriaResultLookup;
	private Bool isSingleCriteriaFieldValue = Bool.FALSE;
	private Bool isCriteriaFieldFound = Bool.FALSE;
	
	private static final String BLANK = "";
	
	@Override
	protected void extraSoftParameterValidations(boolean forAlgorithmValidation) 
	{
		CharacteristicTypeEntities taxCategoryCharacteristicTypeEntities = getTaxCategoryCharacteristicType().getEntities();
		
		boolean taxCategoryCharacteristicTypePriceItemEntityFlag = false;
		for(CharacteristicEntity taxCategoryCharacteristicTypeEntity: taxCategoryCharacteristicTypeEntities)
		{
			if(taxCategoryCharacteristicTypeEntity.fetchIdCharacteristicEntity().equals
			(com.splwg.ccb.api.lookup.CharacteristicEntityLookup.constants.PRICE_ITEM))
				taxCategoryCharacteristicTypePriceItemEntityFlag = true;
		}
		
		if(!taxCategoryCharacteristicTypePriceItemEntityFlag)
			addError(CmMessageRepository.pitmCharEntityNotFound(getTaxCategoryCharacteristicType().getId().getTrimmedValue()));
		
		super.extraSoftParameterValidations(forAlgorithmValidation);
	}
	
	@Override
	public void invoke() 
	{
		String taxCategory = getTaxCategory(new PriceItem_Id(applyRateData.getPriceItemCd()));
		
		if(!isBlankOrNull(taxCategory))
		{
			singlCriteriaResultLookup = LookupHelper.getLookupInstance(CriteriaResultLookup.class, taxCategory);
			isCriteriaFieldFound = Bool.TRUE;
			isSingleCriteriaFieldValue = Bool.TRUE;
		}
	}
	
	/**
	 * This method will be used to find Tax Category from Price Item Char
	 * @param priceItem_Id
	 * @return taxCategory
	 */
	private String getTaxCategory(PriceItem_Id priceItemCd) 
	{
		String taxCategory = BLANK;
		PreparedStatement getTaxCategoryPrepStmt = null;
		StringBuilder getTaxCategoryQuery = new StringBuilder()
		.append("SELECT PI_CHAR.CHAR_VAL FROM CI_PRICEITEM_CHAR PI_CHAR ")
		.append("WHERE PI_CHAR.PRICEITEM_CD=:priceItemCd ")
		.append("AND PI_CHAR.CHAR_TYPE_CD=:taxCategoryCharacteristicType ")
		.append("ORDER BY PI_CHAR.EFFDT DESC");
		
		try
		{
			getTaxCategoryPrepStmt = createPreparedStatement(getTaxCategoryQuery.toString(), "find Tax Category from Rate Component and Price Item Char.");
			getTaxCategoryPrepStmt.setAutoclose(false);
			
			getTaxCategoryPrepStmt.bindId("priceItemCd", priceItemCd);
			getTaxCategoryPrepStmt.bindId("taxCategoryCharacteristicType", getTaxCategoryCharacteristicType().getId());
			
			SQLResultRow resultRow = getTaxCategoryPrepStmt.firstRow();
			if(notNull(resultRow))
				taxCategory = resultRow.getString("CHAR_VAL");
		}
		finally
		{
			if(notNull(getTaxCategoryPrepStmt))
			{
				getTaxCategoryPrepStmt.close();
				getTaxCategoryPrepStmt = null;
			}
		}
		return taxCategory;
	}

	public RateComponent_DTO getRateComponentDto() {
		return this.rateComponentDto;
	}

	@Override
	public ApplyRateData getApplyRateData() {
		return this.applyRateData;
	}
	
	@Override
	public ApplyRateVersionData getApplyRateVersionData() {
		return null;
	}

	@Override
	public List<BillSegmentCalculationHeaderData> getBillSegmentCalculationHeaderAndLinesData() {
		return null;
	}

	@Override
	public List<BillSegmentItemData> getBillSegmentItemData() {
		return null;
	}

	@Override
	public List<BillSegmentReadData> getBillSegmentReadData() {
		return null;
	}

	@Override
	public List<BillSegmentServiceQuantityData> getBillSegmentServiceQuantityData() {
		return null;
	}

	@Override
	public List<CharacteristicData> getCharacteristicData() {
		return null;
	}

	@Override
	public String getCriteriaFieldValue1() {
		return null;
	}

	@Override
	public String getCriteriaFieldValue2() {
		return null;
	}

	@Override
	public String getCriteriaFieldValue3() {
		return null;
	}

	@Override
	public String getCriteriaFieldValue4() {
		return null;
	}

	@Override
	public String getCriteriaFieldValue5() {
		return null;
	}

	@Override
	public Bool getIsCriteriaFieldFound() {
		return this.isCriteriaFieldFound;
	}

	@Override
	public Bool getIsSingleCriteriaFieldValue() {
		return this.isSingleCriteriaFieldValue;
	}

	@Override
	public CriteriaResultLookup getSingleCriteriaFieldValue() {
		return this.singlCriteriaResultLookup;
	}

	@Override
	public void setApplyRateData(ApplyRateData applyRateData) {
		this.applyRateData = applyRateData;
	}

	@Override
	public void setApplyRateVersionData(ApplyRateVersionData arg0) {
	}

	@Override
	public void setBillSegmentCalculationHeaderAndLinesData(List<BillSegmentCalculationHeaderData> arg0) {
	}

	@Override
	public void setBillSegmentItemData(List<BillSegmentItemData> arg0) {
	}

	@Override
	public void setBillSegmentReadData(List<BillSegmentReadData> arg0) {
	}

	@Override
	public void setBillSegmentServiceQuantityData(List<BillSegmentServiceQuantityData> arg0) {
	}

	@Override
	public void setCharacteristicData(List<CharacteristicData> arg0) {
	}

	@Override
	public void setRateComponentEligibilityCriteria(RateComponentEligibilityCriteria arg0) {
	}

	@Override
	public void setRateComponent_DTO(RateComponent_DTO rateComponentDto) {
		this.rateComponentDto = rateComponentDto;
	}

}
