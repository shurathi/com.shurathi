/*                                                               
 *************************************************************************************************                                                                
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Delinquency Calculation
 * This algorithm is designed to complete trigger events when delinquency
 * Process is cancelled                                                             
 **************************************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 *           
 * 2020-05-06   MugdhaP		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework
 * **************************************************************************************************
 */

package com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm;

import java.math.BigInteger;
import java.util.List;

import com.splwg.base.api.Query;
import com.splwg.base.api.QueryResultRow;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.BusinessObjectStatusCode;
import com.splwg.base.api.lookup.BusinessObjectStatusTransitionConditionLookup;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectEnterStatusAlgorithmSpot;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatus;
import com.splwg.base.domain.common.businessObjectStatusReason.BusinessObjectStatusReason_Id;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessTriggerEvent_DTO;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessTriggerEvent_Id;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;

/**
 * @author MugdhaP
 *
@AlgorithmComponent ()
 */
public class CmDelinquencyProcessCompletePendingDelinquencyEventsAlgComp_Impl extends CmDelinquencyProcessCompletePendingDelinquencyEventsAlgComp_Gen implements BusinessObjectEnterStatusAlgorithmSpot {

	private BusinessObjectInstanceKey boKey;

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
	public void setBusinessObject(BusinessObject bo) {

	}

	@Override
	public void setBusinessObjectKey(BusinessObjectInstanceKey key) {
		this.boKey = key;

	}

	@Override
	public void invoke() {

		// fetch Delinquency Process Id
		CmDelinquencyProcess_Id delinquencyProcessId = new CmDelinquencyProcess_Id(boKey.getString("delinquencyProcessId"));
		if (isNull(delinquencyProcessId.getEntity())) {

			addError(MessageRepository.delinquencyProcessRequired());
		}

		// fetch trigger events where status date time field is null
		StringBuilder FETCH_DLPROC_TRIGG_EVNTS = new StringBuilder(" from CmDelinquencyProcessTriggerEvent te ")
				.append(" where te.id.delinquencyProcess.id =  :deliqProcId ")
				.append(" and te.statusDateTime is null ");

		Query<QueryResultRow> query = createQuery(FETCH_DLPROC_TRIGG_EVNTS.toString(), "CmDelinquencyProcessCompletePendingDelinquencyEventsAlgComp_Impl");
		query.bindId("deliqProcId", delinquencyProcessId);
		query.addResult("sequence", "te.id.sequence");
		query.addResult("businessObjectStatus", "te.id.businessObjectStatus");

		List<QueryResultRow> eventList = query.list();

		if (notNull(eventList) && eventList.size() != 0) {

			for (QueryResultRow resultRow : eventList) {

				BusinessObjectStatus boStatus = resultRow.getEntity("businessObjectStatus", BusinessObjectStatus.class);
				BigInteger sequence = resultRow.getInteger("sequence");

				CmDelinquencyProcessTriggerEvent_Id eventId = new CmDelinquencyProcessTriggerEvent_Id(boStatus.getId(), delinquencyProcessId,
						sequence);

				CmDelinquencyProcessTriggerEvent_DTO eventDto = eventId.getEntity().getDTO();
				eventDto.setStatusDateTime(getProcessDateTime());
				eventId.getEntity().setDTO(eventDto);
			}

		}

	}

}

