/*
 *******************************************************************************
 *
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Delinquency Process Type 
 * Calculate Unpaid & Original Amounts plugin spot 
 *                                                             
 *******************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 *           
 * 2020-05-06   VINODW		Initial Version : ANTHM-340 CAB1-9462 Delinquency Framework
 * *****************************************************************************
 */
package com.splwg.cm.domain.delinquency.admin.delinquencyProcessType;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.algorithms.AlgorithmSpot;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessRelatedObject;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;

/**
 * @author VINODW
 *
@AlgorithmSpot (algorithmEntityValues = { "cmCalcUnpaidAndOriginalAmts"})
 */
public interface CmCalculateUnpaidOriginalAmountAlgorithmSpot extends AlgorithmSpot {

	public abstract void setDelinquencyProcessId(CmDelinquencyProcess_Id delProcId);

	public abstract void setDelinquencyProcessRelatedObject(CmDelinquencyProcessRelatedObject delProcRelObject);

	public abstract BigDecimal getUnpaidAmount();

	public abstract BigDecimal getOriginalAmount();

	public abstract Bool getIndeterminateSwitch();

}
