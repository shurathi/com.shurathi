/*                                                               
 ************************************************************************************************************                                                               
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Create To Do for Account Delinquency Process
 * 
 * This algorithm creates a To Do entry using the input To Do Type
 * It also adds a log entry to the delinquency process setting the 
 * Char Type to the input To Do Entry Characteristic Type and the 
 * Characteristic Value to the new To Do Entry ID and setting 
 * the Message Category or Number to the input Message Category or Number
 * 
 ************************************************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 *           
 * 2020-08-21   IGarg		CB-333.Initial version
 * 2020-09-04   JFortin     CB-384
 * 							 
 ************************************************************************************************************
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
import com.splwg.base.api.lookup.BusinessObjectStatusTransitionConditionLookup;
import com.splwg.base.api.lookup.LogEntryTypeLookup;
import com.splwg.base.api.maintenanceObject.MaintenanceObjectLogHelper;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectEnterStatusAlgorithmSpot;
import com.splwg.base.domain.common.businessObjectStatusReason.BusinessObjectStatusReason_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicEntity_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.message.MessageParameters;
import com.splwg.base.domain.common.message.Message_Id;
import com.splwg.base.domain.todo.role.Role_Id;
import com.splwg.base.domain.todo.toDoEntry.ToDoCreator;
import com.splwg.base.domain.todo.toDoEntry.ToDoDrillKeyValue;
import com.splwg.base.domain.todo.toDoEntry.ToDoEntry;
import com.splwg.base.domain.todo.toDoEntry.ToDoEntry_DTO;
import com.splwg.base.domain.todo.toDoEntry.ToDoEntry_Id;
import com.splwg.base.domain.todo.toDoType.ToDoDrillKeyType;
import com.splwg.base.domain.todo.toDoType.ToDoSortKeyType;
import com.splwg.base.domain.todo.toDoType.ToDoTypeRole_Id;
import com.splwg.base.domain.todo.toDoType.ToDoType_Id;
import com.splwg.ccb.domain.customerinfo.account.AccountPerson;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.cm.api.lookup.CharacteristicEntityLookup;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessLog;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;
import com.splwg.shared.common.ServerMessage;

/**
 * @author MugdhaP
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = toDoType, name = toDoType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = role, name = toDoRole, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = logEntryCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = messageCategory, name = messageCategory, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = messageNumber, required = true, type = integer)})
 */
public class CmCreateToDoForAccountDelinquencyAlgComp_Impl extends CmCreateToDoForAccountDelinquencyAlgComp_Gen implements BusinessObjectEnterStatusAlgorithmSpot {

	@SuppressWarnings("unused")
	private BusinessObject businessObject;
	private BusinessObjectInstanceKey businessObjectInstKey;
	private BusinessObjectInstance boInstance;
	private CmDelinquencyProcess_Id delinquencyProcess_Id;

	protected void extraSoftParameterValidations(boolean forAlgorithmValidation) {

		// MessageCategory and messageNumber Combination validation
		if (isNull(new Message_Id(getMessageCategory(), getMessageNumber())
				.getEntity())) {
			addError(MessageRepository
					.msgNumberandMsgCategoryCombinationInvalid(
							getMessageCategory().getId().getIdValue()
									.toString(), getMessageNumber().toString()));
		}

		// If provided parameter To Do Type Role should be valid for parameter
		// To Do Type
		if (notNull(getToDoRole())) {
			ToDoTypeRole_Id todoRoleId = new ToDoTypeRole_Id(getToDoType(), getToDoRole());
			if (isNull(todoRoleId.getEntity())) {
				addError(MessageRepository.inValidRoleForTodoType(getToDoRole().getId().getIdValue().trim(), getToDoType().getId().getIdValue().trim()));
			}
		}

		// Parameter Characteristic Type for Log Entry FK to To Do should be
		// valid for the Delinquency Process Log entity
		validateCharTypeForEntity(getLogEntryCharacteristicType(), CharacteristicEntityLookup.constants.CM_DELINQUENCY_PROCESS_LOG);

	}

	@Override
	public void invoke() {
		Account_Id accountId = null;
		String customerName = null;
		Person_Id perId = null;
		BusinessObjectStatusCode businessObjectStatusCd = null;
		
		// Read Business Object
		boInstance = BusinessObjectDispatcher.read(businessObjectInstKey, false);
		
		// Read the Delinquency Process object using the hard parameter business
		// object
		delinquencyProcess_Id = new CmDelinquencyProcess_Id(businessObjectInstKey.getString(CmCreateToDoForDelinquencyConstant.DELINQUENCY_PROCESS_ID));
		
		// Validate Delinquency Process Id
		if (isNull(delinquencyProcess_Id.getEntity())) {

			addError(MessageRepository.delinquencyProcessRequired());
		}
		CmDelinquencyProcess delinquencyProcess = delinquencyProcess_Id.getEntity();

		// Retrieve the Account of Delinquency Process
		accountId = new Account_Id(boInstance.getString(CmCreateToDoForDelinquencyConstant.ACCOUNT_ID));
		// Retrieve Business Object Status
		businessObjectStatusCd = new BusinessObjectStatusCode(boInstance.getBusinessObject().getId(), delinquencyProcess.getStatus());

		// If both are null or both are populated issue an error
		if (isNull(accountId)) {

			addError(MessageRepository.delLevelRequired());
		}


		
		//Retrieve Customer and Collection Class
		String collectionClass = accountId.getEntity().getCollectionClassId().getTrimmedValue();
		ListFilter<AccountPerson> acctPerListFilter = accountId.getEntity().getPersons().createFilter(" where this.isMainCustomer =:mainCustSw", "CmMonitorDelinquentAccountStatusBalanceAlgComp_Impl");
		acctPerListFilter.bindBoolean("mainCustSw", Bool.TRUE);
		AccountPerson accountPerson = acctPerListFilter.firstRow();
		perId = accountPerson.getId().getPersonId();
		customerName = perId.getEntity().getPersonPrimaryName();
		
        Bool toDoEntryExists = checkForExistingToDoEntry(getToDoType().getId(), delinquencyProcess_Id);
        if (toDoEntryExists.isTrue()) {
    		// Create To Do Entry
    		ToDoEntry_Id toDoEntryId = createToDoEntry(delinquencyProcess_Id, businessObjectStatusCd, customerName, collectionClass);

    		if (notNull(toDoEntryId)) {
                // Create Delinquency Log
                createDelinquencyProcessLog(delinquencyProcess, toDoEntryId);
    		}
        }
	}

    /**
     * Check if a To Do Entry exists for the Delinquency Process
     * @param todoTypeId To Do Type
     * @param delinquencyProcessId Delinquency Process ID
     * @return Bool.TRUE : To Do Entry exists
     */
    private Bool checkForExistingToDoEntry(ToDoType_Id todoTypeId, CmDelinquencyProcess_Id delinquencyProcessId) {
        StringBuilder tdQueryStr = new StringBuilder();
        tdQueryStr.append("from ToDoEntry te,ToDoDrillKeyValue dk ");
        tdQueryStr.append("where te.id = dk.id.toDo.id ");
        tdQueryStr.append("and te.toDoType.id = :tdTypeCd ");
        tdQueryStr.append("and dk.keyValue = :drillKey ");
        
        Query<QueryResultRow> query = createQuery(tdQueryStr.toString(), "getToDoEntry");
        query.bindId("tdTypeCd", todoTypeId);
        query.bindStringProperty("drillKey", ToDoDrillKeyValue.properties.keyValue, delinquencyProcessId.getTrimmedValue());
        return Bool.valueOf(query.listSize() == 0);
    }

	/**
	 * This method creates delinquency process log
	 * @parm delProcess
	 * @parm toDoEntryId
	 */
	private void createDelinquencyProcessLog(CmDelinquencyProcess delProcess, ToDoEntry_Id toDoEntId) {

		MessageParameters messageParms = new MessageParameters();
		ServerMessage message = com.splwg.base.domain.common.message.ServerMessageFactory.Factory.newInstance().createMessage(getMessageCategory().getId(), getMessageNumber().intValue(), messageParms);

		// Add Delinquency Process Log
		MaintenanceObjectLogHelper<CmDelinquencyProcess, CmDelinquencyProcessLog> delProcLogHelper = new MaintenanceObjectLogHelper<CmDelinquencyProcess,
				CmDelinquencyProcessLog>(delProcess.getBusinessObject().getMaintenanceObject(), delProcess);
		delProcLogHelper.addLogEntry(LogEntryTypeLookup.constants.TO_DO, message, null, getLogEntryCharacteristicType(), toDoEntId.getEntity());

	}

	/**
	 * This method creates to do entry 
	 * @return toDoEntry_Id
	 */
	private ToDoEntry_Id createToDoEntry(CmDelinquencyProcess_Id delinquencyProcess_Id, BusinessObjectStatusCode businessObjectStatusCd, String customerName, String collectionClass) {
		ToDoEntry_Id toDoEntryId = null;

		ToDoCreator creator = ToDoCreator.Factory.newInstance();
		ToDoEntry_DTO entryDto = (ToDoEntry_DTO) createDTO(ToDoEntry.class);

		// Set To Do Type Id
		entryDto.setToDoTypeId(getToDoType().getId());

		// Set To Do Role if provided parameter To Do Role. If blank then use
		// default for To Do Type
		if (notNull(getToDoRole())) {

			Role_Id toDoRoleId = getToDoRole().getId();

			if (notNull(toDoRoleId)) {
				entryDto.setToDoRoleId(toDoRoleId);
			} else {
				toDoRoleId = getToDoType().getDefaultRole().fetchIdToDoRole().getId();
				entryDto.setToDoRoleId(toDoRoleId);
			}

		}

		// Set message
		entryDto.setMessageId(MessageRepository.toDoMessage(customerName.trim(), businessObjectStatusCd.getDescription()).getMessageId());
		ServerMessage serverMessage = MessageRepository.toDoMessage(customerName.trim(), businessObjectStatusCd.getDescription());
		for (Object parameter : serverMessage.getMessageParameters().getParameters()) {

			creator.addMessageParameter(parameter.toString());
		}
		creator.setToDoDTO(entryDto);

		// Set Drill by passing Delinquency Process Id
		if (!getToDoType().getDrillKeyTypes().isEmpty()) {

			ToDoDrillKeyType drillKeyType = getToDoType().getDrillKeyTypes().iterator().next();
			creator.addDrillKeyValue(drillKeyType, delinquencyProcess_Id.getTrimmedValue());
		}

		// Set Sort Key Customer Name Delinquency Process Status
		// description Collection Class
		if (!getToDoType().getSortKeyTypes().isEmpty()) {
			for (ToDoSortKeyType sortKeyType : getToDoType().getSortKeyTypes()) {
				if (sortKeyType.fetchIdSequence().equals(BigInteger.TEN)) {
					if (notNull(customerName)) {
						creator.addSortKeyValue(sortKeyType, customerName.trim());
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

	/**
	 * Validates that the Characteristic Type is valid for the given entity.
	 * 
	 * @param charType
	 * @param charEntity
	 */
	private void validateCharTypeForEntity(CharacteristicType charType, CharacteristicEntityLookup charEntity) {

		CharacteristicEntity_Id charEntityId = new CharacteristicEntity_Id(charType, charEntity);

		if (isNull(charEntityId.getEntity())) {

			addError(MessageRepository.charTypeIsInvalidForEntity(charType.getId(), charEntity.getLookupValue().getEffectiveDescription()));
		}

	}

	@Override
	public void setBusinessObject(BusinessObject paramBusinessObject) {
		this.businessObject = paramBusinessObject;

	}

	@Override
	public void setBusinessObjectKey(BusinessObjectInstanceKey paramBusinessObjectInstanceKey) {
		this.businessObjectInstKey = paramBusinessObjectInstanceKey;

	}

	@Override
	public BusinessObjectStatusCode getNextStatus() {
		return null;
	}

	@Override
	public BusinessObjectStatusTransitionConditionLookup getNextStatusCondition() {
		return null;
	}

	@Override
	public boolean getUseDefaultNextStatus() {
		return false;
	}

	@Override
	public BusinessObjectStatusReason_Id getStatusChangeReasonId() {
		return null;
	}

}

