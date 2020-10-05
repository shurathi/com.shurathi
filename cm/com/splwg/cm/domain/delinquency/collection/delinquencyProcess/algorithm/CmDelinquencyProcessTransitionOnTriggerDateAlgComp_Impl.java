/*                                                                
 ********************************************************************************************                                                                
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Delinquency Calculation This algorithm is designed to transition trigger events to next
 * statuses
 *                                                             
 ********************************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text. 
 * 2020-05-06   MugdhaP		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework                       
 * ******************************************************************************************
 */

package com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm;

import java.util.Iterator;

import com.splwg.base.api.Query;
import com.splwg.base.api.QueryResultRow;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.BusinessObjectStatusCode;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.lookup.BusinessObjectStatusTransitionConditionLookup;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatus;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatusAutoTransitionAlgorithmSpot;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatusTransitionRule;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatusTransitionRules;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatus_Id;
import com.splwg.cm.domain.delinquency.admin.delinquencyCustomerMonitor.CmDelinquencyProcessEventConfigurationDataObject;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessTriggerEvent_DTO;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessTriggerEvent_Id;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;

/**
 * @author MugdhaP
 *
@AlgorithmComponent ()
 */
public class CmDelinquencyProcessTransitionOnTriggerDateAlgComp_Impl extends CmDelinquencyProcessTransitionOnTriggerDateAlgComp_Gen implements BusinessObjectStatusAutoTransitionAlgorithmSpot {

	private BusinessObjectInstanceKey boKey;
	private boolean useDefaultNextStatus = false;
	private boolean skipAutoTransitionStatus = false;
	private BusinessObjectStatusCode businessObjectStatusCode;
	private BusinessObjectStatusTransitionConditionLookup nextStatusCondition = null;

	@Override
	public BusinessObjectStatusCode getNextStatus() {
		return businessObjectStatusCode;
	}

	@Override
	public BusinessObjectStatusTransitionConditionLookup getNextStatusCondition() {
		return this.nextStatusCondition;
	}

	@Override
	public boolean getSkipAutoTransitioning() {
		return skipAutoTransitionStatus;
	}

	@Override
	public boolean getUseDefaultNextStatus() {
		return useDefaultNextStatus;
	}

	@Override
	public void setBusinessObject(BusinessObject businessObject) {
	}

	@Override
	public void setBusinessObjectKey(BusinessObjectInstanceKey businessObjectInstanceKey) {
		this.boKey = businessObjectInstanceKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.splwg.base.api.algorithms.AlgorithmSpot#invoke()
	 */
	@Override
	public void invoke() {

		// fetch delinquency Process Id
		CmDelinquencyProcess_Id delinquencyProcessId = new CmDelinquencyProcess_Id(boKey.getString("delinquencyProcessId"));
		BusinessObject businessObj = delinquencyProcessId.getEntity().getBusinessObject();


		// HashMap<String, CmDelinquencyProcessEventConfigurationDataObject>
		// eventMap = fetchTrigerEventsForDeliqProc(delinquencyProcessId);
		
		// fetch nvalid next statuses for this status

		// Iterator<BusinessObjectStatusTransitionRule> iterator =
		// businessObjectStatusTransitionRules.iterator();
		//
		// BusinessObjectStatusTransitionRule boStatusTranRule = null;
		//
		// make final event list, if next statuses from current BO status are
		// availbale in map
		// List<CmDelinquencyProcessEventConfigurationDataObject> finalEventList
		// = new ArrayList<CmDelinquencyProcessEventConfigurationDataObject>();
		//
		// if (notNull(eventMap) && !eventMap.isEmpty()) {
		// while (iterator.hasNext()) {
		// boStatusTranRule = iterator.next();
		//
		// if
		// (eventMap.containsKey(boStatusTranRule.fetchIdNextStatus().trim())) {
		// finalEventList.add(eventMap.get(boStatusTranRule.fetchIdNextStatus().trim()));
		// }
		// }
		// }
		//
		// if (notNull(finalEventList) && !finalEventList.isEmpty()) {
		// // iterate over final list
		// Iterator<CmDelinquencyProcessEventConfigurationDataObject>
		// finalListItr = finalEventList.iterator();
		// CmDelinquencyProcessEventConfigurationDataObject configDataObj =
		// null;
		//


		boolean isBoNextStatusFound = false;

		// while (finalListItr.hasNext()) {
		// configDataObj = finalListItr.next();

		CmDelinquencyProcessEventConfigurationDataObject configDataObj = fetchTrigerEventsForDeliqProc(delinquencyProcessId, businessObj);

		if(notNull(configDataObj)){


			Date triggerDate = configDataObj.getTriggerDate();

			if (triggerDate.isSameOrBefore(getProcessDateTime().getDate())) {

				BusinessObjectStatus_Id businessObjectStatusId = new BusinessObjectStatus_Id(businessObj, configDataObj.getBusinessObjectStatus());
				CmDelinquencyProcessTriggerEvent_Id eventId = new CmDelinquencyProcessTriggerEvent_Id(businessObjectStatusId, delinquencyProcessId,
						configDataObj.getSequence());

				CmDelinquencyProcessTriggerEvent_DTO eventDto = eventId.getEntity().getDTO();
				eventDto.setStatusDateTime(getProcessDateTime());
				eventId.getEntity().setDTO(eventDto);
				isBoNextStatusFound = true;
				businessObjectStatusCode = new BusinessObjectStatusCode(businessObj.getId(), configDataObj.getBusinessObjectStatus());
				// break;

			}
	
		}
	

		// }
		if (!isBoNextStatusFound) {
			skipAutoTransitionStatus = true;
		}
		// }

	}

	/**
	 * This method fetches trigger events for Delinquency Process Id
	 * @param delinquencyProcessId
	 * @param businessObj 
	 * @param businessObjectStatusTransitionRules 
	 * @return HashMap<String, CmDelinquencyProcessEventConfigurationDataObject> eventMap
	 */

	// private HashMap<String, CmDelinquencyProcessEventConfigurationDataObject>
	// fetchTrigerEventsForDeliqProc(CmDelinquencyProcess_Id
	// delinquencyProcessId) {
	// HashMap<String, CmDelinquencyProcessEventConfigurationDataObject>
	// eventMap = new HashMap<String,
	// CmDelinquencyProcessEventConfigurationDataObject>();

	private CmDelinquencyProcessEventConfigurationDataObject fetchTrigerEventsForDeliqProc(CmDelinquencyProcess_Id delinquencyProcessId,
			BusinessObject businessObj) {

		BusinessObjectStatusTransitionRules businessObjectStatusTransitionRules = new BusinessObjectStatus_Id(
				businessObj, delinquencyProcessId.getEntity().getStatus().trim()).getEntity().getTransitionRules();

		BusinessObjectStatusTransitionRule boStatusTranRule = null;
		Iterator<BusinessObjectStatusTransitionRule> iterator = businessObjectStatusTransitionRules.iterator();

		StringBuilder FETCH_DLPROC_TRIGG_EVNTS_PRT1 = new StringBuilder(CmDelinquencyProcessCalculateTriggerDateConstants.FETCH_DLPROC_TRIGG_EVNTS);

		while (iterator.hasNext()) {
			boStatusTranRule = iterator.next();
			FETCH_DLPROC_TRIGG_EVNTS_PRT1.append(":").append(boStatusTranRule.fetchIdNextStatus().trim());
			if (iterator.hasNext()) {
				FETCH_DLPROC_TRIGG_EVNTS_PRT1.append(", ");
			}
		}
		FETCH_DLPROC_TRIGG_EVNTS_PRT1.append(" ) ");

		Query<QueryResultRow> query = createQuery(FETCH_DLPROC_TRIGG_EVNTS_PRT1.toString(), "CmDelinquencyProcessTransitionOnTriggerDateAlgComp_Impl");
		// Query<QueryResultRow> query =
		// createQuery(CmDelinquencyProcessCalculateTriggerDateConstants.FETCH_DLPROC_TRIGG_EVNTS.toString(),
		// "CmDelinquencyProcessTransitionOnTriggerDateAlgComp_Impl");


		query.bindId("deliqProcId", delinquencyProcessId);
		query.bindEntity("businessObject", businessObj);

		iterator = businessObjectStatusTransitionRules.iterator();
		while (iterator.hasNext()) {
			boStatusTranRule = iterator.next();
			
			query.bindStringProperty(boStatusTranRule.fetchIdNextStatus().trim(), BusinessObjectStatus.properties.status, boStatusTranRule.fetchIdNextStatus().trim());
		}
	

		query.addResult("sequence", "te.id.sequence");
		query.addResult("businessObjectStatus", "te.id.businessObjectStatus");
		query.addResult("triggerDate", "te.triggerDate");

	
		query.orderBy("triggerDate", Query.ASCENDING);
		query.orderBy("sequence");
		// List<QueryResultRow> eventList = query.list();
		//
		// CmDelinquencyProcessEventConfigurationDataObject configDataObj =
		// null;
		// if (notNull(eventList) && !eventList.isEmpty()) {
		//
		// Iterator<QueryResultRow> eventIter = eventList.iterator();
		// while (eventIter.hasNext()) {
		//
		// QueryResultRow resultRow = eventIter.next();

		QueryResultRow resultRow = query.firstRow();
	

		CmDelinquencyProcessEventConfigurationDataObject configDataObj = null;


		if(notNull(resultRow)){
			configDataObj = new CmDelinquencyProcessEventConfigurationDataObject();
			
			BusinessObjectStatus boStatus = resultRow.getEntity("businessObjectStatus", BusinessObjectStatus.class);
			String status = boStatus.getId().getStatus().trim();

			configDataObj.setBusinessObjectStatus(status);
			configDataObj.setSequence(resultRow.getInteger("sequence"));
			configDataObj.setTriggerDate(resultRow.getDate("triggerDate"));
		}
		return configDataObj;
	}

}

