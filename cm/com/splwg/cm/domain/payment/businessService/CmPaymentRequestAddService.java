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
 * This program will retrieve allow external systems to
 * add a payment to ORMB using real-time REST API.
 *
 * 
 **************************************************************************
 * 
 * CHANGE HISTORY:
 *
 * Date:        by:        Reason:
 * YYYY-MM-DD   IN         Reason text. 
 * 2020-05-05   DDejes     CB-49. Initial.
 * 2020-06-03	DDejes	   CB-101. Added Bseg Match Type
 **************************************************************************
 */

package com.splwg.cm.domain.payment.businessService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.COTSInstanceList;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.Lookup;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.lookup.CharacteristicTypeLookup;
import com.splwg.base.api.service.DataElement;
import com.splwg.base.api.service.ItemList;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.characteristicType.CharacteristicType_Id;
import com.splwg.base.domain.common.currency.Currency_Id;
import com.splwg.ccb.api.lookup.ExternalTypeLookup;
import com.splwg.ccb.api.lookup.MatchTypeEntityFlgLookup;
import com.splwg.ccb.domain.admin.matchType.MatchType_Id;
import com.splwg.ccb.domain.billing.bill.Bill_Id;
import com.splwg.ccb.domain.billing.billSegment.BillSegment_Id;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.payment.paymentRequest.PaymentRequest_Id;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;
import com.splwg.cm.domain.common.messageRepository.CmMessages;
import com.splwg.cm.domain.payment.businessComponent.CmPaymentServiceData;
import com.splwg.shared.common.ApplicationError;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;

/**
 * @author DDejes
 *
 * @PageMaintenance (secured = false, service = CMPAYADDSERV, modules = {},
 *      body = @DataElement (contents = { @DataField (name = TXN_ID)
 *                              , @DataField (name = PAY_REQ_ID)
 *                              , @DataField (name = PAY_EVENT_ID)
 *                              , @DataField (name = ACCT_ID)
 *                              , @DataField (name = EXT_TYPE_FLG)
 *                              , @DataField (name = PAYMENT_DT)
 *                              , @DataField (name = ERROR_MESSAGE)
 *                              , @DataField (name = WARNING_MSG)
 *        , @FieldGroup (name = paymentTenderRequest,
 *                        contents = { @DataField (name = C1_TNDR_CURR_LBL)
 *                        			 , @DataField (name = TENDER_AMT)
 *                                   , @FieldGroup (name = creditCardDetails,
 *                        							contents = { @DataField (name = ENTITY_NAME_SRCH)
 *                        			   						   , @DataField (name = C1_ID_VALUE)
 *                        			  						   , @DataField (name = NUMBER_LBL)
 *                        			   						   , @DataField (name = CARD_TYPE_FLG)
 *                        			   						   , @DataField (name = CC_EXPIRE_DT)
 *                        			                           , @DataField (name = TRANSACTION_ID_LBL)	
 *                        			                           , @DataField (name = RECEIPT_NUM)})
 *                        			, @FieldGroup (name = directDebitDetails,
 *                        						   contents = { @DataField (name = ENTITY_NAME)
 *                        			   						  , @DataField (name = BILL_RTG)
 *                        			   						  , @DataField (name = ACCOUNT_NBR)})})
 *          , @ListField (name = MATCH_TYPES)}),                     
 *      actions = {"change"},
 *      lists = { @List (name = MATCH_TYPES, size = 999,
 *                  body = @DataElement (contents = { @DataField (name = MATCH_TYPE_CD)
 *                  			, @DataField (name = MATCH_VALUE)
 *                              , @DataField (name = PAY_AMT)}))})
 */

public class CmPaymentRequestAddService
extends CmPaymentRequestAddService_Gen {

	// Work Variables
	private DataElement dataElement;
	private String transactionId;
	private Account_Id accountId;
	private Lookup paymentType;
	private Date paymentDate;
	private String tenderCurrency;
	private Money tenderAmount;
	private String nameOnCard;
	private String tokenId;
	private String lastFourDigits;
	private Lookup creditCardType;
	private String expiryDate;
	private String ccTransactionId;
	private String receiptNbr;
	private String nameOnAccount;
	private String bankRoutingNumber;
	private String customerAccountNumber;
	private String billId;
	private Money paymentAmount;
	private Money totalPayAmt;
	private ItemList<DataElement> matchTypeList;
	

	
	// Constants
	private static final String BLANK_STRING = "";
	private static final String SS_BO = "CM-SelfServiceIntegration";
	private static final String PR_BO = "CM-PaymentRequest";
	private static final String SEMICOLON = ";";
	private static final String ZERO = "0";
	private static final String TEN = "10";
	private static final String TWENTY = "20";
	private static final String THIRTY = "30";
	private static final String FORTY = "40";
	private static final String FIFTY = "50";
	
	//Start Add CB101
	private static final String BILL = "Bill";
	private static final String BILL_SEGMENT = "Bill Segment";
	private static final String FK_BILL = "C1-BILL";
	private static final String FK_ACCOUNT = "C1-ACCT";
	private ArrayList<CmPaymentServiceData> paymentMatchTypeList = new ArrayList<CmPaymentServiceData>();
	// End Add CB101
	
	/**
	 * Main Processing method for Change
	 * 
	 * @param dataElement Data Element
	 */
	protected void change(DataElement dataElement) throws ApplicationError {
		this.dataElement = dataElement;
		retrieveInputValues();
		
		validate();
		
		if(dataElement.get(STRUCTURE.ERROR_MESSAGE).isEmpty()){			
			// Create Payment Request
			createPayRequestBO();
		}
		// Override the base result for change
		setOverrideResultForChange(dataElement);

	}
	private void validate(){
		if(isNull(accountId.getEntity())){
			dataElement.put(STRUCTURE.ERROR_MESSAGE,
					CmMessageRepository.getServerMessage(CmMessages.ACCT_ID_INVALID, accountId.getIdValue()).getMessageText());
			return;
		}
		if(totalPayAmt.getAmount().toString().compareTo(tenderAmount.getAmount().toString()) != 0){
			dataElement.put(STRUCTURE.ERROR_MESSAGE
					,CmMessageRepository.getServerMessage(CmMessages.TENDER_AMT_NOT_MATCH, tenderAmount.getAmount().toString()).getMessageText());
			return;
		}
		if(paymentType.compareTo(ExternalTypeLookup.constants.CREDIT_CARD_WITHDRAWAL) == 0){
			if(isBlankOrNull(nameOnCard) || isBlankOrNull(tokenId) || isBlankOrNull(lastFourDigits) || isNull(creditCardType) ||
					isBlankOrNull(expiryDate) || isBlankOrNull(transactionId) || isBlankOrNull(receiptNbr)){
				dataElement.put(STRUCTURE.ERROR_MESSAGE
						,CmMessageRepository.getServerMessage(CmMessages.INC_CC_DETAILS, BLANK_STRING).getMessageText());
				return;
			}
		}

		if(paymentType.compareTo(ExternalTypeLookup.constants.SAVINGS_WITHDRAWAL) == 0 || 
				paymentType.compareTo(ExternalTypeLookup.constants.CHECKING_WITHDRAWAL) == 0){
			if(isBlankOrNull(nameOnAccount) || isBlankOrNull(bankRoutingNumber) || isBlankOrNull(customerAccountNumber)){
				dataElement.put(STRUCTURE.ERROR_MESSAGE
						,CmMessageRepository.getServerMessage(CmMessages.INC_DD_DETAILS, BLANK_STRING).getMessageText());
				return;
			}
		}
		// Retrieve Pay Id 
		retrievePayId();
	}
	/**
	 * Retrieve Pay Id
	 */
	private void retrievePayId(){
		StringBuilder strBuilder = new StringBuilder();
		
		strBuilder.append("SELECT P.PAY_ID FROM CI_PAY P, CI_PAY_EVENT PE ");
		strBuilder.append("WHERE P.PAY_EVENT_ID = PE.PAY_EVENT_ID ");
		strBuilder.append("AND PE.PAY_DT = :payDt ");
		strBuilder.append("AND P.PAY_AMT = :payAmount ");
		strBuilder.append("AND P.ACCT_ID = :acctId ");

		
		PreparedStatement retPayPS = createPreparedStatement(strBuilder.toString(), "Retrieve Pay Id");
		retPayPS.setAutoclose(false);
		retPayPS.bindDate("payDt", paymentDate);
		retPayPS.bindMoney("payAmount", tenderAmount);
		retPayPS.bindId("acctId", accountId);
		retPayPS.execute();
		
		List<SQLResultRow> resRowList = retPayPS.list();
		 
		retPayPS.close();
		
		if(!resRowList.isEmpty()){
			// Add Error if Payment is already existing
			dataElement.put(STRUCTURE.ERROR_MESSAGE
					,CmMessageRepository.getServerMessage(CmMessages.PAY_EXIST, paymentDate.toString(),
					tenderAmount.getAmount().toString(), accountId.getIdValue()).getMessageText());
			return;
		}
	}
	
	/**
	 * Create Payment Request
	 */
	private void createPayRequestBO(){
		String tenderTypeString = BLANK_STRING;
		String currentMatchValue = BLANK_STRING;
		Lookup paymentTypeLookup;
		DataElement matchTypeElements;
		COTSInstanceList paymentDistributionList;
		//Start Add CB-101
		String matchType = BLANK_STRING;
		String matchValue = BLANK_STRING;
		//End Add CB-101
		
		// Read Self Service Master Config
		BusinessObjectInstance ssBoInstance = BusinessObjectInstance.create(SS_BO);
		ssBoInstance.set("bo", SS_BO);
		ssBoInstance = BusinessObjectDispatcher.read(ssBoInstance);
		COTSInstanceNode paymentInfoGroup = ssBoInstance.getGroup("paymentInfo");
		String paymentRequestType = paymentInfoGroup.getString("paymentRequestType");
		Lookup paymentMode = paymentInfoGroup.getLookup("paymentMode");
		// Start Change CB-101
		//String matchByBillMatchType = paymentInfoGroup.getString("matchByBillMatchType");
		//MatchType_Id matchTypeId = new MatchType_Id(matchByBillMatchType);
		COTSInstanceList matchTypesListFromBO = ssBoInstance.getList("matchTypes");		
		// End Change CB-101

		// Retrieve Characteristic Types
		CharacteristicType_Id creditCardTokenCharacteristicType = new CharacteristicType_Id(paymentInfoGroup.getString("creditCardTokenCharacteristicType"));
		CharacteristicType_Id creditCardTypeCharacteristicType = new CharacteristicType_Id(paymentInfoGroup.getString("creditCardTypeCharacteristicType"));
		CharacteristicType_Id creditCardLastFourDigitsCharacteristicType = new CharacteristicType_Id(paymentInfoGroup.getString("creditCardLastFourDigitsCharacteristicType"));
		CharacteristicType_Id creditCardExpiryDateCharacteristicType = new CharacteristicType_Id(paymentInfoGroup.getString("creditCardExpiryDateCharacteristicType"));
		CharacteristicType_Id receiptNumberCharacteristicType = new CharacteristicType_Id(paymentInfoGroup.getString("receiptNumberCharacteristicType"));

		
		// Create Payment Request BO
		BusinessObjectInstance prBoInstance = BusinessObjectInstance.create(PR_BO);
		prBoInstance.set("bo", PR_BO);
		prBoInstance.set("boStatus", "DRAFT");
		prBoInstance.getFieldAndMD("requestType").setXMLValue(paymentRequestType);
		COTSInstanceNode paymentTenderRequestGroup = prBoInstance.getGroup("businessObjectDataArea");
		paymentTenderRequestGroup.getFieldAndMD("payorAccountId").setXMLValue(accountId.getIdValue());
		paymentTenderRequestGroup.getFieldAndMD("payDate").setXMLValue(paymentDate.toString());
		prBoInstance.getFieldAndMD("paymentModeFlg").setXMLValue(paymentMode.value());
		COTSInstanceList paymentTenderRequestList = prBoInstance.getList("paymentTenderRequest");
		COTSInstanceList paymentTypesList =  ssBoInstance.getList("paymentTypes");

		for(COTSInstanceListNode paymentTypes : paymentTypesList){
			paymentTypeLookup = paymentTypes.getLookup("paymentType");
			if(paymentType.compareTo(paymentTypeLookup) == 0){
				tenderTypeString = paymentTypes.getString("tenderType").trim();
			}			
		}

		COTSInstanceListNode payTenderRequest = paymentTenderRequestList.newChild();
		payTenderRequest.getFieldAndMD("tenderType").setXMLValue(tenderTypeString);
		payTenderRequest.getFieldAndMD("payorAccountId").setXMLValue(accountId.getIdValue());
		payTenderRequest.getFieldAndMD("tenderCurrency").setXMLValue(tenderCurrency);
		payTenderRequest.set("tenderAmount", tenderAmount);

		if(paymentType.compareTo(ExternalTypeLookup.constants.SAVINGS_WITHDRAWAL) == 0 || 
				paymentType.compareTo(ExternalTypeLookup.constants.CHECKING_WITHDRAWAL) == 0){
			payTenderRequest.set("externalReferenceId", bankRoutingNumber.concat(SEMICOLON).concat(customerAccountNumber));
			payTenderRequest.set("externalAccountId", customerAccountNumber);
			payTenderRequest.set("name", nameOnAccount);
		}

		if(paymentType.compareTo(ExternalTypeLookup.constants.CREDIT_CARD_WITHDRAWAL) == 0){
			payTenderRequest.set("externalReferenceId", ccTransactionId);
			payTenderRequest.set("name", nameOnCard);
			COTSInstanceList charList = payTenderRequest.getList("characteristicsList");
			addCharacteristics(creditCardTokenCharacteristicType, tokenId, charList, TEN);
			addCharacteristics(creditCardTypeCharacteristicType, creditCardType.value(), charList, TWENTY);
			addCharacteristics(creditCardLastFourDigitsCharacteristicType, lastFourDigits, charList, THIRTY);
			addCharacteristics(creditCardExpiryDateCharacteristicType, expiryDate, charList, FORTY);
			addCharacteristics(receiptNumberCharacteristicType, receiptNbr, charList, FIFTY);
		}	
		// Start Delete CB-101
		//COTSInstanceList paymentMatchTypeList = prBoInstance.getList("paymentMatchType");
		//COTSInstanceListNode paymentMatchTypeNode;		
		// End Delete CB-101
		
		// For every Match Type List from Input
		if(notNull(matchTypeList)){
			for(Iterator<DataElement> matchTypeIterator = matchTypeList.iterator(); matchTypeIterator.hasNext();){
				matchTypeElements = matchTypeIterator.next();
				// Start Change CB-101
				//billId = matchTypeElements.get(STRUCTURE.list_MATCH_TYPES.BILL_ID).trim();

				// Create Payment Match Type List on the Payment Request BO
				//paymentMatchTypeNode = paymentMatchTypeList.newChild();
				//paymentMatchTypeNode.getFieldAndMD("matchType").setXMLValue(matchByBillMatchType);
				//paymentMatchTypeNode.getFieldAndMD("matchTypeEntityId").setXMLValue(billId);
				//paymentMatchTypeNode.getFieldAndMD("matchTypeEntityType").setXMLValue(matchTypeId.getEntity().getMatchTypeEntityType());
				//paymentMatchTypeNode.getFieldAndMD("foreignKeyValue").setXMLValue(accountId.getIdValue());
				matchType = matchTypeElements.get(STRUCTURE.list_MATCH_TYPES.MATCH_TYPE_CD).trim();
				matchValue = matchTypeElements.get(STRUCTURE.list_MATCH_TYPES.MATCH_VALUE).trim();
				paymentAmount  = matchTypeElements.get(STRUCTURE.list_MATCH_TYPES.PAY_AMT);


				//Check if Valid Match Type
				if(isValidMatchType(matchType, matchTypesListFromBO).isFalse()){
					//addError
					dataElement.put(STRUCTURE.ERROR_MESSAGE,
							CmMessageRepository.getServerMessage(CmMessages.MATCH_TYPE_NOT_SUPPORTED, matchType,matchValue).getMessageText());
					return;
				}	
				
				// Add Payment Match Type List
				addPaymentMatchTypeList(matchType, matchValue, paymentAmount);

				if(!dataElement.get(STRUCTURE.ERROR_MESSAGE).isEmpty()){
					return;
				}
				// End Change CB-101
			}
		}
		
		// Start Add CB-101
        COTSInstanceList payMatchTypeListBO = prBoInstance.getList("paymentMatchType");
        ArrayList<String> existingAccountList = new ArrayList<String>();


        for(CmPaymentServiceData paymentMatchTypeListNode : paymentMatchTypeList){
        	if(paymentMatchTypeListNode.getEntityFlag().compareTo(MatchTypeEntityFlgLookup.constants.BILL_SEGMENT.value()) == 0){
        		if (!existingAccountList.contains(paymentMatchTypeListNode.getMatchEntityId())) {
        			existingAccountList.add(paymentMatchTypeListNode.getMatchEntityId());                    
        			// Create Payment Match Type List on the Payment Request BO
        			createMatchTypeList(payMatchTypeListBO, paymentMatchTypeListNode);
        		}
        	} else {
                // Create Payment Match Type List on the Payment Request BO
            	createMatchTypeList(payMatchTypeListBO, paymentMatchTypeListNode);
            }

        }
        // End Add CB-101
		
		// Add BO
		prBoInstance = BusinessObjectDispatcher.add(prBoInstance);
		// Read BO Again
		// Start Change CB-101
		//paymentMatchTypeList = prBoInstance.getList("paymentMatchType");
		payMatchTypeListBO = prBoInstance.getList("paymentMatchType");
		// End Change CB-101

		// Start Delete CB-101
		//if(notNull(matchTypeList)){
		//	for(Iterator<DataElement> matchTypeIterator = matchTypeList.iterator(); matchTypeIterator.hasNext();){
		//		matchTypeElements = matchTypeIterator.next();
		//		billId = matchTypeElements.get(STRUCTURE.list_MATCH_TYPES.BILL_ID).trim();
		//		paymentAmount  = matchTypeElements.get(STRUCTURE.list_MATCH_TYPES.PAY_AMT);
      
		//		for(COTSInstanceListNode paymentMatchTypes : paymentMatchTypeList){
		//			paymentDistributionList = paymentMatchTypes.getList("paymentDistribution");
		//			for(COTSInstanceListNode paymentDist : paymentDistributionList){
		//				currentMatchValue = paymentDist.getString("matchValue").trim();
		//				if(currentMatchValue.compareTo(billId) == 0){
		//					// Set Payment Amount to the Bill Id specified on the Input
		//					paymentDist.set("paymentAmount", paymentAmount);
		//				}
		//			}
		//		}
		//	}
		//}
		// End Delete CB-101

		// Start Add CB-101
		Bool isMatchFound = Bool.FALSE;
		String matchValueString = BLANK_STRING;
		String matchTypeString = BLANK_STRING;
		// Distribute Payment
		for(CmPaymentServiceData paymentMatchTypeListNode : paymentMatchTypeList){
			isMatchFound = Bool.FALSE;
			for(COTSInstanceListNode payment : payMatchTypeListBO){
				paymentDistributionList = payment.getList("paymentDistribution");
				for(COTSInstanceListNode payDistributionNode : paymentDistributionList){
					if(payment.getString("matchTypeEntityId").compareTo(paymentMatchTypeListNode.getMatchEntityId()) == 0
					   && payDistributionNode.getString("matchValue").compareTo(paymentMatchTypeListNode.getMatchValue()) == 0){
						payDistributionNode.set("paymentAmount", paymentMatchTypeListNode.getPaymentAmount());
						isMatchFound = Bool.TRUE;
					}else{
						matchValueString = paymentMatchTypeListNode.getMatchValue();
						matchTypeString = paymentMatchTypeListNode.getMatchType();
					}
				}
			}
			if(isMatchFound.isFalse()){
				dataElement.put(STRUCTURE.ERROR_MESSAGE,
						CmMessageRepository.getServerMessage(CmMessages.MATCH_VALUE_NOT_IN_PAY_DIST_LIST
								, matchValueString, matchTypeString).getMessageText());
				return;
			}
		}
		// End Add CB-101

		//Retrieve Payment Request Id
		PaymentRequest_Id payReqId = new PaymentRequest_Id(prBoInstance.getString("request"));

		//Update BO status to Distribute and Freeze
		prBoInstance.getFieldAndMD("boStatus").setXMLValue("DISTANDFREEZ");
		BusinessObjectDispatcher.update(prBoInstance.getDocument());


		//Retrieve Payment Event Id
		String paymentEventId = payReqId.getEntity().getPaymentEventId();

		//Retrieve Status
		String status = payReqId.getEntity().getStatus();

		//Set Payment Request Id and Status
		dataElement.put(STRUCTURE.PAY_REQ_ID, payReqId);
		dataElement.put(STRUCTURE.PAY_EVENT_ID, paymentEventId);
		if(status.compareTo("DEFFERDISTR") == 0){
			// Set Warning Message if payment is Deferred
			dataElement.put(STRUCTURE.WARNING_MSG, CmMessageRepository.getServerMessage(CmMessages.PAY_CR_DEFERRED).getMessageText());
		}

	}
	
	// Start Add CB-101
	/**
	 * Check if Valid Match Type
	 * @param matchType
	 * @param matchTypesListFromBO
	 * @return
	 */
	private Bool isValidMatchType(String matchType, COTSInstanceList matchTypesListFromBO){
		for(COTSInstanceNode matchTypesNode : matchTypesListFromBO){
			if(matchTypesNode.getString("matchType").trim().compareTo(matchType) == 0){
				return Bool.TRUE;
			}
		}
		return Bool.FALSE;
	}
	
	/**
	 * Add Payment Match Type List
	 * @param matchType
	 * @param matchValue
	 * @param paymentAmount
	 */
	private void addPaymentMatchTypeList(String matchType, String matchValue, Money paymentAmount){
		String matchEntityId = BLANK_STRING;
		String fkValue = BLANK_STRING;
		String entityFlag = BLANK_STRING;
		MatchType_Id matchTypeId = new MatchType_Id(matchType);
		String matchTypeEntityFlag = matchTypeId.getEntity().getMatchTypeEntityFlg();
		String matchTypeEntityType = matchTypeId.getEntity().getMatchTypeEntityType();

		if(matchTypeEntityFlag.trim().compareTo(MatchTypeEntityFlgLookup.constants.BILL.value()) == 0){
			Bill_Id billId = new Bill_Id(matchValue);
			if(isNull(billId.getEntity())){
				//addError
				dataElement.put(STRUCTURE.ERROR_MESSAGE,
						CmMessageRepository.getServerMessage(CmMessages.MATCH_ENTITY_ID_INVALID, BILL ,matchValue).getMessageText());
				return;
			}else{
				matchEntityId = billId.getIdValue();
				fkValue = FK_BILL;
				entityFlag = MatchTypeEntityFlgLookup.constants.BILL.value();
			}
		}else if(matchTypeEntityFlag.trim().compareTo(MatchTypeEntityFlgLookup.constants.BILL_SEGMENT.value()) == 0){
			BillSegment_Id bsegId = new BillSegment_Id(matchValue);
			if(isNull(bsegId.getEntity())){
				//addError
				dataElement.put(STRUCTURE.ERROR_MESSAGE,
						CmMessageRepository.getServerMessage(CmMessages.MATCH_ENTITY_ID_INVALID, BILL_SEGMENT ,matchValue).getMessageText());
				return;
			}else{
				matchEntityId = bsegId.getEntity().getServiceAgreement().getAccount().getId().getIdValue();
				fkValue = FK_ACCOUNT;
				entityFlag = MatchTypeEntityFlgLookup.constants.BILL_SEGMENT.value();
			}
		}

		CmPaymentServiceData payServiceData = new CmPaymentServiceData();
		payServiceData.setMatchType(matchType);
		payServiceData.setMatchEntityId(matchEntityId);
		payServiceData.setMatchEntityType(matchTypeEntityType);
		payServiceData.setMatchValue(matchValue);
		payServiceData.setPaymentAmount(paymentAmount);
		payServiceData.setFkValue(fkValue);
		payServiceData.setEntityFlag(entityFlag);
		paymentMatchTypeList.add(payServiceData);
		
	}
	
	/**
	 * Create Match Type List
	 * @param paymentMatchTypeNode
	 * @param payMatchTypeListBO
	 * @param ctr
	 */
	private void createMatchTypeList(COTSInstanceList payMatchTypeListBO, CmPaymentServiceData paymentMatchTypeListNode){
		
		COTSInstanceListNode paymentMatchTypeNode = payMatchTypeListBO.newChild();
         paymentMatchTypeNode.getFieldAndMD("matchType").setXMLValue(paymentMatchTypeListNode.getMatchType());
         paymentMatchTypeNode.getFieldAndMD("matchTypeEntityId").setXMLValue(paymentMatchTypeListNode.getMatchEntityId());
         paymentMatchTypeNode.getFieldAndMD("matchTypeEntityType").setXMLValue(paymentMatchTypeListNode.getMatchEntityType());
         paymentMatchTypeNode.getFieldAndMD("foreignKeyValue").setXMLValue(paymentMatchTypeListNode.getFkValue());

	}
	// End Add CB-101
	
	/**
	 * Add Credit Card Characteristics
	 * @param charTypeId
	 * @param charVal
	 * @param charList
	 * @param sequence
	 */
	private void addCharacteristics(CharacteristicType_Id charTypeId, String charVal, COTSInstanceList charList, String sequence){
		CharacteristicTypeLookup charType = charTypeId.getEntity().getCharacteristicType();
		if(notNull(charTypeId)){
			COTSInstanceListNode charListNode = charList.newChild();

			charListNode.getFieldAndMD("characteristicType").setXMLValue(charTypeId.getIdValue());
			charListNode.getFieldAndMD("sequence").setXMLValue(sequence);
			if(charType.equals(CharacteristicTypeLookup.constants.ADHOC_VALUE)){
				charListNode.set("adhocCharacteristicValue", charVal);
			}else if(charType.equals(CharacteristicTypeLookup.constants.FOREIGN_KEY_VALUE)){
				charListNode.set("characteristicValueForeignKey1", charVal);
			}else if(charType.equals(CharacteristicTypeLookup.constants.PREDEFINED_VALUE)){
				charListNode.set("characteristicValue", charVal);
			}
		}
	}
	
	/**
	 * Retrieve Input Values
	 * 
	 */
	private void retrieveInputValues(){
		
		DataElement matchTypeElement;
		transactionId = dataElement.get(STRUCTURE.TXN_ID).trim();
		accountId = new Account_Id(dataElement.get(STRUCTURE.ACCT_ID).trim());
		paymentType = dataElement.get(STRUCTURE.EXT_TYPE_FLG);
		paymentDate = dataElement.get(STRUCTURE.PAYMENT_DT);
		tenderCurrency = dataElement.get(STRUCTURE.C1_TNDR_CURR_LBL).trim();
		totalPayAmt = new Money(ZERO, new Currency_Id(tenderCurrency));
		tenderAmount = dataElement.get(STRUCTURE.TENDER_AMT);
		nameOnCard = dataElement.get(STRUCTURE.ENTITY_NAME_SRCH).trim();
		tokenId = dataElement.get(STRUCTURE.C1_ID_VALUE).trim();
		lastFourDigits = dataElement.get(STRUCTURE.NUMBER_LBL).trim();
		creditCardType = dataElement.get(STRUCTURE.CARD_TYPE_FLG);
		expiryDate = dataElement.get(STRUCTURE.CC_EXPIRE_DT).trim();
		ccTransactionId = dataElement.get(STRUCTURE.TRANSACTION_ID_LBL).trim();
		receiptNbr = dataElement.get(STRUCTURE.RECEIPT_NUM).trim();
		nameOnAccount = dataElement.get(STRUCTURE.ENTITY_NAME).trim();
		bankRoutingNumber = dataElement.get(STRUCTURE.BILL_RTG).trim();
		customerAccountNumber = dataElement.get(STRUCTURE.ACCOUNT_NBR).trim();
		
		matchTypeList = dataElement.getList(STRUCTURE.list_MATCH_TYPES.name);
		if(notNull(matchTypeList)){
			for(Iterator<DataElement> matchTypeIterator = matchTypeList.iterator(); matchTypeIterator.hasNext();){
				matchTypeElement = matchTypeIterator.next();
				paymentAmount  = matchTypeElement.get(STRUCTURE.list_MATCH_TYPES.PAY_AMT);
				totalPayAmt = totalPayAmt.add(paymentAmount);
				
			}
		}
	}

}