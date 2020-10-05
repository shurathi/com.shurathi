/*
 **************************************************************************                                                    
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Delinquency Process Implementation
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
@BusinessEntity (tableName = CM_DELIN_PROC,
 *      oneToManyCollections = { @Child (collectionName = characteristics, childTableName = CM_DELIN_PROC_CHAR,
 *                  orderByColumnNames = { "EFFDT"})
 *            , @Child (collectionName = logs, childTableName = CM_DELIN_PROC_LOG,
 *                  orderByColumnNames = { "SEQNO"})
 *            , @Child (collectionName = relatedObjects, childTableName = CM_DELIN_PROC_REL_OBJ,
 *                  orderByColumnNames = { "CM_DEL_REL_OBJ_TYPE_FLG"})
 *            , @Child (collectionName = triggerEvents, childTableName = CM_DELIN_PROC_TRIG_EVT,
 *                  orderByColumnNames = { "SEQNO"})})
*/

public class CmDelinquencyProcess_Impl
    extends CmDelinquencyProcess_Gen {
   // empty
}
