/*
 **********************************************************************************************************************************************
 * 
 * PROGRAM DESCRIPTION:
 * 
 * This algorithm has two modes of function 
 * For Customer Level Delinquency Processes if Notify Group Customer or Billing Accounts (C or A) is set to C then it will create a customer
 * contact only for the group customer
 * If Notify Group Customer or Bill Accounts (C or A) is set to A then it will create customer contacts 
 * for every billing account associated to the group customer (Account Override address will be used if specified on the account) 
 * For Account Level Delinquency Processes it creates customer contacts for all the persons associated with the account that have been setup
 * to receive notification
 * If specific account relationship types are provided as soft parameters the algorithm only creates a customer 
 * contact for account persons for the given relationship types  
 * If no account relationship type is provided the algorithm creates customer 
 * contacts regardless of the relationship type
 * For each customer contact created the algorithm adds a log entry to the delinquency process
 * The delinquency process id is also stamped on the customer contact as a characteristic
 * 
 **********************************************************************************************************************************************
 * 
 * 
 * CHANGE HISTORY:
 * 
 * Date:      	by:         Reason:
 * 2020-05-06   MugdhaP		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework
 * 2020-08-26   JFerna      CB-267. Updated Customer Contact creation logic 
 * 2020-09-04	KGhuge		CB-283.	Delinquency - Email Extract Algorithms	
 **********************************************************************************************************************************************
 */
package com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.splwg.base.api.ListFilter;
import com.splwg.base.api.Query;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.BusinessObjectStatusCode;
import com.splwg.base.api.businessObject.COTSInstanceList;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.lookup.BusinessObjectStatusTransitionConditionLookup;
import com.splwg.base.api.lookup.CharacteristicTypeLookup;
import com.splwg.base.api.lookup.LogEntryTypeLookup;
import com.splwg.base.api.maintenanceObject.MaintenanceObjectLogHelper;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectEnterStatusAlgorithmSpot;
import com.splwg.base.domain.common.businessObject.BusinessObject_Id;
import com.splwg.base.domain.common.businessObjectStatusReason.BusinessObjectStatusReason_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicEntity_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.characteristicType.CharacteristicType_Id;
import com.splwg.base.domain.common.message.MessageParameters;
import com.splwg.base.domain.common.message.Message_Id;
import com.splwg.cm.api.lookup.BillRoutingMethodLookup;
import com.splwg.cm.api.lookup.CharacteristicEntityLookup;
import com.splwg.ccb.api.lookup.ContactMethodLookup;
import com.splwg.ccb.domain.admin.billRouteType.BillRouteType;
import com.splwg.ccb.domain.admin.customerContactType.CharacteristicTypeContactType;
import com.splwg.ccb.domain.admin.customerContactType.CustomerContactType;
import com.splwg.ccb.domain.admin.customerContactType.CustomerContactType_Id;
import com.splwg.ccb.domain.admin.idType.accountIdType.AccountNumberType_Id;
import com.splwg.ccb.domain.customerinfo.account.Account;
import com.splwg.ccb.domain.customerinfo.account.AccountPerson;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.customerinfo.customerContact.CustomerContact;
import com.splwg.ccb.domain.customerinfo.customerContact.CustomerContactCharacteristic_DTO;
import com.splwg.ccb.domain.customerinfo.customerContact.CustomerContact_Id;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessLog;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.cm.domain.delinquency.interfaces.customerInformation.CmServiceConstants;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;
import com.splwg.shared.common.ServerMessage;

/**
 * @author MugdhaP
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = customerContactClass, name = customerContactClass, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = customerContactType, required = true, type = string)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = customerContactCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = messageCategory, name = messageCategory, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = messageNumber, required = true, type = integer)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = delinquencyProcessCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = contactMethod, name = defaultContactMethod, required = true, type = lookup)
 *            , @AlgorithmSoftParameter (entityName = billRouteType, name = postalBillRouteType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = billRouteType, name = electronicBillRouteType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = notifyGroupCustomerOrBillAccounts, required = true, type = string)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = accountCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = accountRelationshipType, name = accountRelationshipType1, type = entity)
 *            , @AlgorithmSoftParameter (entityName = accountRelationshipType, name = accountRelationshipType2, type = entity)
 *            , @AlgorithmSoftParameter (entityName = accountRelationshipType, name = accountRelationshipType3, type = entity)
 *            , @AlgorithmSoftParameter (entityName = accountRelationshipType, name = accountRelationshipType4, type = entity)
 *            , @AlgorithmSoftParameter (entityName = accountRelationshipType, name = accountRelationshipType5, type = entity)})
 */
public class CmCreateDelinquencyCustomerContactAlgComp_Impl extends CmCreateDelinquencyCustomerContactAlgComp_Gen implements BusinessObjectEnterStatusAlgorithmSpot {

	private static final String customer = "C";
	private static final String account = "A";
	private  BusinessObjectInstanceKey businessObjectInstKey = null;
	private BusinessObjectInstance boInstance = null;

	@Override
	public void invoke() {

		String personId = null;
		String accountId = null;
		
		
		// Fetch Delinquency process Id
		CmDelinquencyProcess_Id delProcId = new CmDelinquencyProcess_Id(businessObjectInstKey.getString("delinquencyProcessId"));
		this.boInstance = BusinessObjectDispatcher.read(this.businessObjectInstKey, true);

		// Fetch Person Id
		if (notNull(this.boInstance.getElement().selectSingleNode("personId")))
			personId = this.boInstance.getElement().selectSingleNode("personId").getText();

		// Fetch Account Id
		if (notNull(this.boInstance.getElement().selectSingleNode("accountId")))
			accountId = this.boInstance.getElement().selectSingleNode("accountId").getText();

		if (isNull(accountId) && isNull(personId))
			addError(MessageRepository.accoutAndPersonNotFound());

		if (notNull(personId))
			evaluateCustomerLevel(new Person_Id(personId), delProcId);

		if (notNull(accountId))
			evaluateAccountLevel(new Account_Id(accountId).getEntity(), delProcId);

	}

	/**
	 * Performs parameter validation.
	 * 
	 * @param forAlgorithmValidation
	 */
	protected void extraSoftParameterValidations(boolean forAlgorithmValidation) {

		// Check that the Customer Contact Type is valid for the Customer Contact Class
		CustomerContactType_Id ccTypeId = new CustomerContactType_Id(getCustomerContactClass().getId(), getCustomerContactType());
		if (isNull(ccTypeId.getEntity())) {
			addError(MessageRepository.invalidCustomerContact(getAlgorithm().fetchLanguageDescription(), getAlgorithm().getAlgorithmType().getParameterAt(1).fetchLanguageParameterLabel(), getCustomerContactType(), getCustomerContactClass().fetchLanguageDescription()));
		}

		// Check that the Message Category parameter value and Message Number parameter value combination is valid
		Message_Id messageId = new Message_Id(getMessageCategory(), getMessageNumber());
		if (isNull(messageId.getEntity()))
		{
			addError(MessageRepository.invalidMessageCategoryMessageNumberCombination(getAlgorithm().fetchLanguageDescription(), getAlgorithm().getAlgorithmType().getParameterAt(1).fetchLanguageParameterLabel(), getMessageCategory().fetchLanguageDescription(), String.valueOf(getMessageNumber())));
		}

		// Check if Delinquency Process Characteristic Type parameter value is a valid characteristic for Customer Contact entity
		validateCharTypeForEntity(getDelinquencyProcessCharacteristicType(), CharacteristicEntityLookup.constants.CUSTOMER_CONTACT);

		// Check if Notify Group Customer or Bill Accounts  (C or A) parameter
		// value has a value of either Y or N
		if (!getNotifyGroupCustomerOrBillAccounts().equals(customer) && !getNotifyGroupCustomerOrBillAccounts().equals(account))
		{
			addError(MessageRepository.invalidNotifyGroupCustomerOrAccountsValue(customer, account));
		}

		// Check if Account Characteristic Type parameter value is a valid
		// characteristic for Account entity
		validateCharTypeForEntity(getAccountCharacteristicType(), CharacteristicEntityLookup.constants.CUSTOMER_CONTACT);
		
		// Check if Customer Contact Characteristic Type parameter value is a valid
		// characteristic for Delinquency Process Log entity
		validateCharTypeForEntity(getCustomerContactCharacteristicType(), CharacteristicEntityLookup.constants.CM_DELINQUENCY_PROCESS_LOG);

	}

	/**
	 * Validates that the Characteristic Type is valid for the given entity.
	 * 
	 * @param charType
	 * @param charEntity
	 */
	private void validateCharTypeForEntity(CharacteristicType charType, CharacteristicEntityLookup charEntity) {
		// Validate If Characteristic Type is Valid Entity
		CharacteristicEntity_Id charEntityId = new CharacteristicEntity_Id(charType, charEntity);
		// If Characteristic Entity is not found,raise an error
		if (isNull(charEntityId.getEntity())) {
			addError(MessageRepository.charTypeIsInvalidForEntity(charType.getId(), charEntity.getLookupValue().getEffectiveDescription()));
		}

	}

	/**
	 * Evaluate the Account Level
	 * @param account
	 * @param delinquencyProcessId
	 */
	private void evaluateAccountLevel(Account account, CmDelinquencyProcess_Id delinquencyProcess_Id) {

		List<AccountPerson> accountPersonList = new ArrayList<AccountPerson>();
		// Fetch Person list with account relationship
		if (notNull(this.getAccountRelationshipType1()) || notNull(this.getAccountRelationshipType2()) || notNull(this.getAccountRelationshipType3()) || notNull(this.getAccountRelationshipType4())
				|| notNull(this.getAccountRelationshipType5())) {
			// Append Account Relation type
			StringBuilder accountPersonQuery = new StringBuilder().append(" AND this.accountRelationshipType IN  ( ");
			
			if(notNull(getAccountRelationshipType1()))
				accountPersonQuery = accountPersonQuery.append("'" + getAccountRelationshipType1().getId().getIdValue() + "' ,");
			
			if(notNull(getAccountRelationshipType2()))
				accountPersonQuery = accountPersonQuery.append("'" + getAccountRelationshipType2().getId().getIdValue() + "' ,");
			
			if(notNull(getAccountRelationshipType3()))
				accountPersonQuery = accountPersonQuery.append("'" + getAccountRelationshipType3().getId().getIdValue() + "' ,");
			
			if(notNull(getAccountRelationshipType4()))
				accountPersonQuery = accountPersonQuery.append("'" + getAccountRelationshipType4().getId().getIdValue() + "' ,");
			
			if(notNull(getAccountRelationshipType5()))
				accountPersonQuery = accountPersonQuery.append("'" + getAccountRelationshipType5().getId().getIdValue() + "' ,");
			
			
			
			// Replace last comma with closing bracket
			int indexOflastCondition = accountPersonQuery.lastIndexOf(",");
			accountPersonQuery = accountPersonQuery.replace(indexOflastCondition, indexOflastCondition + 1, ")");

			ListFilter<AccountPerson> accountPersonListFilter = account.getPersons().createFilter(" where this.receivesNotification = :notifySwitch " + accountPersonQuery.toString(), "CmCreateDelinquencyCustomerContactAlgComp_Impl");
			accountPersonListFilter.bindBoolean("notifySwitch", Bool.TRUE);
			accountPersonList = accountPersonListFilter.list();
		}
		// Fetch person without account relation
		else {
			ListFilter<AccountPerson> accountPersonListFilter = account.getPersons().createFilter(" where this.receivesNotification = :notifySwitch ", "CmCreateDelinquencyCustomerContactAlgComp_Impl");
			accountPersonListFilter.bindBoolean("notifySwitch", Bool.TRUE);
			accountPersonList = accountPersonListFilter.list();
		}

		if (notNull(accountPersonList) && !accountPersonList.isEmpty()) {
			Iterator<AccountPerson> accountPersonIterator = accountPersonList.listIterator();
			// For each account person add customer contact on person
			AccountPerson accountPerson = null;
			while (accountPersonIterator.hasNext()) {
				accountPerson = accountPersonIterator.next();
				if (notNull(accountPerson)) {
					createCustomerContact(accountPerson.fetchIdPerson().getId(), delinquencyProcess_Id, accountPerson.fetchIdAccount().getId().getIdValue(), accountPerson);
				}
			}

		}

	}

	/**
	 * Evaluate the Customer Level
	 * @param perId
	 * @param delinquencyProcessId
	 */
	private void evaluateCustomerLevel(Person_Id perId, CmDelinquencyProcess_Id delinquencyProcess_Id)
	{
		if (getNotifyGroupCustomerOrBillAccounts().equals(customer))
		{
			// Create Customer Contact for Group Customer
			createCustomerContact(perId, delinquencyProcess_Id, null, null);
		}
		else
		{
			// Create Customer Contact for Group Customer
			createCustomerContact(perId, delinquencyProcess_Id, null, null);

			// Retrieve all accounts where current person is the Main Customer
			final StringBuilder RETRIEVE_ACCOUNTS = new StringBuilder()
					.append(" From  AccountPerson accountPerson ")
					.append(" WHERE accountPerson.id.person = :customer")
					.append(" AND accountPerson.isMainCustomer = :isMainCustomer ")
					.append(" AND EXISTS (SELECT accountNumber.id.account from AccountNumber accountNumber WHERE accountNumber.id.account = accountPerson.id.account ")
					.append(" AND accountNumber.id.accountIdentifierType = :accountIdentifierType)");

			Query<AccountPerson> query = createQuery(
					RETRIEVE_ACCOUNTS.toString(),
					"CmCreateDelinquencyCustomerContactAlgComp_Impl");
			query.bindEntity("accountIdentifierType", new AccountNumberType_Id(CmServiceConstants.BILL_GROUP_ACCOUNT_NBR_TYPE_CD).getEntity());
			query.bindEntity("customer", perId.getEntity());
			query.bindBoolean("isMainCustomer", Bool.TRUE);

			query.addResult("accountPerson", "accountPerson");

			List<AccountPerson> accountPersonList = query.list();

			for (AccountPerson accountPerson : accountPersonList) {
				// Create customer contact on Main Customer with account Id
				// stamped as characteristic
				createCustomerContact(perId, delinquencyProcess_Id, accountPerson.getId().getAccountId().getIdValue(), accountPerson);
			}

		}
	}

	/**
	 * Creates Customer Contact
	 * @param Person_Id
	 * @param delinquencyProcessId
	 * @param accountId
	 * @param accountPerson 
	 */
	private void createCustomerContact(Person_Id perId, CmDelinquencyProcess_Id delinquencyProcess_Id, String accountId, AccountPerson accountPerson)
	{

		CustomerContact customerContact = null;
		
		//Start Change - CB-267
		//CustomerContactClass_Id customerCClassId = getCustomerContactClass().getId();
		//CustomerContactType_Id customerCCTypeId = new CustomerContactType_Id(customerCClassId.getEntity(), getCustomerContactType());

		//CustomerContact_DTO ccDto = createDTO(CustomerContact.class);
		//ccDto.setPersonId(perId);
		//ccDto.setCustomerContactTypeId(customerCCTypeId);
		//ccDto.setContactDateTime(getProcessDateTime());
		//ccDto.setUserId(getActiveContextUser().getId());
		//customerContact = ccDto.newEntity();

		BusinessObject_Id ccBo = new BusinessObject_Id("CM-CustomerContact");		
		BusinessObjectInstance boInstance = BusinessObjectInstance.create(ccBo.getEntity());
		
		boInstance.set("personId", perId.getIdValue());
		boInstance.set("contactType", getCustomerContactType());
		boInstance.set("contactClass", getCustomerContactClass().getId().getIdValue());
		boInstance.set("user", getActiveContextUser().getId().getIdValue());
		boInstance.set("contactDateTime", getProcessDateTime());
		
		//Add Characteristics
		COTSInstanceList ccCharsBoList = boInstance.getList("customerContactCharacteristic");
		
		//Delinquency Process Char Type
		CharacteristicTypeLookup delinquencyCharTypeLookup = getDelinquencyProcessCharacteristicType().getCharacteristicType();
		COTSInstanceNode ccDelinquencyCharBoListNode = ccCharsBoList.newChild();
		
		//Start Add CB-283
			CustomerContactType_Id custContTypeId = new CustomerContactType_Id(getCustomerContactClass().getId(), getCustomerContactType());
			CustomerContactType custContactType = custContTypeId.getEntity();
			CharacteristicType_Id charId =null;
			CharacteristicTypeContactType custContactChar = null;
			List<SQLResultRow> customerContactCharTypes = getCustContactCharType();
			if(notNull(customerContactCharTypes)){
				for(SQLResultRow row:customerContactCharTypes){
					charId = new CharacteristicType_Id(row.getString("CHAR_TYPE_CD").trim());
					custContactChar = custContactType.getEffectiveCharacteristicTypeContactType(charId.getEntity());
					COTSInstanceNode ccChar = ccCharsBoList.newChild();
					ccChar.set("characteristicType", charId.getIdValue());		
					if(charId.getEntity().getCharacteristicType().isAdhocValue()){
						ccChar.set("adhocCharacteristicValue",custContactChar.getAdhocCharacteristicValue());
					}else if(charId.getEntity().getCharacteristicType().isForeignKeyValue()){
						ccChar.set("characteristicValueForeignKey1", custContactChar.getCharacteristicValueForeignKey1());
					}else if(charId.getEntity().getCharacteristicType().isPredefinedValue()){
						ccChar.set("characteristicValue", custContactChar.getCharacteristicValue());
					}
				}	
			}
		//End Add CB-283
			
		ccDelinquencyCharBoListNode.set("characteristicType", getDelinquencyProcessCharacteristicType().getId().getIdValue());
		if(delinquencyCharTypeLookup.isAdhocValue()){
			ccDelinquencyCharBoListNode.set("adhocCharacteristicValue", delinquencyProcess_Id.getIdValue());
		}else if(delinquencyCharTypeLookup.isPredefinedValue()){
			ccDelinquencyCharBoListNode.set("characteristicValue", delinquencyProcess_Id.getIdValue());
		}else if(delinquencyCharTypeLookup.isForeignKeyValue()){
			ccDelinquencyCharBoListNode.set("characteristicValueForeignKey1", delinquencyProcess_Id.getIdValue());
		}
		
		//Delinquency Process Char Type
		CharacteristicTypeLookup accountCharTypeLookup = getAccountCharacteristicType().getCharacteristicType();
		COTSInstanceNode ccAccountCharBoListNode = ccCharsBoList.newChild();
		ccAccountCharBoListNode.set("characteristicType", getAccountCharacteristicType().getId().getIdValue());
		
		if(accountCharTypeLookup.isAdhocValue()){
			ccAccountCharBoListNode.set("adhocCharacteristicValue", accountId);
		}else if(accountCharTypeLookup.isPredefinedValue()){
			ccAccountCharBoListNode.set("characteristicValue", accountId);
		}else if(accountCharTypeLookup.isForeignKeyValue()){
			ccAccountCharBoListNode.set("characteristicValueForeignKey1", accountId);
		}
		boInstance = BusinessObjectDispatcher.add(boInstance);
		String customerContactStr = boInstance.getDocument().getRootElement().element("customerContactId").getData().toString();		
		CustomerContact_Id customerContactId = new CustomerContact_Id(customerContactStr);
		customerContact = customerContactId.getEntity();
		
		//End Change - CB-267

		// If Customer Contact successfully created
		if (notNull(customerContact))
		{
			populateContactMethod(accountPerson, customerContact);

            //Start Delete - CB-267
			// Stamp Delinquency Process ID as Characteristic on Customer
			// Contact Created
			/*if (notNull(delinquencyProcess_Id))
			{
				createCustomerContactChar(customerContact, getDelinquencyProcessCharacteristicType(), delinquencyProcess_Id.getIdValue());
			}

			// Stamp Account ID as Characteristic on Customer Contact Created
			if (notNull(accountId))
			{
				createCustomerContactChar(customerContact, getAccountCharacteristicType(), accountId);
			}*/
			//End Delete - CB-267

			// Create Delinquency Process Log Entry of customer contact
			// creation
			MessageParameters messageParms = new MessageParameters();
			ServerMessage message = com.splwg.base.domain.common.message.ServerMessageFactory.Factory.newInstance().createMessage(getMessageCategory().getId(), getMessageNumber().intValue(), messageParms);
			MaintenanceObjectLogHelper<CmDelinquencyProcess, CmDelinquencyProcessLog> logHelper = new MaintenanceObjectLogHelper<CmDelinquencyProcess, CmDelinquencyProcessLog>(
					delinquencyProcess_Id.getEntity().getBusinessObject().getMaintenanceObject(), delinquencyProcess_Id.getEntity());
			logHelper.addLogEntry(LogEntryTypeLookup.constants.SYSTEM, message, null, getCustomerContactCharacteristicType(), customerContact);
		}
	}

	//Start Add CB-283
	/*
	 * Populates Customer Contact Characteristic Types
	 * @return SQLResultRow List
	 * */
	private List<SQLResultRow> getCustContactCharType(){
		List<SQLResultRow> custContactCharTypes = null;
		PreparedStatement getCustContactCharTypeStatement = null;
		StringBuilder getCustContactCharTypeQuery = new StringBuilder();
		getCustContactCharTypeQuery.append(" SELECT CHAR_TYPE_CD FROM CI_CHTY_CCTY ");
		getCustContactCharTypeQuery.append(" WHERE CC_TYPE_CD=:customerContactType AND CHAR_TYPE_CD NOT IN('CMDQPROC','C1ACCT','F1OUTMSG') ");
		getCustContactCharTypeStatement = createPreparedStatement(getCustContactCharTypeQuery.toString(), "Fetch Customer Contact Char Type");
		getCustContactCharTypeStatement.bindString("customerContactType",this.getCustomerContactType().trim(),"CC_TYPE_CD");
		custContactCharTypes = getCustContactCharTypeStatement.list();
		return custContactCharTypes;
	}
	//End Add CB-283
	
	/**
	 * Populates Customer Contact
	 * @param accountPerson 
	 * @param customerContact 
	 */
	private void populateContactMethod(AccountPerson accountPerson, CustomerContact customerContact) {

		// Set Contact Method to default
		if (isNull(accountPerson))
			updateContactMethod(customerContact, getDefaultContactMethod());
		else
		{

			BillRouteType billRouteType = accountPerson.fetchBillRouteType();
			// Set Contact Method to Postal
			if (notNull(billRouteType) && billRouteType.getBillRoutingMethod().getLookupValue().fetchIdFieldValue().equals(BillRoutingMethodLookup.constants.POSTAL.trimmedValue())) {
				updateContactMethod(customerContact, ContactMethodLookup.constants.POSTAL);
			}
			else
			{
				// Set Contact Method to Electronic
				if (notNull(billRouteType) && billRouteType.getBillRoutingMethod().getLookupValue().fetchIdFieldValue().trim().equals(BillRoutingMethodLookup.constants.ELECTRONIC.trimmedValue())) {
					updateContactMethod(customerContact, ContactMethodLookup.constants.EMAIL);
				}
				else
				{
					// Set Contact Method to default
					updateContactMethod(customerContact, getDefaultContactMethod());
				}
			}
		}
	}

	/** 
	 * Updates the Contact Method on the Customer Contact
	 * @param customerContact
	 * @param contactMethod
	 */
	private void updateContactMethod(CustomerContact customerContact, ContactMethodLookup contactMethod) {
		
		PreparedStatement ps = createPreparedStatement("UPDATE CI_CC SET CONTACT_METH_FLG = :contactMethod WHERE CC_ID = :customerContactId",
				"CmCreateDelinquencyCustomerContactAlgComp_Impl");

		ps.bindId("customerContactId", customerContact.getId());
		ps.bindLookup("contactMethod", contactMethod);

		ps.setAutoclose(true);
		ps.executeUpdate();

	}

	/**
	 * This method will create the Customer Contact Characteristics
	 * @param newCC
	 * @param charType
	 * @param value
	 */
	private void createCustomerContactChar(CustomerContact newCC, CharacteristicType charType, String value) {

		CustomerContactCharacteristic_DTO ccCharDto = newCC.getCharacteristics().newChildDTO();

		if (charType.getCharacteristicType().isPredefinedValue()) {
			ccCharDto.setCharacteristicValue(value);
		} else if (charType.getCharacteristicType().isForeignKeyValue()) {
			ccCharDto.setCharacteristicValueForeignKey1(value);
		} else {
			ccCharDto.setAdhocCharacteristicValue(value);
		}

		newCC.getCharacteristics().add(ccCharDto, charType.getId());

	}

	@Override
	public void setBusinessObject(BusinessObject paramBusinessObject) {

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

