/*
 **************************************************************************
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Delinquency Process Log Parameter Change Handler
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
import com.splwg.base.api.maintenanceObject.MaintenanceLogParameterCHandlerHelper;
import com.splwg.base.domain.common.maintenanceObject.MaintenanceObject_Id;

/**
 * @author VINODW
 *
@ChangeHandler (entityName = cmDelinquencyProcessLogParm)
 */
public class CmDelinquencyProcessLogParm_CHandler extends AbstractChangeHandler<CmDelinquencyProcessLogParm> {
	private final MaintenanceLogParameterCHandlerHelper helper = new MaintenanceLogParameterCHandlerHelper<CmDelinquencyProcessLogParm, CmDelinquencyProcessLog>(
			new MaintenanceObject_Id("CM-DELIN-PRC"), CmDelinquencyProcessLogParm.properties, CmDelinquencyProcessLogParm.properties.lookOnParentCmDelinquencyProcessLog());

	@SuppressWarnings("unchecked")
	@Override
	public void prepareToAdd(DataTransferObject newDTO) {
		helper.prepareToAdd(newDTO);
	}

	/**
	 * @see com.splwg.base.api.changehandling.ChangeHandler#getValidationRules()
	 */
	@Override
	public ValidationRule[] getValidationRules() {
		return helper.getValidationRules();
	}
}
