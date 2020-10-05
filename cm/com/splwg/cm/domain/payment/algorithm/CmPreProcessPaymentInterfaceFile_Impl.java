/*	
 **************************************************************************                                                                
 *                            	     Confidentiality Information:
 *
 * This module is the confidential and proprietary information of
 * RIA Advisory; it is not to be copied, reproduced, or
 * transmitted in any form, by any means, in whole or in part,
 * nor is it to be used for any purpose other than that for which
 * it is expressly provided without the written permission of
 * RIA Advisory.
 *                                    
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * This is a custom file request type pre-processing algorithm 
 * to perform below operations. 
 * -It checks if mandatory fields are populated 
 *  in the Payload as well as for validity of the fields.
 * -Determining the payAccountId and appending it to the Payload.
 * -Determining the CharType and Updating the Element in Payload. 
 * 
 * ************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 *           
 * 2020-04-15   DSekar	Initial Version. 
 * 2020-05-20   SKusum CB-74 Replaced payChar to paychar
 * 2020-07-21	KGhuge	CB-231 Receipt Conversion Logic
 **************************************************************************
 */

package com.splwg.cm.domain.payment.algorithm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import com.splwg.base.api.QueryIterator;
import com.splwg.base.api.lookup.CharacteristicTypeLookup;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.characteristicType.CharacteristicType_Id;
import com.splwg.base.domain.common.currency.Currency_Id;
import com.splwg.ccb.domain.admin.idType.IdType_Id;
import com.splwg.ccb.domain.admin.matchType.MatchType_Id;
import com.splwg.ccb.domain.billing.bill.Bill_Id;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.customerinfo.serviceAgreement.ServiceAgreement_Id;
import com.splwg.ccb.domain.dataManagement.fileRequest.CompositeKeyBean_Per;
import com.splwg.ccb.domain.dataManagement.fileRequest.batch.FileRequestPreProcessingAlgorithmSpot;
import com.splwg.ccb.domain.dataManagement.fileRequest.batch.FileTransformationRecord;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;
import com.splwg.cm.domain.common.messageRepository.CmMessages;
import com.splwg.shared.common.LoggedException;

/**
 * @author DSekar
 *
 *			   @AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = idType, name = customerIdentifierType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = matchType, name = billMatchType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = matchType, name = accountMatchType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = matchType, name = generalSuspenseMatchType, required = true, type = entity)})
 */

public class CmPreProcessPaymentInterfaceFile_Impl extends CmPreProcessPaymentInterfaceFile_Gen implements
FileRequestPreProcessingAlgorithmSpot {

	private static final String TENDER_TYPE_NODE = "CM-PayUpload/tenderType";
	private static final String CURRENCY_NODE = "CM-PayUpload/currency";
	private static final String TENDER_AMT_NODE = "CM-PayUpload/tenderAmount";
	private static final String ACCT_DATE_NODE = "CM-PayUpload/accountingDate";
	private static final String PAYMNT_UPLOAD_LIST = "CM-PayUpload/paymentUpld";
	private static final String ACCT_TYPE_NODE = "CM-PayUpload/accountIdentifierType";
	private static final String ACCT_ID_NODE = "CM-PayUpload/accountIdentifier";
	private static final String CUST_ID_NODE = "CM-PayUpload/customerId";
	private static final String EXT_SRC_ID_NODE = "CM-PayUpload/externalSourceId";
	private static final String MATCH_TYPE_STR = "matchType";
	private static final String MATCH_VALUE_STR = "matchValue";
	private static final String CHAR_TYPE_STR = "characteristicType";
	private static final String CHAR_VALUE_STR = "characteristicValue";
	private static final String ADH_CHAR_VALUE_STR = "adhocCharacteristicValue";
	private static final String CHAR_VALUE_FK1_STR = "characteristicValueForeignKey1";
	private static final String PAYMT_AMT_STR = "paymentAmount";
	private static final String CUST_ID_STR = "customerId";
	//CB-74- Start Change
	// private static final String PAY_CHAR_STR = "payChar";
	private static final String PAY_CHAR_STR = "paychar";
	//CB-74- End Change
	private static final String TENDER_TYPE_VAL_PARAM = "Tender Type Value";
	private static final String TENDER_TYPE_PARAM = "Tender Type";
	private static final String TENDER_AMT_VAL_PARAM = "Tender Amount Value";
	private static final String TENDER_AMT_PARAM = "Tender Amount";
	private static final String ACCTING_DATE_VALUE_PARAM = "Accounting Date value";
	private static final String ACCTING_DATE_PARAM = "Accounting Date";
	private static final String CURRENCY_VAL_PARAM = "Currency Value";
	private static final String CURRENCY_CODE_PARAM = "Currency Code";
	private static final String CURRENCY_PARAM = "Currency";
	private static final String PYMNT_AMT_PARAM = "Payment Amount Value";
	private static final String EXT_SRC_NODE_PARAM = "External Source ID";
	private static final String PAYMT_AMT_PARAM = "Payment Amount";
	//Start Change - CB-231
	//private static final String MATCH_TYPE_OR_VAL_PARAM = "Match Value or Match Type";
	private static final String MATCH_TYPE_PARAM = "Match Type";
	private static final String MATCH_VALUE_PARAM = "Match Value";
	private static final String PAYOR_ACCOUNT_IDENTIFIER_NODE = "CM-PayUpload/payorPrimaryAccountIdentifier";
	//End Add - CB-231
	private static final String EMPTY_STR = " ";
	private String servicePayload = null;
	private String fileRequestType = null;
	private String payAccountId = null;
	private Account_Id acctId;
	private int count = 0;

	@Override
	@SuppressWarnings("unchecked")
	public void invoke() {
		Document servicePayloadDoc = null;
		try {
			//Parsing XML from batch.
			servicePayloadDoc = DocumentHelper.parseText(servicePayload);
		} catch (DocumentException e) {
			throw LoggedException.wrap("Exception while fetching service payload", e);
		}
		if (notNull(servicePayloadDoc)) {
			Element rootElement = servicePayloadDoc.getRootElement();
			Node acctIdTypeNode = null, acctIdnode = null, currencyNode = null, acctDateNode = null, tenderAmountNode = null, tenderTypeNode = null;
			//Start Add - CB-231
			Node payorAccountIdentifierNode = null;
			payorAccountIdentifierNode = servicePayloadDoc.selectSingleNode(PAYOR_ACCOUNT_IDENTIFIER_NODE);
			//End Add CB-231 
			tenderTypeNode = servicePayloadDoc.selectSingleNode(TENDER_TYPE_NODE);
			tenderAmountNode = servicePayloadDoc.selectSingleNode(TENDER_AMT_NODE);
			acctDateNode = servicePayloadDoc.selectSingleNode(ACCT_DATE_NODE);
			currencyNode = servicePayloadDoc.selectSingleNode(CURRENCY_NODE);
			acctIdTypeNode = servicePayloadDoc.selectSingleNode(ACCT_TYPE_NODE);
			acctIdnode = servicePayloadDoc.selectSingleNode(ACCT_ID_NODE);
			Node externalSrc = servicePayloadDoc.selectSingleNode(EXT_SRC_ID_NODE);
			//Validate Mandatory nodes
			singleNodesValidation(tenderTypeNode, tenderAmountNode, acctDateNode, currencyNode);
			List < Node > paymentUploadnodes = servicePayloadDoc.selectNodes(PAYMNT_UPLOAD_LIST);
			//Validate Payment Upload list
			pymtUploadNodesValidation(paymentUploadnodes);

			//Start Change CB-231
			
			//Determining Payer Account Id
			//determinePayAcctId(acctIdTypeNode, acctIdnode, paymentUploadnodes);
			determinePayAcctId(payorAccountIdentifierNode);
			
				String accountId = null, matchType = null, matchValue = null;
				Map < String, String > payUpldDetailsMap = null;
				Element acctIdElem = null;
				CharacteristicTypeLookup charLookup = null;
				Element addCharElm = null;
				List < Node > nodesChar = null;
				//Start Change - CB-231
				//String matchTypeInput = null;
				//String matchValueInput = null;
				//End Change - CB-231
				for (Node payUpld: paymentUploadnodes) {
					count++;
					//Start Change CB-231
//					if (!isNull(payUpld.selectSingleNode(MATCH_TYPE_STR)) && !isNull(payUpld.selectSingleNode(MATCH_VALUE_STR))) {
					if (!isNull(payUpld.selectSingleNode(MATCH_TYPE_STR))) {
					//End Change CB-231	
						try {
							acctIdElem = (Element) payUpld;
						} catch (Exception e) {
							throw LoggedException.wrap("Exception while converting payUlpd ", e);
						}

						//Start Change CB-231
//						if ((isBlankOrNull(payUpld.selectSingleNode(MATCH_TYPE_STR).getText()) || isBlankOrNull(payUpld.selectSingleNode(MATCH_VALUE_STR).getText())))
						if ((isBlankOrNull(payUpld.selectSingleNode(MATCH_TYPE_STR).getText())))
						{
//							addError(CmMessageRepository.getServerMessage(CmMessages.MANDATORY_NODE_MISSING, MATCH_TYPE_OR_VAL_PARAM));
							addError(CmMessageRepository.getServerMessage(CmMessages.MANDATORY_NODE_MISSING, MATCH_TYPE_PARAM));
						}						
						//matchTypeInput = payUpld.selectSingleNode(MATCH_TYPE_STR).getText().trim();
						//matchValueInput = payUpld.selectSingleNode(MATCH_VALUE_STR).getText();
						
						// To get accountId, matchType and matchValue for a payUpload node
						//payUpldDetailsMap = getAcctIdMatchTypeAndValueForPayUpld(matchTypeInput, matchValueInput, externalSrc, null);
						payUpldDetailsMap = getAcctIdMatchTypeAndValueForPayUpld(payUpld, externalSrc, null);
						//End Change CB-231

						accountId = payUpldDetailsMap.get("ACCT_ID").trim();
						matchType = payUpldDetailsMap.get("MATCH_TYPE").trim();
						matchValue = payUpldDetailsMap.get("MATCH_VALUE").trim();
						acctIdElem.addElement(CUST_ID_STR).setText(accountId);
						payUpld.selectSingleNode(MATCH_TYPE_STR).setText(matchType);
						payUpld.selectSingleNode(MATCH_VALUE_STR).setText(matchValue);
						nodesChar = payUpld.selectNodes(PAY_CHAR_STR);

						for (Node nodeChar: nodesChar) {
							if (!isNull(nodeChar.selectSingleNode(CHAR_TYPE_STR)) && !isNull(nodeChar.selectSingleNode(CHAR_VALUE_STR))) {
								charLookup = checkValidCharType(nodeChar.selectSingleNode(CHAR_TYPE_STR).getText(), null);
								if (!isNull(charLookup)) {
									try {
										addCharElm = (Element) nodeChar;
									} catch (Exception e) {
										throw LoggedException.wrap("Exception while converting payUlpd Node char", e);
									}
									if (charLookup.equals(CharacteristicTypeLookup.constants.ADHOC_VALUE)) {
										addCharElm.addElement(ADH_CHAR_VALUE_STR).setText(nodeChar.selectSingleNode(CHAR_VALUE_STR).getText());
										nodeChar.selectSingleNode(CHAR_VALUE_STR).detach();
									} else if (charLookup.equals(CharacteristicTypeLookup.constants.FOREIGN_KEY_VALUE)) {
										addCharElm.addElement(CHAR_VALUE_FK1_STR).setText(nodeChar.selectSingleNode(CHAR_VALUE_STR).getText());
										nodeChar.selectSingleNode(CHAR_VALUE_STR).detach();
									}
								} 
							}
						}
					} else {
						addError(CmMessageRepository.getServerMessage(CmMessages.MANDATORY_NODE_MISSING, MATCH_TYPE_STR));
					}
				}
				if (isBlankOrNull(payAccountId)) {
					addError(CmMessageRepository.getServerMessage(CmMessages.UNABLE_TO_FIND_ACCT));
				} else {
					Node custIdNode = servicePayloadDoc.selectSingleNode(CUST_ID_NODE);
					if (isNull(custIdNode)) {
						rootElement.addElement(CUST_ID_STR).setText(payAccountId);

					} else {
						custIdNode.setText(payAccountId);
					}
				}
				servicePayload = servicePayloadDoc.asXML().replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "").trim();
		}
	}

	/**
	 * This method validates given CharacteristicType
	 * 
	 * @param charTypeNode
	 * @param charId
	 * 
	 */

	private CharacteristicTypeLookup checkValidCharType(String charTypeNode, CharacteristicType_Id charId) {

		if(!isBlankOrNull(charTypeNode))
		{
			charId = new CharacteristicType_Id(charTypeNode.trim());
			if (!isNull(charId.getEntity())) {
				return charId.getEntity().getCharacteristicType();
			}
			else{
				addError(CmMessageRepository.getServerMessage(CmMessages.CHAR_TYPE_INVALID, CHAR_TYPE_STR, charTypeNode));

			}
		}
		return null;
	}

	/**
	 * This method is to get Account Id
	 * by using the Match type and Match value
	 * @param matchTypeInput
	 * @param matchValueInput
	 * @param externalSrcNode
	 * @param returnMap
	 * @return
	 */
//
	//Start Change - CB-231
	//private Map < String, String > getAcctIdMatchTypeAndValueForPayUpld(String matchTypeInput,String matchValueInput, Node externalSrcNode, Map < String, String > returnMap) {
	private Map < String, String > getAcctIdMatchTypeAndValueForPayUpld(Node payUpld, Node externalSrcNode, Map < String, String > returnMap) {

		String matchTypeInput = payUpld.selectSingleNode(MATCH_TYPE_STR).getText().trim();
		String matchValueInput = "";
		//End Change CB-231
		
		returnMap = new HashMap < String, String > ();
		acctId = null;
		if (matchTypeInput.equals(getAccountMatchType().getId().getIdValue().trim())) {
			//Start Add - CB-231
			if(!isNull(payUpld.selectSingleNode(MATCH_VALUE_STR))){
				matchValueInput = payUpld.selectSingleNode(MATCH_VALUE_STR).getText().trim();
				if(isBlankOrNull(matchValueInput)){
					addError(CmMessageRepository.getServerMessage(CmMessages.MANDATORY_NODE_MISSING, MATCH_VALUE_PARAM));
				} 
			}else{
				addError(CmMessageRepository.getServerMessage(CmMessages.MANDATORY_NODE_MISSING, MATCH_VALUE_STR));
			}
			
			//End Add - CB-231
			acctId  = new Account_Id(matchValueInput.trim());
			if (!isNull(acctId.getEntity())) {
				returnMap.put("ACCT_ID", matchValueInput);
				returnMap.put("MATCH_TYPE", matchTypeInput);
				returnMap.put("MATCH_VALUE", matchValueInput);
				if(isBlankOrNull(payAccountId) && count==1){
					payAccountId = acctId.getTrimmedValue();
				}
			} 
			
			//Start Add - CB-231
			else
			{
				IdType_Id idTypeId = getCustomerIdentifierType().getId();
				String accountId = null;
				PreparedStatement fetchAccountId = null;
				final StringBuilder fetchAccountIdQuery = new StringBuilder().append(" SELECT A.ACCT_ID FROM CI_ACCT_PER A, CI_PER_ID B "
						+ " WHERE A.PER_ID = B.PER_ID AND "
						+ " B.PER_ID = (SELECT PER_ID FROM CI_PER_ID WHERE ID_TYPE_CD=:idTypeId AND PER_ID_NBR=:perIdNum)");
				fetchAccountId = createPreparedStatement(fetchAccountIdQuery.toString(), "Fetch Account Id");
				fetchAccountId.bindId("idTypeId", idTypeId);
				fetchAccountId.bindString("perIdNum", matchValueInput , "PER_ID_NBR");
				SQLResultRow account = fetchAccountId.firstRow();
				fetchAccountId.close();
				if(notNull(account))
				{
					accountId = account.getString("ACCT_ID");
					returnMap.put("ACCT_ID", accountId);
					returnMap.put("MATCH_TYPE", matchTypeInput);
					returnMap.put("MATCH_VALUE", accountId);
				}
				if(isBlankOrNull(payAccountId) && count==1){
					payAccountId = acctId.getTrimmedValue();
				}
			}
			//End Add - CB-231
			
		}  if (matchTypeInput.equals(getBillMatchType().getId().getIdValue().trim())) {
			//Start Add - CB-231
			if(!isNull(payUpld.selectSingleNode(MATCH_VALUE_STR))){
				matchValueInput = payUpld.selectSingleNode(MATCH_VALUE_STR).getText().trim();
				if(isBlankOrNull(matchValueInput)){
					addError(CmMessageRepository.getServerMessage(CmMessages.MANDATORY_NODE_MISSING, MATCH_VALUE_PARAM));
				} 
			}else{
				addError(CmMessageRepository.getServerMessage(CmMessages.MANDATORY_NODE_MISSING, MATCH_VALUE_STR));
			}
			//End Add - CB-231
			Bill_Id billId = new Bill_Id(matchValueInput.trim());
			if (!isNull(billId.getEntity())) {
				returnMap.put("ACCT_ID", billId.getEntity().getAccount().getId().getTrimmedValue());
				returnMap.put("MATCH_TYPE", getBillMatchType().getId().getIdValue().trim());
				returnMap.put("MATCH_VALUE", matchValueInput);
			}
			if(isBlankOrNull(payAccountId) && count==1){
				payAccountId = billId.getEntity().getAccount().getId().getTrimmedValue();
			}
		}
		
		//Start Add - CB-231
		if(matchTypeInput.equals(getGeneralSuspenseMatchType().getId().getIdValue().trim()))
		{
			if(!isNull(externalSrcNode)){
				if(!isBlankOrNull(externalSrcNode.getText())){
					PreparedStatement fetchSaId = null;
					String extSourceId = externalSrcNode.getText().trim();
					fetchSaId = createPreparedStatement(" SELECT SA_ID FROM CI_TNDR_SRCE WHERE EXT_SOURCE_ID = \'" + extSourceId + "\' ", "Get SA_ID");
					String saId = null;
					if (!isNull(fetchSaId.firstRow())) {
						saId = fetchSaId.firstRow().getString("SA_ID").trim();
						ServiceAgreement_Id serviceAgrId = new ServiceAgreement_Id(saId);
						Account_Id accountId = new Account_Id(serviceAgrId.getEntity().getAccount().getId().getTrimmedValue());
						returnMap.put("ACCT_ID", accountId.getTrimmedValue());
						returnMap.put("MATCH_TYPE", getGeneralSuspenseMatchType().getId().getIdValue().trim());
						returnMap.put("MATCH_VALUE", accountId.getTrimmedValue());
						if(isBlankOrNull(payAccountId) && count==1){
							payAccountId = accountId.getTrimmedValue();
						}
					}else{
						addError(CmMessageRepository.noSuspenseContractRetrive(extSourceId));
					}
				}else{
					addError(CmMessageRepository.getServerMessage(CmMessages.MANDATORY_NODE_MISSING, EXT_SRC_NODE_PARAM));
				}
			}else{
				addError(CmMessageRepository.getServerMessage(CmMessages.MANDATORY_NODE_MISSING, EXT_SRC_ID_NODE));
			}
		}
		//End Add - CB-231
		
		//Start Change - CB-231
		/*
		if(returnMap.isEmpty())
		{
			if (!isNull(externalSrcNode)) {
				if (!isBlankOrNull(externalSrcNode.getText())) {
					PreparedStatement psPreparedStatement = null;
					String extSourceId = externalSrcNode.getText().trim();
					psPreparedStatement = createPreparedStatement(" select SA_ID from ci_tndr_srce where EXT_SOURCE_ID = \'" + extSourceId + "\' ", "");
					String saId = null;
					if (!isNull(psPreparedStatement.firstRow())) {
						saId = psPreparedStatement.firstRow().getString("SA_ID").trim();
						psPreparedStatement.close();
						ServiceAgreement_Id serviceAgrId = new ServiceAgreement_Id(saId);
						acctId = new Account_Id(serviceAgrId.getEntity().getAccount().getId().getTrimmedValue());
						returnMap.put("ACCT_ID", acctId.getTrimmedValue());
						returnMap.put("MATCH_TYPE", getGeneralSuspenseMatchType().getId().getIdValue().trim());
						returnMap.put("MATCH_VALUE", serviceAgrId.getEntity().getId().getTrimmedValue());
					} else {
						addError(CmMessageRepository.getServerMessage(CmMessages.ACCT_NOT_MATCH_TYPE, matchTypeInput,matchValueInput));
					}
				} else {
					addError(CmMessageRepository.getServerMessage(CmMessages.MANDATORY_NODE_MISSING, EXT_SRC_NODE_PARAM));
				}
			} else {
				addError(CmMessageRepository.getServerMessage(CmMessages.MANDATORY_NODE_MISSING, EXT_SRC_ID_NODE));
			}
		}*/
		//End Change - CB-231
		return returnMap;
	}

	/**
	 * This method validates 
	 * Tender Type Node, Tender Amount Node
	 * Accounting Date Node, Currency Node.
	 * @param tenderTypeNode
	 * @param tenderAmountNode
	 * @param acctDateNode
	 * @param currencyNode
	 */

	private void singleNodesValidation(Node tenderTypeNode, Node tenderAmountNode, Node acctDateNode, Node currencyNode) {
		if (!isNull(tenderTypeNode)) {
			if (isBlankOrNull(tenderTypeNode.getText())) {
				addError(CmMessageRepository.getServerMessage(CmMessages.MANDATORY_NODE_MISSING, TENDER_TYPE_VAL_PARAM));
			}
		} else {
			addError(CmMessageRepository.getServerMessage(CmMessages.MANDATORY_NODE_MISSING, TENDER_TYPE_PARAM));
		}
		if (!isNull(tenderAmountNode)) {
			if (isBlankOrNull(tenderAmountNode.getText())) {
				addError(CmMessageRepository.getServerMessage(CmMessages.MANDATORY_NODE_MISSING, TENDER_AMT_VAL_PARAM));
			}
		} else {
			addError(CmMessageRepository.getServerMessage(CmMessages.MANDATORY_NODE_MISSING, TENDER_AMT_PARAM));
		}
		if (!isNull(acctDateNode)) {
			if (isBlankOrNull(acctDateNode.getText())) {
				addError(CmMessageRepository.getServerMessage(CmMessages.MANDATORY_NODE_MISSING, ACCTING_DATE_VALUE_PARAM));
			}
		} else {
			addError(CmMessageRepository.getServerMessage(CmMessages.MANDATORY_NODE_MISSING, ACCTING_DATE_PARAM));
		}

		if (!isNull(currencyNode)) {
			if (isBlankOrNull(currencyNode.getText())) {
				addError(CmMessageRepository.getServerMessage(CmMessages.MANDATORY_NODE_MISSING, CURRENCY_VAL_PARAM));
			} else {
				if (!isCurrencyValid(currencyNode.getText())) {
					addError(CmMessageRepository.getServerMessage(CmMessages.ENTITY_NOT_VALID, currencyNode.getText(), CURRENCY_CODE_PARAM));
				}
			}

		} else {
			addError(CmMessageRepository.getServerMessage(CmMessages.MANDATORY_NODE_MISSING, CURRENCY_PARAM));
		}
	}

	/**
	 * This method validates paymentUpld list
	 * @param paymentUploadnodes
	 * 
	 */

	private void pymtUploadNodesValidation(List < Node > paymentUploadnodes) {
		Node paymentNode = null, matchValueNode = null, matchTypeNode = null;
		MatchType_Id matchTypeId = null;
		for (Node forInstanceNode: paymentUploadnodes) {
			paymentNode = forInstanceNode.selectSingleNode(PAYMT_AMT_STR);
			//matchValueNode = forInstanceNode.selectSingleNode(MATCH_VALUE_STR);
			matchTypeNode = forInstanceNode.selectSingleNode(MATCH_TYPE_STR);
			if (!isNull(paymentNode)) {
				if (isBlankOrNull(paymentNode.getText())) {
					addError(CmMessageRepository.getServerMessage(CmMessages.MANDATORY_NODE_MISSING, PYMNT_AMT_PARAM));
				}
			} else {
				addError(CmMessageRepository.getServerMessage(CmMessages.MANDATORY_NODE_MISSING, PAYMT_AMT_PARAM));
			}
			
			//Add Change - CB-231
			/*
			if (!isNull(matchValueNode)) {
				if (isBlankOrNull(matchValueNode.getText())) {
					addError(CmMessageRepository.getServerMessage(CmMessages.MANDATORY_NODE_MISSING, MATCH_VALUE_STR));
				}
			} else {
				addError(CmMessageRepository.getServerMessage(CmMessages.MANDATORY_NODE_MISSING, MATCH_VALUE_STR));
			}*/
			//End Change - CB-231
			
			if (!isNull(matchTypeNode)) {
				if (isBlankOrNull(matchTypeNode.getText())) {
					addError(CmMessageRepository.getServerMessage(CmMessages.MANDATORY_NODE_MISSING, MATCH_TYPE_STR));
				}else{
					// validate MatchType is in system or not. 
					matchTypeId = new MatchType_Id(matchTypeNode.getText());
					if(isNull(matchTypeId.getEntity()))
					{
						addError(CmMessageRepository.getServerMessage(CmMessages.ENTITY_NOT_VALID, matchTypeNode.getText(), MATCH_TYPE_STR));
					}
				}
			} else {
				addError(CmMessageRepository.getServerMessage(CmMessages.MANDATORY_NODE_MISSING, MATCH_TYPE_STR));
			}
		}
	}
	/**
	 * To find payer Account Id 
	 * @param acctIdTypeNode
	 * @param acctIdnode
	 * @param paymentUploadnodes
	 */
	//Start Change - CB-231
	/*private void determinePayAcctId(Node acctIdTypeNode, Node acctIdnode, List<Node> paymentUploadnodes) {
		if (notNull(acctIdTypeNode) && !isBlankOrNull(acctIdTypeNode.getText()) && notNull(acctIdnode) && !isBlankOrNull(acctIdnode.getText())) {
			payAccountId = getAcctIdByAcctNumAndType(acctIdTypeNode.getText(), acctIdnode.getText()).trim();
		} 		
		if (isBlankOrNull(payAccountId)&& (!paymentUploadnodes.isEmpty())) {
			Node matchTypeNode = paymentUploadnodes.get(0).selectSingleNode(MATCH_TYPE_STR);
			Node matchValueNode = paymentUploadnodes.get(0).selectSingleNode(MATCH_VALUE_STR);
			
			
			
			if (!isNull(matchTypeNode) && !isNull(matchValueNode))
			{
			payAccountId = determineAcctIdByMatchTypeAndValue(matchTypeNode.getText(), matchValueNode.getText()).trim();
			}
		}
	}*/
	private void determinePayAcctId(Node payorAccountIdentifierNode) {
		if(notNull(payorAccountIdentifierNode) && !isBlankOrNull(payorAccountIdentifierNode.getText())){
			payAccountId = getAcctIdByAcctIdentifiere(payorAccountIdentifierNode.getText());
		}
	}
	
	//End Change - CB-231

	/**
	 * This method Validates Currency Code.
	 * @param currencyCd
	 * @return
	 */

	private boolean isCurrencyValid(String currencyCd) {
		if (!isBlankOrNull(currencyCd)) {
			Currency_Id currId = new Currency_Id(currencyCd.trim());
			if (!isNull(currId.getEntity())) {
				return true;
			}
		}
		return false;
	}


	/**
	 * To Find Account Id by Account Id and Number.
	 * @param acctIdType
	 * @param accNumber
	 */

	//Start Change CB-231
	//private String getAcctIdByAcctNumAndType(String acctIdtype) {
	private String getAcctIdByAcctIdentifiere(String acctIdentifier) {
		//End Change CB-231
		String accountId = EMPTY_STR;
		boolean multipleAcctNum = false;
		StringBuilder fetchAccountIdQuery = new StringBuilder();
		fetchAccountIdQuery.append("SELECT ACCT_ID ");
		fetchAccountIdQuery.append("FROM CI_ACCT_NBR ");
		//Start Change CB-231
		//fetchAccountIdQuery.append("WHERE TRIM(ACCT_NBR_TYPE_CD) = trim(:accountTypeCd) ");
		//fetchAccountIdQuery.append("AND trim(ACCT_NBR) = trim(:accountNumber) ");
		//fetchAccountIdStatement.bindString("accountTypeCd", acctIdtype, "ACCT_NBR_TYPE_CD");
		//fetchAccountIdStatement.bindString("accountNumber", accNumber, "ACCT_NBR");
		//End Change CB-231 
		fetchAccountIdQuery.append(" WHERE TRIM(ACCT_NBR) = TRIM(:accountIdentifier) AND PRIM_SW = 'Y' ");
		PreparedStatement fetchAccountIdStatement = createPreparedStatement(fetchAccountIdQuery.toString(), "Fetching Account Id from Account Number");
		fetchAccountIdStatement.bindString("accountIdentifier", acctIdentifier.trim(), "ACCT_NBR");
		QueryIterator < SQLResultRow > resultIterator = null;
		SQLResultRow result = null;
		try {
			resultIterator = fetchAccountIdStatement.iterate();
			while (resultIterator.hasNext()) {
				if (isBlankOrNull(accountId)) {
					result = (SQLResultRow) resultIterator.next();
					accountId = result.getString("ACCT_ID").trim();
				} else {
					multipleAcctNum = true; 
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		fetchAccountIdStatement.close();
		if(multipleAcctNum || isBlankOrNull(accountId))
		{
			//Start Change CB-231
			//addError(CmMessageRepository.getServerMessage(CmMessages.MULTIPLE_ACCT_NBR, acctIdtype, accNumber));
			accountId = null;
			//End Change CB-231
		}
		return accountId;
	}


//Start Change - CB-231
	
	/**
	 * To find Account Id by Match Type 
	 * and Match Value.
	 * @param matchType
	 * @param matchValue
	 */
/*
	private String determineAcctIdByMatchTypeAndValue(String matchType, String matchValue) {

		if(!isBlankOrNull(matchValue) && !isBlankOrNull(matchType))
		{
			if ( matchType.equals(getAccountMatchType().getId().getIdValue().trim())) {
				return matchValue.trim();
			} else if (matchType.equals(getBillMatchType().getId().getIdValue().trim()) ) {
				Bill_Id billId = new Bill_Id(matchValue.trim());
				if (!isNull(billId.getEntity())) {
					return billId.getEntity().getAccount().getId().getTrimmedValue();
				}
			}
		}
		return EMPTY_STR;
	}*/
	
//End Change - CB-231
	@Override
	public CompositeKeyBean_Per getCompositeKeyBean_Per() {
		return null;
	}

	@Override
	public String getFileName() {
		return null;
	}

	@Override
	public FileTransformationRecord getInputFieldValueMap() {
		return null;
	}

	@Override
	public String getMessageCategory() {
		return null;
	}

	@Override
	public String getMessageNumber() {
		return null;
	}

	@Override
	public String getMessageParam1() {
		return null;
	}

	@Override
	public String getMessageParam2() {
		return null;
	}

	@Override
	public String getMessageParam3() {
		return null;
	}

	@Override
	public String getMessageParam4() {
		return null;
	}

	@Override
	public String getMessageParam5() {
		return null;
	}

	@Override
	public String getOperation() {
		return null;
	}

	@Override
	public String getRefId() {
		return null;
	}

	@Override
	public String getRequestPayload() {
		return null;
	}

	@Override
	public String getRequestType() {
		return fileRequestType;
	}

	@Override
	public String getServicePayload() {
		return servicePayload;
	}

	@Override
	public String getSkipReason() {
		return null;
	}

	@Override
	public String getStatus() {
		return null;
	}

	@Override
	public boolean isSkipServiceExecution() {
		return false;
	}

	@Override
	public void setCompositeKeyBean_Per(CompositeKeyBean_Per arg0) {

	}

	@Override
	public void setFileName(String arg0) {

	}

	@Override
	public void setInputFieldValueMap(FileTransformationRecord arg0) {

	}

	@Override
	public void setMessageCategory(String arg0) {

	}

	@Override
	public void setMessageNumber(String arg0) {

	}

	@Override
	public void setMessageParam1(String arg0) {

	}

	@Override
	public void setMessageParam2(String arg0) {

	}

	@Override
	public void setMessageParam3(String arg0) {

	}

	@Override
	public void setMessageParam4(String arg0) {

	}

	@Override
	public void setOperation(String arg0) {

	}

	@Override
	public void setRefId(String arg0) {

	}

	@Override
	public void setRequestPayload(String arg0) {

	}

	@Override
	public void setRequestType(String arg0) {
		fileRequestType = arg0;
	}

	@Override
	public void setServiceName(String arg0) {

	}

	@Override
	public void setServicePayload(String arg0) {
		servicePayload = arg0;
	}

	@Override
	public void setSkipReason(String arg0) {

	}

	@Override
	public void setSkipServiceExecution(boolean arg0) {

	}

	@Override
	public void setStatus(String arg0) {

	}

	@Override
	public String getDataReTransformRequired() {
		return null;
	}

	@Override
	public FileTransformationRecord getInputGroupRecordMap() {
		return null;
	}

	@Override
	public FileTransformationRecord getMapFieldXpathMap() {
		return null;
	}

	@Override
	public void setDataReTransformRequired(String arg0) {

	}

	@Override
	public void setInputGroupRecordMap(FileTransformationRecord arg0) {

	}

	@Override
	public void setMapFieldXpathMap(FileTransformationRecord arg0) {

	}

}