/*
 **************************************************************************
 *                                                               
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Delinquency Process characteristic Change Handler
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
import com.splwg.base.api.characteristic.CharacteristicHelper;

/**
 * @author VINODW
 *
@ChangeHandler (entityName = cmDelinquencyProcessCharacteristic)
 */
public class CmDelinquencyProcessCharacteristic_CHandler extends AbstractChangeHandler<CmDelinquencyProcessCharacteristic> {
	@Override
	public void handleAddOrChange(CmDelinquencyProcessCharacteristic businessEntity, DataTransferObject<CmDelinquencyProcessCharacteristic> oldDTO) {
		CharacteristicHelper.reformatAdhocValue(businessEntity, oldDTO);
		CharacteristicHelper.updateSearchCharacteristicValue(businessEntity);
	}
}
