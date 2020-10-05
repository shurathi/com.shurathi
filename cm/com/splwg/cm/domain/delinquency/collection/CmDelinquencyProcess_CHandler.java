/*
 **************************************************************************                                                            
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Import Change Handler
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
import com.splwg.base.api.ListFilter;
import com.splwg.base.api.changehandling.AbstractChangeHandler;
import com.splwg.base.api.changehandling.ValidationRule;
import com.splwg.base.api.maintenanceObject.BOBasedMaintenanceObjectCHandlerHelper;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatus;
import com.splwg.base.domain.common.maintenanceObject.MaintenanceObject_Id;

/**
 * @author VINODW
 *
@ChangeHandler (entityName = cmDelinquencyProcess)
 */
public class CmDelinquencyProcess_CHandler extends AbstractChangeHandler<CmDelinquencyProcess> {
	private final BOBasedMaintenanceObjectCHandlerHelper<CmDelinquencyProcess> helper = new BOBasedMaintenanceObjectCHandlerHelper<CmDelinquencyProcess>(new MaintenanceObject_Id("CM-DELIN-PRC"),
			CmDelinquencyProcess.properties, CmDelinquencyProcess.properties.lookOnBusinessObject());

	@SuppressWarnings("unchecked")
	@Override
	public void prepareToAdd(DataTransferObject<CmDelinquencyProcess> newDTO) {
		helper.prepareToAdd(newDTO);
		helper.getClass();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleAddOrChange(CmDelinquencyProcess businessEntity, DataTransferObject<CmDelinquencyProcess> oldDTO) {
		helper.handleAddOrChange(businessEntity, oldDTO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void prepareToChange(CmDelinquencyProcess unchangedEntity, DataTransferObject<CmDelinquencyProcess> newDTO) {
		helper.prepareToChange(unchangedEntity, newDTO);

		CmDelinquencyProcess_DTO dto = (CmDelinquencyProcess_DTO) newDTO;
	}

	@Override
	public void handleChange(CmDelinquencyProcess changedBusinessEntity, DataTransferObject<CmDelinquencyProcess> oldDTO) {
		String status = changedBusinessEntity.getStatus();
		ListFilter<CmDelinquencyProcessTriggerEvent> listFilter = changedBusinessEntity.getTriggerEvents().createFilter(" where this.id.businessObjectStatus.id.status = :boStatus and this.statusDateTime is null order by this.id.sequence desc", "CmDelinquencyProcess_CHandler");
		listFilter.bindStringProperty("boStatus", BusinessObjectStatus.properties.status, status);
		CmDelinquencyProcessTriggerEvent cmDelinquencyProcessTriggerEvent = listFilter.firstRow();
		if (notNull(cmDelinquencyProcessTriggerEvent)) {
			CmDelinquencyProcessTriggerEvent_DTO cmDelinquencyProcessTriggerEvent_DTO = cmDelinquencyProcessTriggerEvent.getDTO();
			cmDelinquencyProcessTriggerEvent_DTO.setStatusDateTime(getProcessDateTime());
			cmDelinquencyProcessTriggerEvent.setDTO(cmDelinquencyProcessTriggerEvent_DTO);
		}
		super.handleChange(changedBusinessEntity, oldDTO);
	}

	@Override
	public ValidationRule[] getValidationRules() {
		return helper.getValidationRules();
	}
}

