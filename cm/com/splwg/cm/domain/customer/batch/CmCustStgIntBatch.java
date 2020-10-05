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
 * This batch program will create Inbound Message from Customer Staging
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:         Reason:
 * 2020-04-16	DDejes		CB-37
 * 2020-05-15	DDejes		CB-71
 ***********************************************************************
 */

package com.splwg.cm.domain.customer.batch;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import com.splwg.base.api.batch.JobWork;
import com.splwg.base.api.batch.RunAbortedException;
import com.splwg.base.api.batch.StandardCommitStrategy;
import com.splwg.base.api.batch.ThreadAbortedException;
import com.splwg.base.api.batch.ThreadExecutionStrategy;
import com.splwg.base.api.batch.ThreadWorkUnit;
import com.splwg.base.api.batch.WorkUnitResult;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.businessObject.BusinessObject_Id;
import com.splwg.base.support.context.FrameworkSession;
import com.splwg.base.support.context.SessionHolder;
import com.splwg.ccb.domain.dataManagement.fileRequest.json.JSONException;
import com.splwg.ccb.domain.dataManagement.fileRequest.json.JSONObject;
import com.splwg.ccb.domain.dataManagement.fileRequest.json.XML;
import com.splwg.cm.api.lookup.CmStagingStatusFlagLookupLookup;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;
import com.splwg.cm.domain.customer.customBusinessEntity.CmCustIntStg;
import com.splwg.cm.domain.customer.customBusinessEntity.CmCustIntStg_DTO;
import com.splwg.cm.domain.customer.customBusinessEntity.CmCustIntStg_Id;
import com.splwg.shared.common.ApplicationError;

/**
 * @author DDejes
 *
@BatchJob (multiThreaded = true, rerunnable = true, modules={},
 *         softParameters = { @BatchJobSoftParameter (name = statusList, required = true, type = string)
 *            , @BatchJobSoftParameter (lookupName = personOrBusiness, name = personType, type = lookup)
 *            , @BatchJobSoftParameter (name = MAX-ERRORS, type = integer)
 *            , @BatchJobSoftParameter (name = DIST-THD-POOL, type = string)})
 */
public class CmCustStgIntBatch extends CmCustStgIntBatch_Gen {

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
				addError(CmMessageRepository.invalidLookupValue(status.trim(), CM_STG_STATUS_FLG));

			}
		
		}

    }

    /**
     * Get Job Work using status flag and person type
     */
    @Override
    public JobWork getJobWork() {
    	String[] list = statusList.split(COMMA);
    	CmCustIntStg_Id custIntStgId = null;

    	ArrayList<ThreadWorkUnit> workUnits = new ArrayList<ThreadWorkUnit>();
		List<SQLResultRow> preparedStatementList = null;
		ThreadWorkUnit workUnit = null;
		
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("SELECT CUSTSTG.CM_CUST_INT_STG_ID ");
		stringBuilder.append("FROM CM_CUSTOMER_INT_STG CUSTSTG ");
		stringBuilder.append("WHERE CUSTSTG.CM_STG_STATUS_FLG IN (");
		for(int i=0; i<list.length; i++){
			if(i==0){
				stringBuilder.append(":status"+i);
			}else{
				stringBuilder.append(", :status"+i);
			}
		}
		stringBuilder.append(")");
		if(!isBlankOrNull(getParameters().getPersonType().value())){
			stringBuilder.append(" AND CUSTSTG.PER_OR_BUS_FLG = :perType ");
		}
    	PreparedStatement getCustIntStg = createPreparedStatement(stringBuilder.toString(), "Retrieve Customer Interface Staging");

    	for(int i=0; i<list.length; i++){
    		getCustIntStg.bindString("status"+i, list[i].trim(), "CM_STG_STATUS_FLG");
		}
    	if(!isBlankOrNull(getParameters().getPersonType().value())){
    		getCustIntStg.bindLookup("perType", getParameters().getPersonType()); 
    	}
		preparedStatementList = getCustIntStg.list();
		getCustIntStg.close();
		for (SQLResultRow result : preparedStatementList) {
			
			custIntStgId = new CmCustIntStg_Id(result.getString("CM_CUST_INT_STG_ID"));
			workUnit = new ThreadWorkUnit();
			workUnit.setPrimaryId(custIntStgId);
			workUnits.add(workUnit);
		}
		
		return createJobWorkForThreadWorkUnitList(workUnits);
    }

    /**
     * Thread worker class
     */
    public Class<CmCustStgIntBatchWorker> getThreadWorkerClass() {
        return CmCustStgIntBatchWorker.class;
    }


    public static class CmCustStgIntBatchWorker extends CmCustStgIntBatchWorker_Gen {
    	
    	// Work Parameters
    	private int recordsInError;
    	private int recordsProcessed;
    	private Bool errorEncountered;
    	private FrameworkSession session;
    	
    	// Constants
    	private static final String SAVE_POINT = "SAVE_POINT";
    	private static final String BLANK_STRING = "";
    	private static final String BO = "CM-CustomerInterfaceInboundMsg";


    	public ThreadExecutionStrategy createExecutionStrategy() {
			return new StandardCommitStrategy(this);
		}

    	@Override
    	public void initializeThreadWork(boolean initializationPreviouslySuccessful) throws ThreadAbortedException,
    	RunAbortedException {
    	}

    	@Override
    	public WorkUnitResult executeWorkUnitDetailedResult(ThreadWorkUnit unit) throws ThreadAbortedException,
    	RunAbortedException {

    		WorkUnitResult workUnitResult = new WorkUnitResult(true);

    		recordsProcessed = 0;
    		recordsInError = 0;
    		errorEncountered= Bool.FALSE;
    		CmCustIntStg_Id custIntStgId = null;
    		CmCustIntStg custIntStg = null;
    		
    		try {
    			custIntStgId = (CmCustIntStg_Id) unit.getPrimaryId();
    			custIntStg = custIntStgId.getEntity();
    			session = (FrameworkSession) SessionHolder.getSession();
    			session.setSavepoint(SAVE_POINT);
    		} catch (Exception e) {
    			if (notNull(e.getLocalizedMessage())) {

    				addError(CmMessageRepository.batchProcError(getBatchControlId().getIdValue(),
    						custIntStgId.getIdValue(), e.getLocalizedMessage()));
    			} else {
    		
    				addError(CmMessageRepository.batchProcError(getBatchControlId().getIdValue(),
    						custIntStgId.getIdValue(), e.getMessage()));
    			}
    		}

    		try {
    			JSONObject json;
    			try {
    				//Convert json to XML
    				json = new JSONObject(custIntStg.getBusinessObjectDataArea());

    				String xml = XML.toString(json).trim();
    			
    				//Create Inbound Message
    				createCustIntInMessage(custIntStg, xml);

    			} catch (JSONException e) {
    				errorEncountered = Bool.TRUE;
    				//Start Change CB-71
    				//setCustIntStgStatus(custIntStg, CmStagingStatusFlagLookupLookup.constants.CM_ERROR, e.getMessage().toString());
    				setCustIntStgStatus(custIntStg, CmStagingStatusFlagLookupLookup.constants.CM_ERROR, e.getLocalizedMessage().toString());
    				//End Change CB-71
    			}

    		//Start Change CB-71
    		//} catch (ApplicationException e) {
    		}catch(ApplicationError e){
    		//End Change CB-71
    			errorEncountered = Bool.TRUE;
    			// Error Customer Interface Staging record
        		//Start Change CB-71
    			//setCustIntStgStatus(custIntStg, CmStagingStatusFlagLookupLookup.constants.CM_ERROR, e.getMessage().toString());
    			setCustIntStgStatus(custIntStg, CmStagingStatusFlagLookupLookup.constants.CM_ERROR, e.getServerMessage().getMessageText());
				//End Change CB-71

    			session.rollbackToSavepoint(SAVE_POINT);         
    		}
    		if (notNull(custIntStg)) {
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

    	/**
    	 * Create Inbound Message 
    	 * @param custIntStg
    	 * @param xml
    	 */
    	private void createCustIntInMessage(CmCustIntStg custIntStg, String xml){
    		Document doc;
    		Node node;
    		try{
    			doc = DocumentHelper.parseText(xml);
    			
    			//Create New Inbound Message	
    			BusinessObjectInstance boInstance = BusinessObjectInstance.create(BO);
    			BusinessObject_Id custIntInMsgId = new BusinessObject_Id(BO);
    			boInstance.set("bo", BO);
    			boInstance.set("boStatus", custIntInMsgId.getEntity().getInitialStatus().getStatusString());
    			node = doc.getRootElement();
    			Element message = boInstance.getDocument().getRootElement().element("message");
    			boInstance.getDocument().getRootElement().remove(message);
    			boInstance.getDocument().getRootElement().add(node);
    			BusinessObjectDispatcher.add(boInstance);
    			
    			setCustIntStgStatus(custIntStg, CmStagingStatusFlagLookupLookup.constants.CM_PROCESSED, BLANK_STRING);
        	//Start Change CB-71
    		//}catch(Exception e){
    		} catch (DocumentException e) {
			//End Change CB-71
    			errorEncountered = Bool.TRUE;
    			setCustIntStgStatus(custIntStg, CmStagingStatusFlagLookupLookup.constants.CM_ERROR, e.getLocalizedMessage());

    		}catch(ApplicationError e){
    			errorEncountered = Bool.TRUE;
    			
    			// Error Customer Interface Staging record
            	//Start Change CB-71
    			//setCustIntStgStatus(custIntStg, CmStagingStatusFlagLookupLookup.constants.CM_ERROR, e.getMessage().toString());
    			setCustIntStgStatus(custIntStg, CmStagingStatusFlagLookupLookup.constants.CM_ERROR, e.getServerMessage().getMessageText());
    			//End Change CB-71
    		}

    	}
   
    	/**
    	 * Set Status
    	 * @param custIntStg
    	 * @param statusFlg
    	 * @param message
    	 */
    	private void setCustIntStgStatus(CmCustIntStg custIntStg, CmStagingStatusFlagLookupLookup statusFlg, String message) {
    		CmCustIntStg_DTO custIntStgDto = custIntStg.getDTO();
    		custIntStgDto.setCmStatus(statusFlg.trimmedValue());
    		custIntStgDto.setMessageText(isBlankOrNull(message) ? BLANK_STRING: message);
    		custIntStgDto.setUpdateDateTime(getProcessDateTime());
    		custIntStg.setDTO(custIntStgDto);
    		
    	}
    	
    }

}
