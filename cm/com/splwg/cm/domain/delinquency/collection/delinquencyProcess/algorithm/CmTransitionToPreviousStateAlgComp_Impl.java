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
 * Transition to Previous Delinquency Status
 * 
 * This algorithm will find its way to the previous BO status where it came from. This needs to be plugged in a transitory status.
 *
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:                  Reason:
 * 2020-08-20  SAnart         CB-286.Initial Version.
 */
package com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm;

import java.util.ArrayList;
import java.util.List;

import com.splwg.base.api.Query;
import com.splwg.base.api.QueryResultRow;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.BusinessObjectStatusCode;
import com.splwg.base.api.datatypes.LookupHelper;
import com.splwg.base.api.lookup.BusinessObjectActionLookup;
import com.splwg.base.api.lookup.BusinessObjectStatusTransitionConditionLookup;
import com.splwg.base.api.lookup.BusinessObjectStatusTransitoryLookup;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectExitStatusAlgorithmSpot;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatus;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatusAutoTransitionAlgorithmSpot;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatus_Id;
import com.splwg.base.domain.common.businessObjectStatusReason.BusinessObjectStatusReason_Id;
import com.splwg.ccb.api.lookup.TransitoryFlagLookup;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;

/**
 * @author SAnarthe
 *
@AlgorithmComponent ()
 */
public class CmTransitionToPreviousStateAlgComp_Impl extends
		CmTransitionToPreviousStateAlgComp_Gen implements
		BusinessObjectStatusAutoTransitionAlgorithmSpot 
{
    private BusinessObjectInstanceKey boInstKey;
	String EMPTY_STRING=" ";
	
	CmDelinquencyProcess_Id delinquencyProcessId =null;
    BusinessObjectStatusCode statusCd;
    
	@Override
	public void invoke() 
	{
		
		String status = EMPTY_STRING;
		delinquencyProcessId = new CmDelinquencyProcess_Id(boInstKey.getString("delinquencyProcessId"));
		
		QueryResultRow queryResultRow =retrieveStatus(delinquencyProcessId); 
		
		if(!isNull(queryResultRow))
		{
			status=queryResultRow.getString("status");	
			BusinessObject bo=delinquencyProcessId.getEntity().getBusinessObject();
			statusCd=new BusinessObjectStatusCode(bo.getId(),status);
		}
		else
		{
			addError(MessageRepository.unableToDetPrevStatus(status,delinquencyProcessId.getIdValue()));
		}
	}

	@Override
	public boolean getForcePostProcessing() {
		return false;
	}

	@Override
	public BusinessObjectStatusCode getNextStatus() {
		return statusCd;
	}

	@Override
	public BusinessObjectStatusTransitionConditionLookup getNextStatusCondition() {
		
		return null;
	}

	@Override
	public boolean getSkipAutoTransitioning() {
		return false;
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
		boInstKey=arg0;
		
	}
	/**
	 * This Method will Fetch the Status
	 * @return sqlResultRow
	 */
	public QueryResultRow retrieveStatus(CmDelinquencyProcess_Id delinquencyProcessId) 
	{
		QueryResultRow queryResultRow = null;
		
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("FROM CmDelinquencyProcessLog dpl, CmDelinquencyProcess dp, BusinessObjectStatus bostat ");
		queryBuilder.append("WHERE dpl.id.delinquencyProcess.id = dp.id ");
		queryBuilder.append("AND  dp.businessObject.id = bostat.id.businessObject.id ");
		queryBuilder.append("AND dpl.status = bostat.id.status ");
		queryBuilder.append("AND dpl.id.delinquencyProcess.id = :delinqId ");
		queryBuilder.append("AND bostat.transitoryStatus = :stat ");
	    
		Query<QueryResultRow> ccQuery = createQuery(queryBuilder.toString(), EMPTY_STRING);
		ccQuery.bindId("delinqId", this.delinquencyProcessId);
		ccQuery.bindLookup("stat",BusinessObjectStatusTransitoryLookup.constants.PERMANENT_STATE);
		ccQuery.addResult("status", "dpl.status");
		ccQuery.addResult("logdttm", "dpl.logDateTime");
		ccQuery.orderBy("logdttm",Query.DESCENDING);
		queryResultRow=ccQuery.firstRow();
	
		return queryResultRow;
	}

}
