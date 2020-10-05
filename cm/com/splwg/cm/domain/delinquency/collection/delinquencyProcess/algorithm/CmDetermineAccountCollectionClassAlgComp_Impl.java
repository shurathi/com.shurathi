/* 
 **************************************************************************
 *                Confidentiality Information:
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
 * This class is Delinquency - Determine Account Collection Class Algorithm
 * 
 **************************************************************************
 * 
 * CHANGE HISTORY:
 *
 * Date:        by:        Reason:
 * YYYY-MM-DD   IN         Reason text. 
 * 2020-04-16   AMusal     CB-281 Initial Version 3 
 **************************************************************************
 */

package com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm;

import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.BusinessObjectStatusCode;
import com.splwg.base.api.lookup.BusinessObjectActionLookup;
import com.splwg.base.api.lookup.BusinessObjectStatusTransitionConditionLookup;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectEnterStatusAlgorithmSpot;
import com.splwg.base.domain.common.businessObjectStatusReason.BusinessObjectStatusReason_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.ccb.domain.admin.collectionClass.CollectionClass_Id;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessCharacteristic;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessCharacteristic_DTO;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessCharacteristic_Id;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;

/**
 * @author RIA-IN-L-005
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = characteristicType, name = collectionClassCharacteristicType, required = true, type = entity)})
 */
public class CmDetermineAccountCollectionClassAlgComp_Impl extends
		CmDetermineAccountCollectionClassAlgComp_Gen implements
		BusinessObjectEnterStatusAlgorithmSpot {
	
	private BusinessObjectInstanceKey businessObjectInstKey = null;
	private BusinessObjectInstance boInstance = null;
	public static final String VALUE="NA";
	@Override
	public void invoke() {
		// TODO Auto-generated method stub
		// Initialize Account Id
		String accId = " ";
		
		// Initialize collection class value
		CollectionClass_Id collClassId = null;
		
		// Fetch Delinquency Process Id
		CmDelinquencyProcess_Id delinquencyProcessId = new CmDelinquencyProcess_Id(businessObjectInstKey.getString("delinquencyProcessId"));
		this.boInstance = BusinessObjectDispatcher.read(this.businessObjectInstKey, true);

		// Fetch Person Id from Delinquency Process
		if (notNull(this.boInstance.getElement().selectSingleNode("accountId")))
			accId = this.boInstance.getElement().selectSingleNode("accountId").getText();
		
		// if Account id is not present, issue an error
		if(isBlankOrNull(accId)){
			addError(MessageRepository.invalidAccountId(delinquencyProcessId.getIdValue()));
		}
		
		// Get Collection Class of Account 
		Account_Id accountId=new Account_Id(accId);
		collClassId = accountId.getEntity().getCollectionClassId();
		if(isNull(collClassId) || collClassId.getTrimmedValue().equals(VALUE)){
			addError(MessageRepository.invalidCollectionClassForAccount(accountId.getIdValue()));
		}
		
		// Create Delinquency Process Characteristic Type
		createDelinquencyProcessCharacteristic(delinquencyProcessId, getCollectionClassCharacteristicType(), collClassId.getIdValue());
	}



	/**
	 * This method creates characteristics on delinquency process
	 * @param delinquencyProcessId
	 * @param charType
	 * @param charValue
	 */
	private void createDelinquencyProcessCharacteristic(CmDelinquencyProcess_Id delinquencyProcessId, CharacteristicType charType, String charValue){
	
		CmDelinquencyProcessCharacteristic_Id deliqProcCharTypeId = new CmDelinquencyProcessCharacteristic_Id(delinquencyProcessId, charType.getId(), getProcessDateTime().getDate());
	    CmDelinquencyProcessCharacteristic_DTO charTypeDto = createDTO(CmDelinquencyProcessCharacteristic.class);
	    charTypeDto.setId(deliqProcCharTypeId);

	    if (charType.getCharacteristicType().isAdhocValue()) {
	        charTypeDto.setAdhocCharacteristicValue(charValue);
	    } else if (charType.getCharacteristicType().isPredefinedValue()) {
	        charTypeDto.setCharacteristicValue(charValue);
	    } else if (charType.getCharacteristicType().isForeignKeyValue()) {
	        charTypeDto.setCharacteristicValueForeignKey1(charValue);
	    }
	    charTypeDto.setSearchCharacteristicValue(charValue);
	    charTypeDto.newEntity();
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBusinessObjectKey(BusinessObjectInstanceKey arg0) {
		// TODO Auto-generated method stub
		this.businessObjectInstKey = arg0;
	}


}
