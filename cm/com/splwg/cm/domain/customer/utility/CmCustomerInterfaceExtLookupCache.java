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
 * Customer Interface Configuration Lookup Cache
 *
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:         Reason:
 * 2019-11-30   V        Initial Version. 
 * 2020-05-20	DDejes	 CB-75. Added should receive 
 * 						 notification.
 * 2020-05-28 	JFerna	 CB-70. Remove Bill Route Type 
 *                       defaulting during Customer Interface	
 * 2020-07-01	DDejes	 CB-132. Added Default Char Type
 * 2020-07-31	KGhuge	 CB-54. Capture Statement Construct during Customer Interface 
***********************************************************************
 */
package com.splwg.cm.domain.customer.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Document;
import org.dom4j.Element;

import com.splwg.base.api.ApplicationCache;
import com.splwg.base.api.GenericBusinessComponent;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.characteristicType.CharacteristicType_Id;
import com.splwg.base.domain.common.lookup.LookupField_Id;
import com.splwg.base.domain.common.lookup.LookupValue;
import com.splwg.base.domain.common.lookup.LookupValue_Id;
import com.splwg.base.support.context.ContextHolder;
import com.splwg.ccb.api.lookup.AutoPayMethodLookup;
import com.splwg.ccb.api.lookup.AutoPayTypeFlgLookup;
import com.splwg.ccb.api.lookup.BillFormatLookup;
import com.splwg.ccb.api.lookup.BillingAddressSourceLookup;
import com.splwg.ccb.api.lookup.NameTypeLookup;
import com.splwg.ccb.api.lookup.PersonOrBusinessLookup;
import com.splwg.ccb.api.lookup.StatementLinkTypeLookup;
import com.splwg.ccb.api.lookup.StatementFormatLookup;


/**
 * @author vguddeti
 *
 */
public final class CmCustomerInterfaceExtLookupCache extends
		GenericBusinessComponent implements ApplicationCache {

	private final static CmCustomerInterfaceExtLookupCache INSTANCE = new CmCustomerInterfaceExtLookupCache();
	
	private Map<String,CmCustInterfaceExtLookupVO> custIntfDivExtLookupMap = new ConcurrentHashMap<String,CmCustInterfaceExtLookupVO>();
	private Map<String,CmCustInterfaceExtLookupVO> custIntfCustClassExtLookupMap = new ConcurrentHashMap<String,CmCustInterfaceExtLookupVO>();
	
	
	
	@Override
	public String getName() {
		return "CmPricingCache";
	}

	@Override
	public void flush() {
		custIntfDivExtLookupMap.clear();
		custIntfCustClassExtLookupMap.clear();
	}
	
	
	
	private CmCustomerInterfaceExtLookupCache(){
		ContextHolder.getContext().registerCache(this);	
	}
	
	
	public static CmCustInterfaceExtLookupVO getCustomerInterfaceConfigLookupByDiv(BusinessObject extendedLookupBO,String division){
		CmCustInterfaceExtLookupVO extLookupVO = null;
		
		if(INSTANCE.custIntfDivExtLookupMap.containsKey(division)){
			extLookupVO = INSTANCE.custIntfDivExtLookupMap.get(division);
		}else{
			extLookupVO = INSTANCE.fetchExtLookupDataByDiv(extendedLookupBO, division);
			INSTANCE.custIntfDivExtLookupMap.put(division,extLookupVO);
		}
		
		return extLookupVO;
	}
	
	
	public static CmCustInterfaceExtLookupVO getCustomerInterfaceConfigLookupByCustClass(BusinessObject extendedLookupBO,String customerClass){
		CmCustInterfaceExtLookupVO extLookupVO = null;
		
		if(INSTANCE.custIntfCustClassExtLookupMap.containsKey(customerClass)){
			extLookupVO = INSTANCE.custIntfCustClassExtLookupMap.get(customerClass);
		}else{
			extLookupVO = INSTANCE.fetchExtLookupDataByCustClass(extendedLookupBO, customerClass);
			INSTANCE.custIntfCustClassExtLookupMap.put(customerClass,extLookupVO);
		}
		return extLookupVO;
	}
	
	
	
	@SuppressWarnings("unchecked")
	private  synchronized CmCustInterfaceExtLookupVO fetchExtLookupDataByDiv(BusinessObject extendedLookupBO,String division){
		
		CmCustInterfaceExtLookupVO lookupVO = null;
		CmCustInterfaceUtilityBussComp bussComp = CmCustInterfaceUtilityBussComp.Factory.newInstance();
		Document extLookupDocument = bussComp.fetchActiveExtendedLookupDocument(extendedLookupBO.getId().getTrimmedValue(), division);
		if(notNull(extLookupDocument)){
			//read and all config data
			lookupVO = new CmCustInterfaceExtLookupVO();
			Element rootElement = extLookupDocument.getRootElement();
			Element customerInfoElement = rootElement.element(CmCustomerInterfaceConstants.EXT_CUSTOMERINFO_ELE);
			
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_PERSONTYPE_ELE))){
				String personTypeString = customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_PERSONTYPE_ELE);
				LookupValue_Id perTypeLookupId = new LookupValue_Id(new LookupField_Id("PER_OR_BUS_FLG"),personTypeString);
				LookupValue personTypeLookup = perTypeLookupId.getEntity();
				if(notNull(personTypeLookup)){
					lookupVO.setPersonTypeLookup ((PersonOrBusinessLookup)personTypeLookup.asLookup());
				}
			}
			/*if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_PRIMARYPERSONIDTYPE_ELE))){
				lookupVO.setIdType (customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_PRIMARYPERSONIDTYPE_ELE));
			}
			*/
			lookupVO.setAccessGroup ( customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_ACCESSGROUP_ELE));
			
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_PRIMARYNAMETYPE_ELE))){
				String nameTypeString = customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_PRIMARYNAMETYPE_ELE);
				LookupValue_Id nameTypeLookupId = new LookupValue_Id(new LookupField_Id("NAME_TYPE_FLG"),nameTypeString);
				LookupValue nameTypLookup = nameTypeLookupId.getEntity();
				if(notNull(nameTypLookup)){
					lookupVO.setNameTypeLookup ((NameTypeLookup)nameTypLookup.asLookup());
				}
			}
			
			/*if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_PHONETYPE_ELE))){
				lookupVO.setPhoneType ( customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_PHONETYPE_ELE));
			}*/
			
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_DIVISION_ELE))){
				lookupVO.setDivision ( customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_DIVISION_ELE));
			}
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_INVOICECURRENCY_ELE))){
				lookupVO.setCurrency ( customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_INVOICECURRENCY_ELE));
			}
			
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_RECEIVECOPYOFBILL_ELE))){
				lookupVO.setShouldReceiveCopyOfBill ( customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_RECEIVECOPYOFBILL_ELE));
			}

			//Start Add CB-75
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_RECEIVENOTIF_ELE))){
				lookupVO.setShouldReceiveNotification ( customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_RECEIVENOTIF_ELE));
			}
			//End Add CB-75
			
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_ACCOUNTRELATIONSHIPTYPE_ELE))){
				lookupVO.setAccountRelationshipType(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_ACCOUNTRELATIONSHIPTYPE_ELE));
			}

			
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_ADDRESSSOURCE_ELE))){
				String addrSourceString = customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_ADDRESSSOURCE_ELE);
				LookupValue_Id addrSourceLookupId = new LookupValue_Id(new LookupField_Id("BILL_ADDR_SRCE_FLG"),addrSourceString);
				LookupValue addrSourceLookup = addrSourceLookupId.getEntity();
				if(notNull(addrSourceLookup)){
					lookupVO.setBillAddressSource ((BillingAddressSourceLookup)addrSourceLookup.asLookup());
				}
			}
			
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_BILLFORMAT_ELE))){
				String billFormatString = customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_BILLFORMAT_ELE);
				LookupValue_Id billFormatLookupId = new LookupValue_Id(new LookupField_Id("BILL_FORMAT_FLG"),billFormatString);
				LookupValue billFormatLookup = billFormatLookupId.getEntity();
				if(notNull(billFormatLookup)){
					lookupVO.setBillFormat ( (BillFormatLookup)billFormatLookup.asLookup());
				}
			}
			
			//CB-70 - Start Delete			 
			/*if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_BILLROUTETYPE_ELE))){
				lookupVO.setBillRouteType ( customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_BILLROUTETYPE_ELE));
			}*/
			//CB-70 - End Delete		   
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_PRIMARYACCTIDTYPE_ELE))){
				lookupVO.setAccountIdType ( customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_PRIMARYACCTIDTYPE_ELE));
			}
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_BILLCYCLE_ELE))){
				lookupVO.setBillCycle ( customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_BILLCYCLE_ELE));
			}
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_COLLECTIONCLASS_ELE))){
				lookupVO.setCollectionClass( customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_COLLECTIONCLASS_ELE));
			}
			
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_AUTOPAYTYPE_ELE))){
				String autopayTypeString = customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_AUTOPAYTYPE_ELE);
				LookupValue_Id autopayTypeLookupId = new LookupValue_Id(new LookupField_Id("APAY_TYPE_FLG"),autopayTypeString);
				LookupValue autopayTypeLookup = autopayTypeLookupId.getEntity();
				if(notNull(autopayTypeLookup)){
					lookupVO.setAutopayType((AutoPayTypeFlgLookup)autopayTypeLookup.asLookup());
				}else{
					lookupVO.setAutopayType(AutoPayTypeFlgLookup.constants.REGULAR_AUTO_PAY);
				}
			}else{
				lookupVO.setAutopayType(AutoPayTypeFlgLookup.constants.REGULAR_AUTO_PAY);
			}
			
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_AUTOPAYMETHOD_ELE))){
				String autopayMethodString = customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_AUTOPAYMETHOD_ELE);
				LookupValue_Id autopayMethodLookupId = new LookupValue_Id(new LookupField_Id("APAY_METHOD_FLG"),autopayMethodString);
				LookupValue autopayMethod = autopayMethodLookupId.getEntity();
				if(notNull(autopayMethod)){
					lookupVO.setAutopayMethod((AutoPayMethodLookup)autopayMethod.asLookup());
				}else{
					lookupVO.setAutopayMethod(AutoPayMethodLookup.constants.DIRECT_DEBIT);
				}
			}else{
				lookupVO.setAutopayMethod(AutoPayMethodLookup.constants.DIRECT_DEBIT);
			}
			
			
			List<Element> contractTypes = customerInfoElement.elements(CmCustomerInterfaceConstants.EXT_CONTRACTTYPES_ELE);
			if(notNull(contractTypes)&&!contractTypes.isEmpty()){
				List<String> saTypeList = new ArrayList<String>();
				for(Element contractType : contractTypes){
					saTypeList.add(contractType.elementText(CmCustomerInterfaceConstants.EXT_CONTRACTTYPE_ELE));
				}
				lookupVO.setSaTypeList(saTypeList);
			}
			
			//Start Add - CB-132
			List<Element> defaultChars = customerInfoElement.elements(CmCustomerInterfaceConstants.DEFAULT_CHARS_ELE);
			List<CmCharData> charDataList = new ArrayList<CmCharData>();
			Element defaultCharGroup = null;
			CharacteristicType charType = null;
			LookupValue charEntity = null;
			String charTypeString;
			String charValString;
			String charEntityString;
			if(notNull(defaultChars)&&!defaultChars.isEmpty()){
				for(Element defaultChar : defaultChars){
					defaultCharGroup = defaultChar.element(CmCustomerInterfaceConstants.DEFAULT_CHAR_ELE);	
					if(notNull(defaultCharGroup) ){
						charTypeString = defaultCharGroup.elementText(CmCustomerInterfaceConstants.CHAR_TYPE);
						charValString = defaultCharGroup.elementText(CmCustomerInterfaceConstants.CHAR_VAL);
						charEntityString = defaultCharGroup.elementText(CmCustomerInterfaceConstants.CHAR_ENT);

						if(!isBlankOrNull(charTypeString) && !isBlankOrNull(charValString)){
							charType = new CharacteristicType_Id(charTypeString).getEntity();
							charEntity = new LookupValue_Id(new LookupField_Id("CHAR_ENTITY_FLG"), charEntityString).getEntity();
							CmCharData cmCharData = new CmCharData();
							cmCharData.setCharType(charType);
							cmCharData.setCharEntity(charEntity.asLookup());
							cmCharData.setCharValue(charValString);
							charDataList.add(cmCharData);
						}


					}
				}
			}
			lookupVO.setCharDefault(charDataList);
			//End Add - CB-132
			
			//Start Add - CB-54			 
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.NBROFCOPIES_ELE))){
				lookupVO.setNbrOfCopies(customerInfoElement.numberValueOf(CmCustomerInterfaceConstants.NBROFCOPIES_ELE));
			}
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.STATEMENT_DESCR_ELE))){
				lookupVO.setStatementDescription(customerInfoElement.elementText(CmCustomerInterfaceConstants.STATEMENT_DESCR_ELE));
			}
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.STATEMENT_FORMAT_ELE))){
				String statementFormat = customerInfoElement.elementText(CmCustomerInterfaceConstants.STATEMENT_FORMAT_ELE);
				LookupValue_Id stmtFormatLookupId = new LookupValue_Id(new LookupField_Id("STM_FORMAT_FLG"),statementFormat);
				LookupValue stmtFormatLookup = stmtFormatLookupId.getEntity();
				if(notNull(stmtFormatLookup)){
					lookupVO.setStatementFormat((StatementFormatLookup)stmtFormatLookup.asLookup());
				}
			}
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.STATEMENT_DETAIL_ELE))){
				String stmtDetailType = customerInfoElement.elementText(CmCustomerInterfaceConstants.STATEMENT_DETAIL_ELE);
				LookupValue_Id stmtDetailTypeLookupId = new LookupValue_Id(new LookupField_Id("STMT_LINK_TYP_FLG"),stmtDetailType);
				LookupValue stmtDetailTypeLookup = stmtDetailTypeLookupId.getEntity();
				if(notNull(stmtDetailTypeLookup)){
					lookupVO.setStatementDetailType((StatementLinkTypeLookup) stmtDetailTypeLookup.asLookup());
				}
			}
			
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.STATEMENT_DETAIL_DESC_ELE))){
				lookupVO.setStatementDetailDescription(customerInfoElement.elementText(CmCustomerInterfaceConstants.STATEMENT_DETAIL_DESC_ELE));
			}
			//End Add - CB-54
		}
		
		return lookupVO;
	}
	
	
	@SuppressWarnings("unchecked")
	private  synchronized CmCustInterfaceExtLookupVO fetchExtLookupDataByCustClass(BusinessObject extendedLookupBO,String division){
		
		CmCustInterfaceExtLookupVO lookupVO = new CmCustInterfaceExtLookupVO();
		CmCustInterfaceUtilityBussComp bussComp = CmCustInterfaceUtilityBussComp.Factory.newInstance();
		Document extLookupDocument = bussComp.fetchActiveExtendedLookupDocument(extendedLookupBO.getId().getTrimmedValue(), division);
		if(notNull(extLookupDocument)){
			//read and all config data
			Element rootElement = extLookupDocument.getRootElement();
			Element customerInfoElement = rootElement.element(CmCustomerInterfaceConstants.EXT_CUSTOMERINFO_ELE);
			
			
			
			lookupVO.setAccessGroup ( customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_ACCESSGROUP_ELE));
			
			
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_DIVISION_ELE))){
				lookupVO.setDivision ( customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_DIVISION_ELE));
			}
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_INVOICECURRENCY_ELE))){
				lookupVO.setCurrency ( customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_INVOICECURRENCY_ELE));
			}
			
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_RECEIVECOPYOFBILL_ELE))){
				lookupVO.setShouldReceiveCopyOfBill ( customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_RECEIVECOPYOFBILL_ELE));
			}
			//Start Add CB-75
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_RECEIVENOTIF_ELE))){
				lookupVO.setShouldReceiveNotification ( customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_RECEIVENOTIF_ELE));
			}
			//End Add CB-75

			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_ACCOUNTRELATIONSHIPTYPE_ELE))){
				lookupVO.setAccountRelationshipType(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_ACCOUNTRELATIONSHIPTYPE_ELE));
			}

			
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_ADDRESSSOURCE_ELE))){
				String addrSourceString = customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_ADDRESSSOURCE_ELE);
				LookupValue_Id addrSourceLookupId = new LookupValue_Id(new LookupField_Id("BILL_ADDR_SRCE_FLG"),addrSourceString);
				LookupValue addrSourceLookup = addrSourceLookupId.getEntity();
				if(notNull(addrSourceLookup)){
					lookupVO.setBillAddressSource ((BillingAddressSourceLookup)addrSourceLookup.asLookup());
				}
			}
			
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_BILLFORMAT_ELE))){
				String billFormatString = customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_BILLFORMAT_ELE);
				LookupValue_Id billFormatLookupId = new LookupValue_Id(new LookupField_Id("BILL_FORMAT_FLG"),billFormatString);
				LookupValue billFormatLookup = billFormatLookupId.getEntity();
				if(notNull(billFormatLookup)){
					lookupVO.setBillFormat ( (BillFormatLookup)billFormatLookup.asLookup());
				}
			}
			
			//CB-70 - Start Delete			 
			/*if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_BILLROUTETYPE_ELE))){
				lookupVO.setBillRouteType ( customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_BILLROUTETYPE_ELE));
			}*/
			//CB-70 - End Delete		   
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_PRIMARYACCTIDTYPE_ELE))){
				lookupVO.setAccountIdType ( customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_PRIMARYACCTIDTYPE_ELE));
			}
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_BILLCYCLE_ELE))){
				lookupVO.setBillCycle ( customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_BILLCYCLE_ELE));
			}
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_COLLECTIONCLASS_ELE))){
				lookupVO.setCollectionClass( customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_COLLECTIONCLASS_ELE));
			}
			
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_AUTOPAYTYPE_ELE))){
				String autopayTypeString = customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_AUTOPAYTYPE_ELE);
				LookupValue_Id autopayTypeLookupId = new LookupValue_Id(new LookupField_Id("APAY_TYPE_FLG"),autopayTypeString);
				LookupValue autopayTypeLookup = autopayTypeLookupId.getEntity();
				if(notNull(autopayTypeLookup)){
					lookupVO.setAutopayType((AutoPayTypeFlgLookup)autopayTypeLookup.asLookup());
				}else{
					lookupVO.setAutopayType(AutoPayTypeFlgLookup.constants.REGULAR_AUTO_PAY);
				}
			}else{
				lookupVO.setAutopayType(AutoPayTypeFlgLookup.constants.REGULAR_AUTO_PAY);
			}
			
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_AUTOPAYMETHOD_ELE))){
				String autopayMethodString = customerInfoElement.elementText(CmCustomerInterfaceConstants.EXT_AUTOPAYMETHOD_ELE);
				LookupValue_Id autopayMethodLookupId = new LookupValue_Id(new LookupField_Id("APAY_METHOD_FLG"),autopayMethodString);
				LookupValue autopayMethod = autopayMethodLookupId.getEntity();
				if(notNull(autopayMethod)){
					lookupVO.setAutopayMethod((AutoPayMethodLookup)autopayMethod.asLookup());
				}else{
					lookupVO.setAutopayMethod(AutoPayMethodLookup.constants.DIRECT_DEBIT);
				}
			}else{
				lookupVO.setAutopayMethod(AutoPayMethodLookup.constants.DIRECT_DEBIT);
			}
			
			
			List<Element> contractTypes = customerInfoElement.elements(CmCustomerInterfaceConstants.EXT_CONTRACTTYPES_ELE);
			if(notNull(contractTypes)&&!contractTypes.isEmpty()){
				List<String> saTypeList = new ArrayList<String>();
				for(Element contractType : contractTypes){
					saTypeList.add(contractType.elementText(CmCustomerInterfaceConstants.EXT_CONTRACTTYPE_ELE));
				}
				lookupVO.setSaTypeList(saTypeList);
			}
			
			//Start Add - CB-132
			List<Element> defaultChars = customerInfoElement.elements(CmCustomerInterfaceConstants.DEFAULT_CHARS_ELE);
			List<CmCharData> charDataList = new ArrayList<CmCharData>();

			Element defaultCharGroup = null;
			CharacteristicType charType = null;
			LookupValue charEntity = null;
			String charTypeString;
			String charValString;
			String charEntityString;
			if(notNull(defaultChars)&&!defaultChars.isEmpty()){
				for(Element defaultChar : defaultChars){
					defaultCharGroup = defaultChar.element(CmCustomerInterfaceConstants.DEFAULT_CHAR_ELE);	
					if(notNull(defaultCharGroup) ){
							charTypeString = defaultCharGroup.elementText(CmCustomerInterfaceConstants.CHAR_TYPE);
							charValString = defaultCharGroup.elementText(CmCustomerInterfaceConstants.CHAR_VAL);
							charEntityString = defaultCharGroup.elementText(CmCustomerInterfaceConstants.CHAR_ENT);

							if(!isBlankOrNull(charTypeString) && !isBlankOrNull(charValString)){
								charType = new CharacteristicType_Id(charTypeString).getEntity();
								charEntity = new LookupValue_Id(new LookupField_Id("CHAR_ENTITY_FLG"), charEntityString).getEntity();
								CmCharData cmCharData = new CmCharData();
								cmCharData.setCharType(charType);
								cmCharData.setCharEntity(charEntity.asLookup());
								cmCharData.setCharValue(charValString);
								charDataList.add(cmCharData);
							}
							
						
						}
					}
				}
			lookupVO.setCharDefault(charDataList);
			//End Add - CB-132
			
			//Start Add - CB-54			 
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.NBROFCOPIES_ELE))){
				lookupVO.setNbrOfCopies(customerInfoElement.numberValueOf(CmCustomerInterfaceConstants.NBROFCOPIES_ELE));
			}
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.STATEMENT_DESCR_ELE))){
				lookupVO.setStatementDescription(customerInfoElement.elementText(CmCustomerInterfaceConstants.STATEMENT_DESCR_ELE));
			}
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.STATEMENT_FORMAT_ELE))){
				String statementFormat = customerInfoElement.elementText(CmCustomerInterfaceConstants.STATEMENT_FORMAT_ELE);
				LookupValue_Id stmtFormatLookupId = new LookupValue_Id(new LookupField_Id("STM_FORMAT_FLG"),statementFormat);
				LookupValue stmtFormatLookup = stmtFormatLookupId.getEntity();
				if(notNull(stmtFormatLookup)){
					lookupVO.setStatementFormat((StatementFormatLookup)stmtFormatLookup.asLookup());
				}
			}
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.STATEMENT_DETAIL_ELE))){
				String stmtDetailType = customerInfoElement.elementText(CmCustomerInterfaceConstants.STATEMENT_DETAIL_ELE);
				LookupValue_Id stmtDetailTypeLookupId = new LookupValue_Id(new LookupField_Id("STMT_LINK_TYP_FLG"),stmtDetailType);
				LookupValue stmtDetailTypeLookup = stmtDetailTypeLookupId.getEntity();
				if(notNull(stmtDetailTypeLookup)){
					lookupVO.setStatementDetailType((StatementLinkTypeLookup) stmtDetailTypeLookup.asLookup());
				}
			}
			
			if(!isBlankOrNull(customerInfoElement.elementText(CmCustomerInterfaceConstants.STATEMENT_DETAIL_DESC_ELE))){
				lookupVO.setStatementDetailDescription(customerInfoElement.elementText(CmCustomerInterfaceConstants.STATEMENT_DETAIL_DESC_ELE));
			}
			//End Add - CB-54
		}
		
		return lookupVO;
	}
	
}
