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
 * The Billable Charge Interface Staging batch will process records in
 * the Billable Charge Staging table that are in Pending status, 
 * and proceed on processing to create billable charges. 
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:         Reason:
 * 2020-04-24	DDejes		CB-36
 * 2020-05-15	DDejes		CB-72
 * 2020-05-15	DDejes 		CB-73
 * 2020-07-08   SPatil		CB-163
 * 2020-09-04   SPatil		CB-360
 ***********************************************************************
 */
package com.splwg.cm.domain.billing.batch;



import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.Query;
import com.splwg.base.api.batch.JobWork;
import com.splwg.base.api.batch.RunAbortedException;
import com.splwg.base.api.batch.StandardCommitStrategy;
import com.splwg.base.api.batch.ThreadAbortedException;
import com.splwg.base.api.batch.ThreadExecutionStrategy;
import com.splwg.base.api.batch.ThreadWorkUnit;
import com.splwg.base.api.batch.WorkUnitResult;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.COTSInstanceList;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.businessObject.batch.MessageRepository;
import com.splwg.base.api.businessService.BusinessServiceDispatcher;
import com.splwg.base.api.businessService.BusinessServiceInstance;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.lookup.CharacteristicTypeLookup;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.characteristicType.CharacteristicType_Id;
import com.splwg.base.support.context.FrameworkSession;
import com.splwg.base.support.context.SessionHolder;
import com.splwg.ccb.api.lookup.BillableChargeStatusLookup;
import com.splwg.ccb.api.lookup.ServiceAgreementStatusLookup;
import com.splwg.ccb.domain.admin.idType.accountIdType.AccountNumberType_Id;
import com.splwg.ccb.domain.admin.serviceAgreementType.ServiceAgreementType;
import com.splwg.ccb.domain.admin.serviceQuantityIdentifier.ServiceQuantityIdentifier_Id;
import com.splwg.ccb.domain.billing.billableCharge.BillableCharge_Id;
import com.splwg.ccb.domain.billing.billableCharge.BilllableChargeCharacteristic;
import com.splwg.ccb.domain.customerinfo.account.AccountNumber;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.customerinfo.serviceAgreement.ServiceAgreement_Id;
import com.splwg.ccb.domain.dataManagement.fileRequest.json.JSONException;
import com.splwg.ccb.domain.dataManagement.fileRequest.json.JSONObject;
import com.splwg.ccb.domain.dataManagement.fileRequest.json.XML;
import com.splwg.ccb.domain.pricing.priceitem.PriceItem_Id;
import com.splwg.ccb.domain.pricing.priceparm.PriceParmValue_Id;
import com.splwg.ccb.domain.pricing.priceparm.PriceParm_Id;
import com.splwg.cm.api.lookup.CmStagingStatusFlagLookupLookup;
import com.splwg.cm.domain.billing.customBusinessEntity.CmBchgIntStg;
import com.splwg.cm.domain.billing.customBusinessEntity.CmBchgIntStg_DTO;
import com.splwg.cm.domain.billing.customBusinessEntity.CmBchgIntStg_Id;
import com.splwg.cm.domain.common.customBusinessComponent.CmCommonAPI;
import com.splwg.cm.domain.common.customBusinessComponent.CmCommonAPI_Impl;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;
import com.splwg.cm.domain.common.messageRepository.CmMessages;
import com.splwg.shared.common.ApplicationError;

/**
 * @author Denise De Jesus
 *
@BatchJob (multiThreaded = true, rerunnable = true, modules={},
 * softParameters = { @BatchJobSoftParameter (name = statusList, required = true, type = string)
 *              , @BatchJobSoftParameter (entityName = characteristicType, name = orderNumberCharacteristicType, required = true, type = entity)
 *              , @BatchJobSoftParameter (name = maxErrors, type = string)
 *              , @BatchJobSoftParameter (name = DIST-THD-POOL, type = string)})
 *            
 */
public class CmBillableChargeInterfaceStagingBatch extends
		CmBillableChargeInterfaceStagingBatch_Gen {

	
	//Constants
	private static final String CM_STG_STATUS_FLG = "CM_STG_STATUS_FLG";    
	private static final String COMMA = ",";
	
    // Soft Parameter
    private String statusList;
     
	  /**
     * Validate batch parameters
     */
    @Override
    public void validateSoftParameters(boolean isNewRun) {
        statusList = getParameters().getStatusList().trim();
        for(String status : statusList.split(COMMA)){

			if(!(CmStagingStatusFlagLookupLookup.constants.CM_PENDING.toString().equalsIgnoreCase(status.trim())
					|| CmStagingStatusFlagLookupLookup.constants.CM_ERROR.toString().equalsIgnoreCase(status.trim()))){
				addError(CmMessageRepository.getServerMessage(CmMessages.INVALID_LOOKUP_VALUE, status.trim(), CM_STG_STATUS_FLG));

			}
		
		}

    }

	public JobWork getJobWork() {
    	String[] list = statusList.split(COMMA);
    	CmBchgIntStg_Id bchgId = null;

    	ArrayList<ThreadWorkUnit> workUnits = new ArrayList<ThreadWorkUnit>();
		List<SQLResultRow> preparedStatementList = null;
		ThreadWorkUnit workUnit = null;
		
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("SELECT BCHG.CM_BCHG_INT_ID ");
		stringBuilder.append("FROM CM_BCHG_INT_STG BCHG ");
		stringBuilder.append("WHERE BCHG.CM_STG_STATUS_FLG IN (");
		for(int i=0; i<list.length; i++){
			if(i==0){
				stringBuilder.append(":status"+i);
			}else{
				stringBuilder.append(", :status"+i);
			}
		}
		stringBuilder.append(")");
		
    	PreparedStatement getCustIntStg = createPreparedStatement(stringBuilder.toString(), "Retrieve Customer Interface Staging");

    	for(int i=0; i<list.length; i++){
    		getCustIntStg.bindString("status"+i, list[i].trim(), "CM_STG_STATUS_FLG");
		}
    
		preparedStatementList = getCustIntStg.list();

		getCustIntStg.close();
		for (SQLResultRow result : preparedStatementList) {
			
			bchgId = new CmBchgIntStg_Id(result.getString("CM_BCHG_INT_ID"));
			workUnit = new ThreadWorkUnit();
			workUnit.setPrimaryId(bchgId);
			workUnits.add(workUnit);
		}
		
		return createJobWorkForThreadWorkUnitList(workUnits);
	}

	public Class<CmBillableChargeInterfaceStagingBatchWorker> getThreadWorkerClass() {
		return CmBillableChargeInterfaceStagingBatchWorker.class;
	}

	public static class CmBillableChargeInterfaceStagingBatchWorker extends
			CmBillableChargeInterfaceStagingBatchWorker_Gen {
		
    	// Work Parameters
    	private int recordsInError;
    	private int recordsProcessed;
    	private Bool errorEncountered;
    	private FrameworkSession session;
    
		//Start Add - CB-163
    	private Query<BillableCharge_Id> query=null;
    	private StringBuilder queryString= null;
    	private CharacteristicType orderNumChar = null;
    	//End  Add - CB-163

    	// Constants
    	private static final String SAVE_POINT = "SAVE_POINT";
    	private static final String BLANK_STRING = "";
    	private static final String BO = "C1-BILLCHARGE";
		//Start Add - CB-163
    	private static final String DELIMITER = ",";
    	//End Add - CB-163
    	
		public ThreadExecutionStrategy createExecutionStrategy() {
			return new StandardCommitStrategy(this);
		}

		public WorkUnitResult executeWorkUnitDetailedResult(ThreadWorkUnit unit)
				throws ThreadAbortedException, RunAbortedException {
			WorkUnitResult workUnitResult = new WorkUnitResult(true);

    		recordsProcessed = 0;
    		recordsInError = 0;
    		errorEncountered= Bool.FALSE;
    		CmBchgIntStg_Id bchgId = null;
    		CmBchgIntStg bchg = null;
    		CmCommonAPI commonAPI = new CmCommonAPI_Impl();
    		
    		try {
    			bchgId = (CmBchgIntStg_Id) unit.getPrimaryId();
    			bchg = bchgId.getEntity();
    			session = (FrameworkSession) SessionHolder.getSession();
    			session.setSavepoint(SAVE_POINT);
    		} catch (Exception e) {
    			if (notNull(e.getLocalizedMessage())) {

    				addError(CmMessageRepository.getServerMessage(CmMessages.BATCH_PROC_ERROR, getBatchControlId().getIdValue(),
    						bchgId.getIdValue(), e.getLocalizedMessage()));
    			} else {
    		
    				addError(CmMessageRepository.getServerMessage(CmMessages.BATCH_PROC_ERROR, getBatchControlId().getIdValue(),
    						bchgId.getIdValue(), e.getLocalizedMessage()));
    			}
    		}

    		try {
    			JSONObject json;
    			try {
    				// Convert json to XML
    				String boDataFormatted = commonAPI.jsonFormatter(bchg.getBusinessObjectDataArea());
    				json = new JSONObject("{value:".concat(boDataFormatted).concat("}"));
    				String xml = XML.toString(json).trim();

    				// Validate Values and create Billable Charge
    				validateAndCreateBillCharge(bchg, xml);

    			} catch (JSONException e) {
    				errorEncountered = Bool.TRUE;
    				setCustIntStgStatus(bchg, null, CmStagingStatusFlagLookupLookup.constants.CM_ERROR, CmMessageRepository.getServerMessage(CmMessages.JSON_FORMAT_ERROR, BLANK_STRING).getMessageText());
    			//Start Change CB-72
    			//} catch (ApplicationException e) {
    			}catch(ApplicationError e){
    			//End Change CB-72
    				errorEncountered = Bool.TRUE;
    				//Start Change CB-72
    				//setCustIntStgStatus(bchg, null, CmStagingStatusFlagLookupLookup.constants.CM_ERROR, e.getMessage().toString());
    				setCustIntStgStatus(bchg, null, CmStagingStatusFlagLookupLookup.constants.CM_ERROR, e.getServerMessage().getMessageText());
        			//End Change CB-72
    			}


    		//Start Change CB-72
    		//} catch (ApplicationException e) {
    		}catch(ApplicationError e){
    	    //End Change CB-72
    			errorEncountered = Bool.TRUE;
    			// Error Customer Interface Staging record
    			//setCustIntStgStatus(bchg, null, CmStagingStatusFlagLookupLookup.constants.CM_ERROR, e.getMessage().toString());
    			setCustIntStgStatus(bchg, null, CmStagingStatusFlagLookupLookup.constants.CM_ERROR, e.getServerMessage().getMessageText());
    			session.rollbackToSavepoint(SAVE_POINT);         
    		}
    		if (notNull(bchg)) {
    			recordsProcessed++;
    			if (errorEncountered.isTrue()) {                   
    				recordsInError++;
    			}

    			saveChanges();
    		}

    		workUnitResult.setUnitsInError(recordsInError);
    		workUnitResult.setUnitsProcessed(recordsProcessed);

    		return workUnitResult;
		}
		
		@SuppressWarnings("unchecked")	
		private void validateAndCreateBillCharge(CmBchgIntStg bchg, String xml){
			Account_Id accountId = null;
			//Start Change - CB-163
			//Start CB-360
			//Person_Id personId = null;
			//IdType_Id personIdType = null;
			AccountNumberType_Id acctIdType = null;
			//End CB - 360
			List<Node> billableChargePriceParamList = null;
			PriceParm_Id paramCode = null;
			PriceParmValue_Id paramValue = null;
			//End Change CB-163
			Document boDataAreaXml = null;
			ServiceAgreement_Id contractId = null;
			Node rootNode = null;

			String priceItemCode = BLANK_STRING;
			String descriptionOnBill = BLANK_STRING;
			String contractTypeId = BLANK_STRING;
			String startDate= BLANK_STRING;
			String endDate = BLANK_STRING;
			
			//Start Change - CB-163
			//String accountIdentifierType = BLANK_STRING;
			//String accountIdentifierValue = BLANK_STRING;
			String customerIdentifierType = BLANK_STRING;
			String customerIdentifierValue = BLANK_STRING;
			String parameterCode = BLANK_STRING;
    		String parameterValue = BLANK_STRING;
			StringBuilder sb = new StringBuilder();
    		StringBuilder sb1 = new StringBuilder();
			String parameterCodeString =BLANK_STRING;
    		String parameterCodeValue= BLANK_STRING;
    		String priceGroupId=BLANK_STRING;
			String FLAG="Y";
			//End Change - CB-163	


			try {
				boDataAreaXml = DocumentHelper.parseText(xml);
				rootNode = boDataAreaXml.getRootElement();
			

				// Retrieve Start Date
				startDate = isNull(rootNode.selectSingleNode("startDate"))? BLANK_STRING
						: rootNode.selectSingleNode("startDate").getText().trim();
				  if(isBlankOrNull(startDate)){
					addError(MessageRepository.fieldMissing("Start Date"));
				    }
			

				//Start Change - CB-163
				/*// Retrieve Account Identifier Type
				accountIdentifierType = isNull(rootNode.selectSingleNode("accountIdentifierType"))? BLANK_STRING
						: rootNode.selectSingleNode("accountIdentifierType").getText().trim();
				if(isBlankOrNull(accountIdentifierType)){
					addError(MessageRepository.fieldMissing("External Account Identifier Type"));
				}*/
				
				/*// Retrieve Account Identifier Value
				accountIdentifierValue = isNull(rootNode.selectSingleNode("accountIdentifierValue"))? BLANK_STRING
						: rootNode.selectSingleNode("accountIdentifierValue").getText().trim();
				if(isBlankOrNull(accountIdentifierValue)){
					addError(MessageRepository.fieldMissing("External Account Identifier Value"));
				}*/
				
				/*// Validate Account Identifier Type if Valid
				acctIdType = new AccountNumberType_Id(accountIdentifierType);
				if(isNull(acctIdType.getEntity())){
					addError(MessageRepository.fieldMissing("Account Identifier Type"));
				}
				*/
				// Retrieve Customer Identifier Type
				customerIdentifierType = isNull(rootNode.selectSingleNode("customerIdentifierType"))? BLANK_STRING
				        : rootNode.selectSingleNode("customerIdentifierType").getText().trim();
		        if(isBlankOrNull(customerIdentifierType)){
			         addError(MessageRepository.fieldMissing("External Customer Identifier Type"));
		          }
		        
				// Retrieve Customer Identifier Value
				customerIdentifierValue = isNull(rootNode.selectSingleNode("customerIdentifierValue"))? BLANK_STRING
						: rootNode.selectSingleNode("customerIdentifierValue").getText().trim();
				if(isBlankOrNull(customerIdentifierValue)){
					addError(MessageRepository.fieldMissing("External Customer Identifier Value"));
				 }
				
				//Start CB-360
				// Validate Account Identifier Type if Valid
				acctIdType = new AccountNumberType_Id(customerIdentifierType);
				if(isNull(acctIdType.getEntity())){
					//End CB-360
			    //Start Change CB-246			
				//addError(MessageRepository.fieldMissing("Customer Identifier Type"));
				addError(CmMessageRepository.getServerMessage(CmMessages.INVALID_ENTITY, "Customer Identifier Type", customerIdentifierType));
				//End Change CB-246   
		
				}
				//End Change - CB-163

				// Retrieve and Validate Price Item Code
				priceItemCode = isNull(rootNode.selectSingleNode("priceItemCode"))? BLANK_STRING
						: rootNode.selectSingleNode("priceItemCode").getText().trim();
				//Start Change CB-246
				if(isBlankOrNull(priceItemCode)){
			         addError(MessageRepository.fieldMissing("Price Item Code"));
		          }
				//End Change CB-246
				PriceItem_Id priceItem = new PriceItem_Id(priceItemCode);
				if(isNull(priceItem.getEntity())){
					addError(CmMessageRepository.getServerMessage(CmMessages.INVALID_ENTITY, "Price Item", priceItemCode));
				}
				

				//Validate Billable Charge Service Quantity
				if(rootNode.selectNodes("billableChargeServiceQuantity").isEmpty()){
					addError(CmMessageRepository.getServerMessage(CmMessages.NO_SQI));
				}
				
				//Start CB-360
				/*//Start Change - CB-163
				// Retrieve and Validate Person Id given Customer Identifier Type and Customer Identifier Value
				personId = retrievePerId(personIdType, customerIdentifierValue);
				if(isNull(personId)){
					addError(CmMessageRepository.getServerMessage(CmMessages.NO_PERSON_ID_RETRIEVED, customerIdentifierType
							, customerIdentifierValue));
				}*/
			    // End CB-360
				
				//Start CB-360
				// Retrieve and Validate Account Id given Account Identifier Type and Account Identifier Value
				accountId = retrieveAcctId(acctIdType, customerIdentifierValue);
				if(isNull(accountId)){
					addError(CmMessageRepository.getServerMessage(CmMessages.NO_ACCOUNT_ID_RETRIEVED, customerIdentifierType
							, customerIdentifierValue));
				}
				
				/*// Retrieve and validate the Account Id for given person Id and Main Customer flag
				accountId = retrieveAcctId(personId);
				if(isNull(accountId))
				{
					addError(CmMessageRepository.getServerMessage(CmMessages.NO_ACCOUNT_ID_RETRIEVED,customerIdentifierType ,customerIdentifierValue));
				}	*/
				
				//End CB-360
				
				//End Change - CB-163

				// Retrieve and Validate Contract Type given Price Item
				contractTypeId = retrieveContractType(priceItem).trim();
				if(isBlankOrNull(contractTypeId)){
					addError(CmMessageRepository.getServerMessage(CmMessages.NO_CONTRACT_TYPE_RETRIEVED, priceItemCode));
				}
			

				// Retrieve and Validate Contract Id given Account Id and Contract Type Id
				contractId = retrieveSA(accountId, contractTypeId);
				if(isNull(contractId)){
					addError(CmMessageRepository.getServerMessage(CmMessages.NO_CONTRACT_RETRIEVED, contractTypeId));
				}
				
				// Retrieve End Date
				endDate = isNull(rootNode.selectSingleNode("endDate"))? BLANK_STRING
						: rootNode.selectSingleNode("endDate").getText().trim();
						

				// Retrieve Description on Bill
				descriptionOnBill = isNull(rootNode.selectSingleNode("descriptionOnBill"))? BLANK_STRING
						: rootNode.selectSingleNode("descriptionOnBill").getText().trim();						
			
				
				//Start Add CB-163
				// Add Billable Charge Price Parameter
				billableChargePriceParamList = rootNode.selectNodes("billableChargePriceParameter");
			
				if(!billableChargePriceParamList.isEmpty()){
				for(Node PrcParmNode : billableChargePriceParamList)
				    {
						// Retrieve Parameter Code
						parameterCode = isNull(PrcParmNode.selectSingleNode("parameterCode"))? BLANK_STRING
    						: PrcParmNode.selectSingleNode("parameterCode").getText().trim();
						//Start Change CB-246
						if(isBlankOrNull(parameterCode)){
					         addError(MessageRepository.fieldMissing("Parameter Code"));
				          }
						//End Change CB-246
    			        paramCode = new PriceParm_Id(parameterCode);	
						if(isNull(paramCode.getEntity())){
							// Start Change - CB-246
							/* addError(CmMessageRepository.getServerMessage(CmMessages.INVALID_ENTITY, "Price Parameter", 
										parameterCode));*/
					     addError(CmMessageRepository.getServerMessage(CmMessages.INVALID_ENTITY, "Parameter Code", 
							parameterCode));
				        }
						   // End Change - CB-246
						sb.append(parameterCode + DELIMITER);
						
						// Retrieve Parameter Value
						parameterValue = isNull(PrcParmNode.selectSingleNode("parameterValue"))? BLANK_STRING
						: PrcParmNode.selectSingleNode("parameterValue").getText().trim();
						//Start Change -CB-246
						if(isBlankOrNull(parameterValue)){
					         addError(MessageRepository.fieldMissing("Parameter Value"));
				          }
						//End Change - CB-246
						
						paramValue = new PriceParmValue_Id(paramCode, parameterValue);
						//Start Change CB-247
						//if(isNull(paramValue))
						if(isNull(paramValue.getEntity()))
						//End Change CB-247	
						{
							 addError(CmMessageRepository.getServerMessage(CmMessages.INVALID_ENTITY, "Parameter Value", 
									 parameterValue));	
						}
						
						sb1.append(parameterValue + DELIMITER);
					}
				if(notNull(sb))
				{
				   parameterCodeString = sb.substring(0, sb.length()-1).trim();
				}
					
				if(notNull(sb1))
				{
				   parameterCodeValue = sb1.substring(0, sb1.length()-1).trim();
				}
				
				BusinessServiceInstance parmServiceInstance = BusinessServiceInstance.create("C1_PriceItemParmGroupService");
    			parmServiceInstance.set("flag",FLAG);
    		    parmServiceInstance.set("priceItem",priceItemCode);
    		    parmServiceInstance.set("priceItemParmCode", parameterCodeString);
    		    parmServiceInstance.set("priceItemParmValue", parameterCodeValue);
    		    BusinessServiceInstance responseInstance = BusinessServiceDispatcher.execute(parmServiceInstance);
    		  
    		    priceGroupId=responseInstance.getElement().selectSingleNode("groupId").getStringValue().trim();
    		   
    		    if(isEmptyOrNull(priceGroupId))
    		    {
    		    	addError(CmMessageRepository.getServerMessage(CmMessages.UNMATCHED_PRICEPARAM, parameterCodeString
							, parameterCodeValue));
    		    }
    		    
		     }				
			//End Add - CB-163
                
				 //Create Billable Charge
				//Start Change - CB-163
				/*createBillableCharge(bchg, rootNode, contractId.getIdValue(), priceItemCode, startDate, endDate, descriptionOnBill, xml);*/
				createBillableCharge(bchg, rootNode, contractId.getIdValue(), priceItemCode, startDate, endDate, descriptionOnBill, xml,priceGroupId);
				//End Change - CB-163

			//Start Change CB-72
			//} catch (Exception e) {
			} catch (DocumentException e) {
				setCustIntStgStatus(bchg, null, CmStagingStatusFlagLookupLookup.constants.CM_ERROR, e.getMessage().toString());
			}catch(ApplicationError e){
			//End Change CB-72
				errorEncountered = Bool.TRUE;

				// Error Customer Interface Staging record
				//Start Change CB-72
				//setCustIntStgStatus(bchg, null, CmStagingStatusFlagLookupLookup.constants.CM_ERROR, e.getMessage().toString());
				setCustIntStgStatus(bchg, null, CmStagingStatusFlagLookupLookup.constants.CM_ERROR, e.getServerMessage().getMessageText());
				//End Change CB-72
			}
		}
		
		@SuppressWarnings("unchecked")
		//Start Change - CB-163
		/*private void createBillableCharge(CmBchgIntStg bchg, Node boDataAreaXml, String contractId, String priceItemCode, String startDate, String endDate
				, String descriptionOnBill, String xml){*/
		private void createBillableCharge(CmBchgIntStg bchg, Node boDataAreaXml, String contractId, String priceItemCode, String startDate, String endDate
				, String descriptionOnBill, String xml, String priceGroupId){		
		//End Change - CB-163	
    		List<Node> billableChargeSQList;
    		List<Node> billableChargeChar;
    		COTSInstanceList billableChargeServiceQuantity = null;
    		COTSInstanceList billableChargeCharacteristic = null;
    		COTSInstanceListNode sqNode = null;
    		COTSInstanceListNode characteristicNode = null;
    		ServiceQuantityIdentifier_Id sqiId = null;
    		CharacteristicType charType = null;
    		BillableCharge_Id bchgId = null;
    		
			//Start Add - CB-163
			CharacteristicType_Id charTypeId = null;
			BillableCharge_Id billChargeId= null;
			//End Add - CB-163
    		
    		String sqiString = BLANK_STRING;
    		String dailyServiceQuantity = BLANK_STRING;
    		String characteristicType = BLANK_STRING;
    		String characteristicValue = BLANK_STRING;
    		String effectiveDate = BLANK_STRING;

    		
    		try{ 

    			// Create New Billable Charge	
    			BusinessObjectInstance boInstance = BusinessObjectInstance.create(BO);
    			boInstance.set("billableChargeStatus", BillableChargeStatusLookup.constants.BILLABLE);
    			boInstance.set("serviceAgreement", contractId);
    			boInstance.set("priceItemCode", priceItemCode);
    			boInstance.getFieldAndMD("startDate").setXMLValue(startDate);
    			boInstance.getFieldAndMD("endDate").setXMLValue(endDate);
    			boInstance.set("descriptionOnBill", descriptionOnBill);
    			//Start Add - CB-163
    			
    			//Start Change - CB-240
    			//boInstance.set("priceItemParmGroupId", new BigDecimal(priceGroupId));
    			if(!isEmptyOrNull(priceGroupId))
    			{
				boInstance.set("priceItemParmGroupId", new BigDecimal(priceGroupId));
    			}
    			//End Change - CB-240
  
				//End Add - CB-163

    			// Add Billable Charge SQ
    			billableChargeSQList = boDataAreaXml.selectNodes("billableChargeServiceQuantity");
    			Integer i = 1;
    			for(Node sq : billableChargeSQList){

    				// Retrieve SQI
    				sqiString = isNull(sq.selectSingleNode("serviceQuantityIdentifier"))? BLANK_STRING
    						: sq.selectSingleNode("serviceQuantityIdentifier").getText().trim();
    				//Start Change CB-246
    				if(isBlankOrNull(sqiString)){
				         addError(MessageRepository.fieldMissing("Service Quantity"));
			          }
    				//End Change CB-246

    				sqiId = new ServiceQuantityIdentifier_Id(sqiString);
    				if(isNull(sqiId.getEntity())){
    					addError(CmMessageRepository.getServerMessage(CmMessages.INVALID_ENTITY, "Service Quantity Identifier",
    							sqiString));

    				}

    				// Retrieve Daily SQ
    				dailyServiceQuantity = isNull(sq.selectSingleNode("dailyServiceQuantity"))? BLANK_STRING
    						: sq.selectSingleNode("dailyServiceQuantity").getText().trim();

    				billableChargeServiceQuantity = boInstance.getList("billableChargeServiceQuantity");

    				// Set Billable SQ
    				sqNode = billableChargeServiceQuantity.newChild();
    				sqNode.set("serviceQuantityIdentifier", sqiString);
    				sqNode.getFieldAndMD("dailyServiceQuantity").setXMLValue(dailyServiceQuantity);
    				sqNode.getFieldAndMD("sequence").setXMLValue(i.toString());

    				i++;

    			}

    			// Add Billable Charge Char
    			billableChargeChar = boDataAreaXml.selectNodes("billableChargeCharacteristic");
    			//Start Change CB-240
    			if(!billableChargeChar.isEmpty())
    			{ //End Change CB-240
    			for(Node charNode : billableChargeChar){

    				// Retrieve Characteristic Type
    				characteristicType = isNull(charNode.selectSingleNode("characteristicType"))? BLANK_STRING
    						: charNode.selectSingleNode("characteristicType").getText().trim();
    				//Start Change CB-246
    				if(isBlankOrNull(characteristicType)){
				         addError(MessageRepository.fieldMissing("Characteristic Type"));
			          }
    				//End Change CB-246
					//Start Change - CB-163		
					charTypeId = new CharacteristicType_Id(characteristicType);
				
    				//charType = new CharacteristicType_Id(characteristicType); 
					if(isNull(charTypeId.getEntity())){
						addError(CmMessageRepository.getServerMessage(CmMessages.INVALID_ENTITY, "Characteristic Type", 
    							characteristicType));
					}
					else{
					    charType = charTypeId.getEntity();
					}

    				/*if(isNull(charType.getEntity())){
    					addError(CmMessageRepository.getServerMessage(CmMessages.INVALID_ENTITY, "Characteristic Type", 
    							characteristicType));
    				}*/
					if(isNull(charType)){
    					addError(CmMessageRepository.getServerMessage(CmMessages.INVALID_ENTITY, "Characteristic Type", 
    							characteristicType));
    				}
					//End Change - CB-163

    				// Retrieve Characteristic Value
    				characteristicValue = isNull(charNode.selectSingleNode("characteristicValue"))? BLANK_STRING
    						: charNode.selectSingleNode("characteristicValue").getText().trim();
					
					//Start Add - CB-163
    				orderNumChar= getParameters().getOrderNumberCharacteristicType();
    				if(charType.equals(orderNumChar))
    				{
    					
    					billChargeId=retrieveBillableCharge(orderNumChar,characteristicValue);
					    if(notNull(billChargeId)){
						 addError(CmMessageRepository.getServerMessage(CmMessages.BILLABLECHARGE_EXISTS,characteristicValue));
					 }
    					
    				}
    				//End Add - CB-163

    				// Retrieve Effective Date
    				effectiveDate = isNull(charNode.selectSingleNode("effectiveDate"))? BLANK_STRING
    						: charNode.selectSingleNode("effectiveDate").getText().trim();

    				billableChargeCharacteristic = boInstance.getList("billlableChargeCharacteristic");

    				// Set Billable Charge Characteristic
    				characteristicNode = billableChargeCharacteristic.newChild();
    				characteristicNode.set("characteristicType", characteristicType);
					//Start Change - CB-163
    				//characteristicNode.set("characteristicValue", characteristicValue);
					if(charType.getCharacteristicType().equals(CharacteristicTypeLookup.constants.ADHOC_VALUE))
    				{
    					characteristicNode.set("adhocCharacteristicValue", characteristicValue);	
    				}
    				else if(charType.getCharacteristicType().equals(CharacteristicTypeLookup.constants.FOREIGN_KEY_VALUE))
        			{
        					characteristicNode.set("characteristicValueForeignKey1", characteristicValue);	
        			}
    				else if(charType.getCharacteristicType().equals(CharacteristicTypeLookup.constants.PREDEFINED_VALUE))
        			{
        					characteristicNode.set("characteristicValue", characteristicValue);	
        			}
					//End Change - CB-163
					
    				if(notBlank(effectiveDate)){
    					characteristicNode.getFieldAndMD("effectiveDate").setXMLValue(effectiveDate);
    				}else{
    					characteristicNode.getFieldAndMD("effectiveDate").setXMLValue(startDate);
    				}

    			}
    		}
    			

    			bchgId = new BillableCharge_Id(BusinessObjectDispatcher.add(boInstance).getString("billableChargeId"));
    			setCustIntStgStatus(bchg, bchgId, CmStagingStatusFlagLookupLookup.constants.CM_PROCESSED, BLANK_STRING);


    		//Start Change CB-72
    		//} catch (Exception e) {
    		}catch(ApplicationError e){
    		//End Change CB-72
    			errorEncountered = Bool.TRUE;
    			// Error Customer Interface Staging record
        		//Start Change CB-72
    			//setCustIntStgStatus(bchg, null, CmStagingStatusFlagLookupLookup.constants.CM_ERROR, e.getMessage().toString());
    			setCustIntStgStatus(bchg, null, CmStagingStatusFlagLookupLookup.constants.CM_ERROR, e.getServerMessage().getMessageText());
        		//End Change CB-72
    		}
		}
		
		//Start Change CB-360
		/*//Start Change - CB-163
		*//**
		 * Retrieve Person Id
		 * @param personIdType
		 * @param personIdValue
		 * @return Person Id
		 *//*
		private Person_Id retrievePerId(IdType_Id personIdType, String personIdValue){

			StringBuilder queryString = new StringBuilder();
			queryString.append("FROM PersonId perId       ");
			queryString.append("WHERE perId.id.idType.id = :personIdType  ");
			queryString.append("AND perId.personIdNumber = :personIdValue  ");

			Query<Person_Id> query = createQuery(queryString.toString(), "retrievePersonId");
			query.bindId("personIdType", personIdType);
			query.bindStringProperty("personIdValue", PersonId.properties.personIdNumber, personIdValue);
			query.addResult("perId.id.person.id", "perId.id.person.id");

			return query.firstRow();

		}*/
		
		
		/**
		 * Retrieve Account Id
		 * @param acctIdType
		 * @param acctNumber
		 * @return Account Id
		 */
		private Account_Id retrieveAcctId(AccountNumberType_Id acctIdType, String acctNumber){

			StringBuilder queryString = new StringBuilder();
			queryString.append("FROM AccountNumber acctNumber       ");
			queryString.append("WHERE acctNumber.id.accountIdentifierType.id = :acctIdType  ");
			queryString.append("AND acctNumber.accountNumber = :acctNumber  ");

			Query<Account_Id> query = createQuery(queryString.toString(), "retrieveAccountId");
			query.bindId("acctIdType", acctIdType);
			query.bindStringProperty("acctNumber", AccountNumber.properties.accountNumber, acctNumber);
			query.addResult("acctNumber.id.account.id", "acctNumber.id.account.id");

			return query.firstRow();

		}
		//End Change CB-360
		//Start Change CB-360
		/**
		 * Retrieve Account Id
		 * @param personId
		 * @return Account Id
		 *//*
		private Account_Id retrieveAcctId(Person_Id personId){

			StringBuilder queryString = new StringBuilder();
			queryString.append("FROM AccountPerson acctPer       ");
			queryString.append("WHERE acctPer.id.person.id = :personId  ");
			queryString.append("AND acctPer.isMainCustomer = :isMainCust  ");

			Query<Account_Id> query = createQuery(queryString.toString(), "retrieveAccountId");
			query.bindId("personId", personId);
			query.bindLookup("isMainCust", YesNoOptionLookup.constants.YES);
			query.addResult("acctPer.id.account.id", "acctPer.id.account.id");

			
			return query.firstRow();

		}
*/		
		// End Change CB-360
		/**
		 * Retrieve BillableCharge Id
		 * @param characteristicType
		 * @param characteristicValue
		 * @return billChargeId 
		 */
		private BillableCharge_Id retrieveBillableCharge(CharacteristicType characteristicType , String characteristicValue){
			
			queryString = new StringBuilder();
			
			queryString.append("FROM BilllableChargeCharacteristic bc ");
			queryString.append("WHERE bc.id.characteristicType.id=:characteristicType ");
			queryString.append("AND bc.searchCharacteristicValue=:searchCharacteristicValue ");
		    query = createQuery(queryString.toString(), "retrieveBillableCharge");
			query.bindId("characteristicType", characteristicType.getId());
			query.bindStringProperty("searchCharacteristicValue",BilllableChargeCharacteristic.properties.searchCharacteristicValue, characteristicValue);
			query.addResult("billableChargeId","bc.id.billableCharge.id");
			 
			return query.firstRow();
			
			
		}
		//End Change - CB-163
		
		/**
		 * Retrieve Contract Type
		 * @param priceItem
		 * @return Contract Type
		 */
		private String retrieveContractType(PriceItem_Id priceItem){
			StringBuilder queryString = new StringBuilder();

			queryString.append("FROM PriceItem priceItem       ");
			queryString.append("WHERE priceItem.id = :priceItem  ");

			Query<String> query = createQuery(queryString.toString(), "retrieveContractType");
			query.bindId("priceItem", priceItem);
			query.addResult("priceItem.saType", "priceItem.saType");

			List<String> contractTypeList = query.list();
			if (contractTypeList.size() < 1) {
				addError(CmMessageRepository.getServerMessage(CmMessages.NO_CONTRACT_TYPE_RETRIEVED, priceItem.getIdValue()));
			}
			return query.firstRow();

		}
		/**
		 * Retrieve Contract Id
		 * @param accountId
		 * @param contractTypeId
		 * @return Contract Id
		 */
		private ServiceAgreement_Id retrieveSA(Account_Id accountId, String contractTypeId){
			StringBuilder queryString = new StringBuilder();

			queryString.append("FROM ServiceAgreement sa       ");
			queryString.append("WHERE sa.account.id = :accountId  ");
			queryString.append("AND sa.serviceAgreementType.id.saType = :contractTypeId  ");
			queryString.append("AND sa.status IN (:pendingStart, :activeStat, :pendingStop) ");


			Query<ServiceAgreement_Id> query = createQuery(queryString.toString(), "retrieveContractId");
			query.bindId("accountId", accountId);
			query.bindStringProperty("contractTypeId", ServiceAgreementType.properties.saType, contractTypeId);
			query.bindLookup("pendingStart", ServiceAgreementStatusLookup.constants.PENDING_START);
			query.bindLookup("activeStat", ServiceAgreementStatusLookup.constants.ACTIVE);
			query.bindLookup("pendingStop", ServiceAgreementStatusLookup.constants.PENDING_STOP);
			query.addResult("sa.id", "sa.id");

			List<ServiceAgreement_Id> contractList = query.list();

			if (contractList.size() < 1) {
				addError(CmMessageRepository.getServerMessage(CmMessages.NO_CONTRACT_RETRIEVED, contractTypeId));
			//Start Add - CB-73
			}else if(contractList.size() > 1){
				addError(CmMessageRepository.getServerMessage(CmMessages.MORE_THAN_ONE_CONTRACT, contractTypeId, accountId.getIdValue()));
			}
			//End Add - CB-73
			return query.firstRow();
		}
		
		
		/**
    	 * Set Status
    	 * @param custIntStg
    	 * @param statusFlg
    	 * @param message
    	 */
    	private void setCustIntStgStatus(CmBchgIntStg bchg, BillableCharge_Id bchgId, CmStagingStatusFlagLookupLookup statusFlg, String message) {
    		CmBchgIntStg_DTO bchgDto = bchg.getDTO();

			if(notNull(bchgId)){
				bchgDto.setBillableChargeId(bchgId);
			}
    		bchgDto.setCmStatus(statusFlg.trimmedValue());
    		bchgDto.setMessageText(isBlankOrNull(message) ? BLANK_STRING: message);
    		bchgDto.setUpdateDateTime(getProcessDateTime());
    		bchgDto.setUser(getActiveContextUser().getId().getIdValue());
    		bchg.setDTO(bchgDto);
    		
    	}
	}

}
