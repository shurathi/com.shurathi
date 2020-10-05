/*
 **************************************************************************                                                   
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * This algorithm validates the Delinquency Control when it is saved. It will check whether
 * deliquency monitor rule algorithm is configured on deliquency control.
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
package com.splwg.cm.domain.delinquency.common.customBusinessEntity;

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
import com.splwg.ccb.domain.admin.collectionClass.CollectionClass;
import com.splwg.ccb.domain.admin.collectionClass.CollectionClass_Id;
import com.splwg.cm.api.lookup.CmDelinquencyControlSystemEventLookup;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;

/**
 * @author VINODW
 *
@AlgorithmComponent ()
 */
public class CmDelinquencyControlValidationAlgComp_Impl extends CmDelinquencyControlValidationAlgComp_Gen implements ValidateBusinessObjectAlgorithmSpot {

	// hard parameters
	@SuppressWarnings("unused")
	private BusinessObjectInstance boInst;
	private BusinessObjectInstanceKey boInstKey;
	private BusinessObjectActionLookup actionLookup;
	public final String BO_ELEM_IMPORT_ID = "collectionClass";

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
		// Read delinquency control entity
		CollectionClass collectionClass = new CollectionClass_Id(boInstKey.getString((BO_ELEM_IMPORT_ID))).getEntity();
		CmDelinquencyControl delinquencyControl = new CmDelinquencyControl_Id(collectionClass).getEntity();
		// for all action except Delete
		if (!this.actionLookup.isDelete()) {
			List<CmDelinquencyControlAlgorithm> configuredAlgoList = getDelinquencyControlAlgorithmForSpot(delinquencyControl, CmDelinquencyControlSystemEventLookup.constants.CM_DELINQUENCY_MONITOR_RULE);
			List<String> monitorAlgoList = fetchDelinquencyControlAlgorithmForSpot(delinquencyControl);
			if (isNull(configuredAlgoList) || configuredAlgoList.size() == 0) {
				addError(MessageRepository.delinquencyMonitorRuleAlgoIsRequired(CmDelinquencyControlSystemEventLookup.constants.CM_DELINQUENCY_MONITOR_RULE.getLookupValue().fetchLanguageDescription()));
			}
			// Checking if delinquency monitor rule algorithm
			// is having duplicate entry
			if (notNull(monitorAlgoList) && hasDuplicate(monitorAlgoList)) {
				addError(MessageRepository.multipleDeliquencyMonitorRuleAlgorithmsFound(delinquencyControl.getId().getCollectionClassId().getEntity().fetchLanguageDescription()));
			}
		}

	}

	/**
	 * @param monitorAlgoList
	 * @return
	 */
	private boolean hasDuplicate(List<String> monitorAlgoList) {
		Set<String> set = new HashSet<String>(monitorAlgoList);
		return (set.size() < monitorAlgoList.size()) ? true : false;
	}

	/**
	 * @param delinquencyControl
	 * @return
	 */
	private List<String> fetchDelinquencyControlAlgorithmForSpot(CmDelinquencyControl delinquencyControl) {
		CmDelinquencyControlAlgorithms delinquencyProcessAlgQry = delinquencyControl.getAlgorithms();
		List<String> algoList = new ArrayList<String>();
		Iterator<CmDelinquencyControlAlgorithm> algoIter = delinquencyProcessAlgQry.iterator();
		while (algoIter.hasNext())
		{
			algoList.add(algoIter.next().getAlgorithm().getId().getIdValue());
		}
		return algoList;
	}

	/**
	 * This method fetches Delinquency Control Algorithm For Spot.
	 * @param delinquencyControl
	 * @param lookupValue
	 * @return List<CmDelinquencyControlAlgorithm>
	 */
	public List<CmDelinquencyControlAlgorithm> getDelinquencyControlAlgorithmForSpot(CmDelinquencyControl delinquencyControl, CmDelinquencyControlSystemEventLookup lookupValue) {
		ListFilter<CmDelinquencyControlAlgorithm> delinquencyProcessAlgQry = delinquencyControl.getAlgorithms().
				createFilter(" where this.id.delinquencyControlSystemEvent =:delinquencyControlAlgoEntityLkpVal order by this.id.sequence ", "CmDelinquencyControlValidationAlgComp_Impl");
		delinquencyProcessAlgQry.bindLookup("delinquencyControlAlgoEntityLkpVal", lookupValue);
		delinquencyProcessAlgQry.addResult("Algorithm", "this.algorithm");
		List<CmDelinquencyControlAlgorithm> algoList = delinquencyProcessAlgQry.list();
		return algoList;
	}
}

