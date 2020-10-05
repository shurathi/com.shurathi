/*
 **************************************************************************                                                   
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Delinquency Control Maintenance File
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

/**
  *  
@EntityPageMaintenance (entity = cmDelinquencyControl, service = CM-DELIN-CTR, program = CM-DELIN-CTR,
 *      body = @DataElement (contents = { @RowField (entity = cmDelinquencyControl, name = cmDelinquencyControl)
 *                  , @ListField (name = ALGORITHMS, owner = cmDelinquencyControl, property = algorithms)}),
 *      modules = { "demo"},
 *      actions = { "change"
 *            , "delete"
 *            , "read"
 *            , "add"},
 *      lists = { @List (name = ALGORITHMS, size = 50, service = ALGORITHMS,
 *                  body = @DataElement (contents = { @RowField (entity = cmDelinquencyControlAlgorithm, name = cmDelinquencyControlAlgorithm)}))})
*/

public class CmDelinquencyControlMaintenance
		extends CmDelinquencyControlMaintenance_Gen {

}
 
