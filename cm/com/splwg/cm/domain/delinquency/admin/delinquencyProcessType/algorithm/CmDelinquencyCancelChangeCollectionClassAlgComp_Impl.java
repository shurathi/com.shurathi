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
 * This class is Delinquency - Cancel Criteria Algorithm - Change in Collection Class Algorithm
 * 
 **************************************************************************
 * 
 * CHANGE HISTORY:
 *
 * Date:        by:        Reason:
 * YYYY-MM-DD   IN         Reason text. 
 * 2020-08-21   AMusal     CB-279 Initial Version 1
 **************************************************************************
 */
package com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.algorithm;
import com.splwg.base.api.Query;
import com.splwg.base.api.QueryResultRow;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.domain.common.businessObjectStatusReason.BusinessObjectStatusReason_Id;
import com.splwg.base.domain.common.maintenanceObject.MaintenanceObject;
import com.splwg.ccb.domain.admin.collectionClass.CollectionClass_Id;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.cm.api.lookup.CmDelinquencyRelatedObjTypeLookup;
import com.splwg.cm.domain.delinquency.admin.delinquencyCustomerMonitor.CmDelinquencyProcessCalculateTriggerDateConstants;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmDelinquencyProcessCancelCriteriaAlgorithmSpot;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessCharacteristic;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;

/**
 * @author RIA-IN-L-005
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = characteristicType, name = collectionClassCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = businessObjectStatusReason, name = bOStatusReason, type = entity)})
 */
public class CmDelinquencyCancelChangeCollectionClassAlgComp_Impl extends
		CmDelinquencyCancelChangeCollectionClassAlgComp_Gen implements
		CmDelinquencyProcessCancelCriteriaAlgorithmSpot {

	
	
	private CmDelinquencyProcess_Id delinquencyProcessId;
	private Bool okToCancelSwitch = Bool.FALSE;
	private BusinessObjectStatusReason_Id bOStatusReasonId=null;
	
	
	@Override
	public void invoke() {	
		
		// Fetch Delinquency Process Id
		CmDelinquencyProcess delinquencyProcess = delinquencyProcessId.getEntity();
		
		if(isNull(delinquencyProcess)){
			
			addError(MessageRepository.delinquencyProcessRequired());
		}
		// Fetch  Maintenance Object and Primary Key Value
		QueryResultRow resultRow = fetchLevelAndEntityForDelinquencyProcess();

		MaintenanceObject maintenanceObject = resultRow.getEntity("maintenanceObject", MaintenanceObject.class);
		
		Account_Id accountId = null;
		
		// Fetch Account 
		if (maintenanceObject.getId().getTrimmedValue().equalsIgnoreCase(CmDelinquencyProcessCalculateTriggerDateConstants.MAINTENANCE_OBJ_ACCOUNT))
		{
			accountId = new Account_Id(resultRow.getString("primaryKeyValue1"));
		}
		
		// If Account id is not present, issue an error
		if ((isNull(accountId))) {
			addError(MessageRepository.deliquencyProcessCMDLReqd(delinquencyProcessId));
		}
		
		//call collection class change for customer related to delinquency process 
		if (okToCancelSwitch.isFalse() && notNull(getCollectionClassCharacteristicType())) 
		{
			evaluateCollectionClassChange(accountId);
		}
		
		
	}
	
	/**
	 * This method fetches level and maintenance object for delinquencyProcessId
	 * @return QueryResultRow resultRow
	 */
	private QueryResultRow fetchLevelAndEntityForDelinquencyProcess() 
	{

		Query<QueryResultRow> query = createQuery(CmDelinquencyProcessCalculateTriggerDateConstants.FETCH_DELIQ_LVL_ID.toString(), "CmDelinquencyCancelChangeCollectionClassAlgComp_Impl");
		query.bindId("delinProcId", delinquencyProcessId);
		query.bindLookup("relatedObjTypeFlag", CmDelinquencyRelatedObjTypeLookup.constants.CM_DELINQUENCY_LEVEL);
		query.addResult("maintenanceObject", "dpo.id.maintenanceObject");
		query.addResult("primaryKeyValue1", "dpo.id.primaryKeyValue1");
		QueryResultRow resultRow = query.firstRow();
		return resultRow;

	}
	/**
	 * This method evaluates collection class change for customer related to delinquency process 
	 * @param accountId		
	 */
	private void evaluateCollectionClassChange(Account_Id accountId) {
		CmDelinquencyProcessCharacteristic delinCollectionClass  = delinquencyProcessId.getEntity().getEffectiveCharacteristic(getCollectionClassCharacteristicType());
		if (notNull(delinCollectionClass )) {
			CollectionClass_Id accountCollectionClass = accountId.getEntity().getCollectionClassId();
				
			//If Collection Class is different between Account and Delinquency Process Characteristic, cancel the Delinquency Process Process
			if (notNull(accountCollectionClass)) {
				if (!delinCollectionClass .getCharacteristicValueForeignKey1().trim().equalsIgnoreCase(accountCollectionClass.getTrimmedValue())) {
					okToCancelSwitch = Bool.TRUE;
					// Set BO Status Reason if populated
					if(notNull(getBOStatusReason()))
					{
						bOStatusReasonId=getBOStatusReason().getId();
					}
					
				}
			}

		}

	}


	@Override
	public void setCmDelinquencyProcessId(
			CmDelinquencyProcess_Id delinquencyProcessId) {
			this.delinquencyProcessId = delinquencyProcessId;
	}

	@Override
	public Bool getOkToCancelSwitch() {
		return okToCancelSwitch;
	}

	@Override
	public BusinessObjectStatusReason_Id getBusinessObjectStatusReason() {
	
		
		return bOStatusReasonId;
	}

}
