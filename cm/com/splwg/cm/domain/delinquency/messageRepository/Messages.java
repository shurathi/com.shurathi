/* 
 **************************************************************************
 *                Confidentiality Information:
 *
 * This module is the confidential and proprietary information of
 * RIA Advisory; it is not to be copied, reproduced, or
 * transmitted in any form, by any means, in whole or in part,
 * nor is it to be used for any purpose other than that for which
 * it is expressly provided without the written permission of
 * RIA Advisory.
 *                 
 **************************************************************************
 * 
 * PROGRAM DESCRIPTION:
 * 
 * This class is for RMB Delinquency message customization.
 * 
 **************************************************************************
 * 
 * CHANGE HISTORY:
 *
 * Date:        by:        Reason:
 * YYYY-MM-DD   IN         Reason text. 
 * 2020-07-17   KChan      CB-150. Delinquency Framework.
 * 2020-08-20   SAnart     CB-286 Transition to Previous Status Algorithm
 * 2020-08-21	KGhuge	   CB-277 Delinquency - Hold Criteria - Account Balance above Threshold Algorithm
 **************************************************************************
 */
package com.splwg.cm.domain.delinquency.messageRepository;

import com.splwg.base.api.changehandling.ServerMessageParameter;
import com.splwg.base.api.changehandling.ServerMessageTemplate;
import com.splwg.base.domain.common.message.AbstractMessageRepository;
import com.splwg.base.domain.common.message.MessageCategory_Id;
import com.splwg.base.domain.common.message.MessageParameters;
import com.splwg.shared.common.ServerMessage;

/**
 * This class defines messages for Pricing.
 */
public class Messages extends AbstractMessageRepository {
    /**
     * String constants
     */
    public static final int MESSAGE_CATEGORY = 90000;
    private static MessageRepository instance;
    
	// CB-150 Delinquency Framework - Start add
	  public static final int OMR_ALGO_PARM_MISSING = 23002;
	  public static final int OMR_INVALID_CHAR_TYPE_CD = 23004;
	  public static final int OMR_VAL_UNP_AMT_AND_PER_PARM = 23007;
	  public static final int OMR_VAL_PRO_BILL_WITH_UNP_PER = 23010;
	  public static final int OMR_CUST_STATUS_INVALID = 23012;
	  public static final int OPTION_VALUE_IS_INVALID_CUSTOMER_CLASS = 30004;
	  public static final int INVALID_SYS_STMT_DAY = 30927;
	  public static final int MISSING_FEATURE_CONFIG = 34035;
	  public static final int MISSING_FEATURE_CONFIG_OPT_TY = 34036;
	  public static final int MULTIPLE_FEATURE_CONFIG_OPT_TY = 34037;
	  public static final int UNABLE_TO_DET_TERM_DT_RULE = 34101;
	  public static final int MISSING_EOG_CHAR_TYPE = 34104;
	  public static final int CHAR_VALUE_IS_INVALID_FOR_CHAR_TYPE = 40401;
	  public static final int ATLEAST_ONE_PARAMETER_SHOULD_BE_PROVIDED = 40705;
	  public static final int INVALID_STATUS_FOR_POLICY_BUSINESS_OBJECT = 42212;
	  public static final int CAN_CRIT_INV_UNPAID_AMT_PERCT = 50003;
	  public static final int CALCULATE_UNPAID_ORIGINAL_AMT_ALGORITHM_IS_REQUIRED = 50018;
	  public static final int CANCEL_CRITERIA_ALGORITHM_IS_REQUIRED = 50019;
	  public static final int ZERO_OR_MULTIPLE_RECORDS_IN_REL_OBJ = 50025;
	  public static final int MAINT_OBJ_MISMATCH = 50026;
	  public static final int INVALID_CC = 50030;
	  public static final int INVALID_MESSAGE_NUMBER_CATEGORY_COMBINATION = 50031;
	  public static final int INVALID_C_A_VALUE = 50032;
	  public static final int ACCOUNT_AND_PERSON_NOT_FOUND = 50033;
	  public static final int MULTIPLE_DELIQUENCY_PROCESS_TYPE_ALG_FOUND = 50036;
	  public static final int INVALID_STATUS_TRANSITION_CONDITION = 50044;
	  public static final int STATE_OF_ISSUE_NOT_FOUND_ON_POLICY = 50049;
	  public static final int DELINQUENCY_NOT_LINKED_TO_CUSTOMER = 50500;
	  public static final int INVALID_PERCENTAGE_VALUE = 50501;
	  public static final int DELINQUENCY_PROCESS_REQUIRED = 50502;
	  public static final int ERROR_DURING_COMPUTATION = 50503;
	  public static final int NO_PERSON_PHONE_FOUND = 50509;
	  public static final int ALGO_PARM_VALID_VALUES = 51402;
	  public static final int REQ_VALUES_PLUG_IN_SPOT = 51403;
	  public static final int REL_OBJ_REQ_VALUES_PLUG_IN_SPOT = 51404;
	  public static final int DEL_PRO_TYP_PARM = 51405;
	  public static final int DEL_LVL_PARM = 51407;
	  public static final int BILL_ID_REQUIRED_FOR_ALGORITHM = 51408;
	  public static final int BILL_NOT_BELONG_TO_DEL_PROC_FOR_PER = 51409;
	  public static final int BILL_NOT_BELONG_TO_DEL_PROC_FOR_ACCT = 51410;
	  public static final int ARITHMETIC_EXPERSSION_HAS_INVALID_OUTPUT = 51413;
	  public static final int STATE_OF_ISSUE_LIST_NOT_FOUND = 51418;
	  public static final int UNABLE_TO_DETERMINE_TERMINATION_RULE = 51419;
	  public static final int INVALID_CHAR_TYPE = 51420;
	  public static final int TRIGGER_DATE_NOT_RETRIEVED = 51421;
	  public static final int UNPAID_AMOUNT_AND_PERCENTAGE_MISSING = 51432;
	  public static final int CUST_ACCT_INCONSISTANCY = 51701;
	  public static final int BILL_CYC_NT_AVL = 51702;
	  public static final int DLC_CMDL_REQD = 51720;
	  public static final int NO_PERSON_FOUND_FOR_DELINQUENCY_PROCESS = 51802;
	  public static final int ADMIN_CONTRACT_TYPES_FEATURE_CONFIG_MISSING = 50519;
	  public static final int ADMIN_CONTRACT_TYPE_OPTION_TYPE_MISSING = 50520;
	  public static final int TRAN_COND_EVALUATE_DEBT_NOTIFICATION_SENT_STATUS_MISSING = 50521;
	  public static final int NO_PERSON_FOUND_FOR_DEL = 50508;
	  public static final int DEL_BILL_ID_MSG = 51411;
	  public static final int DQCDM_OUTMSG_CREATED = 50515;
	  public static final int DQCDM_OUTMSG_NOT_CREATED = 50516;
	  public static final int CUST_ID_TYPE_NOT_FOUND = 70013;
	  public static final int CUST_NBR_FRM_SRC_SYS_NOT_FOUND = 70014;
	  public static final int MISSING_POLICY_COLLECTION_LOCATION = 50210;
	  public static final int NO_UNPAID_AMOUNT_FOUND = 50511;
	  public static final int NO_UNPAID_AMOUNT_CALC_ALG_FOUND = 50512;
	  public static final int NO_BILLS_FOUND_DQ = 50513;
	  public static final int NO_POLICY_FOUND_DQ = 50514;
	  public static final int MISSING_OFFICE_OF_REGISTRATION = 50206;
	  public static final int MISSING_FROZEN_MARKET_SEGMENT = 50207;
	  public static final int EXCEPTION = 50507;
	  public static final int INVALID_UNPAID_AMT_CALC_VAL = 50506;
	  public static final int INVALID_OUTBOUND_MESSAGE_PROFILE = 50203;
	  public static final int CUST_OR_ACCT_LEVEL_DEL_REQUIRED = 51426;
	  public static final int TO_DO_MESSAGE = 51427;
	  public static final int DEL_PROC_LOG_ON_HOLD = 51430;
	  public static final int PAYMENTS_FROM_REQUEST_CLOB_READ_ERROR = 50504;
	  public static final int INVALID_ENTITY = 50505;
	  public static final int CANNOT_DET_STATE_OF_ISSUE = 34100;
	  public static final int MULTIPLE_FEATURE_CONFIG = 34038;
	  public static final int BUSINESS_OBJECT_MISMATCH = 50028;
	  public static final int INVALID_ROLE_FOR_TODO_TYPE = 5350;
	  public static final int INVALID_ENTITY_TO_CHAR_TYPE = 5300;
	  public static final int CHAR_TYPE_NUMERIC_VALUE = 5304;
	  public static final int MSG_NBR_AND_MSG_ACTEGORY_COMBINATION_INVALID = 307;
	  public static final int DEL_LOG_WRITE_OFF_INITIATED = 51422;
	  public static final int WRITE_OFF_REQUEST_CREATED = 51423;
	  public static final int TERM_EFFECTIVE_DATE_RULE_NOT_RETRIEVED = 51416;
	  public static final int NO_UNPAID_BILL_RETRIEVED = 51417;
	  public static final int FUNDING_AGMT_CHAR_TYPE_MISSING = 50517;
	  public static final int CHARGE_LINE_CHAR_TYPE_MISSING = 50518;
	  public static final int BROKER_ID_NOT_FOUND = 70600;
	  public static final int OUTBOUND_MESSAGE_WAS_CREATED_FOR_SYSTEMATIC_STMT_PROCESS = 70601;
	  public static final int CUST_CONTACT_CREATED_FOR_SYSTEMATIC_STMT_PROCESS = 70602;
	  public static final int CANNOT_DETERMINE_TERMINATION_DATE_ON_DELIN_PROC = 34109;
	  public static final int MONITOR_RULE_ALGORITHM_IS_REQUIRED = 50017;
	  public static final int MULTIPLE_DELIQUENCY_MONITOR_RULE_ALG_FOUND = 50035;
	  public static final int DELINQUENCY_CONTROL_ALGORITHMS_NOT_CONFIGURED = 51401;
	  public static final int INVALID_GROUP_PERSON = 52004;
	  public static final int INVALID_COLLECTION_CLASS = 52005;
	  public static final int DIVISION_MISSING = 5555;
	  public static final int OMR_ALGO_PARM_INVALID = 23003;
	  public static final int OPTION_VALUE_MISSING_FOR_FEATURE_CONFIGURATION = 30002;
	  public static final int INVALID_CHAR_TYPE_FOR_ENTITY = 40201;
	  public static final int CHAR_VALUE_INVALID_FOR_CHAR_TYPE = 40104;
	  public static final int CHAR_TYPE_IS_INVALID_FOR_ENTITY = 40402;
	  public static final int CHAR_VALUE_INVALID = 5473;
	  //Start add CB-281
	  public static final int ACCOUNT_ID_INVALID =6000;
	  public static final int INVALID_COLLECTION_CLASS_ACCOUNT=6001;
	  //End cCB-281
	  
	  //CB-286 Transition to Previous Status Algorithm- Start add
	  public static final int UNABLE_RETURN_TO_PREV_STATUS = 50011;
	  //CB-286 Transition to Previous Status Algorithm- Stop add
	  
	  
	  // CB-150 Delinquency Framework - Stop add
	  
	  //Start Add CB-277
	  public static final  int OVER_DUE_AMOUNT = 51430;
	  //End Add CB-277
    
    private static MessageRepository getInstance() {
        if (instance == null) {
            instance = new MessageRepository();
        }
        return instance;
    }

    /**
     * constructor method.
     */
    public Messages() {
        super(MESSAGE_CATEGORY);
    }

    @Override protected MessageCategory_Id getCategoryId() {
        return super.getCategoryId();
    }

    @Override protected ServerMessageTemplate getDeclarativeMessage(int arg0, ServerMessageParameter[] arg1) {
        return super.getDeclarativeMessage(arg0, arg1);
    }

    @Override protected ServerMessage getMessage(int arg0, MessageParameters arg1) {
        return super.getMessage(arg0, arg1);
    }

    @Override protected ServerMessage getMessage(int arg0) {
        return super.getMessage(arg0);
    }


}
