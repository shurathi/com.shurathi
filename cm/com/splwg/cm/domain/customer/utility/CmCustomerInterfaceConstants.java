/*
 ***********************************************************************
 *                Confidentiality Information:
 *
 * This module is the confidential and proprietary information of
 * RIA Advisory LLC; it is not to be copied, reproduced, or
 * transmitted in any form, by any means, in whole or in part,
 * nor is it to be used for any purpose other than that for which
 * it is expressly provided without the written permission of
 * RIA Advisory LLC.
 ***********************************************************************
 *
 * PROGRAM DESCRIPTION:
 *
 * Process Customer Interface
 *
 * This class have all constants that are required for customer interface
 *   - Person
 *   - Account
 *   - Contract
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:                  Reason:
 * 2020-04-13   VLaksh              CB-10. Initial Version. 
 * 2020-04-23	DIVA				CB-9. Added new Constants. 
 * 2020-04-28	DIVA				CB-9. Added new Constants. 
 * 2020-04-29   VLaksh				CB-10. Added new Constants.
 * 2020-05-20	DDejes				CB-75. Added new Constants.
 * 2020-06-01	DDejes				CB-86. Added new Constants.
 * 2020-06-26   JFerna              CB-133. Added new Constants.
 * 2020-07-01   DDejes              CB-132. Added new Constants.
 * 2020-07-07   JFerna              CB-176. Added new Constants.
 * 2020-07-06   JFerna              CB-52. Added new Constants.
 * 2020-07-21   JFerna              CB-177. Added new Constants.
 * 2020-07-27   JFerna              CB-256. Added new Constants.
 * 2020-07-31	KGhuge				CB-54. Capture Statement Construct during Customer Interface
 ***********************************************************************
 */
package com.splwg.cm.domain.customer.utility;

public class CmCustomerInterfaceConstants {

	public static final String BLANK_VALUE = " ";
	public static final String INBOUNDMESSAGEID_ELE = "c1InboundMessage";
	public static final String MESSAGE_ELE ="message";
	public static final String MESSAGE_DATA_ELE = "messageData";
	public static final String MAIN_CUSTOMER_ELE="mainCustomer";
	public static final String EFFECTIVE_DATE_ELE="effectiveDate";
	public static final String DIVISION_ELE="division";
	public static final String EMAIL_ELE ="email";
	public static final String BIRTH_DT_ELE ="birthDate";
	public static final String PERSON_TYPE_ELE ="personType";
	public static final String ADDRESS_ELE = "address";
	public static final String ADDRESS1_ELE = "address1";
	public static final String ADDRESS2_ELE = "address2";
	public static final String ADDRESS3_ELE = "address3";
	public static final String ADDRESS4_ELE = "address4";
	public static final String CITY_ELE = "city";
	public static final String STATE_ELE = "state";
	public static final String ZIP_ELE = "zip";
	public static final String COUNTY_ELE = "county";
	public static final String COUNTRY_ELE = "country";
	public static final String IDENTIFIERS_ELE="identifiers";
	public static final String ID_ELE="id";
	public static final String ID_TYPE_ELE="idType";
	public static final String ID_VALUE_ELE="idValue";
	public static final String IS_PRIMARY_ELE="isPrimary";
	public static final String  NAME_ELE = "name";
	public static final String  PERSONTYPE_ELE = "personType";
	public static final String  CHARACTERISTICS_ELE = "characteristics";
	public static final String  CHARACTERISTIC_ELE = "characteristic";
	public static final String  EFFECTIVEDATE_ELE = "effectiveDate";
	public static final String  CHARACTERISTICTYPE_ELE = "characteristicType";
	public static final String  CHARACTERISTICVALUE_ELE = "characteristicValue";
	public static final String  PHONES_ELE = "phones";
	public static final String  PHONE_ELE = "phone";
	public static final String  PHONETYPE_ELE = "phoneType";
	public static final String  PHONEVALUE_ELE = "phoneValue";
	public static final String  PHONEEXTENSION_ELE = "phoneExtension";
	public static final String  PERSONS_ELE = "persons";
	public static final String  PERSON_ELE = "person";
	public static final String  PERSONPERSONRELATIONSHIPTYPE_ELE = "personPersonRelationshipType";
	public static final String  SEQNO_ELE = "seqNo";
	public static final String  PRIMARYPERSONIDTYPE_ELE = "primaryPersonIdType";
	public static final String  PRIMARYPERSONIDVALUE_ELE = "primaryPersonIdValue";
	public static final String  RELATIONSHIPSTARTDATE_ELE = "relationshipStartDate";
	public static final String  RELATIONSHIPENDDATE_ELE = "relationshipEndDate";
	public static final String  FINANCIALRELATIONSHIPSWITCH_ELE = "financialRelationshipSwitch";
	public static final String  ACCOUNTS_ELE = "accounts";	
	public static final String  ACCOUNT_ELE = "account";
	public static final String  CURRENCY_ELE = "currency";
	public static final String  BILLAFTER_ELE = "billAfter";
	public static final String  SETUPDATE_ELE = "setUpDate";
	public static final String  CUSTOMERCLASS_ELE = "customerClass";
	public static final String  ISMAINCUSTOMER_ELE = "isMainCustomer";
	public static final String  ISFINANCIALLYRESPONSIBLE_ELE = "isFinanciallyResponsible";
	public static final String  SHOULDRECEIVECOPYOFBILL_ELE = "shouldReceiveCopyOfBill";
	public static final String  NUMBEROFBILLCOPIES_ELE = "numberOfBillCopies";
	public static final String  RECEIVESNOTIFICATION_ELE = "receivesNotification";
	public static final String  ACCOUNTUSAGETYPE_ELE = "accountUsageType";
	public static final String  BILLADDRESSSOURCE_ELE = "billAddressSource";
	public static final String  BILLFORMAT_ELE = "billFormat";
	public static final String  BILLROUTETYPE_ELE = "billRouteType";
	public static final String  ACCOUNTPERSONS_ELE = "accountPersons";
	public static final String  ACCOUNTRELATIONSHIPTYPE_ELE = "accountRelationshipType";
	public static final String  STARTDATE_ELE = "startDate";
	public static final String  ENDDATE_ELE = "endDate";
	public static final String  EXTERNALACCOUNTID_ELE = "externalAccountId";
	public static final String  APAYSOURCECODE_ELE = "autopaySourceCode";
	public static final String  APAYROUTETYPE_ELE = "autopayRouteType";
	public static final String  AUTOPAYMAXWITHDRAWALAMOUNT_ELE = "autopayMaxWithdrawalAmount";
	public static final String  EXPMONTH_ELE = "expMonth";
	public static final String  EXPYEAR_ELE = "expYear";
	public static final String  ENTITYNAME_ELE = "entityName";
	public static final String  AUTOPAY_ELE = "autopay";
	public static final String  ACCOUNTAUTOPAY_ELE = "accountAutopay";
	public static final String  MAINCUSTOMER_ELE = "mainCustomer";
	public static final String  MESSAGEDATA_ELE = "messageData";
	public static final String  PERSONID_ELE = "personId";
	public static final String  ACCOUNTID_ELE = "accountId";
	public static final String  PERSONID1_ELE = "personId1";	
	public static final String CUSTOMERSEGMENT_ELE ="customerSegment";
	public static final String CUSTOMERTIER_ELE="customerTier";

	public static final String EXT_CUSTOMERINFO_ELE="customerInfo";
	public static final String EXT_PERSONTYPE_ELE="personType";
	public static final String EXT_PRIMARYPERSONIDTYPE_ELE="primaryPersonIdType";
	public static final String EXT_ACCESSGROUP_ELE="accessGroup";
	public static final String EXT_PRIMARYNAMETYPE_ELE="primaryNameType";
	public static final String EXT_PHONETYPE_ELE="customerInfo";
	public static final String EXT_DIVISION_ELE="division";
	public static final String EXT_INVOICECURRENCY_ELE="invoiceCurrency";
	public static final String EXT_RECEIVECOPYOFBILL_ELE="receiveCopyOfBill";
	public static final String EXT_ACCOUNTRELATIONSHIPTYPE_ELE="accountRelationshipType";
	public static final String EXT_ADDRESSSOURCE_ELE="addressSource";
	public static final String EXT_BILLFORMAT_ELE="billFormat";
	public static final String EXT_BILLROUTETYPE_ELE="billRouteType";
	public static final String EXT_PRIMARYACCTIDTYPE_ELE="primaryAcctIdType";
	public static final String EXT_BILLCYCLE_ELE="billCycle";
	public static final String EXT_COLLECTIONCLASS_ELE="collectionClass";
	public static final String EXT_CONTRACTTYPES_ELE="contractTypes";
	public static final String EXT_CONTRACTTYPE_ELE="contractType";
	public static final String EXT_AUTOPAYTYPE_ELE="autopayType";
	public static final String EXT_AUTOPAYMETHOD_ELE="autopayMethod";

	public static final String PRIORITYNUMBER_ELE="priorityNumber";
	public static final String PERCENT_ELE="percent";
	public static final String AUTOPAYTYPE_ELE="autopayType";
	public static final String AUTOPAYMETHOD_ELE="autopayMethod";

	public static final String ERRORLIST_ELE="errorList";
	public static final String ERROR_ELE="error";
	public static final String MESSAGECATEGORY_ELE="messageCategory";
	public static final String MESSAGENBR_ELE="messageNbr";
	public static final String MESSAGETEXT_ELE="messageText";
	public static final String HASFINANCIALRELALATIONSHIP_ELE="hasFinancialRelalationship";

	//CB 9 START
	public static final String  MESSAGEHEADER_ELE = "messageHeader";
	public static final String  EXTERNALTRANSACTIONID_ELE = "externalTransactionId";
	public static final String  PRIM_ID_PARAM = "Primary Identifier";
	public static final String  ACCT_PRIM_ID_PARAM = "Primary Account Identifier";
	public static final String  APAY_START_DT_PARAM = "Auto Pay Start Date";
	public static final String  APAY_END_DT_PARAM = "Auto End Start Date";
	public static final String  MESSAGECATOGERY_ELE = "messageCategory";
	public static final String  ACCT_IDENTIFIERS_PARM = "Account Identifiers";
	public static final String  AUTO_PAY_DATE_COMB_PARM = "Auto Pay Start Date and End Date combination";	
	public static final String  PRIM_ID_VAL_PARAM = "Primary Identifier Value";
	public static final String  DIVISION_PARAM = "Division";
	public static final String  ADDRESS1_PARAM = "Address 1";
	public static final String  CITY_PARAM = "City";
	public static final String  STATE_PARAM = "State";
	public static final String  COUNTRY_PARAM = "Country";
	public static final String  ACCOUNT_LIST_PARAM = "List of accounts";
	public static final String  CUSTOMER_CL_PARAM = "Customer Class";
	public static final String  ACCOUNT_PER_LIST_PARAM = "List of account persons";
	public static final String  ACCOUNT_REL_TYPE_PARAM = "Account Relationship Type";
	public static final String  BILL_ROUTE_TYPE_PARAM = "Bill Route Type";
	public static final String  APAY_SRC_CD_PARAM = "Auto Pay Source Code";
	public static final String  EXT_ACCT_ID_PARAM = "Auto Pay External Account Id";
	public static final String  EXP_MONTH_PARAM = "Auto Pay Expiry Month";
	public static final String  EXP_YEAR_PARAM = "Auto Pay Expiry Year";
	public static final String  ENTITY_NAME_PARAM = "Auto Pay Entity Name";
	public static final String  APAY_RTE_TYPE_PARAM = "Auto Pay Route Type";
	public static final String  MSG_DATA_PARAM = "Message Data";
	public static final String  MAIN_CUST_PARAM = "Main Customer";
	
	//CB 9 END
	
	//Start Add CB-75
	public static final String TRUE = "true";
	public static final String EXT_RECEIVENOTIF_ELE="receiveNotification";
	//End Add CB-75
	
	//Start Add CB-86
	public static final String ALERT_TYPE = "alertType";
	public static final String ACCT_ALERT = "accountAlert";
	//End Add CB-86
	
	//Start Add - CB-133
	public static final String PERSONID2_ELE = "personId2";
	//End Add - CB-133

	//Start Add - CB-132
	public static final String DEFAULT_CHARS_ELE = "defaultChar";
	public static final String DEFAULT_CHAR_ELE = "characteristic";
	public static final String CHAR_TYPE = "characteristicType";
	public static final String CHAR_VAL = "characteristicValue";
	public static final String CHAR_ENT = "characteristicEntity";
	public static final String BO_ACTION_FLG = "boActionFlag";
	public static final String BO_NEXT_STATUS = "nextStatus";
	//End Add - CB-132
	
	//Start Add - CB-176
	public static final String BO_STATUS_CD_ELE = "boStatus";
	public static final String LINK_STATUS = "LINK";
	//End Add - CB-176
	
	//Start Add - CB-52
	public static final int PARM_VAL_IDX_ADDR_BILLTO_IND_CHAR_TYPE = 2;
	//Start Change - CB-256
	//public static final int PARM_VAL_IDX_ADDR_SHPTO_IND_CHAR_TYPE = 7;
	public static final int PARM_VAL_IDX_ADDR_SHPTO_IND_CHAR_TYPE = 6;
	//End Change - CB-256
	public static final int PARM_SETUP_IDX_ADDR_BILLTO_IND_CHAR_TYPE = 5;
	//Start Change - CB-256
	//public static final int PARM_SETUP_IDX_ADDR_SHPTO_IND_CHAR_TYPE = 10;
	public static final int PARM_SETUP_IDX_ADDR_SHPTO_IND_CHAR_TYPE = 9;
	//End Change - CB-256
	public static final String ADDRESSES_ELE = "addresses";
	public static final String ADDRESSES_ID_ELE = "addressId";
	public static final String ADDRESS_ENTITIES_ELE = "addressEntities";
	public static final String ADDRESS_ENTITY_ELE = "addressEntity";
	public static final String ADDRESS_TYPE_ELE = "addressType";
	public static final String ENTITY_ID_ELE = "entityId";
	public static final String ENTITY_FLG_ELE = "entityFlg";
	public static final String ADD_ACTION = "ADD";
	public static final String UPDATE_ACTION = "UPDATE";
	public static final String PRIMARY = "PRIMARY";
	public static final String ADDRESS_BO = "C1-Address";
	public static final String ADDRESS_ENTITIES_BO_ELE = "entities";
	public static final String ADDRESS_ENTITY_BO_ELE = "addressEntity";
    public static final String POSTAL_BO_ELE = "postal";
	public static final String ENTITY_TYPE_BO_ELE = "entityType";
	public static final String COLL_ENTITY_ID_BO_ELE = "collectionEntityId";
	public static final String ADDRESS_TYPE_CD_BO_ELE = "addressTypeCd";
	public static final String ADDRESS_CHARS_BO_ELE = "addressCharacteristic";
	public static final String ADHOC_VAL_BO_ELE = "adhocCharacteristicValue";
	public static final String CHAR_VAL_FK1_BO_ELE = "characteristicValueForeignKey1";
	public static final String BO_ELE = "bo";
	public static final String REQUEST_BO_ELE = "request";
	public static final String CONTRACTS_ELE = "contracts";
	public static final String CONTRACT_ELE = "contract";
	public static final String SA_ID_ELE = "saId";
	//End Add - CB-52
	
	//Start Add - CB-177
	public static final String SRCH_CHAR_VAL_BO_ELE = "searchCharacteristicValue";
	public static final int PARM_AUD_IDX_ADDR_BILLTO_CHAR_TYPE = 0;
	public static final int PARM_AUD_IDX_ADDR_SHIPTO_CHAR_TYPE = 2;
	public static final int PARM_AUD_IDX_ADDR_STM_CHAR_TYPE = 4;
	public static final int PARM_AUD_IDX_ADDR_STM_CHAR_VAL = 5;
	//End Add - CB-177
	
	//Start Add - 54
	public static final String STATEMENTS_ELE = "statement";
	public static final String DESCRIPTION_ELE = "description";
	public static final String NBROFCOPIES_ELE = "numberOfCopies";
	public static final String STATEMENT_ADDRESS_SOURCE_ELE = "statementAddressSource";
	public static final String STATEMENT_CYCLE_ELE = "statementCycle";
	public static final String STATEMENT_FORMAT_ELE = "statementFormat";
	public static final String STATEMENT_ROUTE_TYPE_ELE = "statementRouteType";
	public static final String STATEMENT_CNST_DETAILS_ELE = "statementConstructDetails";
	public static final String STATEMENT_CNST_DETAIL_ELE = "statementConstructDetail";
	public static final String PRINT_ORDER_ELE = "printOrder";
	public static final String STATEMENT_DESCR_ELE = "statementDescription";
	public static final String STATEMENT_DETAIL_ELE = "statementDetailType";
	public static final String STATEMENT_DETAIL_DESC_ELE = "statementDetailDescription";
	public static final String EFF_STATUS_ELE = "effectiveStatus";
	public static final String CNST_DETAIL_TYPE_ELE = "constructDetailType";
	public static final String NBR_OF_COPIES = "nbrOfCopies";
	public static final String STATEMENT_CNST_ID_ELE = "statementConstructId";
	public static final String STATEMENT_PRINT_DESC_ELE = "statementPrintDescription";
	// End Add - CB-54
	
	/*
	 * Queries
	 */

	public static final String  GET_PER_BY_PRIM_ID  = " SELECT PER_ID FROM CI_PER_ID WHERE "
			+ " ID_TYPE_CD=:idType AND PER_ID_NBR=:personIdNumber AND PRIM_SW=:true " ;

	public static final String  GET_PER_BY_ID = " SELECT PER_ID FROM CI_PER_ID WHERE "
			+ " ID_TYPE_CD=:idType AND PER_ID_NBR=:personIdNumber  " ;

	public static final String GET_ACCT_BY_PRIM_ID =" SELECT ACCT_ID FROM CI_ACCT_NBR WHERE ACCT_NBR_TYPE_CD=:idType AND ACCT_NBR=:accountNumber "
			+ " AND PRIM_SW=:true";

	public static final String GET_ACCT_BY_ID =" from AccountNumber a where a.id.accountIdentifierType=:idType "
			+ " and a.accountNumber=:accountNumber ";

	//Start Add - CB-52
	public static final String GET_PER_BY_EMAIL  = " SELECT PER_ID FROM CI_PER WHERE EMAILID=:email " ;
	public static final String GET_ADDRESS_ID  = " SELECT ADDRESS_ID FROM C1_ADDRESS_ENTITY WHERE ENTITY_ID=:personId " ;
	//End Add - CB-52

	private CmCustomerInterfaceConstants(){

	}


}
