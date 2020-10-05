/*
 ********************************************************************************************
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Delinquency Process 
 * This algorithm spot is designed to invoke cancel criteria algorithm
 *     
 ********************************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 *           
 * 2020-05-06   VINODW		Initial Version : ANTHM-340 CAB1-9462 Delinquency Framework
 * ******************************************************************************************
 */

package com.splwg.cm.domain.delinquency.admin.delinquencyProcessType;

import com.splwg.base.api.algorithms.AlgorithmSpot;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.domain.common.businessObjectStatusReason.BusinessObjectStatusReason_Id;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;

/**
 * @author VINODW
 *
@AlgorithmSpot (algorithmEntityValues = { "cmCancelCriteria"})
 */
public interface CmDelinquencyProcessCancelCriteriaAlgorithmSpot extends AlgorithmSpot {
	
	public abstract void setCmDelinquencyProcessId(CmDelinquencyProcess_Id delinquencyProcessId);
	public abstract Bool getOkToCancelSwitch();

	public abstract BusinessObjectStatusReason_Id getBusinessObjectStatusReason();
}

