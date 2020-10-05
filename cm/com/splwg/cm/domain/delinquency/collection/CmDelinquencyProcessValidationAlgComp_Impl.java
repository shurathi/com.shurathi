/*
 **************************************************************************
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * This algorithm validates the delinquency Process.
 *                                                             
 **************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    	    Reason:                                     
 * YYYY-MM-DD  	IN       	Reason text.                                
 *           
 * 2020-05-06   VINODW		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework
 *
 **************************************************************************
 */
package com.splwg.cm.domain.delinquency.collection;

import java.util.Iterator;

import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.datatypes.EntityId;
import com.splwg.base.api.lookup.BusinessObjectActionLookup;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.ValidateBusinessObjectAlgorithmSpot;
import com.splwg.base.domain.common.maintenanceObject.MaintenanceObject;
import com.splwg.cm.api.lookup.CmDelinquencyLevelLookup;
import com.splwg.cm.api.lookup.CmDelinquencyRelatedObjTypeLookup;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmDelinquencyProcessType;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;

/**
 * @author VINODW
 *
@AlgorithmComponent ()
 */
public class CmDelinquencyProcessValidationAlgComp_Impl extends CmDelinquencyProcessValidationAlgComp_Gen implements ValidateBusinessObjectAlgorithmSpot {

	// hard parameters
	@SuppressWarnings("unused")
	private BusinessObjectInstance boInst;
	private BusinessObjectInstanceKey boInstKey;
	@SuppressWarnings("unused")
	private BusinessObjectActionLookup actionLookup;
	private static final String personMaintenance = "PERSON";
	private static final String accountMaintenance = "ACCOUNT";
	@Override
	public void setBusinessObject(BusinessObject arg0) {

	}

	@Override
	public void setBusinessObjectKey(BusinessObjectInstanceKey arg0) {
		boInstKey = arg0;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void setEntityId(EntityId arg0) {

	}

	@Override
	public void setMaintenanceObject(MaintenanceObject arg0) {

	}

	@Override
	public void setNewBusinessObject(BusinessObjectInstance arg0) {
		boInst = arg0;
	}

	@Override
	public void setOriginalBusinessObject(BusinessObjectInstance arg0) {

	}

	@Override
	public void setAction(BusinessObjectActionLookup businessObjectActionlookup) {
		actionLookup = businessObjectActionlookup;
	}

	@Override
	public void invoke() {
		int personCount = 0;
		int accountCount = 0;
		CmDelinquencyProcessRelatedObject delinquencyRelatedObject = null;
		CmDelinquencyProcess delProcess = new CmDelinquencyProcess_Id(boInstKey.getString("delinquencyProcessId")).getEntity();
		CmDelinquencyProcessType delinquencyProcessType = delProcess.getCmDelinquencyProcessType().getId().getEntity();
		CmDelinquencyLevelLookup delinquencyLevel = delinquencyProcessType.getCmDelinquencyLevel();
		CmDelinquencyProcessRelatedObjects relatedObjects = delProcess.getRelatedObjects();	
		Iterator<CmDelinquencyProcessRelatedObject> relatedObjectIter = relatedObjects.iterator();		
		
		while (relatedObjectIter.hasNext()) {
			delinquencyRelatedObject = relatedObjectIter.next();
			if (delinquencyLevel.equals(CmDelinquencyLevelLookup.constants.CM_CUSTOMER))
			{
				// There should be exactly 1 record in CM_DELIN_PROC_REL_OBJ
				// where CM_DEL_REL_OBJ_TYPE_FLG = CMDL and MAINT_OBJ_CD = PERSON
				// delinquencyRelatedObject = relatedObjectIter.next();
				if (delinquencyRelatedObject.fetchIdCmDelinquencyRelatedObjTypeFlg().equals(CmDelinquencyRelatedObjTypeLookup.constants.CM_DELINQUENCY_LEVEL) && delinquencyRelatedObject.fetchIdMaintenanceObject().getId().getTrimmedValue().equalsIgnoreCase(personMaintenance))
		 		personCount++;				
			}
			if (delinquencyLevel.equals(CmDelinquencyLevelLookup.constants.CM_ACCOUNT))
			{				
				// There should be exactly 1 record in CM_DELIN_PROC_REL_OBJ
				// where CM_DEL_REL_OBJ_TYPE_FLG = CMDL and MAINT_OBJ_CD = ACCOUNT			
				// delinquencyRelatedObject = relatedObjectIter.next();
				if (delinquencyRelatedObject.fetchIdCmDelinquencyRelatedObjTypeFlg().equals(CmDelinquencyRelatedObjTypeLookup.constants.CM_DELINQUENCY_LEVEL) && delinquencyRelatedObject.fetchIdMaintenanceObject().getId().getTrimmedValue().equalsIgnoreCase(accountMaintenance))
					accountCount++;				
			}

			// Check Related Objects records in CM_DELIN_PROC_REL_OBJ where
			// CM_DEL_REL_OBJ_TYPE_FLG = CMCO should have MAINT_OBJ_CD equal
			// to the Delinquency Process Types CM_COLL_ON_OBJ_TYPE_FLG
			// delinquencyRelatedObject = relatedObjectIter.next();
			// if (delinquencyRelatedObject.fetchIdCmDelinquencyRelatedObjTypeFlg().equals(CmDelinquencyRelatedObjTypeLookup.constants.CM_COLLECTING_ON))
			if (notNull(delinquencyProcessType.getCmCollectingOnObjectTypeId()) && delinquencyRelatedObject.fetchIdCmDelinquencyRelatedObjTypeFlg().equals(CmDelinquencyRelatedObjTypeLookup.constants.CM_COLLECTING_ON))
			{
				if (!delinquencyRelatedObject.fetchIdMaintenanceObject().equals(delinquencyProcessType.fetchCmCollectingOnObjectType()))
				{
					addError(MessageRepository.maintenanceObjectMismatch(delinquencyRelatedObject.fetchIdMaintenanceObject().fetchLanguageDescription(), delinquencyProcessType.fetchCmCollectingOnObjectType().fetchLanguageDescription()));
				}
			}
			
		}
		
		if (delinquencyLevel.equals(CmDelinquencyLevelLookup.constants.CM_CUSTOMER) && personCount != 1)
		{
			addError(MessageRepository.zeroOrMultipleRecordsInRelatedObjTable(CmDelinquencyLevelLookup.constants.CM_CUSTOMER.getLookupValue().fetchLanguageDescription(), personMaintenance));
		}
		if (delinquencyLevel.equals(CmDelinquencyLevelLookup.constants.CM_ACCOUNT) && accountCount != 1)
		{
			addError(MessageRepository.zeroOrMultipleRecordsInRelatedObjTable(CmDelinquencyLevelLookup.constants.CM_ACCOUNT.getLookupValue().fetchLanguageDescription(), accountMaintenance));
		}
		
		CmDelinquencyProcessTriggerEvents evts = delProcess.getTriggerEvents();
		CmDelinquencyProcessTriggerEvent event = null;
		Iterator<CmDelinquencyProcessTriggerEvent> eventIterator = evts.iterator();
		while (eventIterator.hasNext()) {
			// Check Trigger Events, BUS_OBJ_CD on CM_DELIN_PROC_TRIG_EVT should
			// match BUS_OBJ_CD on CM_DELIN_PROC
			event = eventIterator.next();
			if (!event.fetchIdBusinessObjectStatus().fetchIdBusinessObject().equals(delProcess.getBusinessObject()))
			{
				addError(MessageRepository.businessObjectMismatch(event.fetchIdBusinessObjectStatus().fetchIdBusinessObject().fetchLanguageDescription(), delProcess.getStatus()));
			}
		}

	}

}

