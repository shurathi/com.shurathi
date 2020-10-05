/*                                                               
 *******************************************************************************************************                                                               
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Define constants
 *                                                          
 *******************************************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:    	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 * 2019-06-18   NIMYAKAL	Initial Version. 
 * *********************************************************************************************************
 * */
package com.splwg.cm.domain.delinquency.utils;

import com.splwg.base.domain.common.characteristicType.CharacteristicType_Id;

public class CmPolicyConstants {
	public static final String TERMINATION_INFO_GROUP_ELEMENT = "terminationInformation";
	public static final String TERMINATION_DATE_ELEMENT = "terminateDate";
	public static final String TERMINATION_REASON_ELEMENT = "terminateReason";
	public static final String TEMINATED_STATUS = "TERMINATED";
	
	public final static StringBuilder CHECK_ACTIVE_POLICY_LIST = new StringBuilder(
            "SELECT 'X' FROM CI_POLICY_PER PP, CI_POLICY P, F1_BUS_OBJ_STATUS_OPT OPT ")
            .append(" WHERE PP.PER_ID = :personId AND PP.MAIN_CUST_SW = 'Y' ")
            .append(" AND ((PP.END_DT >= :processDate AND PP.END_DT IS NOT NULL) OR PP.END_DT IS NULL ) ")
            .append(" AND P.POLICY_ID = PP.POLICY_ID AND P.BUS_OBJ_CD = OPT.BUS_OBJ_CD AND P.BO_STATUS_CD = OPT.BO_STATUS_CD ")
            .append(" AND OPT.BO_OPT_FLG  = :optionType ")
            .append(" AND OPT.SEQ_NUM = (SELECT MAX (OPT2.SEQ_NUM) FROM F1_BUS_OBJ_STATUS_OPT OPT2 ")
            .append(" WHERE OPT2.BUS_OBJ_CD = OPT.BUS_OBJ_CD AND OPT2.BO_STATUS_CD = OPT.BO_STATUS_CD AND OPT2.BO_OPT_FLG = OPT.BO_OPT_FLG ) ")
            .append(" AND ((OPT.BO_OPT_VAL  = :activeOptionVal) OR (OPT.BO_OPT_VAL = :terminatedOptionVal ")
            .append(" AND P.END_DT > :processDate)) ");
}
