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
 * Statement Extract Extendable Lookup Cache
 *
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:         Reason:
 * 2020-07-07 	DDejes	 	CB-157 Initial Version															 
 ***********************************************************************
 */
package com.splwg.cm.domain.billing.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import com.splwg.base.api.ApplicationCache;
import com.splwg.base.api.GenericBusinessComponent;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.support.context.ContextHolder;
import com.splwg.cm.domain.customer.utility.CmCustInterfaceUtilityBussComp;

public final class CmStatementExtractExtLookupCache extends
GenericBusinessComponent implements ApplicationCache {

	private final static CmStatementExtractExtLookupCache INSTANCE = new CmStatementExtractExtLookupCache();

	private Map<String,CmStatementExtractExtLookupVO> stmtExtractCustClassExtLookupMap = new ConcurrentHashMap<String,CmStatementExtractExtLookupVO>();



	@Override
	public String getName() {
		return "CmStatementExtractExtLookupCache";
	}

	@Override
	public void flush() {
		stmtExtractCustClassExtLookupMap.clear();
	}



	private CmStatementExtractExtLookupCache(){
		ContextHolder.getContext().registerCache(this);	
	}




	public static CmStatementExtractExtLookupVO getStatementExtractConfigLookupByCustClass(BusinessObject extendedLookupBO,String customerClass){
		CmStatementExtractExtLookupVO extLookupVO = null;

		if(INSTANCE.stmtExtractCustClassExtLookupMap.containsKey(customerClass)){
			extLookupVO = INSTANCE.stmtExtractCustClassExtLookupMap.get(customerClass);
		}else{
			extLookupVO = INSTANCE.fetchExtLookupDataByCustClass(extendedLookupBO, customerClass);
			INSTANCE.stmtExtractCustClassExtLookupMap.put(customerClass,extLookupVO);
			}
		return extLookupVO;
	}



	private  synchronized CmStatementExtractExtLookupVO fetchExtLookupDataByCustClass(BusinessObject extendedLookupBO,String custClass){

		CmStatementExtractExtLookupVO lookupVO = new CmStatementExtractExtLookupVO();
		CmCustInterfaceUtilityBussComp bussComp = CmCustInterfaceUtilityBussComp.Factory.newInstance();
		Document extLookupDocument = bussComp.fetchActiveExtendedLookupDocument(extendedLookupBO.getId().getTrimmedValue(), custClass);
		if(notNull(extLookupDocument)){
			Element rootElement=extLookupDocument.getRootElement();
			Element remitToNode = rootElement.element("remitTo");
			@SuppressWarnings("unchecked")
			List<Node> tempList=extLookupDocument.selectNodes("template");
			if(notNull(tempList)&&!tempList.isEmpty()){
				List<String> templtList = new ArrayList<String>();
				for(Node templateList : tempList){
					templtList.add((!isNull(templateList.selectSingleNode("templateCode")) ? templateList.selectSingleNode("templateCode").getText(): " "));
				}
				lookupVO.setTemplate(templtList);
			}
			lookupVO.setLogo((!isNull(extLookupDocument.selectSingleNode("logo")) ? extLookupDocument.selectSingleNode("logo").getText() :" " ));	
			lookupVO.setAddressLine1((!isNull(remitToNode.selectSingleNode("addressLine1"))?remitToNode.selectSingleNode("addressLine1").getText():" "));
			lookupVO.setAddressLine2((!isNull(remitToNode.selectSingleNode("addressLine2"))?remitToNode.selectSingleNode("addressLine2").getText():" "));
			lookupVO.setAddressLine3((!isNull(remitToNode.selectSingleNode("addressLine3"))?remitToNode.selectSingleNode("addressLine3").getText():" "));
			lookupVO.setAddressLine4((!isNull(remitToNode.selectSingleNode("addressLine4"))?remitToNode.selectSingleNode("addressLine4").getText():" "));
			lookupVO.setCity((!isNull(remitToNode.selectSingleNode("city"))?remitToNode.selectSingleNode("city").getText():" "));
			lookupVO.setState((!isNull(remitToNode.selectSingleNode("state"))?remitToNode.selectSingleNode("state").getText():" "));
			lookupVO.setPhoneNbr((!isNull(remitToNode.selectSingleNode("phoneNbr"))?remitToNode.selectSingleNode("phoneNbr").getText():" "));
			lookupVO.setCountry((!isNull(remitToNode.selectSingleNode("country"))?remitToNode.selectSingleNode("country").getText():" "));
			lookupVO.setCounty((!isNull(remitToNode.selectSingleNode("county"))?remitToNode.selectSingleNode("county").getText():" "));
			lookupVO.setEmailId((!isNull(remitToNode.selectSingleNode("emailId"))?remitToNode.selectSingleNode("emailId").getText():" "));
			lookupVO.setBusinessUnit((!isNull(remitToNode.selectSingleNode("businessUnit"))?remitToNode.selectSingleNode("businessUnit").getText():" "));
			lookupVO.setName((!isNull(remitToNode.selectSingleNode("name"))?remitToNode.selectSingleNode("name").getText():" "));
			lookupVO.setPostal((!isNull(remitToNode.selectSingleNode("postal"))?remitToNode.selectSingleNode("postal").getText():" "));
		}
		return lookupVO;

	}
}
