/*                                                                
 **************************************************************************                                                                
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Constants file for Write Off Reason Code Cash Posting Tolerance
 *                                                             
 **************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text                               
 * 2020-05-06   MugdhaP		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework       
 **************************************************************************
 */
/**
 * 
 */
package com.splwg.cm.domain.delinquency.admin.customerClass;

/**
 * @author MugdhaP
 *
 */
public class CmCashPostingToleranceWriteOffConstants {
	
	public static final String REQUEST_TYPE="requestType";
	public static final String PARAMETERS_INFO_GROUP_ELEMENT = "parameters";
	public static final String ADJUSTMENT_TYPE="adjustmentType";
	public static final String BO_ELEMENT_NAME = "bo";
	public static final String ACCOUNT="accountId";
	public static final String CREATE_DATE_TIME="createDateTime";
	public static final String REQUEST="request";
	public static final String BUSINESS_OBJ_STATUS="boStatus";
	public static final String BUSINESS_OBJ_STATUS_RSN="statusReason";
	public static final String TOLERANCE_PER_THRESHOLD="Tolerance Percentage Threshold";
	public static final String TOLERANCE_AMT_THRESHOLD="Tolerance Amount Threshold";
	public static final String WRITE_OFF_REQUEST_CANCELLED_STATUS="W/O Request Cancelled Status";
	
	public static final StringBuilder COUNT_ACCOUNTS_LINKED_TO_PAYMENTS= new StringBuilder()
	.append(" from Payment pay ,PaymentEvent pe ")
	.append(" where pay.paymentEvent.id=pe.id ")
	.append(" AND pe.id=:paymentEventId ")
	.append(" AND pay.paymentStatus IN (:incomplete,:error,:freezable,:frozen ) ");
	
	public static final StringBuilder RETRIEVE_OPEN_FT_FROM_BILL=new StringBuilder()
	.append("  SELECT   FT.SIBLING_ID ")
	.append(" , FT.SA_ID ")
	.append(" , FT.FT_TYPE_FLG ")
	.append(" , FT.CURRENCY_CD ")
	.append(" , FT.CUR_AMT ENTITYAMOUNT ")
	.append(" , CASE WHEN FT.MATCH_EVT_ID = ' ' THEN FT.CUR_AMT ")
	.append(" ELSE (SELECT SUM(FT2.CUR_AMT) ")
	.append(" FROM CI_FT FT2 ")
	.append(" WHERE FT2.MATCH_EVT_ID = FT.MATCH_EVT_ID ")
	.append(" AND FT2.FREEZE_SW = 'Y' ")
	.append(" ) END AS WRITEOFFAMOUNT ")
	.append(" FROM CI_FT FT ")
	.append(" WHERE FT.BILL_ID = :billID ")
	.append(" AND FT.FREEZE_SW = 'Y' ")
	.append(" AND FT.CUR_AMT <> 0 ")
	.append(" AND (FT.MATCH_EVT_ID = ' ' ")
	.append(" OR (SELECT MEVT.MEVT_STATUS_FLG ")
	.append(" FROM CI_MATCH_EVT MEVT ")
	.append(" WHERE  MEVT.MATCH_EVT_ID = FT.MATCH_EVT_ID) <> 'B') ");
}

