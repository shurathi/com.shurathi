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
 * Delinquency Account Monitor Rule for CondoSafe Non-Strategic
 * 
 * This algorithm determines if account's debt is overdue based on the age 
 * and amount thresholds (including $ Amount and % of Unpaid amount 
 * with respect to original amount).If so, a new delinquency process is created.
 * If delinquency process already exists, it adds overdue bills to it.
 * 
 **************************************************************************
 * 
 * CHANGE HISTORY:
 *
 * Date:        by:        Reason:
 * YYYY-MM-DD   IN         Reason text. 
 * 2020-08-19   SPatil     CB-280. Initial	
 * 
 **************************************************************************
 */
package com.splwg.cm.domain.delinquency.admin.delinquencyCustomerMonitor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;


import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.COTSInstanceList;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.Query;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.lookup.BusinessObjectActionLookup;
import com.splwg.base.api.lookup.LogEntryTypeLookup;
import com.splwg.base.api.lookup.YesNoOptionLookup;
import com.splwg.base.api.maintenanceObject.MaintenanceObjectLogHelper;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.ccb.api.lookup.FinancialTransactionTypeLookup;
import com.splwg.ccb.domain.billing.bill.Bill_Id;
import com.splwg.ccb.domain.billing.bill.OpenItemBillAmountResults;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.cm.api.lookup.CmDelinquencyRelatedObjTypeLookup;
import com.splwg.cm.domain.delinquency.admin.collectionClassOverdueRules.CmDetermineOpenBillItemAmounts_Impl;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmDelinquencyProcessType;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmDelinquencyProcessType_Id;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessLog;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessRelatedObject;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessRelatedObject_DTO;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.maintenanceObject.MaintenanceObject_Id;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;

/**
 * @author SPatil
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (name = determinesAgeDate, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = processBillsWithAge, required = true, type = string)
 *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = unpaidAmountAndPercentage, required = true, type = lookup)
 *            , @AlgorithmSoftParameter (name = processUnpaidAmountBills, type = string)
 *            , @AlgorithmSoftParameter (name = processUnpaidPercentageBills, type = string)
 *            , @AlgorithmSoftParameter (entityName = cmDelinquencyProcessType, name = delinquencyProcessType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = includeUnappliedPayments, type = lookup)
 *            , @AlgorithmSoftParameter (entityName = featureConfiguration, name = unappliedContractTypeFeatureConfig, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = customerManagementOption, name = unappliedContractTypeOptionType, type = lookup)
 *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = userCurrentRevenuePeriodBilled, type = lookup)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = billIdCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = businessObject, name = delinquencyProcessSeederBO, required = true, type = entity)})
 */
public class CmDelinquencyAccountMonitorRuleAlgComp_Impl extends
		CmDelinquencyAccountMonitorRuleAlgComp_Gen implements
		CmDelinquencyControlMonitorAlgorithmSpot {

	Logger logger = LoggerFactory.getLogger(CmDelinquencyAccountMonitorRuleAlgComp_Impl.class);
	private Person_Id personId = null;
	private Account_Id accountId = null;
	private Bool isProcessingCompleteSwitch;
		
	@Override
	protected void extraSoftParameterValidations(boolean forAlgorithmValidation) 
	{
		// Determine Age Date Value is B or D
		if (notBlank(getDeterminesAgeDate()) && !(getDeterminesAgeDate().equals(CmDelinquencyCustomerMonitorRuleConstants.CONST_B)
				|| getDeterminesAgeDate().equals(CmDelinquencyCustomerMonitorRuleConstants.CONST_D))) 
		    {
			  addError(MessageRepository.invalidAlgorithmParmValues(CmDelinquencyCustomerMonitorRuleConstants.DATE_DETERMINE_AGE_DESC, CmDelinquencyCustomerMonitorRuleConstants.DATE_DETERMINE_AGE_DESC_VAL));
		    }

		// Validate include on account payments is Y or N
		if (notNull(getIncludeUnappliedPayments()) && !(getIncludeUnappliedPayments().trimmedValue().equals(YesNoOptionLookup.constants.YES.trimmedValue())
				|| getIncludeUnappliedPayments().trimmedValue().equals(YesNoOptionLookup.constants.NO.trimmedValue()))) 
		   {
			  addError(MessageRepository.invalidUnpaidAmtandPer(getAlgorithm().getAlgorithmType().getParameterAt(6).fetchLanguageParameterLabel(),getIncludeUnappliedPayments().trimmedValue()));
		   }

		// if on account payments is Y validates feature configuration and option type 
		if (notNull(getIncludeUnappliedPayments()) && getIncludeUnappliedPayments().trimmedValue().equals(YesNoOptionLookup.constants.YES.trimmedValue())) 
		  {
			if (isNull(getUnappliedContractTypeFeatureConfig()))
				reportRequiredParameter("unappliedContractTypeFeatureConfig", 7);
			if (isNull(getUnappliedContractTypeOptionType()))
				reportRequiredParameter("unappliedContractTypeOptionType", 8);
		  }

		// Validate Use current Revenue period is Y or N
		if (notNull(getUserCurrentRevenuePeriodBilled()) && !(getUserCurrentRevenuePeriodBilled().trimmedValue().equals(YesNoOptionLookup.constants.YES.trimmedValue()))
				|| (getUserCurrentRevenuePeriodBilled().trimmedValue().equals(YesNoOptionLookup.constants.NO.trimmedValue()))) {

			addError(MessageRepository.invalidUnpaidAmtandPer(getAlgorithm().getAlgorithmType().getParameterAt(9).fetchLanguageParameterLabel(),getUserCurrentRevenuePeriodBilled().trimmedValue()));
		}
	}
	@Override
	public void invoke() 
	{
	  evaluateAccountForDeliquency(accountId);
      isProcessingCompleteSwitch = Bool.TRUE;
	}

	private void evaluateAccountForDeliquency(Account_Id accountId) 
	{
		CmDelinquencyProcess_Id existingActiveDelinquencyProcessId = getExistingActiveDelinquency(accountId,getDelinquencyProcessType()); 
		
		Date latestDueDate = null;
		BigDecimal totalCustomerOriginalAmount = BigDecimal.ZERO;
		BigDecimal totalCustomerUnpaidAmount = BigDecimal.ZERO;
		BigDecimal hundread = new BigDecimal(100.00);
		BigDecimal unpaidPercentage = BigDecimal.ZERO;
		boolean addDelinquency = false;
		
		// Get list of completed bills not fully paid that are not linked to an active delinquency	 
		List<SQLResultRow> billList = getCompletedBillsNotLinkToActiveDelinquencyProcess(accountId);
		
		 // Define list to hold Delinquency bills of the account
		List<String> delinquencyProcessBills = new ArrayList<String>();

		if (!isNull(billList) && billList.size() > 0) 
		{	  
			  for(SQLResultRow bill : billList )
			  {
				String billIdStr = bill.getString("BILL_ID");
				Date billDueDt = bill.getDate("DUE_DT");
				Date billDt = bill.getDate("BILL_DT");
				Bill_Id billId = new Bill_Id(billIdStr);				
				BigInteger ageOfBill = determineAgeOfBill(billDueDt,billDt);
				CmDetermineOpenBillItemAmounts_Impl cm = new CmDetermineOpenBillItemAmounts_Impl();
				OpenItemBillAmountResults output = cm.getBillAmounts(billId.getEntity(),null);
			    BigDecimal billUnpaidAmount = output.getUnpaidBillAmount().getAmount();
			    BigDecimal billOriginalAmount  = output.getOriginalBillAmount().getAmount();
			    
			    if(!isNull(existingActiveDelinquencyProcessId))
			    {
				   if(ageOfBill.compareTo(new BigInteger(getProcessBillsWithAge()))>=0)
					   {
						  addBilltoDelinquencyProcess(billIdStr, existingActiveDelinquencyProcessId);
					   }
				   else
				   {
					   if(billUnpaidAmount.compareTo(BigDecimal.ZERO) < 0) 
					       {
						     addBilltoDelinquencyProcess(billIdStr, existingActiveDelinquencyProcessId);
					       }
				   }
			    }
			    else
			    {
			    	if(ageOfBill.compareTo(new BigInteger(getProcessBillsWithAge()))>=0)
			    	    {
			    		  delinquencyProcessBills.add(billId.getTrimmedValue());
			    		  totalCustomerOriginalAmount = totalCustomerOriginalAmount.add(billOriginalAmount);
						  totalCustomerUnpaidAmount = totalCustomerUnpaidAmount.add(billUnpaidAmount);
						  if (isNull(latestDueDate) || latestDueDate.isBefore(billDueDt)) 
						     {
							   latestDueDate = billDueDt;
						     }
			    	    }
			    	else
			    	{
			    		if(billUnpaidAmount.compareTo(BigDecimal.ZERO) < 0) 
			    		{
			    			delinquencyProcessBills.add(billId.getTrimmedValue());
			    			totalCustomerOriginalAmount = totalCustomerOriginalAmount.add(billOriginalAmount); 
			    			totalCustomerUnpaidAmount = totalCustomerUnpaidAmount.add(billUnpaidAmount);
			    		}
			    	}
			    } 
			 }
		 }
	  
	  if(!delinquencyProcessBills.isEmpty())
	  {
		  if (notNull(getIncludeUnappliedPayments()) && (getIncludeUnappliedPayments().trimmedValue().equals(YesNoOptionLookup.constants.YES.trimmedValue())))
		  {
			 Money unappliedPayment = retrieveUnappliedPayments(accountId);
			 totalCustomerUnpaidAmount = totalCustomerUnpaidAmount.add(unappliedPayment.getAmount()) ; 
		  }
		  if (notNull(getUserCurrentRevenuePeriodBilled()) && (getUserCurrentRevenuePeriodBilled().trimmedValue().equals(YesNoOptionLookup.constants.YES.trimmedValue())) && notNull(latestDueDate) ) 
		  {
			Money totalAmountOfLatestBill = retrieveTotalAmountBill(accountId,latestDueDate);
			totalCustomerOriginalAmount = totalAmountOfLatestBill.getAmount(); 
		  }	  
		  if(totalCustomerOriginalAmount.compareTo(BigDecimal.ZERO)>0)
		  {
			  unpaidPercentage =(totalCustomerUnpaidAmount.divide(totalCustomerOriginalAmount).multiply(hundread));
		  }
		  if(getUnpaidAmountAndPercentage().trimmedValue().equals(YesNoOptionLookup.constants.YES.trimmedValue()))
		  {
			  logger.info(getProcessUnpaidPercentageBills());
			  logger.info(getProcessUnpaidAmountBills());
			  logger.info("new BigDecimal(getProcessUnpaidPercentageBills()) "+new BigDecimal(getProcessUnpaidPercentageBills()));
			  logger.info("new BigDecimal(getProcessUnpaidAmountBills()) "+new BigDecimal(getProcessUnpaidAmountBills()));
			  if(unpaidPercentage.compareTo(new BigDecimal(getProcessUnpaidPercentageBills()))>=0 && totalCustomerUnpaidAmount.compareTo(new BigDecimal(getProcessUnpaidAmountBills()))>0 )
			  {
				   addDelinquency = true;
			  }
		  }
		  else
		  {
				if(notNull(getProcessUnpaidPercentageBills()) && unpaidPercentage.compareTo(new BigDecimal(getProcessUnpaidPercentageBills())) >= 0)
				{	
					addDelinquency = true;
				}
				if(notNull(getProcessUnpaidAmountBills()) && totalCustomerUnpaidAmount.compareTo(new BigDecimal(getProcessUnpaidAmountBills()))>=0)
				{
				    addDelinquency = true;
				}
			 
		  }
		  if(addDelinquency=true)
		  {
			  createDelinquencyProcess(getDelinquencyProcessType(), delinquencyProcessBills);
		  }
		  
	  }
	}

	private CmDelinquencyProcess_Id getExistingActiveDelinquency(Account_Id accountId2,
			CmDelinquencyProcessType delinquencyProcessType) 
	 {
		CmDelinquencyProcess_Id existingDelinquencyProcessID=delinquencyProcessExists(delinquencyProcessType);
		return existingDelinquencyProcessID;
	 }
	
	private Money retrieveUnappliedPayments(Account_Id accountId) 
	{
		StringBuilder sb = new StringBuilder();
		sb.append(" FROM FinancialTransaction FT, ServiceAgreement SA WHERE SA.account = :account AND FT.serviceAgreement = SA.id ");
		sb.append(" AND FT.isFrozen = :frozen AND SA.serviceAgreementType.id.saType IN (SELECT RPAD(WO.value,8) FROM FeatureConfigurationOption WO WHERE WO.id.workforceManagementSystem = :unAppliedContrFeatureConfig AND WO.id.optionType = :unAppliedContrOptionType ) ");
	    Query<Money> query =createQuery(sb.toString(), "RetrieveUnappliedPayments");
		query.bindId("account", accountId);
		query.bindLookup("frozen", YesNoOptionLookup.constants.YES);
		query.bindEntity("unAppliedContrFeatureConfig", getUnappliedContractTypeFeatureConfig());
		query.bindLookup("unAppliedContrOptionType", getUnappliedContractTypeOptionType());
		query.addResult("amount", "SUM(FT.currentAmount)");
		return query.firstRow();
	}
	
	private Money retrieveTotalAmountBill(Account_Id accountId,Date latestDueDate) 
	{
		StringBuilder sb = new StringBuilder();
		sb.append(" FROM FinancialTransaction FT, Bill BL, FinancialTransactionExtension CMFT ");
		sb.append(" WHERE BL.account = :accountId ");
		sb.append(" AND BL.billStatus = :complete AND BL.dueDate = :latestDueDate AND FT.billId = BL.id AND FT.isFrozen = :frozen");
		sb.append(" AND FT.shouldShowOnBill = :show AND FT.financialTransactionType IN (:adj, :adjX, :bseg, bsegX) AND CMFT.id = FT.id AND CMFT.startDate <= BL.dueDate) ");
	    Query<Money> query =createQuery(sb.toString(), "RetrieveTotalAmountBill");
		query.bindId("account", accountId);
		query.bindDate("latestDueDate", latestDueDate);
		query.bindLookup("frozen", YesNoOptionLookup.constants.YES);
		query.bindLookup("show", YesNoOptionLookup.constants.YES);
		query.bindLookup("adj", FinancialTransactionTypeLookup.constants.ADJUSTMENT);
		query.bindLookup("adjX", FinancialTransactionTypeLookup.constants.ADJUSTMENT_CANCELLATION);
		query.bindLookup("bseg", FinancialTransactionTypeLookup.constants.BILL_SEGMENT);
		query.bindLookup("bsegX", FinancialTransactionTypeLookup.constants.BILL_SEGMENT);
		query.addResult("amount", "SUM(FT.currentAmount)");
		return query.firstRow();
	}
	
	/**
	 * Get Completed Bills Not Linked to Active Delinquency Process That is Not Fully Balanced
	 * @return - List<SQLResultRow> - Bill list 
	 */
	List<SQLResultRow> getCompletedBillsNotLinkToActiveDelinquencyProcess(Account_Id accountId) 
	{
		PreparedStatement pst = null;
		List<SQLResultRow> billList = null;
		try {
				pst = createPreparedStatement(
						CmDelinquencyCustomerMonitorRuleConstants.GET_COMPLT_BILL_ACCT_SQL
								.toString(),
						"CmDelinquencyAccountMonitorRuleAlgComp_Impl");
				pst.bindId("accountId", accountId);
			billList = pst.list();
		} finally {
			if (!isNull(pst)) {
				pst.close();
				pst = null;
			}
		}
		return billList;
	}
	
	/**
	 * Determine Age of Bill
	 * @param dueDate -  Due Date of Bill 
	 * @param billDate - Bill Date of Bill
	 * return billAge
	 */
	private BigInteger determineAgeOfBill(Date dueDate, Date billDate) 
	{
		BigInteger billAge = null;
		if (getDeterminesAgeDate().trim().compareTo(CmDelinquencyCustomerMonitorRuleConstants.CONST_D) == 0) 
		{
			billAge = new BigInteger(String.valueOf(getProcessDateTime().getDate().difference(dueDate).getTotalDays()));
		} 
		else 
		{
			billAge = new BigInteger(String.valueOf(getProcessDateTime().getDate().difference(billDate).getTotalDays()));
		}
		return billAge;
	}
	
	/**
	 * Check if delinquency process exists for current customer and current Delinquency Process Type.
	 * 
	 */
	private CmDelinquencyProcess_Id delinquencyProcessExists(CmDelinquencyProcessType delinquencyProcessType) 
	{
		PreparedStatement pst = null;
		CmDelinquencyProcess_Id delinquencyProcessId = null;
		try {
             	pst = createPreparedStatement(
						CmDelinquencyCustomerMonitorRuleConstants.EXISTS_DELINQ_PROC_ACCT_LVL.toString(),
						"CmDelinquencyAccountMonitorRuleAlgComp_Impl");
				pst.bindId("accountId", accountId);
				pst.bindEntity("delinProcTypeCd", delinquencyProcessType);
			
			List<SQLResultRow> result = pst.list();
			pst.close();
			if (!isNull(result) && result.size() > 0) {

				SQLResultRow output = result.get(0);
				delinquencyProcessId = new CmDelinquencyProcess_Id(output.getString("CM_DELIN_PROC_ID"));
			}
		} finally {
			if (!isNull(pst)) {
				pst.close();
				pst = null;
			}
		}
		return delinquencyProcessId;
	}
	
	/**
	 * Add Bill to Delinquency Process
	 * @param billId -  Bill Id
	 */
	private void addBilltoDelinquencyProcess(String billId, CmDelinquencyProcess_Id delinqProcessId) 
	{
		CmDelinquencyProcessRelatedObject_DTO relObjDTO = createDTO(CmDelinquencyProcessRelatedObject.class);
		MaintenanceObject_Id maintenanceObjectId = new MaintenanceObject_Id(CmDelinquencyCustomerMonitorRuleConstants.MAINTENANCE_OBJ_BILL);
		delinqProcessId.getEntity().getRelatedObjects().add(relObjDTO, maintenanceObjectId, CmDelinquencyRelatedObjTypeLookup.constants.CM_COLLECTING_ON, billId);
		logger.info("Bill ==" + billId + " added to Existing Delinquency Process==" + delinqProcessId.getIdValue());
		MaintenanceObjectLogHelper<CmDelinquencyProcess, CmDelinquencyProcessLog> logHelper = new MaintenanceObjectLogHelper<
				CmDelinquencyProcess, CmDelinquencyProcessLog>(
						delinqProcessId.getEntity().getBusinessObject().getMaintenanceObject(), delinqProcessId.getEntity());
		logHelper.addLogEntry(LogEntryTypeLookup.constants.SYSTEM,
				MessageRepository.billIdMessageDel(billId), null, getBillIdCharacteristicType(), new
				Bill_Id(billId).getEntity());
		logger.info("Bill ==" + billId +
				" added to Delinquency Process Log for Delinquency Process ==" +
				delinqProcessId.getIdValue());
	}
	
	/**
	 * Create Delinquency Process
	 * @param bandProcessType -  Delinquency Process Type Code
	 * @param delinquentBills - List of delinquent bill
	 */
	private void createDelinquencyProcess(CmDelinquencyProcessType bandDelProcessType, List<String> billIdList) 
	{
		CmDelinquencyProcess_Id delinquencyProcessId = null;
		CmDelinquencyProcess cmDelinquencyProcess = null;
		CmDelinquencyProcessType_Id delProcTypeId = bandDelProcessType.getId();
		BusinessObjectInstance delProcBOInstance = BusinessObjectInstance.create(getDelinquencyProcessSeederBO());
		delProcBOInstance.set("cmDelinquencyProcessType", delProcTypeId.getEntity());
		delProcBOInstance.set("businessObject", getDelinquencyProcessSeederBO());
		delProcBOInstance.set("status", delProcTypeId.getEntity().getRelatedTransactionBO().getInitialStatus().getStatusString());
		delProcBOInstance.set("creationDateTime", getProcessDateTime());
		delProcBOInstance.set("statusDateTime", getProcessDateTime());	
		COTSInstanceList cotsInstanceList = delProcBOInstance.getList(CmDelinquencyCustomerMonitorRuleConstants.DEL_REL_OBJ_LIST_NODE);
		{
			COTSInstanceListNode cotsInstanceListNode = cotsInstanceList.newChild();
			cotsInstanceListNode.set("maintenanceObject", CmDelinquencyCustomerMonitorRuleConstants.MAINTENANCE_OBJ_ACCOUNT);
			cotsInstanceListNode.set("cmDelinquencyRelatedObjTypeFlg", CmDelinquencyCustomerMonitorRuleConstants.DEL_PROC_RELATED_OBJECT_TYPE_FLAG);
			cotsInstanceListNode.set("primaryKeyValue1", accountId.getIdValue());		
		}
		Iterator<String> bill = billIdList.iterator();
		while (bill.hasNext()) 
		{
			COTSInstanceListNode cotsInstanceListNodeBill = cotsInstanceList.newChild();
			cotsInstanceListNodeBill.set("maintenanceObject", CmDelinquencyCustomerMonitorRuleConstants.MAINTENANCE_OBJ_BILL);
			cotsInstanceListNodeBill.set("cmDelinquencyRelatedObjTypeFlg", CmDelinquencyCustomerMonitorRuleConstants.RELATED_OBJECT_TYPE_FLAG);
			cotsInstanceListNodeBill.set("primaryKeyValue1", bill.next());		
		}
			
		delProcBOInstance = BusinessObjectDispatcher.execute(delProcBOInstance, BusinessObjectActionLookup.constants.FAST_ADD);
		
		String delProcId = delProcBOInstance.getElement().selectSingleNode(CmDelinquencyCustomerMonitorRuleConstants.DEL_PROC_ID).getText();
		if (notNull(delProcId)) 
		{
			delinquencyProcessId = new CmDelinquencyProcess_Id(delProcId);
			cmDelinquencyProcess = delinquencyProcessId.getEntity();
		}

		Iterator<String> delinquentBill = billIdList.iterator();
		while (delinquentBill.hasNext()) 
		{
			String billId = delinquentBill.next();
			MaintenanceObjectLogHelper<CmDelinquencyProcess, CmDelinquencyProcessLog> logHelper = new MaintenanceObjectLogHelper<
					CmDelinquencyProcess, CmDelinquencyProcessLog>(
							cmDelinquencyProcess.getBusinessObject().getMaintenanceObject(), cmDelinquencyProcess);
			logHelper.addLogEntry(LogEntryTypeLookup.constants.SYSTEM,
					MessageRepository.billIdMessageDel(billId), null, getBillIdCharacteristicType(), new
					Bill_Id(billId).getEntity());
		}
	}

	@Override
	public void setPersonId(Person_Id personId) {
		this.personId = personId;
	}

	@Override
	public Bool getProcessingCompletedSwitch() {
		return isProcessingCompleteSwitch;
	}

	@Override
	public void setAccountId(Account_Id accountId) {
		this.accountId = accountId;
		
	}

}
