/*
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Determine Delinquency Process Collection Class Algorithm
 *                                                             
 *******************************************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:        by:         Reason:                                     
 * YYYY-MM-DD   IN          Reason text.                                
 *           
 * 2020-05-22  VINODW   	Initial version : ANTHM-340 Delinquency Framework.
 * *******************************************************************************************************
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
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessCharacteristic;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessCharacteristic_DTO;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessCharacteristic_Id;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.cm.domain.delinquency.common.customBusinessEntity.CmPersonCollection_Id;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;

/**
 * @author VINODW
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = characteristicType, name = collectionClassCharacteristicType, required = true, type = entity)})
 */
public class CmDelinquencyProcessDetermineCollectionClassAlgComp_Impl
		extends CmDelinquencyProcessDetermineCollectionClassAlgComp_Gen
		implements BusinessObjectEnterStatusAlgorithmSpot {

	private BusinessObjectInstanceKey businessObjectInstKey = null;
	private BusinessObjectInstance boInstance = null;
	private  final String BLANK_STRING_VALUE = "";
	
	@Override
	public boolean getForcePostProcessing() {
		return false;
	}

	@Override
	public BusinessObjectStatusCode getNextStatus() {
		return null;
	}

	@Override
	public BusinessObjectStatusTransitionConditionLookup getNextStatusCondition() {
		return null;
	}

	@Override
	public BusinessObjectStatusReason_Id getStatusChangeReasonId() {
		return null;
	}

	@Override
	public boolean getUseDefaultNextStatus() {
		return false;
	}

	@Override
	public void setAction(BusinessObjectActionLookup arg0) {
		
	}

	@Override
	public void setBusinessObject(BusinessObject arg0) {
		
	}

	@Override
	public void setBusinessObjectKey(BusinessObjectInstanceKey arg0) {
		this.businessObjectInstKey = arg0;
	}
	
	@Override
	public void invoke() {
		
		// Initialize Person Id
		String personId = BLANK_STRING_VALUE;
		
		// Initialize collection class value
		CollectionClass_Id collClassId = null;
		
		// Fetch Delinquency Process Id
		CmDelinquencyProcess_Id delinquencyProcessId = new CmDelinquencyProcess_Id(businessObjectInstKey.getString("delinquencyProcessId"));
		this.boInstance = BusinessObjectDispatcher.read(this.businessObjectInstKey, true);

		// Fetch Person Id from Delinquency Process
		if (notNull(this.boInstance.getElement().selectSingleNode("personId")))
		    personId = this.boInstance.getElement().selectSingleNode("personId").getText();
		
		// if person id is not present, issue an error
		if(isBlankOrNull(personId)){
			addError(MessageRepository.invalidGroupPerson(delinquencyProcessId.getIdValue()));
		}
		
		// Get Collection Class of Person
		CmPersonCollection_Id perCollId = new CmPersonCollection_Id(new Person_Id(personId));          
		if(notNull(perCollId.getEntity())){                 
			collClassId = perCollId.getEntity().getCollectionClass().getId();                        
		}
		
		if(isNull(collClassId)){
			addError(MessageRepository.invalidCollectionClass(personId));
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
}