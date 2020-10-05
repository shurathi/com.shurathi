/*
 **********************************************************************************************************************************************
 * 
 * PROGRAM DESCRIPTION:
 * 
 * This algorithm triggers the Delinquency Cancel Criteria algorithms on the Delinquency Process Type to determine if the Delinquency Process 
 * should be transitioned to Cancelled status
 **********************************************************************************************************************************************
 * 
 * 
 * CHANGE HISTORY:
 * 
 * Date:       	by:         Reason:
 * 2020-05-06   MugdhaP		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework
 **********************************************************************************************************************************************
 */

package com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.splwg.base.api.ListFilter;
import com.splwg.base.api.Query;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.BusinessObjectStatusCode;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.LookupHelper;
import com.splwg.base.api.lookup.BusinessObjectStatusTransitionConditionLookup;
import com.splwg.base.domain.common.algorithm.AlgorithmComponentCache;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatus;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatusAutoTransitionAlgorithmSpot;
import com.splwg.base.domain.common.businessObjectStatusReason.BusinessObjectStatusReason_Id;
import com.splwg.cm.api.lookup.CmDelinquencyProcessTypeSystemEventLookup;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmDelinquencyProcessCancelCriteriaAlgorithmSpot;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmDelinquencyProcessType;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmDelinquencyProcessTypeAlgorithm;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;

/**
 * @author MugdhaP
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (name = transitionConditionforCancelledStatus, required = true, type = string)
 *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = evaluateOnTriggerDates, required = true, type = lookup)})
 */

public class CmEvaluateDelinquencyProcessCancelCriteriaAlgComp_Impl extends CmEvaluateDelinquencyProcessCancelCriteriaAlgComp_Gen implements BusinessObjectStatusAutoTransitionAlgorithmSpot {

	private BusinessObjectInstanceKey boInstanceKey;
	private BusinessObjectStatusTransitionConditionLookup nextTransitionCondition;
	private BusinessObjectStatusReason_Id statusReasonId;

	@Override
	public void setBusinessObject(BusinessObject paramBusinessObject) {

	}

	@Override
	public void setBusinessObjectKey(BusinessObjectInstanceKey paramBusinessObjectInstanceKey) {
		boInstanceKey = paramBusinessObjectInstanceKey;
	}

	@Override
	public BusinessObjectStatusCode getNextStatus() {
		return null;
	}

	@Override
	public BusinessObjectStatusTransitionConditionLookup getNextStatusCondition() {
		return nextTransitionCondition;
	}

	@Override
	public boolean getUseDefaultNextStatus() {
		return false;
	}

	@Override
	public boolean getSkipAutoTransitioning() {
		return false;
	}

	@Override
	public BusinessObjectStatusReason_Id getStatusChangeReasonId() {
		return statusReasonId;
	}

	@Override
	public void invoke() {

		nextTransitionCondition = null;
		Date triggerDate = null;
		CmDelinquencyProcess_Id delinquencyProcId = new CmDelinquencyProcess_Id(boInstanceKey.getString("delinquencyProcessId"));

		if (isNull(delinquencyProcId) && isNull(delinquencyProcId.getEntity())) {
			addError(MessageRepository.delinquencyProcessRequired());
		}
		CmDelinquencyProcess delinquencyProcess = delinquencyProcId.getEntity();
		// check whether parameter Only Evaluate on Trigger Dates is Y
		if (getEvaluateOnTriggerDates().isYes()) {

			// Retrieve the valid next statuses for the BOs current status
			List<String> validNextStatusList = retValidNextStatusList(delinquencyProcess);

			// If validNextStatusList is empty end processing
			if (isNull(validNextStatusList) || (validNextStatusList.isEmpty())) {
				return;
			}
			// Retrieve the Delinquency Process trigger events first rows
			// trigger date
			triggerDate = retDelProcTriggerDate(validNextStatusList);

			// If trigger date is on or after process date
			if (notNull(triggerDate) && (triggerDate.isAfter(getProcessDateTime().getDate()))) {
				return;
			}
		}

		CmDelinquencyProcessType delinquencyProcessType = delinquencyProcId.getEntity().getCmDelinquencyProcessType();
		// Fetch all Cancel Criteria Algorithms on Delinquency Process Type
		ListFilter<CmDelinquencyProcessTypeAlgorithm> cancelCriteriaAlgorithmList = delinquencyProcessType.getAlgorithms().createFilter(" where this.id.cmDelinquencyProcessTypeSystemEvent = :cancelCriteriaSystemEvent order by this.id.sequence asc", "CmEvaluateDelinquencyProcessCancelCriteriaAlgComp_Impl");
		cancelCriteriaAlgorithmList.bindLookup("cancelCriteriaSystemEvent", CmDelinquencyProcessTypeSystemEventLookup.constants.CM_CANCEL_CRITERIA);

		if (!isNull(cancelCriteriaAlgorithmList)) {
			for (Iterator<CmDelinquencyProcessTypeAlgorithm> iter = cancelCriteriaAlgorithmList.iterate(); iter.hasNext();) {

				// Invoke the algorithm passing Delinquency Process ID as input
				CmDelinquencyProcessTypeAlgorithm algorithm = iter.next();
				CmDelinquencyProcessCancelCriteriaAlgorithmSpot algorithmComp = AlgorithmComponentCache.getAlgorithmComponent(algorithm.getAlgorithm().getId(), CmDelinquencyProcessCancelCriteriaAlgorithmSpot.class);
				algorithmComp.setCmDelinquencyProcessId(delinquencyProcId);
				algorithmComp.invoke();
				if (algorithmComp.getOkToCancelSwitch().equals(Bool.TRUE))
				{
					nextTransitionCondition = LookupHelper.getLookupInstance(BusinessObjectStatusTransitionConditionLookup.class, getTransitionConditionforCancelledStatus());
					if (notNull(algorithmComp.getBusinessObjectStatusReason())) {
						statusReasonId = algorithmComp.getBusinessObjectStatusReason();
					}
					break;
				}

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

		Query<Date> query = createQuery(delProcTriggerEvent.toString(), "CmEvaluateDelinquencyProcessCancelCriteriaAlgComp_Impl");
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
				, "CmEvaluateDelinquencyProcessCancelCriteriaAlgComp_Impl");
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

