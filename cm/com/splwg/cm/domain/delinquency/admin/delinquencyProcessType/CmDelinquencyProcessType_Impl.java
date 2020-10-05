/*
 *******************************************************************************
 *
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Delinquency Process Type Implementation
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
/**
 *  
@BusinessEntity (tableName = CM_DELIN_PROC_TYP,
*      oneToManyCollections = { @Child (collectionName = algorithms, childTableName = CM_DELIN_PROC_TYP_ALG,
*                  orderByColumnNames = { "CM_DELIN_PROC_TYP_SEVT_FLG"
*                        , "SEQ_NUM"})
*            , @Child (collectionName = characteristics, childTableName = CM_DELIN_PROC_TYP_CHAR,
*                  orderByColumnNames = { "SEQ_NUM"})})
*/


public class CmDelinquencyProcessType_Impl
    extends CmDelinquencyProcessType_Gen {
   // empty
}
