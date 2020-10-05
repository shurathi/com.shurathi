/*                                                               
 ************************************************************************************************************                                                               
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Create To Do Constant File
 * 
 ************************************************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 *           
 * 2020-05-06   MugdhaP		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework
 * 							 
 ************************************************************************************************************
 */

package com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm;

public class CmCreateToDoForDelinquencyConstant {
	public static final String PERSON_ID = "personId";
	public static final String ACCOUNT_ID = "accountId";
	public static final String STATUS_UPDATE_DATE_TIME = "updateStatusDateTime";
	public static final String DELINQUENCY_PROCESS_ID = "delinquencyProcessId";

	public final static StringBuilder RET_PRIM_NAME_AND_COLL_CLASS = new StringBuilder()
			.append(" FROM AccountPerson AP, PersonName PN, CmPersonCollection PC ")
			.append(" WHERE AP.id.account = :accountId ")
			.append(" AND AP.isMainCustomer = 'Y' ")
			.append(" AND PN.id.person = AP.id.person ")
			.append(" AND PN.isPrimaryName = 'Y' ")
			.append(" AP.id.person = PC.id.person ");

}

