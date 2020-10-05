/*
 **************************************************************************                                                     
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Delinquency Process Log Implementation
 *                                                             
 **************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    	Reason:                                     
 * YYYY-MM-DD  	IN     	Reason text.                                
 *           
 * 2020-05-06   VINODW	Initial version : ANTHM-340 CAB1-9462 Delinquency Framework
 **************************************************************************
 */
package com.splwg.cm.domain.delinquency.collection;

/**
  *  
@BusinessEntity (tableName = CM_DELIN_PROC_LOG,
 *      oneToManyCollections = { @Child (collectionName = parms, childTableName = CM_DELIN_PROC_LOG_PARM,
 *                  orderByColumnNames = { "PARM_SEQ"})})
*/

public class CmDelinquencyProcessLog_Impl
    extends CmDelinquencyProcessLog_Gen {
   // empty
}
