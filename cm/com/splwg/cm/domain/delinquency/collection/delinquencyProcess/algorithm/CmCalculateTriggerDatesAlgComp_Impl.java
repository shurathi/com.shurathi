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
 * Calculate Trigger Dates
 * 
 * This algorithm will calculate the trigger dates when the Delinquency Process is created.
 *
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:                  Reason:
 * 2020-08-19  SAnart         CB-273.Initial Version.
 */
package com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.impl.common.DocumentHelper;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.BusinessObjectStatusCode;
import com.splwg.base.api.businessObject.COTSInstanceList;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.lookup.BusinessObjectActionLookup;
import com.splwg.base.api.lookup.BusinessObjectStatusTransitionConditionLookup;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectEnterStatusAlgorithmSpot;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatus_Id;
import com.splwg.base.domain.common.businessObject.BusinessObject_Id;
import com.splwg.base.domain.common.businessObjectStatusReason.BusinessObjectStatusReason_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.characteristicType.CharacteristicType_Id;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmDelinquencyProcessType;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessCharacteristic_DTO;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessCharacteristic_Id;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessRelatedObject;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessRelatedObject_DTO;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessRelatedObject_Id;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessRelatedObjects;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessTriggerEvent_DTO;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessTriggerEvent_Id;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.shared.common.Dom4JHelper;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;

/**
 * @author SAnarthe
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = characteristicType, name = delincRefDueDtCharType, required = true, type = entity)})
 */
public class CmCalculateTriggerDatesAlgComp_Impl extends
		CmCalculateTriggerDatesAlgComp_Gen implements
		BusinessObjectEnterStatusAlgorithmSpot 
{

	private BusinessObjectInstance boInstance=null;
    private BusinessObjectInstanceKey boInstKey;
    CmDelinquencyProcess_Id delinquencyProcessId =null;
    CmDelinquencyProcess delinquencyProcess = null;

	@Override
	public void invoke() 
	{
		
		delinquencyProcessId = new CmDelinquencyProcess_Id(boInstKey.getString("delinquencyProcessId"));
		delinquencyProcess = delinquencyProcessId.getEntity();
		
		List<CmDelinquencyProcessStatusConfigListObject> statusList = retrieveEventConfigurationStatusList(delinquencyProcess.getCmDelinquencyProcessType());
		
		this.boInstance = BusinessObjectDispatcher.read(this.boInstKey, true);
		
		String days;
		int numDays;
		Date currDate;
		Date triggerDate;
		BusinessObjectStatus_Id boStatusId;
		CmDelinquencyProcessTriggerEvent_Id delincProcTriEvtId;
		CmDelinquencyProcessTriggerEvent_DTO delincProcTriEvtDto;
		
		for(CmDelinquencyProcessStatusConfigListObject obj : statusList)
		{
			
			days=obj.getDays();
			numDays=Integer.parseInt(days);
			currDate=getProcessDateTime().getDate();
			triggerDate = currDate.addDays(numDays);
			
			boStatusId = new BusinessObjectStatus_Id(boInstance.getBusinessObject(), obj.getBusinessObjectStatus())	;	
			
			delincProcTriEvtId = new CmDelinquencyProcessTriggerEvent_Id(boStatusId.getEntity(),delinquencyProcessId.getEntity(),obj.getSequence());
			
			delincProcTriEvtDto = new CmDelinquencyProcessTriggerEvent_DTO();
			delincProcTriEvtDto.setId(delincProcTriEvtId);
			delincProcTriEvtDto.setTriggerDate(triggerDate);
			delincProcTriEvtDto.newEntity();
			
		}
		createDelinquencyProcessCharacteristics(delinquencyProcessId, getDelincRefDueDtCharType(), getProcessDateTime().getDate().toString());
	}
		
	/**
	 	* This method stamps characteristics on delinquency process
	 	* @param delinquencyProcessId
	 	* @param charType
	 	* @param charValue
	*/
	public void createDelinquencyProcessCharacteristics(CmDelinquencyProcess_Id delinquencyProcessId, CharacteristicType charType, String charValue)
	{
   
		CmDelinquencyProcessCharacteristic_Id delinquencyProcCharId = new CmDelinquencyProcessCharacteristic_Id(delinquencyProcessId, charType.getId(), getProcessDateTime().getDate());
   
		CmDelinquencyProcessCharacteristic_DTO delinquencyProcCharDto = new CmDelinquencyProcessCharacteristic_DTO();
		delinquencyProcCharDto.setId(delinquencyProcCharId);
		if(charType.getCharacteristicType().isAdhocValue())
		{
			delinquencyProcCharDto.setAdhocCharacteristicValue(charValue);
		}
		else if(charType.getCharacteristicType().isPredefinedValue())
		{
			delinquencyProcCharDto.setCharacteristicValue(charValue);
		}
		else if(charType.getCharacteristicType().isForeignKeyValue())
		{
			delinquencyProcCharDto.setCharacteristicValueForeignKey1(charValue);
		}
		delinquencyProcCharDto.setSearchCharacteristicValue(charValue);
		delinquencyProcCharDto.newEntity();
	}

	/**
 	* This method retrieves data from delinquency process Type Data Area
 	* @param CmDelinquencyProcessType
 	*/
	private List<CmDelinquencyProcessStatusConfigListObject> retrieveEventConfigurationStatusList
	(CmDelinquencyProcessType cmDelinquencyProcessType) 
	{
		List<CmDelinquencyProcessStatusConfigListObject> eventList = new ArrayList<CmDelinquencyProcessStatusConfigListObject>();

		CmDelinquencyProcessStatusConfigListObject configDataObj = null;
		
		String seq;
		String boStatus;
		String Days;
		
		// Get the BO Data Area of the Delinquency Process Type from method
		// input
		String boDataArea = cmDelinquencyProcessType.getBusinessObjectDataArea();
		if (notNull(boDataArea)) 
		{
			// Get the eventList from the XML of the BO Data Area where the node
			// name is "delinquencyStatusList"
			Document document = null;
			try 
			{
				document = Dom4JHelper.parseText("<root>".concat(boDataArea).concat("</root>"));
			} 
			catch (DocumentException e) 
			{
				 e.printStackTrace();
				//addError(MessageRepository.errorWhileProcessingAlgorithm(getClass().getName(), e.getMessage()));
			}
			List<Node> delinquencyStatusList = null;
			if (notNull(document) && notNull(document.getRootElement()))
			{
				delinquencyStatusList = document.getRootElement().selectNodes("delinquencyProcessStatusConfiguration");
			}
			for (Node delinquencyStatusListNode : delinquencyStatusList) 
			{
				seq = delinquencyStatusListNode.selectSingleNode("sequence").getText();
				boStatus=delinquencyStatusListNode.selectSingleNode("businessObjectStatus").getText();
				Days=delinquencyStatusListNode.selectSingleNode("days").getText();
				
				configDataObj = new CmDelinquencyProcessStatusConfigListObject();
				configDataObj.setSequence(new BigInteger(seq));
				configDataObj.setBusinessObjectStatus(boStatus);
				configDataObj.setDays(Days);
				
				eventList.add(configDataObj);
			}
		}
		return eventList;
	}

	@Override
	public boolean getForcePostProcessing() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public BusinessObjectStatusCode getNextStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BusinessObjectStatusTransitionConditionLookup getNextStatusCondition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BusinessObjectStatusReason_Id getStatusChangeReasonId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getUseDefaultNextStatus() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setAction(BusinessObjectActionLookup arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBusinessObject(BusinessObject arg0) {
		
	}

	@Override
	public void setBusinessObjectKey(BusinessObjectInstanceKey arg0) {
		boInstKey=arg0;
		
	}

}
