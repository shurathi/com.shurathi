/*
 **************************************************************************
 *                                                                                                                           
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Delinquency characteristic Change Handler
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
import com.splwg.base.api.DataTransferObject;
import com.splwg.base.api.changehandling.AbstractChangeHandler;
import com.splwg.base.api.characteristic.CharacteristicHelper;


/**
 * @author VINODW
 *
@ChangeHandler (entityName = cmDelinquencyProcessTypeCharacteristic)
 */
public class CmDelinquencyProcessTypeCharacteristic_CHandler extends AbstractChangeHandler<CmDelinquencyProcessTypeCharacteristic> {
	@Override
	public void handleAddOrChange(CmDelinquencyProcessTypeCharacteristic businessEntity, DataTransferObject<CmDelinquencyProcessTypeCharacteristic> oldDTO) {
		CharacteristicHelper.reformatAdhocValue(businessEntity, oldDTO);
		CharacteristicHelper.updateSearchCharacteristicValue(businessEntity);
	}
}
