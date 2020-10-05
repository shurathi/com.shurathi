/*
 **************************************************************************
 *                                                                                                                      
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * This algorithm validates the Delinquency Process Type when it is saved. It will check whether
 * calculate unpaid and original amount cancel criteria algorithm is configured.
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
package com.splwg.cm.domain.delinquency.admin.delinquencyProcessType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.splwg.base.api.ListFilter;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.datatypes.EntityId;
import com.splwg.base.api.lookup.BusinessObjectActionLookup;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.ValidateBusinessObjectAlgorithmSpot;
import com.splwg.base.domain.common.maintenanceObject.MaintenanceObject;
import com.splwg.cm.api.lookup.CmDelinquencyProcessTypeSystemEventLookup;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;

/**
 * @author VINODW
 *
@AlgorithmComponent ()
 */
public class CmDelinquencyProcessTypeValidationAlgComp_Impl extends CmDelinquencyProcessTypeValidationAlgComp_Gen implements ValidateBusinessObjectAlgorithmSpot {

	// hard parameters
	@SuppressWarnings("unused")
	private BusinessObjectInstance boInst;
	private BusinessObjectInstanceKey boInstKey;
	private BusinessObjectActionLookup actionLookup;
	public final String BO_ELEM_IMPORT_ID = "delinquencyProcessType";

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
		// Read delinquency Process type entity
		CmDelinquencyProcessType delinquencyProcessType = new CmDelinquencyProcessType_Id(boInstKey.getString(BO_ELEM_IMPORT_ID)).getEntity();
		// for all action except Delete
		if (!this.actionLookup.isDelete()) {
			List<CmDelinquencyProcessTypeAlgorithm> cancelUnpaidAndOrgnleAlgoList = fetchDelinquencyProcessTypeAlgorithmForSpot(delinquencyProcessType, CmDelinquencyProcessTypeSystemEventLookup.constants.CM_CALC_UNPAID_AND_ORIGINAL_AMTS);
			List<CmDelinquencyProcessTypeAlgorithm> cancelCriteriaAlgoList = fetchDelinquencyProcessTypeAlgorithmForSpot(delinquencyProcessType, CmDelinquencyProcessTypeSystemEventLookup.constants.CM_CANCEL_CRITERIA);
			List<String> algorithmList = getDelinquencyProcessTypeAlgorithmForSpot(delinquencyProcessType);
			// Check if cancel criteria and cancel unpaid and original algorithm
			// is configured or not

			//Check if cancel unpaid and original algorithm is configured or not
			 if ( notNull(delinquencyProcessType.getCmCollectingOnObjectTypeId()) && cancelUnpaidAndOrgnleAlgoList.size() == 0) {	
				addError(MessageRepository.calcUnpaidAndOriginalAmtAlgoIsRequired(CmDelinquencyProcessTypeSystemEventLookup.constants.CM_CALC_UNPAID_AND_ORIGINAL_AMTS.getLookupValue().fetchLanguageDescription()));
			 }
			
						
			// Check if cancel criteria algorithm is configure or not
			if (notNull(cancelCriteriaAlgoList) && cancelCriteriaAlgoList.size() == 0)
			{		
				addError(MessageRepository.cancelCriteriaAlgoIsRequired(CmDelinquencyProcessTypeSystemEventLookup.constants.CM_CANCEL_CRITERIA.getLookupValue().fetchLanguageDescription()));
			}
			// Check if duplicate algorithm are configured on deliquency process
			// type
			if (notNull(algorithmList) && hasDuplicate(algorithmList)) {
				addError(MessageRepository.multipleDeliquencyProcessTypeAlgorithmsFound(delinquencyProcessType.fetchLanguageDescription()));				
			}
		}

	}

	/**
	 * This method fetches Delinquency Process Type Algorithm For Spot.
	 * @param delinquencyProcessType
	 * @param lookupValue
	 * @return List<CmDelinquencyProcessTypeAlgorithm>
	 */
	private List<CmDelinquencyProcessTypeAlgorithm> fetchDelinquencyProcessTypeAlgorithmForSpot(CmDelinquencyProcessType delinquencyProcessType, CmDelinquencyProcessTypeSystemEventLookup lookupValue) {
		ListFilter<CmDelinquencyProcessTypeAlgorithm> delinquencyProcessAlgQry = delinquencyProcessType.getAlgorithms().
				createFilter(" where this.id.cmDelinquencyProcessTypeSystemEvent =:delinquencyProcessTypeAlgoEntityLkpVal order by this.id.sequence ", "CmDelinquencyProcessTypeValidationAlgComp_Impl");
		delinquencyProcessAlgQry.bindLookup("delinquencyProcessTypeAlgoEntityLkpVal", lookupValue);
		List<CmDelinquencyProcessTypeAlgorithm> algoList = delinquencyProcessAlgQry.list();
		return algoList;
	}

	/**
	 * @param delinquencyProcessType
	 * @return
	 */
	private List<String> getDelinquencyProcessTypeAlgorithmForSpot(CmDelinquencyProcessType delinquencyProcessType) {
		CmDelinquencyProcessTypeAlgorithms delinquencyProcessAlgQry = delinquencyProcessType.getAlgorithms();
		List<String> algoList = new ArrayList<String>();
		Iterator<CmDelinquencyProcessTypeAlgorithm> algoIter = delinquencyProcessAlgQry.iterator();
		while (algoIter.hasNext())
		{
			algoList.add(algoIter.next().getAlgorithm().getId().getIdValue());
		}
		return algoList;
	}

	/**
	 * @param monitorAlgoList
	 * @return
	 */
	private boolean hasDuplicate(List<String> algoList) {
		Set<String> set = new HashSet<String>(algoList);
		return (set.size() < algoList.size()) ? true : false;
	}

}

