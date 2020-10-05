/*
 **************************************************************************                                                   
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Delinquency Control Implementation.
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
@BusinessEntity (tableName = CM_DELIN_CNTL,
 *      oneToManyCollections = { @Child (collectionName = algorithms, childTableName = CM_DELIN_CNTL_ALG,
 *                  orderByColumnNames = { "CM_DELIN_CTRL_SEVT_FLG"
 *                        , "SEQ_NUM"})})
*/

public class CmDelinquencyControl_Impl
    extends CmDelinquencyControl_Gen {
   // empty
}
