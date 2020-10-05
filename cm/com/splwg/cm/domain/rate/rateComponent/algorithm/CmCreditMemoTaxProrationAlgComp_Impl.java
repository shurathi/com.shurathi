/*
 ***********************************************************************
 *                Confidentiality Information:
 *
 * This module is the confidential and proprietary information of
 * RIA Advisory; it is not to be copied, reproduced, or
 * transmitted in any form, by any means, in whole or in part,
 * nor is it to be used for any purpose other than that for which
 * it is expressly provided without the written permission of
 * RIA Advisory.
 ***********************************************************************
 *
 * PROGRAM DESCRIPTION:
 *
 * This Algorithm calculates Tax Percentage on total Bill segment Amount*
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:         Reason:
 * 2020-08-25   V        Initial Version. 
 * 2020-08-28   V		 Tax percentage Decimal positions fixed
***********************************************************************
 */
package com.splwg.cm.domain.rate.rateComponent.algorithm;

import java.util.List;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.ccb.domain.adjustment.adjustment.Adjustment;
import com.splwg.ccb.domain.adjustment.adjustment.AdjustmentCharacteristic;
import com.splwg.ccb.domain.adjustment.adjustment.Adjustment_Id;
import com.splwg.ccb.domain.billing.billSegment.BillSegmentCalculationHeaderData;
import com.splwg.ccb.domain.billing.billSegment.BillSegmentItemData;
import com.splwg.ccb.domain.billing.billSegment.BillSegmentServiceQuantityData;
import com.splwg.ccb.domain.billing.billSegment.BillSegment_Id;
import com.splwg.ccb.domain.common.characteristic.CharacteristicData;
import com.splwg.ccb.domain.rate.ApplyRateData;
import com.splwg.ccb.domain.rate.rateComponent.RateComponent;
import com.splwg.ccb.domain.rate.rateComponent.RateComponentValueAlgorithmSpot;
import com.splwg.ccb.domain.rate.rateVersion.ApplyRateVersionData;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;

/**
 * @author venky
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = characteristicType, name = adjustmentIdCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = bsegIdCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = generalLedgerDistributionCode, name = taxDistCode, required = true, type = entity)})
 */
public class CmCreditMemoTaxProrationAlgComp_Impl extends
		CmCreditMemoTaxProrationAlgComp_Gen implements
		RateComponentValueAlgorithmSpot {

	private static final Logger logger = LoggerFactory.getLogger(CmCreditMemoTaxProrationAlgComp_Impl.class);
	//Hard Parameters
	private ApplyRateData applyRateData = null;
	private ApplyRateVersionData applyRateVersionData = null;
	private List<BillSegmentCalculationHeaderData> listBillSegmentCalculationHeaderData = null;
	private List<BillSegmentItemData> listBillSegmentItemData = null;
	private List<BillSegmentServiceQuantityData> listBillSegmentServiceQuantityData = null;
	private List<CharacteristicData> listCharacteristicData = null;
	private BigDecimal value = BigDecimal.ZERO;
	
	private static final StringBuilder BSEG_CALC_DTL = new StringBuilder()
	.append(" SELECT A.CALC_AMT BSEG_AMT , B.CALC_AMT TAX_AMT FROM CI_BSEG_CALC A , CI_BSEG_CALC_LN B ")
	.append(" WHERE A.BSEG_ID=:bsegId AND A.BSEG_ID = B.BSEG_ID ")
	.append(" AND B.DST_ID=:tax " );
	
	@Override
	public void invoke() {
		logger.debug("### Tax  Percentage on Bill segment calculation ##");
		Adjustment_Id adjId = fetchAdjId();
		if(notNull(adjId)){
			BillSegment_Id bsegId = fetchBsegId(adjId);
			if(notNull(bsegId)){
				logger.debug("### Bill Segment ID ##"+bsegId);
				prorateTax(bsegId);
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	private Adjustment_Id fetchAdjId(){
		Adjustment_Id adjId = null;
		if(notNull(listCharacteristicData)){
			for(CharacteristicData charData :listCharacteristicData ){
				if(charData.getCharacteristicType().equals(getAdjustmentIdCharType())){
					adjId = new Adjustment_Id(charData.getCharacteristicValue());
					break;
				}
			}
		}
		return adjId;
	}
	
	/**
	 * 
	 * @param adjId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private BillSegment_Id fetchBsegId(Adjustment_Id adjId){
		BillSegment_Id bsegId = null;
		if(notNull(adjId)){
			Adjustment adj = adjId.getEntity();
			if(notNull(adj)){
				List<AdjustmentCharacteristic> bsegs = adj.getSortedCharacteristicsOfType(getBsegIdCharType());
				if(notNull(bsegs)&&!bsegs.isEmpty()){
					AdjustmentCharacteristic adjChar = bsegs.get(0);
					String bsegIdStr = adjChar.getSearchCharacteristicValue();
					if(!isBlankOrNull(bsegIdStr)){
						bsegId = new BillSegment_Id(bsegIdStr);
					}
				}
			}
		}
		
		return bsegId;
	}
	
	private void prorateTax(BillSegment_Id bsegId){
		BigDecimal bsegAmt = BigDecimal.ZERO;
		BigDecimal taxAmt = BigDecimal.ZERO;
		PreparedStatement pst = null;
		try{
			pst = createPreparedStatement(BSEG_CALC_DTL.toString(),"Get Tax Amount from Bseg");
			pst.setAutoclose(false);
			pst.bindId("bsegId", bsegId);
			pst.bindId("tax", getTaxDistCode().getId());
			SQLResultRow resultRow = pst.firstRow();
			if(notNull(resultRow)){
				bsegAmt = resultRow.getBigDecimal("BSEG_AMT");
				taxAmt = resultRow.getBigDecimal("TAX_AMT");
				
				if(notNull(bsegAmt)&&BigDecimal.ZERO.compareTo(bsegAmt)!=0 
						&& notNull(taxAmt)&&BigDecimal.ZERO.compareTo(taxAmt)!=0){
					value = (taxAmt.divide(bsegAmt,8,BigDecimal.ROUND_HALF_UP)).multiply(new BigDecimal("100"));
				}else{
					value = BigDecimal.ZERO;
				}
			}else{
				value = BigDecimal.ZERO;
			}
		}finally{
			if(notNull(pst)){
				pst.close();
				pst =null;
			}
		}
		logger.debug("Bill Segment "+bsegId+" Bill Segment Amount :"+bsegAmt+" Tax Amount :"+taxAmt+" Percentage:"+value);
	}
	
	
	@Override
	public BigDecimal getAggSqQuantity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ApplyRateData getApplyRateData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ApplyRateVersionData getApplyRateVersionData() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public List<BillSegmentCalculationHeaderData> getBillSegmentCalculationHeaderData() {
		// TODO Auto-generated method stub
		return listBillSegmentCalculationHeaderData;
	}

	@Override
	public List<BillSegmentItemData> getBillSegmentItemData() {
		// TODO Auto-generated method stub
		return listBillSegmentItemData;
	}

	@Override
	public List<BillSegmentServiceQuantityData> getBillSegmentServiceQuantityData() {
		// TODO Auto-generated method stub
		return listBillSegmentServiceQuantityData;
	}

	@Override
	public List<CharacteristicData> getCharacteristicData() {
		// TODO Auto-generated method stub
		return listCharacteristicData;
	}

	@Override
	public String getPriceCompId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BigDecimal getValue() {
		// TODO Auto-generated method stub
		return this.value;
	}

	@Override
	public void setAggSqQuantity(BigDecimal arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setApplyRateData(ApplyRateData arg0) {
		applyRateData =  arg0;
	}

	@Override
	public void setApplyRateVersionData(ApplyRateVersionData arg0) {
			applyRateVersionData = arg0;
	}

	@Override
	public void setBillSegmentCalculationHeaderData(
			List<BillSegmentCalculationHeaderData> arg0) {
		listBillSegmentCalculationHeaderData = arg0;
	}

	@Override
	public void setBillSegmentItemData(List<BillSegmentItemData> arg0) {
		listBillSegmentItemData = arg0;
	}

	@Override
	public void setBillSegmentServiceQuantityData(
			List<BillSegmentServiceQuantityData> arg0) {
		listBillSegmentServiceQuantityData = arg0;
	}

	@Override
	public void setCharacteristicData(List<CharacteristicData> arg0) {
		listCharacteristicData = arg0;
	}

	@Override
	public void setCrossReferenceAmount(BigDecimal arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCrossReferenceFound(Bool arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCrossReferenceServiceQuantity(BigDecimal arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPriceCompId(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRateComponent(RateComponent arg0) {
		// TODO Auto-generated method stub

	}

}
