/*
 ************************************************************************** *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Monitor Delinquent Account Status and Balance
 * 
 * This will check if the Delinquent Account is in a certain status. 
 * The status is stored in the characteristic provided in the soft parameter. 
 * This will also check that if the Account Overdue balance is zero 
 * for the bills that are X days past due. Where X is the 
 * Number of Days Past Due Threshold soft parameter.
 *	If all the conditions are satisfied, it will create a To Do so that users 
 *	will be able to change the Account Status.
 * 
 *                                                             
 **************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 *           
 * 2020-08-20   Ishita Garg		Initial version
 * 2020-08-21   Ishita Garg	    Updated version
 * 2020-09-08   SPatil			CB-359-Updated Version
 * **************************************************************************
 */
package com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm;


import java.math.BigInteger;

import com.splwg.base.api.ListFilter;
import com.splwg.base.api.Query;
import com.splwg.base.api.QueryResultRow;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.BusinessObjectStatusCode;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.lookup.BusinessObjectActionLookup;
import com.splwg.base.api.lookup.BusinessObjectStatusTransitionConditionLookup;
import com.splwg.base.api.lookup.LogEntryTypeLookup;
import com.splwg.base.api.maintenanceObject.MaintenanceObjectLogHelper;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatusAutoTransitionAlgorithmSpot;
import com.splwg.base.domain.common.businessObjectStatusReason.BusinessObjectStatusReason_Id;
import com.splwg.base.domain.todo.role.Role_Id;
import com.splwg.base.domain.todo.toDoEntry.ToDoCreator;
import com.splwg.base.domain.todo.toDoEntry.ToDoDrillKeyValue;
import com.splwg.base.domain.todo.toDoEntry.ToDoEntry;
import com.splwg.base.domain.todo.toDoEntry.ToDoEntry_DTO;
import com.splwg.base.domain.todo.toDoEntry.ToDoEntry_Id;
import com.splwg.base.domain.todo.toDoType.ToDoDrillKeyType;
import com.splwg.base.domain.todo.toDoType.ToDoSortKeyType;
import com.splwg.base.domain.todo.toDoType.ToDoType;
import com.splwg.base.domain.todo.toDoType.ToDoType_Id;
import com.splwg.ccb.domain.customerinfo.account.AccountCharacteristic;
import com.splwg.ccb.domain.customerinfo.account.AccountPerson;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessLog;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.cm.domain.delinquency.common.businessComponent.CmComputeAccountOverdueBalance;

/**
 * @author IshitaGarg
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = characteristicType, name = statusCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = statusCharVal, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = numOfDaysPastDueThreshold, required = true, type = integer)
 *            , @AlgorithmSoftParameter (name = amountThreashold, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = includeUnPayInThreasholdEv, type = boolean)
 *            , @AlgorithmSoftParameter (name = toDoType, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = toDoRole, type = string)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = charTypeForLogEntry, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = unappContTypesFtrConfig, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = unappContrTypesOptType, required = true, type = string)})
 */
public class CmMonitorDelinquentAccountStatusBalanceAlgComp_Impl extends
		CmMonitorDelinquentAccountStatusBalanceAlgComp_Gen implements
		BusinessObjectStatusAutoTransitionAlgorithmSpot {
	
	private BusinessObjectInstance boInstance;
	private BusinessObjectInstanceKey businessObjectInstKey;

	@Override
	public void setBusinessObject(BusinessObject bo) {
		
	}

	@Override
	public void setBusinessObjectKey(BusinessObjectInstanceKey bo) {
		businessObjectInstKey = bo;
	}

	@Override
	public void setAction(BusinessObjectActionLookup boAction) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public BusinessObjectStatusCode getNextStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BusinessObjectStatusTransitionConditionLookup getNextStatusCondition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getUseDefaultNextStatus() {
		// TODO Auto-generated method stub
		return false;
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
	public boolean getForcePostProcessing() {
		// TODO Auto-generated method stub
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void invoke() {
		Person_Id perId = null;
		String perName = null;
		ToDoType_Id todoTypeId = new ToDoType_Id(getToDoType());
		boInstance = BusinessObjectDispatcher.read(businessObjectInstKey, true);
		CmDelinquencyProcess_Id delinquencyProcessId = new CmDelinquencyProcess_Id(this.businessObjectInstKey.getString("delinquencyProcessId"));
		BusinessObjectStatusCode businessObjectStatusCd = new BusinessObjectStatusCode(boInstance.getBusinessObject().getId(), delinquencyProcessId.getEntity().getStatus());
		
		
		String acctId = boInstance.getFieldAndMDForPath("accountId").getXMLValue();
		Account_Id accountId = new Account_Id(acctId);
		String collectionClass = accountId.getEntity().getCollectionClassId().getTrimmedValue();
		// Get Primary Person
		ListFilter<AccountPerson> acctPerListFilter = accountId.getEntity().getPersons().createFilter(" where this.isMainCustomer =:mainCustSw", "CmMonitorDelinquentAccountStatusBalanceAlgComp_Impl");
		acctPerListFilter.bindBoolean("mainCustSw", Bool.TRUE);
		AccountPerson accountPerson = acctPerListFilter.firstRow();
		perId = accountPerson.getId().getPersonId();
		perName = perId.getEntity().getPersonPrimaryName();
		
	//Start CB-359
	   AccountCharacteristic acctChar = accountId.getEntity().getEffectiveCharacteristic(getStatusCharType());
	   if(isNull(acctChar) || !acctChar.getCharacteristicValue().trim().equals(getStatusCharVal().trim()))
		{
			return;
		}
	   
		/*PersonCharacteristic perChar = perId.getEntity().getEffectiveCharacteristic(getStatusCharType());
		if(isNull(perChar) || !perChar.getCharacteristicValue().trim().equals(getStatusCharVal().trim()))
		{
			return;
		}
		*/
	   
	// End CB-359
		// Call Compute Account Overdue Balance
		CmComputeAccountOverdueBalance computeAccountOverdueBal = CmComputeAccountOverdueBalance.Factory.newInstance();
		
		computeAccountOverdueBal.setAccountId(accountId.getTrimmedValue());
		computeAccountOverdueBal.setDaysPastDue(getNumOfDaysPastDueThreshold());
		if(isNull(getIncludeUnPayInThreasholdEv()))
		{
			computeAccountOverdueBal.setIncludeUnapplied(false);
		}
		else
		{
			computeAccountOverdueBal.setIncludeUnapplied(getIncludeUnPayInThreasholdEv().value());
		}
		computeAccountOverdueBal.setContractTypesFeatureConfiguration(getUnappContTypesFtrConfig());
		computeAccountOverdueBal.setContractTypesOptionType(getUnappContrTypesOptType());
		
		
		Money overdueBal = computeAccountOverdueBal.getOverdueBalance();
		
		Money threasholdAmt = new Money(getAmountThreashold());
		
		if(overdueBal.isLessThanOrEqual(threasholdAmt))
		{
			// Checking for existing ToDo Entry
			Query<QueryResultRow> query = checkForExistingToDoEntry(todoTypeId,delinquencyProcessId);
			if(query.listSize() == 0)
			{				
				ToDoEntry_Id todoEntryId = createTodoEntry(todoTypeId,delinquencyProcessId,perName,businessObjectStatusCd,collectionClass);
				
				// Adding Log Entry for Delinquency Process Log
				 createDelinqProcLog(delinquencyProcessId, todoEntryId);
			}
			
		}
		
	}

	/**
	 * @param delinquencyProcessId
	 * @param todoEntryId
	 */
	private void createDelinqProcLog(CmDelinquencyProcess_Id delinquencyProcessId,ToDoEntry_Id todoEntryId) {
		
		MaintenanceObjectLogHelper<CmDelinquencyProcess, CmDelinquencyProcessLog> logHelper = new MaintenanceObjectLogHelper<
		            CmDelinquencyProcess, CmDelinquencyProcessLog>(
		            		delinquencyProcessId.getEntity().getBusinessObject().getMaintenanceObject(), delinquencyProcessId.getEntity());
		    logHelper.addLogEntry(LogEntryTypeLookup.constants.SYSTEM,
		    		CmMessageRepository.todoCreated(), null, getCharTypeForLogEntry(),todoEntryId.getEntity());
	}

	/**
	 * @param todoTypeId
	 * @param delinquencyProcessId
	 * @return
	 */
	private Query<QueryResultRow> checkForExistingToDoEntry(
			ToDoType_Id todoTypeId, CmDelinquencyProcess_Id delinquencyProcessId) {
		StringBuilder tdQueryStr = new StringBuilder();
		tdQueryStr.append("from ToDoEntry te,ToDoDrillKeyValue dk ");
		tdQueryStr.append("where te.id = dk.id.toDo.id ");
		tdQueryStr.append("and te.toDoType.id = :tdTypeCd ");
		tdQueryStr.append("and dk.keyValue = :drillKey ");
		
		Query<QueryResultRow> query = createQuery(tdQueryStr.toString(), "getToDoEntry");
		query.bindId("tdTypeCd", todoTypeId);
		query.bindStringProperty("drillKey", ToDoDrillKeyValue.properties.keyValue, delinquencyProcessId.getTrimmedValue());
		return query;
	}
	
	public ToDoEntry_Id createTodoEntry(ToDoType_Id todoTypeId,CmDelinquencyProcess_Id delinquencyProcessId,String perName,BusinessObjectStatusCode businessObjectStatusCd,String collectionClass)
	{

		ToDoEntry_Id toDoEntryId = null;
		ToDoType todoType = todoTypeId.getEntity();
		
		ToDoCreator creator = ToDoCreator.Factory.newInstance();
		ToDoEntry_DTO entryDto = (ToDoEntry_DTO) createDTO(ToDoEntry.class);

		// Set To Do Type Id
		entryDto.setToDoTypeId(todoTypeId);

		// Set To Do Role if provided parameter To Do Role. If blank then use
		// default for To Do Type
		if (notNull(getToDoRole())) {

			Role_Id toDoRoleId = new Role_Id(getToDoRole());

			if (notNull(toDoRoleId)) {
				entryDto.setToDoRoleId(toDoRoleId);
			} else {
				toDoRoleId = todoTypeId.getEntity().getDefaultRole().fetchIdToDoRole().getId();
				entryDto.setToDoRoleId(toDoRoleId);
			}

		}

		creator.setToDoDTO(entryDto);

		// Set Drill by passing Delinquency Process Id
		if (!todoTypeId.getEntity().getDrillKeyTypes().isEmpty()) {

			ToDoDrillKeyType drillKeyType = todoTypeId.getEntity().getDrillKeyTypes().iterator().next();
			creator.addDrillKeyValue(drillKeyType, delinquencyProcessId.getTrimmedValue());
		}

		// Set Sort Key Customer Name Delinquency Process Status
		// description Collection Class
		if (!todoType.getSortKeyTypes().isEmpty()) {
			for (ToDoSortKeyType sortKeyType : todoType.getSortKeyTypes()) {
				if (sortKeyType.fetchIdSequence().equals(BigInteger.TEN)) {
					if (notNull(perName)) {
						creator.addSortKeyValue(sortKeyType, perName.trim());
					}
				}
				if (sortKeyType.fetchIdSequence().equals(BigInteger.valueOf(20))) {
					creator.addSortKeyValue(sortKeyType, businessObjectStatusCd.getDescription());
				}
				if (sortKeyType.fetchIdSequence().equals(BigInteger.valueOf(30))) {
					if (notNull(collectionClass)) {
						creator.addSortKeyValue(sortKeyType, collectionClass.trim());
					}
					
				}
			}
		}

		toDoEntryId = creator.create().getId();

		return toDoEntryId;
	
	}


}
