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
 * This algorithm retrieves the Tax Rate Value from the custom table 
 * CM_TAX_RATE. If the Bill Factor parameter is provided, the tax 
 * rate value applied can be overridden using State as an input.
 * 
 **************************************************************************
 * 
 * CHANGE HISTORY:
 *
 * Date:        by:        Reason:
 * YYYY-MM-DD   IN         Reason text. 
 * 2020-10-08   JFerna     CB-239. Initial		   
 **************************************************************************
 */

package com.splwg.cm.domain.billing.algorithm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.Query;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.characteristicType.CharacteristicEntity_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.characteristicType.CharacteristicValue;
import com.splwg.ccb.api.lookup.CharacteristicEntityLookup;
import com.splwg.ccb.api.lookup.EntityFlagLookup;
import com.splwg.ccb.domain.admin.billFactor.BillFactor;
import com.splwg.ccb.domain.billing.billSegment.BillSegmentCalculationHeaderData;
import com.splwg.ccb.domain.billing.billSegment.BillSegmentItemData;
import com.splwg.ccb.domain.billing.billSegment.BillSegmentServiceQuantityData;
import com.splwg.ccb.domain.common.characteristic.CharacteristicData;
import com.splwg.ccb.domain.customerinfo.account.Account;
import com.splwg.ccb.domain.customerinfo.account.AccountPerson;
import com.splwg.ccb.domain.customerinfo.address.Address;
import com.splwg.ccb.domain.customerinfo.address.AddressCharacteristic;
import com.splwg.ccb.domain.customerinfo.address.AddressEntity;
import com.splwg.ccb.domain.customerinfo.person.Person;
import com.splwg.ccb.domain.customerinfo.serviceAgreement.ServiceAgreement;
import com.splwg.ccb.domain.rate.ApplyRateData;
import com.splwg.ccb.domain.rate.rateComponent.RateComponentValueAlgorithmSpot;
import com.splwg.ccb.domain.rate.rateVersion.ApplyRateVersionData;
import com.splwg.cm.api.lookup.AddressTypeFlgLookup;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;
import com.splwg.cm.domain.common.messageRepository.CmMessages;

/**
 * @author JFerna
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = characteristicType, name = billToPrimaryIndicatorCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = addressTypeFlg, name = billToAddressType, required = true, type = lookup)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = shipToPrimaryIndicatorCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = addressTypeFlg, name = shipToAddressType, required = true, type = lookup)
 *            , @AlgorithmSoftParameter (entityName = billFactor, name = billFactorCode, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = billFactorCharacteristicType, type = entity)})
 */
public class CmGetTaxRateValueAlgComp_Impl extends CmGetTaxRateValueAlgComp_Gen
		implements RateComponentValueAlgorithmSpot {
	
	//Constants
	private static final String PRIMARY_CHAR_VAL = "PRIMARY";
	private static final String EMPTY_STRING = "";
	
	//Hard Parameters
	private ApplyRateData applyRateData;
	private BigDecimal value;
	
	//Soft Parameters
	private CharacteristicType addressBillToIndicatorCharType;
	private CharacteristicType addressShipToIndicatorCharType;
	private BillFactor taxOverrideBillFactor;
	private CharacteristicType billFactorCharacteristicType;
	
	//Work Variables
	private Account account;
	private Person person;
		
	/**
	 * Validate Soft Parameters
	 * @param forAlgorithmValidation Boolean value
	 */
	protected void extraSoftParameterValidations(boolean forAlgorithmValidation) {
    	//Retrieve Algorithm Parameter Descriptions
    	String addressBillToIndicatorCharTypeDesc = getAlgorithm().getAlgorithmType().getParameterAt(0).fetchLanguageParameterLabel().trim();
    	String addressShipToIndicatorCharTypeDesc = getAlgorithm().getAlgorithmType().getParameterAt(2).fetchLanguageParameterLabel().trim();
    	String taxOverrideBillFactorDesc = getAlgorithm().getAlgorithmType().getParameterAt(4).fetchLanguageParameterLabel().trim();
    	String billFactorCharacteristicTypeDesc = getAlgorithm().getAlgorithmType().getParameterAt(5).fetchLanguageParameterLabel().trim();
    	
    	//Retrieve Soft Parameters
    	addressBillToIndicatorCharType = this.getBillToPrimaryIndicatorCharacteristicType();
    	addressShipToIndicatorCharType = this.getShipToPrimaryIndicatorCharacteristicType();
    	taxOverrideBillFactor = this.getBillFactorCode();
    	billFactorCharacteristicType = this.getBillFactorCharacteristicType();

    	
    	//Validate Bill To Address Characteristic Type and Value
    	validateCharacteristicType(addressBillToIndicatorCharType,CharacteristicEntityLookup.constants.ADDRESS_CHARACTERISTIC,addressBillToIndicatorCharTypeDesc);
			
    	//Validate Ship To Address Characteristic Type and Value
    	validateCharacteristicType(addressShipToIndicatorCharType,CharacteristicEntityLookup.constants.ADDRESS_CHARACTERISTIC,addressShipToIndicatorCharTypeDesc);
  	
    	//Validate Statement Indicator Address Characteristic Type and Value
    	if (notNull(billFactorCharacteristicType)){
        	validateCharacteristicType(billFactorCharacteristicType,CharacteristicEntityLookup.constants.BILL_FACTOR,billFactorCharacteristicTypeDesc);    
    	}
    	
    	if (notNull(taxOverrideBillFactor) && isNull(billFactorCharacteristicType)){
			addError(CmMessageRepository.getServerMessage(CmMessages.BOTH_PARM_MUST_BE_PROVIDED,
					taxOverrideBillFactorDesc,
					billFactorCharacteristicTypeDesc));
    	}
    	
    	if (isNull(taxOverrideBillFactor) && notNull(billFactorCharacteristicType)){
			addError(CmMessageRepository.getServerMessage(CmMessages.BOTH_PARM_MUST_BE_PROVIDED,
					taxOverrideBillFactorDesc,
					billFactorCharacteristicTypeDesc));
    	}		
	}
	
	/**
	 * Main Processing
	 */
	public void invoke() {
		
		//Initialize Tax Rate
		BigDecimal taxRate = BigDecimal.ZERO;
		
		//Retrieve Bill Segment Start Date
		Date bsegStartDate = applyRateData.getBillSegmentPeriodStart();
		
		//Retrieve Contract
		ServiceAgreement sa = applyRateData.getServiceAgreement();
		
		//Retrieve Account
		account = notNull(sa) ? sa.getAccount() : null;
		
		//Retrieve Main Person
		person = null;
		AccountPerson accountPerson = null;
		if(notNull(account)){
			Iterator<AccountPerson> accountPersonIter = account.getPersons().iterator();
	        while(accountPersonIter.hasNext()) {
	        	accountPerson = accountPersonIter.next();
	            if(accountPerson.getIsMainCustomer().isTrue()) {
	            	person = accountPerson.fetchIdPerson();
	                break;
	            }
	        } 
		}
		
		if (notNull(person)){
			//Retrieve Main Person Address
			Address mainPersonAddress = retrieveMainPersonAddress();
			String city = !isBlankOrNull(mainPersonAddress.getCity()) ? mainPersonAddress.getCity().toUpperCase().trim() : null;
			String county = !isBlankOrNull(mainPersonAddress.getCounty()) ? mainPersonAddress.getCounty().toUpperCase().trim() : null;
			String state = !isBlankOrNull(mainPersonAddress.getState()) ? mainPersonAddress.getState().toUpperCase().trim() : null;
			String postal = !isBlankOrNull(mainPersonAddress.getPostal()) ? mainPersonAddress.getPostal().toUpperCase().trim() : null;
			
			//Retrieve Effective Combined Rate
			BigDecimal combinedRate = retrieveCmTaxRate(bsegStartDate,city,county,state,postal);
			
			//Determine Tax Override for State
			if (notNull(taxOverrideBillFactor) && notNull(billFactorCharacteristicType)){
				
				//Retrieve Effective Bill Factor Value
				BigDecimal bfVal = retrieveBillFactorValue(bsegStartDate,state);
				
				//If Bill Factor Value is found, calculate the tax rate
				if (notNull(bfVal)){
					taxRate = combinedRate.multiply(bfVal);
				//Otherwise, set tax rate equal to combined rate
				}else{
					taxRate = combinedRate;	
				}				
			}else{
				taxRate = combinedRate;			
			}			
		}	
		
		//Set Tax Rate Value
		value = taxRate;
	}
	
	/**
	 * This method retrieves Main Person Address
	 * @return Address
	 */
	private Address retrieveMainPersonAddress(){
		//Initialize
		Address shipToAddress = null;
		Address billToAddress = null;
		
		//Retrieve Main Person Ship To Address
		shipToAddress = getPrimaryPersonAddress(person,
				this.getShipToAddressType(), 
				addressShipToIndicatorCharType, 
				PRIMARY_CHAR_VAL);
		
		//If Main Person has no Primary Ship To Address, retrieve Primary Bill To Address
		if (isNull(shipToAddress)){
			billToAddress = getPrimaryPersonAddress(person,
					this.getBillToAddressType(), 
					addressBillToIndicatorCharType, 
					PRIMARY_CHAR_VAL);
			
			if (isNull(billToAddress)){
				//If Main Person has no Primary Bill To Address, raise an error.
				addError(CmMessageRepository.getServerMessage(CmMessages.NO_PRIM_SHIP_BILL_TO_ADDRESS_FOUND,
						account.getId().getIdValue(),
						person.getId().getIdValue()));
			}else{
				return billToAddress;
			}
		}		
		return shipToAddress;
	}
	
	/**
	 * This method checks if the Characteristic Type is valid for an Entity.
	 * @param Characteristic Type to Validate
	 * @param Entity to be checked on
	 * @param Description of the Soft Parameter
	 */
	private void validateCharacteristicType(CharacteristicType charType, CharacteristicEntityLookup charEntLkup,
			String parmDesc){
		CharacteristicEntity_Id charEntityId = new CharacteristicEntity_Id(charType, charEntLkup);
		
		if(isNull(charEntityId.getEntity())){			
			addError(CmMessageRepository.getServerMessage(CmMessages.CHAR_TYPE_INVALID_FOR_ENTITY,
					parmDesc,charType.getId().getIdValue(),
					charEntLkup.getLookupValue().fetchLanguageDescription()));
		}
	}
	
	/**
	 * This method retrieves Primary Bill To/Ship To Person Address
	 * @param person
	 * @param addressCharType
	 * @param addressCharVal
	 * @return Address
	 */
	private Address getPrimaryPersonAddress(Person person, AddressTypeFlgLookup addressType, CharacteristicType addressCharType, String addressCharVal){
		Query<Address> getAddressQry = createQuery(
				"FROM Address address, " +
				"     AddressEntity addressEntity, " +
				"     AddressCharacteristic addressChar " +
				"WHERE address.id = addressEntity.id.address " +
				"  AND addressEntity.id.address = addressChar.id.address " +
				"  AND addressChar.id.characteristicType = :addressCharType " +
				"  AND addressChar.searchCharacteristicValue= :addressCharVal " +
				"  AND addressEntity.id.collectionEntityId = :entityId " +
				"  AND addressEntity.id.addressTypeFlg = :addressTypeFlag " +
				"  AND addressEntity.id.entityType = :entityType ", "");
		
		getAddressQry.bindEntity("addressCharType", addressCharType);
		getAddressQry.bindStringProperty("addressCharVal", AddressCharacteristic.properties.searchCharacteristicValue, addressCharVal);
		getAddressQry.bindStringProperty("entityId", AddressEntity.properties.collectionEntityId, person.getId().getIdValue());
		getAddressQry.bindLookup("addressTypeFlag", addressType);
		getAddressQry.bindLookup("entityType", EntityFlagLookup.constants.PERSON);
		getAddressQry.addResult("address", "address");
		
		return getAddressQry.firstRow();
	}
	
	/**
	 * This method retrieves combined rate from custom tax rate table
	 * @param effectiveDate
	 * @param city
	 * @param county
	 * @param state
	 * @param postal
	 * @return combinedRate
	 */
	private BigDecimal retrieveCmTaxRate(Date effectiveDate, String city, String county, String state, String postal){
	     BigDecimal combinedRate = BigDecimal.ZERO;
		 List<SQLResultRow> resultRowList = new ArrayList<SQLResultRow>();
		 
		 StringBuilder strBuilder = new StringBuilder();
	     strBuilder.append(" SELECT ");
	     strBuilder.append(" 	A.CM_COMB_RATE ");
	     strBuilder.append(" FROM  ");
	     strBuilder.append(" 	CM_TAX_RATE A "); 
	     
	     //City
	     if (notNull(city)){
		     strBuilder.append(" 	WHERE TRIM(UPPER(A.CITY)) = :city ");
	     }else{
	    	 strBuilder.append(" 	WHERE TRIM(UPPER(A.CITY)) IS NULL ");
	     }	     
	     //County
	     if (notNull(county)){
		     strBuilder.append(" 	AND TRIM(UPPER(A.COUNTY)) = :county ");
	     }else{
	    	 strBuilder.append(" 	AND TRIM(UPPER(A.COUNTY)) IS NULL ");
	     }	     
	     //State
	     if (notNull(state)){
		     strBuilder.append(" 	AND TRIM(UPPER(A.STATE)) = :state ");
	     }else{
	    	 strBuilder.append(" 	AND TRIM(UPPER(A.STATE)) IS NULL ");
	     }	     
	     //Postal
	     if (notNull(postal)){
		     strBuilder.append(" 	AND TRIM(UPPER(A.POSTAL_CD)) = :postal ");
	     }else{
	    	 strBuilder.append(" 	AND TRIM(UPPER(A.POSTAL_CD)) IS NULL ");
	     }
     
	     strBuilder.append(" 	    AND A.EFFDT = (SELECT MAX(B.EFFDT) ");
	     strBuilder.append("                       FROM CM_TAX_RATE B ");
	     strBuilder.append("                       WHERE B.CM_TAID = A.CM_TAID ");
	     
	     //City
	     if (notNull(city)){
		     strBuilder.append(" 	               AND TRIM(UPPER(B.CITY)) = :city ");
	     }else{
	    	 strBuilder.append(" 	               AND TRIM(UPPER(B.CITY)) IS NULL ");
	     }	     
	     //County
	     if (notNull(county)){
		     strBuilder.append(" 	               AND TRIM(UPPER(B.COUNTY)) = :county ");
	     }else{
	    	 strBuilder.append(" 	               AND TRIM(UPPER(B.COUNTY)) IS NULL ");
	     }	     
	     //State
	     if (notNull(state)){
		     strBuilder.append(" 	               AND TRIM(UPPER(B.STATE)) = :state ");
	     }else{
	    	 strBuilder.append(" 	               AND TRIM(UPPER(B.STATE)) IS NULL ");
	     }	     
	     //Postal
	     if (notNull(postal)){
		     strBuilder.append(" 	               AND TRIM(UPPER(B.POSTAL_CD)) = :postal ");
	     }else{
	    	 strBuilder.append(" 	               AND TRIM(UPPER(B.POSTAL_CD)) IS NULL ");
	     }
	     //Effective Date
	     strBuilder.append("                       AND TO_CHAR(B.EFFDT,'YYYY-MM-DD') <= TO_CHAR(:bsegStartDate,'YYYY-MM-DD')) ");
	     
	     PreparedStatement cmTaxQuery = createPreparedStatement(strBuilder.toString(), EMPTY_STRING);
	     //City
	     if (notNull(city)){
		     cmTaxQuery.bindString("city", city, "");
	     }
	     //County
	     if (notNull(county)){
	    	 cmTaxQuery.bindString("county", county, "");
	     }
	     //State
	     if (notNull(state)){
	    	 cmTaxQuery.bindString("state", state, "");
	     }
	     //Postal
	     if (notNull(postal)){
	    	 cmTaxQuery.bindString("postal", postal, "");
	     }
	     //Effective Date
	     cmTaxQuery.bindDate("bsegStartDate", effectiveDate);
	     
	     resultRowList = cmTaxQuery.list();
	     cmTaxQuery.close();
	     
	     SQLResultRow resultRow = null;
	     if (notNull(resultRowList) && !resultRowList.isEmpty()){
	    	 resultRow = resultRowList.get(0);
	     }
	     
	     if (notNull(resultRow)){
	    	 combinedRate = resultRow.getBigDecimal("CM_COMB_RATE");	    	 
	     }
	     	
		return combinedRate;
	}
	
	/**
	 * This method retrieves Bill Factor Value
	 * @return Bill Factor Value
	 */
	private BigDecimal retrieveBillFactorValue(Date effectiveDate, String state) {
		Query<BigDecimal> billFactorValueQuery = createQuery ("FROM BillFactorValue BFV" +
				" WHERE BFV.id.billFactorCharacteristic.id.billFactor = :billFactor" +
				" AND BFV.id.billFactorCharacteristic.id.characteristicValue.id.characteristicType = :charType" +
				" AND BFV.id.billFactorCharacteristic.id.characteristicValue.id.characteristicValue = :charVal" +
				" AND BFV.id.effectiveDate = (SELECT MAX(BFV2.id.effectiveDate) FROM BillFactorValue BFV2" +
				                           " WHERE BFV2.id.billFactorCharacteristic.id.billFactor = BFV.id.billFactorCharacteristic.id.billFactor" +
				                           " AND BFV2.id.billFactorCharacteristic.id.characteristicValue.id.characteristicType = BFV.id.billFactorCharacteristic.id.characteristicValue.id.characteristicType" +
				                           " AND BFV2.id.billFactorCharacteristic.id.characteristicValue.id.characteristicValue = BFV.id.billFactorCharacteristic.id.characteristicValue.id.characteristicValue" +
				                           " AND BFV2.id.effectiveDate <= :effDt)", EMPTY_STRING);

		billFactorValueQuery.bindEntity("billFactor", taxOverrideBillFactor);
		billFactorValueQuery.bindEntity("charType", billFactorCharacteristicType);
		billFactorValueQuery.bindStringProperty("charVal", CharacteristicValue.properties.characteristicValue, state);
		billFactorValueQuery.bindDate("effDt", effectiveDate);
		billFactorValueQuery.addResult("bfValue", "BFV.value");

		return billFactorValueQuery.firstRow();
	}

	/**
	 * This method is responsible for returning the value for Apply Rate
	 * Data.
	 * 
	 * @return applyRateData
	 */
	public ApplyRateData getApplyRateData() {
		return applyRateData;
	}

	/**
	 * This method is responsible for setting the value for Apply Rate
	 * Data.
	 * 
	 * @param applyRateData
	 */
	public void setApplyRateData(ApplyRateData applyRateData) {
		this.applyRateData = applyRateData;
	}

	/**
	 * This method is responsible for returning the value for Rate Value
	 * Data.
	 * 
	 * @return value
	 */
	public BigDecimal getValue() {
		return value;
	}
	
	/*
	 * Unused Override Getters and Setters
	 */
	@Override
	public BigDecimal getAggSqQuantity() {
		return null;
	}

	@Override
	public ApplyRateVersionData getApplyRateVersionData() {
		return null;
	}

	@Override
	public List<BillSegmentCalculationHeaderData> getBillSegmentCalculationHeaderData() {
		return null;
	}

	@Override
	public List<BillSegmentItemData> getBillSegmentItemData() {
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
	public String getPriceCompId() {
		return null;
	}

}
