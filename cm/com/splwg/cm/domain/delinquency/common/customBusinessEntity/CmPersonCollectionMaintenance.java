/*
 **************************************************************************                                                   
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Person Collection Maintenance File
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
@EntityPageMaintenance (entity = cmPersonCollection, service = CM-PERCOLL, program = CM-PERCOLL,
 *      body = @DataElement (contents = { @RowField (entity = cmPersonCollection, name = cmPersonCollection)
 *                  , @ListField (name = CM_CUSTOMER_REVIEW_SCHEDULES, owner = CM, property = cmCustomerReviewSchedules)}),
 *      modules = { "demo"},
 *      actions = { "change"
 *            , "delete"
 *            , "read"
 *            , "add"},
 *      lists = { @List (name = CM_CUSTOMER_REVIEW_SCHEDULES, size = 50, service = CM_CUSTOMER_REVIEW_SCHEDULES,
 *                  body = @DataElement (contents = { @RowField (entity = cmCustomerReviewSchedule, name = cmCustomerReviewSchedule)}),
 *                  childTables = { "CM_CUS_RVW_SCH"})})
*/

public class CmPersonCollectionMaintenance
    extends CmPersonCollectionMaintenance_Gen {
   // empty
}
