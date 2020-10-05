/*
 *******************************************************************************************************************
 *
 * PROGRAM DESCRIPTION:
 *
 * This algorithm will hold the delinquency process if there exists a pending payment within tolerance limit
 * or there is an On Account Payment regardless of the amount or an active pay plan exist for any of the
 * delinquent customers accounts.
 *
 *******************************************************************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:       	by:    		Reason:
 * YYYY-MM-DD  	IN     		Reason text.
 * 2020-05-06   MugdhaP		Initial Version : ANTHM-340 CAB1-9462 Delinquency Framework
 * ******************************************************************************************************************
 */
package com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.algorithm;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.ListFilter;
import com.splwg.base.api.Query;
import com.splwg.base.api.QueryResultRow;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.lookup.LogEntryTypeLookup;
import com.splwg.base.api.lookup.YesNoOptionLookup;
import com.splwg.base.api.maintenanceObject.MaintenanceObjectLogHelper;
import com.splwg.base.domain.common.algorithm.Algorithm;
import com.splwg.base.domain.common.algorithm.AlgorithmComponentCache;
import com.splwg.base.domain.common.algorithmType.AlgorithmTypeParameter_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicEntity_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.characteristicType.CharacteristicValue_Id;
import com.splwg.ccb.api.lookup.C1RequestRelationshipObjectTypeLookup;
import com.splwg.ccb.domain.billing.bill.Bill;
import com.splwg.ccb.domain.billing.bill.Bill_Id;
import com.splwg.ccb.domain.common.c1Request.C1Request;
import com.splwg.ccb.domain.common.c1Request.C1RequestCharacteristic;
import com.splwg.ccb.domain.common.c1Request.C1RequestRelatedObject;
import com.splwg.ccb.domain.common.c1Request.C1Request_Id;
import com.splwg.ccb.domain.common.c1RequestType.C1RequestType_Id;
import com.splwg.ccb.domain.customerinfo.account.Account;
import com.splwg.ccb.domain.customerinfo.account.AccountPerson;
import com.splwg.ccb.domain.customerinfo.paymentPlan.PaymentPlan_Id;
import com.splwg.ccb.domain.customerinfo.person.Person;
import com.splwg.cm.api.lookup.CharacteristicEntityLookup;
import com.splwg.cm.api.lookup.CmDelinquencyProcessTypeSystemEventLookup;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmCalculateUnpaidOriginalAmountAlgorithmSpot;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmDelinquencyProcessType;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmDelinquencyProcessTypeAlgorithm;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmHoldDelinquencyProcessCriteriaAlgorithmSpot;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessCharacteristic;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessLog;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessRelatedObject;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm.CmDelinquencyProcessConstant;
import com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm.CmDelinquencyProcessHelper;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;
import com.splwg.shared.common.Dom4JHelper;

/**
 * @author MugdhaP
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (name = tolerancePercentage, type = decimal)
 *            , @AlgorithmSoftParameter (name = toleranceAmount, type = decimal)
 *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = toleranceAmountandPercentageRequired, required = true, type = lookup)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = tenderAmountCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = paymentRequestType, required = true, type = string)
 *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = holdWhenOnAccountBalanceExists, required = true, type = lookup)
 *            , @AlgorithmSoftParameter (entityName = featureConfiguration, name = adminstrativeContractTypeFeatureConfig, required = true, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = customerManagementOption, name = adminstrativeContractTypeOptionType, required = true, type = lookup)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = holdReasonCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = pendingPaymentsOrPayPlanHoldReason, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = paymentRequestStatus, required = true, type = string)})
 */

public class CmHoldDelinquencyProcessForPendingPaymentsOrPayPlanAlgComp_Impl extends CmHoldDelinquencyProcessForPendingPaymentsOrPayPlanAlgComp_Gen implements CmHoldDelinquencyProcessCriteriaAlgorithmSpot {

	private Bool holdProcessSwitch;
	private CmDelinquencyProcess_Id delinquencyProcessId;
	private CmDelinquencyProcessHelper cmDelinquencyProcessHelper;
	private BigDecimal totalCustomerUnpaidAmount;
	private BigDecimal totalCustomerOriginalAmount;
	private BigDecimal totalTenderAmount;
	private String requestTypesFilterStr="";

	@Override
	public Bool getIsHoldProcessSwitch() {
		return holdProcessSwitch;
	}

	@Override
	public void setDelinquencyProcessId(CmDelinquencyProcess_Id delinquencyProcessId) {
		this.delinquencyProcessId = delinquencyProcessId;
	}

	@Override
	protected void extraSoftParameterValidations(boolean forAlgorithmValidation) {

		AlgorithmTypeParameter_Id algTypeParam = null;

		if(getToleranceAmountandPercentageRequired().trimmedValue().equals(YesNoOptionLookup.constants.YES.trimmedValue())){
			if(isNull(getTolerancePercentage())){
				reportRequiredParameter("tolerancePercentage", 0);
			}

			if(isNull(getToleranceAmount())){
				reportRequiredParameter("toleranceAmount", 1);
			}
		}else if(getToleranceAmountandPercentageRequired().trimmedValue().equals(YesNoOptionLookup.constants.NO.trimmedValue())){
			if(isNull(getTolerancePercentage()) && isNull(getToleranceAmount()) ){

				algTypeParam = new AlgorithmTypeParameter_Id(getAlgorithm().getAlgorithmType(), new BigInteger("10"));
				String unpaidAmountLbl = algTypeParam.getEntity().fetchLanguageParameterLabel();

				algTypeParam = new AlgorithmTypeParameter_Id(getAlgorithm().getAlgorithmType(), new BigInteger("20"));
				String unpaidPercentLbl = algTypeParam.getEntity().fetchLanguageParameterLabel();

				addError(MessageRepository.atleastOneParameterShouldBeProvided(unpaidAmountLbl, unpaidPercentLbl));
			}
		}

		if(notNull(getTolerancePercentage())){

			if(getTolerancePercentage().compareTo(BigDecimal.ZERO) < 0 || getTolerancePercentage().compareTo(new BigDecimal("100")) > 0) {
				addError(MessageRepository.invalidAlgorithmParameterValue(this.getAlgorithm(), getAlgorithm().getAlgorithmType().getParameterAt(0).fetchLanguageParameterLabel(), getTolerancePercentage().toString()));
			}
		}

		// separate Request Type
		String[] paymentRequestTypes = getPaymentRequestType().split(",");
		for (int i = 0; i < paymentRequestTypes.length; i++) {
			if (paymentRequestTypes[i].length() > 30)
				addError(MessageRepository.invalidEntity(paymentRequestTypes[i], "Request Type"));
			C1RequestType_Id c1RequestType_Id = new C1RequestType_Id(paymentRequestTypes[i].trim());
			if (isNull(c1RequestType_Id) || isNull(c1RequestType_Id.getEntity())) {
				addError(MessageRepository.invalidEntity(paymentRequestTypes[i], "Request Type"));
			}
			requestTypesFilterStr = requestTypesFilterStr.concat(",'" + paymentRequestTypes[i] + "'");
		}
		requestTypesFilterStr = requestTypesFilterStr.replaceFirst(",", "");
		// separate Request Type
		// Validate Characteristics Types for the Delinquency Process Log entity
		validateCharTypeForEntity(getHoldReasonCharacteristicType(), CharacteristicEntityLookup.constants.CM_DELINQUENCY_PROCESS_LOG);

		// Validate Characteristics Values for Characteristic Type
		validateCharValueForCharType(getHoldReasonCharacteristicType(), getPendingPaymentsOrPayPlanHoldReason());
	}

	@Override
	public void invoke() {
		// Initialize Variables
		holdProcessSwitch = Bool.FALSE;
		cmDelinquencyProcessHelper = CmDelinquencyProcessHelper.Factory.newInstance();
		totalCustomerUnpaidAmount = BigDecimal.ZERO;
		totalCustomerOriginalAmount = BigDecimal.ZERO;
		totalTenderAmount = BigDecimal.ZERO;

		// Retrieve Person and account from delinquency process
		Person person = cmDelinquencyProcessHelper.fetchPersonOfDelinquencyProcess(delinquencyProcessId.getEntity());
		Account account = cmDelinquencyProcessHelper.fetchAccountOfDelinquencyProcess(delinquencyProcessId.getEntity());

		// If both account and person is null through an error
		if (isNull(person) && isNull(account)) {
			// Through Error
			addError(MessageRepository.delinquencyNotLinkedToCustomer(delinquencyProcessId));
		}
		// Check Active payment plan on Person and account
		holdProcessSwitch = checkActivepayPlan(person, account);

		// If Hold Process Switch = False and Hold When On Account Balance
		// Exists - Y/N parameter value = Y
		if (holdProcessSwitch.isFalse() && getHoldWhenOnAccountBalanceExists().isYes()) {
			holdProcessSwitch = checkAccountBalance(person, account);
		}
		// If Hold Process Switch = False
		if (holdProcessSwitch.isFalse()) {
			// Retrieve bill unpaid and original amounts
			retrieveBillsForDelinquencyProcess();

			// Retrieve Pending Payments on account
		
			Bool paymentRequestFound = retrievePendingPaymentRequest(person, account);
			//retrievePendingPaymentRequest(person, account);

			// Check hold Switch
		
			if(paymentRequestFound.isTrue()){
				holdProcessSwitch = getUnpaidAmount();
			}
		}

		if (holdProcessSwitch.isTrue()) {
		
			// Fetch existing delinquency process log for particular char value
			Query<QueryResultRow> existingLogQuery = createQuery(CmHoldDelinquencyProcessConstants.FETCH_EXISTING_DEL_PROC_LOG.toString(), "CmHoldDelinquencyProcessForPendingPaymentsOrPayPlanAlgComp_Impl");
			existingLogQuery.bindId("delProcId", delinquencyProcessId);
			existingLogQuery.bindStringProperty("boStatus", CmDelinquencyProcess.properties.status, delinquencyProcessId.getEntity().getStatus());
			existingLogQuery.bindId("msgCategoryNumber", MessageRepository.addDelProcLogForCharVal(getPendingPaymentsOrPayPlanHoldReason()).getMessageId().getMessageCategoryId());
			existingLogQuery.bindBigInteger("msgNumber", MessageRepository.addDelProcLogForCharVal(getPendingPaymentsOrPayPlanHoldReason()).getMessageId().getMessageNumber());
			existingLogQuery.bindEntity("charType", getHoldReasonCharacteristicType());
			existingLogQuery.bindStringProperty("charValue", CmDelinquencyProcessCharacteristic.properties.characteristicValue, getPendingPaymentsOrPayPlanHoldReason());
			existingLogQuery.addResult("delProcessId", "DPL.id.delinquencyProcess.id");
			existingLogQuery.addResult("sequence", "DPL.id.sequence");

			// If no existing log entry found
			if (existingLogQuery.list().isEmpty()) {

				// No existing log entry add new log entry
				CharacteristicValue_Id charValId = new CharacteristicValue_Id(getHoldReasonCharacteristicType(), getPendingPaymentsOrPayPlanHoldReason());
				MaintenanceObjectLogHelper<CmDelinquencyProcess, CmDelinquencyProcessLog> logHelper = new MaintenanceObjectLogHelper<CmDelinquencyProcess, CmDelinquencyProcessLog>(
						delinquencyProcessId.getEntity().getBusinessObject().getMaintenanceObject(), delinquencyProcessId.getEntity());
				logHelper.addLogEntry(LogEntryTypeLookup.constants.SYSTEM, MessageRepository.addDelProcLogForCharVal(charValId.getEntity().fetchLanguageDescription()), null, charValId.getEntity());
			}

		}
	}

	/**
	 * This method fetch unpaid amount
	 * @return
	 */
	private Bool getUnpaidAmount() {

		// Initialize unpaid amount and unpaid percentage for calculation
		BigDecimal unpaidAmount = BigDecimal.ZERO;
		BigDecimal unpaidAmountPercentage = BigDecimal.ZERO;

		// Added try block to avoid error during computation
		try {
			unpaidAmount = totalCustomerUnpaidAmount.subtract(totalTenderAmount);
			if (unpaidAmount.compareTo(BigDecimal.ZERO) != 0) {
				unpaidAmountPercentage = (unpaidAmount.divide(totalCustomerOriginalAmount)).multiply(new BigDecimal("100"));
			}
		}
		// Add error for calculation
		catch (Exception exp) {
			addError(MessageRepository.errorDuringComputation());
		}
		// If tolerance Amount Percentage required parameter is set to Y
		if (getToleranceAmountandPercentageRequired().isYes()) {
			// If unpaid amount is less than tolerance amount and unpaid
			// percentage is less than tolerance percentage
			if (unpaidAmount.compareTo(getToleranceAmount()) <= 0 && unpaidAmountPercentage.compareTo(getTolerancePercentage()) <= 0) {
				return Bool.TRUE;
			}
		}

		// If tolerance percentage is not required
		else {
			if(notNull(getToleranceAmount()) && unpaidAmount.compareTo(getToleranceAmount()) <= 0){
				return Bool.TRUE;
			}

			if(notNull(getTolerancePercentage()) && unpaidAmountPercentage.compareTo(getTolerancePercentage()) <= 0){
				return Bool.TRUE;
			}
		}

		return Bool.FALSE;
	}

	/**
	 * This method checks active pay plan on customer
	 * @param person
	 * @param account
	 */
	private Bool checkActivepayPlan(Person person, Account account) {

		// if person is populated
		if (notNull(person)) {
			Query<PaymentPlan_Id> query = createQuery(CmDelinquencyProcessConstant.ACTIVE_PAY_PLAN_PERSON_QUERY.toString(), "CmHoldDelinquencyProcessForPendingPaymentsOrPayPlanAlgComp_Impl");
			query.bindEntity("person", person);
			query.addResult("paymentPlanId", "PP.id");
			if (notNull(query.firstRow())) {
				return Bool.TRUE;
			}
		}

		// if account is populated
		if (notNull(account)) {
			Query<PaymentPlan_Id> query = createQuery(CmDelinquencyProcessConstant.ACTIVE_PAY_PLAN_ACCOUNT_QUERY.toString(), "CmHoldDelinquencyProcessForPendingPaymentsOrPayPlanAlgComp_Impl");
			query.bindEntity("account", account);
			query.addResult("paymentPlanId", "PP.id");
			if (notNull(query.firstRow())) {
				return Bool.TRUE;
			}
		}

		return Bool.FALSE;
	}

	/**
	 * This method checks acount balance
	 *
	 * @param person
	 * @param account
	 * @return
	 */
	private Bool checkAccountBalance(Person person, Account account) {

		BigDecimal onAccountPayments = BigDecimal.ZERO;

		// If delinquency is person level
		if (notNull(person)) {
			// Retrieve all accounts of person
			List<Account> accountList = cmDelinquencyProcessHelper.fetchAccountListForPerson(person);

			// For each person
			for (Account accountLinkedToPerson : accountList) {
				onAccountPayments = cmDelinquencyProcessHelper.checkOnAcctPaymentForAccount(accountLinkedToPerson, getAdminstrativeContractTypeFeatureConfig(), getAdminstrativeContractTypeOptionType());
				if (onAccountPayments.compareTo(BigDecimal.ZERO) < 0) {
					return Bool.TRUE;
				}
			}

		}
		// Delinquency is account level
		else {
			onAccountPayments = cmDelinquencyProcessHelper.checkOnAcctPaymentForAccount(account, getAdminstrativeContractTypeFeatureConfig(), getAdminstrativeContractTypeOptionType());
			if (onAccountPayments.compareTo(BigDecimal.ZERO) < 0) {
				return Bool.TRUE;
			}
		}
		return Bool.FALSE;
	}

	/**
	 * This method retrieve bill amounts for delinquency process
	 *
	 */
	private void retrieveBillsForDelinquencyProcess() {

		BigDecimal unpaidBillAmount = BigDecimal.ZERO;
		BigDecimal originalBillAmount = BigDecimal.ZERO;

		// Fetch all related object of delinquency for bill
		ListFilter<CmDelinquencyProcessRelatedObject> delinquencyProcessRelObjListFilter = delinquencyProcessId.getEntity().getRelatedObjects()
				.createFilter(" where this.id.maintenanceObject = 'BILL' ", "CmHoldDelinquencyProcessForPendingPaymentsOrPayPlanAlgComp_Impl");

		// Retrieve Delinquency process type
		CmDelinquencyProcessType delinquencyProcessType = delinquencyProcessId.getEntity().getCmDelinquencyProcessType();

		// Retrieve calculate unpaid amount Algo list from process type
		ListFilter<CmDelinquencyProcessTypeAlgorithm> deliquencyProcessTypeAlgoListFilter = delinquencyProcessType.getAlgorithms().createFilter(" where this.id.cmDelinquencyProcessTypeSystemEvent = :cmDelinquencyProcessTypeSystemEvent "
				+ " order by this.version ASC ", "CmHoldDelinquencyProcessForPendingPaymentsOrPayPlanAlgComp_Impl");
		deliquencyProcessTypeAlgoListFilter.bindLookup("cmDelinquencyProcessTypeSystemEvent", CmDelinquencyProcessTypeSystemEventLookup.constants.CM_CALC_UNPAID_AND_ORIGINAL_AMTS);

		// List of all algos
		List<CmDelinquencyProcessTypeAlgorithm> delinquncyProcessTypeAlgoList = deliquencyProcessTypeAlgoListFilter.list();

		// List of related object
		List<CmDelinquencyProcessRelatedObject> relObjList = delinquencyProcessRelObjListFilter.list();

		// Iterate through all related object
		for (CmDelinquencyProcessRelatedObject delProcRelObject : relObjList) {
			for (CmDelinquencyProcessTypeAlgorithm delinquncyProcessTypeAlgo : delinquncyProcessTypeAlgoList) {
				Algorithm algorithm = delinquncyProcessTypeAlgo.getAlgorithm();

				CmCalculateUnpaidOriginalAmountAlgorithmSpot algorithmComp = AlgorithmComponentCache.getAlgorithmComponent(algorithm.getId(), CmCalculateUnpaidOriginalAmountAlgorithmSpot.class);
				// set Algorithm input parameters.
				algorithmComp.setDelinquencyProcessId(delinquencyProcessId);
				algorithmComp.setDelinquencyProcessRelatedObject(delProcRelObject);
				// Invoke and set the output parameters
				algorithmComp.invoke();

				// Retrieve original and unpaid amount from Algorithm
				originalBillAmount = algorithmComp.getOriginalAmount();
				unpaidBillAmount = algorithmComp.getUnpaidAmount();

				// Add Original amount to total amount
				totalCustomerOriginalAmount = totalCustomerOriginalAmount.add(originalBillAmount);
				totalCustomerUnpaidAmount = totalCustomerUnpaidAmount.add(unpaidBillAmount);
			}
		}
	}

	/**
	 * This method retrieves all pending payments on Person or account
	 * @param person
	 * @param account
	 */

	private Bool retrievePendingPaymentRequest(Person person, Account account) {
	//private void retrievePendingPaymentRequest(Person person, Account account) {

		Bool paymentRequestFound = Bool.FALSE;

		ArrayList<String> statusList = new ArrayList<String>(Arrays.asList(getPaymentRequestStatus().split(",")));
		String statusCodeString = "'"+statusList.toString().replace("[","").replace("]", "").replace(" ","").replace(",","','")+"'";

		Query<C1Request_Id> query = null;

		StringBuilder  personRequests = CmDelinquencyProcessConstant.PENDIN_PAYMENTS_ON_PERSON_QUERY;

		// String personRequestsQuery = personRequests.toString().replace(":paymentRequestTypeParmVal", requestTypesFilterStr);
		String personRequestsQuery = personRequests.toString().replace(":paymentRequestTypeParmVal", requestTypesFilterStr).replace(":paymentStausCodes", statusCodeString.trim());;
	
		// If delinquency is person level
		if (notNull(person)) {
			query = createQuery(personRequestsQuery, "CmHoldDelinquencyProcessForPendingPaymentsOrPayPlanAlgComp_Impl");
			query.bindEntity("person", person);
			query.bindLookup("c1RequestRelationshipObjectTypeLookupPerson", C1RequestRelationshipObjectTypeLookup.constants.PERSON);
			// query.bindLookup("businessObjectStatusConditionLookupInterim", BusinessObjectStatusConditionLookup.constants.INTERIM);		
			query.addResult("requestId", "REQ.id");

		}
		// Delinquency is account level
		else {

			Person mainPerson = null;
			// Get Primary Person
			ListFilter<AccountPerson> acctPerListFilter = account.getPersons().createFilter(" where this.isMainCustomer =:mainCustSw", "CmHoldDelinquencyProcessForPendingPaymentsOrPayPlanAlgComp_Impl");
			acctPerListFilter.bindBoolean("mainCustSw", Bool.TRUE);
			AccountPerson accountPerson = acctPerListFilter.firstRow();

			// if account person is not null
			if (notNull(accountPerson)) {
				mainPerson = accountPerson.fetchIdPerson();
			}
			if (notNull(mainPerson)) {

				query = createQuery(personRequestsQuery, "CmHoldDelinquencyProcessForPendingPaymentsOrPayPlanAlgComp_Impl");
				query.bindEntity("person", mainPerson);
				query.bindLookup("c1RequestRelationshipObjectTypeLookupPerson", C1RequestRelationshipObjectTypeLookup.constants.PERSON);

				// query =
				// createQuery(CmDelinquencyProcessConstant.PENDIN_PAYMENTS_ON_ACCOUNT_QUERY.toString(),
				// "CmHoldDelinquencyProcessForPendingPaymentsOrPayPlanAlgComp_Impl");
				// query.bindEntity("account", account);
				// query.bindLookup("c1RequestRelationshipObjectTypeLookupAccount",
				// C1RequestRelationshipObjectTypeLookup.constants.ACCOUNT);
				// query.bindLookup("businessObjectStatusConditionLookupInterim", BusinessObjectStatusConditionLookup.constants.INTERIM);
				query.bindStringProperty("paymentStausCodes",C1Request.properties.status,statusCodeString);

				query.addResult("requestId", "REQ.id");
			}
		}

		// Request List
		List<C1Request_Id> requestList = query.list();

		if(notNull(requestList) && !requestList.isEmpty()){
			paymentRequestFound = Bool.TRUE;
		}

		// get tender amount for all requests
		for (C1Request_Id requestId : requestList) {
			ListFilter<C1RequestCharacteristic> reqCharListFilter = requestId.getEntity().getCharacteristics().createFilter(" WHERE this.id.characteristicType = :tenderAmountCharType ORDER BY this.id.effectiveDate DESC ", "CmHoldDelinquencyProcessForPendingPaymentsOrPayPlanAlgComp_Impl");
			reqCharListFilter.bindEntity("tenderAmountCharType", getTenderAmountCharacteristicType());
			C1RequestCharacteristic tenderAmountReqChar = reqCharListFilter.firstRow();
			String tenderAmountStr = tenderAmountReqChar.getSearchCharacteristicValue();

			// If tender amount retrieved
			if (notBlank(tenderAmountStr)) {

				// totalTenderAmount = totalTenderAmount.add(new
				// BigDecimal(tenderAmountStr.trim()));

				// Person level Delinquency
				if (notNull(person)) {
					totalTenderAmount = totalTenderAmount.add(new BigDecimal(tenderAmountStr.trim()));
				}
				// Account level Delinquency process
				else {
					// read clob
					Document paymentRequestClob = null;
					try {
						paymentRequestClob = Dom4JHelper.parseText(requestId.getEntity().getDTO().getBusinessObjectDataArea());
					} catch (DocumentException e1) {
						addError(MessageRepository.failedToReadPaymentFromClob());
					}
					// If clob populated
					if (notNull(paymentRequestClob) && paymentRequestClob.hasContent()) {

						@SuppressWarnings("unchecked")
						List<Node> list = paymentRequestClob.selectNodes("//payment");
						for (Node node : list) {
							Element paymentElement = (Element) node;
							String invoiceNumber = ((Element) paymentElement.selectSingleNode("invoiceNumber")).getStringValue();
							Bill bill = new Bill_Id(invoiceNumber).getEntity();
							if (notNull(bill) && bill.getAccount().equals(account)) {
								totalTenderAmount = totalTenderAmount.add(new BigDecimal(tenderAmountStr.trim()));
							}
						}
					}
					// CLOB is not populated for IVR payments
					else {
						// Request related Object
						ListFilter<C1RequestRelatedObject> relObjListFilter = requestId.getEntity().getRelatedObjects()
								.createFilter(" where this.id.c1RequestRelationshipObjectType = :c1RequestRelationshipObjectTypeLookupAccount "
										+ "AND this..primaryKeyValue1 = :account", "CmHoldDelinquencyProcessForPendingPaymentsOrPayPlanAlgComp_Impl");

						relObjListFilter.bindEntity("account", account);
						relObjListFilter.bindLookup("c1RequestRelationshipObjectTypeLookupAccount", C1RequestRelationshipObjectTypeLookup.constants.ACCOUNT);
						C1RequestRelatedObject c1requestRelObj = relObjListFilter.firstRow();
						if (notNull(c1requestRelObj)) {
							totalTenderAmount = totalTenderAmount.add(new BigDecimal(tenderAmountStr.trim()));
						}
					}

				}
			}
		}
		return paymentRequestFound;
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

	/**
	 * Validate Char Value For Char Type
	 * @param charType
	 * @param charValue
	 */
	private void validateCharValueForCharType(CharacteristicType charType, String charValue) {
		if (charType.getCharacteristicType().isPredefinedValue()) {

			CharacteristicValue_Id charValId = new CharacteristicValue_Id(charType, charValue);
			if (isNull(charValId.getEntity())) {

				addError(MessageRepository.charValueIsInvalidForCharType(charType.getId(), charValue));
			}
		}
	}
}

