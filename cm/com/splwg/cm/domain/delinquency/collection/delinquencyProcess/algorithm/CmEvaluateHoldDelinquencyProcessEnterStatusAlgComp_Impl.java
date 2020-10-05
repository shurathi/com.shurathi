/*                                                                
 *******************************************************************************************************************                                                                
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * This algorithm triggers the Hold Delinquency Process Criteria algorithms on the Delinquency Process Type to 
 * determine if the Delinquency Process should be allowed to progress to the next status
 *                                                             
 *******************************************************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 * 2020-05-06   MugdhaP		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework
 * ******************************************************************************************************************
 */
package com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm;

import java.util.ArrayList;
import java.util.List;

import com.splwg.base.api.ListFilter;
import com.splwg.base.api.Query;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.BusinessObjectStatusCode;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.lookup.BusinessObjectStatusTransitionConditionLookup;
import com.splwg.base.domain.common.algorithm.Algorithm;
import com.splwg.base.domain.common.algorithm.AlgorithmComponentCache;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatus;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatusAutoTransitionAlgorithmSpot;
import com.splwg.cm.api.lookup.CmDelinquencyProcessTypeSystemEventLookup;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmDelinquencyProcessType;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmDelinquencyProcessTypeAlgorithm;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmHoldDelinquencyProcessCriteriaAlgorithmSpot;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;

/**
 * @author MugdhaP
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (lookupName = yesNoOption, name = evaluateOnTriggerDates, required = true, type = lookup)})
 */

public class CmEvaluateHoldDelinquencyProcessEnterStatusAlgComp_Impl extends CmEvaluateHoldDelinquencyProcessEnterStatusAlgComp_Gen implements BusinessObjectStatusAutoTransitionAlgorithmSpot {

	private boolean skipAutoTransition;
	private BusinessObjectInstanceKey boKey;

	public boolean getSkipAutoTransitioning() {
		return this.skipAutoTransition;
	}

	public boolean getUseDefaultNextStatus() {
		return false;
	}

	public BusinessObjectStatusCode getNextStatus() {
		return null;
	}

	public BusinessObjectStatusTransitionConditionLookup getNextStatusCondition() {
		return null;
	}

	public void setBusinessObject(BusinessObject bo) {

	}

	public void setBusinessObjectKey(BusinessObjectInstanceKey boRequest) {
		boKey = boRequest;

	}

	@Override
	public void invoke() {
		// Initialize variables
		skipAutoTransition = false;
		Date triggerDate = null;

		// Fetch Delinquency process
		CmDelinquencyProcess_Id delProcId = new CmDelinquencyProcess_Id(boKey.getString("delinquencyProcessId"));

		// If no delinquency process throw an error
		if (isNull(delProcId) && isNull(delProcId.getEntity())) {
			addError(MessageRepository.delinquencyProcessRequired());
		}
		CmDelinquencyProcess delinquencyProcess = delProcId.getEntity();

		// check whether parameter Only Evaluate on Trigger Dates is Y
		if (getEvaluateOnTriggerDates().isYes()) {

			// Retrieve the valid next statuses for the BO�s current status
			List<String> validNextStatusList = retValidNextStatusList(delinquencyProcess);

			// If validNextStatusList is empty end processing
			if (isNull(validNextStatusList) || (validNextStatusList.isEmpty())) {
				return;
			}
			// Retrieve the Delinquency Process� trigger events first rows
			// trigger date
			triggerDate = retDelProcTriggerDate(validNextStatusList);

			// If trigger date is on or after process date
			if (notNull(triggerDate) && (triggerDate.isAfter(getProcessDateTime().getDate()))) {
				return;
			}
		}


		// Retrieve Delinquency Process Type
		CmDelinquencyProcessType delinquencyProcessType = delinquencyProcess.getCmDelinquencyProcessType();

		ListFilter<CmDelinquencyProcessTypeAlgorithm> deliquencyProcessTypeAlgoListFilter = delinquencyProcessType.getAlgorithms().createFilter(" WHERE this.id.cmDelinquencyProcessTypeSystemEvent = :cmDelinquencyProcessTypeSystemEvent "
				+ " order by this.id.sequence ASC ", "CmEvaluateHoldDelinquencyProcessEnterStatusAlgComp_Impl");
		deliquencyProcessTypeAlgoListFilter.bindLookup("cmDelinquencyProcessTypeSystemEvent", CmDelinquencyProcessTypeSystemEventLookup.constants.CM_HOLD_DELINQUENCY_PROCESS_CRITERIA);
		List<CmDelinquencyProcessTypeAlgorithm> delinquncyProcessTypeAlgoList = deliquencyProcessTypeAlgoListFilter.list();
		for (CmDelinquencyProcessTypeAlgorithm delinquncyProcessTypeAlgo : delinquncyProcessTypeAlgoList) {
			Algorithm algorithm = delinquncyProcessTypeAlgo.getAlgorithm();

			CmHoldDelinquencyProcessCriteriaAlgorithmSpot algorithmComp = AlgorithmComponentCache.getAlgorithmComponent(algorithm.getId(), CmHoldDelinquencyProcessCriteriaAlgorithmSpot.class);
			// set Algorithm input parameters
			algorithmComp.setDelinquencyProcessId(delProcId);
			// Invoke and set the output parameters
			algorithmComp.invoke();

			// Retrieve Hold switch from algorithm
			Bool holdProcessSwitch = algorithmComp.getIsHoldProcessSwitch();

			// If hold process switch is true
			if (holdProcessSwitch.isTrue()) {
				skipAutoTransition = true;
				break;
			}

		}

	}


	/**
	 * This method retrieves trigger date for corresponding delinquency process trigger events
	 * @param statusList
	 * @return - Date - triggerDate
	 */
	private Date retDelProcTriggerDate(List<String> statusList) {
		Date triggerDate = null;
		StringBuilder delProcTriggerEvent = new StringBuilder().append(CmDelinquencyProcessConstant.FETCH_DEL_PROC_TRIGG_EVNTS);

		for (int index = 0; index < statusList.size(); index++)
		{
			if (index != 0)
			{
				delProcTriggerEvent.append(",");
			}
			delProcTriggerEvent.append("'").append(statusList.get(index).trim()).append("'");
		}
		delProcTriggerEvent.append(")");

		Query<Date> query = createQuery(delProcTriggerEvent.toString(), "CmEvaluateHoldDelinquencyProcessEnterStatusAlgComp_Impl");
		query.addResult("triggerDate", "TE.triggerDate");
		query.orderBy("triggerDate");
		triggerDate = query.firstRow();
		
		return triggerDate;
	}

	/**
	 * This method retrieves valid next status list for current status of delinquency process
	 * @param delinquencyProces
	 * @return - List - statusList
	 */
	public List<String> retValidNextStatusList(CmDelinquencyProcess delinqProc) {
		List<String> statusList = new ArrayList<String>();
		List<String> resultList = null;
		BusinessObject businessObject = delinqProc.getBusinessObject();
		
		Query<String> query = createQuery(" from BusinessObjectStatusTransitionRule rule where rule.id.businessObjectStatus.id.businessObject = :businessObject and rule.id.businessObjectStatus.id.status=:currentBOStatus "
				, "CmEvaluateHoldDelinquencyProcessEnterStatusAlgComp_Impl");
		query.bindEntity("businessObject", businessObject);
		query.bindStringProperty("currentBOStatus", BusinessObjectStatus.properties.status, delinqProc.getStatus());
		query.addResult("nextStatus", "rule.id.nextStatus");
		resultList = query.list();
		
		if (!isNull(resultList) && (resultList.size() > 0)) {
			for (String businessObjectStatus : resultList) {
				statusList.add(businessObjectStatus);
			}
		}
		return statusList;
	}
}

