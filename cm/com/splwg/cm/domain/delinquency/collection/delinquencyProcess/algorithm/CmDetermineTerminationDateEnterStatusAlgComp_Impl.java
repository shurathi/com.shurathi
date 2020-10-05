/*                                                              
 **************************************************************************                                                                
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Manage Delinquency
 * Determine Termination Date
 * This algorithm calculates the termination date based on the most lenient state 
 * and stores the termination effective date rule and termination date 
 * as delinquency process characteristics for subsequent processes
 *                                                             
 **************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 * 2020-05-06   MugdhaP		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework   
 * **************************************************************************
 */

package com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.Query;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.BusinessObjectStatusCode;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.DateFormat;
import com.splwg.base.api.datatypes.DateFormatParseException;
import com.splwg.base.api.lookup.BusinessObjectStatusTransitionConditionLookup;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectEnterStatusAlgorithmSpot;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatus_Id;
import com.splwg.base.domain.common.businessObjectStatusReason.BusinessObjectStatusReason_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicEntity_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.characteristicType.CharacteristicValue_Id;
import com.splwg.ccb.api.lookup.FinancialTransactionTypeLookup;
import com.splwg.ccb.api.lookup.MatchEventStatusLookup;
import com.splwg.ccb.domain.billing.bill.Bill_Id;
import com.splwg.ccb.domain.customerinfo.account.Account;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.cm.api.lookup.CharacteristicEntityLookup;
import com.splwg.cm.api.lookup.CmDelinquencyRelatedObjTypeLookup;
import com.splwg.cm.domain.delinquency.admin.delinquencyCustomerMonitor.CmDelinquencyCustomerMonitorRuleConstants;
import com.splwg.cm.domain.delinquency.admin.overdueEventType.CmOverdueProcessConstants;
import com.splwg.cm.domain.delinquency.admin.overdueEventType.CmTerminationData;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessCharacteristic;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessCharacteristic_DTO;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessCharacteristic_Id;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;

/**
 * @author MugdhaP
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = characteristicType, name = terminationDateRuleCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = dateProcessingRuleCharVal, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = endGraceRuleCharVal, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = retroPaidRuleCharVal, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = currentDueDateRuleCharVal, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = endCoveragePeriodRuleCharVal, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = boStatusTerminationRequest, required = true, type = string)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = endOfGraceCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = terminationDateCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = addDaysToEndOfCoverage, required = true, type = integer)
 *            , @AlgorithmSoftParameter (name = policyTerminatedStatus, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = policyActiveStatus, required = true, type = string)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = membershipCharType, required = true, type = entity)})
 */

public class CmDetermineTerminationDateEnterStatusAlgComp_Impl extends CmDetermineTerminationDateEnterStatusAlgComp_Gen implements BusinessObjectEnterStatusAlgorithmSpot {

	private BusinessObjectInstanceKey businessObjectInstKey = null;
	private BusinessObjectInstance boInstance = null;
	private BusinessObject businessObject = null;
	private String terminationDateRule = null;
	private CmDelinquencyProcess_Id cmDelinquencyProcess_Id = null;
	private CmTerminationData terminationData;
	private Date terminationDate = null;
	private Person_Id personId = null;
	private Account_Id accountId = null;
	private List<Account_Id> customerAccountList;
	
	/**
	 * Performs parameter validation.
	 * 
	 * @param algorithm
	 */
	protected void extraSoftParameterValidations(boolean forAlgorithmValidation) {

		// Validate Characteristics Types for the Delinquency Process entity
		validateCharTypeForEntity(getTerminationDateRuleCharType(), CharacteristicEntityLookup.constants.CM_DELINQUENCY_PROCESS);
		validateCharTypeForEntity(getEndOfGraceCharType(), CharacteristicEntityLookup.constants.CM_DELINQUENCY_PROCESS);
		validateCharTypeForEntity(getTerminationDateCharType(), CharacteristicEntityLookup.constants.CM_DELINQUENCY_PROCESS);

		// Valdate Characteristics Types for the Billable Charge entity
		validateCharTypeForEntity(getMembershipCharType(), CharacteristicEntityLookup.constants.BILLABLE_CHARGE_CHARACTERISTICS);

		// Validate Characteristics Values for the Termination Date Rule
		// Characteristic Type
		validateCharValueForCharType(getTerminationDateRuleCharType(), getDateProcessingRuleCharVal());
		validateCharValueForCharType(getTerminationDateRuleCharType(), getEndGraceRuleCharVal());
		validateCharValueForCharType(getTerminationDateRuleCharType(), getRetroPaidRuleCharVal());
		validateCharValueForCharType(getTerminationDateRuleCharType(), getCurrentDueDateRuleCharVal());
		validateCharValueForCharType(getTerminationDateRuleCharType(), getEndCoveragePeriodRuleCharVal());

	}
	
	@Override
	public void invoke() {

		Date policyStartDate = null;
		// Read Business Object
		boInstance = BusinessObjectDispatcher.read(businessObjectInstKey, false);
		cmDelinquencyProcess_Id = new CmDelinquencyProcess_Id(boInstance.getString(CmDetermineTerminationDateRuleConstants.DELINQUENCY_PROCESS_ID));
		
		// Validate Business Object Status
		validateBusObjStatus(getBoStatusTerminationRequest().trim());
		
		// Call termination effective date rule helper 
		CmDetermineTerminationEffectiveDateRuleHelper cmTerminationEffectiveDate = CmDetermineTerminationEffectiveDateRuleHelper.Factory.newInstance();

		personId = new Person_Id(boInstance.getString(CmDetermineTerminationDateRuleConstants.PERSON_ID));
		if (notNull(personId)) {
			// Retrieve Delinquent Customer Bills Accounts
			customerAccountList = getCustomerAccountList();
		}
		accountId = new Account_Id(boInstance.getString(CmDetermineTerminationDateRuleConstants.ACCOUNT_ID));
		terminationData = cmTerminationEffectiveDate.fetchTerminationEffectiveDateRule(cmDelinquencyProcess_Id);
		terminationDateRule = terminationData.getTerminationEffectiveRule();
		if (isBlankOrNull(terminationDateRule)) {

			addError(MessageRepository.terminationEffectiveDateRuleNotRetrieved());

		} else {
			// Create TerminationEffectiveRule Characteristic
			createDelinquencyProcessCharacteristic(cmDelinquencyProcess_Id, getTerminationDateRuleCharType(), terminationData.getTerminationEffectiveRule());
			// Determine Termination Date
			determineTerminationDate();

			// Retrieve minimum policy date
			if (notNull(customerAccountList) && (customerAccountList.size() > 0)) {
				Date minPolicyStartDate = null;
				for (Account_Id acctId : customerAccountList) {
					// fetch earliest policy start date
					policyStartDate = fetchMinimumPolicyStartDate(acctId);
					if (isNull(minPolicyStartDate)) {

						minPolicyStartDate = policyStartDate;
					}

					else if ((notNull(policyStartDate) && policyStartDate.isBefore(minPolicyStartDate))) {

						minPolicyStartDate = policyStartDate;
					}

				}
				// termination date should be minimum policy date if termination date is before policy start date
				if (notNull(minPolicyStartDate) && minPolicyStartDate.isAfter(terminationDate)) {

					terminationDate = minPolicyStartDate;
				}

			}
			// account level delinquency
			else if (notNull(accountId)) {

				policyStartDate = fetchMinimumPolicyStartDate(accountId);
				if (notNull(policyStartDate) && policyStartDate.isAfter(terminationDate)) {

					terminationDate = policyStartDate;
				}
			}

			// Termination Date Characteristic created.
			DateFormat YYYY_MM_DD = new DateFormat("yyyy-MM-dd");
			// create termination date characteristic on delinquency process
			createDelinquencyProcessCharacteristic(cmDelinquencyProcess_Id, getTerminationDateCharType(), YYYY_MM_DD.format(terminationDate));

		}

	}
	/**
	 * Validates Business Object Status for corresponding Business Object.
	 * 
	 * @param Business Object Status
	 */
	private void validateBusObjStatus(String status) {
		// TODO Auto-generated method stub
		BusinessObjectStatus_Id busObjStatusId = new BusinessObjectStatus_Id(boInstance.getBusinessObject(), status);
		if (isNull(busObjStatusId.getEntity())) {
			addError(MessageRepository.invalidBusinessObjectStatus(status, boInstance.getBusinessObject().getId()));
		}

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
			if (isNull(new CharacteristicValue_Id(charType, charValue.trim()).getEntity())) {

				addError(MessageRepository.charValueIsInvalidForCharType(charType.getId(), charValue.trim()));
			}
		}
	}

	/**
	 * This method creates delinquency process characteristics
	 * @param delinquencyProcessId
	 * 		  charType
	 * 		  charValue
	 */
	private void createDelinquencyProcessCharacteristic(CmDelinquencyProcess_Id delinquencyProcessId, CharacteristicType charType, String charValue) {
		CmDelinquencyProcessCharacteristic_Id deliqProcCharTypeId = new CmDelinquencyProcessCharacteristic_Id(delinquencyProcessId, charType.getId(), getProcessDateTime().getDate());
		CmDelinquencyProcessCharacteristic_DTO charTypeDto = createDTO(CmDelinquencyProcessCharacteristic.class);
		charTypeDto.setId(deliqProcCharTypeId);

		if (charType.getCharacteristicType().isAdhocValue()) {
			charTypeDto.setAdhocCharacteristicValue(charValue.trim());
		} else if (charType.getCharacteristicType().isPredefinedValue()) {
			charTypeDto.setCharacteristicValue(charValue.trim());
		} else if (charType.getCharacteristicType().isForeignKeyValue()) {
			charTypeDto.setCharacteristicValueForeignKey1(charValue.trim());
		}

		charTypeDto.setSearchCharacteristicValue(charValue.trim());
		charTypeDto.newEntity();

	}
	/**
	 * This method determines termination date determining method 
	 * 
	 */
	private void determineTerminationDate() {
		// process if rule termination date rule is date processing
		if (terminationDateRule.trim().equalsIgnoreCase(getDateProcessingRuleCharVal().trim())) {
			processDateOfProcessingRule();
		}
		// process if rule termination date rule is end of grace rule
		if (terminationDateRule.trim().equalsIgnoreCase(getEndGraceRuleCharVal().trim())) {
			processEndOfGraceRule();
		}
		// process if rule termination date rule is retro paid rule
		if (terminationDateRule.trim().equalsIgnoreCase(getRetroPaidRuleCharVal().trim())) {
			processRetroPaidRule();
		}
		// process if rule termination date rule is current due date rule
		if (terminationDateRule.trim().equalsIgnoreCase(getCurrentDueDateRuleCharVal().trim())) {
			processCurrentDueDateRule();
		}
		// process if rule termination date rule is end of coverage period rule
		if (terminationDateRule.trim().equalsIgnoreCase(getEndCoveragePeriodRuleCharVal().trim())) {
			processEndOfCoveragePeriodRule();
		}
	}
	/**
	 * This method process date of processing rule 
	 * 
	 */
	private void processDateOfProcessingRule() {
		// retrieve trigger date
		Date triggerDate = retrieveTriggerDate();
		
		if (isNull(triggerDate)) {

			addError(MessageRepository.unableToRetrieveTriggerDate());

		} else {
			terminationDate = triggerDate;

			if (terminationData.getDateofProcessingNumberofDaysPrior().compareTo(BigInteger.ZERO) > 0) {

				terminationDate = triggerDate.addDays(terminationData.getDateofProcessingNumberofDaysPrior().negate().intValue());
			}
			// days to end of coverage should be added to termination date if available
			if (notNull(getAddDaysToEndOfCoverage()) && getAddDaysToEndOfCoverage().compareTo(BigInteger.ZERO) > 0) {

				terminationDate = terminationDate.addDays(getAddDaysToEndOfCoverage().intValue());
			}
		}

	}
	/**
	 * This method process end of grace rule 
	 * 
	 */
	private void processEndOfGraceRule() {

		Date endOfGraceDate = retrieveEndOfGraceDate();
		// throw error if end of grace date not retrieved
		if (isNull(endOfGraceDate)) {

			addError(MessageRepository.missingEOGCharType(getEndOfGraceCharType().toString()));
		}
		else {

			terminationDate = endOfGraceDate;
			// days to end of coverage should be added to termination date if available
			if (notNull(getAddDaysToEndOfCoverage()) && getAddDaysToEndOfCoverage().compareTo(BigInteger.ZERO) > 0) {

				terminationDate = terminationDate.addDays(getAddDaysToEndOfCoverage().intValue());
			}
		}

	}
	/**
	 * This method process retro paid rule 
	 * 
	 */
	private void processRetroPaidRule() {
		String billId = "";
		Date dueDate = null;
		SQLResultRow result = null;
		BigDecimal totallFtAmount = BigDecimal.ZERO;
		BigDecimal totalUnpaidAmount = BigDecimal.ZERO;
		BigDecimal unpaidPercentage = BigDecimal.ZERO;
		Bool unpaidBillFoundSwitch = Bool.FALSE;
		BigDecimal hundred = new BigDecimal(100.00);
		Date minimumDueDate = null;
		Date policyStartDate = null;
		// retrieve open debt bills
		List<SQLResultRow> openDebtBillList = retriveLatestUnpaidBillList();
		if (notNull(openDebtBillList) && (openDebtBillList.size() > 0)) {
			Iterator<SQLResultRow> iterator = openDebtBillList.iterator();
			
			if (terminationData.getRetroPaidPercentageThreshold().compareTo(BigDecimal.ZERO) > 0) {

				while (iterator.hasNext() && unpaidBillFoundSwitch.isFalse()) {
					result = iterator.next();
					billId = result.getString("BILL_ID");
					dueDate = result.getDate("DUE_DT");
					totallFtAmount = result.getBigDecimal("TOTAL_FT_AMT");
					totalUnpaidAmount = result.getBigDecimal("TOTAL_UNPAID_AMT");

					if (isNull(minimumDueDate)) {

						minimumDueDate = dueDate;
					}
					// minimum date should be due date if it is before minimum due date
					else if (notNull(dueDate) && dueDate.isBefore(minimumDueDate)) {

						minimumDueDate = dueDate;
					}

					if (isNull(totalUnpaidAmount)) {

						unpaidPercentage = hundred;

					} else {
						try {
							unpaidPercentage = totalUnpaidAmount.divide(totallFtAmount).multiply(hundred);
						} catch (ArithmeticException ae) {

							addError(MessageRepository.arithmeticExpressionError(ae.getLocalizedMessage()));
						}
					}
					if (unpaidPercentage.compareTo(terminationData.getRetroPaidPercentageThreshold()) > 0) {

						unpaidBillFoundSwitch = Bool.TRUE;
					}

				}

			} else {

				minimumDueDate = openDebtBillList.get(0).getDate("DUE_DT");
				unpaidBillFoundSwitch = Bool.TRUE;
			}
		}

		if (unpaidBillFoundSwitch.isFalse()) {

			addError(MessageRepository.noUnpaidBillRetrieved(cmDelinquencyProcess_Id.getIdValue()));
		}
		else {
			terminationDate = minimumDueDate.addDays(-1);
			if (notNull(getAddDaysToEndOfCoverage()) && getAddDaysToEndOfCoverage().compareTo(BigInteger.ZERO) > 0) {

				terminationDate = terminationDate.addDays(getAddDaysToEndOfCoverage().intValue());
			}
			// account level delinquency
			if (notNull(accountId)) {
				policyStartDate = retrieveActivePolicyStartDate(accountId.getEntity());
				if (notNull(policyStartDate) && policyStartDate.isAfter(terminationDate)) {

					terminationDate = policyStartDate;
				}
			// customer level delinquency
			} else if (notNull(customerAccountList) && (customerAccountList.size() > 0)) {
				Date minPolicyStartDate = null;
				for (Account_Id acctId : customerAccountList) {
					// fetch policy start date
					policyStartDate = fetchMinimumPolicyStartDate(acctId);
					if (isNull(minPolicyStartDate)) {

						minPolicyStartDate = policyStartDate;
					}
					else if ((notNull(policyStartDate) && policyStartDate.isBefore(minPolicyStartDate))) {

						minPolicyStartDate = policyStartDate;
					}

				}
				if (notNull(minPolicyStartDate) && minPolicyStartDate.isAfter(terminationDate)) {

					terminationDate = minPolicyStartDate;
				}

			}

		}
	}
	/**
	 * This method process current due date rule 
	 * 
	 */
	private void processCurrentDueDateRule() {
		// retrieve current date
		Date currentDueDate = retriveLatestDueDate();

		if (isNull(currentDueDate)) {

			addError(MessageRepository.noUnpaidBillRetrieved(cmDelinquencyProcess_Id.getIdValue()));
		}
		else {

			terminationDate = currentDueDate.addDays(-1);

			if (notNull(getAddDaysToEndOfCoverage()) && getAddDaysToEndOfCoverage().compareTo(BigInteger.ZERO) > 0) {

				terminationDate = terminationDate.addDays(getAddDaysToEndOfCoverage().intValue());
			}
		}

	}
	/**
	 * This method process end of coverage period rule 
	 * 
	 */
	private void processEndOfCoveragePeriodRule() {
		// retrieve end of grace date
		Date endOfGraceDate = retrieveEndOfGraceDate();
		if (isNull(endOfGraceDate)) {

			addError(MessageRepository.missingEOGCharType(getEndOfGraceCharType().toString()));

		} else {
			// account level delinquency
			if (notNull(accountId)) {

				terminationDate = retriveMaximumCoverageEndDateBilled(accountId, endOfGraceDate);

			} 
			// customer level delinquency
			else if (notNull(customerAccountList) && (customerAccountList.size() > 0)) {
				Date maxCoverageEndDate = null;
				Date termDate = null;
				for (Account_Id acctId : customerAccountList) {
					// retrieve latest coverage end date
					maxCoverageEndDate = retriveMaximumCoverageEndDateBilled(acctId, endOfGraceDate);

					if (isNull(termDate)) {

						termDate = maxCoverageEndDate;

					} else if (maxCoverageEndDate.isAfter(termDate)) {

						termDate = maxCoverageEndDate;
					}

				}
				// termination date should be maximum coverage end date
				terminationDate = termDate;
			}

		}

	}

	/**
	 * This method fetch minimum policy start date from account
	 * @param account
	 * @return
	 */
	private Date fetchMinimumPolicyStartDate(Account_Id acctId) {
		Date startDate = null;
		PreparedStatement preparedStatement = createPreparedStatement(CmOverdueProcessConstants.RETRIEVE_MIN_POLICIES_START_DT.toString(), "CmDetermineTerminationDateEnterStatusAlgComp_Impl");
		preparedStatement.bindId("account", acctId);
		preparedStatement.bindDate("terminationDate", getProcessDateTime().getDate());
		preparedStatement.bindEntity("mbrshipCharType", getMembershipCharType());
		preparedStatement.bindString("activeStatus", getPolicyActiveStatus(), "BO_STATUS_CD");
		preparedStatement.bindString("terminatedStatus", getPolicyTerminatedStatus(), "BO_STATUS_CD");
		SQLResultRow sqlResultRow = preparedStatement.firstRow();
		if (notNull(sqlResultRow)) {

			startDate = sqlResultRow.getDate("START_DT");
		}
		preparedStatement.close();
		return startDate;
	}

	/**
	 * Get Account List of Customers Bills on Delinquency Process
	 * @return accountId
	 */

	private List<Account_Id> getCustomerAccountList() {

		List<String> billList = null;
		List<Account_Id> accountList = new ArrayList<Account_Id>();
		Bill_Id billId = null;
		Account_Id accountId = null;

		Query<String> hqlQuery = createQuery(CmDelinquencyCustomerMonitorRuleConstants.DETERMINE_DEL_LVL.toString(), "CmDetermineTerminationDateEnterStatusAlgComp_Impl");
		hqlQuery.bindId("delinProcId", cmDelinquencyProcess_Id);
		hqlQuery.bindLookup("relObjTypeFlg", CmDelinquencyRelatedObjTypeLookup.constants.CM_COLLECTING_ON);
		hqlQuery.addResult("PK_VALUE1", "DPRO.id.primaryKeyValue1");
		billList = hqlQuery.list();

		for (String bill : billList) {

			billId = new Bill_Id(bill);
			if (notNull(billId)) {

				accountId = billId.getEntity().getAccount().getId();
			}

			if (!accountList.contains(accountId)) {

				accountList.add(accountId);
			}

		}
		return accountList;
	}
	
	/**
	 * This method retrieves Maximum Coverage End Date 
	 * 
	 * @return trigger date
	 */

	private Date retrieveTriggerDate() {
		Date triggerDate = null;
		PreparedStatement preparedStatement = createPreparedStatement(CmDetermineTerminationDateRuleConstants.RET_TRIG_DATE.toString(), "CmDetermineTerminationDateEnterStatusAlgComp_Impl");
		preparedStatement.bindId("delinquencyProcessId", cmDelinquencyProcess_Id);
		preparedStatement.bindString("boStatusTermRequest", getBoStatusTerminationRequest().trim(), "BO_STATUS_CD");
		preparedStatement.bindString("busObjCdDelinqProcess", cmDelinquencyProcess_Id.getEntity().getBusinessObject().getId().getIdValue(), "BUS_OBJ_CD");

		SQLResultRow sqlResultRow = preparedStatement.firstRow();
		if (notNull(sqlResultRow)) {

			triggerDate = sqlResultRow.getDate("TRIGGER_DT");
		}
		preparedStatement.close();
		return triggerDate;
	}

	/**
	 * This method retrieve End of Grace value from the Delinquency Process logs, using the End Of Grace Characteristic Type 
	 * @param 
	 * @return
	 */
	private Date retrieveEndOfGraceDate() {
		Date endOfGraceDate = null;
		CmDelinquencyProcessCharacteristic delinquencyProcessChar = null;
		DateFormat sourceFormater = new DateFormat("yyyy-MM-dd");
		String endOfGrace = null;
		delinquencyProcessChar = cmDelinquencyProcess_Id.getEntity().getEffectiveCharacteristic(getEndOfGraceCharType());

		if (notNull(delinquencyProcessChar))
		{
			if (delinquencyProcessChar.fetchIdCharacteristicType().getCharacteristicType().isAdhocValue()) {
				endOfGrace = delinquencyProcessChar.getAdhocCharacteristicValue();
			} else if (delinquencyProcessChar.fetchIdCharacteristicType().getCharacteristicType().isPredefinedValue()) {
				endOfGrace = delinquencyProcessChar.getCharacteristicValue();
			} else if (delinquencyProcessChar.fetchIdCharacteristicType().getCharacteristicType().isForeignKeyValue()) {
				endOfGrace = delinquencyProcessChar.getCharacteristicValueForeignKey1();
			}

		}
		
		
		try {
		
			if(!isBlankOrNull(endOfGrace)){
			
				endOfGraceDate = sourceFormater.parseDate(endOfGrace.trim());
			}
		} catch (DateFormatParseException e) {
			
			e.printStackTrace();
		}

		return endOfGraceDate;
	}
	/**
	 * This method retrieves Latest Date 
	 * 
	 * @param 
	 * @return due date
	 */
	private Date retriveLatestDueDate() {
		Date latestDueDate = null;
		PreparedStatement preparedStatement = createPreparedStatement(CmDetermineTerminationDateRuleConstants.RET_CURRENT_BILL_DUE_DATE.toString(), "CmDetermineTerminationDateEnterStatusAlgComp_Impl");
		preparedStatement.bindId("delinquencyProcId", cmDelinquencyProcess_Id);
		preparedStatement.bindLookup("collectingObjTypeFlg", CmDelinquencyRelatedObjTypeLookup.constants.CM_COLLECTING_ON);
		preparedStatement.bindLookup("openFlg", MatchEventStatusLookup.constants.OPEN);
		SQLResultRow sqlResultRow = preparedStatement.firstRow();
		if (notNull(sqlResultRow)) {

			latestDueDate = sqlResultRow.getDate("DUE_DT");
		}
		preparedStatement.close();
		return latestDueDate;
	}
	/**
	 * This method retrieves Unpaid Bill List 
	 * 
	 * @param
	 * @return billList
	 */
	private List<SQLResultRow> retriveLatestUnpaidBillList() {
		List<SQLResultRow> billList = null;
		PreparedStatement preparedStatement = createPreparedStatement(CmDetermineTerminationDateRuleConstants.RET_CURRENT_BILL_DUE_DATE_RETRO.toString(), "CmDetermineTerminationDateEnterStatusAlgComp_Impl");
		preparedStatement.bindId("delinquencyProcId", cmDelinquencyProcess_Id);
		preparedStatement.bindLookup("collectingObjTypeFlg", CmDelinquencyRelatedObjTypeLookup.constants.CM_COLLECTING_ON);
		preparedStatement.bindLookup("openFlg", MatchEventStatusLookup.constants.OPEN);
		billList = preparedStatement.list();

		preparedStatement.close();
		return billList;
	}

	/**
	 * This method retrieves Maximum Coverage End Date 
	 * 
	 * @param endOfGrace
	 * @return
	 */
	private Date retriveMaximumCoverageEndDateBilled(Account_Id acctId, Date endOfGrace) {
		Query<Date> query = createQuery(CmOverdueProcessConstants.FETCH_MAX_COV_END_DATE.toString(), "CmDetermineTerminationDateEnterStatusAlgComp_Impl");
		query.bindId("accountId", acctId);
		query.bindLookup("billSegment", FinancialTransactionTypeLookup.constants.BILL_SEGMENT);
		query.bindLookup("billCancellation", FinancialTransactionTypeLookup.constants.BILL_CANCELLATION);
		query.bindDate("endOfGrace", endOfGrace);
		query.addResult("maxCovEndDate", "max(cmft.endDate)");
		return query.firstRow();
	}

	private Date retrieveActivePolicyStartDate(Account account) {
		Date startDate = null;
		Date earliestStartDate = null;
		PreparedStatement preparedStatement = createPreparedStatement(CmDetermineTerminationDateRuleConstants.RETRIEVE_ACTIVE_POLICIES.toString(), "CmDetermineTerminationDateEnterStatusAlgComp_Impl");
		preparedStatement.bindId("account", account.getId());
		preparedStatement.bindDate("terminationDate", terminationDate);
		preparedStatement.bindString("activeStatus", getPolicyActiveStatus(), "BO_STATUS_CD");
		preparedStatement.bindString("terminatedStatus", getPolicyTerminatedStatus(), "BO_STATUS_CD");

		if (notNull(preparedStatement.list())) {
			SQLResultRow sqlResultRow = null;
			List<SQLResultRow> sqlResultRowList = preparedStatement.list();
			Iterator<SQLResultRow> iterator = sqlResultRowList.iterator();
			while (iterator.hasNext()) {
				sqlResultRow = (SQLResultRow) iterator.next();
				startDate = sqlResultRow.getDate("START_DT");
				if (isNull(earliestStartDate)) {
					earliestStartDate = startDate;
				} else if (notNull(startDate) && earliestStartDate.isAfter(startDate)) {
					earliestStartDate = startDate;
				}

			}

		}
		preparedStatement.close();
		return earliestStartDate;
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

