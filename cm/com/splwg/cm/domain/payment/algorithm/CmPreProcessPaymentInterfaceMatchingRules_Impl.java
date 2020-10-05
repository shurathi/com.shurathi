/*
 ***********************************************************************
 *                Confidentiality Information:
 *
 * This module is the confidential and proprietary information of
 * RIA Advisory; it is not to be copied, reproduced, or
 * transmitted in any form, by any means, in whole or in part,
 * nor is it to be used for any purpose other than that for which
 * it is expressly provided without the written permission of
 * RIA Advisory.
 ***********************************************************************
 *
 * PROGRAM DESCRIPTION:
 *
 * This will preprocess the payment XML file to derive the corresponding 
 * match type and match value given the payment matching input so that 
 * the File Upload will be able to create new Payment Staging records.
 *
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:                Reason:
 * 2020-06-09   DDejes/JRaymu      Initial Version. 
 * 2020-07-30	IGarg			   Updated Version for CB-145
 ***********************************************************************
 */
package com.splwg.cm.domain.payment.algorithm;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.DateFormat;
import com.splwg.base.api.datatypes.DateFormatParseException;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.domain.common.currency.Currency;
import com.splwg.base.domain.common.currency.Currency_Id;
import com.splwg.ccb.domain.dataManagement.fileRequest.CompositeKeyBean_Per;
import com.splwg.ccb.domain.dataManagement.fileRequest.batch.FileRequestPreProcessingAlgorithmSpot;
import com.splwg.ccb.domain.dataManagement.fileRequest.batch.FileTransformationRecord;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;
import com.splwg.cm.domain.common.messageRepository.CmMessages;
import com.splwg.cm.domain.payment.businessComponent.CmPaymentMatchingInput;
import com.splwg.cm.domain.payment.businessComponent.CmPaymentMatchingRules;
import com.splwg.cm.domain.payment.businessComponent.CmPaymentServiceData;
import com.splwg.shared.common.ApplicationError;
import com.splwg.shared.common.LoggedException;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;

/**
 * @author Jerrick Raymundo
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (name = receiptNumCharType, required = true, type = string)})
 */
public class CmPreProcessPaymentInterfaceMatchingRules_Impl extends CmPreProcessPaymentInterfaceMatchingRules_Gen implements
        FileRequestPreProcessingAlgorithmSpot {
    // Hard Parameters
    private String servicePayload;
    private String requestType;
    private String requestPayload;
    private static final Logger logger = LoggerFactory.getLogger(CmPreProcessPaymentInterfaceMatchingRules_Impl.class);

    // Work Parameter
    private Element payUploadGroup;
    private Currency currency;
    String requestPayloadNode;

    @Override
    public void invoke() {
    	logger.info("requestType "+requestType);
    	logger.info("requestPayload "+requestPayload);
    	int index = requestPayload.indexOf("</requestType>");
    	if(index > 0)
    	{
    		requestPayloadNode = requestPayload.substring(0,index+14);
    	}
    	else
    	{
    		addError(CmMessageRepository.entityNotValid("Request Type"," "));
    	}
        Document servicePayloadDoc = null;
        Document requestPayloadDoc = null;
        try {
            //Parsing XML from batch.
            servicePayloadDoc = DocumentHelper.parseText(servicePayload);
            requestPayloadDoc = DocumentHelper.parseText(requestPayloadNode);
        } catch (DocumentException e) {
            throw LoggedException.wrap("Exception while fetching service payload", e);
        }
        // Validate required inputs
        payUploadGroup = servicePayloadDoc.getRootElement();

        if (isNull(payUploadGroup)) {
            addError(CmMessageRepository.getServerMessage(CmMessages.MANDATORY_NODE_MISSING,
                    CmPreProcessPaymentInterfaceMatchingRulesConstants.PAY_UPLOAD_GROUP));
        }
        retrieveRequiredInput(payUploadGroup, CmPreProcessPaymentInterfaceMatchingRulesConstants.TENDER_TYPE);
        Node tenderAmountNode = retrieveRequiredInput(payUploadGroup,
                CmPreProcessPaymentInterfaceMatchingRulesConstants.TENDER_AMOUNT);
        Node accountingDateNode = retrieveRequiredInput(payUploadGroup,
                CmPreProcessPaymentInterfaceMatchingRulesConstants.ACCOUNTING_DATE);
        /*
         * Code Added for CB-287
         */
         Node receiptNum = retrieveRequiredInput(payUploadGroup,
                CmPreProcessPaymentInterfaceMatchingRulesConstants.RECEIPT_NUM);
        Node creationDttm = retrieveRequiredInput(payUploadGroup,
                CmPreProcessPaymentInterfaceMatchingRulesConstants.CREATION_DTTM);
        
        if(!requestPayloadDoc.selectSingleNode("requestType").getText().equals(requestType))
        {
        	addError(CmMessageRepository.entityNotValid("Request Type",requestPayloadDoc.selectSingleNode("requestType").getText()));
        }
        
        /*
         * Code Added for CB-287
         */

        Node currencyNode = payUploadGroup.selectSingleNode(CmPreProcessPaymentInterfaceMatchingRulesConstants.CURRENCY);
        if (notNull(currencyNode)) {
            currency = new Currency_Id(currencyNode.getText()).getEntity();
            if (isNull(currency)) {
                addError(CmMessageRepository.getServerMessage(CmMessages.ENTITY_NOT_VALID, "Currency",
                        currencyNode.getText()));
            }
        } else {
            currency = CmPreProcessPaymentInterfaceMatchingRulesConstants.DEFAULT_CURRENCY.getEntity();
        }
        // Clear the data of the paymentUpId list node from the payload schema hard parameter if there is any data.
        if (notNull(payUploadGroup.selectSingleNode(CmPreProcessPaymentInterfaceMatchingRulesConstants.PAY_UPLOAD_LIST))) {
            payUploadGroup.selectSingleNode(CmPreProcessPaymentInterfaceMatchingRulesConstants.PAY_UPLOAD_LIST).detach();
        }
        try {
            // Initialize Business Component inputs
            Node externalSourceIdNode = payUploadGroup
                    .selectSingleNode(CmPreProcessPaymentInterfaceMatchingRulesConstants.EXTERNAL_SOURCE_ID);
            Node payorPrimAcctIdentNode = payUploadGroup
                    .selectSingleNode(CmPreProcessPaymentInterfaceMatchingRulesConstants.PAYOR_PRIM_ACCT_IDENT);

            // Call Payment Matching Rules Business Component
            CmPaymentMatchingRules paymentMatchingRuleBusComp = CmPaymentMatchingRules.Factory.newInstance();

            paymentMatchingRuleBusComp.setPaymentMatchingInput(generatePaymentMatchingInputGroup());
            BigDecimal tenderAmount = BigDecimal.valueOf(Double.parseDouble(tenderAmountNode.getText()));
            paymentMatchingRuleBusComp.setTenderAmount(new Money(tenderAmount, currency.getId()));

            paymentMatchingRuleBusComp.setAccountingDate(parseDate(accountingDateNode.getText(),
                    CmPreProcessPaymentInterfaceMatchingRulesConstants.dateFormat));
            paymentMatchingRuleBusComp.setExternalSourceId(notNull(externalSourceIdNode) ? externalSourceIdNode.getText()
                    : null);
            paymentMatchingRuleBusComp
                    .setPayorPrimaryAccountIdentifier(notNull(payorPrimAcctIdentNode) ? payorPrimAcctIdentNode.getText()
                            : null);
            paymentMatchingRuleBusComp.determineMatchingRule();

            // Populate output
            createNodeIfNeededAndSetText(CmPreProcessPaymentInterfaceMatchingRulesConstants.CUSTOMER_ID,
                    paymentMatchingRuleBusComp.getPayorAccountId());
            logger.info("Payor Account Id :"+paymentMatchingRuleBusComp.getPayorAccountId());
            populateOutput(paymentMatchingRuleBusComp.getMatchDetails(), paymentMatchingRuleBusComp.getPayorAccountId());
            servicePayload = payUploadGroup.asXML();
            logger.info("Output XML : "+servicePayload);
        } catch (ApplicationError e) {
            addError(e.getServerMessage());
        } catch (Exception e) {
            e.getStackTrace();
        }

    }

    /**
     * Create Node If needed and Set text
     * @param nodeName Node Name
     * @param value Value
     */
    private void createNodeIfNeededAndSetText(String nodeName, String value) {
        Node node = payUploadGroup.selectSingleNode(nodeName);
        if (isNull(node)) {
            payUploadGroup.addElement(nodeName).setText(value);
        } else {
            node.setText(value);
        }
    }

    /**
     * Generate Payment Matching Input Group
     * @return Payment Matching Input
     */
    private CmPaymentMatchingInput generatePaymentMatchingInputGroup() {
        CmPaymentMatchingInput input = new CmPaymentMatchingInput();
        Node paymentMatchInptGrp = payUploadGroup
                .selectSingleNode(CmPreProcessPaymentInterfaceMatchingRulesConstants.PAYMENT_MATCH_INPT_GRP);
        /*
         * Code changed By Ishita on 24-07-2020 start
         * Commenting the below line as per the changes suggested in CB-145
         * Making Bill,Order Node to a Bill List,Order List Node
         */
        //Node billIdNode = paymentMatchInptGrp.selectSingleNode(CmPreProcessPaymentInterfaceMatchingRulesConstants.BILL_ID);
      //  Node orderNumberNode = paymentMatchInptGrp
             //   .selectSingleNode(CmPreProcessPaymentInterfaceMatchingRulesConstants.ORDER_NUM);
        //Node loanNumberNode = paymentMatchInptGrp
               // .selectSingleNode(CmPreProcessPaymentInterfaceMatchingRulesConstants.LOAN_NUMBER);
        
        /*
         * Code changed By Ishita on 24-07-2020 end 
         */
       
        
        Node customerNameNode = paymentMatchInptGrp
                .selectSingleNode(CmPreProcessPaymentInterfaceMatchingRulesConstants.CUSTOMER_NAME);
        Node customerAddressNode = paymentMatchInptGrp
                .selectSingleNode(CmPreProcessPaymentInterfaceMatchingRulesConstants.CUSTOMER_ADDRESS_GRP);
        Node address1Node = customerAddressNode
                .selectSingleNode(CmPreProcessPaymentInterfaceMatchingRulesConstants.ADDRESS1);
        Node address2Node = customerAddressNode
                .selectSingleNode(CmPreProcessPaymentInterfaceMatchingRulesConstants.ADDRESS2);
        Node address3Node = customerAddressNode
                .selectSingleNode(CmPreProcessPaymentInterfaceMatchingRulesConstants.ADDRESS3);
        Node address4Node = customerAddressNode
                .selectSingleNode(CmPreProcessPaymentInterfaceMatchingRulesConstants.ADDRESS4);
        Node cityNode = customerAddressNode.selectSingleNode(CmPreProcessPaymentInterfaceMatchingRulesConstants.CITY);
        Node countyNode = customerAddressNode.selectSingleNode(CmPreProcessPaymentInterfaceMatchingRulesConstants.COUNTY);
        Node stateNode = customerAddressNode.selectSingleNode(CmPreProcessPaymentInterfaceMatchingRulesConstants.STATE);
        Node postalNode = customerAddressNode.selectSingleNode(CmPreProcessPaymentInterfaceMatchingRulesConstants.POSTAL);
        Node countryNode = customerAddressNode.selectSingleNode(CmPreProcessPaymentInterfaceMatchingRulesConstants.COUNTRY);

        input.setAccountIdentifier(buildAccountIdentifierList(paymentMatchInptGrp));
        input.setAddress1(notNull(address1Node) ? address1Node.getText() : null);
        input.setAddress2(notNull(address2Node) ? address2Node.getText() : null);
        input.setAddress3(notNull(address3Node) ? address3Node.getText() : null);
        input.setAddress4(notNull(address4Node) ? address4Node.getText() : null);
        /*
         * Code changes done By Ishita on 24-07-2020 start
         * Commenting the below line as per the design changes suggested in CB-145 and adding new one
         * Making Bill Id a list node
         */
        //input.setBillId((notNull(billIdNode)) ? billIdNode.getText() : null);
       // input.setOrderNumber(notNull(orderNumberNode) ? orderNumberNode.getText() : null);
        //input.setLoanNumber(notNull(loanNumberNode) ? loanNumberNode.getText() : null);
        input.setBillIdList(buildBillIdList(paymentMatchInptGrp));
        input.setOrderNumList(buildOrderNumList(paymentMatchInptGrp));
        input.setLoanNumList(buildLoanNumList(paymentMatchInptGrp));
        /*
         * Code changes done By Ishita on 24-07-2020 start
         */
        input.setCity(notNull(cityNode) ? cityNode.getText() : null);
        input.setCountry(notNull(countryNode) ? countryNode.getText() : null);
        input.setCounty(notNull(countyNode) ? countyNode.getText() : null);
        input.setCustomerName(notNull(customerNameNode) ? customerNameNode.getText() : null);
        
        input.setPostal(notNull(postalNode) ? postalNode.getText() : null);
        input.setState(notNull(stateNode) ? stateNode.getText() : null);
        return input;
    }

    /**
     * Retrieve and validate required input
     * @param payUploadGroup Payment Upload Group
     * @param nodeName Node Name
     * @return Node
     */
    private Node retrieveRequiredInput(Node payUploadGroup, String nodeName) {
        Node node = payUploadGroup.selectSingleNode(nodeName);
        if (isNull(node)) {
            addError(CmMessageRepository.getServerMessage(CmMessages.MANDATORY_NODE_MISSING, nodeName));
        }
        if (isBlankOrNull(node.getText())) {
            addError(CmMessageRepository.getServerMessage(CmMessages.MANDATORY_NODE_MISSING, nodeName));
        }
        return node;
    }

    /**
     * Populate Output
     * @param matchDetailsList Match Details List
     * @param payorAccountId Payor Account ID
     */
    private void populateOutput(List<CmPaymentServiceData> matchDetailsList, String payorAccountId) {
        Element pyUploadList;
        Element payCharList;
        boolean isCharAdded = false;
        for (CmPaymentServiceData matchDetail : matchDetailsList) {
            pyUploadList = payUploadGroup.addElement(CmPreProcessPaymentInterfaceMatchingRulesConstants.PAY_UPLOAD_LIST);
            pyUploadList.addElement(CmPreProcessPaymentInterfaceMatchingRulesConstants.PAYMENT_AMOUNT).setText(
                    matchDetail.getPaymentAmount().getAmount().toString());
            pyUploadList.addElement(CmPreProcessPaymentInterfaceMatchingRulesConstants.MATCH_TYPE).setText(
                    matchDetail.getMatchType());
            pyUploadList.addElement(CmPreProcessPaymentInterfaceMatchingRulesConstants.MATCH_VALUE).setText(
                    matchDetail.getMatchValue());
            pyUploadList.addElement(CmPreProcessPaymentInterfaceMatchingRulesConstants.CUSTOMER_ID).setText(matchDetail.getAccountId());
            
            /*
             *  Code Added By Ishita on 27-07-2020 start
             *  CB-145 
             */
            if(!isCharAdded)
            {
	            payCharList = pyUploadList.addElement(CmPreProcessPaymentInterfaceMatchingRulesConstants.PAY_CHAR_LIST);
	            payCharList.addElement(CmPreProcessPaymentInterfaceMatchingRulesConstants.MATCH_TYPE).setText(
	                    matchDetail.getMatchType());
	            payCharList.addElement(CmPreProcessPaymentInterfaceMatchingRulesConstants.MATCH_VALUE).setText(
	                    matchDetail.getMatchValue());
	            payCharList.addElement(CmPreProcessPaymentInterfaceMatchingRulesConstants.CUSTOMER_ID).setText(matchDetail.getAccountId());
	            payCharList.addElement(CmPreProcessPaymentInterfaceMatchingRulesConstants.SEQUENCE).setText(CmPreProcessPaymentInterfaceMatchingRulesConstants.SEQ10);
	            payCharList.addElement(CmPreProcessPaymentInterfaceMatchingRulesConstants.CHAR_TYPE).setText(getReceiptNumCharType());
	            payCharList.addElement(CmPreProcessPaymentInterfaceMatchingRulesConstants.ADHOC_CHAR_VAL).setText(payUploadGroup.selectSingleNode(CmPreProcessPaymentInterfaceMatchingRulesConstants.RECEIPT_NUM).getText());
	            payCharList.addElement(CmPreProcessPaymentInterfaceMatchingRulesConstants.SRCH_CHAR_VAL).setText(payUploadGroup.selectSingleNode(CmPreProcessPaymentInterfaceMatchingRulesConstants.RECEIPT_NUM).getText());
	            isCharAdded = true;
            }
            /*
             * Code Added By Ishita on 27-07-2020 end
             */

        }
        
    }

    /**
    * Build Account Identifier List
    * @param paymentMatchInptGrp
    * @return Account Identifier List
    */
    private List<String> buildAccountIdentifierList(Node paymentMatchInptGrp) {
        List<Node> acctIdentifierList = paymentMatchInptGrp
                .selectNodes(CmPreProcessPaymentInterfaceMatchingRulesConstants.ACCT_IDENTIFIERS_LIST);
        List<String> accountIdentifierList = new ArrayList<String>();
        for (Node acctIdentifierNode : acctIdentifierList) {
            if (isNull(acctIdentifierNode
                    .selectSingleNode(CmPreProcessPaymentInterfaceMatchingRulesConstants.PRIM_ACCT_IDENT))) {
                continue;
            }
            accountIdentifierList.add(acctIdentifierNode.selectSingleNode(
                    CmPreProcessPaymentInterfaceMatchingRulesConstants.PRIM_ACCT_IDENT).getText());
        }
        return accountIdentifierList;
    }
    /*
     * Code Added By Ishita on 24-07-2020 start
     * As per the design changes suggested in CB-145
     */
    /**
     * Build BillId List
     * @param paymentMatchInptGrp
     * @return Bill Id List
     */
     private List<String> buildBillIdList(Node paymentMatchInptGrp) {
         List<Node> billIds = paymentMatchInptGrp
                 .selectNodes(CmPreProcessPaymentInterfaceMatchingRulesConstants.BILL_IDS_LIST);
         List<String> billIdList = new ArrayList<String>();
         for (Node billId : billIds) {
             if (isNull(billId.selectSingleNode(CmPreProcessPaymentInterfaceMatchingRulesConstants.BILL_ID)))
             {
                 continue;
             }
             billIdList.add(billId.selectSingleNode(
                     CmPreProcessPaymentInterfaceMatchingRulesConstants.BILL_ID).getText());
         }
         return billIdList;
     }
     /**
      * Build OrderNum List
      * @param paymentMatchInptGrp
      * @return Order Number List
      */
      private List<String> buildOrderNumList(Node paymentMatchInptGrp) {
          List<Node> OrderNums = paymentMatchInptGrp
                  .selectNodes(CmPreProcessPaymentInterfaceMatchingRulesConstants.ORDER_NUM_LIST);
          List<String> orderNumList = new ArrayList<String>();
          for (Node orderNum : OrderNums) {
              if (isNull(orderNum.selectSingleNode(CmPreProcessPaymentInterfaceMatchingRulesConstants.ORDER_NUM)))
              {
                  continue;
              }
              orderNumList.add(orderNum.selectSingleNode(
                      CmPreProcessPaymentInterfaceMatchingRulesConstants.ORDER_NUM).getText());
          }
          return orderNumList;
      }
      /**
       * Build Loan Number List
       * @param paymentMatchInptGrp
       * @return Loan Number List
       */
       private List<String> buildLoanNumList(Node paymentMatchInptGrp) {
           List<Node> loanNums = paymentMatchInptGrp
                   .selectNodes(CmPreProcessPaymentInterfaceMatchingRulesConstants.LOAN_NUM_LIST);
           List<String> loanNumList = new ArrayList<String>();
           for (Node loanNum : loanNums) {
               if (isNull(loanNum.selectSingleNode(CmPreProcessPaymentInterfaceMatchingRulesConstants.LOAN_NUMBER)))
               {
                   continue;
               }
               loanNumList.add(loanNum.selectSingleNode(
                       CmPreProcessPaymentInterfaceMatchingRulesConstants.LOAN_NUMBER).getText());
           }
           return loanNumList;
       }
    
    /*
     * Code Changes done By Ishita on 24-07-2020 end
     */

    /**
     * Parse Date and raise error if date format is not valid
     * @param dateString Date String
     * @param dateFormat Valid Date Format
     * @return Date
     */
    private Date parseDate(String dateString, DateFormat dateFormat) {
        Date date = null;
        try {
            date = dateFormat.parseDate(dateString);
        } catch (DateFormatParseException e) {
            addError(CmMessageRepository.getServerMessage(CmMessages.ENTITY_NOT_VALID, "Invalid Date Format", dateString));
        }
        return date;
    }

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

    public String getMessageCategory() {
        return null;
    }

    public String getMessageNumber() {
        return null;
    }

    public String getMessageParam1() {
        return null;
    }

    public String getMessageParam2() {
        return null;
    }

    public String getMessageParam3() {
        return null;
    }

    public String getMessageParam4() {
        return null;
    }

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
        return null;
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

    public void setMessageCategory(String arg0) {
    }

    public void setMessageNumber(String arg0) {
    }

    public void setMessageParam1(String arg0) {
    }

    public void setMessageParam2(String arg0) {
    }

    public void setMessageParam3(String arg0) {
    }

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
    	requestPayload = arg0;
    }

    @Override
    public void setRequestType(String arg0) {
    	requestType = arg0;
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

    public String getDataReTransformRequired() {
        return null;
    }

    public FileTransformationRecord getInputGroupRecordMap() {
        return null;
    }

    public FileTransformationRecord getMapFieldXpathMap() {
        return null;
    }

    public void setDataReTransformRequired(String arg0) {
    }

    public void setInputGroupRecordMap(FileTransformationRecord arg0) {
    }

    public void setMapFieldXpathMap(FileTransformationRecord arg0) {
    }

}
