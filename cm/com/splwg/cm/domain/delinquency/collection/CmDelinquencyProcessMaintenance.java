/*
 **************************************************************************                                                    
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Delinquency Process Maintenance File
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
@EntityPageMaintenance (entity = cmDelinquencyProcess, service = CM-DELIN-PRC, program = CIPYPGDJ,
 *      body = @DataElement (contents = { @RowField (entity = cmDelinquencyProcess, includeRCopybook = false, name = cmDelinquencyProcess)
 *                  , @ListField (name = CM_DELINQUENCY_PROCESS_CHARACTERISTIC, owner = cmDelinquencyProcess, property = characteristics)
 *                  , @ListField (name = CM_DELINQUENCY_PROCESS_LOG, owner = cmDelinquencyProcess, property = logs)
 *                  , @ListField (name = CM_DELINQUENCY_PROCESS_RELATED_OBJECT, owner = cmDelinquencyProcess, property = relatedObjects)
 *                  , @ListField (name = CM_DELINQUENCY_PROCESS_TRIGGER_EVENT, owner = cmDelinquencyProcess, property = triggerEvents)}),
 *      modules = { "demo"},
 *      actions = { "change"
 *            , "delete"
 *            , "read"
 *            , "add"},
 *      lists = { @List (name = CM_DELINQUENCY_PROCESS_CHARACTERISTIC, size = 50,
 *                  body = @DataElement (contents = { @RowField (entity = cmDelinquencyProcessCharacteristic, name = cmDelinquencyProcessCharacteristic)}))
 *            , @List (name = CM_DELINQUENCY_PROCESS_LOG, size = 50,
 *                  body = @DataElement (contents = { @RowField (entity = cmDelinquencyProcessLog, name = cmDelinquencyProcessLog)
 *                              , @ListField (name = CM_DELINQUENCY_PROCESS_LOG_PARM, owner = cmDelinquencyProcessLog, property = cmDelinquencyProcessLog)}))
 *            , @List (name = CM_DELINQUENCY_PROCESS_LOG_PARM, size = 50,
 *                  body = @DataElement (contents = { @RowField (entity = cmDelinquencyProcessLogParm, name = cmDelinquencyProcessLogParm)}))
 *            , @List (name = CM_DELINQUENCY_PROCESS_RELATED_OBJECT, size = 50,
 *                  body = @DataElement (contents = { @RowField (entity = cmDelinquencyProcessRelatedObject, name = cmDelinquencyProcessRelatedObject)}))
 *            , @List (name = CM_DELINQUENCY_PROCESS_TRIGGER_EVENT, size = 50,
 *                  body = @DataElement (contents = { @RowField (entity = cmDelinquencyProcessTriggerEvent, name = cmDelinquencyProcessTriggerEvent)}))})
*/

public class CmDelinquencyProcessMaintenance
    extends CmDelinquencyProcessMaintenance_Gen {}
