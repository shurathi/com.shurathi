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
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:                  Reason:
 * 2020-06-17   JRaymu               CB-108. Initial Version. 
 * 2020-07-30	IGarg			   Updated Version for CB-145
 ***********************************************************************
 */
package com.splwg.cm.domain.payment.algorithm;

import com.splwg.base.api.datatypes.DateFormat;
import com.splwg.base.domain.common.currency.Currency_Id;

public class CmPreProcessPaymentInterfaceMatchingRulesConstants {

    public static final String PAY_UPLOAD_GROUP = "CM-PayUpload";
    public static final String EXTERNAL_SOURCE_ID = "externalSourceId";
    public static final String PAYOR_PRIM_ACCT_IDENT = "payorPrimaryAccountIdentifier";
    public static final String TENDER_AMOUNT = "tenderAmount";
    public static final String CURRENCY = "currency";
    public static final String TENDER_TYPE = "tenderType";
    public static final String ACCOUNTING_DATE = "accountingDate";
    public static final String PAY_UPLOAD_LIST = "paymentUpld";
    public static final String PAYMENT_AMOUNT = "paymentAmount";
    public static final String MATCH_TYPE = "matchType";
    public static final String MATCH_VALUE = "matchValue";
    public static final String PAYMENT_MATCH_INPT_GRP = "paymentMatchingInput";
    public static final String BILL_ID = "billId";
    public static final String ORDER_NUM = "orderNumber";
    public static final String ACCT_IDENTIFIERS_LIST = "accountIdentifiers";
    public static final String PRIM_ACCT_IDENT = "primaryAccountIdentifier";
    public static final String LOAN_NUMBER = "loanNumber";
    public static final String CUSTOMER_NAME = "customerName";
    public static final String CUSTOMER_ADDRESS_GRP = "customerAddress";
    public static final String ADDRESS1 = "address1";
    public static final String ADDRESS2 = "address2";
    public static final String ADDRESS3 = "address3";
    public static final String ADDRESS4 = "address4";
    public static final String CITY = "city";
    public static final String COUNTY = "county";
    public static final String STATE = "state";
    public static final String POSTAL = "postal";
    public static final String COUNTRY = "country";
    public static final String CUSTOMER_ID = "customerId";
    public static final DateFormat dateFormat = new DateFormat("yyyy-MM-dd");
    public static final Currency_Id DEFAULT_CURRENCY = new Currency_Id("USD");
    
    /*
     * Code Added By Ishita on 24-07-2020 - start
     *  Code Added for CB-145
     */
   
    public static final String BILL_IDS_LIST = "bills";
    public static final String ORDER_NUM_LIST = "orderNumbers";
    public static final String LOAN_NUM_LIST = "loanNumbers";
    public static final String RECEIPT_NUM = "receiptNumber";
    public static final String ADHOC_CHAR_VAL = "adhocCharacteristicValue";
    public static final String PAY_CHAR_LIST = "paychar";
    public static final String SEQUENCE = "sequence";
    public static final String CHAR_TYPE = "characteristicType";
    public static final String SRCH_CHAR_VAL = "searchCharVal";
    public static final String SEQ10 = "10";
    
    
    // Code Added By Ishita on 24-07-2020 - end
    
    /*
     * Code Added for CB-287 Start
     */
    
    public static final String REQUEST_TYPE = "requestType";
    public static final String CREATION_DTTM = "creationDateTime";
    
    /*
     * Code Added for CB-287 End
     */

    private CmPreProcessPaymentInterfaceMatchingRulesConstants() {

    }

}
