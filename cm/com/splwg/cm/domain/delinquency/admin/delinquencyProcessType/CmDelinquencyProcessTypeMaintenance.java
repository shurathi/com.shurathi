/*
 **************************************************************************
 *                                                                                                                           
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Delinquency Process Type Maintenance File
 *                                                             
 **************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    	Reason:                                     
 * YYYY-MM-DD  	IN     	Reason text.                                
 *           
 * 2020-05-06   VINODW	Initial Version : ANTHM-340 CAB1-9462 Delinquency Framework
 **************************************************************************
 */
package com.splwg.cm.domain.delinquency.admin.delinquencyProcessType;
/**
*
@EntityPageMaintenance (entity = cmDelinquencyProcessType, service = CM-DLQ-PR-TP, program = CM-DLQ-PR-TP,
*      body = @DataElement (contents = { @RowField (entity = cmDelinquencyProcessType, name = cmDelinquencyProcessType)
*                  , @ListField (name = ALGORITHMS, owner = CM, property = algorithms)
*                  , @ListField (name = CHARACTERISTICS, owner = CM, property = characteristics)}),
*      modules = { "demo"},
*      lists = { @List (name = ALGORITHMS, size = 50,
*                  body = @DataElement (contents = { @RowField (entity = cmDelinquencyProcessTypeAlgorithm, name = cmDelinquencyProcessTypeAlgorithm)}))
*            , @List (name = CHARACTERISTICS, size = 50,
*                  body = @DataElement (contents = { @RowField (entity = cmDelinquencyProcessTypeCharacteristic, name = cmDelinquencyProcessTypeCharacteristic)}))},
*      actions = { "change"
*            , "delete"
*            , "read"
*            , "add"})
*/

public class CmDelinquencyProcessTypeMaintenance
    extends CmDelinquencyProcessTypeMaintenance_Gen {
   // empty
}
