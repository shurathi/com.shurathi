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
 * Payment Characteristic Change Handler
 * 
 **************************************************************************
 * 
 * CHANGE HISTORY:
 *
 * Date:        by:        Reason:
 * YYYY-MM-DD   IN         Reason text. 
 * 2020-08-13   JFerna     CB-263. Initial		   
 **************************************************************************
 */
package com.splwg.cm.domain.payment.changeHandler;

import java.math.BigInteger;

import com.splwg.base.api.DataTransferObject;
import com.splwg.base.api.Query;
import com.splwg.base.api.changehandling.AbstractChangeHandler;
import com.splwg.base.api.lookup.CharacteristicTypeLookup;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.featureConfiguration.FeatureConfigurationOption;
import com.splwg.ccb.domain.payment.payment.Payment;
import com.splwg.ccb.domain.payment.payment.PaymentCharacteristic;
import com.splwg.ccb.domain.payment.paymentEvent.PaymentEventPaymentTenders;
import com.splwg.ccb.domain.payment.paymentEvent.PaymentTender;
import com.splwg.ccb.domain.payment.paymentEvent.PaymentTenderCharacteristic;
import com.splwg.ccb.domain.payment.paymentEvent.PaymentTenderCharacteristic_DTO;
import com.splwg.ccb.domain.payment.paymentEvent.PaymentTenderCharacteristic_Id;
import com.splwg.cm.api.lookup.CmpcOptTypFlgLookup;
import com.splwg.cm.api.lookup.ExternalSystemTypeLookup;

/**
 * @author JFerna
 *
@ChangeHandler (entityName = paymentCharacteristic)
 */
public class CmPaymentCharacteristic_CHandler extends AbstractChangeHandler<PaymentCharacteristic> {

	@Override
	public void handleAddOrChange(PaymentCharacteristic payChar,
			DataTransferObject<PaymentCharacteristic> oldDTO) {
		CharacteristicType charType = payChar.fetchIdCharacteristicType();
		CharacteristicTypeLookup characteristicTypeLookup=charType.getCharacteristicType();
		String charValue = fetchCharvalRelatedToCharType(characteristicTypeLookup, payChar);
		if(isPaymentCharTypeConfigured(charType.getId().getIdValue().trim())){
			Payment payment=payChar.fetchIdPayment();
			PaymentEventPaymentTenders paymentTenders=payment.getPaymentEvent().getPaymentTenders();
			for(PaymentTender payTndr: paymentTenders){
				addUpdateTenderCharacteristic(payTndr, charType, charValue, characteristicTypeLookup);
			}
		}
	}
	
	/**
	 * This method will add or update the characteristics on Payment tender.
	 * 
	 * @param paymentTender
	 * @param charType
	 * @param charValue
	 * @param characteristicTypeLookup
	 * 
	 */
	public void addUpdateTenderCharacteristic(PaymentTender paymentTender, CharacteristicType charType, String charValue, CharacteristicTypeLookup characteristicTypeLookup) {
		PaymentTenderCharacteristic_DTO paymentTenderCharacteristicDTO=null;
		PaymentTenderCharacteristic_Id paymentTenderCharacteristicId=new PaymentTenderCharacteristic_Id(paymentTender, charType, BigInteger.ONE);
		PaymentTenderCharacteristic paymentTenderCharacteristic = paymentTenderCharacteristicId.getEntity();
		//Add new characteristic
		if(isNull(paymentTenderCharacteristic)){
			paymentTenderCharacteristicDTO=paymentTender.getCharacteristics().newChildDTO();
			//Set Id
			paymentTenderCharacteristicDTO.setId(paymentTenderCharacteristicId);
		}
		//Update existing characteristic
		else{
			paymentTenderCharacteristicDTO=paymentTenderCharacteristic.getDTO();
		}
		
		//Set Characteristic Value
		if(characteristicTypeLookup.isAdhocValue()){
			paymentTenderCharacteristicDTO.setAdhocCharacteristicValue(charValue);
		}else if(characteristicTypeLookup.isPredefinedValue()){
			paymentTenderCharacteristicDTO.setCharacteristicValue(charValue);
		}else if(characteristicTypeLookup.isForeignKeyValue()){
			paymentTenderCharacteristicDTO.setCharacteristicValueForeignKey1(charValue);
		}
		else
			paymentTenderCharacteristicDTO.setAdhocCharacteristicValue(charValue);
		
		//Add new characteristic
		if(isNull(paymentTenderCharacteristic)){
			paymentTender.getCharacteristics().add(paymentTenderCharacteristicDTO, charType.getId(), null);
		}
		//Update existing characteristic
		else{
			paymentTenderCharacteristic.setDTO(paymentTenderCharacteristicDTO);
		}
	}
	
	/**
	 * This method checks whether Payment Char type is configured to be copied in Lookup or not
	 * @param charType
	 * @return
	 */
	public Boolean isPaymentCharTypeConfigured(String charType)	{
		StringBuilder queryString = new StringBuilder();
		queryString.append("FROM FeatureConfiguration featureConfig, FeatureConfigurationOption featureConfigOpt ");
		queryString.append("WHERE featureConfig.id=featureConfigOpt.id.workforceManagementSystem ");
		queryString.append("AND featureConfig.featureType=:featureType ");        
		queryString.append("AND featureConfigOpt.id.optionType=:optionType ");  
		queryString.append("AND featureConfigOpt.value= :charType" );
		Query<Long> query = createQuery(queryString.toString(),"isPaymentCharTypeConfigured");                            							
		query.bindStringProperty("charType", FeatureConfigurationOption.properties.value, charType);
		query.bindLookup("featureType", ExternalSystemTypeLookup.constants.CM_PAYMENT_CHAR_CHANGE_HANDLER);
		query.bindLookup("optionType", CmpcOptTypFlgLookup.constants.CM_CHARACTERISTIC_TYPE_TO_COPY);
		
		query.addResult("charCount", "count(featureConfigOpt.value)"); 
		Long result=query.firstRow();

		if((notNull(result)) && result>0){
			return true;
		}
			
		return false;
	}
	
	/**
	 * This method fetches characteristic value based on characteristic type provided
	 * @param characteristicTypeLookup
	 * @param businessEntity
	 * @param charType
	 * @return
	 */
	public String fetchCharvalRelatedToCharType(CharacteristicTypeLookup characteristicTypeLookup, PaymentCharacteristic payChar){
		String charValue="";
		if(characteristicTypeLookup.isAdhocValue())
			charValue =  payChar.getAdhocCharacteristicValue().trim();
		else if(characteristicTypeLookup.isForeignKeyValue())
			charValue =  payChar.getCharacteristicValueForeignKey1().trim();
		else if(characteristicTypeLookup.isPredefinedValue())
			charValue =  payChar.getCharacteristicValue().trim();
		else
			charValue = payChar.getAdhocCharacteristicValue();
		return charValue;
	}
}
