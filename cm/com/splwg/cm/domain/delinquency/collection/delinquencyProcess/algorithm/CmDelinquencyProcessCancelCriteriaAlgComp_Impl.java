/*                                                                
 ********************************************************************************************                                                                
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Delinquency Calculation
 * This algorithm is designed to calculate trigger events and add
 * them to trigger events table.
 *                                                             
 ********************************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.               
 * 2020-05-17	MugdhaP		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework       
 * ******************************************************************************************
 */

package com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.Query;
import com.splwg.base.api.QueryResultRow;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.lookup.YesNoOptionLookup;
import com.splwg.base.domain.common.algorithm.Algorithm;
import com.splwg.base.domain.common.algorithm.AlgorithmComponentCache;
import com.splwg.base.domain.common.algorithmType.AlgorithmTypeParameter_Id;
import com.splwg.base.domain.common.businessObjectStatusReason.BusinessObjectStatusReason_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicEntity;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.maintenanceObject.MaintenanceObject;
import com.splwg.base.domain.common.maintenanceObject.MaintenanceObject_Id;
import com.splwg.ccb.domain.admin.cisDivision.CisDivision_Id;
import com.splwg.ccb.domain.admin.collectionClass.CollectionClass_Id;
import com.splwg.ccb.domain.billing.bill.Bill;
import com.splwg.ccb.domain.billing.bill.Bill_Id;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.cm.api.lookup.CharacteristicEntityLookup;
import com.splwg.cm.api.lookup.CmDelinquencyRelatedObjTypeLookup;
import com.splwg.cm.domain.delinquency.admin.delinquencyCustomerMonitor.CmDelinquencyCustomerMonitorRuleConstants;
import com.splwg.cm.domain.delinquency.admin.delinquencyCustomerMonitor.CmDelinquencyProcessCalculateTriggerDateConstants;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmCalculateUnpaidOriginalAmountAlgorithmSpot;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmDelinquencyProcessCancelCriteriaAlgorithmSpot;
import com.splwg.cm.domain.delinquency.admin.overdueEventType.CmOverdueEventTypeHelper;
import com.splwg.cm.domain.delinquency.admin.overdueEventType.CmTerminationData;
import com.splwg.cm.domain.delinquency.admin.overdueProcessTemplate.CmOverdueProcessHelper;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessCharacteristic;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessRelatedObject_Id;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.cm.domain.delinquency.common.customBusinessEntity.CmPersonCollection_Id;
import com.splwg.cm.domain.delinquency.utils.CmPolicyHelper;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;

/**
 * @author MugdhaP
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (name = customerTemplateStatus, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = tolerancePercentage, type = decimal)
 *            , @AlgorithmSoftParameter (name = toleranceAmount, type = decimal)
 *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = toleranceAmountAndPercentageRequired, required = true, type = lookup)
 *            , @AlgorithmSoftParameter (lookupName = businessObjectStatusOptionType, name = policyBusinessObjectStatusOptionType, required = true, type = lookup)
 *            , @AlgorithmSoftParameter (name = activePolicyBusinessObjectOptionTypeValue, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = terminatedPolicyBusinessObjectOptionTypeValue, required = true, type = string)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = collectionClassCharacteristicType, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = graceDaysCharacteristicType, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = terminationDateRuleCharacteristicType, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = stateOfIssueCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = policyPlanCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = includeOnAccountPayments, type = lookup)
 *            , @AlgorithmSoftParameter (entityName = featureConfiguration, name = adminstrativeContractTypeFeatureConfig, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = customerManagementOption, name = adminstrativeContractTypeOptionType, type = lookup)
 *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = userCurrentRevenuePeriodBilled, type = lookup)
 *            , @AlgorithmSoftParameter (name = determinesAgeDate, type = string)
 *            , @AlgorithmSoftParameter (name = processBillsWithAge, type = decimal)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = endOfGraceDueDateCharacteristicType, required = true, type = entity)})
 */

public class CmDelinquencyProcessCancelCriteriaAlgComp_Impl extends CmDelinquencyProcessCancelCriteriaAlgComp_Gen implements CmDelinquencyProcessCancelCriteriaAlgorithmSpot {
	private CmDelinquencyProcess_Id delinquencyProcessId = null;
	private Bool okToCancelSwitch = Bool.FALSE;
	private Date latestDueDate = null;
	
	@Override
	public void setCmDelinquencyProcessId(CmDelinquencyProcess_Id delinquencyProcessId) {
		this.delinquencyProcessId = delinquencyProcessId;

	}

	@Override
	public Bool getOkToCancelSwitch() {
		return okToCancelSwitch;
	}
	
	@Override
	public BusinessObjectStatusReason_Id getBusinessObjectStatusReason() {
		return null;
	}

	@Override
	public void invoke() {

		CmDelinquencyProcess delinquencyProcess = delinquencyProcessId.getEntity();
		
		if(isNull(delinquencyProcess)){
			
			addError(MessageRepository.delinquencyProcessRequired());
		}
		
		CmDelinquencyProcessHelper deliqProcHelper = CmDelinquencyProcessHelper.Factory.newInstance();

		QueryResultRow resultRow = fetchLevelAndEntityForDelinquecnyProcess();

		MaintenanceObject maintenanceObject = resultRow.getEntity("maintenanceObject", MaintenanceObject.class);
		Person_Id personId = null;
		Account_Id accountId = null;

		if (maintenanceObject.getId().getTrimmedValue().equalsIgnoreCase(CmDelinquencyProcessCalculateTriggerDateConstants.MAINTENANCE_OBJ_PERSON)) {
			personId = new Person_Id(resultRow.getString("primaryKeyValue1"));

		} else if (maintenanceObject.getId().getTrimmedValue().equalsIgnoreCase(CmDelinquencyProcessCalculateTriggerDateConstants.MAINTENANCE_OBJ_ACCOUNT)) {
			accountId = new Account_Id(resultRow.getString("primaryKeyValue1"));
		}

		if ((isNull(personId) && isNull(accountId))) {
			addError(MessageRepository.deliquencyProcessCMDLReqd(delinquencyProcessId));
		}

		// check if customer is active

		Boolean isCustomerActive = determineCustomerStatus(personId, accountId);

		if (getCustomerTemplateStatus().trim().equalsIgnoreCase(CmDelinquencyProcessCancelCriteriaConstants.CONST_ACTIVE)) {
			if (isCustomerActive) {
				evaluateToleranceAmountAndPercentage(isCustomerActive, personId, accountId, deliqProcHelper);
			} else {
				okToCancelSwitch = Bool.TRUE;
			}
		} else if (getCustomerTemplateStatus().trim().equalsIgnoreCase(CmDelinquencyProcessCancelCriteriaConstants.CONST_CANCELLED)) {
			if (isCustomerActive) {
				okToCancelSwitch = Bool.TRUE;
			} else {
				evaluateToleranceAmountAndPercentage(isCustomerActive, personId, accountId, deliqProcHelper);
			}
		}
		if (okToCancelSwitch.isFalse() && notNull(getCollectionClassCharacteristicType())) {
			evaluateCollectionClassChange(personId, accountId);
		}

		if (okToCancelSwitch.isFalse() && notNull(getGraceDaysCharacteristicType())) {
			evaluateGraceDays(personId, accountId, deliqProcHelper);
		}

		if (okToCancelSwitch.isFalse() && notNull(getTerminationDateRuleCharacteristicType())) {
			evaluateTerminationDateRule();
		}		

	}

	/**
	 * This method evaluates termination date rule for customer related to delinquency process 
	 */
	private void evaluateTerminationDateRule() {
		CmDelinquencyProcessCharacteristic termDateRuleChar = delinquencyProcessId.getEntity().getEffectiveCharacteristic(getTerminationDateRuleCharacteristicType());

		// fetch termination Date rule for the deliq proc Id
		CmDetermineTerminationEffectiveDateRuleHelper termRuleHelper = CmDetermineTerminationEffectiveDateRuleHelper.Factory.newInstance();
		CmTerminationData termData = termRuleHelper.fetchTerminationEffectiveDateRule(delinquencyProcessId);
		String termDateRule = null;
		
		if(notNull(termData)){
			termDateRule = termData.getTerminationEffectiveRule();
		}

		if (notNull(termDateRuleChar) && notNull(termDateRule)) {
			if (!termDateRuleChar.getSearchCharacteristicValue().trim().equalsIgnoreCase(termDateRule.trim())) {
				okToCancelSwitch = Bool.TRUE;
			}
		}

	}

	/**
	 * This method evaluate collection grace days change for customer related to delinquency process 
	 * @param personId
	 * 			accountId
	 * deliqProcHelper
	 */
	private void evaluateGraceDays(Person_Id personId, Account_Id accountId, CmDelinquencyProcessHelper deliqProcHelper) {
		CmDelinquencyProcessCharacteristic graceDaysChar = delinquencyProcessId.getEntity().getEffectiveCharacteristic(getGraceDaysCharacteristicType());
		CmOverdueEventTypeHelper ovdEvtHelper = CmOverdueEventTypeHelper.Factory.newInstance();

		List<CisDivision_Id> stateOfIssueList = null;

		if (notNull(personId)) {
			// fetch list of stat of issue for customers
			stateOfIssueList = deliqProcHelper.fetchStateOIssueForMainCustomer(personId, this.latestDueDate, getPolicyPlanCharacteristicType(), getPolicyBusinessObjectStatusOptionType(),
					getStateOfIssueCharacteristicType(), getActivePolicyBusinessObjectOptionTypeValue().trim(), getTerminatedPolicyBusinessObjectOptionTypeValue().trim());

		} else if (notNull(accountId)) {

			// fetch list of stat of issue for customers
			stateOfIssueList = ovdEvtHelper.fetchPolicyStateOfIssue(accountId.getEntity(), this.latestDueDate, getStateOfIssueCharacteristicType(),
					getPolicyBusinessObjectStatusOptionType(), getActivePolicyBusinessObjectOptionTypeValue().trim(), getTerminatedPolicyBusinessObjectOptionTypeValue().trim(),
					getPolicyPlanCharacteristicType());
		}

		if(notNull(stateOfIssueList) && stateOfIssueList.size() != 0){
			BigInteger graceDaysState = ovdEvtHelper.retrieveMostLenientGraceDays(stateOfIssueList, this.latestDueDate, getGraceDaysCharacteristicType());

			if (notNull(graceDaysChar) && notNull(graceDaysState)) {

				if (graceDaysState.intValue() != Integer.parseInt(graceDaysChar.getSearchCharacteristicValue().trim())) {
					okToCancelSwitch = Bool.TRUE;
				}
			}
		} 		

	}

	/**
	 * This method evaluate collection class change for customer related to delinquency process 
	 * @param personId
	 * 			accountId
	 */
	private void evaluateCollectionClassChange(Person_Id personId, Account_Id accountId) {
		CmDelinquencyProcessCharacteristic collClassChar = delinquencyProcessId.getEntity().getEffectiveCharacteristic(getCollectionClassCharacteristicType());
		if (notNull(collClassChar)) {
			CollectionClass_Id collClassId = null;
			if (notNull(personId)) {
				CmPersonCollection_Id perCollId = new CmPersonCollection_Id(personId);
				if (notNull(perCollId.getEntity())) {
					
					collClassId = perCollId.getEntity().getCollectionClass().getId();
				}	
				
			} else if (notNull(accountId)) {
				collClassId = fetchCollectionClassForCustomer(accountId);
			}

			if (notNull(collClassId)) {
				if (!collClassChar.getCharacteristicValueForeignKey1().trim().equalsIgnoreCase(collClassId.getTrimmedValue())) {
					okToCancelSwitch = Bool.TRUE;
				}
			}

		}

	}

	/**
	 * This method fetches collection class for customer from account
	 * @param accountId
	 * @return CollectionClass_Id collClassId
	 */
	private CollectionClass_Id fetchCollectionClassForCustomer(Account_Id accountId) {
		CollectionClass_Id collClassId = null;

		Query<CollectionClass_Id> query = createQuery(CmDelinquencyProcessCalculateTriggerDateConstants.COLL_CLS_CUST.toString(), "CmDelinquencyProcessCancelCriteriaAlgComp_Impl");
		query.bindId("accountId", accountId);
		query.addResult("collectionClassId", "pc.collectionClass.id");

		collClassId = query.firstRow();

		return collClassId;
	}

	/**
	 * This method evaluate tolerance amount and percentage for customer related to delinquency process 
	 * @param isCustomerActive
	 * 		  personId
	 * 		  accountId
	 * 		  deliqProcHelper
	*/

	private void evaluateToleranceAmountAndPercentage(Boolean isCustomerActive, Person_Id personId, Account_Id accountId, CmDelinquencyProcessHelper deliqProcHelper) {
		BigDecimal totalCustomerOriginalAmount = BigDecimal.ZERO;
		BigDecimal totalCustomerUnpaidAmount = BigDecimal.ZERO;
		BigDecimal onAccountPayments = BigDecimal.ZERO;
		BigDecimal totalCustomerUnpaidPercentage = BigDecimal.ZERO;

		List<String> billList = null;
		String endOfGraceDueDateString = null;
		Date endOfGraceDueDate = null;
		if (isCustomerActive) {
			// Retrieve End of Grace Due Date characteristic from Delinquency Process
			endOfGraceDueDateString = retrieveDelinquencyProcessChar(getEndOfGraceDueDateCharacteristicType());
			// Set End of Grace Due Date
			if (!isBlankOrNull(endOfGraceDueDateString)) {
				endOfGraceDueDate = Date.fromIso(endOfGraceDueDateString.trim());
			}
		}
		// If End of Grace Due Date is provided retrieve bill list based on it.
		if (notNull(endOfGraceDueDate)) {
			billList = fetchRelatedBillObjectsBasedOnEOGDueDate(endOfGraceDueDate);
		}
		else {
			billList = fetchRelatedBillObjects();
		}	
		
		CmOverdueProcessHelper ovdProcHelper = CmOverdueProcessHelper.Factory.newInstance();

		for (String billId : billList) {

			Query<QueryResultRow> query = createQuery(
					CmDelinquencyCustomerMonitorRuleConstants.FETCH_DELINQ_PROC_TYP_ALG
							.toString(), "CmDelinquencyProcessCancelCriteriaAlgComp_Impl");
			query.bindEntity("delinquencyProcessType", delinquencyProcessId.getEntity().getCmDelinquencyProcessType());
			query.addResult("algorithm", "DPTA.algorithm");

			Algorithm algorithm = (Algorithm) query.firstRow();

			if (notNull(algorithm)) {
				CmCalculateUnpaidOriginalAmountAlgorithmSpot algorithmSpot = AlgorithmComponentCache
						.getAlgorithmComponent(algorithm.getId(), CmCalculateUnpaidOriginalAmountAlgorithmSpot.class);

				algorithmSpot.setDelinquencyProcessId(delinquencyProcessId);

				MaintenanceObject_Id maintenanceObjectId = new MaintenanceObject_Id(CmDelinquencyCustomerMonitorRuleConstants.MAINTENANCE_OBJ_BILL);
				CmDelinquencyProcessRelatedObject_Id cmDelProcRelObjId = new CmDelinquencyProcessRelatedObject_Id(delinquencyProcessId, maintenanceObjectId,
						CmDelinquencyRelatedObjTypeLookup.constants.CM_COLLECTING_ON, billId);

				if (notNull(cmDelProcRelObjId.getEntity())) {

					algorithmSpot.setDelinquencyProcessRelatedObject(cmDelProcRelObjId.getEntity());
					algorithmSpot.invoke();

					totalCustomerOriginalAmount = totalCustomerOriginalAmount.add(algorithmSpot.getOriginalAmount());
					totalCustomerUnpaidAmount = totalCustomerUnpaidAmount.add(algorithmSpot.getUnpaidAmount());

					if (notNull(getUserCurrentRevenuePeriodBilled()) && getUserCurrentRevenuePeriodBilled().trimmedValue().equals(YesNoOptionLookup.constants.YES.trimmedValue())
							&& isCustomerActive.booleanValue()) {

						//determine bill age
						long daysDiff = 0L;
						
						Bill bill = new Bill_Id(billId).getEntity();

						if (getDeterminesAgeDate().equals(CmDelinquencyProcessCancelCriteriaConstants.CONST_B)) {

							daysDiff = getProcessDateTime().getDate().difference(bill.getBillDate()).getTotalDays();

						} else {
							daysDiff = getProcessDateTime().getDate().difference(bill.getDueDate()).getTotalDays();
						}

						BigDecimal billAge = new BigDecimal(daysDiff);

						if (billAge.compareTo(getProcessBillsWithAge()) >= 0) {
							if (isNull(this.latestDueDate) || (this.latestDueDate.isBefore(bill.getDueDate()))) {
								this.latestDueDate = bill.getDueDate();
							}
						}

					}

				}
			}

		}

		if (notNull(getIncludeOnAccountPayments()) && getIncludeOnAccountPayments().trimmedValue().equals(YesNoOptionLookup.constants.YES.trimmedValue())) {
			onAccountPayments = calculateOnAccountPayments(personId, accountId, deliqProcHelper, ovdProcHelper);

			totalCustomerUnpaidAmount = totalCustomerUnpaidAmount.subtract(onAccountPayments);
		}

		if (notNull(getUserCurrentRevenuePeriodBilled()) && getUserCurrentRevenuePeriodBilled().trimmedValue().equals(YesNoOptionLookup.constants.YES.trimmedValue())
				&& isCustomerActive.booleanValue() && notNull(this.latestDueDate)) {
			
			totalCustomerOriginalAmount = fetchTotalBilledAmountForRevenuePeriod(personId, accountId, this.latestDueDate, deliqProcHelper, ovdProcHelper);			
		}

		if (totalCustomerOriginalAmount.compareTo(BigDecimal.ZERO) == 1) {

			try {
				totalCustomerUnpaidPercentage = (totalCustomerUnpaidAmount.divide(totalCustomerOriginalAmount)).multiply(new BigDecimal(100));

			} catch (ArithmeticException ae) {

				addError(MessageRepository.arithmeticExpressionError(ae.getLocalizedMessage()));
			}

		}

		if (notNull(getToleranceAmountAndPercentageRequired()) && getToleranceAmountAndPercentageRequired().trimmedValue().equals(YesNoOptionLookup.constants.YES.trimmedValue())) {

			if ((totalCustomerUnpaidAmount.compareTo(getToleranceAmount()) <= 0) && (totalCustomerUnpaidPercentage.compareTo(getTolerancePercentage()) <= 0)) {

				okToCancelSwitch = Bool.TRUE;
			} else {
				okToCancelSwitch = Bool.FALSE;
			}
		} else {
			
				okToCancelSwitch = Bool.FALSE;
			if(notNull(getToleranceAmount()) && (totalCustomerUnpaidAmount.compareTo(getToleranceAmount()) <= 0)){
				okToCancelSwitch = Bool.TRUE;
			}
			
			if(notNull(getTolerancePercentage()) && (totalCustomerUnpaidPercentage.compareTo(getTolerancePercentage()) <= 0)){
				okToCancelSwitch = Bool.TRUE;
			}


			//if ((totalCustomerUnpaidAmount.compareTo(getToleranceAmount()) <= 0) || (totalCustomerUnpaidPercentage.compareTo(getTolerancePercentage()) <= 0)) {
			//
			//	okToCancelSwitch = Bool.TRUE;
			//} else {
			//	okToCancelSwitch = Bool.FALSE;
			//}

		}

	}

	private BigDecimal fetchTotalBilledAmountForRevenuePeriod(Person_Id personId, Account_Id accountId, Date latestDueDate2,
			CmDelinquencyProcessHelper deliqProcHelper, CmOverdueProcessHelper ovdProcHelper) {
		
		BigDecimal totalCustomerOriginalAmount = BigDecimal.ZERO;
		
		if(notNull(personId)){
			totalCustomerOriginalAmount = deliqProcHelper.fetchTotalBilledAmtForCurrRevPeriodForCustomer(personId, this.latestDueDate);
		}else if(notNull(accountId)){
			totalCustomerOriginalAmount = ovdProcHelper.fetchTotalBilledAmountForCurrentRevenuePeriod(latestDueDate, accountId.getEntity());
		}
				
				
		return totalCustomerOriginalAmount;
	}

	/**
	 * This method calculates on account payments for customer related to delinquency process 
	 * @param personId
	 * 		  accountId
	 * 		  deliqProcHelper
	 * @param ovdProcHelper 
	 * @return BigDecimal onAccountPayments
	 */

	private BigDecimal calculateOnAccountPayments(Person_Id personId, Account_Id accountId, CmDelinquencyProcessHelper deliqProcHelper, CmOverdueProcessHelper ovdProcHelper) {
		BigDecimal onAccountPayments = BigDecimal.ZERO;

		if (notNull(accountId)) {
			
			onAccountPayments = ovdProcHelper.fetchOnAccountpayments(accountId.getEntity(),
					getAdminstrativeContractTypeFeatureConfig(), getAdminstrativeContractTypeOptionType());

		} else if (notNull(personId)) {

			onAccountPayments = deliqProcHelper.fetchOnAccountPaymentsForPerson(personId.getEntity(), getAdminstrativeContractTypeFeatureConfig(), getAdminstrativeContractTypeOptionType());
		}
		return onAccountPayments;
	}

	/**
	 * This method fetches bills related to delinquency process
	 * @return List<String> 
	 */
	private List<String> fetchRelatedBillObjects() {
		Query<String> query = createQuery(CmDelinquencyProcessCancelCriteriaConstants.FETCH_BILL_LIST.toString(), "CmDelinquencyProcessCancelCriteriaAlgComp_Impl");
		query.bindId("delinProcId", delinquencyProcessId);
		query.bindLookup("relatedObjTypeFlag", CmDelinquencyRelatedObjTypeLookup.constants.CM_COLLECTING_ON);
		query.addResult("billId", "dpo.id.primaryKeyValue1");

		return query.list();
	}


	/**
	 * This method fetches bills related to delinquency process
	 * @param endOfGraceDueDate
	 * @return List<String> 
	 */
	private List<String> fetchRelatedBillObjectsBasedOnEOGDueDate(Date endOfGraceDueDate) {
		Query<String> query = createQuery(CmDelinquencyProcessCancelCriteriaConstants.FETCH_BILL_LIST_BASED_ON_DUE_DT.toString(), "CmDelinquencyProcessCancelCriteriaAlgComp_Impl");
		query.bindId("delinProcId", delinquencyProcessId);
		query.bindLookup("relatedObjTypeFlag", CmDelinquencyRelatedObjTypeLookup.constants.CM_COLLECTING_ON);
		query.bindDate("endOfGraceDueDate", endOfGraceDueDate);
		query.addResult("billId", "dpo.id.primaryKeyValue1");

		return query.list();
	}

	
	/**
	 * This method determines status of customer related to delinquency process
	 * @param delinquencyProcessId
	 * 			deliqAccountId
	 * @return Boolean status 
	 */
	private Boolean determineCustomerStatus(Person_Id deliqPersonId, Account_Id deliqAccountId) {

		CmPolicyHelper policyHelper = CmPolicyHelper.Factory.newInstance();

		Person_Id personId = null;

		if (notNull(deliqPersonId)) {
			personId = deliqPersonId;
		} else if (notNull(deliqAccountId)) {

			Query<Person_Id> query = createQuery(CmDelinquencyProcessCancelCriteriaConstants.FETCH_ACCT_PER.toString(), "CmDelinquencyProcessCancelCriteriaAlgComp_Impl");
			query.bindId("accountId", deliqAccountId);
			query.addResult("personId", "ap.id.person.id");

			personId = query.firstRow();
		}

		Boolean status = policyHelper.isActiveCustomer(personId, getProcessDateTime().getDate(), getPolicyBusinessObjectStatusOptionType().trimmedValue(),
				getActivePolicyBusinessObjectOptionTypeValue());

		return status;

	}

	/**
	 * This method fetches level and maintenance object for delinquencyProcessId
	 * @param delinquencyProcessId
	 * @return QueryResultRow resultRow
	 */
	private QueryResultRow fetchLevelAndEntityForDelinquecnyProcess() {

		Query<QueryResultRow> query = createQuery(CmDelinquencyProcessCalculateTriggerDateConstants.FETCH_DELIQ_LVL_ID.toString(), "CmDelinquencyProcessCancelCriteriaAlgComp_Impl");
		query.bindId("delinProcId", delinquencyProcessId);
		query.bindLookup("relatedObjTypeFlag", CmDelinquencyRelatedObjTypeLookup.constants.CM_DELINQUENCY_LEVEL);
		query.addResult("maintenanceObject", "dpo.id.maintenanceObject");
		query.addResult("primaryKeyValue1", "dpo.id.primaryKeyValue1");

		QueryResultRow resultRow = query.firstRow();

		return resultRow;

	}

	@Override
	protected void extraSoftParameterValidations(boolean forAlgorithmValidation) {


		AlgorithmTypeParameter_Id algTypeParam = null;

				
		if (!(getCustomerTemplateStatus().equals(CmDelinquencyProcessCancelCriteriaConstants.CONST_ACTIVE)
		|| getCustomerTemplateStatus().equals(CmDelinquencyProcessCancelCriteriaConstants.CONST_CANCELLED))) {

			addError(MessageRepository.invalidAlgorithmParmValues(getAlgorithm().getAlgorithmType().getParameterAt(10).fetchLanguageParameterLabel(),
					CmDelinquencyProcessCancelCriteriaConstants.CONST_ACTIVE_CANCELLED));
		}


		if (notNull(getToleranceAmountAndPercentageRequired())){
			if(getToleranceAmountAndPercentageRequired().trimmedValue().equals(YesNoOptionLookup.constants.YES.trimmedValue())) {
				if(isNull(getTolerancePercentage())){
					reportRequiredParameter("tolerancePercentage", 1);
				}
				
				if(isNull(getToleranceAmount())){
					reportRequiredParameter("toleranceAmount", 2);
				}
			}else if(getToleranceAmountAndPercentageRequired().trimmedValue().equals(YesNoOptionLookup.constants.NO.trimmedValue())){
				if(isNull(getToleranceAmount()) && isNull(getTolerancePercentage()) ){
					
					algTypeParam = new AlgorithmTypeParameter_Id(getAlgorithm().getAlgorithmType(), new BigInteger("3"));
					String unpaidAmountLbl = algTypeParam.getEntity().fetchLanguageParameterLabel();
					
					algTypeParam = new AlgorithmTypeParameter_Id(getAlgorithm().getAlgorithmType(), new BigInteger("2"));
					String unpaidPercentLbl = algTypeParam.getEntity().fetchLanguageParameterLabel();
					
					addError(MessageRepository.atleastOneParameterShouldBeProvided(unpaidAmountLbl, unpaidPercentLbl));
				}
			}
			
			
		}

		if(notNull(getTolerancePercentage())){

			if (!(getTolerancePercentage().compareTo(BigDecimal.ZERO) >= 0)
					|| !(getTolerancePercentage().compareTo(new BigDecimal(100)) <= 0)) {
	
				addError(MessageRepository
						.invalidProcessBillWithUnpaidPer(CmDelinquencyProcessCancelCriteriaConstants.TOLRNC_PRCNT));
	
			}

		}


		if(notNull(getCollectionClassCharacteristicType())){
	
			if (!hasValidCharTypeEntity(getCollectionClassCharacteristicType(), CharacteristicEntityLookup.constants.CM_DELINQUENCY_PROCESS.trimmedValue())) {
				addError(MessageRepository.invalidCharTypeToEntity(getCollectionClassCharacteristicType().getId().getIdValue(),
						CharacteristicEntityLookup.constants.CM_DELINQUENCY_PROCESS.trimmedValue()));
			}
	
		}

		if(notNull(getGraceDaysCharacteristicType())){
		
			if (!hasValidCharTypeEntity(getGraceDaysCharacteristicType(), CharacteristicEntityLookup.constants.CM_DELINQUENCY_PROCESS.trimmedValue())) {
				addError(MessageRepository.invalidCharTypeToEntity(getGraceDaysCharacteristicType().getId().getIdValue(), CharacteristicEntityLookup.constants.CM_DELINQUENCY_PROCESS.trimmedValue()));
			}

		}
	

		if(notNull(getTerminationDateRuleCharacteristicType())){
		
			if (!hasValidCharTypeEntity(getTerminationDateRuleCharacteristicType(), CharacteristicEntityLookup.constants.CM_DELINQUENCY_PROCESS.trimmedValue())) {
				addError(MessageRepository.invalidCharTypeToEntity(getTerminationDateRuleCharacteristicType().getId().getIdValue(),
						CharacteristicEntityLookup.constants.CM_DELINQUENCY_PROCESS.trimmedValue()));
			}

		}
	

		if (!hasValidCharTypeEntity(getStateOfIssueCharacteristicType(), CharacteristicEntityLookup.constants.POLICY.trimmedValue())) {
			addError(MessageRepository.invalidCharTypeToEntity(getStateOfIssueCharacteristicType().getId().getIdValue(), CharacteristicEntityLookup.constants.POLICY.trimmedValue()));
		}

		if (!hasValidCharTypeEntity(getStateOfIssueCharacteristicType(), CharacteristicEntityLookup.constants.POLICY_PLAN.trimmedValue())) {
			addError(MessageRepository.invalidCharTypeToEntity(getStateOfIssueCharacteristicType().getId().getIdValue(), CharacteristicEntityLookup.constants.POLICY_PLAN.trimmedValue()));
		}
		
		if (!hasValidCharTypeEntity(getPolicyPlanCharacteristicType(), CharacteristicEntityLookup.constants.SERVICE_AGREEMENT.trimmedValue())) {
			addError(MessageRepository.invalidCharTypeToEntity(getPolicyPlanCharacteristicType().getId().getIdValue(),
					CharacteristicEntityLookup.constants.SERVICE_AGREEMENT.trimmedValue()));
		}
		

		if (!hasValidCharTypeEntity(getEndOfGraceDueDateCharacteristicType(), CharacteristicEntityLookup.constants.CM_DELINQUENCY_PROCESS.trimmedValue())) {
			addError(MessageRepository.invalidCharTypeToEntity(getEndOfGraceDueDateCharacteristicType().getId().getIdValue(),
					CharacteristicEntityLookup.constants.CM_DELINQUENCY_PROCESS.trimmedValue()));
		}


		if (notNull(getIncludeOnAccountPayments()) && getIncludeOnAccountPayments().trimmedValue().equals(YesNoOptionLookup.constants.YES.trimmedValue())) {
			if (isNull(getAdminstrativeContractTypeFeatureConfig()))
				reportRequiredParameter("adminstrativeContractTypeFeatureConfig", 15);
			if (isNull(getAdminstrativeContractTypeOptionType()))
				reportRequiredParameter("adminstrativeContractTypeOptionType", 16);

		}

		// Validate Use current Revenue period is Y or N
		if (notNull(getUserCurrentRevenuePeriodBilled()) && getIncludeOnAccountPayments().trimmedValue().equals(YesNoOptionLookup.constants.YES.trimmedValue())) {

			if (isBlankOrNull(getDeterminesAgeDate())) {
				reportRequiredParameter("determinesAgeDate", 18);
			}

			if (!(getDeterminesAgeDate().equals(CmDelinquencyProcessCancelCriteriaConstants.CONST_B) ||
					getDeterminesAgeDate().equals(CmDelinquencyProcessCancelCriteriaConstants.CONST_D))) {

				addError(MessageRepository.invalidAlgorithmParmValues(getAlgorithm().getAlgorithmType().getParameterAt(18).fetchLanguageParameterLabel(),
						CmDelinquencyProcessCancelCriteriaConstants.CONST_B_D));
			}

			if (isNull(getProcessBillsWithAge())) {
				reportRequiredParameter("processBillsWithAge", 19);
			}

		}

	}

	/**
	 * Check if valid char entity is attached to char type code input parameter
	 * @param charTypeCode -  String Characteristic Type code input parameter
	 * 		  entity - entity cd
	 * @return - boolean - true or false
	 */

	private Boolean hasValidCharTypeEntity(CharacteristicType characteristicType, String entity) {

		Iterator<CharacteristicEntity> iterator = characteristicType.getId()
				.getEntity().getEntities().iterator();

		while (iterator.hasNext()) {

			CharacteristicEntity charEntity = iterator.next();
			String charEntityValue = charEntity.getDTO().getId()
					.getCharacteristicEntity().getLookupValue().getId()
					.getFieldValue();

			if (!isNull(charEntityValue) && charEntityValue.trim().equals(entity)) {

				return true;
			}
		}

		return false;

	}


	/**
	 * This method will retrieve the Characteristic value of passed char type from Delinquency Process Chars 
	 * 
	 * @param delinProcId
	 * @return charType
	 */
	private String retrieveDelinquencyProcessChar(CharacteristicType charType) {
		String CharVal = null;
		CmDelinquencyProcessCharacteristic delinquencyProcessChar = null;
		if (notNull(delinquencyProcessId.getEntity())) {
			delinquencyProcessChar = delinquencyProcessId.getEntity().getEffectiveCharacteristic(charType);
		}
		if (notNull(delinquencyProcessChar)) {
			if (delinquencyProcessChar.fetchIdCharacteristicType().getCharacteristicType().isAdhocValue()) {
				CharVal = delinquencyProcessChar.getAdhocCharacteristicValue();
			} else if (delinquencyProcessChar.fetchIdCharacteristicType().getCharacteristicType().isPredefinedValue()) {
				CharVal = delinquencyProcessChar.getCharacteristicValue();
			} else if (delinquencyProcessChar.fetchIdCharacteristicType().getCharacteristicType().isForeignKeyValue()) {
				CharVal = delinquencyProcessChar.getCharacteristicValueForeignKey1();
			}
		}
		return CharVal;
	}

}

