/*
 **************************************************************************
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Delinquency Process Log Change Handler
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

import com.splwg.base.api.DataTransferObject;
import com.splwg.base.api.changehandling.AbstractChangeHandler;
import com.splwg.base.api.changehandling.ValidationRule;
import com.splwg.base.api.maintenanceObject.MaintenanceLogCHandlerHelper;
import com.splwg.base.domain.common.maintenanceObject.MaintenanceObject_Id;
import com.splwg.cm.api.lookup.CharacteristicEntityLookup;

/**
 * @author VINODW
 *
@ChangeHandler (entityName = cmDelinquencyProcessLog)
 */
public class CmDelinquencyProcessLog_CHandler extends AbstractChangeHandler<CmDelinquencyProcessLog> {

	private final MaintenanceLogCHandlerHelper helper = new MaintenanceLogCHandlerHelper<CmDelinquencyProcessLog, CmDelinquencyProcess>(
			new MaintenanceObject_Id("CM-DELIN-PRC"), CmDelinquencyProcessLog.properties, CmDelinquencyProcessLog.properties.lookOnParentDelinquencyProcess(), CharacteristicEntityLookup.constants.CM_DELINQUENCY_PROCESS_LOG);

	@SuppressWarnings("unchecked")
	@Override
	public void prepareToAdd(DataTransferObject<CmDelinquencyProcessLog> newDTO) {
		helper.prepareToAdd(newDTO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void prepareToChange(CmDelinquencyProcessLog unchangedEntity, DataTransferObject<CmDelinquencyProcessLog> newDTO) {
		helper.prepareToChange(unchangedEntity, newDTO);
	}

	@Override
	public ValidationRule[] getValidationRules() {
		return helper.getValidationRules();
	}

}
