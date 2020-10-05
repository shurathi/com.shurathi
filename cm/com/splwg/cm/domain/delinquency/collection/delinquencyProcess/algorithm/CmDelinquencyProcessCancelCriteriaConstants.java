/*                                                                
 ********************************************************************************************                                                                
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Delinquency Calculation 
 * This constant file is created for calculate trigger events
 *                                                             
 ********************************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 * 2020-05-06   MugdhaP		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework     
 * ******************************************************************************************
 */

package com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm;


/**
 * @author MugdhaP
 *
 */
public class CmDelinquencyProcessCancelCriteriaConstants {

	public  final static String CONST_ACTIVE = "ACTIVE";


	public  final static String CONST_CANCELLED = "CANCELLED";
	
	public  final static String CONST_ACTIVE_CANCELLED = "ACTIVE or CANCELLED";
	
	public  final static String TOLRNC_PRCNT = "Tolerance Percentage";
	
	public final static String CONST_B = "B";

	public final static String CONST_D = "D";
	
	public final static String CONST_B_D = "B or D";


	
	public final static StringBuilder FETCH_DELIQ_LVL_ID = new StringBuilder("from CmDelinquencyProcessRelatedObject dpo ")
			.append(" where dpo.id.delinquencyProcess.id = :delinProcId ")
			.append(" and dpo.id.cmDelinquencyRelatedObjTypeFlg = :relatedObjTypeFlag ");
	
	public final static StringBuilder FETCH_ACCT_PER = new StringBuilder(" from AccountPerson ap where ")

		// .append(" and ap.id.account = :accountId ")
		.append(" ap.id.account = :accountId ")

		.append(" and ap.isMainCustomer = 'Y' ");
	
	
	public final static StringBuilder FETCH_BILL_LIST = new StringBuilder("from CmDelinquencyProcessRelatedObject dpo ")
	.append(" where dpo.id.delinquencyProcess.id = :delinProcId ")
	.append(" and dpo.id.maintenanceObject = 'BILL' ")
	.append(" and dpo.id.cmDelinquencyRelatedObjTypeFlg = :relatedObjTypeFlag ");

	public final static StringBuilder FETCH_DLPROC_TRIGG_EVNTS = new StringBuilder(" from CmDelinquencyProcessTriggerEvent te ")
	.append(" where te.id.delinquencyProcess.id =  :deliqProcId ")
	.append(" and te.statusDateTime is null ");
	

	public final static StringBuilder FETCH_BILL_LIST_BASED_ON_DUE_DT = new StringBuilder("from CmDelinquencyProcessRelatedObject dpo, Bill bl ")
		.append(" where dpo.id.delinquencyProcess.id = :delinProcId ")
		.append(" and dpo.id.maintenanceObject = 'BILL' ")
		.append(" and dpo.id.cmDelinquencyRelatedObjTypeFlg = :relatedObjTypeFlag ")
		.append(" and dpo.id.primaryKeyValue1 = bl.id ")
		.append(" and bl.dueDate <= :endOfGraceDueDate ");

}

