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
 * This class is for RMB message customization.
 * 
 **************************************************************************
 * 
 * CHANGE HISTORY:
 *
 * Date:        by:        Reason:
 * YYYY-MM-DD   IN         Reason text. 
 * 2020-04-13   JFerna     Initial.
 * 2020-04-16   DSekar	   CB-25 
 * 2020-04-16	DDejes	   CB-37
 * 2020-04-17	KGhuge	   CB-24
 * 2020-04-17   DSekar	   CB-9
 * 2020-04-21   VLaksh     CB-10
 * 2020-04-28	DDejes	   CB-36 
 * 2020-04-30   JRaymu     CB-33 
 * 2020-05-05 	DSekar     CB-09 
 * 2020-05-05	DDejes	   CB-49
 * 2020-05-08	DSekar	   CB-25 
 * 2020-05-08   DDejes	   CB-36
 * 2020-05-11   JFerna	   CB-53 
 * 2020-05-12	DSekar	   CB-25 
 * 2020-05-14   JFerna	   CB-59 
 * 2020-05-15	DDejes	   CB-73
 * 2020-05-19   JFerna     CB-61
 * 2020-05-25   JFerna     CB-91
 * 2020-05-27	DDejes	   CB-97
 * 2020-05-27	DDejes	   CB-101
 * 2020-05-27   SPatil     CB-55
 * 2020-06-28   SKusum     CB-89
 * 2020-06-10	DDejes	   CB-108
 * 2020-06-11   SPatil     CB-127
 * 2020-06-15   JFerna     CB-94
 * 2020-06-29   KChan      CB-159
 * 2020-07-13	DDejes	   CB-157
 * 2020-07-14   JFerna     CB-52
 * 2020-07-15	SPatil	   CB-163
 * 2020-07-28	ShrutikaA  CB-224
 * 2020-07-30 	Ishita	   CB-145
 * 2020-08-05   JFerna     CB-177
 * 2020-08-11	ShrutikaA  CB-227
 * 2020-08-12   JFerna     CB-239
 * 2020-08-12	Shreyas	   CB-302
 * 2020-08-14	Shreyas	   CB-270
 * 2020-08-21   Ishita     CB-274
 * 2020-08-28	KGhuge	   CB-283	
 * 2020-09-01	ShrutikaA  CB-264
 * 2020-09-08   JFerna     CB-380
 * 2020-09-24   AMusal     CB-39
 * 2020-09-24	ShrutikaA  CB-444
 **************************************************************************
 */
package com.splwg.cm.domain.common.messageRepository;

import com.splwg.base.api.changehandling.ServerMessageParameter;
import com.splwg.base.api.changehandling.ServerMessageTemplate;
import com.splwg.base.domain.common.message.AbstractMessageRepository;
import com.splwg.base.domain.common.message.MessageCategory_Id;
import com.splwg.base.domain.common.message.MessageParameters;
import com.splwg.shared.common.ServerMessage;

/**
 * This class defines messages for Pricing.
 */
public class CmMessages extends AbstractMessageRepository {
    /**
     * String constants
     */
    public static final int MESSAGE_CATEGORY = 92000;
    private static CmMessageRepository instance;
    
  //Manage Customer (10000 - 19999)
    // Strat Add CB-9
    public static final int  NOTFOUND_FOR_TRANSNSID  = 10000;
    public static final int  MULTI_PERSONS_ID_TYPE_VALUE = 10001;
    public static final int  MULTI_PRIM_TRUE = 10002;
    public static final int  END_DATE_GREATER_START_DATE = 10003;
    public static final int  INAVLD_FOR_TRANSNSID = 10004;
    public static final int  MISSING_FOR_TRANSNSID = 10005;
	public static final int  INVALID_STATE_COUNTRY_COMBINATION = 10006;
    // End Add CB-9


    //Start Add - CB-37
    public static final int INVALID_LOOKUP_VALUE = 10036;
    public static final int BATCH_PROC_ERROR = 10037;
    //End Add - CB-37
	
	//Start Add - CB-10
    public static final int PERSON_ENTITY_CREATED = 11001;
    public static final int INVALID_BO_NEXT_STATUS = 11002;
    public static final int ACCOUNT_ENTITY_CREATED = 11003;
    public static final int CUST_INTF_EXT_LUKUP_MISSING_BY_DIV = 11004;
    public static final int CUST_INTF_EXT_LUKUP_MISSING_BY_CUSTCLS = 11005;
    public static final int MAIN_CUST_NOT_EXISTS = 11006;
    //End Add - CB-10
    
    //Start Add - CB-25
    public static final int MANDATORY_NODE_MISSING = 40000; 
    public static final int CHAR_TYPE_INVALID = 40001;
    public static final int ENTITY_NOT_VALID = 40004; 	
    public static final int ACCT_NOT_MATCH_TYPE = 40005;
    public static final int UNABLE_TO_FIND_ACCT = 40006; 
    public static final int MULTIPLE_ACCT_NBR = 40007; 
    //End Add - CB-25
    
	//Start Add - CB-24
	public static final int NUMBER_OF_RECORDS_NOT_MATCHED=40003;
    //End End Add - CB-24
    
	//Start Add - CB-36
	public static final int INVALID_ENTITY = 30000;
	public static final int NO_ACCOUNT_ID_RETRIEVED = 30001;
	public static final int NO_CONTRACT_TYPE_RETRIEVED = 30002;
	public static final int NO_CONTRACT_RETRIEVED = 30003;
	public static final int JSON_FORMAT_ERROR = 30005;
	public static final int NO_SQI = 30006;
	//End Add - CB-36

	//Start Add - CB-87
	public static final int NO_ACTIVE_STATEMENT_CONSTRUCT_EXISTS = 30101;
	public static final int INVALID_CUSTOMER_IDENTIFIER_TYPE = 30102;
	public static final int NO_OPEN_BILLS_FOR_STATEMENT_CONSTRUCT = 30103;
	//End Add - CB-87
	//Satrt Add - CB-89
	public static final int INVALID_CHAR_TYPE=30104;
	//End Add - CB-89
    // Start Add - CB-33
    public static final int MULT_CONT_ACTIVE = 50000;
    public static final int CONT_NOT_FOUND = 50001;
    public static final int BATCH_RUN_TREE_ERR = 50002;

    // End Add - CB-33
	
	//Start Add- CB-49
    public static final int ACCT_ID_INVALID = 40010;
    public static final int TENDER_AMT_NOT_MATCH = 40011;
    public static final int INC_CC_DETAILS = 40012;
    public static final int INC_DD_DETAILS = 40013;
    public static final int PAY_EXIST = 40014;
    public static final int PAY_CR_DEFERRED = 40015;
    //End Add- CB-49
	
	//Start Add - CB-53
	public static final int  ACCOUNT_NOT_FOUND_FOR_ID_TYPE_VALUE = 10007;
	public static final int  ACCOUNT_NOT_FOUND_FOR_PERSONID1 = 10008;
	//End Add - CB-53
	
	//Start Add - CB-59
	public static final int  CUST_NUM_ID_TYPE_NOT_FOUND = 10009;
	//End Add - CB-59
	
	//Start Add -CB-73
    public static final int MORE_THAN_ONE_CONTRACT = 30007;
    //End Add -CB-73
	
    //Start Add - CB-61
    public static final int ACCOUNT_NOT_EXISTS = 11007;
    //End Add - CB-61
	
	//Start Add - CB-91
	public static final int ACCOUNT_PERSON_MUST_HAVE_ONE_MAIN_CUST_ = 10010;
	//End Add - CB-91
	
	   //Start Add - CB-55
	 public static final int INVALID_FILEPATH=60100;
	 public static final int INVALID_DIRECTORY=60101;
	 public static final int INVALID_EXTENSION=60102;
	 public static final int MISS_FEAT_CONFIG=60103;
	 public static final int MISS_FEAT_OPT=60104;
	 public static final int FILE_ALREADY_EXISTS=60105;
	 public static final int COMMON_MESSAGE_IO_FILE_ERROR=60106; 
     public static final int ERROR_UPDATING_STATUS = 60107;
   
    //End Add -  CB-55
	
	//Start Add - CB-227
	public static final int OPEN_IO_FILE_ERROR = 40201;
	//End Add - CB-227
	
	
	//Start Add- CB-97
    public static final int PERSON_UPDATED = 11008;
    public static final int ACCOUNT_UPDATED = 11009;
    //End Add- CB-97
	
	//Start Add - CB-101
    public static final int MATCH_TYPE_NOT_SUPPORTED = 40310;
    public static final int MATCH_ENTITY_ID_INVALID = 40311;
    public static final int MATCH_VALUE_NOT_IN_PAY_DIST_LIST = 40312;
    //End Add - CB-101
	
	//Start Add - CB-127
	public static final int INVALID_ACCT_REL_TYPE_CD = 60108;
	//End Add - CB-127
	
	//Start Add - CB-108
    public static final int MATCH_RULE_PARM_REQUIRED = 40320;
    public static final int NO_SUSPENSE_CONTRACT_FOUND = 40322;
    //End Add - CB-108
	
	//Start Add - CB-94
    public static final int INVALID_DATE_FORMAT = 30012;
    public static final int CHAR_VAL_INVALID_FOR_CHAR_TYPE = 30013;
    public static final int NO_EXT_LOOKUP_CONFIG_FOR_CUST_CL = 30014;
    public static final int ERROR_PARSING_DATE = 30015;
    public static final int ERROR_INVOKING_BS = 30016;   
    //End Add - CB-94
	
    //Start Add - CB-159
    protected static final int MISSING_CUSTOMER_CLASS = 30201;
    protected static final int LEGACY_BILL_ID_MISSING = 30202;
	public static final int MISSING_CHAR_ENTITY = 30203;
    protected static final int ACCT_PRICEITEM_COMB_INCORRECT = 30204;
	protected static final int NO_ACCOUNT_EXISTS = 30205;
    public static final int BILL_GEN_LOG = 30206;
    //End Add - CB-159
    
    //Start Add - CB-163
    public static final int UNMATCHED_PRICEPARAM=30008;
    public static final int BILLABLECHARGE_EXISTS=30009;
    public static final int NO_PERSON_ID_RETRIEVED = 30010;
    //End Add -CB - 163
	
	//Start Add-CB-157
    public static final int FILE_EXT_NOT_XML = 30105;
    public static final int NO_UNMATCHED_ITEMS = 30113;
	public static final int NO_STATEMENT_CONST_DET = 30114;
    //End Add-CB-157
	
	//Start Add - CB-52
    public static final int PRIM_SHIP_TO_ADDRESS_MISSING = 10011;
    public static final int ADDR_INFO_FOR_MAIN_CUST_MISSING = 10012;
    public static final int MULT_PRIM_BILL_TO_ADDRESS_FOUND = 10013;
    public static final int PRIM_BILL_TO_ADDRESS_MISSING = 10014;
    public static final int MULT_PRIM_SHIP_TO_ADDRESS_FOUND = 10015;
    public static final int PRIM_ID_AND_EMAIL_MISSING = 10016;
    public static final int MULT_PERSON_FOR_EMAIL_FOUND = 10017;
    public static final int CHAR_TYPE_INVALID_FOR_ENTITY = 10018;
    //End Add - CB-52

    //Start Add - CB-177
    public static final int NO_PRIM_BILL_TO_ADDRESS_WARNING = 10019;
    public static final int NO_PRIM_SHIP_TO_ADDRESS_WARNING = 10020;
    public static final int NO_STATEMENT_INDICATOR_ADDRESS_WARNING = 10021;
    public static final int NO_PRIM_BILL_TO_ADDRESS_ERROR = 10022;
    public static final int EXISTING_PRIM_BILL_TO_ADDRESS_ERROR = 10023;
    public static final int EXISTING_PRIM_SHIP_TO_ADDRESS_ERROR = 10024;
    public static final int EXISTING_PRIM_STM_IND_ADDRESS_ERROR = 10025;
    public static final int BOTH_PARM_MUST_BE_PROVIDED = 10026;
    //End Add - CB-177
	
	 //Start Add - CB-224	
	public static final int NO_SEGMENT_VALUE_FOUND = 60110;
    public static final int NO_VALUE_FOUND = 60111;
    public static final int NO_CHAR_FOUND = 60112;
	 //End Add - CB-224	
	 
	 // Start Add CB-253
	  public static final int INVALID_DIRECTORY_OR_FILE_NOT_EXIST = 70000;
      public static final int INVALID_FILE_NAME=70001;
	 // End CB-253
	 
      //Start CB-302 changes
      public static final int ACCT_NOT_FOUND = 20000;
      public static final int PERS_NOT_FOUND = 20001;
      public static final int PLST_NOT_FOUND = 20002;
      //End CB-302 changes
	  
	  //Start CB-270 changes
      public static final int PITM_CHAR_ENTITY_NOT_FOUND = 30122;
      //End CB-270 changes
	 
	 /*
	 * Code Added By Ishita start
	 * Code Added for CB-145
	 */
	public static final int ENTITY_DOES_NOT_EXIST = 40330;
	/*
	 * Code Added By Ishita CB-145 end
	 */
	 
	//Start Add - CB-239
    public static final int NO_PRIM_SHIP_BILL_TO_ADDRESS_FOUND = 30121;
    //End Add - CB-239
	 
    //Start Add CB-276
	public static final int MISSING_FIELD = 50013;
	public static final int UNABLE_TO_DETERMINE_EXCESS_CREDIT_CONTRAT_TYPE = 50014;
	//End Add CB-276
	
	 //Start Add - CB-274
		public static final int TO_DO_ENTRY_CREATED = 50010;
    //End Add - CB-274
	
	//Start Add CB-283
	public static final int PRIMARY_ADDRESS_DOES_NOT_EXIST = 30121;
	public static final int EXTERNAL_SYSTEM_ERROR = 100;
	public static final int EMAIL_DOES_NOT_EXISTS = 50018;
	public static final int REQUIRED_ATTRIBUTE_NOT_FOUND = 50019;
	public static final int REQUIRED_ATTRIBUTE_NOT_FOUND_IN_EXTENDABLE_LOOKUP = 50025;
	public static final int UNAPPLIED_PAYMENTS_DO_NOT_EXISTS = 50017;
	public static final int NO_ON_ACCOUNT_CONTRACT_DEFINED = 50016;
	public static final int MESSAGE_SENDER_CONTEXT_NOT_FOUND = 50026;
	public static final int REQUIRED_CONTEXT_TYPE_MISSING = 50027;
	//End Add CB-283
	
	//Start Add - CB-39
	public static final int NEW_ORDER_REQUEST_ERROR = 40331;
	public static final int PROCESS_STATUS_ERROR = 40332;
	//End Add - CB-39
	
	//Start Add - CB-264
    
    public static final int EXPECTED_FT_TYPE_NOT_FOUND = 60120;
    public static final int NO_SEGMENT_CHARACTERISTICS_FOUND = 60121;
    
    //End Add - CB-264
	
	//Start Add - CB-380
	public static final int CANNOT_FT_FREEZE = 60130;
	//End Add - CB-380
	
  	//Start Add CB-393
  	public static final int NO_BILL_SEG_FOUND = 60140;
  	public static final int PRICE_ITEM_IS_NOT_FOUND = 60141;
  	public static final int CHAR_TYPE_IS_NOT_VALID_FOR_ADJ_TYPE = 60142;
  	//End Add CB-393
	
	//Start Add CB-444
  	public static final int CANNOT_FIND_TNDR_SRC_OF_PAYMENT = 60114;
  	public static final int GLSTR_IN_CHARACTERISTIC_CANNOT_BE_FOUND = 60113;
  	//End Add CB-444
	
    private static CmMessageRepository getInstance() {
        if (instance == null) {
            instance = new CmMessageRepository();
        }
        return instance;
    }

    /**
     * constructor method.
     */
    public CmMessages() {
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
