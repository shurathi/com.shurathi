/*
 *******************************************************************************************************************
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Delinquency Monitor Rule
 * Delinquency control plug-in spot 
 *                                                             
 *******************************************************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 *           
 * 2020-05-06   VINODW		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework
 * ******************************************************************************************************************
 */
package com.splwg.cm.domain.delinquency.admin.delinquencyProcessType;

import com.splwg.base.api.algorithms.AlgorithmSpot;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;

/**
 * @author VINODW
 *
@AlgorithmSpot (algorithmEntityValues = { "cmHoldDelinquencyProcessCriteria"})
 */
public interface CmHoldDelinquencyProcessCriteriaAlgorithmSpot extends AlgorithmSpot {

	public abstract void setDelinquencyProcessId(CmDelinquencyProcess_Id delinquencyProcessId);

	public abstract Bool getIsHoldProcessSwitch();
}
