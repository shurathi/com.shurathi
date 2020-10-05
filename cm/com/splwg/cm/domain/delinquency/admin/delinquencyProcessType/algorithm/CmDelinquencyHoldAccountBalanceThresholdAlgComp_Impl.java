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
 * This algorithm will hold the delinquency process if the Account balance 
 * is above a certain threshold defined in the soft parameter
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:                  Reason:
 * 2020-08-21   KGhuge        CB-277. Initial Version.
 *
*/
package com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.algorithm;

import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.COTSInstanceList;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.lookup.LogEntryTypeLookup;
import com.splwg.base.api.maintenanceObject.MaintenanceObjectLogHelper;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.domain.common.characteristicType.CharacteristicValue_Id;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmHoldDelinquencyProcessCriteriaAlgorithmSpot;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessLog;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.cm.domain.delinquency.common.businessComponent.CmComputeAccountOverdueBalance;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;
/**
 * @author KGhuge
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (name = thresholdAmount, required = true, type = decimal)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = holdReasonCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = holdReasonCharValue, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = includeUnappliedPaymentsInThreshold, type = boolean)
 *            , @AlgorithmSoftParameter (entityName = featureConfiguration, name = unappliedContractTypeFeatureConfiguration, required = true, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = customerManagementOption, name = contractTypesOptionType, required = true, type = lookup)})
 */
public class CmDelinquencyHoldAccountBalanceThresholdAlgComp_Impl extends
		CmDelinquencyHoldAccountBalanceThresholdAlgComp_Gen implements
		CmHoldDelinquencyProcessCriteriaAlgorithmSpot {

	//Hard Parameter
	private CmDelinquencyProcess_Id delinquencyProcessId = null;
	private Bool isHoldProcessSwitch = Bool.FALSE;
	
	//Constants
	private static final String MAINNTENANCE_OBJ_ELE = "maintenanceObject";
	private static final String ACCOUNT = "ACCOUNT";
	private static final String PRIMARYKEY_VAL1 = "primaryKeyValue1";
	private static final String DELINQUENCY_PROCESS_RELATED_OBJECT = "cmDelinquencyProcessRelatedObject";
	private static final String DELINQUENCY_PROCESS_ID_ELE = "delinquencyProcessId";
	
	@Override
	public void invoke() {
		BusinessObjectInstance boInstance = BusinessObjectInstance.create("CM-DelinquencyProcessSeederBO");
		boInstance.set(DELINQUENCY_PROCESS_ID_ELE,this.delinquencyProcessId.getTrimmedValue());
		boInstance = BusinessObjectDispatcher.read(boInstance, true);
		CmComputeAccountOverdueBalance businessCompInstance = CmComputeAccountOverdueBalance.Factory.newInstance();
		businessCompInstance.setAccountId(getAccountId(boInstance));
		
		if(isNull(this.getIncludeUnappliedPaymentsInThreshold())){
			businessCompInstance.setIncludeUnapplied(false);			
		}else{
			businessCompInstance.setIncludeUnapplied(this.getIncludeUnappliedPaymentsInThreshold().isTrue()?true:false);
		}
		
		businessCompInstance.setContractTypesFeatureConfiguration(this.getUnappliedContractTypeFeatureConfiguration().getId().getIdValue());
		
		businessCompInstance.setContractTypesOptionType(this.getContractTypesOptionType().value());
		
		Money overDueBalance = businessCompInstance.getOverdueBalance();
		
		int thresholdAmount = this.getThresholdAmount().intValue();
		int overDueAmount = overDueBalance.getAmount().intValue();
		
		if(thresholdAmount <= overDueAmount){
			if(!isExists()){
				MaintenanceObjectLogHelper<CmDelinquencyProcess, CmDelinquencyProcessLog> delProcLogHelper = new MaintenanceObjectLogHelper<>
				(this.delinquencyProcessId.getEntity().getBusinessObject().getMaintenanceObject(),this.delinquencyProcessId.getEntity());
				CharacteristicValue_Id charValId = new CharacteristicValue_Id(this.getHoldReasonCharType().getId(), this.getHoldReasonCharValue().trim());
				delProcLogHelper.addLogEntry(LogEntryTypeLookup.constants.SYSTEM, MessageRepository.overDueAccountBalance(getDescription()), null,charValId.getEntity());
			}
			this.isHoldProcessSwitch = Bool.TRUE;
			}
		}

	private String getDescription() {
		String descr = null;
		PreparedStatement getDescriptionStatement = null;
		StringBuilder getDescriptionQuery = new StringBuilder(" SELECT DESCR FROM CI_CHAR_VAL_L WHERE CHAR_TYPE_CD=:charTypeCd AND CHAR_VAL=:charValue ");
		getDescriptionStatement = createPreparedStatement(getDescriptionQuery.toString(), "");
		getDescriptionStatement.bindId("charTypeCd", this.getHoldReasonCharType().getId());
		getDescriptionStatement.bindString("charValue", this.getHoldReasonCharValue(),"CHAR_VAL");
		descr = getDescriptionStatement.firstRow().getString("DESCR");
		getDescriptionStatement.close();
		return descr;
	}

	@Override
	public void setDelinquencyProcessId(CmDelinquencyProcess_Id delinquencyProcessId) {
		this.delinquencyProcessId = delinquencyProcessId;
	}

	@Override
	public Bool getIsHoldProcessSwitch() {
		return this.isHoldProcessSwitch;
	}
	
	/* This Method will returns an Account Id
	 * @param  boInstance
	 * @return accountId
	 * */	
	public String getAccountId(BusinessObjectInstance boInstance){
		String accountId = null;
		if(!isNull(boInstance)){
			COTSInstanceList cmDelinquencyProcessRelatedObjectList = boInstance.getList(DELINQUENCY_PROCESS_RELATED_OBJECT);
			if(!isNull(cmDelinquencyProcessRelatedObjectList) && !cmDelinquencyProcessRelatedObjectList.isEmpty()){
				for(COTSInstanceNode node : cmDelinquencyProcessRelatedObjectList){
					if(node.getString(MAINNTENANCE_OBJ_ELE).equalsIgnoreCase(ACCOUNT)){
						accountId = node.getString(PRIMARYKEY_VAL1).trim();
						break;
					}
				}
			}
		}
		return accountId.trim();
	}
	
	public boolean isExists(){
		boolean exists = false;
		StringBuilder checkExistsQuery = new StringBuilder();
		PreparedStatement checkExistsStatement = null;
		checkExistsQuery.append(" SELECT 'X' FROM CM_DELIN_PROC_LOG WHERE CM_DELIN_PROC_ID=:delinquencyProcessId ");
		checkExistsQuery.append(" AND BO_STATUS_CD=:boStatus AND CHAR_TYPE_CD=:charTypeCd AND CHAR_VAL=:charValue ");
		checkExistsStatement = createPreparedStatement(checkExistsQuery.toString(),"Check Existance of record");
		checkExistsStatement.bindId(DELINQUENCY_PROCESS_ID_ELE,this.delinquencyProcessId);
		checkExistsStatement.bindId("charTypeCd", this.getHoldReasonCharType().getId());
		checkExistsStatement.bindString("boStatus", this.delinquencyProcessId.getEntity().getStatus(),"BO_STATUS_CD");
		checkExistsStatement.bindString("charValue",this.getHoldReasonCharValue() ,"CHAR_VAL");
		exists = isNull(checkExistsStatement.firstRow())?false:true;
		checkExistsStatement.close();
		return exists;
	}
}
