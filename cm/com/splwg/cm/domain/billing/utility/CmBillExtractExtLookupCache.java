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
 * Bill Extract Extendable Lookup Cache
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:         Reason:
 * 2020-06-08   JFerna      CB-94. Initial Version. 
***********************************************************************
 */
package com.splwg.cm.domain.billing.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Document;
import org.dom4j.Element;

import com.splwg.base.api.ApplicationCache;
import com.splwg.base.api.GenericBusinessComponent;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.characteristicType.CharacteristicType_Id;
import com.splwg.base.domain.common.lookup.LookupField_Id;
import com.splwg.base.domain.common.lookup.LookupValue;
import com.splwg.base.domain.common.lookup.LookupValue_Id;
import com.splwg.base.support.context.ContextHolder;
import com.splwg.base.support.schema.BusinessObjectInfo;
import com.splwg.base.support.schema.BusinessObjectInfoCache;
import com.splwg.base.support.schema.ExtendedLookupValueInfo;
import com.splwg.ccb.api.lookup.BillFormatLookup;
import com.splwg.ccb.domain.admin.idType.IdType_Id;
import com.splwg.ccb.domain.admin.serviceQuantityIdentifier.ServiceQuantityIdentifier_Id;
import com.splwg.cm.domain.billing.utility.CmTemplateMappingData;

/**
 * @author JFerna
 *
 */
public final class CmBillExtractExtLookupCache extends
		GenericBusinessComponent implements ApplicationCache {

	private final static CmBillExtractExtLookupCache INSTANCE = new CmBillExtractExtLookupCache();	
	private Map<String,CmBillExtractExtLookupVO> billExtractDefaultExtLookupMap = new ConcurrentHashMap<String,CmBillExtractExtLookupVO>();
	private Map<String,CmBillExtractExtLookupVO> billExtractConfigExtLookupMap = new ConcurrentHashMap<String,CmBillExtractExtLookupVO>();

	/**
	 * This method gets the name of cache
	 */
	@Override
	public String getName() {
		return "CmBillExtractExtLookupCache";
	}

	/**
	 * This method clears the cache
	 */
	@Override
	public void flush() {
		billExtractDefaultExtLookupMap.clear();
		billExtractConfigExtLookupMap.clear();
	}

	/**
	 * This method registers the cache
	 */
	private CmBillExtractExtLookupCache(){
		ContextHolder.getContext().registerCache(this);	
	}
	
	/**
	 * This method gets Bill Extract Default Configuration Lookup
	 * @param extendedLookupBO
	 * @param customerClass
	 * @return Extendabale Lookup VO
	 */
	public static CmBillExtractExtLookupVO getBillExtractDefaultConfigLookup(BusinessObject extendedLookupBO, String customerClass){
		CmBillExtractExtLookupVO extLookupVO = null;
		
		if(INSTANCE.billExtractDefaultExtLookupMap.containsKey(customerClass)){
			extLookupVO = INSTANCE.billExtractDefaultExtLookupMap.get(customerClass);
		}else{
			extLookupVO = INSTANCE.fetchBillExtractDefaultExtLookupData(extendedLookupBO, customerClass);
			
			if(extLookupVO != null){
				INSTANCE.billExtractDefaultExtLookupMap.put(customerClass,extLookupVO);
			} 		
		}		
		return extLookupVO;
	}
	
	/**
	 * Thsi method gets Bill Extract Configuration Lookup
	 * @param extendedLookupBO
	 * @param customerClass
	 * @return Extendabale Lookup VO
	 */
	public static CmBillExtractExtLookupVO getBillExtractConfigLookup(BusinessObject extendedLookupBO,String customerClass){
		CmBillExtractExtLookupVO extLookupVO = null;
		
		if(INSTANCE.billExtractConfigExtLookupMap.containsKey(customerClass)){
			extLookupVO = INSTANCE.billExtractConfigExtLookupMap.get(customerClass);
		}else{
			extLookupVO = INSTANCE.fetchBillExtractConfigExtLookupData(extendedLookupBO, customerClass);
			
			if (extLookupVO != null){
				INSTANCE.billExtractConfigExtLookupMap.put(customerClass,extLookupVO);
			}			
		}
		return extLookupVO;
	}
	
	/**
	 * This method retrieves Bill Extract Default Configuration Data
	 * @param extendedLookupBO
	 * @param customerClass
	 * @return Extendabale Lookup VO
	 */
	@SuppressWarnings("unchecked")
	private  synchronized CmBillExtractExtLookupVO fetchBillExtractDefaultExtLookupData(BusinessObject extendedLookupBO,String customerClass){
		
		CmBillExtractExtLookupVO lookupVO = null;
		Document extLookupDocument = fetchActiveExtendedLookupDocument(extendedLookupBO.getId().getTrimmedValue(),customerClass);

		if(notNull(extLookupDocument)){
			//Read and set all configuration data
			lookupVO = new CmBillExtractExtLookupVO();
			Element rootElement = extLookupDocument.getRootElement();
			
			//Details
			String fileNamePrefix = rootElement.elementText(CmBillExtractConstants.FILE_NAME_PREFIX_ELEM);
			String extractDateFormat = rootElement.elementText(CmBillExtractConstants.EXTRACT_DT_FORMAT_ELEM);	
			String billPeriodDateFormat = rootElement.elementText(CmBillExtractConstants.BILL_PERIOD_DT_FORMAT_ELEM);
			String federalTaxIdNumber = rootElement.elementText(CmBillExtractConstants.FEDERAL_TAX_ID_NBR_ELEM);
			String billToCountry = rootElement.elementText(CmBillExtractConstants.BILL_TO_COUNTRY_ELEM);
			
			//Remittance Detail
			Element remittanceDetailElement = rootElement.element(CmBillExtractConstants.REMITTANCE_DTL_GRP);
			String brandLogo = remittanceDetailElement.elementText(CmBillExtractConstants.BRAND_LOGO_ELEM);
			String remittanceName = remittanceDetailElement.elementText(CmBillExtractConstants.REMITTANCE_NAME_ELEM);
			String remittanceAddress1 = remittanceDetailElement.elementText(CmBillExtractConstants.REMITTANCE_ADDR1_ELEM);
			String remittanceAddress2 = remittanceDetailElement.elementText(CmBillExtractConstants.REMITTANCE_ADDR2_ELEM);
			String remittanceAddress3 = remittanceDetailElement.elementText(CmBillExtractConstants.REMITTANCE_ADDR3_ELEM);
			String remittanceAddress4 = remittanceDetailElement.elementText(CmBillExtractConstants.REMITTANCE_ADDR4_ELEM);
			String remittanceCity = remittanceDetailElement.elementText(CmBillExtractConstants.REMITTANCE_CITY_ELEM);
			String remittanceCountry = remittanceDetailElement.elementText(CmBillExtractConstants.REMITTANCE_COUNTRY_ELEM);
			String remittanceCounty = remittanceDetailElement.elementText(CmBillExtractConstants.REMITTANCE_COUNTY_ELEM);
			String remittanceState = remittanceDetailElement.elementText(CmBillExtractConstants.REMITTANCE_STATE_ELEM);
			String remittancePostal = remittanceDetailElement.elementText(CmBillExtractConstants.REMITTANCE_POSTAL_ELEM);
			String remittanceContact = remittanceDetailElement.elementText(CmBillExtractConstants.REMITTANCE_CONTACT_ELEM);
			String bankName = remittanceDetailElement.elementText(CmBillExtractConstants.BANK_NAME_ELEM);
			String bankAccountNumber = remittanceDetailElement.elementText(CmBillExtractConstants.BANK_ACCT_NBR_ELEM);
			String wireRoutingNumber = remittanceDetailElement.elementText(CmBillExtractConstants.WIRE_ROUTING_NBR_ELEM);
			String achRoutingNumber = remittanceDetailElement.elementText(CmBillExtractConstants.ACH_ROUTING_NBR_ELEM);
			String email = remittanceDetailElement.elementText(CmBillExtractConstants.EMAIL_ELEM);
			
			//Template Mappings
			List<Element> templateMappings = rootElement.elements(CmBillExtractConstants.TEMPLATE_MAPPINGS_LIST);			
			List<CmTemplateMappingData> templateMappingDataList = new ArrayList<CmTemplateMappingData>();
			CmTemplateMappingData templateMappingData;
			String billFormatString;
			LookupValue_Id billFormatLookupId;
			LookupValue billFormatLookup = null;	
			List<Element> temaplateCodeElemList;
			List<String> temaplateCodeList = null;		
			
			for(Element template : templateMappings){
				templateMappingData = null;
		    	templateMappingData = CmTemplateMappingData.Factory.newInstance();		    	
				billFormatString = template.elementText(CmBillExtractConstants.BILL_FORMAT_ELEM);
				temaplateCodeElemList = template.elements(CmBillExtractConstants.TEMPLATE_LIST);
				
				billFormatLookupId = new LookupValue_Id(new LookupField_Id("BILL_FORMAT_FLG"),billFormatString);				
				if(notNull(billFormatLookupId.getEntity())){
					billFormatLookup = billFormatLookupId.getEntity();
				}
								
				if(notNull(temaplateCodeElemList) && !temaplateCodeElemList.isEmpty()){
					temaplateCodeList = new ArrayList<String>();
					for(Element templateCodeElem : temaplateCodeElemList){
						temaplateCodeList.add(templateCodeElem.elementText(CmBillExtractConstants.TEMPLATE_CD_ELEM));
					}
				}
				
				templateMappingData.setBillFormat((BillFormatLookup)billFormatLookup.asLookup());
				templateMappingData.setTemplateCodes(temaplateCodeList);				
				templateMappingDataList.add(templateMappingData);
			}
			
			//Set Default Configuration Values
			//File Name Prefix
			if(!isBlankOrNull(fileNamePrefix)){
				lookupVO.setFileNamePrefix(fileNamePrefix);
			}
			
			//Extract Date Format
			if(!isBlankOrNull(extractDateFormat)){
				lookupVO.setExtractDateFormat(extractDateFormat);
			}
			
			//Bill Period Date Format
			if(!isBlankOrNull(billPeriodDateFormat)){
				lookupVO.setBillPeriodDateFormat(billPeriodDateFormat);
			}
			
			//Federal Tax Id Number
			if(!isBlankOrNull(federalTaxIdNumber)){
				lookupVO.setFederalTaxIdNumber(federalTaxIdNumber);
			}
			
			//Bill To Country
			if(!isBlankOrNull(billToCountry)){
				lookupVO.setBillToCountry(billToCountry);
			}
			
			//Brand Logo
			if(!isBlankOrNull(brandLogo)){
				lookupVO.setBrandLogo(brandLogo);
			}
			
			//Remittance Name
			if(!isBlankOrNull(brandLogo)){
				lookupVO.setRemittanceName(remittanceName);
			}
			
			//Remittance Address 1
			if(!isBlankOrNull(remittanceAddress1)){
				lookupVO.setRemittanceAddress1(remittanceAddress1);
			}
			
			//Remittance Address 2
			if(!isBlankOrNull(remittanceAddress2)){
				lookupVO.setRemittanceAddress2(remittanceAddress2);
			}
			
			//Remittance Address 3
			if(!isBlankOrNull(remittanceAddress3)){
				lookupVO.setRemittanceAddress3(remittanceAddress3);
			}
			
			//Remittance Address 4
			if(!isBlankOrNull(remittanceAddress4)){
				lookupVO.setRemittanceAddress4(remittanceAddress4);
			}
			
			//Remittance City
			if(!isBlankOrNull(remittanceCity)){
				lookupVO.setRemittanceCity(remittanceCity);
			}
			
			//Remittance Country
			if(!isBlankOrNull(remittanceCountry)){
				lookupVO.setRemittanceCountry(remittanceCountry);
			}
			
			//Remittance County
			if(!isBlankOrNull(remittanceCounty)){
				lookupVO.setRemittanceCounty(remittanceCounty);
			}
			
			//Remittance State
			if(!isBlankOrNull(remittanceState)){
				lookupVO.setRemittanceState(remittanceState);
			}
			
			//Remittance Postal
			if(!isBlankOrNull(remittancePostal)){
				lookupVO.setRemittancePostal(remittancePostal);
			}
			
			//Remittance Contact
			if(!isBlankOrNull(remittanceContact)){
				lookupVO.setRemittanceContact(remittanceContact);
			}
			
			//Bank Name
			if(!isBlankOrNull(bankName)){
				lookupVO.setBankName(bankName);
			}
			
			//Bank Account Number
			if(!isBlankOrNull(bankAccountNumber)){
				lookupVO.setBankAccountNumber(bankAccountNumber);
			}
			
			//Wire Routing Number
			if(!isBlankOrNull(wireRoutingNumber)){
				lookupVO.setWireRoutingNumber(wireRoutingNumber);
			}
			
			//ACH Routing Number
			if(!isBlankOrNull(achRoutingNumber)){
				lookupVO.setAchRoutingNumber(achRoutingNumber);
			}
			
			//Email
			if(!isBlankOrNull(email)){
				lookupVO.setEmail(email);
			}
			
			//Template Mappings
			if(notNull(templateMappingDataList)){
				lookupVO.setTemplateMappings(templateMappingDataList);
			}			
		}
		
		return lookupVO;
	}
	
	/**
	 * Thsi method retrieves Bill Extract Configuration Data
	 * @param extendedLookupBO
	 * @param customerClass
	 * @return Extendable Lookup VO
	 */
	private synchronized CmBillExtractExtLookupVO fetchBillExtractConfigExtLookupData(BusinessObject extendedLookupBO,String customerClass){
		
		CmBillExtractExtLookupVO lookupVO = null;
		Document extLookupDocument = fetchActiveExtendedLookupDocument(extendedLookupBO.getId().getTrimmedValue(),customerClass);

		if(notNull(extLookupDocument)){
			//Read and set all configuration data
			lookupVO = new CmBillExtractExtLookupVO();
			Element rootElement = extLookupDocument.getRootElement();
			
			//Details
			String customerNumberIdentifierTypeStr = rootElement.elementText(CmBillExtractConstants.CUSTNUM_ID_TYPE_ELEM);
			String transactionQuantitySqiStr = rootElement.elementText(CmBillExtractConstants.TRANS_QTY_SQI_ELEM);
			
			//Characteristic Configuration
			Element characteristicConfiguration = rootElement.element(CmBillExtractConstants.CHAR_CONFIG_GRP);
			String loanNumberCharacteristicTypeStr = characteristicConfiguration.elementText(CmBillExtractConstants.LOAN_NBR_CHAR_TYPE_ELEM);
			String orderNumberCharacteristicTypeStr = characteristicConfiguration.elementText(CmBillExtractConstants.ORDER_NBR_CHAR_TYPE_ELEM);
			String borrowerNameCharacteristicTypeStr = characteristicConfiguration.elementText(CmBillExtractConstants.BRW_NAME_CHAR_TYPE_ELEM);
			String propertyAddressCharacteristicTypeStr = characteristicConfiguration.elementText(CmBillExtractConstants.PROPERTY_ADDR_CHAR_TYPE_ELEM);
			String propertyCityCharacteristicTypeStr = characteristicConfiguration.elementText(CmBillExtractConstants.PROPERTY_CITY_CHAR_TYPE_ELEM);
			String propertyStateCharacateristicTypeStr = characteristicConfiguration.elementText(CmBillExtractConstants.PROPERTY_STATE_CHAR_TYPE_ELEM);
			String propertyZipCharacteristicTypeStr = characteristicConfiguration.elementText(CmBillExtractConstants.PROPERTY_ZIP_CHAR_TYPE_ELEM);
			String paymentTermsCharacteristicTypeStr = characteristicConfiguration.elementText(CmBillExtractConstants.PAY_TERMS_CHAR_TYPE_ELEM);
			String chargeCalcLineCharacteristicTypeStr = characteristicConfiguration.elementText(CmBillExtractConstants.CHG_CALC_LINE_CHAR_TYPE_ELEM);
			String taxCalcLineCharacteristicValue = characteristicConfiguration.elementText(CmBillExtractConstants.TAX_CALC_LINE_CHAR_VAL_ELEM);
			String discountCalcLineCharacteristicValue = characteristicConfiguration.elementText(CmBillExtractConstants.DISCOUNT_CALC_LINE_CHAR_VAL_ELEM);
			
			//Set Configuration Values
			//Customer Number Id Type
			if(!isBlankOrNull(customerNumberIdentifierTypeStr)){
				IdType_Id customerNumberIdentifierType = new IdType_Id(customerNumberIdentifierTypeStr); 
				if(notNull(customerNumberIdentifierType.getEntity())){
					lookupVO.setCustomerNumberIdentifierType(customerNumberIdentifierType);
				}
			}
			
			//Transaction SQI
			if(!isBlankOrNull(transactionQuantitySqiStr)){
				ServiceQuantityIdentifier_Id transactionQuantitySqi = new ServiceQuantityIdentifier_Id(transactionQuantitySqiStr); 
				if(notNull(transactionQuantitySqi.getEntity())){
					lookupVO.setTransactionQuantitySqi(transactionQuantitySqi);
				}
			}
			
			//Loan Number Char Type
			if(!isBlankOrNull(loanNumberCharacteristicTypeStr)){
				CharacteristicType_Id loanNumberCharacteristicType = new CharacteristicType_Id(loanNumberCharacteristicTypeStr); 
				if(notNull(loanNumberCharacteristicType.getEntity())){
					lookupVO.setLoanNumberCharacteristicType(loanNumberCharacteristicType.getEntity());
				}
			}
			
			//Order Number Char Type
			if(!isBlankOrNull(orderNumberCharacteristicTypeStr)){
				CharacteristicType_Id orderNumberCharacteristicType = new CharacteristicType_Id(orderNumberCharacteristicTypeStr); 
				if(notNull(orderNumberCharacteristicType.getEntity())){
					lookupVO.setOrderNumberCharacteristicType(orderNumberCharacteristicType.getEntity());
				}
			}
			
			//Borrower Name Char Type
			if(!isBlankOrNull(borrowerNameCharacteristicTypeStr)){
				CharacteristicType_Id borrowerNameCharacteristicType = new CharacteristicType_Id(borrowerNameCharacteristicTypeStr); 
				if(notNull(borrowerNameCharacteristicType.getEntity())){
					lookupVO.setBorrowerNameCharacteristicType(borrowerNameCharacteristicType.getEntity());
				}
			}
			
			//Property Address Char Type
			if(!isBlankOrNull(propertyAddressCharacteristicTypeStr)){
				CharacteristicType_Id propertyAddressCharacteristicType = new CharacteristicType_Id(propertyAddressCharacteristicTypeStr); 
				if(notNull(propertyAddressCharacteristicType.getEntity())){
					lookupVO.setPropertyAddressCharacteristicType(propertyAddressCharacteristicType.getEntity());
				}
			}
			
			//Property City Char Type
			if(!isBlankOrNull(propertyCityCharacteristicTypeStr)){
				CharacteristicType_Id propertyCityCharacteristicType = new CharacteristicType_Id(propertyCityCharacteristicTypeStr); 
				if(notNull(propertyCityCharacteristicType.getEntity())){
					lookupVO.setPropertyCityCharacteristicType(propertyCityCharacteristicType.getEntity());
				}
			}
			
			//Property State Char Type
			if(!isBlankOrNull(propertyStateCharacateristicTypeStr)){
				CharacteristicType_Id propertyStateCharacateristicType = new CharacteristicType_Id(propertyStateCharacateristicTypeStr); 
				if(notNull(propertyStateCharacateristicType.getEntity())){
					lookupVO.setPropertyStateCharacateristicType(propertyStateCharacateristicType.getEntity());
				}
			}
			
			//Property Zip Char Type
			if(!isBlankOrNull(propertyZipCharacteristicTypeStr)){
				CharacteristicType_Id propertyZipCharacteristicType = new CharacteristicType_Id(propertyZipCharacteristicTypeStr); 
				if(notNull(propertyZipCharacteristicType.getEntity())){
					lookupVO.setPropertyZipCharacteristicType(propertyZipCharacteristicType.getEntity());
				}
			}
			
			//Payment Terms Char Type
			if(!isBlankOrNull(paymentTermsCharacteristicTypeStr)){
				CharacteristicType_Id paymentTermsCharacteristicType = new CharacteristicType_Id(paymentTermsCharacteristicTypeStr); 
				if(notNull(paymentTermsCharacteristicType.getEntity())){
					lookupVO.setPaymentTermsCharacteristicType(paymentTermsCharacteristicType.getEntity());
				}
			}
			
			//Charge Calc Line Char Type
			if(!isBlankOrNull(chargeCalcLineCharacteristicTypeStr)){
				CharacteristicType_Id chargeCalcLineCharacteristicType = new CharacteristicType_Id(chargeCalcLineCharacteristicTypeStr); 
				if(notNull(chargeCalcLineCharacteristicType.getEntity())){
					lookupVO.setChargeCalcLineCharacteristicType(chargeCalcLineCharacteristicType.getEntity());
				}
			}
			
			//Tax Calc Line Char Type
			if(!isBlankOrNull(taxCalcLineCharacteristicValue)){
				lookupVO.setTaxCalcLineCharacteristicValue(taxCalcLineCharacteristicValue);
			}
			
			//Discount Calc Line Char Type
			if(!isBlankOrNull(discountCalcLineCharacteristicValue)){
				lookupVO.setDiscountCalcLineCharacteristicValue(discountCalcLineCharacteristicValue);
			}
			
		}
		
		return lookupVO;
	}
	
	/**
	 * This methid retrieves active extendable lookup document
	 * @param boName
	 * @param lookupName
	 * @return Extendable Lookup Document
	 */
	public Document fetchActiveExtendedLookupDocument(String boName, String lookupName){
		Document extLookupDocument = null;
		 BusinessObjectInfo boInfo = BusinessObjectInfoCache.getRequiredBusinessObjectInfo(boName);
		 if(!isBlankOrNull(lookupName)){
			 ExtendedLookupValueInfo extendedLookupValueInfo = boInfo.getExtendedLookupValue(lookupName);
			 if(notNull(extendedLookupValueInfo)&&extendedLookupValueInfo.getLookupUsage().isActive()){
				 extLookupDocument =  extendedLookupValueInfo.getXMLRepresentation();
			 }
		 }else{
			 for(ExtendedLookupValueInfo extendedLookupValueInfo: boInfo.getActiveExtendedLookupValues()){
				 extLookupDocument = extendedLookupValueInfo.getXMLRepresentation();
				 if(notNull(extLookupDocument)){
					 break;
				 }
			}
		 }
		return extLookupDocument;
	}
	
}
