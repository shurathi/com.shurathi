/*
 **************************************************************************                                                   
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Person Collection Implementation
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
@BusinessEntity (tableName = CM_PER_COLL,
 *      oneToManyCollections = { @Child (collectionName = cmCustomerReviewSchedules, childTableName = CM_CUS_RVW_SCH,
 *                  orderByColumnNames = { "NEXT_CR_RVW_DT"})})
*/

public class CmPersonCollection_Impl
    extends CmPersonCollection_Gen {
   // empty
}
