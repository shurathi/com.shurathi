/*                                                                
 *******************************************************************************************************************                                                                
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Hold the Delinquency Process Constants File
 *                                                             
 *******************************************************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                          
 * 2020-05-06   MugdhaP		Initial Version : ANTHM-340 CAB1-9462 Delinquency Framework
 * ******************************************************************************************************************
 */
package com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.algorithm;

import java.math.BigInteger;

public class CmHoldDelinquencyProcessConstants {

	public static final BigInteger MESSAGE_CATEGORY = new BigInteger("90000");
	public static final BigInteger MESSAGE_NUMBER = new BigInteger("51430");

	public final static StringBuilder FETCH_EXISTING_DEL_PROC_LOG = new StringBuilder()
			.append(" FROM CmDelinquencyProcessLog DPL WHERE DPL.id.delinquencyProcess.id = :delProcId AND DPL.status = :boStatus ")
			.append(" AND DPL.messageId.messageCategoryId = :msgCategoryNumber AND DPL.messageId.messageNumber = :msgNumber ")
			.append(" AND DPL.characteristicTypeId = :charType AND DPL.characteristicValue = :charValue ");
}

