/* 
 **************************************************************************
 *           	     Confidentiality Information:
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
 * Monitor Delinquent Account Balance above Threshold
 * 
 * This will monitor if the Delinquent Account balance is above or equal to a 
 * set Threshold Amount. If satisfied, then it will transition to the BO status 
 * with Transition Condition defined in soft parameter.
 * 
 **************************************************************************
 * 
 * CHANGE HISTORY:
 *
 * Date:        by:        Reason:
 * YYYY-MM-DD   IN         Reason text. 
 * 2020-08-20   SPatil     CB-275. Initial	
 * 
 **************************************************************************
 */
package com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm;

import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.BusinessObjectStatusCode;
import com.splwg.base.api.datatypes.LookupHelper;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.lookup.BusinessObjectActionLookup;
import com.splwg.base.api.lookup.BusinessObjectStatusTransitionConditionLookup;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatusAutoTransitionAlgorithmSpot;
import com.splwg.base.domain.common.businessObjectStatusReason.BusinessObjectStatusReason_Id;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.cm.domain.delinquency.common.businessComponent.CmComputeAccountOverdueBalance;

/**
 * @author SPatil
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (name = thresholdAmount, required = true, type = decimal)
 *            , @AlgorithmSoftParameter (name = transitionCondition, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = includeUnappliedPayment, type = boolean)
 *            , @AlgorithmSoftParameter (entityName = featureConfiguration, name = unappliedContractTypeFeatureConfig, required = true, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = customerManagementOption, name = unappliedContractTypeOptionType, required = true, type = lookup)})
 */
public class CmMonitorDelinquentAccountBalanceAlgComp_Impl extends
CmMonitorDelinquentAccountBalanceAlgComp_Gen implements
		BusinessObjectStatusAutoTransitionAlgorithmSpot  {
	
	BusinessObjectInstanceKey boInstanceKey=null;
	private BusinessObjectInstance boInstance;
	private BusinessObjectStatusTransitionConditionLookup nextTransitionCondition;
	
	
	
	@Override
	public void invoke() 
	{
		nextTransitionCondition = null;
		boInstance = BusinessObjectDispatcher.read(boInstanceKey, true);
		String acctId = boInstance.getFieldAndMDForPath("accountId").getXMLValue();
		Account_Id accountId = new Account_Id(acctId);
		CmComputeAccountOverdueBalance businessCompInstance = CmComputeAccountOverdueBalance.Factory.newInstance();
		businessCompInstance.setAccountId(accountId.getTrimmedValue());
		
		if(isNull(this.getIncludeUnappliedPayment()))
		  {
			businessCompInstance.setIncludeUnapplied(false);			
		  }
		else
		  {
		    businessCompInstance.setIncludeUnapplied(this.getIncludeUnappliedPayment().isTrue()?true:false);
		  }

        businessCompInstance.setContractTypesFeatureConfiguration(this.getUnappliedContractTypeFeatureConfig().getId().getIdValue());
		businessCompInstance.setContractTypesOptionType(this.getUnappliedContractTypeOptionType().getLookupValue().getId().getFieldValue());
				
		Money overDueBalance = businessCompInstance.getOverdueBalance();
		int thresholdAmount = this.getThresholdAmount().intValue();
		int overDueAmount = overDueBalance.getAmount().intValue();
		
		if(thresholdAmount<= overDueAmount)
		{
			nextTransitionCondition = LookupHelper.getLookupInstance(BusinessObjectStatusTransitionConditionLookup.class, getTransitionCondition());
		}
	}

	@Override
	public boolean getForcePostProcessing() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public BusinessObjectStatusCode getNextStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BusinessObjectStatusTransitionConditionLookup getNextStatusCondition() {
		return nextTransitionCondition;
	}

	@Override
	public boolean getSkipAutoTransitioning() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public BusinessObjectStatusReason_Id getStatusChangeReasonId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getUseDefaultNextStatus() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setAction(BusinessObjectActionLookup arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBusinessObject(BusinessObject arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBusinessObjectKey(BusinessObjectInstanceKey boInstanceKey) {
		this.boInstanceKey= boInstanceKey;
		
	}

	
	
}
